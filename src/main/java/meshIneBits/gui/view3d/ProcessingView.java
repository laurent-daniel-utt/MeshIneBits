/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.gui.view3d;

import java.awt.geom.Area;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.SubWindow;
import meshIneBits.util.*;
import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PJOGL;

import remixlab.proscene.Scene;
import remixlab.dandelion.geom.Vec;

import static java.awt.event.KeyEvent.VK_SPACE;

/**
 * The 3D view of model loaded
 *
 * @author Nicolas
 *
 */
public class ProcessingView extends PApplet implements Observer, SubWindow {

	private Controller curVO = null;
	private HashMap<Position, PShape> shapeMap;

	private final int BACKGROUND_COLOR = color(150, 150, 150);
	private final int BIT_COLOR = color(219, 100, 50);
	private float printerX;
	private float printerY;
	private float printerZ;

	private Scene scene;

	private static ProcessingView currentInstance = null;
	private enum Mode { full, sliced };
	private Mode mode = Mode.sliced ;

	/**
	 *
	 * @param args input from CLI
	 */
	public static void startProcessingView(String[] args) {
		if (currentInstance == null)
			PApplet.main("meshIneBits.gui.view3d.ProcessingView");
	}

	/**
	 *
	 */
	public static void closeProcessingView() {
		if (currentInstance != null)
			currentInstance.destroyGLWindow();
	}

	/**
	 *
	 */
	private void destroyGLWindow() {
		((com.jogamp.newt.opengl.GLWindow) surface.getNative()).destroy();
	}

	/**
	 *
	 */
	public void settings() {
		size(640, 360, P3D);
		PJOGL.setIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/icon.png")).getPath());
		currentInstance = this;
	}

	public ProcessingView() {
		curVO = Controller.getInstance();
		curVO.addObserver(this);
	}

	/**
	 *
	 */
	private void setCloseOperation() {
		// Removing close listeners
		com.jogamp.newt.opengl.GLWindow win = ((com.jogamp.newt.opengl.GLWindow) surface.getNative());
		for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
			win.removeWindowListener(wl);
		}
		
		win.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);

		win.addWindowListener(new WindowAdapter() {
			public void windowDestroyed(WindowEvent e) {
				curVO.deleteObserver(currentInstance);
				dispose();
				currentInstance = null;
			}
		});

		win.addWindowListener(new WindowAdapter() {
			@Override
			public void windowResized(WindowEvent e) {
				super.windowResized(e);
				surface.setSize(win.getWidth(),win.getHeight());
			}
		});
	}

	/**
	 *
	 */
	public void setup() {

		surface.setTitle("MeshIneBits - 3D view");

		curVO = Controller.getInstance();
		curVO.addObserver(this);
		setCloseOperation();

		scene = new Scene(this);
		scene.eye().setPosition(new Vec(0, 1, 1));
		scene.eye().lookAt(scene.eye().sceneCenter());
		scene.setRadius(2500);
		scene.showAll();
		scene.disableKeyboardAgent();
		scene.toggleGridVisualHint();

		buildBits();
	}
	/**
	 *
	 */
	private void buildBits() {

		Logger.updateStatus("Start building 3D model");

		shapeMap = new HashMap<>();
		Vector<Layer> layers = curVO.getCurrentPart().getLayers();
		float bitThickness = (float) CraftConfig.bitThickness;
		float layersOffSet = (float) CraftConfig.layersOffset;

		PShape uncutBitPShape = getUncutBitPShape(bitThickness);

		float zLayer;
		int bitCount = 0;

		for (Layer curLayer : layers) {
			zLayer = curLayer.getLayerNumber() * (bitThickness + layersOffSet);
			for (Vector2 curBitKey : curLayer.getBits3dKeys()) {
				bitCount++;
				Bit3D curBit = curLayer.getBit3D(curBitKey);
				PShape bitPShape;
				// if(curBit.getCutPaths() == null)
				// bitPShape = uncutBitPShape;
				// else
				bitPShape = getBitPShapeFrom(curBit.getRawArea(), bitThickness);
				if (bitPShape != null) {
					bitPShape.setFill(BIT_COLOR);
					Vector2 curBitCenter = curBit.getOrigin();
					float curBitCenterX = (float) curBitCenter.x;
					float curBitCenterY = (float) curBitCenter.y;
					float[] translation = { curBitCenterX, curBitCenterY, zLayer };
					float rotation = (float) curBit.getOrientation().getEquivalentAngle2();
					Position curBitPosition = new Position(translation, rotation);
					shapeMap.put(curBitPosition, bitPShape);
				}
			}
		}

		Logger.updateStatus("3D model built : " + bitCount + " bits generated.");
	}

	/**
	 *
	 * @param extrudeDepth
	 * @return
	 */
	private PShape getUncutBitPShape(float extrudeDepth) {

		Vector2 cornerUpRight = new Vector2(+CraftConfig.bitLength / 2.0, -CraftConfig.bitWidth / 2.0);
		Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + CraftConfig.bitWidth);
		Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - CraftConfig.bitLength, cornerUpRight.y);
		Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - CraftConfig.bitLength, cornerDownRight.y);

		Vector<int[]> pointList = new Vector<>();
		pointList.add(new int[] { (int) cornerUpRight.x, (int) cornerUpRight.y, 0 });
		pointList.add(new int[] { (int) cornerDownRight.x, (int) cornerDownRight.y, 0 });
		pointList.add(new int[] { (int) cornerDownLeft.x, (int) cornerDownLeft.y, 0 });
		pointList.add(new int[] { (int) cornerUpLeft.x, (int) cornerUpLeft.y, 0 });

		PolygonPointsList poly = null;
		try {
			poly = new PolygonPointsList(pointList);
		} catch (Exception e) {
			System.out.println("Polygon point list exception");
			return null;
		}

		return extrude(new PolygonPointsList[] { poly, null }, (int) extrudeDepth);
	}

	/**
	 *
	 * @param bitArea
	 * @param extrudeDepth
	 * @return
	 */
	private PShape getBitPShapeFrom(Area bitArea, float extrudeDepth) {

		Vector<Segment2D> segmentList = AreaTool.getLargestPolygon(bitArea);
		if (segmentList == null)
			return null;

		Vector<int[]> pointList = new Vector<>();
		for (Segment2D s : segmentList) {
			pointList.add(new int[] { (int) Math.round(s.start.x), (int) Math.round(s.start.y), 0 });
			pointList.add(new int[] { (int) Math.round(s.end.x), (int) Math.round(s.end.y), 0 });
		}

		PolygonPointsList poly = null;
		try {
			poly = new PolygonPointsList(pointList);
		} catch (Exception e) {
			System.out.println("Polygon point list exception");
			return null;
		}

		return extrude(new PolygonPointsList[] { poly, null }, (int) extrudeDepth);
	}

	/**
	 *
	 */
	public void draw() {
		background(BACKGROUND_COLOR);
		lights();
		drawWorkspace();
		drawBits();
	}

	private void drawBits(){
		float bitThickness = (float) CraftConfig.bitThickness;
		float layersOffSet = (float) CraftConfig.layersOffset;

		int lNumber = curVO.getCurrentLayerNumber();
		float zLayer = 0;
		if (mode == Mode.sliced){
			zLayer = lNumber * (bitThickness + layersOffSet);
		}
		else if (mode == Mode.full){
			zLayer = (int) (curVO.getCurrentPart().getLayers().size() * (bitThickness + layersOffSet));
		}

		Vector3 v = curVO.getModel().getPos();
		for (Position p : shapeMap.keySet()) {
			pushMatrix();
			translate((float)v.x, (float)v.y, 0);
			float[] t = p.getTranslation();
			translate(t[0], t[1], t[2]);
			rotateZ(radians(p.getRotation()));

			PShape s = shapeMap.get(p);

			if (t[2] <= zLayer) {
				shape(s);
			}
			popMatrix();
		}
	}
	private void drawWorkspace() {
		try {
			File filename = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("meshIneBits/config/PrinterConfig.txt")).getPath());
			FileInputStream file = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(file));
			String strline;
			int linenumber = 0;
			while ((strline = br.readLine()) != null){
				linenumber++;
				br.skip(3);
				if (linenumber == 3){
					printerX = Float.valueOf(strline);
				}
				else if (linenumber == 4){
					printerY = Float.valueOf(strline);
				}
				else if (linenumber == 5){
					printerZ = Float.valueOf(strline);
				}

			}
			br.close();
			file.close();

		}
		catch(Exception e){
			System.out.println("Error :" + e.getMessage());
		}
		pushMatrix();
		noFill();

		stroke(255,255,0);
		translate(0,0,printerZ/2);
		box(printerX,printerY,printerZ);
		popMatrix();
		stroke(80);
		scene.pg().pushStyle();
		scene.pg().beginShape(LINES);
		for (int i = -(int)printerX/2; i <= printerX/2; i+=100) {
			vertex(i,printerY/2,0);
			vertex(i,-printerY/2,0);

		}
		for (int i = -(int)printerY/2; i <= printerY/2; i+=100) {
			vertex(printerX/2,i,0);
			vertex(-printerX/2,i,0);
		}
		scene.pg().endShape();
		scene.pg().popStyle();

	}

	public void keyPressed(){
		if (keyCode == VK_SPACE) {
			if (mode == Mode.sliced) {
				mode = Mode.full;
			}
			else if (mode == Mode.full) {
				mode = Mode.sliced;
			}
		}
	}
	/**
	 *
	 * @param pointA
	 * @param pointB
	 * @param z
	 * @return
	 */
	private PShape getFaceExtrude(int[] pointA, int[] pointB, int z) {
		PShape face = createShape();
		face.beginShape();
		face.vertex(pointA[0], pointA[1], pointA[2] + z);
		face.vertex(pointB[0], pointB[1], pointB[2] + z);
		face.vertex(pointB[0], pointB[1], pointB[2]);
		face.vertex(pointA[0], pointA[1], pointA[2]);
		face.endShape(CLOSE);

		return face;
	}

	/**
	 *
	 * @param poly
	 * @param z
	 * @return
	 */
	private PShape getSideExtrude(PolygonPointsList poly, int z) {

		PShape side = createShape(GROUP);

		int length = poly.getLength();
		int[] pointA = poly.getNextPoint();
		int[] pointB = poly.getNextPoint();

		for (int j = 0; j < length; j++) {
			side.addChild(getFaceExtrude(pointA, pointB, z));
			pointA = pointB;
			pointB = poly.getNextPoint();
		}

		return side;
	}

	/**
	 *
	 * @param poly
	 * @param z
	 * @return
	 */
	private PShape getPShape(PolygonPointsList[] poly, int z) {

		int length;
		int[] point;

		PShape myShape = createShape();
		myShape.beginShape();
		// Exterior path
		length = poly[0].getLength();
		for (int j = 0; j < length + 1; j++) {
			point = poly[0].getNextPoint();
			myShape.vertex(point[0], point[1], point[2] + z);
		}
		// Interior path
		if (poly[1] != null) {
			myShape.beginContour();
			length = poly[1].getLength();
			for (int j = 0; j < length + 1; j++) {
				point = poly[1].getNextPoint();
				myShape.vertex(point[0], point[1], point[2] + z);
			}
			myShape.endContour();
		}

		myShape.endShape();

		return myShape;
	}

	/**
	 * Work only for shape on the xy plan
	 *
	 */
	private PShape extrude(PolygonPointsList[] poly, int z) {

		PShape extrudedObject = createShape(GROUP);

		PShape exterior = getSideExtrude(poly[0], z);
		extrudedObject.addChild(exterior);

		if (poly[1] != null) {
			PShape holeSides = getSideExtrude(poly[1], z);
			extrudedObject.addChild(holeSides);
		}

		PShape topFace = getPShape(poly, 0);
		extrudedObject.addChild(topFace);
		PShape bottomFace = getPShape(poly, z);
		extrudedObject.addChild(bottomFace);

		return extrudedObject;
	}

	/**
	 *
	 */

	@Override
	public void update(Observable o, Object arg) {
		redraw();

	}

	@Override
	public void toggle() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCurrentMesh(Mesh mesh) {
		System.out.println("Processing view, setting current part" + mesh);
		curVO.setCurrentPart(mesh);
	}
}

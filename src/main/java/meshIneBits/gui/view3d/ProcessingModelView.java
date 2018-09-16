/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;

import javafx.util.Pair;
import meshIneBits.Mesh;
import meshIneBits.Model;
import meshIneBits.util.Vector3;
import meshIneBits.gui.SubWindow;

import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PJOGL;

import remixlab.dandelion.geom.Quat;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;
import remixlab.dandelion.geom.Vec;

import controlP5.*;

import static remixlab.bias.BogusEvent.CTRL;
import static remixlab.proscene.MouseAgent.*;

/**
 *
 * @author Nicolas
 *
 * 3D view + GUI
 * Use Builder to creates Meshes
 *
 */
public class ProcessingModelView extends PApplet implements Observer, SubWindow {

	private final int BACKGROUND_COLOR = color(150, 150, 150);
	private int height = 450;
	private int width = 800;
	private float printerX;
	private float printerY;
	private float printerZ;

	private static boolean visible = false;
	private static boolean initialized = false;

	private Builder builder;
	private static ProcessingModelView currentInstance = null;
	private static Model MODEL;
	private static Controller controller = null;

	private PShape shape;
	private Vector<Pair<Position, PShape>> shapeMap = null;
	private Scene scene;
	private InteractiveFrame frame;
	private ControlP5 cp5;
	private Textlabel txt;
	private Textlabel modelSize;
	private Textarea tooltipGrav;
	private Textarea tooltipReset;
	private Textarea tooltipCamera;
	private Textarea tooltipApply;
	private Toggle toggleBits;

	private DecimalFormat df;

	private boolean[] borders;
	private boolean hidden = false;
	private boolean built = false;
	private boolean applied = false;

	/**
	 *
	 */
	private static void startProcessingModelView(){
		if (currentInstance == null){
			PApplet.main("meshIneBits.gui.view3d.ProcessingModelView");
		}
	}
	/**
	 *
	 */
	public static void closeProcessingModelView(){
		if (currentInstance != null) {
			currentInstance.destroyGLWindow();
		}
	}
	/**
	 *
	 */
	private void destroyGLWindow(){
		((com.jogamp.newt.opengl.GLWindow) surface.getNative()).destroy();
	}
	/**
	 *
	 */
	public void settings(){
		PJOGL.setIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/icon.png")).getPath());
		currentInstance = this;
		size(width, height, P3D);
	}
	/**
	 *
	 */
	private void setCloseOperation(){
		//Removing close listeners

		com.jogamp.newt.opengl.GLWindow win = ((com.jogamp.newt.opengl.GLWindow) surface.getNative());
		for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()){
			win.removeWindowListener(wl);
		}

		win.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);

		win.addWindowListener(new WindowAdapter() {
			public void windowDestroyed(WindowEvent e) {
				controller.deleteObserver(currentInstance);
				dispose();
				currentInstance = null;
				MODEL = null;
				initialized = false;
				visible = false;
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

	public ProcessingModelView() {
		controller = Controller.getInstance();
		controller.addObserver(this);
	}

	/**
	 *
	 */
	public void setup(){

		this.surface.setResizable(true);
		this.surface.setTitle("MeshIneBits - Model view");
		try{
			MODEL = controller.getModel();
		} catch (Exception e){
			System.out.print("Model loading failed");
		}
		// each bool corresponds to 1 face of the workspace. is true if the the shape is crossing the associate face.
		// borders[0] =  xmin border / borders[1] = xmax border ...
		borders = new boolean[6];
		for (int i = 0; i < 6; i++){ borders[i] = false;}

		scene = new Scene(this);
		scene.eye().setPosition(new Vec(0, 1, 1));
		scene.eye().lookAt(scene.eye().sceneCenter());
		scene.setRadius(2500);
		scene.showAll();
		scene.disableKeyboardAgent();
		scene.toggleGridVisualHint();

		builder = new Builder(this);
		setCloseOperation();
		shapeMap = new Vector<>();
		shape = createShape(GROUP);
		builder.buildShape(MODEL, shape);
		cp5 = new ControlP5(this);

		df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);

		frame = new InteractiveFrame(scene,shape);
		customFrameBindings(frame);
		applyGravity();

		createButtons(cp5);
	}

	private void customFrameBindings(InteractiveFrame frame){
		frame.removeBindings();
		frame.setHighlightingMode(InteractiveFrame.HighlightingMode.NONE);
		frame.setPickingPrecision(InteractiveFrame.PickingPrecision.ADAPTIVE);
		frame.setGrabsInputThreshold(scene.radius()/3);
		frame.setRotationSensitivity(3);

		frame.setMotionBinding(CTRL, LEFT_CLICK_ID, "rotate");
		frame.setMotionBinding(CTRL, RIGHT_CLICK_ID, "translate");
	}

	public void draw(){
		background(BACKGROUND_COLOR);
		lights();
		ambientLight(255,255,255);
		drawWorkspace();
		mouseConstraints();
		scene.drawFrames();
		if (built) {
			drawBits(shapeMap);
		}
		scene.beginScreenDrawing();
		updateButtons();
		cp5.draw();
		displayTooltips();
		scene.endScreenDrawing();
	}

	/*
	 * Draw 3D Bits
	 *
	 */

	private void drawBits(Vector<Pair<Position, PShape>> shapeMap) {
		Vector3 v = controller.getModel().getPos();
		for (int i = 0; i < shapeMap.size(); i++) {
			pushMatrix();
			translate((float)v.x, (float)v.y, (float)v.z);
			float[] t = shapeMap.get(i).getKey().getTranslation();
			translate(t[0], t[1], t[2]);
			rotateZ(radians(shapeMap.get(i).getKey().getRotation()));

			PShape s = shapeMap.get(i).getValue();
			shape(s);
			popMatrix();
		}
	}

	/*
	 *  Display the printer workspace
	 *
	 */

	private void drawWorkspace() {
		try {
			File filename = new File(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/PrinterConfig.txt")).getPath());
			FileInputStream file = new FileInputStream(filename);
			BufferedReader br = new BufferedReader(new InputStreamReader(file));
			String strline;
			while ((strline = br.readLine()) != null){
				if (strline.startsWith("x")){
					printerX = Float.valueOf(strline.substring(3));
				}
				else if (strline.startsWith("y")){
					printerY = Float.valueOf(strline.substring(3));
				}
				else if (strline.startsWith("z")){
					printerZ = Float.valueOf(strline.substring(3));
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

	/*
	 *  GUI - Buttons + tooltips
	 *
	 */

	private void createButtons(ControlP5 cp5){
		GLWindow win = ((GLWindow) surface.getNative());

		cp5.addTextfield("RotationX").setPosition(20,40).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("RotationY").setPosition(20,80).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("RotationZ").setPosition(20,120).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("PositionX").setPosition(70,40).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("PositionY").setPosition(70,80).setSize(30,20).setInputFilter(0)
				.setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		cp5.addTextfield("PositionZ").setPosition(70,120).setSize(30,20).setInputFilter(0)
				.setColorBackground(color(255,250))
				.setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0);

		Button gravity = cp5.addButton("ApplyGravity").setPosition(20, 250).setSize(80, 20).setColorLabel(255);
		cp5.addButton("Reset").setPosition(20,280).setSize(80,20).setColorLabel(255);
		cp5.addButton("CenterCamera").setPosition(20,310).setSize(80,20).setColorLabel(255);
		cp5.addButton("Apply").setPosition(20,340).setSize(80,20).setColorLabel(255);

		int color = gravity.getColor().getBackground();

		tooltipGrav =  cp5.addTextarea("tooltipGrav").setPosition(100,250).setText("Pose le modèle").setSize(90,18)
				.setColorBackground(color(220))
				.setColor(color(50)).setFont(createFont("arial",10)).setLineHeight(12).hide()
				.hideScrollbar();
		tooltipGrav.getValueLabel().getStyle().setMargin(1,0,0,5);

		tooltipReset =  cp5.addTextarea("tooltipReset").setPosition(100,280).setText("Remise à zero").setSize(85,18)
				.setColorBackground(color(220))
				.setColor(color(50)).setFont(createFont("arial",10)).setLineHeight(12).hide()
				.hideScrollbar();
		tooltipReset.getValueLabel().getStyle().setMargin(1,0,0,5);

		tooltipCamera =  cp5.addTextarea("tooltipCamera").setPosition(100,310).setText("Centre le modèle").setSize(105,18)
				.setColorBackground(color(220))
				.setColor(color(50)).setFont(createFont("arial",10)).setLineHeight(12).hide()
				.hideScrollbar();
		tooltipCamera.getValueLabel().getStyle().setMargin(1,0,0,5);

		tooltipApply =  cp5.addTextarea("tooltipApply").setPosition(100,340).setText("Applique les modifications").setSize(145,18)
				.setColorBackground(color(220))
				.setColor(color(50)).setFont(createFont("arial",10)).setLineHeight(12).hide()
				.hideScrollbar();
		tooltipApply.getValueLabel().getStyle().setMargin(1,0,0,5);

		txt = cp5.addTextlabel("label").setText("Current Position : (0,0,0)").setPosition(win.getWidth() - 100,win.getHeight() - 50)
				.setSize(80,40).setColor(255);

		modelSize = cp5.addTextlabel("model size", "Model Size :\n Depth:" + shape.getDepth() + "\n Height :" + shape.getHeight() + "\n Width : " + shape.getWidth())
				.setPosition(win.getWidth() - 100,10).setColor(255);

		cp5.addToggle("Model").setPosition(20, 220).setSize(10,10)
					.setColorBackground(color(255,250)).setColorActive(color).setColorForeground(color + 50).toggle();

		toggleBits = cp5.addToggle("Bits").setPosition(60, 220).setSize(10,10)
					.setColorBackground(color(255,250)).setColorActive(color).setColorForeground(color + 50);

		cp5.addSlider("M").setRange(0,100).setPosition(20,180).setSize(70, 10).setValue(100)
				.setColorForeground(color+50).setColorActive(color + 40);
		cp5.addSlider("B").setRange(0,100).setPosition(20,195).setSize(70, 10).setValue(100)
				.setColorForeground(color+50).setColorActive(color + 40);

		cp5.setAutoDraw(false);
	}

	private void updateButtons() {
		GLWindow win = ((GLWindow) surface.getNative());
		modelSize.setPosition(win.getWidth() - 100,10);
		txt.setPosition(win.getWidth() - 100, win.getHeight() - 50);
		txt.setText("Current position :\n" + " x : " + df.format(frame.position().x()) + "\n y : " + df.format(frame.position().y()) + "\n z : " + df.format(frame.position().z()));
/*
		gravity.setPosition(20, win.getHeight() - 150);
		reset.setPosition(20, win.getHeight() - 120);
		camera.setPosition(20, win.getHeight() - 90);
		apply.setPosition(20, win.getHeight() - 60);

		toggleModel.setPosition(20, win.getHeight() - 190);
		toggleBits.setPosition(60, win.getHeight() - 190);*/
		if (mouseX < 100){
			scene.disableMotionAgent();
		}
		else{
			scene.enableMotionAgent();
		}
	}

	private void displayTooltips(){
		tooltipGrav.hide();
		tooltipReset.hide();
		tooltipCamera.hide();
		tooltipApply.hide();
		tooltipGrav.setPosition(mouseX+20 ,mouseY-20);
		tooltipReset.setPosition(mouseX+20 ,mouseY-20);
		tooltipCamera.setPosition(mouseX+20 ,mouseY-20);
		tooltipApply.setPosition(mouseX+20 ,mouseY-20);
		if (!hidden) {
			if ((mouseX > 20) && (mouseX < 100) && (mouseY > 250) && (mouseY < 270)) {
				if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
					tooltipGrav.show();
				}
			}
			if ((mouseX > 20) && (mouseX < 100) && (mouseY > 280) && (mouseY < 300)) {
				if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
					tooltipReset.show();
				}
			}
			if ((mouseX > 20) && (mouseX < 100) && (mouseY > 310) && (mouseY < 330)) {
				if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
					tooltipCamera.show();
				}
			}
			if ((mouseX > 20) && (mouseX < 100) && (mouseY > 340) && (mouseY < 360)) {
				if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
					tooltipApply.show();
				}
			}
		}
	}

	/*
	 * Rotate the shape
	 *
	 */
	private void rotateShape(float angleX, float angleY, float angleZ){
		applied = false;
		Quat r = new Quat();
		float angXRad = (float)Math.toRadians((double)angleX);
		float angYRad = (float)Math.toRadians((double)angleY);
		float angZRad = (float)Math.toRadians((double)angleZ);
		r.fromEulerAngles(angXRad,angYRad,angZRad);
		frame.rotate(r);
		applyGravity();
	}

	/*
	 *	Move the shape
	 *
	 */
	private void translateShape(float transX, float transY, float transZ){
		applied = false;
		frame.translate(transX, transY, transZ);
		boolean checkIn = checkShapeInWorkspace();
		if (!checkIn) {
			if (borders[0]){
				frame.translate((-printerX / 2 - (float)getMinShape().x), 0, 0);
			}
			if (borders[1]) {
				frame.translate((printerX / 2 - (float)getMaxShape().x), 0,0);
			}
			if (borders[2]){
				frame.translate(0,(- printerY/2 - (float)getMinShape().y),0);
			}
			if (borders[3]){
				frame.translate(0,(printerY/2 - (float)getMaxShape().y),0);
			}
			if (borders[4]){
				frame.translate(0,0,(- (float)getMinShape().z));
			}
			if(borders[5]){
				frame.translate(0,0,(printerZ - (float)getMaxShape().z));
			}
		}
	}

	/**
	 *	Return minimum x, y, and z of the shape
	 *
	 */

	private Vector3 getMinShape() {
		double minx = Double.MAX_VALUE;
		double miny = Double.MAX_VALUE;
		double minz = Double.MAX_VALUE;
		int size = shape.getChildCount();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < 3; j++){
				Vec vertex = new Vec(shape.getChild(i).getVertex(j).x,shape.getChild(i).getVertex(j).y, shape.getChild(i).getVertex(j).z);
				Vec v = frame.inverseCoordinatesOf(vertex);
				if (minx > v.x()){
					minx = v.x();
				}
				if (miny > v.y()){
					miny = v.y();
				}
				if (minz > v.z()){
					minz = v.z();
				}
			}
		}
		return new Vector3(minx,miny,minz);
	}

	/**
	 *
	 * Return maximum x, y, and z of the shape
	 */

	private Vector3 getMaxShape() {
		double maxx = - printerX;
		double maxy = - printerY;
		double maxz = - printerZ;
		int size = shape.getChildCount();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < 3; j++){
				Vec vertex = new Vec(shape.getChild(i).getVertex(j).x,shape.getChild(i).getVertex(j).y, shape.getChild(i).getVertex(j).z);
				Vec v = frame.inverseCoordinatesOf(vertex);
				if (maxx < v.x()){
					maxx = v.x();
				}
				if (maxy < v.y()){
					maxy = v.y();
				}
				if (maxz < v.z()){
					maxz = v.z();
				}
			}
		}
		return new Vector3(maxx,maxy,maxz);
	}

	/*
	 * 	Apply modifications of the shape to the model
	 *
	 */

	private void applyRotation(){
		MODEL.rotate(frame.rotation());
	}

	private void applyTranslation(){
		MODEL.setPos(new Vector3(frame.position().x(), frame.position().y(), 0));
		MODEL.move(new Vector3(0, 0, - MODEL.getMin().z));

	}

	private void applyGravity(){
		float minz = (float)getMinShape().z;
		if (minz != 0) {
			frame.translate(0, 0, - minz);
		}
	}

	/*
	 *	Reset the orientation and the position of the shape
	 *
	 */
	private void resetModel(){
		MODEL.rotate(frame.rotation().inverse());
		MODEL.setPos(new Vector3(0,0,0));
		frame.setPosition(new Vec(0,0,0));
		frame.rotate(frame.rotation().inverse());
		applyGravity();
	}

	/*
	 *  Move camera to center the shape
	 */
	private void centerCamera(){
		scene.eye().setPosition(new Vec(frame.position().x(),3000,3000));
		scene.eye().lookAt(frame.position());
	}

	/**
	 *	Check if the model is in the workspace
	 *  Return true if in the workspace
	 */
	private boolean checkShapeInWorkspace(){
		for (int i = 0; i < 6; i++) { borders[i] = false;}
		float minX = -printerX/2;
		float maxX = printerX/2;
		float minY = -printerY/2;
		float maxY = printerY/2;
		float minZ = 0;
		float maxZ = printerZ;
		Vec minPos = new Vec((float)getMinShape().x, (float)getMinShape().y, (float)getMinShape().z);
		Vec maxPos = new Vec((float)getMaxShape().x, (float)getMaxShape().y, (float)getMaxShape().z);
		boolean inWorkspace = true;
		if (minPos.x() < minX){
			inWorkspace = false;
			borders[0] = true;
		}
		if (maxPos.x() >= maxX){
			inWorkspace = false;
			borders[1] = true;
		}
		if (minPos.y() < minY){
			inWorkspace = false;
			borders[2] = true;
		}
		if (maxPos.y() >= maxY){
			inWorkspace = false;
			borders[3] = true;
		}
		if (minPos.z() < minZ){
			inWorkspace = false;
			borders[4] = true;
		}
		if (maxPos.z() >= maxZ){
			inWorkspace = false;
			borders[5] = true;
		}
		return inWorkspace;
	}

	private void mouseConstraints(){
		boolean checkIn = checkShapeInWorkspace();
		if (!checkIn) {
			if (borders[0]) {
				frame.translate((- printerX / 2 - (float)getMinShape().x), 0,0);
			}
			if (borders[1]) {
				frame.translate((printerX / 2 - (float)getMaxShape().x), 0,0);
			}
			if (borders[2]) {
				frame.translate(0,(- printerY / 2 - (float)getMinShape().y), 0);
			}
			if (borders[3]){
				frame.translate(0,(printerY / 2 - (float)getMaxShape().y), 0);
			}
			if (borders[4]){
				frame.translate(0,0, (- (float)getMinShape().z));
			}
			if (borders[5]){
				frame.translate(0,0, (printerZ - (float)getMaxShape().z));
			}
		}
	}

	/**
	 *  Listeners for GUI
	 */
	public void RotationX(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShape(angle,0,0);
	}

	public void RotationY(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShape(0, angle, 0);
	}

	public void RotationZ(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShape(0,0, angle);
	}

	public void PositionX(String theValue) {
		float pos = Float.valueOf(theValue);
		translateShape(pos,0,0);
	}

	public void PositionY(String theValue){
		float pos = Float.valueOf(theValue);
		translateShape(0, pos, 0);
	}

	public void PositionZ(String theValue){
		float pos = Float.valueOf(theValue);
		translateShape(0, 0, pos);
	}

	public void Reset(float theValue){
		resetModel();
		centerCamera();
		built = false;
		toggleBits.setState(false);
		applied = false;

	}

	public void ApplyGravity(float theValue) {
		applyGravity();
	}

	public void CenterCamera(float theValue){
		centerCamera();
	}

	public void Apply(float theValue){
		if (!applied) {
			applyRotation();
			applyGravity();
			applyTranslation();
			applied = true;
		}
	}

	public void Model(boolean flag){
		if (flag == false){
			shape.setVisible(false);
			frame.removeMotionBindings();
		}
		else {
			shape.setVisible(true);
			customFrameBindings(frame);
		}
	}

	public void Bits(boolean flag) {
		if (flag) {
            if (controller.getCurrentPart().isPaved() && !built) {
				builder.buildBits(shapeMap);
				built = true;
			}
			if (built){
				for (int i = 0; i < shapeMap.size(); i++) {
					shapeMap.get(i).getValue().setVisible(true);
				}
			}
		} else {
			if (built) {
				for (int i = 0; i < shapeMap.size(); i++) {
					shapeMap.get(i).getValue().setVisible(false);
				}
			}
		}
	}

	public void M (int value){
		value *= 255./100.;
		shape.setFill(color(219, 100, 50, value));
	}

	public void B (int value){
		value *= 255./100.;
		for (Pair<Position, PShape> p : shapeMap){
			p.getValue().setFill(color(19, 100, 50, value));
		}
	}

	@Override
	public void update(Observable o, Object arg){}

	private void open(){
		// TODO Auto-generated method stub
		if (!initialized){
			ProcessingModelView.startProcessingModelView();
			visible = true;
			initialized = true;
		} else{
			setVisible(true);
		}
	}

	private void hide(){
		// TODO Auto-generated method stub
		setVisible(false);
	}

	@Override
	public void toggle(){
		if (visible){
			hide();
		} else{
			open();
		}
	}

	@Override
	public void setCurrentMesh(Mesh mesh) {
		controller.setCurrentPart(mesh);
	}

	private void setVisible(boolean b){
		// TODO Auto-generated method stub
		currentInstance.getSurface().setVisible(b);
		visible = b;
	}

	public void setModel(Model m) {
		controller.setModel(m);
	}


}
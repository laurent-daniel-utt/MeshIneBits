package meshIneBits.gui.view3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;

import meshIneBits.GeneratedPart;
import meshIneBits.Model;
import meshIneBits.gui.view2d.Controller;
import meshIneBits.util.Logger;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;
import meshIneBits.gui.SubWindow;

import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PJOGL;

import remixlab.dandelion.geom.Quat;
import remixlab.dandelion.geom.Rotation;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;
import remixlab.dandelion.geom.Vec;

import controlP5.*;

import static remixlab.proscene.MouseAgent.WHEEL_ID;


/**
 * 
 * @author Nicolas
 *
 */
public class ProcessingModelView extends PApplet implements Observer, SubWindow {

	private final int BACKGROUND_COLOR = color(150, 150, 150);
	private final int MODEL_COLOR = color(219, 100, 50);
	private float printerX;
	private float printerY;
	private float printerZ;

	private static boolean visible = false;
	private static boolean initialized = false;

	private static ProcessingModelView currentInstance = null;
	private static Model MODEL;
	private static Controller controller = null;

	private PShape shape;
	private Scene scene;
	private InteractiveFrame frame;
	private ControlP5 cp5;
	private Textlabel txt;

	private DecimalFormat df;

	/**
	 *
	 */
	public static void startProcessingModelView(){
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
	public void destroyGLWindow(){
		((com.jogamp.newt.opengl.GLWindow) surface.getNative()).destroy();
	}

	/**
	 *
	 */
	public void settings(){
		size(700, 400, P3D);


		PJOGL.setIcon(this.getClass().getClassLoader().getResource("resources/icon.png").getPath());
		currentInstance = this;
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
				//controller.deleteObserver(currentInstance);
				dispose();
				currentInstance = null;
				MODEL = null;
				initialized = false;
				visible = false;
			}
		});
	}

	/**
	 *
	 */
	public void setup(){
		controller = Controller.getInstance();
		controller.addObserver(this);
		try{
			MODEL = controller.getModel();
		} catch (Exception e){
			System.out.print(" Model loading failed");
		}
		buildModel();

		cp5 = new ControlP5(this);

		cp5.addTextfield("RotationX").setPosition(20,40).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,220))
				.setColor(0).setColorLabel(255);

		cp5.addTextfield("RotationY").setPosition(20,80).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,220))
				.setColor(0).setColorLabel(255);

		cp5.addTextfield("RotationZ").setPosition(20,120).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,220))
				.setColor(0).setColorLabel(255);

		cp5.addTextfield("PositionX").setPosition(70,40).setSize(30,20)
				.setInputFilter(0).setColorBackground(color(255,220))
				.setColor(0).setColorLabel(255);

		cp5.addTextfield("PositionY").setPosition(70,80).setSize(30,20).setInputFilter(0)
				.setColorBackground(color(255,220))
				.setColor(0).setColorLabel(255);

		cp5.addTextfield("PositionZ").setPosition(70,120).setSize(30,20).setInputFilter(0)
				.setColorBackground(color(255,220))
				.setColor(0).setColorLabel(255);

		cp5.addButton("ApplyGravity").setPosition(20,250).setSize(80,20).setColorLabel(255);
		cp5.addButton("Reset").setPosition(20,280).setSize(80,20).setColorLabel(255);
		cp5.addButton("CenterCamera").setPosition(20,310).setSize(80,20).setColorLabel(255);
		cp5.addButton("Apply").setPosition(20,340).setSize(80,20).setColorLabel(255);


		cp5.getTooltip().register("Reset","reset model");

		txt = cp5.addTextlabel("label").setText("Current Position : (0,0,0)").setPosition(570,350)
				.setSize(80,40).setColorLabel(0);

		cp5.addTextlabel("model size", "Model Size\n Depth :" + shape.getDepth() + "\n Height :" + shape.getHeight() + "\n Width : " + shape.getWidth())
				.setPosition(570,0)
				.setColorLabel(0);

		cp5.setAutoDraw(false);

		df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.CEILING);

		scene = new Scene(this);
		scene.eye().setPosition(new Vec(0, 1, 1));
		scene.eye().lookAt(scene.eye().sceneCenter());
		scene.setRadius(2500);
		scene.showAll();
		scene.toggleGridVisualHint();
		scene.disableKeyboardAgent();
		setCloseOperation();
		frame = new InteractiveFrame(scene,shape);
		frame.setMotionBinding(WHEEL_ID, null);

		//setInitialScale();



	}

	private void buildModel(){
		Logger.updateStatus("Start building STL model");

		Vector<Triangle> stlTriangles = MODEL.getTriangles();

		//triangles = new ArrayList<PShape>()
		shapeMode(CORNER);
		shape = createShape(GROUP);

		for (Triangle t : stlTriangles){
			shape.addChild(getPShapeFromTriangle(t));
		}

		println((MODEL.getMax().z - MODEL.getMin().z));
		Logger.updateStatus("STL model built.");
	}

	public Vector3 getPShapeMin(PShape s) {
		Vector3 min = new Vector3();
		min.x = Float.MAX_VALUE;
		min.y = Float.MAX_VALUE;
		min.z = Float.MAX_VALUE;
		int size = s.getChildCount();
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < 3; j++) {
				if (min.x > s.getChild(i).getVertex(j).x) {
					min.x = s.getChild(i).getVertex(j).x;
				}
				if (min.y > s.getChild(i).getVertex(j).y) {
					min.y = s.getChild(i).getVertex(j).y;
				}
				if (min.z > s.getChild(i).getVertex(j).z) {
					min.z = s.getChild(i).getVertex(j).z;
				}
			}
		}
		return min;
	}
	/**
	 * @param t
	 * @return
	 */
	private PShape getPShapeFromTriangle(Triangle t){
		PShape face = createShape();
		face.beginShape();

		for (Vector3 p : t.point){
			face.vertex((float) p.x, (float) p.y, (float) p.z);
		}

		face.endShape(CLOSE);

		face.setStroke(false);
		face.setFill(MODEL_COLOR);

		return face;
	}

	/**
	 *
	 */

	public void draw(){
		background(BACKGROUND_COLOR);
		lights();
		ambientLight(255,255,255);
		drawWorkspace();
		scene.drawFrames();
		scene.beginScreenDrawing();
		//txt.setText("Current position :\n" + String.format("%.2g%n",frame.position().x()) + String.format("%.2g%n",frame.position().y()) + String.format("%.2g%n",frame.position().z()));
		txt.setText("Current position :\n" + " x : " + df.format(frame.position().x()) + "\n y : " + df.format(frame.position().y()) + "\n z : " + df.format(frame.position().z()));
		cp5.draw();
		scene.endScreenDrawing();

	}

	@Override
	public void update(Observable o, Object arg){
		redraw();

	}

	private void drawWorkspace() {
		try {
			File filename = new File(this.getClass().getClassLoader().getResource("resources/PrinterConfig.txt").getPath());
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


	public void open(){
		// TODO Auto-generated method stub
		if (!initialized){
			ProcessingModelView.startProcessingModelView();
			visible = true;
			initialized = true;
		} else{
			setVisible(true);
		}
	}

	@Override
	public void hide(){
		// TODO Auto-generated method stub
		setVisible(false);
	}

	@Override
	public void refresh(){
		// TODO Auto-generated method stub
		redraw();
	}

	@Override
	public void setPart(GeneratedPart part){
		// TODO Auto-generated method stub

	}

	@Override
	public void getPart(){
		// TODO Auto-generated method stub

	}

	@Override
	public void toggle(){
		if (visible){
			hide();
		} else{
			open();
		}
	}

	private void setVisible(boolean b){
		// TODO Auto-generated method stub
		currentInstance.getSurface().setVisible(b);
		visible = b;
	}

	private void rotateShapeX(float angleX){
		Quat r = new Quat();
		float angXRad = (float)Math.toRadians((double)angleX);

		r.fromEulerAngles(angXRad,0,0);
		float h = shape.getHeight()/2;
		float rot = ((Quat)frame.orientation()).eulerAngles().x() +angXRad;
/*
		if ((angXRad % PI < 0.0001) && angXRad != 0){
			frame.translate(frame.position().x(), frame.position().y(), frame.position().z()+ shape.getDepth());
		}
		else{
			frame.translate(frame.position().x(), frame.position().y(), frame.position().z() + abs(h * sin(angXRad)));
		}*/
		frame.rotate(r);
	}


	private void rotateShapeY( float angleY){
		Quat r = new Quat();

		float angYRad = (float)Math.toRadians((double)angleY);

		r.fromEulerAngles(0,angYRad,0);

		float h = shape.getWidth()/2;

		frame.translate(0,0,abs(h*sin(angYRad)));
		frame.rotate(r);

	}

	private void rotateShapeZ(float angleZ){
		Quat r = new Quat();

		float angZRad = (float)Math.toRadians((double)angleZ);
		r.fromEulerAngles(0,0,angZRad);
		frame.rotate(r);

	}

	private void applyRotation(){
		Rotation r = frame.orientation();
		MODEL.rotate(r);
	}
	private void applyTranslation(){
		Vec trans = frame.position();
		MODEL.translate(new Vector3(trans.x(), trans.y(),trans.z()));
	}


	private void resetModel(){
		MODEL.rotate(frame.rotation().inverse());
		frame.setPosition(new Vec(0,0,0));
		frame.rotate(frame.rotation().inverse());

	}

	public void RotationX(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShapeX(angle);
	}

	public void RotationY(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShapeY(angle);
	}

	public void RotationZ(String theValue){
		float angle = Float.valueOf(theValue);
		rotateShapeZ(angle);
	}

	public void PositionX(String theValue) {
		boolean side = true;
		float pos = Float.valueOf(theValue);
		frame.translate(pos, 0, 0);
		boolean checkIn = checkShapeInWorkspace(side);
		if (!checkIn) {
			if (side) {
				frame.setPosition(printerX / 2 - shape.getWidth() / 2, frame.position().y(), frame.position().z());
			} else {
				frame.setPosition(-printerX / 2 + shape.getWidth() / 2, frame.position().y(), frame.position().z());
			}
			Vector3 v = new Vector3(frame.translation().x(), frame.translation().y(), frame.translation().z());
		}
	}

	public void PositionY(String theValue){
		boolean side = true;
		float pos = Float.valueOf(theValue);
		frame.translate(0,pos,0);
		boolean checkIn = checkShapeInWorkspace(side);
		if (!checkIn){
			if(side){
				frame.setPosition(frame.position().x(),printerY/2 - shape.getHeight()/2,frame.position().z());
			}
			else{
				frame.setPosition(frame.position().x(),- printerY/2 + shape.getHeight()/2,frame.position().z());
			}
		}
		Vector3 v = new Vector3(frame.translation().x(),frame.translation().y(),frame.translation().z());
	}

	public void PositionZ(String theValue){
		boolean side = true;
		float pos = Float.valueOf(theValue);
		frame.translate(0,0,pos);
		boolean checkIn = checkShapeInWorkspace(side);
		if (!checkIn){
			if(side){
				frame.setPosition(frame.position().x(),frame.position().y(),printerZ - shape.getDepth());
			}
			else{
				frame.setPosition(frame.position().x(),frame.position().y(),0);
			}
		}
		Vector3 v = new Vector3(frame.translation().x(),frame.translation().y(),frame.translation().z());
	}

	public void Reset(float theValue){
		resetModel();
		scene.eye().setPosition(new Vec(0, scene.radius(), scene.radius()));
		scene.eye().lookAt(scene.eye().sceneCenter());
	}

	public void ApplyGravity(float theValue) {
		Vec trans = frame.position();
		frame.translate(0,0,-trans.z());
	}

	public void CenterCamera(float theValue){
		float y = scene.eye().position().y();
		float z = scene.eye().position().z();
		scene.eye().setPosition(new Vec(frame.position().x(), y, z));
		scene.eye().lookAt(frame.position());
	}

	public void Apply(float theValue){
		applyTranslation();
		applyRotation();
	}

	// side == false => min border is crossed / side == true => max border crossed
	private boolean checkShapeInWorkspace(boolean side){
		Vector<Triangle> triangles = MODEL.getTriangles();
		float minX = -printerX/2;
		float maxX = printerX/2;
		float minY = -printerY/2;
		float maxY = printerY/2;
		float minZ = 0;
		float maxZ = printerZ;
		Vec pos = frame.position();
		Vec minPos = new Vec(pos.x() - shape.getWidth()/2,pos.y() - shape.getHeight()/2, pos.z());
		Vec maxPos = new Vec(pos.x() + shape.getWidth()/2,pos.y() + shape.getHeight()/2,pos.z() + shape.getDepth());
		boolean inWorkspace = true;
		if ((minPos.x() < minX) ||  (minPos.y() < minY) ||  (minPos.z() < minZ)){
			inWorkspace = false;
			side = false;
		}
		if ((maxPos.x() >= maxX) || (maxPos.y() >= maxY) ||  (maxPos.z() >= maxZ)){
			side = true;
			inWorkspace = false;
		}
		return inWorkspace;
	}

}


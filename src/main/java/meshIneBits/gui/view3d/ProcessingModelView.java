package meshIneBits.gui.view3d;

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
import processing.core.PGraphics;
import processing.core.PShape;
import processing.opengl.PJOGL;

import remixlab.dandelion.geom.Quat;
import remixlab.dandelion.geom.Rotation;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;
import remixlab.dandelion.geom.Vec;

import controlP5.*;
import static java.awt.event.KeyEvent.*;
import static remixlab.proscene.MouseAgent.WHEEL_ID;


/**
 * 
 * @author Nicolas
 *
 */
public class ProcessingModelView extends PApplet implements Observer, SubWindow {

	private final int BACKGROUND_COLOR = color(240, 240, 240);
	private final int MODEL_COLOR = color(219, 90, 10);

	private static boolean visible = false;
	private static boolean initialized = false;

	private static ProcessingModelView currentInstance = null;
	private static Model MODEL;
	private static Controller controller = null;

	private float RotationX = 0;
	private float RotationY = 0;
	private float RotationZ = 0;


	private PShape shape;
	private Scene scene;
	private InteractiveFrame frame;
	private ControlP5 cp5;

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
		size(640, 360, P3D);
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

		cp5 = new ControlP5(this);

		cp5.addButton("test").setPosition(200,200).setValue(0);
		cp5.setAutoDraw(false);

		controller = Controller.getInstance();
		controller.addObserver(this);
		try{
			MODEL = controller.getModel();
		} catch (Exception e){
			System.out.print(" Model loading failed");
		}

		scene = new Scene(this);
		scene.eye().setPosition(new Vec(0, 1, 1));
		scene.eye().lookAt(scene.eye().sceneCenter());
		scene.setRadius(1000);
		scene.showAll();
		scene.disableKeyboardAgent();
		setCloseOperation();
		buildModel();

		frame = new InteractiveFrame(scene,shape);
		frame.setMotionBinding(WHEEL_ID, null);

		//setInitialScale();

	}

	/**
	 *
	 */
//    private void setInitialScale() {
//		float modelSkirtDiameter = (float) (curVO.getCurrentPart().getSkirtRadius() * 2);
//		
//		float bitThickness = (float) CraftConfig.bitThickness;
//    	float layersOffSet = (float) CraftConfig.layersOffset;
//    	float modelTotalHeight = curVO.getCurrentPart().getLayers().size() * (bitThickness + layersOffSet);
//    	
//    	float heightRatio = height / modelTotalHeight;
//    	float widthRatio = width / modelSkirtDiameter;
//    	
//    	if(heightRatio < widthRatio)
//    		scale = heightRatio;
//    	else
//    		scale = widthRatio;	
//	}

	/**
	 *
	 */
	private void buildModel(){

		Logger.updateStatus("Start building STL model");

		Vector<Triangle> stlTriangles = MODEL.getTriangles();

		//triangles = new ArrayList<PShape>();

		shapeMode(CORNER);
		shape = createShape(GROUP);

		for (Triangle t : stlTriangles){
			shape.addChild(getPShapeFromTriangle(t));
		}

		Logger.updateStatus("STL model built.");
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

		//scale(scale);
		scene.drawFrames();
		//applyRotation();
		//cp5.draw();


	}


	@Override
	public void update(Observable o, Object arg){
		redraw();

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

	public void keyPressed(){
		if (keyCode == VK_ENTER) {
			applyRotation();
		}
		if (keyCode == VK_R){
			rotateShape(PI,0,0);
		}

	}

	private void rotateShape(float angleX, float angleY, float angleZ){
		Quat r = new Quat();
		r.fromEulerAngles(angleX,angleY,angleZ);

		shape.rotateX(angleX);
		shape.rotateY(angleY);
		shape.rotateZ(angleZ);

		shape.translate(0,0,shape.getDepth());
		MODEL.rotate(r);
	}


	private void applyRotation(){
		Rotation rot = frame.rotation();
		MODEL.rotate(rot);
	}
	/*private void applyTranslation(){
		Vec trans = frame.position();
		MODEL.translate(new Vector3(trans.x(), trans.y(), trans.z()));
	}
*/

}
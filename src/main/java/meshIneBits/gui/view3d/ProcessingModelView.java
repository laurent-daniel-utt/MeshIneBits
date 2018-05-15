package meshIneBits.gui.view3d;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;

import meshIneBits.MeshIneBitsMain;
import meshIneBits.Model;
import meshIneBits.util.Logger;
import meshIneBits.util.Triangle;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PShape;
import processing.event.MouseEvent;
import processing.opengl.PJOGL;

/**
 * 
 * @author Nicolas
 *
 */
public class ProcessingModelView extends PApplet implements Observer{
		
	private float xRotation = 0;
	private float zRotation = 0;
	private float scale = (float) 0.4;
	private boolean okClicked = false;
	
	
	private PShape s;
	
	
	private final float ROTATION_STEP = PI/12;
	private final float ZOOM_STEP = (float) 1.25;
	private final float FRAME_RATE = 20;
	private final int BACKGROUND_COLOR = color(240,240,240);
	private final int MODEL_COLOR = color(219, 90, 10);
	
	private static ProcessingModelView currentInstance = null;
	private static Model MODEL;
	
	/**
	 * 
	 */
	public static ProcessingModelView startProcessingView(Model m){
		if(currentInstance == null){
			MODEL = m;
			PApplet.main("meshIneBits.gui.processing.ProcessingModelView");			
		}
		
		return currentInstance;
	}
	
	/**
	 * 
	 */
	public static void closeProcessingView(){
		if(currentInstance != null){
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
		size(640,360, P3D);		
    	PJOGL.setIcon(this.getClass().getClassLoader().getResource("resources/icon.png").getPath());
    	currentInstance = this;
    }
	
	/**
	 * 
	 */
	private void setCloseOperation(){
		//Removing close listeners
    	com.jogamp.newt.opengl.GLWindow win = ((com.jogamp.newt.opengl.GLWindow) surface.getNative());
		for(com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()){
			win.removeWindowListener(wl);
		}
		
		win.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
		
        win.addWindowListener( new WindowAdapter() {
        	public void  windowDestroyed(WindowEvent e){
        		//curVO.deleteObserver(currentInstance);
        		dispose();
        		currentInstance = null;
        		MODEL = null;
        	}
        } );
	}
	
	/**
	 * 
	 */
    public void setup(){
    	
    	surface.setTitle("MeshIneBits - 3D view");
    	
    	frameRate(FRAME_RATE);   	
    	
    	noLoop();
    	
//    	curVO = ViewObservable.getInstance();
//    	curVO.addObserver(this);
    	
    	setCloseOperation();
  	
    	buildModel();
    	
//    	setInitialScale();
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
    	
		Logger.updateStatus("Start building 3D model");
		
		Vector<Triangle> stlTriangles = MODEL.getTriangles();
		
		//triangles = new ArrayList<PShape>();
		
		s = createShape(GROUP);
		
		for(Triangle t : stlTriangles){
			s.addChild(getPShapeFromTriangle(t));
		}
		
		Logger.updateStatus("STL model built.");
    }
	
	/**
	 * 
	 * @param t
	 * @return
	 */
	private PShape getPShapeFromTriangle(Triangle t){
    	PShape face = createShape();
    	face.beginShape();
    	
    	for(Vector3 p : t.point){
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
    	
    	drawButton();
    	
    	lights();
    	
    	translate(width/2,(float) (height * 0.65), -100);
    	rotateX(-PI/6);
    	rotateX(PI/2);//In order to have the z axis upward
    	
    	rotateZ(PI/4);
    	
    	drawAxis();
	
    	rotateX(xRotation);
    	
    	//X = x;
    	 //Y = y*cos(p)-z*sin(p);
    	// Z = y*sin(p)+z*cos(p);
    	
    	//rotate(zRotation, 0, -sin(-xRotation), cos(-xRotation));
    	
    	rotateZ(zRotation);
    	
    	drawAxis();
    	
    	
    	
    	scale(scale);
    	
    	shape(s);
    }
    
    /**
     * 
     */
    private void drawAxis(){
    	
    	float axisLength = (float) (height * 0.65);
    	
    	shape(getArrow(new int[]{1,0,0}, axisLength, color(255,0,0)));
    	shape(getArrow(new int[]{0,1,0}, axisLength, color(0,255,0)));
    	shape(getArrow(new int[]{0,0,1}, axisLength, color(0,0,255)));   	
    }
    
    /**
     * 
     * @param vector
     * @param length
     * @param color
     * @return
     */
    private PShape getArrow(int[] vector, float length, int color){
    	PShape arrow = createShape(LINE, 0,0,0,vector[0] * length, vector[1] * length, vector[2] * length);
    	
    	arrow.setStroke(true);
    	arrow.setStroke(color);
    	
    	return arrow;
    }
    
    /**
     * 
     */
    private void drawButton(){
    	fill(255);
    	rect(width - 70, height - 55, 50, 35);
    	
    	PFont f = createFont("Arial",30,true);
        textFont(f);       
        fill(0);

        textAlign(CENTER, CENTER);
        
        text("OK", width - 45, height - 40, 0);
    }
    
    /**
     * 
     */
    public void keyPressed() {
	
    	if (key == CODED) {
    	    if (keyCode == UP) {
    	    	xRotation += ROTATION_STEP;
    	    } else if (keyCode == RIGHT) {
    	    	zRotation -= ROTATION_STEP;
    	    } 
    	    else if (keyCode == DOWN) {
    	    	xRotation -= ROTATION_STEP;
    	    } else if (keyCode == LEFT) {
    	    	zRotation += ROTATION_STEP;
    	    } 
    	  
    	}    	
    	
    	redraw();
    }
    
    /**
     * 
     */
    public void mouseWheel(MouseEvent event) {
    	  float e = event.getCount();
    	  if(e > 0)
    		  scale = scale / ZOOM_STEP;
    	  else
    		  scale = scale * ZOOM_STEP;

    	  redraw();
    }
    
    /**
     * 
     */
    public void mousePressed(){
    	if(!okClicked && (mouseX >= width - 70) && mouseX <= (width - 20) && mouseY >= (height - 55) && mouseY <= (height - 20)){
    		okClicked = true;
    		sliceModel();
    	}
    }
    
    /**
     * 
     */
    private void sliceModel(){
    	
    	MODEL.rotate();// function rotate() TODO (Rotation parameters will have to be given to that function)
    	
//    	MeshIneBitsMain.sliceModel(MODEL);
    	
    	destroyGLWindow();
    }

	@Override
	public void update(Observable o, Object arg) {
		redraw();
		
	}

}
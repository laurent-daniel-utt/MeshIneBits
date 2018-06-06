package meshIneBits.gui.view3d;

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import meshIneBits.Bit3D;
import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.SubWindow;
import meshIneBits.util.AreaTool;
import meshIneBits.util.Logger;
import meshIneBits.util.Segment2D;
import meshIneBits.util.Vector2;
import processing.core.PApplet;
import processing.core.PShape;
import processing.event.MouseEvent;
import processing.opengl.PJOGL;

import java.awt.geom.Area;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

/**
 * The 3D view of model loaded
 * @author Nicolas
 *
 */
public class ProcessingView extends PApplet implements Observer, SubWindow{
	
	private float zRotation = 0;
	private float xRotation = -PI/6;	
	private float scale = 1;
	private Controller curVO = null;	
	private HashMap<Position, PShape> shapeMap;
	private boolean autoRotate = true;
	
	private final float ROTATION_STEP = PI/12;
	private final float ZOOM_STEP = (float) 1.25;
	private final float FRAME_RATE = 20;
	private final int BACKGROUND_COLOR = color(240,240,240);
	private final int BIT_COLOR = color(155, 132, 91);
	
	private static ProcessingView currentInstance = null;
	
	/**
	 * 
	 * @param args
	 */
	public static void startProcessingView(String[] args){
		if(currentInstance == null)
			PApplet.main("meshIneBits.gui.view3d.ProcessingView");
	}
	
	/**
	 * 
	 */
	public static void closeProcessingView(){
		if(currentInstance != null)
			currentInstance.destroyGLWindow();			
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
        		curVO.deleteObserver(currentInstance);
        		dispose();
        		currentInstance = null;
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
    	
    	setCloseOperation();
    	
    	buildModel();
    	
    	setInitialScale();
    }
    
    /**
     * 
     */
    private void setInitialScale() {
		float modelSkirtDiameter = (float) (curVO.getCurrentPart().getSkirtRadius() * 2);
		
		float bitThickness = (float) CraftConfig.bitThickness;
    	float layersOffSet = (float) CraftConfig.layersOffset;
    	float modelTotalHeight = curVO.getCurrentPart().getLayers().size() * (bitThickness + layersOffSet);
    	
    	float heightRatio = height / modelTotalHeight;
    	float widthRatio = width / modelSkirtDiameter;
    	
    	if(heightRatio < widthRatio)
    		scale = heightRatio;
    	else
    		scale = widthRatio;	
	}
    
    /**
     * 
     */
	private void buildModel(){
    	
		Logger.updateStatus("Start building 3D model");
    	
    	shapeMap = new HashMap<Position, PShape>();
    	Vector<Layer> layers = curVO.getCurrentPart().getLayers();
    	float bitThickness = (float) CraftConfig.bitThickness;
    	float layersOffSet = (float) CraftConfig.layersOffset;
    	
    	PShape uncutBitPShape = getUncutBitPShape(bitThickness);
    	
    	float zLayer;
    	
    	int bitCount = 0;
    	
    	for(Layer curLayer : layers){

    		zLayer = curLayer.getLayerNumber() * (bitThickness + layersOffSet);
    		
    		for(Vector2 curBitKey : curLayer.getBits3dKeys()){
    			
    			bitCount++;

    			Bit3D curBit = curLayer.getBit3D(curBitKey);
    			
    			PShape bitPShape;
    			
//    			if(curBit.getCutPaths() == null)
//    				bitPShape = uncutBitPShape;
//    			else
    				bitPShape = getBitPShapeFrom(curBit.getRawArea(), bitThickness);
		
    			if(bitPShape != null){
    				
    				bitPShape.setFill(BIT_COLOR);
    				
    				Vector2 curBitCenter = curBit.getOrigin();
        			float curBitCenterX = (float) curBitCenter.x;
        			float curBitCenterY = (float) curBitCenter.y;

        			float[] translation = {curBitCenterX, curBitCenterY, zLayer};
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
    private PShape getUncutBitPShape(float extrudeDepth){
    	
    	Vector2 cornerUpRight = new Vector2(+CraftConfig.bitLength / 2.0, -CraftConfig.bitWidth / 2.0);
		Vector2 cornerDownRight = new Vector2(cornerUpRight.x, cornerUpRight.y + CraftConfig.bitWidth);
		Vector2 cornerUpLeft = new Vector2(cornerUpRight.x - CraftConfig.bitLength, cornerUpRight.y);
		Vector2 cornerDownLeft = new Vector2(cornerDownRight.x - CraftConfig.bitLength, cornerDownRight.y);    	
    	
    	Vector<int[]> pointList = new Vector<int[]>();
    	pointList.add(new int[]{(int) cornerUpRight.x, (int) cornerUpRight.y, 0});
    	pointList.add(new int[]{(int) cornerDownRight.x, (int) cornerDownRight.y, 0});
    	pointList.add(new int[]{(int) cornerDownLeft.x, (int) cornerDownLeft.y, 0});
    	pointList.add(new int[]{(int) cornerUpLeft.x, (int) cornerUpLeft.y, 0});
    	
    	PolygonPointsList poly = null;
		try {
			poly = new PolygonPointsList(pointList);
		} catch (Exception e) {
			System.out.println("Polygon point list exception");
			return null;
		}		
    	
		return extrude(new PolygonPointsList[]{poly, null}, (int) extrudeDepth);
    }
    
    /**
     * 
     * @param bitArea
     * @param extrudeDepth
     * @return
     */
    private PShape getBitPShapeFrom(Area bitArea, float extrudeDepth){
    	
    	Vector<Segment2D> segmentList = AreaTool.getLargestPolygon(bitArea);
    	if(segmentList == null)
    		return null;
    	
    	
    	Vector<int[]> pointList = new Vector<int[]>();
    	for(Segment2D s : segmentList){
			pointList.add(new int[]{(int) Math.round(s.start.x), (int) Math.round(s.start.y), 0});
			pointList.add(new int[]{(int) Math.round(s.end.x), (int) Math.round(s.end.y), 0});
		}
    	
    	PolygonPointsList poly = null;
		try {
			poly = new PolygonPointsList(pointList);
		} catch (Exception e) {
			System.out.println("Polygon point list exception");
			return null;
		}		
    	
		return extrude(new PolygonPointsList[]{poly, null}, (int) extrudeDepth);
    }
   
    /**
     * 
     */
    public void draw(){
    	background(BACKGROUND_COLOR);
    	
    	if(autoRotate){
    		loop();
    		rect(20, height - 70, 20, 50);
    		rect(50, height - 70, 20, 50);
    		zRotation += PI/200;
    	}
    	else{
    		noLoop();
    		triangle(20, height - 70, 20, height - 20, 70, height - 45);
    	}
    	
    	float bitThickness = (float) CraftConfig.bitThickness;
    	float layersOffSet = (float) CraftConfig.layersOffset;
    	
    	int totalHeight = (int) (curVO.getCurrentPart().getLayers().size() * (bitThickness + layersOffSet));
    	
    	//camera(width/2, height/2, (height/2) / tan(PI/6), width/2, height/2, 0, 0, 1, 0);
    	
    	//translate(width/2, height/2, (float) (curVO.getCurrentPart().getSkirtRadius() * -1));
    	translate(width/2, height/2, -totalHeight);

    	rotateX(xRotation);
    	rotateX(PI/2);//In order to have the z axis upward
    	
    	rotateZ(zRotation);
    	
    	
    	translate(0, 0, -totalHeight/2);
    	
    	scale(scale);
    	stroke(0,0,0);
    	
    	
    	int lNumber = curVO.getCurrentLayerNumber();
    	float zLayer = lNumber * (bitThickness + layersOffSet);
    	
    	for(Position p : shapeMap.keySet()){

    		pushMatrix();
        	float[] t = p.getTranslation();
        	translate(t[0], t[1], t[2]);
        	rotateZ(radians(p.getRotation()));
        	
        	PShape s = shapeMap.get(p);
        	
        	if(t[2] <= zLayer){
        		shape(s);
    		}
          	        	
        	popMatrix();
    	}
	
    }
    
    /**
     * 
     * @param pointA
     * @param pointB
     * @param z
     * @return
     */
    private PShape getFaceExtrude(int[] pointA, int[] pointB, int z){
    	PShape face = createShape();
    	face.beginShape();
    	face.vertex(pointA[0], pointA[1], pointA[2] + z);
    	face.vertex(pointB[0],pointB[1],pointB[2] + z);
    	face.vertex(pointB[0],pointB[1],pointB[2]);
    	face.vertex(pointA[0],pointA[1],pointA[2]);
    	face.endShape(CLOSE);
    	
    	return face;
    }
    
    /**
     * 
     * @param poly
     * @param z
     * @return
     */
    private PShape getSideExtrude(PolygonPointsList poly, int z){
    	
    	PShape side = createShape(GROUP);
    	
    	int length = poly.getLength();
    	int[] pointA = poly.getNextPoint();
    	int[] pointB = poly.getNextPoint();

    	for(int j = 0; j < length; j++){   		
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
    private PShape getPShape(PolygonPointsList[] poly, int z){
    	
    	int length;
    	int[] point;
    	
    	PShape myShape = createShape();
    	myShape.beginShape();
    	//Exterior path
    	length = poly[0].getLength();
    	for(int j = 0; j < length + 1; j++){
    		point = poly[0].getNextPoint();
    		myShape.vertex(point[0],point[1],point[2] + z);
    	}
    	//Interior path
    	if(poly[1] != null){
    		myShape.beginContour();
        	length = poly[1].getLength();
        	for(int j = 0; j < length + 1; j++){
        		point = poly[1].getNextPoint();
        		myShape.vertex(point[0],point[1],point[2] + z);
        	}
        	myShape.endContour();
		}
   	
    	myShape.endShape();
    	
    	return myShape;
    }
    
    
    /**
     * Work only for shape on the xy plan
     */
    private PShape extrude(PolygonPointsList[] poly, int z){
    	
    	PShape extrudedObject = createShape(GROUP);
    	
    	PShape exterior = getSideExtrude(poly[0], z);
    	extrudedObject.addChild(exterior);

    	if(poly[1] != null){
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
    	if(mouseX >= 20 && mouseX <= 70 && mouseY >= (height - 70) && mouseY <= (height - 20)){
    		changeAutoRotate();
    	}
    }
    
    /**
     * 
     */
    public void changeAutoRotate(){
    	if(autoRotate){   		
    		autoRotate = false;
    	}
    	else{
    		autoRotate = true;
    	}
    	redraw();
    }

	@Override
	public void update(Observable o, Object arg) {
		redraw();
		
	}

	@Override
	public void toggle() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCurrentPart(GeneratedPart currentPart) {
    	curVO = Controller.getInstance();
		curVO.addObserver(this);
		curVO.setCurrentPart(currentPart);
	}

}

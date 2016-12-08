package bitSlicer.gui;

import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PGraphics3D;
import remixlab.proscene.*;

public class ProcessingWindow extends PApplet{
	private String filename;
	private PShape obj;
	private Scene scene;
	private String renderer = P3D;
	private boolean record = false;
	
	private InteractiveFrame frame;
	
	public ProcessingWindow (String filename) {
		this.filename = filename.replace("\\", "/");
		String[] args = new String[]{""};
		
		//obj = loadShape(filename.replace("\\", "/"));
		 
		PApplet.runSketch(args, this);
	}
	
    public void settings(){
    	size(1000,1000, renderer);  
    }

    public void setup(){
    	scene = new Scene(this);
    	scene.setRadius(2000);
    	scene.eye().showEntireScene();
    	scene.setPickingVisualHint(true);
    	
        obj = loadShape(filename);

        frame = new InteractiveFrame(scene, obj);
        frame.setHighlightingMode(InteractiveFrame.HighlightingMode.FRONT_PICKING_SHAPES);
    }

    public void draw(){
    	background(0);
    	lights();
    	fill(250, 0, 0);
    	
    	if (record) {
    		beginRaw("nervoussystem.obj.OBJExport", filename+"test.obj");
    	}    		
    	scene.drawFrames();
    	if (record) {
    		endRaw();
    		record = false;
    	}  
    }
    
    public void keyPressed()
    {
      if (key == 'r') {
        record = true;
      }
    }
}

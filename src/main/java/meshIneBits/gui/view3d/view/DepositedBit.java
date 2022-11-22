package meshIneBits.gui.view3d.view;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import javafx.util.Pair;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.builder.ExtrusionFromAreaService;
import meshIneBits.util.supportImportFile.DomParser;
import meshIneBits.util.supportImportFile.FallType;
import meshIneBits.util.supportImportFile.Reconstitute;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.event.MouseEvent;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.Scene;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class DepositedBit extends PApplet {
   // private int id=42;
   private int id=6;
    private PShape bitShape;
    private Scene scene;
    private CustomInteractiveFrame frame;
    private ArrayList<ArrayList<Pair<FallType, Path2D.Double>>> cutpaths;
    private com.jogamp.newt.opengl.GLWindow win;
    private PShape limit1;
    private PShape limit2;




    public static void main(String[]args){
        PApplet.main(DepositedBit.class.getCanonicalName());
    }

    @Override
    public void settings() {
        size(Visualization3DConfig.V3D_WINDOW_WIDTH, Visualization3DConfig.V3D_WINDOW_HEIGHT-100, P3D);
    }
    public void setup(){

        configWindow(
                "Deposited Bit",
                Visualization3DConfig.V3D_WINDOW_LOCATION_X,
                Visualization3DConfig.V3D_WINDOW_LOCATION_Y);
        init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS/2);
        init3DFrame();
        cutpaths = DomParser.parseXml(0);
        Area bitArea= Reconstitute.getInstance().recreateArea(cutpaths,id);
        bitShape = ExtrusionFromAreaService.getInstance()
                .buildShapeFromArea(this, bitArea, Visualization3DConfig.BIT_THICKNESS);

        limit1=createShape();
        limit1.beginShape();
        limit1.vertex((float) CraftConfig.lengthFull,0,0);
        limit1.vertex((float) CraftConfig.lengthFull,(float) CraftConfig.lengthFull,0);
        limit1.vertex((float) CraftConfig.lengthFull,(float) CraftConfig.lengthFull,(float) 0.001);
        limit1.endShape(PConstants.CLOSE);

        limit2=createShape();
        limit2.beginShape();
        limit2.vertex(0,(float) CraftConfig.bitWidth,0);
        limit2.vertex((float) CraftConfig.lengthFull,(float) CraftConfig.bitWidth,0);
        limit2.vertex((float) CraftConfig.lengthFull,(float) CraftConfig.bitWidth,(float) 0.001);
        limit2.endShape(PConstants.CLOSE);
    }

    @Override
    public void mouseClicked(MouseEvent event) {

        id=id+1;
        if(id% CraftConfig.nbBitesBatch==0){System.out.println("changing batch:");
            cutpaths = DomParser.parseXml(DomParser.getBatch_num()+1);}
        System.out.println("id_bit:"+id+" num_batch:"+DomParser.getBatch_num());
        Area bitArea= Reconstitute.getInstance().recreateArea(cutpaths,id);
        bitShape = ExtrusionFromAreaService.getInstance()
                .buildShapeFromArea(this, bitArea, Visualization3DConfig.BIT_THICKNESS);
    }

    @Override
    public void draw() {
        lights();
        background(200,200,200);
        pushMatrix();
        //translate(Visualization3DConfig.V3D_WINDOW_WIDTH/2,Visualization3DConfig.V3D_WINDOW_HEIGHT/2,0);
        translate((float)CraftConfig.lengthFull/2*6,(float)CraftConfig.bitWidth/2*6,0);
        scale(6);
        shape(bitShape);
        popMatrix();
        pushMatrix();
        scale(6);
        shape(limit1);
        shape(limit2);
        popMatrix();
    }

    private void setCloseOperation() {

        //Removing close listeners
        win = (com.jogamp.newt.opengl.GLWindow) surface.getNative();
        for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
            win.removeWindowListener(wl);
        }
        win.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        win.addWindowListener(new WindowAdapter() {
            public void windowDestroyed(WindowEvent e) {
                System.exit(0);
            }
        });
        win.addWindowListener(new WindowAdapter() {
            @Override
            public void windowResized(WindowEvent e) {
                super.windowResized(e);
                surface.setSize(win.getWidth(), win.getHeight());
            }
        });

    }

    private void init3DScene(Vec eyePosition, float radius) {
        scene = new Scene(this);
        scene.eye().setPosition(eyePosition);
        scene.eye().lookAt(scene.eye().sceneCenter());
        scene.setRadius(radius);
        scene.showAll();
        scene.disableKeyboardAgent();
        scene.toggleGridVisualHint();

    }
    private void init3DFrame() {
        if (scene == null) {
            return;
        }
        frame = new CustomInteractiveFrame(scene);
        //set position of frame in scene
        frame.translate(
                Visualization3DConfig.V3D_WINDOW_WIDTH/2,
                Visualization3DConfig.V3D_WINDOW_HEIGHT/2,
                100);
    }

    private void configWindow(String title, int locationX, int locationY) {
        this.surface.setResizable(true);
        this.surface.setTitle(title);
        this.surface.setLocation(locationX, locationY);
        setCloseOperation();
        // refresh();
    }
}

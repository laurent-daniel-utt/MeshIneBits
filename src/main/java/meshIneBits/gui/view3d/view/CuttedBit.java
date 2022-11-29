package meshIneBits.gui.view3d.view;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import controlP5.ControlEvent;
import javafx.util.Pair;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.builder.ExtrusionFromAreaService;
import meshIneBits.opcuaHelper.RobotCommander;
import meshIneBits.util.supportImportFile.DomParser;
import meshIneBits.util.supportImportFile.FallType;
import meshIneBits.util.supportImportFile.Reconstitute;
import processing.core.PConstants;
import processing.core.PShape;
import processing.event.MouseEvent;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.Scene;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class CuttedBit extends UIParameterWindow {
    // private int id=42;
    private int id=0;
    private PShape bitShape;
    private Scene scene;
    private CustomInteractiveFrame frame;
    private ArrayList<ArrayList<Pair<FallType, Path2D.Double>>> cutpaths;
    private com.jogamp.newt.opengl.GLWindow win;
    private PShape limit1;
    private PShape limit2;
    private int width;
    private int height;
    private int posx;
    private int posy;
    private int scale;
    private String title;
    private RobotCommander robotCommander;
    public CuttedBit(String title, RobotCommander robotCommander,int width,int height,int posx,int posy,int scale){
        this.title=title;
        this.robotCommander=robotCommander;
        this.width=width;
        this.height=height;
        this.posx=posx;
        this.posy=posy;
        this.scale=scale;
    }

    public String getTitle() {
        return title;
    }

    public static void main(String[]args){/*CuttedBit p=new CuttedBit("Cutted Bit",null);
        runSketch(new String[]{"--display=1", "Projector"},p);
        // PApplet.main(DepositedBit.class.getCanonicalName());
   */ }


    @Override
    public void settings() {
        size(width, height, P3D);
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {

    }

    @Override
    protected void generateButton() {

    }

    @Override
    protected void updateButton() {

    }

    @Override
    public void controlEvent(ControlEvent theEvent) {

    }
    public void setup(){

        configWindow(title,posx, posy);
        init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
        surface.setAlwaysOnTop(true);

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
        UpdateId();
        lights();
        background(200,200,200);
        pushMatrix();
        //translate(Visualization3DConfig.V3D_WINDOW_WIDTH/2,Visualization3DConfig.V3D_WINDOW_HEIGHT/2,0);
        translate((float)CraftConfig.lengthFull/2*scale,(float)CraftConfig.bitWidth/2*scale,0);
        scale(scale);
        shape(bitShape);
        popMatrix();
        pushMatrix();
        scale(scale);
        shape(limit1);
        shape(limit2);
        popMatrix();
    }
    public void UpdateId(){
        try {
            if(id!=robotCommander.getHoldingRegisters()[3]){
            updateShape(robotCommander.getHoldingRegisters()[3]);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void updateShape(int newId){

        if(id!=newId){
            id=newId;
            if(id% CraftConfig.nbBitesBatch==0){
                System.out.println("changing batch:");
                cutpaths = DomParser.parseXml(id/CraftConfig.nbBitesBatch);}
            System.out.println("id_bit:"+id+" num_batch:"+DomParser.getBatch_num());
            Area bitArea= Reconstitute.getInstance().recreateArea(cutpaths,id);
            bitShape = ExtrusionFromAreaService.getInstance()
                    .buildShapeFromArea(this, bitArea, Visualization3DConfig.BIT_THICKNESS);
        }
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
                closeWindow();
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

    private void configWindow(String title, int locationX, int locationY) {
        this.surface.setResizable(true);
        this.surface.setTitle(title);
        this.surface.setLocation(locationX, locationY);
        setCloseOperation();
        // refresh();
    }
}
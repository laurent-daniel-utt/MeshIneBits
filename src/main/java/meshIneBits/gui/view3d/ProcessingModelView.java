/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
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

import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.newt.util.MainThread;
import controlP5.*;
import controlP5.Button;
import javafx.util.Pair;
import meshIneBits.*;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.SubWindow;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.patterntemplates.ClassicBrickPattern;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;
import meshIneBits.util.Vector3;
import nervoussystem.obj.OBJExport;
import processing.core.PApplet;
import processing.core.PShape;
import processing.core.PSurface;
import processing.event.MouseEvent;
import processing.opengl.PJOGL;
import remixlab.dandelion.geom.Quat;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.InteractiveFrame;
import remixlab.proscene.Scene;

import java.awt.*;
import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static remixlab.bias.BogusEvent.CTRL;
import static remixlab.proscene.MouseAgent.*;

//import nervoussystem.obj.*;
/**
 * @author Vallon BENJAMIN
 * <p>
 * 3D view + GUI
 * Use Builder to creates Meshes
 */
public class ProcessingModelView extends PApplet implements Observer, SubWindow {
    public static final String IMPORTED_MODEL = "import";
    public static final int ANIMATION_BITS = 100;
    public static final int ANIMATION_LAYERS = 111;
    public static final int ANIMATION_BATCHES = 121;
    public static final int ANIMATION_FULL = 211;
    public static final int ANIMATION_CURRENT = 221;


    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final int BACKGROUND_COLOR = color(150, 150, 150);
    private float printerX;
    private float printerY;
    private float printerZ;

    private ArrayList<Integer> listIndexWorkingSpace= new ArrayList<Integer>();
    private double workingSpacePosition;

    private double minXDistancePoint;
    private double maxXDistancePoint;

    private double safetySpace=CraftConfig.margin;

    private Builder builder;
    private static ProcessingModelView currentInstance = null;
    private static Model model;
    private static ControllerView3D controllerView3D = null;

    private PShape newShape;
    private PShape newShapeMeshPaved;
    private Vector<Pair<Layer, PShape>> newShapeMapByLayer = new Vector<>();
    private Vector<Pair<Bit3D, PShape>> newShapeMapByBits = new Vector<>();
    private boolean updatingNewData = false;
    private InteractiveFrame NewFrame;


    private PShape shape;
    private PShape shapeMeshPaved;
    private Vector<Pair<Layer, PShape>> shapeMapByLayer;
    private Vector<Pair<Bit3D, PShape>> shapeMapByBits;
    private Scene scene;
    private InteractiveFrame frame;
    private ControlP5 cp5;

    private Textfield TFRotationX;
    private Textfield TFRotationY;
    private Textfield TFRotationZ;
    private Textfield TFPositionX;
    private Textfield TFPositionY;
    private Textfield TFPositionZ;

    private Slider sliderAnimation;
    private Slider sliderM;
    private Slider sliderB;

    private Button apply;
    private Button camera;
    private Button reset;
    private Button gravity;
    private Button animation;
    private Button export;
    private Button pauseButton;

    private Button speedUpButton;
    private Button speedDownButton;

    private Textlabel txt;
    private Textlabel modelSize;
    private Textlabel shortcut;
    private Textlabel slicingWarning;

    private Textarea tooltipRotation;
    private Textarea tooltipGravity;
    private Textarea tooltipReset;
    private Textarea tooltipCamera;
    private Textarea tooltipApply;
    private Textarea tooltipExport;
    private Textarea tooltipBits;
    private Textarea tooltipLayers;
    private Textarea tooltipBatch;
    private Textarea tooltipCurrent;
    private Textarea tooltipFull;



    private Toggle toggleViewModel;
    private Toggle toggleViewBits;
    private Toggle toggleBits;
    private Toggle toggleLayers;
    private Toggle toggleBatch;

    private Toggle toggleCurrent;
    private Toggle toggleFull;

    private boolean viewModel = true;
    // Animation variable
    private boolean isAnimated = false;

    //For Module Export OBJ
    private boolean exportOBJ=false;
    private boolean record=false;


    private int layerIndex = 0;
    //    private float fpsRatioSpeed = 2;
    private int lastFrames = 500;
    private final int frameMin = 10;
    private boolean pauseAnimation = false;
    private int counter =0;
    private int animationType = ANIMATION_LAYERS;
    private int animationWays = ANIMATION_FULL;
    private Vector<PShape> currentShapeMap;


    private DecimalFormat df;

    private boolean[] borders;
    private boolean viewMeshPaved = false;
    private boolean applied = false;
    private boolean isToggled = false;

    private double scale = 1;

    public static void startProcessingModelView() {
        if (!ControllerView3D.getInstance().isAvailable()) return;
        if (currentInstance == null) {
            PApplet.main(ProcessingModelView.class.getCanonicalName());
        }
    }


    public void settings() {
        currentInstance = this;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        size(screenSize.width-20, screenSize.height-60, P3D);
        PJOGL.setIcon("resources/icon.png");
    }

    private void setCloseOperation() {
        //Removing close listeners
        com.jogamp.newt.opengl.GLWindow win = (com.jogamp.newt.opengl.GLWindow) surface.getNative();
        for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
            win.removeWindowListener(wl);
        }

        win.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);

        win.addWindowListener(new WindowAdapter() {
            public void windowDestroyed(WindowEvent e) {
                controllerView3D.deleteObserver(currentInstance);
                currentInstance = null;
                builder.onTerminated();
                dispose();
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
    public ProcessingModelView() {
        controllerView3D = ControllerView3D.getInstance();
        controllerView3D.addObserver(this);


    }

    public static void closeInstance(){
        currentInstance=null;
        model=null;
    }
    /**
     *
     */
    public void setup() {
        this.surface.setResizable(true);
        this.surface.setTitle("MeshIneBits - Model view");
        if (model == null) {
            model = controllerView3D.getModel();
        }
        // each bool corresponds to 1 face of the workspace.
        // is true if the the shape is crossing the associated face.
        // borders[0] =  xmin border / borders[1] = xmax border ...
        borders = new boolean[6];
        for (int i = 0; i < 6; i++) {
            borders[i] = false;
        }
//
        scene = new Scene(this);
        scene.eye().setPosition(new Vec(0, 1, 1));
        scene.eye().lookAt(scene.eye().sceneCenter());
        scene.setRadius(2500);
        scene.showAll();
        scene.disableKeyboardAgent();
        scene.toggleGridVisualHint();
        builder = new Builder(this);
        setCloseOperation();
        shapeMapByLayer = new Vector<>();
        shapeMapByBits = new Vector<>();
        shape = createShape(GROUP);
        builder.buildShape(model, shape);
//        builder.buildLayers(shapeMap);
        shapeMeshPaved = builder.buildMeshPaved(shapeMapByLayer, shapeMapByBits);
//        frame = new InteractiveFrame(scene, shape);
        frame = new InteractiveFrame(scene, shape);
        frame.translate((float)controllerView3D.getModel().getPos().x,(float)controllerView3D.getModel().getPos().y,(float)controllerView3D.getModel().getPos().z);
        //       loadNewData();
        cp5 = new ControlP5(this);
        df = new DecimalFormat("#.##");
        df.setMaximumFractionDigits ( 2 );
        df.setRoundingMode(RoundingMode.CEILING);
//        frame.setDefaultMouseBindings();
        customFrameBindings(frame);
        applyGravity();

        createButtons(cp5);

        // Setup workspace
        printerX = CraftConfig.printerX;
        printerY = CraftConfig.printerY;
        printerZ = CraftConfig.printerZ;
        if (controllerView3D.getCurrentMesh().isSliced()){
            shape.setFill(color(205,92,92));
        }
    }

    private synchronized void loadNewData() {
        //if(!isToggled) return;
        newShape = this.createShape(GROUP);
        newShapeMapByLayer.clear();
        newShapeMapByBits.clear();
        builder.buildShape(model, newShape);
        shape = newShape;
        frame.setShape(shape);
        newShapeMeshPaved = builder.buildMeshPaved(newShapeMapByLayer, newShapeMapByBits);
        updatingNewData = true;
    }

    // used to Start export the model to OBJ
    public void keyPressed(){
        if (key =='s'|| key=='S'){
            record=true;
            counter +=1;
        }
    }

    @Override
    public void mouseWheel(MouseEvent event) {
        if (!frame.isEyeFrame()){
            modelSize.setText("Model Size :\n Depth:" + df.format(shape.getDepth() * frame.scaling()) + "\n Height :" + df.format(shape.getHeight() * frame.scaling()) + "\n Width : " + df.format(shape.getWidth() * frame.scaling()) + "\n Scale : " + df.format(frame.scaling()));
        }
        if (frame.scaling()!=1){
            shape.setFill(color(205,92,92));
        }
    }

    @Override
    public void mouseWheel() {
        super.mouseWheel();
    }

    private void customFrameBindings(InteractiveFrame frame) {
        frame.removeBindings();
        frame.setHighlightingMode(InteractiveFrame.HighlightingMode.NONE);
        frame.setPickingPrecision(InteractiveFrame.PickingPrecision.ADAPTIVE);
        frame.setGrabsInputThreshold(scene.radius() / 3);
        frame.setRotationSensitivity(3);
        if (!controllerView3D.getCurrentMesh().isSliced()){
            frame.setMotionBinding(CTRL, LEFT_CLICK_ID, "rotate");
            frame.setMotionBinding(WHEEL_ID, scene.is3D() ? (frame.isEyeFrame() ? "translateZ" : "scale") : "scale");
        }
        frame.setMotionBinding(CTRL, RIGHT_CLICK_ID, "translate");
    }

    private int compteur = 0;

    public void draw() {
        updateNewData();
        background(BACKGROUND_COLOR);
        lights();
        ambientLight(255, 255, 255);
        drawWorkspace();
        drawWorkingSpace();
        //mouseConstraints();

        // To start Export the model in .obj
        if (record){
            if (isAnimated){
                switch (animationType){
                    case ANIMATION_LAYERS:
                        switch (animationWays){
                            case ANIMATION_FULL:
                                beginRaw("nervoussystem.obj.OBJExport", model.getModelName()+"_"+"layer_Evolution_"+counter+".obj");
                                break;
                            case ANIMATION_CURRENT:
                                beginRaw("nervoussystem.obj.OBJExport", model.getModelName()+"_"+"Current_layer_"+counter+".obj");
                                break;
                        }
                        break;
                    case ANIMATION_BITS:
                        switch (animationWays){
                            case ANIMATION_FULL:
                                beginRaw("nervoussystem.obj.OBJExport", model.getModelName()+"_"+"bits_Evolution_"+counter+".obj");
                                break;
                            case ANIMATION_CURRENT:
                                beginRaw("nervoussystem.obj.OBJExport", model.getModelName()+"_"+"Current_bits_"+counter+".obj");
                                break;
                        }
                        break;
                    case ANIMATION_BATCHES:
                        switch (animationWays){
                            case ANIMATION_FULL:
                                beginRaw("nervoussystem.obj.OBJExport", model.getModelName()+"_"+"Batch_Evolution_"+counter+".obj");
                                break;
                            case ANIMATION_CURRENT:
                                beginRaw("nervoussystem.obj.OBJExport", model.getModelName()+"_"+"Current_batch_"+counter+".obj");
                                break;
                        }
                        break;
                }
            }
            if (viewMeshPaved){
                beginRaw("nervoussystem.obj.OBJExport", model.getModelName()+"_"+"Paved"+".obj");
            }
            if (viewModel){
                beginRaw("nervoussystem.obj.OBJExport", model.getModelName()+".obj");
            }
        }
        if (viewModel) scene.drawFrames();
        animationProcess();
        if (viewMeshPaved) {
            drawBits();
        }
        //To end the export
        if (record){
            endRaw();
            record=false;
        }
//        if(isAnimated)drawAnimation();
        scene.beginScreenDrawing();
        updateButtons();
        cp5.draw();
        displayTooltips();
        scene.endScreenDrawing();

    }

    private void updateNewData() {
        if (updatingNewData) {
            shapeMapByLayer = new Vector<>(newShapeMapByLayer);
            shapeMapByBits = new Vector<>(newShapeMapByBits);
            shapeMeshPaved = newShapeMeshPaved;
            //shape=this.createShape(GROUP);
            //builder.buildShape(model,shape);
            System.out.println("shape size " + shape.getChildCount());
            System.out.println("new shape size " + newShape.getChildCount());
            shape = newShape;
            frame.resetShape();
            frame.setShape(shape);
            updatingNewData = false;

        }
    }

    /*
     * Draw 3D Bits
     *
     */

    private void drawBits() {
        Vector3 v = controllerView3D.getModel().getPos();
        pushMatrix();
        translate((float) v.x, (float) v.y, (float) v.z);
        shape(shapeMeshPaved);
        popMatrix();
    }

    /*
     *  Display the printer workspace
     *
     */
    private void drawWorkspace() {
        pushMatrix();
        noFill();
        translate(0, 0, printerZ / 2);
        strokeWeight(2);
        stroke(0);
        box(printerX, printerY, printerZ);
        popMatrix();
        scene.pg().pushStyle();
        stroke(80);
        scene.pg().beginShape(LINES);
//        stroke(255, 255, 0);
        for (int i = -(int) printerX / 2; i <= printerX / 2; i += 100) {
            vertex(i, printerY / 2, 0);
            vertex(i, -printerY / 2, 0);

        }
        for (int i = -(int) printerY / 2; i <= printerY / 2; i += 100) {
            vertex(printerX / 2, i, 0);
            vertex(-printerX / 2, i, 0);
        }
        scene.pg().endShape();
        scene.pg().popStyle();

    }
    private void drawWorkingSpace(){
        stroke(255,0,0);
        rect(-printerX/2 -CraftConfig.workingWidth-20,-printerY/2,CraftConfig.workingWidth,CraftConfig.printerY);
    }

    /*
     *  GUI - Buttons + tooltips
     *
     */

    private void createButtons(ControlP5 cp5) {
        GLWindow win = ((GLWindow) surface.getNative());

        TFRotationX=cp5.addTextfield("RotationX").setSize(45, 30)
                .setInputFilter(0).setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial", 15));

        TFRotationY=cp5.addTextfield("RotationY").setSize(45, 30)
                .setInputFilter(0).setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial", 15));

        TFRotationZ= cp5.addTextfield("RotationZ").setSize(45, 30)
                .setInputFilter(0).setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial", 15));

        TFPositionX= cp5.addTextfield("PositionX").setSize(45, 30)
                .setInputFilter(0).setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial", 15));

        TFPositionY=cp5.addTextfield("PositionY").setSize(45, 30).setInputFilter(0)
                .setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial", 15));

        TFPositionZ= cp5.addTextfield("PositionZ").setSize(45, 30).setInputFilter(0)
                .setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial", 15));

        gravity = cp5.addButton("ApplyGravity").setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial", 15));
        int color = gravity.getColor().getBackground();
        reset = cp5.addButton("Reset").setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial", 15));
        camera = cp5.addButton("CenterCamera").setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial", 15));
        apply= cp5.addButton("Apply").setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial", 15));
        animation = cp5.addButton("Animation").setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial", 15));
        export = cp5.addButton("Export").setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial", 15));
        toggleBits = cp5.addToggle("byBits")
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel("by Bits")
                .setFont(createFont("arial", 15))
                .setState(false);
        toggleBatch= cp5.addToggle("byBatches")
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel("by Batches")
                .setFont(createFont("arial", 15))
                .setState(false);
        toggleLayers= cp5.addToggle("byLayers")
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel("by Layers")
                .setFont(createFont("arial", 15))
                .setState(true);
        toggleCurrent = cp5.addToggle("current")
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel("Current")
                .setFont(createFont("arial", 15))
                .setState(false);
        toggleFull = cp5.addToggle("full")
                .setSize(20, 20)
                .setColorBackground(color(255, 250))
                .setColorActive(color).setColorForeground(color + 50)
                .setLabel("Full")
                .setFont(createFont("arial", 15))
                .setState(true);

        sliderAnimation = cp5.addSlider("animationSlider");
        sliderAnimation.setVisible(false).setSize(200, 30).getCaptionLabel().setText("")
                .setFont(createFont("arial", 15));
        sliderAnimation.onChange(e -> {
            if (pauseAnimation) this.layerIndex = (int) sliderAnimation.getValue();
        });

        speedUpButton= cp5.addButton("animationSpeedUp").setVisible(false).setSize(30, 30).setColorLabel(255).setFont(createFont("arial", 15));
        speedUpButton.getCaptionLabel().setText(">>");
        speedDownButton= cp5.addButton("animationSpeedDown").setVisible(false).setSize(30, 30).setColorLabel(255).setFont(createFont("arial", 15));
        speedDownButton.getCaptionLabel().setText("<<");
        pauseButton = cp5.addButton("PauseAnimation").setVisible(false).setSize(50, 30).setColorLabel(255);
        pauseButton.getCaptionLabel().setText("Pause").setFont(createFont("arial", 15));
        pauseButton.onClick(e -> {
            pauseAnimation = !pauseAnimation;
            if (pauseAnimation) {
                executorService.shutdownNow();
                pauseButton.getCaptionLabel().setText("Start");
            } else {
                executorService = Executors.newSingleThreadExecutor();
                pauseButton.getCaptionLabel().setText("Pause");
            }
        });


        tooltipGravity = cp5.addTextarea("tooltipGravity").setText("Set the model").setSize(90, 18)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar().setFont(createFont("ariel", 10));
        tooltipGravity.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipRotation = cp5.addTextarea("tooltipRotation").setText("You can't rotate a sliced Model").setSize(220, 36)
                .setColorBackground(color(255,0,0))
                .setColor(color(50)).setFont(createFont("arial", 15)).setLineHeight(12).hide()
                .hideScrollbar().setFont(createFont("ariel", 15));
        tooltipRotation.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipReset = cp5.addTextarea("tooltipReset").setText("Reset to zero").setSize(85, 18)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipReset.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipCamera = cp5.addTextarea("tooltipCamera").setText("Center model").setSize(105, 18)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipCamera.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipApply = cp5.addTextarea("tooltipApply").setText("Apply the modifications").setSize(145, 18)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipApply.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipExport = cp5.addTextarea("tooltipExport").setText("Export to OBJ").setSize(145, 18)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipExport.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipBits = cp5.addTextarea("tooltipBits").setText("Set Animation by Bits").setSize(145, 18)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipBits.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipBatch = cp5.addTextarea("tooltipBatch").setText("Set Animation by Batch").setSize(145, 18)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipBatch.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipLayers = cp5.addTextarea("tooltipLayers").setText("Set Animation by Layers").setSize(145, 18)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipLayers.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipCurrent = cp5.addTextarea("tooltipCurrent").setText("Animation show \n only the current").setSize(145, 36)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipCurrent.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        tooltipFull = cp5.addTextarea("tooltipFull").setText("Animation show \n the evolution").setSize(145, 36)
                .setColorBackground(color(220))
                .setColor(color(50)).setFont(createFont("arial", 10)).setLineHeight(12).hide()
                .hideScrollbar();
        tooltipFull.getValueLabel().getStyle().setMargin(1, 0, 0, 5);

        txt = cp5.addTextlabel("label").setText("Current Position : (0,0,0)")
                .setSize(80, 40).setColor(255).setFont(createFont("arial", 15));

        modelSize = cp5.addTextlabel("model size").setText("Model Size :\n Depth:" + df.format(shape.getDepth()) + "\n Height :" + df.format(shape.getHeight()) + "\n Width : " + df.format(shape.getWidth())+ "\n Scale : " + df.format(frame.scaling()))
                .setColor(255).setFont(createFont("arial", 15));

        shortcut= cp5.addTextlabel("shortcut").setText("Shortcut : \n Rotation : CTRL + Mouse Left Click, Cannot be used when Mesh is sliced \n Translation : CTRL + Mouse Right Click \n Change Model Size : Mouse on the Model + Mouse Wheel , Cannot be used when Mesh is sliced\n Zoom : Mouse Wheel\n Export to Obj: press button 'S'" )
                .setColor(255).setFont(createFont("arial", 15));


        slicingWarning= cp5.addTextlabel("slicingWarning").setText("The Model is Sliced \n You can't rotate \n You can't scale" )
                .setColor(255).setFont(createFont("arial", 20)).hide();
        if (controllerView3D.getCurrentMesh().isSliced()){
            slicingWarning.show();
        }

        toggleViewModel = cp5.addToggle("Model").setSize(20, 20)
                .setColorBackground(color(255, 250)).setColorActive(color).setColorForeground(color + 50).toggle().setFont(createFont("arial", 15));

        toggleViewBits = cp5.addToggle("Bits").setSize(20, 20)
                .setColorBackground(color(255, 250)).setColorActive(color).setColorForeground(color + 50).setFont(createFont("arial", 15));

        sliderM = cp5.addSlider("M").setRange(0, 100).setSize(100, 20).setValue(100)
                .setColorForeground(color + 50).setColorActive(color + 40).setFont(createFont("arial", 15));
        sliderB = cp5.addSlider("B").setRange(0, 100).setSize(100, 20).setValue(100)
                .setColorForeground(color + 50).setColorActive(color + 40).setFont(createFont("arial", 15));


        cp5.setAutoDraw(false);
    }

    private void updateButtons() {
        GLWindow win = ((GLWindow) surface.getNative());
        modelSize.setPosition(win.getWidth() - 150, 10);
        txt.setPosition(win.getWidth() - 150, win.getHeight() - 80);
        txt.setText("Current position :\n" + " x : " + df.format(frame.position().x()) + "\n y : " + df.format(frame.position().y()) + "\n z : " + df.format(frame.position().z()));
        slicingWarning.setPosition(win.getWidth()-450, 10);
        shortcut.setPosition(30, win.getHeight() - 120);

        tooltipRotation.setPosition(30, win.getHeight()/5-30);
        TFRotationX.setPosition(30,win.getHeight()/5);
        TFRotationY.setPosition(30,win.getHeight()/5 + 60);
        TFRotationZ.setPosition(30,win.getHeight()/5 + 120);
        TFPositionX.setPosition(130,win.getHeight()/5 );
        TFPositionY.setPosition(130,win.getHeight()/5 + 60);
        TFPositionZ.setPosition(130,win.getHeight()/5 + 120);

        sliderM.setPosition(30, win.getHeight()/5 + 180);
        sliderB.setPosition(30, win.getHeight()/5 + 210);

        toggleViewModel.setPosition(30, win.getHeight()/5 + 250);
        toggleViewBits.setPosition(90, win.getHeight()/5 + 250);

        apply.setPosition(win.getWidth()-200, win.getHeight()/4);
        tooltipApply.setPosition(win.getWidth()-200, win.getHeight()/4-18);
        gravity.setPosition(win.getWidth()-200, win.getHeight()/4+50);
        tooltipGravity.setPosition(win.getWidth()-200, win.getHeight()/4+32);
        reset.setPosition(win.getWidth()-200, win.getHeight()/4+100);
        tooltipReset.setPosition(win.getWidth()-200, win.getHeight()/4+82);
        camera.setPosition(win.getWidth()-200, win.getHeight()/4+150);
        tooltipCamera.setPosition(win.getWidth()-200, win.getHeight()/4+132);
        animation.setPosition(win.getWidth()-200, win.getHeight()/4+200);
        export.setPosition(win.getWidth()-200, win.getHeight()/4+365);
        tooltipExport.setPosition(win.getWidth()-200, win.getHeight()/4+347);

        toggleBits.setPosition(win.getWidth()-200, win.getHeight()/4+230);
        tooltipBits.setPosition(win.getWidth()-200, win.getHeight()/4+212);
        toggleBatch.setPosition(win.getWidth()-200, win.getHeight()/4+275);
        tooltipBatch.setPosition(win.getWidth()-200, win.getHeight()/4+257);
        toggleLayers.setPosition(win.getWidth()-200, win.getHeight()/4+320);
        tooltipLayers.setPosition(win.getWidth()-200, win.getHeight()/4+302);
        toggleCurrent.setPosition(win.getWidth()-100, win.getHeight()/4+230);
        tooltipCurrent.setPosition(win.getWidth()-100, win.getHeight()/4+250);
        toggleFull.setPosition(win.getWidth()-100, win.getHeight()/4+275);
        tooltipFull.setPosition(win.getWidth()-100, win.getHeight()/4+295);
        speedDownButton.setPosition(win.getWidth()/2+20, win.getHeight()-50);
        speedUpButton.setPosition(win.getWidth()/2+60, win.getHeight()-50);
        pauseButton.setPosition(win.getWidth()/2+100, win.getHeight()-50);
        sliderAnimation.setPosition(win.getWidth()/2-200, win.getHeight()-50);

        if (mouseX < 100) {
            scene.disableMotionAgent();
        } else {
            scene.enableMotionAgent();
        }
    }

    private void displayTooltips() {
        tooltipGravity.hide();
        tooltipReset.hide();
        tooltipCamera.hide();
        tooltipApply.hide();
        tooltipExport.hide();
        tooltipBits.hide();
        tooltipBatch.hide();
        tooltipLayers.hide();
        tooltipCurrent.hide();
        tooltipFull.hide();

        float[] rotationTooltipsPostition= tooltipRotation.getPosition();
        float[] gravityPosition= tooltipGravity.getPosition();
        float[] resetPosition= tooltipReset.getPosition();
        float[] cameraPosition= tooltipCamera.getPosition();
        float[] applyPosition= tooltipApply.getPosition();
        float[] exportPosition= tooltipExport.getPosition();
        float[] byBitsPosition= tooltipBits.getPosition();
        float[] byBatchPosition= tooltipBatch.getPosition();
        float[] byLayersPosition= tooltipLayers.getPosition();
        float[] currentPosition= toggleCurrent.getPosition();
        float[] fullPosition= toggleFull.getPosition();


        if ((mouseX > rotationTooltipsPostition[0]) && (mouseX < rotationTooltipsPostition[0]+220) && (mouseY > rotationTooltipsPostition[1]) && (mouseY < rotationTooltipsPostition[1]+36)){
            tooltipRotation.hide();
        }

        if ((mouseX > gravityPosition[0]) && (mouseX < gravityPosition[0]+140) && (mouseY > gravityPosition[1]) && (mouseY < gravityPosition[1]+48)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipGravity.show();
            }
        }
        if ((mouseX > resetPosition[0]) && (mouseX < resetPosition[0]+140) && (mouseY > resetPosition[1]) && (mouseY < resetPosition[1]+48)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipReset.show();
            }
        }
        if ((mouseX > cameraPosition[0]) && (mouseX < cameraPosition[0]+140) && (mouseY > cameraPosition[1]) && (mouseY < cameraPosition[1]+48)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipCamera.show();
            }
        }
        if ((mouseX > applyPosition[0]) && (mouseX < applyPosition[0]+140) && (mouseY > applyPosition[1]) && (mouseY < applyPosition[1]+48)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipApply.show();
            }
        }
        if ((mouseX > exportPosition[0]) && (mouseX < exportPosition[0]+140) && (mouseY > exportPosition[1]) && (mouseY < exportPosition[1]+48)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipExport.show();
            }
        }
        if ((mouseX > byBitsPosition[0]) && (mouseX < byBitsPosition[0]+20) && (mouseY > byBitsPosition[1]) && (mouseY < byBitsPosition[1]+40)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipBits.show();
            }
        }
        if ((mouseX > byBatchPosition[0]) && (mouseX < byBatchPosition[0]+20) && (mouseY > byBatchPosition[1]) && (mouseY < byBatchPosition[1]+40)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipBatch.show();
            }
        }
        if ((mouseX > byLayersPosition[0]) && (mouseX < byLayersPosition[0]+20) && (mouseY > byLayersPosition[1]) && (mouseY < byLayersPosition[1]+40)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipLayers.show();
            }
        }
        if ((mouseX > currentPosition[0]) && (mouseX < currentPosition[0]+20) && (mouseY > currentPosition[1]) && (mouseY < currentPosition[1]+20)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipCurrent.show();
            }
        }
        if ((mouseX > fullPosition[0]) && (mouseX < fullPosition[0]+20) && (mouseY > fullPosition[1]) && (mouseY < fullPosition[1]+20)) {
            if ((pmouseX - mouseX) == 0 && (pmouseY - mouseY) == 0) {
                tooltipFull.show();
            }
        }
    }

    /*
     * Rotate the shape
     *
     */
    private void rotateShape(float angleX, float angleY, float angleZ) {
        if (!controllerView3D.getCurrentMesh().isSliced()){
            applied = false;
            Quat r = new Quat();
            float angXRad = (float) Math.toRadians(angleX);
            float angYRad = (float) Math.toRadians(angleY);
            float angZRad = (float) Math.toRadians(angleZ);
            r.fromEulerAngles(angXRad, angYRad, angZRad);
            frame.rotate(r);
            applyGravity();
        }
        else{
            tooltipRotation.show();
        }
    }

    /*
     *	Move the shape
     *
     */
    private void translateShape(float transX, float transY, float transZ) {
        applied = false;
        frame.translate(transX, transY, transZ);
        boolean checkIn = checkShapeInWorkspace();
        if (!checkIn) {
            if (borders[0]) {
                frame.translate((-printerX / 2 - (float) getMinShape().x), 0, 0);
            }
            if (borders[1]) {
                frame.translate((printerX / 2 - (float) getMaxShape().x), 0, 0);
            }
            if (borders[2]) {
                frame.translate(0, (-printerY / 2 - (float) getMinShape().y), 0);
            }
            if (borders[3]) {
                frame.translate(0, (printerY / 2 - (float) getMaxShape().y), 0);
            }
            if (borders[4]) {
                frame.translate(0, 0, (-(float) getMinShape().z));
            }
            if (borders[5]) {
                frame.translate(0, 0, (printerZ - (float) getMaxShape().z));
            }
        }
    }

    /**
     * Return minimum x, y, and z of the shape
     */

    private Vector3 getMinShape() {
        double minx = Double.MAX_VALUE;
        double miny = Double.MAX_VALUE;
        double minz = Double.MAX_VALUE;
        int size = shape.getChildCount();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < 3; j++) {
                Vec vertex = new Vec(shape.getChild(i).getVertex(j).x, shape.getChild(i).getVertex(j).y, shape.getChild(i).getVertex(j).z);
                Vec v = frame.inverseCoordinatesOf(vertex);
                if (minx > v.x()) {
                    minx = v.x();
                }
                if (miny > v.y()) {
                    miny = v.y();
                }
                if (minz > v.z()) {
                    minz = v.z();
                }
            }
        }
        return new Vector3(minx, miny, minz);
    }

    /**
     * Return maximum x, y, and z of the shape
     */

    private Vector3 getMaxShape() {
        double maxx = -printerX;
        double maxy = -printerY;
        double maxz = -printerZ;
        int size = shape.getChildCount();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < 3; j++) {
                Vec vertex = new Vec(shape.getChild(i).getVertex(j).x, shape.getChild(i).getVertex(j).y, shape.getChild(i).getVertex(j).z);
                Vec v = frame.inverseCoordinatesOf(vertex);
                if (maxx < v.x()) {
                    maxx = v.x();
                }
                if (maxy < v.y()) {
                    maxy = v.y();
                }
                if (maxz < v.z()) {
                    maxz = v.z();
                }
            }
        }
        return new Vector3(maxx, maxy, maxz);
    }

    /*
     * 	Apply modifications of the shape to the model
     *
     */
    private void applyRotation() {
        model.rotate(frame.rotation());
    }

    private void applyTranslation() {
        model.setPos(new Vector3(frame.position().x(), frame.position().y(), 0));
        model.move(new Vector3(0, 0, -model.getMin().z));


    }

    private void applyGravity() {
        float minZ = (float) getMinShape().z;
        if (minZ != 0) {
            translateShape(0,0,-minZ);
        }
    }

    /*
     *	Reset the orientation and the position of the shape
     *
     */
    private void resetModel() {
        model.rotate(frame.rotation().inverse());
        model.setPos(new Vector3(0, 0, 0));
        frame.setPosition(new Vec(0, 0, 0));
        frame.rotate(frame.rotation().inverse());
        applyGravity();
    }

    /*
     *  Move camera to center the shape
     */
    private void centerCamera() {
        scene.eye().setPosition(new Vec(frame.position().x(), 3000, 3000));
        scene.eye().lookAt(frame.position());
    }

    private void fixPositionCamera(float x,float y, float z) {
        scene.eye().setPosition(new Vec(x, y, z));
    }
    private void fixAngleCamera(float x,float y, float z){
        scene.eye().lookAt(new Vec(x,y,z));
    }

    /**
     * Check if the model is in the workspace
     *
     * @return true if in the workspace
     */
    private boolean checkShapeInWorkspace() {
        for (int i = 0; i < 6; i++) {
            borders[i] = false;
        }
        float minX = -printerX / 2;
        float maxX = printerX / 2;
        float minY = -printerY / 2;
        float maxY = printerY / 2;
        float minZ = 0;
        float maxZ = printerZ;
        Vector3 vMin = getMinShape();
        Vec minPos = new Vec((float) vMin.x, (float) vMin.y, (float) vMin.z);
        Vector3 vMax = getMaxShape();
        Vec maxPos = new Vec((float) vMax.x, (float) vMax.y, (float) vMax.z);
        boolean inWorkspace = true;
        if (minPos.x() < minX) {
            inWorkspace = false;
            borders[0] = true;
        }
        if (maxPos.x() >= maxX) {
            inWorkspace = false;
            borders[1] = true;
        }
        if (minPos.y() < minY) {
            inWorkspace = false;
            borders[2] = true;
        }
        if (maxPos.y() >= maxY) {
            inWorkspace = false;
            borders[3] = true;
        }
        if (minPos.z() < minZ) {
            inWorkspace = false;
            borders[4] = true;
        }
        if (maxPos.z() >= maxZ) {
            inWorkspace = false;
            borders[5] = true;
        }
        return inWorkspace;
    }

    private void mouseConstraints() {
        boolean checkIn = checkShapeInWorkspace();
        if (!checkIn) {
            if (borders[0]) {
                frame.translate((-printerX / 2 - (float) getMinShape().x), 0, 0);
            }
            if (borders[1]) {
                frame.translate((printerX / 2 - (float) getMaxShape().x), 0, 0);
            }
            if (borders[2]) {
                frame.translate(0, (-printerY / 2 - (float) getMinShape().y), 0);
            }
            if (borders[3]) {
                frame.translate(0, (printerY / 2 - (float) getMaxShape().y), 0);
            }
            if (borders[4]) {
                frame.translate(0, 0, (-(float) getMinShape().z));
            }
            if (borders[5]) {
                frame.translate(0, 0, (printerZ - (float) getMaxShape().z));
            }
        }
    }

    /**
     * Listeners for GUI
     *
     * @param theValue in float
     */
    public void RotationX(String theValue) {
        float angle = Float.parseFloat(theValue);
        rotateShape(angle, 0, 0);
    }
    public void RotationY(String theValue) {
        float angle = Float.parseFloat(theValue);
        rotateShape(0, angle, 0);
    }

    public void RotationZ(String theValue) {
        float angle = Float.parseFloat(theValue);
        rotateShape(0, 0, angle);
    }

    @SuppressWarnings("unused")
    public void PositionX(String theValue) {
        float pos = Float.parseFloat(theValue);
        translateShape(pos, 0, 0);
    }

    @SuppressWarnings("unused")
    public void PositionY(String theValue) {
        float pos = Float.parseFloat(theValue);
        translateShape(0, pos, 0);
    }

    @SuppressWarnings("unused")
    public void PositionZ(String theValue) {
        float pos = Float.parseFloat(theValue);
        translateShape(0, 0, pos);
    }

    @SuppressWarnings("unused")
    public void Reset(float theValue) {
        resetModel();
        centerCamera();
        viewMeshPaved = false;
        toggleViewBits.setState(false);
        toggleViewModel.setState(true);
        applied = false;

    }

    @SuppressWarnings("unused")
    public void ApplyGravity(float theValue) {
        applyGravity();
    }

    @SuppressWarnings("unused")
    public void CenterCamera(float theValue) {
        centerCamera();
    }

    @SuppressWarnings("unused")
    public void Apply(float theValue) {
        if (!applied) {
            applyRotation();
            applyScale();
            applyTranslation();
            applied = true;
        }
        applyGravity();
    }

    private void applyScale() {
        if (frame.scaling() != scale) {
            model.applyScale(frame.scaling());
            scale = frame.scaling();
            modelSize.setText("Model Size :\n Depth:" + df.format(shape.getDepth()*scale) + "\n Height :" +df.format(shape.getHeight()*scale) + "\n Width : " + df.format(shape.getWidth()*scale) + "\n Scale : " + df.format(scale));
        }
        if (frame.scaling()!=1){
            shape.setFill(color(205,92,92));
        }
    }

    public void Model(boolean flag) {
        resetModeView();
        viewModel = flag;
        if (!flag) {
            toggleViewBits.setState(false);
            shape.setVisible(false);
            frame.removeMotionBindings();
        } else {
            shape.setVisible(true);
            customFrameBindings(frame);
        }
    }

    public void Bits(boolean flag) {
        resetModeView();
        shape.setVisible(!shape.isVisible());
        viewMeshPaved = flag;
    }

    @SuppressWarnings("unused")
    public void Animation() {
        isAnimated = !isAnimated;

        if (!isAnimated) {
            viewMeshPaved = true;
            if (executorService != null && !executorService.isShutdown()) executorService.shutdownNow();
            cp5.getController("animationSlider").setVisible(false);
            cp5.getController("animationSpeedUp").setVisible(false);
            cp5.getController("animationSpeedDown").setVisible(false);
            cp5.getController("PauseAnimation").setVisible(false);
            cp5.getController("byBits").setVisible(true);
        } else {

            executorService = Executors.newSingleThreadExecutor();
            viewMeshPaved = false;
            this.layerIndex = 0;
            Model(false);

            cp5.getController("animationSpeedUp").setVisible(true);
            cp5.getController("animationSpeedDown").setVisible(true);
            cp5.getController("PauseAnimation").setVisible(true);
            cp5.getController("byBits").setVisible(true);

            Vector<PShape> current = new Vector<>();
            switch (animationType) {
                case ANIMATION_BITS:
                    workingSpacePosition= printerX/2 -CraftConfig.workingWidth-20;

                    shapeMapByBits.forEach((ele) -> {
                        Bit3D currentBit= ele.getKey();
                        //init + find the min and max position x of the distance points from the currentBit.
                        Vector<Vector2> allDistancePoints=currentBit.getTwoDistantPointsInMeshCoordinate();
                        minXDistancePoint = allDistancePoints.get(0).x;
                        maxXDistancePoint = allDistancePoints.get(0).x;
                        for (int i=0; i<allDistancePoints.size();i++){
                            if (allDistancePoints.get(i).x<minXDistancePoint){
                                minXDistancePoint=allDistancePoints.get(i).x;
                            }
                            if (allDistancePoints.get(i).x>maxXDistancePoint){
                                maxXDistancePoint=allDistancePoints.get(i).x;
                            }
                        }

                        // Look if we need to move working space
                        if (workingSpacePosition== printerX/2 -CraftConfig.workingWidth-20){
                            workingSpacePosition= minXDistancePoint-safetySpace;
                            if (!exportOBJ){
                                current.add(createShape(RECT, Math.round(workingSpacePosition),-printerY/2,CraftConfig.workingWidth,CraftConfig.printerY));
                                listIndexWorkingSpace.add(0);
                            }

                        }
                        if (Math.round(minXDistancePoint-safetySpace) <= workingSpacePosition || Math.round(maxXDistancePoint+safetySpace) >= (workingSpacePosition+CraftConfig.workingWidth) ){
                            workingSpacePosition = minXDistancePoint-safetySpace;
                            if (!exportOBJ){
                                current.add(createShape(RECT, Math.round(workingSpacePosition),-printerY/2,CraftConfig.workingWidth,CraftConfig.printerY));
                                listIndexWorkingSpace.add(current.size()-1);
                            }

                        }
                        current.add(shapeMapByBits.get(shapeMapByBits.size()-1).getValue());
                        current.add(ele.getValue());
                    });
                    break;
                case ANIMATION_LAYERS:
                    shapeMapByLayer.forEach((ele) -> {
                        current.add(ele.getValue());
                    });
                    current.add(shapeMapByLayer.get(shapeMapByLayer.size()-1).getValue());
                    break;
                case ANIMATION_BATCHES:
                    int numberOfBatches= controllerView3D.getCurrentMesh().getScheduler().getSubBitBatch(shapeMapByBits.get(shapeMapByBits.size()-1).getKey());
                    for (int i = 0; i<=numberOfBatches; i++){
                        current.add(builder.getBatchPShapeForm(shapeMapByBits,i));
                    }
                    current.add(builder.getBatchPShapeForm(shapeMapByBits,numberOfBatches));
                    break;
            }
            currentShapeMap = current;
            ((Slider) (cp5.getController("animationSlider"))).setRange(0, currentShapeMap.size()).setValue(0).setVisible(true);
        }
    }

    public void Export() {
        exportOBJ= !exportOBJ;
        Animation();
    }

    @SuppressWarnings("unused")
    public void animationSpeedUp() {
//        fpsRatioSpeed += 0.5;
        int value = lastFrames / 2;
        lastFrames = Math.max(value, frameMin);
        System.out.println(lastFrames);

    }

    @SuppressWarnings("unused")
    public void animationSpeedDown() {
//        fpsRatioSpeed -= 0.5;
        lastFrames = lastFrames * 2;
        System.out.println(lastFrames);

    }

    public void animationProcess() {

        if (!this.isAnimated){
            exportOBJ=false;
            counter=0;
            return;
        }

        //increase layer number
        if (!pauseAnimation) increaseLayerIndex();
        //update the value of
        if (!pauseAnimation) cp5.getController("animationSlider").setValue(this.layerIndex);

        // Boucle de raffraichissement
        switch (animationType){
            case ANIMATION_LAYERS:
                switch (animationWays){
                    case ANIMATION_FULL:
                        for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex)) {
                            aShapeMap.setVisible(true);
                        }
                        break;
                    case ANIMATION_CURRENT:
                        for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex)) {
                            aShapeMap.setVisible(true);
                        }
                        if (this.layerIndex != 0) {
                            for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex-1)) {
                                aShapeMap.setVisible(false);
                            }
                        }
                        break;
                }
                break;
            case ANIMATION_BATCHES:
                switch (animationWays){
                    case ANIMATION_FULL:
                        for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex)) {
                            aShapeMap.setVisible(true);
                        }
                        break;
                    case ANIMATION_CURRENT:
                        for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex)) {
                            aShapeMap.setVisible(true);
                        }
                        if (this.layerIndex != 0) {
                            for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex-1)) {
                                aShapeMap.setVisible(false);
                            }
                        }
                        break;
                }
                break;
            case ANIMATION_BITS:
                switch (animationWays){
                    case ANIMATION_FULL:
                        for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex)) {
                            aShapeMap.setVisible(true);
                        }
                        break;
                    case ANIMATION_CURRENT:
                        for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex)) {
                            aShapeMap.setVisible(true);
                        }
                        if (this.layerIndex != 0) {
                            for (PShape aShapeMap : currentShapeMap.subList(0, this.layerIndex-1)) {
                                aShapeMap.setVisible(false);
                            }
                        }
                        break;
                }
                if (!exportOBJ){
                    //Hide old workingSpace in the Animation
                    int lastIndexWorkingspace=0;
                    for (int i: listIndexWorkingSpace){
                        if (i<this.layerIndex){
                            lastIndexWorkingspace++;
                        }
                    }
                    if(lastIndexWorkingspace!=0){
                        for (int i=0; i<lastIndexWorkingspace-1;i++){
                            currentShapeMap.get(listIndexWorkingSpace.get(i)).setVisible(false);
                        }
                    }
                }
                break;
        }

        if (this.layerIndex < currentShapeMap.size()) {
            for (PShape aShapeMap : currentShapeMap.subList(this.layerIndex + 1, currentShapeMap.size())) {
                aShapeMap.setVisible(false);
            }
        }
        Vector3 v = controllerView3D.getCurrentMesh().getModel().getPos();
        pushMatrix();
        translate((float) v.x, (float) v.y, (float) v.z);
        for (PShape aShapeMap : currentShapeMap) {
//            if (currentShapeMap == shapeMapByBits) {
//                pushMatrix();
//                shape(aShapeMap.getValue());
//                popMatrix();
//            } else {
            shape(aShapeMap);
//            }
        }
        popMatrix();


    }

    private void increaseLayerIndex() {
        executorService.submit(() -> {
            try {
                Thread.sleep(lastFrames);
            } catch (InterruptedException e) {
                System.out.println("Thread shutdown");
            }
            this.layerIndex = (this.layerIndex + 1) % this.currentShapeMap.size();

            // done when we export to obj
            if (exportOBJ){
                if (animationType==ANIMATION_BITS && animationWays==ANIMATION_CURRENT){
                    if (this.layerIndex!=0){
                        Bit3D bit= shapeMapByBits.get(this.layerIndex-1).getKey();
                        float bitOrientation = (float) bit.getOrientation().getEquivalentAngle() * (float)Math.PI/180;
                        float x = (float) bit.getLiftPoints().get(0).x;
                        float y = (float) bit.getLiftPoints().get(0).y;
                        float z = (float) (bit.getHigherAltitude()+ bit.getLowerAltitude())/2;
                        fixPositionCamera(x, y, z);
                        fixAngleCamera(scene.eye().position().x(), scene.eye().position().y(), printerZ);
                        scene.eye().setOrientation(new Quat(0,0,bitOrientation+ (float) Math.PI/2));
                    }
                }
                else {
                    fixPositionCamera(0,0,printerZ);
                    fixAngleCamera(0,0,0);
                }
                counter++;
                record=true;
            }
        });
    }

    @SuppressWarnings("unused")
    public void byBits(boolean flag) {
        if (flag){
            animationType = ANIMATION_BITS;
            toggleBatch.setState(false);
            toggleLayers.setState(false);
        }
    }

    public void byBatches(boolean flag) {
        if (flag){
            animationType = ANIMATION_BATCHES;
            toggleBits.setState(false);
            toggleLayers.setState(false);
        }
    }
    public void byLayers(boolean flag) {
        if (flag){
            animationType = ANIMATION_LAYERS;
            toggleBatch.setState(false);
            toggleBits.setState(false);
        }
    }

    public void current(boolean flag) {
        if (flag){
            animationWays = ANIMATION_CURRENT;
            toggleFull.setState(false);
        }
    }
    public void full(boolean flag) {
        if (flag){
            animationWays = ANIMATION_FULL;
            toggleCurrent.setState(false);
        }
    }

//    @SuppressWarnings("unused")
//    public void byLayers(boolean flag){
//        ((Toggle)cp5.getController("byBits")).setState(!flag);
//        if(flag)animationType=ANIMATION_LAYERS;
//    }

    @SuppressWarnings("unused")
    public void M(int value) {
        value *= 255. / 100.;
        shape.setFill(color(219, 100, 50, value));
    }

    @SuppressWarnings("unused")
    public void B(int value) {
        value *= 255. / 100.;
//        for (Pair<Position, PShape> p : shapeMap) {
//            p.getValue().stroke(0, 0, 0, value);
//            p.getValue().setFill(color(112, 66, 20, value));
//        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (IMPORTED_MODEL.equals(arg)) model = controllerView3D.getModel();
        redraw();
        loadNewData();
        updateButtons();

    }

    @Override
    public void toggle() {
        if (model == null) {
            Logger.error("No model has been loaded.");
            return;
        }
        isToggled = true;
        ProcessingModelView.startProcessingModelView();
        // Keep opening
    }

    @Override
    public void setCurrentMesh(Mesh mesh) {
        controllerView3D.setMesh(mesh);
        model = mesh.getModel();
        controllerView3D.setModel(model);
    }

    public void setModel(Model m) {
        controllerView3D.setModel(m);
        model = m;
    }

    public static void main(String[] args) {
        Mesh mesh = new Mesh();
        File file = new File("E:\\UTT\\MeshIneBits\\src\\test\\resources\\stlModel\\Blob.stl");
        try {
            mesh.importModel(file.getPath());
            mesh.setState(MeshEvents.IMPORTED);
            mesh.slice();
            mesh.setState(MeshEvents.SLICED);
            mesh.isSliced();
            mesh.pave(new ClassicBrickPattern());
            mesh.setState(MeshEvents.PAVED_MESH);
            mesh.isPaved();
            ProcessingModelView process = new ProcessingModelView();
            process.setCurrentMesh(mesh);
            process.toggle();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void resetModeView() {
        viewModel = false;
        viewMeshPaved = false;
//        isAnimated=false;
    }

    @Override
    protected PSurface initSurface() {
        if (surface == null) return super.initSurface();
        return this.getSurface();
    }
}
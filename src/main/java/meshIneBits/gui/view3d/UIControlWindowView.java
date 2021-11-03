package meshIneBits.gui.view3d;

import controlP5.*;
import controlP5.Button;
import processing.core.PApplet;

import java.awt.*;

import static meshIneBits.gui.view3d.ButtonsLabel.*;

public class UIControlWindowView extends UIControlWindow implements ProcessingModelView.ModelChangesListener {
    private static final Dimension SCREEN_DIMENSION = Toolkit.getDefaultToolkit().getScreenSize();

    private Textfield TFRotationX;
    private Textfield TFRotationY;
    private Textfield TFRotationZ;
    private Textfield TFPositionX;
    private Textfield TFPositionY;
    private Textfield TFPositionZ;
    private Toggle toggleViewMesh;
    private Button gravity;
    private Button reset;
    private Button camera;
    private Button apply;

    private Textlabel txt;
    private Textlabel modelPosition;
    private Textlabel modelSize;
    private Textarea shortcut;
    private Textlabel slicingWarning;

    private double currentX;
    private double currentY;
    private double currentZ;
    private double currentScale;
    private double currentDepth;
    private double currentWidth;
    private double currentHeight;

//    public void settings() {
//        size(SCREEN_DIMENSION.width/5, SCREEN_DIMENSION.height - 60, P3D);
//    }

    @Override
    protected void updateButton() {
//        PSurfaceAWT win = ((GLWindow) surface.getNative());

        TFRotationX.setPosition(30, displayHeight / 5);
        TFRotationY.setPosition(30, displayHeight / 5 + 60);
        TFRotationZ.setPosition(30, displayHeight / 5 + 120);
        TFPositionX.setPosition(130, displayHeight / 5);
        TFPositionY.setPosition(130, displayHeight / 5 + 60);
        TFPositionZ.setPosition(130, displayHeight / 5 + 120);


        toggleViewMesh.setPosition(30, displayHeight / 5 + 250);

        gravity.setPosition(30, displayHeight / 4 + 300);
        reset.setPosition(30, displayHeight / 4 + 350);
        camera.setPosition(30, displayHeight / 4 + 400);
        apply.setPosition(30, displayHeight / 4 + 450);

        modelSize.setPosition(30, 70)
                .setText("Model Size :\n Depth:" + currentDepth + "\n Height :" + currentHeight + "\n Width : " + currentWidth + "\n Scale : " + currentScale);
        txt.setPosition(150, 70);
        txt.setText("Current position :\n" + " x : " + currentX + "\n y : " + currentY + "\n z : " + currentZ);
        slicingWarning.setPosition(30, height-400);
        shortcut.setPosition(30, height - 200);

        modelPosition.setPosition(30, height- 50);

    }

    @Override
    public void onOpen() {
        logger.logDEBUGMessage("View window is opened");
    }

    @Override
    public void onClose() {
        logger.logDEBUGMessage("View window is closed");
    }

    @Override
    protected void generateButton() {
        int color = 255;

        TFRotationX = getControl().addTextfield(ROTATION_X).setSize(45, 30)
                .setInputFilter(0).setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial bold", 15));

        TFRotationY = getControl().addTextfield(ROTATION_Y).setSize(45, 30)
                .setInputFilter(0).setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial bold", 15));

        TFRotationZ = getControl().addTextfield(ROTATION_Z).setSize(45, 30)
                .setInputFilter(0).setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial bold", 15));

        TFPositionX = getControl().addTextfield(POSITION_X).setSize(45, 30)
                .setInputFilter(0).setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial bold", 15));

        TFPositionY = getControl().addTextfield(POSITION_Y).setSize(45, 30).setInputFilter(0)
                .setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial bold", 15));

        TFPositionZ = getControl().addTextfield(POSITION_Z).setSize(45, 30).setInputFilter(0)
                .setColorBackground(color(255, 250))
                .setColor(0).setColorLabel(255).setAutoClear(false).setColorCursor(0)
                .setFont(createFont("arial bold", 15));


        toggleViewMesh = getControl().addToggle(VIEW_MESH).setSize(20, 20)
                .setColorBackground(color(255, 250)).setColorActive(color).setColorForeground(color + 50).setFont(createFont("arial bold", 15));

        apply = getControl().addButton(APPLY).setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial bold", 15));
        gravity = getControl().addButton(GRAVITY).setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial bold", 15, false));
        reset = getControl().addButton(RESET).setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial bold", 15));
        camera = getControl().addButton(CENTER_CAMERA).setSize(140, 30)
                .setColorLabel(255).setFont(createFont("arial bold", 15));

        txt = getControl().addTextlabel("label").setText("Current Position : (0,0,0)")
                .setSize(80, 40).setColor(255).setFont(createFont("arial bold", 15));

        modelSize = getControl().addTextlabel("model size").setText("Model Size :\n Depth:" + currentDepth + "\n Height :" + currentHeight + "\n Width : " + currentWidth + "\n Scale : " + currentScale).setColor(255).setFont(createFont("arial bold", 15));

        modelPosition = getControl().addTextlabel("model position").setText("Model Position in \n Printing Space ")
                .setColor(255).setFont(createFont("arial bold", 20));

        shortcut = getControl().addTextarea("shortcut").setText("Shortcut : \n Rotation : CTRL + Mouse Left Click, Cannot be used when Mesh is sliced \n Translation : CTRL + Mouse Right Click \n Change Model Size : Mouse on the Model + Mouse Wheel , Cannot be used when Mesh is sliced\n Zoom : Mouse Wheel\n Export to Obj: press button 'S'")
                .setColor(255).setFont(createFont("arial bold", 15));


        slicingWarning = getControl().addTextlabel("slicingWarning").setText("The Model is Sliced \n You can't rotate \n You can't scale")
                .setColor(255).setFont(createFont("arial bold", 20)).hide();
    }

    @Override
    public void controlEvent(ControlEvent theEvent) {
        if (getListener() == null) {
            logger.logWARNMessage("This window " + this.getClass().getName() + " need to be setted a listener!!!");
            println(theEvent.getName());
            System.out.println(theEvent.getValue());
            System.out.println(theEvent.getStringValue());
            return;
        }
        switch (theEvent.getName()) {
            case ROTATION_X:
            case ROTATION_Y:
            case ROTATION_Z:
            case POSITION_X:
            case POSITION_Y:
            case POSITION_Z:
                getListener().onActionListener(theEvent.getName(), Float.parseFloat(theEvent.getStringValue()));
                break;
            case VIEW_MESH:
                getListener().onActionListener(theEvent.getName(), (float) 1.0 == theEvent.getValue());
                break;
            case APPLY:
            case GRAVITY:
            case RESET:
            case CENTER_CAMERA:
                getListener().onActionListener(theEvent.getName(), theEvent.getValue());
                break;
            default:
                logger.logWARNMessage("The event invoked is not handled by method ControlEvent");
                break;
        }
        println(theEvent.getName());
        System.out.println(theEvent.getValue());
        System.out.println(theEvent.getStringValue());

    }

    public static void main(String[] args) {
        PApplet.main(UIControlWindowView.class.getCanonicalName());
    }


    @Override
    public void onSizeChange(double scale, double dept, double width, double height) {
        currentScale=scale;
        currentDepth=dept;
        currentWidth=width;
        currentHeight=height;
    }

    @Override
    public void onPositionChange(double x, double y, double z) {
        currentX=x;
        currentY=y;
        currentZ=z;
    }

    @Override
    public void onRotationChange(double x, double y, double z) {

    }
}

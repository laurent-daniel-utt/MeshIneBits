package meshIneBits.gui.view3d;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.MultiThreadServiceExecutor;
import processing.core.PApplet;

import javax.swing.*;
import java.awt.*;

public abstract class UIControlWindow extends PApplet {

    private final int BACKGROUND =  color(150, 150, 150);
    private UIControllerListener listener;
    final CustomLogger logger = new CustomLogger(this.getClass());

    private ControlP5 control;
    private Integer locationX;
    private Integer locationY;
    private String title;

    public abstract void onOpen();
    public abstract void onClose();
    protected abstract void generateButton();
    protected abstract void updateButton();
    public abstract void controlEvent(ControlEvent theEvent);

    public void setup(){
        surface.setTitle(title);
        surface.setResizable(true);
//        surface.setLocation(locationX, locationY);
        frameRate(30);
//        setCloseOperation();
        control = new ControlP5(this);
        generateButton();
        control.enableShortcuts();
    }

    public void draw(){
        background(BACKGROUND);
        noStroke();
        updateButton();
    }
    public UIControllerListener getListener() {
        return listener;
    }

    public ControlP5 getControl() {
        return control;
    }

    public UIControlWindow setLocation(Integer locationX,Integer locationY) {
        this.locationX = locationX;
        this.locationY = locationY;
        return this;
    }

    public UIControlWindow setTitle(String title) {
        this.title = title;
        return this;
    }
    public UIControlWindow setUIControllerListener(UIControllerListener listener) {
        this.listener=listener;
        return this;
    }

    @Override
    public void exit(){
        listener=null;
        onClose();
        this.dispose();
    }

}

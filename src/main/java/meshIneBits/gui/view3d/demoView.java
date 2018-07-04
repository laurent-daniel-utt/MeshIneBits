/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

import controlP5.ControlP5;
import controlP5.Textlabel;
import javafx.util.Pair;
import meshIneBits.GeneratedPart;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.SubWindow;
import meshIneBits.util.Vector3;
import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PJOGL;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.Scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class demoView extends PApplet implements Observer, SubWindow {

    private final int BACKGROUND_COLOR = color(150, 150, 150);
    private final int framerate = 60;
    private static demoView currentInstance = null;
    private static Controller controller = null;
    private static boolean initialized = false;
    private static boolean visible = false;
    private int height = 450;
    private int width = 800;
    private Scene scene;
    private Builder builder;
    private float printerX;
    private float printerY;
    private float printerZ;
    private ControlP5 cp5;
    private static Vector<Pair<Position,PShape>> shapeMap = null;
    private boolean start = false;
    private int n;
    private Textlabel FPS;


    public  static void startdemoView(){
        if (currentInstance == null){
            PApplet.main("meshIneBits.gui.view3d.demoView");
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
    private void destroyGLWindow(){
        ((com.jogamp.newt.opengl.GLWindow) surface.getNative()).destroy();
    }
    /**
     *
     */
    public void settings(){
        PJOGL.setIcon(this.getClass().getClassLoader().getResource("resources/icon.png").getPath());
        currentInstance = this;
        size(width, height, P3D);
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
                controller.deleteObserver(currentInstance);
                dispose();
                currentInstance = null;
                initialized = false;
                visible = false;
            }
        });

        win.addWindowListener(new WindowAdapter() {
            @Override
            public void windowResized(WindowEvent e) {
                super.windowResized(e);
                surface.setSize(win.getWidth(),win.getHeight());
            }
        });
    }

    public demoView() {
        controller = Controller.getInstance();
        controller.addObserver(this);
    }
    /**
     *
     */
    public void setup() {
        frameRate(framerate);
        this.surface.setResizable(true);
        this.surface.setTitle("MeshIneBits - Demo view");

        scene = new Scene(this);
        scene.eye().setPosition(new Vec(0, 1, 1));
        scene.eye().lookAt(scene.eye().sceneCenter());
        scene.setRadius(2500);
        scene.showAll();
        scene.disableKeyboardAgent();
        shapeMap = new Vector<Pair<Position, PShape>>();
        builder = new Builder(this);
        try {
            builder.buildBits(shapeMap);
        }
        catch (Exception e){
            println("Mesh not generated yet");
        }
        setCloseOperation();
        cp5 = new ControlP5(this);
        cp5.addButton("Play").setPosition(20, 250).setSize(80, 20).setColorLabel(255);
        cp5.addButton("Stop").setPosition(20, 280).setSize(80, 20).setColorLabel(255);
        FPS = cp5.addTextlabel("FPS").setText("frameRate : " + frameRate).setPosition(10,10)
                .setSize(80,40).setColor(255);
        cp5.setAutoDraw(false);

        n = 0;
    }

    public void draw(){
        background(BACKGROUND_COLOR);
        lights();
        ambientLight(255,255,255);
        drawWorkspace();
        scene.beginScreenDrawing();
        cp5.draw();
        FPS.setText("frameRate : " + frameRate);
        scene.endScreenDrawing();
        drawBits(shapeMap, n);
        if (start){
            n++;
        }
    }

    private  void drawBits(Vector<Pair<Position, PShape>> shapeMap, int n){
        float bitThickness = (float) CraftConfig.bitThickness;
        float layersOffSet = (float) CraftConfig.layersOffset;

        float zLayer = (int) (controller.getCurrentPart().getLayers().size() * (bitThickness + layersOffSet));

        Vector3 v = controller.getModel().getPos();
        int j = 0;
        for (int i = 0; i < shapeMap.size(); i++) {
            if (j < n) {
                pushMatrix();
                translate((float) v.x, (float) v.y, (float) v.z);
                float[] t = shapeMap.get(i).getKey().getTranslation();
                translate(t[0], t[1], t[2]);
                rotateZ(radians(shapeMap.get(i).getKey().getRotation()));

                PShape s = shapeMap.get(i).getValue();

                if (t[2] <= zLayer) {
                    shape(s);
                }
                shape(s);
                popMatrix();
                j++;
            }
        }
    }

    /*
     *  Display the printer workspace
     *
     */
    private void drawWorkspace() {
        try {
            File filename = new File(this.getClass().getClassLoader().getResource("resources/PrinterConfig.txt").getPath());
            FileInputStream file = new FileInputStream(filename);
            BufferedReader br = new BufferedReader(new InputStreamReader(file));
            String strline;
            while ((strline = br.readLine()) != null){
                if (strline.startsWith("x")){
                    printerX = Float.valueOf(strline.substring(3));
                }
                else if (strline.startsWith("y")){
                    printerY = Float.valueOf(strline.substring(3));
                }
                else if (strline.startsWith("z")){
                    printerZ = Float.valueOf(strline.substring(3));
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

    public void Play(float theValue) {
        if (start){
            start = false;
        }
        else{
            start = true;
        }

    }

    public void Stop(float theValue) {
        n = 0;
    }

    @Override
    public void update(Observable o, Object arg){}

    public void open(){
        // TODO Auto-generated method stub
        if (!initialized){
           demoView.startdemoView();
            visible = true;
            initialized = true;
        } else{
            setVisible(true);
        }
    }

    public void hide(){
        // TODO Auto-generated method stub
        setVisible(false);
    }

    @Override
    public void toggle(){
        if (visible){
            hide();
        } else{
            open();
        }
    }

    @Override
    public void setCurrentPart(GeneratedPart currentPart) {
        controller.setCurrentPart(currentPart);
    }

    private void setVisible(boolean b){
        // TODO Auto-generated method stub
        currentInstance.getSurface().setVisible(b);
        visible = b;
    }
}

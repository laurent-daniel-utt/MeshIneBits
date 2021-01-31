/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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
import javafx.util.Pair;
import meshIneBits.Mesh;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.SubWindow;
import processing.core.PApplet;
import processing.core.PShape;
import processing.opengl.PJOGL;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.Scene;

import java.util.Objects;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

public class demoView extends PApplet implements Observer, SubWindow {

    private final int BACKGROUND_COLOR = color(150, 150, 150);
    private final int framerate = 30;
    private static demoView currentInstance = null;
    private static ControllerView3D controllerView3D = null;
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
    private static Vector<Pair<Position, PShape>> shapeMap = null;
    private boolean start = false;
    private int n;

    private static void startdemoView() {
        if (currentInstance == null) {
            PApplet.main("meshIneBits.gui.view3d.demoView");
        }
    }

    /**
     *
     */
    public static void closeProcessingModelView() {
        if (currentInstance != null) {
            currentInstance.destroyGLWindow();
        }
    }

    /**
     *
     */
    private void destroyGLWindow() {
        ((com.jogamp.newt.opengl.GLWindow) surface.getNative()).destroy();
    }

    /**
     *
     */
    public void settings() {
        PJOGL.setIcon(Objects.requireNonNull(this.getClass().getClassLoader().getResource("resources/icon.png")).getPath());
        currentInstance = this;
        size(width, height, P3D);
    }

    /**
     *
     */
    private void setCloseOperation() {
        //Removing close listeners

        com.jogamp.newt.opengl.GLWindow win = ((com.jogamp.newt.opengl.GLWindow) surface.getNative());
        for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
            win.removeWindowListener(wl);
        }

        win.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);

        win.addWindowListener(new WindowAdapter() {
            public void windowDestroyed(WindowEvent e) {
                controllerView3D.deleteObserver(currentInstance);
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
                surface.setSize(win.getWidth(), win.getHeight());
            }
        });
    }

    public demoView() {
        controllerView3D = ControllerView3D.getInstance();
        controllerView3D.addObserver(this);
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
        shapeMap = new Vector<>();
        builder = new Builder(this);
        try {
//            builder.buildBits(shapeMap);
        } catch (Exception e) {
            println("Mesh not generated yet");
        }
        setCloseOperation();
        cp5 = new ControlP5(this);
        cp5.addButton("Play").setPosition(20, 250).setSize(80, 20).setColorLabel(255);
        cp5.addButton("Stop").setPosition(20, 280).setSize(80, 20).setColorLabel(255);
        cp5.setAutoDraw(false);

        n = 0;
    }

    public void draw() {
        background(BACKGROUND_COLOR);
        lights();
        ambientLight(255, 255, 255);
        drawWorkspace();
        drawBits(shapeMap, n);
        if (start) {
            n++;
        }
        scene.beginScreenDrawing();
        cp5.draw();
        scene.endScreenDrawing();
    }

    private void drawBits(Vector<Pair<Position, PShape>> shapeMap, int n) {

        int j = 0;
        for (int i = 0; i < shapeMap.size(); i++) {
            if (j < n) {
                pushMatrix();
                float[] t = shapeMap.get(i).getKey().getTranslation();
                translate(t[0], t[1], t[2]);
                translate((float) controllerView3D.getCurrentMesh().getModel().getPos().x,
                        (float) controllerView3D.getCurrentMesh().getModel().getPos().y,
                        (float) controllerView3D.getCurrentMesh().getModel().getPos().z);

                rotateZ(radians(shapeMap.get(i).getKey().getRotation()));

                PShape s = shapeMap.get(i).getValue();

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
        printerX = CraftConfig.printerX;
        printerY = CraftConfig.printerY;
        printerZ = CraftConfig.printerZ;
        pushMatrix();
        noFill();
        stroke(255, 255, 0);
        translate(0, 0, printerZ / 2);
        box(printerX, printerY, printerZ);
        popMatrix();
        stroke(80);
        scene.pg().pushStyle();
        scene.pg().beginShape(LINES);
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

    public void Play(float theValue) {
        if (start) {
            start = false;
        } else {
            start = true;
        }

    }

    public void Stop(float theValue) {
        start = false;
        n = 0;
    }

    @Override
    public void update(Observable o, Object arg) {
    }

    private void open() {
        // TODO Auto-generated method stub
        if (!initialized) {
            demoView.startdemoView();
            visible = true;
            initialized = true;
        } else {
            setVisible(true);
        }
    }

    private void hide() {
        // TODO Auto-generated method stub
        setVisible(false);
    }

    @Override
    public void toggle() {
        if (visible) {
            hide();
        } else {
            open();
        }
    }

    @Override
    public void setCurrentMesh(Mesh mesh) {
        controllerView3D.setMesh(mesh);
    }

    private void setVisible(boolean b) {
        // TODO Auto-generated method stub
        currentInstance.getSurface().setVisible(b);
        visible = b;
    }
}

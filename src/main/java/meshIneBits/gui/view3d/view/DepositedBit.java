/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2015-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -  Copyright (C) 2026 Hugo BENOIT
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

package meshIneBits.gui.view3d.view;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import meshIneBits.util.Pair;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.gui.view3d.builder.ExtrusionFromAreaService;
import meshIneBits.util.LiftPointCalc;
import meshIneBits.util.Vector2;
import meshIneBits.util.supportImportFile.DomParser;
import meshIneBits.util.supportImportFile.FallType;
import meshIneBits.util.supportImportFile.Reconstitute;
import processing.awt.PSurfaceAWT;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.event.MouseEvent;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.Scene;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;

public class DepositedBit extends PApplet implements DepositingProcessView.IdUpdated {
    private int id=57;
  // private int id=57;
    private PShape bitShape;
    private Scene sceneD;
    private CustomInteractiveFrame frame;
    private ArrayList<ArrayList<Pair<FallType, Path2D.Double>>> cutpaths;
    private com.jogamp.newt.opengl.GLWindow win;
  //  private PShape limit1;
    //private PShape limit2;
    private int width;
    private int height;
    private int posx;
    private int posy;
    private int scale;
    private DepositingProcessView mainInterface;
    private String title;
    private boolean exited = false;
    private float theta=0;
    private double rotationSpeed=0.2;
    private Vector2 liftpoint;
    private Vector2 newOrigin;

public DepositedBit(DepositingProcessView maininterface,String title,int width,int height,int posx,int posy,int scale ){
    this.width=width;
    this.height=height;
    this.posx=posx;
    this.posy=posy;
    this.scale=scale;
    this.title=title;
    this.mainInterface=maininterface;
}

    public String getTitle() {
        return title;
    }

    public static void main(String[]args){DepositedBit p=new DepositedBit(null,"Deposited Bit",Visualization3DConfig.V3D_WINDOW_WIDTH,
            Visualization3DConfig.V3D_WINDOW_HEIGHT,Visualization3DConfig.V3D_WINDOW_LOCATION_X,
            Visualization3DConfig.V3D_WINDOW_LOCATION_Y,10);
    runSketch(new String[]{"--display=1", "Projector"},p);
   // PApplet.main(DepositedBit.class.getCanonicalName());
}


    @Override
    public void settings() {
        size(width, height, P3D);
    }



    public void setup(){
        configWindow(title,posx, posy);
        init3DScene(Visualization3DConfig.V3D_EYE_POSITION, Visualization3DConfig.V3D_RADIUS);
        surface.setAlwaysOnTop(true);

        cutpaths = DomParser.parseXml(0);
        Area bitArea= Reconstitute.getInstance().recreateArea(cutpaths,id,false);
        bitShape = ExtrusionFromAreaService.getInstance()
                .buildShapeFromArea(this, bitArea, Visualization3DConfig.BIT_THICKNESS);
        liftpoint= LiftPointCalc.instance.getLiftPoint(bitArea, CraftConfig.suckerDiameter / 2);
        newOrigin=new Vector2((liftpoint.x+CraftConfig.lengthFull/2),(liftpoint.y+CraftConfig.bitWidth/2));
       /* limit1=createShape();
        limit1.beginShape();
        limit1.vertex((float) CraftConfig.lengthFull,0,0);
        limit1.vertex((float) CraftConfig.lengthFull,(float) CraftConfig.bitWidth+2,0);
        limit1.vertex((float) CraftConfig.lengthFull,(float) CraftConfig.bitWidth+2,(float) 0.001);
        limit1.endShape(PConstants.CLOSE);

        limit2=createShape();
        limit2.beginShape();
        limit2.vertex(0,(float) CraftConfig.bitWidth,0);
        limit2.vertex((float) CraftConfig.lengthFull+2,(float) CraftConfig.bitWidth,0);
        limit2.vertex((float) CraftConfig.lengthFull+2,(float) CraftConfig.bitWidth,(float) 0.001);
        limit2.endShape(PConstants.CLOSE);*/

}
    @Override
    public void mouseClicked(MouseEvent event) {
        Area bitArea= Reconstitute.getInstance().recreateArea(cutpaths,id,false);
        bitShape = ExtrusionFromAreaService.getInstance()
                .buildShapeFromArea(this, bitArea, Visualization3DConfig.BIT_THICKNESS);
        liftpoint= LiftPointCalc.instance.getLiftPoint(bitArea, CraftConfig.suckerDiameter / 2);
        newOrigin=new Vector2((liftpoint.x+CraftConfig.lengthFull/2),(liftpoint.y+CraftConfig.bitWidth/2));

    }



    @Override
    public void draw() {
        lights();
        background(200,200,200);

        translate((float) (newOrigin.x*scale), (float) (newOrigin.y*scale),0);
        pushMatrix();
        theta= (float) (theta+rotationSpeed);
        rotateX((float)  Math.toRadians(theta));
        rotateY((float)  Math.toRadians(theta));
        rotateZ((float)  Math.toRadians(theta));
        translate((float)CraftConfig.lengthFull/2*scale,(float)CraftConfig.bitWidth/2*scale,0);
        translate(-(float) (newOrigin.x*scale), -(float) (newOrigin.y*scale),0);
        scale(scale);
        shape(bitShape);
        popMatrix();

       /* pushMatrix();
        translate(-(float) (newOrigin.x*scale), -(float) (newOrigin.y*scale),0);
        scale(scale);
        shape(limit1);
        shape(limit2);
        popMatrix();*/



    }


    private void setCloseOperation() {

        //Removing close listeners
        win = (com.jogamp.newt.opengl.GLWindow) this.surface.getNative();
        for (com.jogamp.newt.event.WindowListener wl : win.getWindowListeners()) {
            win.removeWindowListener(wl);
        }
        win.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);
        win.addWindowListener(new WindowAdapter() {
            public void windowDestroyed(WindowEvent e) {
              //  System.exit(0);
           exit();
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
        sceneD = new Scene(DepositedBit.this);
        sceneD.eye().setPosition(eyePosition);
        sceneD.eye().lookAt(sceneD.eye().sceneCenter());
        sceneD.setRadius(radius);
        sceneD.showAll();
        sceneD.toggleGridVisualHint();
        sceneD.toggleAxesVisualHint();
    }

    @Override
    public void exit() {
        if (!exited) {
            super.exit();
            exited = true;
        }
    }
    @Override
    public void exitActual() {
        PSurfaceAWT.SmoothCanvas surf = (PSurfaceAWT.SmoothCanvas) this.getSurface().getNative();
        Frame frame = surf.getFrame();
        frame.dispose();
    }

    private void configWindow(String title, int locationX, int locationY) {
        this.surface.setResizable(true);
        this.surface.setTitle(title);
        this.surface.setLocation(locationX, locationY);
        setCloseOperation();
    }



    public void updateShape(int newId){
        System.out.println("updating dep");
        if(id!=newId){
            id=newId;
            if(id% CraftConfig.nbBitesBatch==0){
                System.out.println("changing batch:");
                int batch_num=id/CraftConfig.nbBitesBatch;
                cutpaths = DomParser.parseXml(batch_num);
                Reconstitute.setCurrentDeposeBatchNum(batch_num);
            }
            System.out.println("id_bit:"+id+" num_batch:"+DomParser.getBatch_num());
            Area bitArea= Reconstitute.getInstance().recreateArea(cutpaths,id,false);
            bitShape = ExtrusionFromAreaService.getInstance()
                    .buildShapeFromArea(this, bitArea, Visualization3DConfig.BIT_THICKNESS);
            liftpoint= LiftPointCalc.instance.getLiftPoint(bitArea, CraftConfig.suckerDiameter / 2);
            newOrigin=new Vector2((liftpoint.x+CraftConfig.lengthFull/2),(liftpoint.y+CraftConfig.bitWidth/2));
            surface.setTitle(title+" (Id:"+String.valueOf(id)+"; Batch:"+(int)Math.ceil(id/ CraftConfig.nbBitesBatch)+"; Id in batch:"+id% CraftConfig.nbBitesBatch+")");
        }
    }

    @Override
    public void idUpdated(int newId) {
        updateShape(newId);
    }
}


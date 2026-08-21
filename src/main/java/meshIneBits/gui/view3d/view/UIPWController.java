/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
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

import meshIneBits.gui.view3d.Processor.IVisualization3DProcessor;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.util.CustomLogger;
import processing.core.PShape;

import java.util.concurrent.atomic.AtomicBoolean;

import static meshIneBits.gui.view3d.oldversion.GraphicElementLabel.*;
import static meshIneBits.gui.view3d.util.animation.AnimationProcessor.getpausing;
import static meshIneBits.gui.view3d.view.BaseVisualization3DView.IndexExport;
import static meshIneBits.gui.view3d.view.UIPWAnimation.Animation;

public class UIPWController implements UIPWListener {


  private final CustomLogger logger = new CustomLogger(UIPWController.class);
  private final AtomicBoolean isAnimating = new AtomicBoolean(false);
private PShape pp;

  private IVisualization3DProcessor processor;
public static Boolean Exportation=false;
private int i=0;
  public UIPWController(IVisualization3DProcessor view) {
    this.processor = view;
  }

  @Override
  public void onActionListener(Object callbackObj, String event, Object value) {
    switch (event) {
      case ROTATION_X:
        processor.rotationX((float) value);
        break;
      case ROTATION_Y:
        processor.rotationY((float) value);
        break;
      case ROTATION_Z:
        processor.rotationZ((float) value);
        break;
      case POSITION_X:
        processor.translateX((float) value);
        break;
      case POSITION_Y:
        processor.translateY((float) value);
        break;
      case POSITION_Z:
        processor.translateZ((float) value);
        break;
      case VIEW_MESH:
        if ((boolean) value) {
          processor.displayMesh(true);
        }else{
          processor.displayModel(true);
        }
        break;
      case APPLY:
        processor.apply();
        break;
      case GRAVITY:
        processor.applyGravity();
        break;
      case CENTER_CAMERA:
        processor.centerCamera();
        break;
      case RESET:
        processor.reset();
        break;
      case BY_SUB_BIT:
        processor.setAnimationBySubBit((boolean) value);
        break;
      case BY_BIT:
        processor.setAnimationByBit((boolean) value);
        break;
      case BY_BATCH:
        processor.setAnimationByBatch((boolean) value);
        break;
      case BY_LAYER:
        processor.setAnimationByLayer((boolean) value);
        break;
      case ONE_BY_ONE:
        processor.setDisplayOneByOne((boolean) value);
        break;
      case FULL:
        processor.setDisplayFull((boolean) value);
        break;
      case EXPORT:
        processor.export();
        break;
      case ANIMATION:

        isAnimating.set(!isAnimating.get());
        if (isAnimating.get()) {Animation.getCaptionLabel().setText(STOP);

          processor.activateAnimation();
        } else {Animation.getCaptionLabel().setText(ANIMATION);
          processor.deactivateAnimation();
        }
        break;
      case SPEED_UP:
        processor.speedUp();
        break;
      case SPEED_DOWN:
        processor.speedDown();
        break;
      case PAUSE:

        processor.pauseAnimation();
        break;
      case ANIMATION_SLICER:
        processor.setAnimationIndex(Math.round((float) value));
        break;
      case EXPORTAll:
        if (MeshProvider.getInstance().getCurrentMesh().isPaved()){
          processor.setDisplayOneByOne(true);
          Exportation=true;
          processor.activateAnimation();
          IndexExport=0;
          processor.exportAll();
          Exportation=false;}
        break;
      case NEXT:
      if(isAnimating.get() && getpausing()){
        processor.incrementIndex();
      }
        break;
      case PREVIOUS:
        if(isAnimating.get() && getpausing()){
          processor.decrementIndex();
        }
        break;
        default:
        logger.logERRORMessage("The event invoked is not handled by UserControllerListener object: "
            + this.getClass());
        break;
    }
  }

  public void close() {
    processor = null;
  }


}

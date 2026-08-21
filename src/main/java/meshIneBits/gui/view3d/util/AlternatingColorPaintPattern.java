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

package meshIneBits.gui.view3d.util;

import meshIneBits.Layer;
import meshIneBits.gui.view3d.builder.BitShape;
import meshIneBits.gui.view3d.builder.PavedMeshBuilderResult;
import meshIneBits.gui.view3d.builder.SubBitShape;
import meshIneBits.gui.view3d.provider.MeshProvider;
import meshIneBits.gui.view3d.util.animation.AnimationProcessor.AnimationOption;

import java.awt.*;
import java.util.Vector;

import static meshIneBits.gui.view3d.view.BaseVisualization3DView.meshstrips;

public class AlternatingColorPaintPattern implements IPaintShapePattern {

  private final Color color1 = new Color(112, 66, 20);
  private final Color color2 = new Color(20, 66, 112);

  private  Color colorS=color1;
  @Override
  public void paintAnimation(PavedMeshBuilderResult pavedMesh, AnimationOption animationOption)  {
    Vector<BitShape> bitShapes = pavedMesh.getBitShapes();
    if (bitShapes == null || bitShapes.size() == 0) {
      return;
    }
    switch (animationOption) {
      case BY_LAYER:
        for (BitShape bitShape : bitShapes) {
          if (bitShape.getLayerId() % 2 == 0) {
            bitShape.getShape().setFill(color1.getRGB());
          } else {
            bitShape.getShape().setFill(color2.getRGB());
          }
        }
        break;
      case BY_BATCH:
        for (BitShape bitShape : bitShapes) {
          for (SubBitShape subBitShape : bitShape.getSubBitShapes()) {
            if (subBitShape.getBatchId() % 2 == 0) {
              subBitShape.getShape().setFill(color1.getRGB());
            } else {
              subBitShape.getShape().setFill(color2.getRGB());
            }
          }
        }
        break;
      case BY_BIT:
      case BY_SUB_BIT:
        int l=0,s=0,size_layer=0,size_strip=0;
        for (BitShape bitShape : bitShapes) {

          if(size_strip>meshstrips.get(l).get(s).getBits().size()-1){
            Layer layer= MeshProvider.getInstance().getCurrentMesh().getLayers().get(l);
            if(size_layer< (layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size())) {
              size_strip=0;
              s++;
             if((s+l+1)%2==0) colorS=color2;
             else {  colorS=color1;   }
            }
            else {size_strip=0;
              size_layer=0;
              s=0;
              l++;
              layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(l);
              while  ((layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size())==0)  {
                l++;
                layer=MeshProvider.getInstance().getCurrentMesh().getLayers().get(l);
              }

              if(l%2!=0)colorS=color2;
            else {  colorS=color1;   }
            }
          }



          size_strip++;
          size_layer++;


          bitShape.getShape().setFill(colorS.getRGB());


        }
        break;
    }
   // throw new IndexOutOfBoundsException("you have to refresh the 3d interface");
  }

  @Override
  public void paintMesh(PavedMeshBuilderResult pavedMesh) {

  }
}

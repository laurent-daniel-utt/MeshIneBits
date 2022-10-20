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
  public void paintAnimation(PavedMeshBuilderResult pavedMesh, AnimationOption animationOption) throws IndexOutOfBoundsException {
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
//System.out.println("layer_Capa:"+(layer.getBits3dKeys().size()-layer.getKeysOfIrregularBits().size()));
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
    throw new IndexOutOfBoundsException("you have to refresh the 3d interface");
  }

  @Override
  public void paintMesh(PavedMeshBuilderResult pavedMesh) {

  }
}

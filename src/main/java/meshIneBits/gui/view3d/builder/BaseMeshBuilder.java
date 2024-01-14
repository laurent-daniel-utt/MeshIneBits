package meshIneBits.gui.view3d.builder;

import meshIneBits.*;
import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.scheduler.AdvancedScheduler;
import meshIneBits.util.CustomLogger;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

import java.util.*;

import static meshIneBits.config.CraftConfig.nbBitesBatch;
import static meshIneBits.gui.view3d.view.BaseVisualization3DView.WindowStatus;

public class BaseMeshBuilder implements IMeshShapeBuilder {

  public static final CustomLogger logger = new CustomLogger(BaseMeshBuilder.class);
  private final PApplet context;
  private final Project project;

  private int num=0;
//private Vector<Strip> layerstrips=new Vector<>();
private  ArrayList<ArrayList<Strip>> meshstrips=new ArrayList<>();
  public BaseMeshBuilder(PApplet context, Project project) {
    this.context = context;
    this.project = project;
  }

  @Override
  public PavedMeshBuilderResult buildMeshShape() {
    //TODO project has to be scheduled before build the shape of project.
    if (!project.isPaved()) {
      return new PavedMeshBuilderResult(null, null);
    }
    Vector<BitShape> bitShapes = new Vector<>();
    PShape meshShape = context.createShape(PConstants.GROUP);
    project.getLayers().forEach((layer) -> {
      //TODO temporary code, need to be clean after!!!
      List<Bit3D> bitsInCurrentLayer = AScheduler.getSetBit3DsSortedFrom(
              project.getScheduler().filterBits(layer.sortBits()));
      int layerId = layer.getLayerNumber();
      bitsInCurrentLayer.forEach(bit3D -> {
        BitShape bitShape = buildBitShape((NewBit3D) bit3D);
        updateBitShapeLocation(bit3D, bitShape);
        bitShape.setLayerId(layerId);
        NewBit2D newBit2D = ((NewBit3D) bit3D).getBaseBit();
        Vector<SubBit2D> validSubBits = newBit2D.getValidSubBits();
        for (int i = 0; i < validSubBits.size(); i++) {
          int batchId = ((AdvancedScheduler) project.getScheduler()).getSubBitBatch(
                  validSubBits.get(i));
          if (batchId != -1) {
            bitShape.getSubBitShapes()
                    .get(i)
                    .setLayerId(layerId)
                    .setBatchId(batchId);
          }
        }

        meshShape.addChild(bitShape.getShape());
        bitShapes.add(bitShape);
      });
    });
    return new PavedMeshBuilderResult(meshShape, bitShapes);
  }

  private void updateBitShapeLocation(Bit3D bit3D, BitShape bitShape) {
    bitShape.rotateZ(PApplet.radians((float) bit3D.getOrientation().getEquivalentAngle2()));
//    bitShape.getShape().rotate(bit3D.getOrientation().get);
    bitShape.translate(
        (float) bit3D.getOrigin().x,
        (float) bit3D.getOrigin().y,
        (float) bit3D.getLowerAltitude());
  }

  BitShape buildBitShape(Bit3D bit3D) {
    /*Modify method to build shape of bit here (ie: extrude from area ...*/
    BitShape shapeBit = BitShape.create(context);
    bit3D.getRawAreas().forEach(area -> {
      PShape subBit = ExtrusionFromAreaService.getInstance()
          .buildShapeFromArea(context, area, Visualization3DConfig.BIT_THICKNESS);
      shapeBit.addChild(subBit);
    });
    shapeBit.getShape().setFill(Visualization3DConfig.MESH_COLOR.getRGB());

    return shapeBit;
  }

  @SuppressWarnings("unused")
  BitShape buildBitShape(NewBit3D bit3D) {
    /*Modify method to build shape of bit here (ie: extrude from area ...*/
    BitShape shapeBit = BitShape.create(context);
    bit3D.getBaseBit().getValidSubBits().forEach(subBit2D -> {
      PShape shape = ExtrusionFromAreaService.getInstance()
          .buildShapeFromArea(context, subBit2D.getAreaCB(), Visualization3DConfig.BIT_THICKNESS);
      SubBitShape subBitShape = new SubBitShape(shape);
      shapeBit.addSubBit(subBitShape);
    });
    shapeBit.getShape().setFill(Visualization3DConfig.MESH_COLOR.getRGB());
   if(WindowStatus==2){
    shapeBit.getShape().setStrokeWeight(1);
   }
    return shapeBit;
  }

  /**
   * the method that creates Stripes.
   * we iterate through the whole project, Layer per Layer,we place the first Stripe of a Layer on the bit which has
   * the extremist point to left,then we see what bits can fit in this stripe and we put them in,then we move on to the next Stripe
   * of the layer(again this new Stripe starts with the extremist bit to left from remaining bits of the layer), and so on until
   * each bit of te layer has been added to a Stripe, once a Layer is done we move to the next Layer and so on until every single
   * bit of the project is added to a stripe.
   * @return List of Stripes per Layer(each case is a list of stripes in a Layer)
   * example:meshstrips[3]==>List of Stripes of the 4th Layer
   */

  public ArrayList<ArrayList<Strip>> build_strips(){
    if(!meshstrips.isEmpty())meshstrips.clear();
    if (!project.isPaved()) {
      return null;
    }
    project.getLayers().forEach((layer) -> {
      List<Bit3D> bitsInCurrentLayer = AScheduler.getSetBit3DsSortedFrom(
              project.getScheduler().filterBits(layer.sortBits()));
      ArrayList<Strip> layerstrips=new ArrayList<>();
      HashSet<Bit3D> toremove=new HashSet<>();
      /**we sort bits by the extremist point to left*/
      Collections.sort(bitsInCurrentLayer,Comparator.comparing(Bit3D::getMinX));
      /**while we still have bits in the layer that were not added to a stripe*/
      while(bitsInCurrentLayer.size()>0){
        /** loop to find the extremist bit to left,when creating a new stripe we need to find the extremist bit to left(of the
         remaining bits in the current layer)so we start the stripe at this bit */
        TreeSet<Bit3D>tofindfirstbit=new TreeSet<>(Comparator.comparing(Bit3D::getMinX ));
        for (Bit3D bit:bitsInCurrentLayer){
          tofindfirstbit.add(bit);
        }
        /**creating a new Stripe at its first bit*/
        Iterator<Bit3D>itfirst=tofindfirstbit.iterator();
        layerstrips.add(  new Strip ((NewBit3D)itfirst.next(),layer));
        /**loop through bits of the current layer*/
        for(Bit3D bit3D:bitsInCurrentLayer){
          /**we verify if the bit can fit in the current stripe if not we create a new stripe*/
          if(bit3D.getTwoExtremeXPointsCS().get(0).x>=layerstrips.get(layerstrips.size()-1).getXposition()&&
                  bit3D.getTwoExtremeXPointsCS().get(1).x<=layerstrips.get(layerstrips.size()-1).getXposition()
                          + CraftConfig.workingWidth && num<nbBitesBatch)
          {
            layerstrips.get(layerstrips.size()-1).addBit3D((NewBit3D) bit3D);
            /**we stock the added bits so we can remove them later from the layer so we dont add the same bits multiple times*/
            toremove.add(bit3D);
            /**we increment the number of added bits*/
            num++;
          }
        }
        /**if the number of added bits=number of bits per batch ==>batch is over so we need to reload the deposing machine so
         we need to create a new Stripe even if only 1 bit is remaining in the layer*/
        if(num==nbBitesBatch) {
          num=0;
        }
        layerstrips.get(layerstrips.size()-1).getBits().sort(Comparator.comparing(Bit3D::getMinX));
        /**removing added bits*/
        bitsInCurrentLayer.removeAll(toremove);
      }
      meshstrips.add(layerstrips);
    });
    return meshstrips;
  }





}

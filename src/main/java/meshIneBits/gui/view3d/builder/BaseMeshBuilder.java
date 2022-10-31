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
  private final Mesh mesh;

  private int num=0;
//private Vector<Strip> layerstrips=new Vector<>();
private  ArrayList<ArrayList<Strip>> meshstrips=new ArrayList<>();
  public BaseMeshBuilder(PApplet context, Mesh mesh) {
    this.context = context;
    this.mesh = mesh;
  }

  @Override
  public PavedMeshBuilderResult buildMeshShape() {
    //TODO mesh has to be scheduled before build the shape of mesh.
    if (!mesh.isPaved()) {
      return new PavedMeshBuilderResult(null, null);
    }
    Vector<BitShape> bitShapes = new Vector<>();
    PShape meshShape = context.createShape(PConstants.GROUP);
    mesh.getLayers().forEach((layer) -> {
      //TODO temporary code, need to be clean after!!!
      List<Bit3D> bitsInCurrentLayer = AScheduler.getSetBit3DsSortedFrom(
          mesh.getScheduler().filterBits(layer.sortBits()));

      //Collections.sort(bitsInCurrentLayer,Comparator.comparing(Bit3D::getXliftpoint ).thenComparing(Bit3D::getYliftpoint));
      int layerId = layer.getLayerNumber();

      bitsInCurrentLayer.forEach(bit3D -> {

        BitShape bitShape = buildBitShape((NewBit3D) bit3D);

        updateBitShapeLocation(bit3D, bitShape);

        bitShape.setLayerId(layerId);
        NewBit2D newBit2D = ((NewBit3D) bit3D).getBaseBit();
        Vector<SubBit2D> validSubBits = newBit2D.getValidSubBits();
        for (int i = 0; i < validSubBits.size(); i++) {
          int batchId = ((AdvancedScheduler) mesh.getScheduler()).getSubBitBatch(
              validSubBits.get(i));
          //System.out.println("batchId="+batchId);
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
    //shapeBit.getShape().setStroke(-199999980);
    shapeBit.getShape().setStroke(-999999999);
   }
    return shapeBit;

  }

public ArrayList<ArrayList<Strip>> build_strips(){
if(!meshstrips.isEmpty())meshstrips.clear();

  if (!mesh.isPaved()) {
    return null;
  }


  mesh.getLayers().forEach((layer) -> {

    List<Bit3D> bitsInCurrentLayer = AScheduler.getSetBit3DsSortedFrom(
            mesh.getScheduler().filterBits(layer.sortBits()));
    ArrayList<Strip> layerstrips=new ArrayList<>();

    HashSet<Bit3D> toremove=new HashSet<>();
Collections.sort(bitsInCurrentLayer,Comparator.comparing(Bit3D::getMinX));


int size=bitsInCurrentLayer.size();

while(bitsInCurrentLayer.size()>0){


// loop to find the extremist bit to left
  TreeSet<Bit3D>tofindfirstbit=new TreeSet<>(Comparator.comparing(Bit3D::getMinX ));


  for (Bit3D bit:bitsInCurrentLayer){
tofindfirstbit.add(bit);
  }

  Iterator<Bit3D>itfirst=tofindfirstbit.iterator();
  layerstrips.add(  new Strip ((NewBit3D)itfirst.next(),layer));

   for(Bit3D bit3D:bitsInCurrentLayer){
        //we verify if the bit can fit in the current strip if not we create a new strip
        if(bit3D.getTwoExtremeXPointsCS().get(0).x>=layerstrips.get(layerstrips.size()-1).getXposition()&&
                bit3D.getTwoExtremeXPointsCS().get(1).x<=layerstrips.get(layerstrips.size()-1).getXposition()
                        + CraftConfig.workingWidth && num<nbBitesBatch)
        {
          layerstrips.get(layerstrips.size()-1).addBit3D((NewBit3D) bit3D);
          toremove.add(bit3D);


          num++;

        }


   }
  if(num==nbBitesBatch) {
    num=0;
  }
   layerstrips.get(layerstrips.size()-1).getBits().sort(Comparator.comparing(Bit3D::getMinX));
   bitsInCurrentLayer.removeAll(toremove);
}
    meshstrips.add(layerstrips);


  });

  return meshstrips;




    }





}

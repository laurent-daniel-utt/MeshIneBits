package meshIneBits.gui.view3d.builder;

import java.util.Collection;
import java.util.Vector;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.NewBit2D;
import meshIneBits.NewBit3D;
import meshIneBits.SubBit2D;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.scheduler.AdvancedScheduler;
import meshIneBits.util.CustomLogger;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class BaseMeshBuilder implements IMeshShapeBuilder {

  public static final CustomLogger logger = new CustomLogger(BaseMeshBuilder.class);
  private final PApplet context;
  private final Mesh mesh;

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
      Collection<Bit3D> bitsInCurrentLayer = AScheduler.getSetBit3DsSortedFrom(
          mesh.getScheduler().filterBits(layer.sortBits()));
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
    return shapeBit;
  }
}

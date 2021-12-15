package meshIneBits.gui.view3d.builder;

import java.util.Collection;
import java.util.Vector;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.gui.view3d.Visualization3DConfig;
import meshIneBits.scheduler.AScheduler;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class BaseMeshBuilder implements IMeshShapeBuilder {

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
      bitsInCurrentLayer.forEach(bit3D -> {
        BitShape bitShape = buildBitShape(bit3D);
        int layerId = layer.getLayerNumber();
        int batchId = mesh.getScheduler().getSubBitBatch(bit3D);
        updateBitShapeLocation(bit3D, bitShape);
        bitShape.setLayerId(layerId).setBatchId(batchId);
        meshShape.addChild(bitShape.getShape());
        bitShapes.add(bitShape);
      });
    });
    return new PavedMeshBuilderResult(meshShape, bitShapes);
  }

  private void updateBitShapeLocation(Bit3D bit3D, BitShape bitShape) {
    bitShape.getShape()
        .rotateZ(PApplet.radians((float) bit3D.getOrientation().getEquivalentAngle2()));
    bitShape.getShape().translate(
        (float) bit3D.getOrigin().x,
        (float) bit3D.getOrigin().y,
        (float) bit3D.getLowerAltitude());
  }

  private BitShape buildBitShape(Bit3D bit3D) {
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
}

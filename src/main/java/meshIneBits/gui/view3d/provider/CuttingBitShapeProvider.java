package meshIneBits.gui.view3d.provider;

import java.util.HashMap;
import java.util.Map;
import meshIneBits.Mesh;
import meshIneBits.NewBit3D;
import meshIneBits.gui.view3d.builder.CuttingBitShape;
import meshIneBits.gui.view3d.builder.CuttingBitBuilder;
import processing.core.PApplet;

public class CuttingBitShapeProvider {

  private final CuttingBitBuilder builder;
  private final Mesh mesh;
  private final Map<NewBit3D, CuttingBitShape> shapeMap = new HashMap<>();

  public CuttingBitShapeProvider(PApplet context, Mesh mesh) {
    this.builder = new CuttingBitBuilder(context);
    this.mesh = mesh;
    initializeShapeMap(mesh);
  }

  private void initializeShapeMap(Mesh mesh) {
    mesh.getScheduler().getSortedBits().forEach(pair -> {
      NewBit3D bit3D = (NewBit3D) pair.getKey();
      shapeMap.put(bit3D, buildCuttingBitShape(bit3D));
    });
  }

  private CuttingBitShape buildCuttingBitShape(NewBit3D bit3D) {
    return builder.buildCuttingBitShape(bit3D);
  }

  public CuttingBitShape getCuttingBitShapeByBit(NewBit3D bit3D) {
    return shapeMap.get(bit3D);
  }

  public CuttingBitShape getCuttingBitShapeById(int bitId) {
    return shapeMap.get(mesh.getScheduler().getSortedBits().get(bitId).getKey());
  }


}

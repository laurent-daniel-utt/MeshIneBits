package meshIneBits.gui.view3d.provider;

import java.util.HashMap;
import java.util.Map;
import meshIneBits.Project;
import meshIneBits.NewBit3D;
import meshIneBits.gui.view3d.builder.CuttingBitBuilder;
import meshIneBits.gui.view3d.builder.CuttingBitShape;
import processing.core.PApplet;

public class CuttingBitShapeProvider {

  private final CuttingBitBuilder builder;
  private final Project project;
  private final Map<NewBit3D, CuttingBitShape> shapeMap = new HashMap<>();

  public CuttingBitShapeProvider(PApplet context, Project project) {
    this.builder = new CuttingBitBuilder(context);
    this.project = project;
    initializeShapeMap(project);
  }

  private void initializeShapeMap(Project project) {
    if (project == null) {
      return;
    }
    project.getScheduler().getSortedBits().forEach(pair -> {
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
    return shapeMap.get(project.getScheduler().getSortedBits().get(bitId).getKey());
  }


}

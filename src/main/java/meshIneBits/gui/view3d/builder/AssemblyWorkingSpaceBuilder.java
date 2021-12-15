package meshIneBits.gui.view3d.builder;

import meshIneBits.gui.view3d.Visualization3DConfig;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

public class AssemblyWorkingSpaceBuilder {

  private final PApplet context;

  public AssemblyWorkingSpaceBuilder(PApplet context) {
    this.context = context;
  }

  public PShape build() {
    return context.createShape(PConstants.RECT, 0,
        -Visualization3DConfig.PRINTER_Y / 2,
        Visualization3DConfig.ASSEMBLY_WORKING_SPACE_WIDTH,
        Visualization3DConfig.ASSEMBLY_WORKING_SPACE_HEIGHT);
  }
}

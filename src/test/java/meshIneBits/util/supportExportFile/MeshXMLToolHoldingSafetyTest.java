package meshIneBits.util.supportExportFile;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import meshIneBits.Bit2D;
import meshIneBits.NewBit2D;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Vector2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MeshXMLToolHoldingSafetyTest {

  private boolean savedUseRule;
  private double savedAnchorX;
  private double savedAnchorY;
  private double savedHoldingX;
  private double savedHoldingY;

  @BeforeEach
  void saveCraftConfig() {
    savedUseRule = CraftConfig.useDataMatrixAnchorForFallRule;
    savedAnchorX = CraftConfig.dataMatrixAnchorX_CB;
    savedAnchorY = CraftConfig.dataMatrixAnchorY_CB;
    savedHoldingX = CraftConfig.holdingPointX_CB;
    savedHoldingY = CraftConfig.holdingPointY_CB;
  }

  @AfterEach
  void restoreCraftConfig() {
    CraftConfig.useDataMatrixAnchorForFallRule = savedUseRule;
    CraftConfig.dataMatrixAnchorX_CB = savedAnchorX;
    CraftConfig.dataMatrixAnchorY_CB = savedAnchorY;
    CraftConfig.holdingPointX_CB = savedHoldingX;
    CraftConfig.holdingPointY_CB = savedHoldingY;
  }

  private static Area twoVerticalHalves() {
    double halfL = CraftConfig.lengthFull / 2.0;
    double halfW = CraftConfig.bitWidth / 2.0;
    double gap = 0.05;
    Area a = new Area();
    a.add(new Area(new Rectangle2D.Double(-halfL, -halfW, halfL - gap, 2.0 * halfW)));
    a.add(new Area(new Rectangle2D.Double(gap, -halfW, halfL - gap, 2.0 * halfW)));
    return a;
  }

  @Test
  void nominal_anchorAndHoldingInSamePolygon_safeNoException() {
    CraftConfig.useDataMatrixAnchorForFallRule = true;
    CraftConfig.dataMatrixAnchorX_CB = -70.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;
    CraftConfig.holdingPointX_CB = -60.0;
    CraftConfig.holdingPointY_CB = 0.0;

    NewBit2D bit = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));
    bit.updateBoundaries(twoVerticalHalves());

    Bit2D.HoldingSafetyStatus safety = bit.checkHoldingSafety();
    assertTrue(safety.isSafe());
    try {
      MeshXMLTool.throwIfUnsafeHolding(safety, 42);
    } catch (Exception e) {
      fail("No exception expected in nominal safe case");
    }
  }

  @Test
  void conflict_anchorAndHoldingInDifferentPolygons_warningOnlyNoException() {
    CraftConfig.useDataMatrixAnchorForFallRule = true;
    CraftConfig.dataMatrixAnchorX_CB = -70.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;
    CraftConfig.holdingPointX_CB = 70.0;
    CraftConfig.holdingPointY_CB = 0.0;

    NewBit2D bit = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));
    bit.updateBoundaries(twoVerticalHalves());

    Bit2D.HoldingSafetyStatus safety = bit.checkHoldingSafety();
    assertTrue(!safety.isSafe());
    try {
      MeshXMLTool.throwIfUnsafeHolding(safety, 99);
    } catch (Exception e) {
      fail("No exception expected: unsafe holding must only warn, not block export");
    }
  }

  @Test
  void nonApplicable_singleArea_noException() {
    CraftConfig.useDataMatrixAnchorForFallRule = true;
    CraftConfig.dataMatrixAnchorX_CB = -70.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;
    CraftConfig.holdingPointX_CB = 70.0;
    CraftConfig.holdingPointY_CB = 0.0;

    NewBit2D bit = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));

    Bit2D.HoldingSafetyStatus safety = bit.checkHoldingSafety();
    assertTrue(!safety.isApplicable());
    try {
      MeshXMLTool.throwIfUnsafeHolding(safety, 7);
    } catch (Exception e) {
      fail("No exception expected when holding safety is not applicable");
    }
  }
}

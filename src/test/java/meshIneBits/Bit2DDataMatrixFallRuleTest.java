/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 */

package meshIneBits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.Vector2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Two disjoint rectangles in bit CB (vertical split) exercise {@link Bit2D#calcCutPath()} DataMatrix
 * fall ordering: the sub-piece containing the anchor is never tagged as chute; the other is last for
 * export.
 */
class Bit2DDataMatrixFallRuleTest {

  private boolean savedUseRule;
  private double savedAnchorX;
  private double savedAnchorY;

  @BeforeEach
  void saveCraftConfig() {
    savedUseRule = CraftConfig.useDataMatrixAnchorForFallRule;
    savedAnchorX = CraftConfig.dataMatrixAnchorX_CB;
    savedAnchorY = CraftConfig.dataMatrixAnchorY_CB;
  }

  @AfterEach
  void restoreCraftConfig() {
    CraftConfig.useDataMatrixAnchorForFallRule = savedUseRule;
    CraftConfig.dataMatrixAnchorX_CB = savedAnchorX;
    CraftConfig.dataMatrixAnchorY_CB = savedAnchorY;
  }

  /** Gap so the two parts are topologically disjoint ({@link meshIneBits.util.AreaTool#segregateArea} yields two areas). */
  private static final double SPLIT_GAP_MM = 0.1;

  /**
   * Two rectangles in mesh space = CB for origin (0,0), orientation (1,0), split near x=0 with a
   * narrow gap (not one connected polygon).
   */
  private static Area twoVerticalHalves() {
    double halfL = CraftConfig.lengthFull / 2.0;
    double halfW = CraftConfig.bitWidth / 2.0;
    double g = SPLIT_GAP_MM / 2.0;
    Area a = new Area();
    a.add(new Area(new Rectangle2D.Double(-halfL, -halfW, halfL - g, 2.0 * halfW)));
    a.add(new Area(new Rectangle2D.Double(g, -halfW, halfL - g, 2.0 * halfW)));
    return a;
  }

  /** Left narrow (40 mm), right wide; anchor in left — main is smaller than fall. */
  private static Area narrowLeftWideRight() {
    double halfL = CraftConfig.lengthFull / 2.0;
    double halfW = CraftConfig.bitWidth / 2.0;
    double leftW = 40.0;
    double g = SPLIT_GAP_MM;
    Area a = new Area();
    a.add(new Area(new Rectangle2D.Double(-halfL, -halfW, leftW - g / 2.0, 2.0 * halfW)));
    a.add(new Area(new Rectangle2D.Double(-halfL + leftW + g / 2.0, -halfW,
        CraftConfig.lengthFull - leftW - g / 2.0, 2.0 * halfW)));
    return a;
  }

  @Test
  void anchorInLeftHalf_fallPathIsLast_explicitChuteSet() {
    CraftConfig.useDataMatrixAnchorForFallRule = true;
    CraftConfig.dataMatrixAnchorX_CB = -70.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;

    NewBit2D bit = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));
    bit.updateBoundaries(twoVerticalHalves());

    assertEquals(1, bit.getFallCutPathIndex());
    assertEquals(Boolean.TRUE, bit.getExplicitChuteOnLastExportPath());
  }

  @Test
  void anchorInRightHalf_fallPathIsLast_mainIsSmallerSide() {
    CraftConfig.useDataMatrixAnchorForFallRule = true;
    CraftConfig.dataMatrixAnchorX_CB = 40.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;

    NewBit2D bit = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));
    bit.updateBoundaries(twoVerticalHalves());

    assertEquals(1, bit.getFallCutPathIndex());
    assertEquals(Boolean.TRUE, bit.getExplicitChuteOnLastExportPath());
  }

  @Test
  void anchorInSmallerPiece_largerPieceIsFall_stillFallPathLast() {
    CraftConfig.useDataMatrixAnchorForFallRule = true;
    CraftConfig.dataMatrixAnchorX_CB = -60.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;

    NewBit2D bit = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));
    bit.updateBoundaries(narrowLeftWideRight());

    assertEquals(1, bit.getFallCutPathIndex());
    assertEquals(Boolean.TRUE, bit.getExplicitChuteOnLastExportPath());
  }

  @Test
  void anchorOnCut_resolvedByRayTowardCentroid_fallPathLast() {
    CraftConfig.useDataMatrixAnchorForFallRule = true;
    CraftConfig.dataMatrixAnchorX_CB = 0.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;

    NewBit2D bit = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));
    bit.updateBoundaries(twoVerticalHalves());

    assertEquals(1, bit.getFallCutPathIndex());
    assertEquals(Boolean.TRUE, bit.getExplicitChuteOnLastExportPath());
  }

  @Test
  void ruleDisabled_noExplicitChuteFlag() {
    CraftConfig.useDataMatrixAnchorForFallRule = false;
    CraftConfig.dataMatrixAnchorX_CB = -70.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;

    NewBit2D bit = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));
    bit.updateBoundaries(twoVerticalHalves());

    assertEquals(-1, bit.getFallCutPathIndex());
    assertNull(bit.getExplicitChuteOnLastExportPath());
  }
}

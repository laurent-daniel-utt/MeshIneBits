package meshIneBits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.nio.file.Paths;
import meshIneBits.config.CraftConfig;
import meshIneBits.config.MeshTagXML;
import meshIneBits.slicer.Slice;
import meshIneBits.util.Vector2;
import meshIneBits.util.supportExportFile.MeshXMLTool;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class MeshXMLIntegrityTest {

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

  private static Area polygonArea(double... xy) {
    Path2D.Double path = new Path2D.Double();
    path.moveTo(xy[0], xy[1]);
    for (int i = 2; i < xy.length; i += 2) {
      path.lineTo(xy[i], xy[i + 1]);
    }
    path.closePath();
    return new Area(path);
  }

  private static Area twoDiagonalPieces() {
    double halfL = CraftConfig.lengthFull / 2.0;
    double halfW = CraftConfig.bitWidth / 2.0;
    Area a = new Area();
    // Left piece (DataMatrix side), right edge is diagonal.
    a.add(polygonArea(
        -halfL, -halfW,
        -halfL, halfW,
        -8.0, halfW,
        -18.0, -halfW
    ));
    // Right piece (fall), left edge is diagonal and separated by a narrow gap.
    a.add(polygonArea(
        2.0, -halfW,
        12.0, halfW,
        halfL, halfW,
        halfL, -halfW
    ));
    return a;
  }

  @Test
  void diagonalCut_xmlReflectsDataMatrixGeometry_fallIsLastAndDropped() {
    CraftConfig.useDataMatrixAnchorForFallRule = true;
    CraftConfig.dataMatrixAnchorX_CB = -60.0;
    CraftConfig.dataMatrixAnchorY_CB = 0.0;
    CraftConfig.holdingPointX_CB = -55.0;
    CraftConfig.holdingPointY_CB = 0.0;

    NewBit2D bit2D = new NewBit2D(new Vector2(0, 0), new Vector2(1, 0));
    bit2D.updateBoundaries(twoDiagonalPieces());
    assertEquals(2, bit2D.getAreasCB().size());
    assertEquals(1, bit2D.getFallCutPathIndex());

    Slice slice = new Slice();
    slice.setAltitude(0.0);
    Layer layer = new Layer(0, slice);
    NewBit3D bit3D = new NewBit3D(bit2D, layer);

    Mesh mesh = new Mesh();
    MeshXMLTool tool = new MeshXMLTool(Paths.get("build", "tmp", "integrity-test.xml"));
    tool.initialize(mesh);
    Element bitElement = tool.buildBitElement(bit3D);

    NodeList cuttingNodes = bitElement.getElementsByTagName(MeshTagXML.CUT_BIT);
    assertEquals(1, cuttingNodes.getLength());
    Element cutting = (Element) cuttingNodes.item(0);

    NodeList cutPaths = cutting.getElementsByTagName(MeshTagXML.CUT_PATHS);
    assertEquals(2, cutPaths.getLength());

    Element firstCutPath = (Element) cutPaths.item(0);
    Element lastCutPath = (Element) cutPaths.item(1);

    assertEquals(0, firstCutPath.getElementsByTagName(MeshTagXML.DROP).getLength());
    assertEquals(1, lastCutPath.getElementsByTagName(MeshTagXML.DROP).getLength());

    NodeList fallTypeTags = lastCutPath.getElementsByTagName(MeshTagXML.FALL_TYPE);
    assertTrue(fallTypeTags.getLength() >= 2);
    assertEquals(MeshTagXML.CHUTE_TYPE, fallTypeTags.item(0).getTextContent());
    assertEquals(MeshTagXML.SUB_BIT, fallTypeTags.item(fallTypeTags.getLength() - 1).getTextContent());
  }
}

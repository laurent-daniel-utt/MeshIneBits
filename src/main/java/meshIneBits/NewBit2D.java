package meshIneBits;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Vector;
import java.util.stream.Collectors;
import meshIneBits.SubBit2D.SubBitBuilder;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.CustomLogger;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;

public class NewBit2D extends Bit2D {

  private static final CustomLogger logger = new CustomLogger(NewBit2D.class);
  private final Vector<SubBit2D> subBits = new Vector<>();

  public NewBit2D(Vector2 origin, Vector2 orientation) {
    this(origin, orientation, CraftConfig.lengthFull, CraftConfig.bitWidth);
  }

  public NewBit2D(Vector2 origin, Vector2 orientation, double length, double width) {
    super(origin, orientation, length, width);
    calcCutPath();
    buildSubBits(getAreasCS(), getCutPathsCB());
  }

  private NewBit2D(Vector2 origin,
      Vector2 orientation,
      double length,
      double width,
      AffineTransform transfoMatrix,
      AffineTransform inverseTransfoMatrix,
      Vector<Path2D> cutPaths,
      Vector<Area> areas) {
    super(origin, orientation, length, width, transfoMatrix, inverseTransfoMatrix, cutPaths, areas);
    buildSubBits(getAreasCB(), getCutPathsCB());

  }

  private void buildSubBits(Vector<Area> areas, Vector<Path2D> cutPaths) {
    subBits.clear();
    if (cutPaths.size() == 0) {
      SubBitBuilder builder = new SubBitBuilder();
      SubBit2D subBit2D = builder.setOriginPositionCS(getOriginCS())
          .setOrientationCS(getOrientation())
          .setTransformMatrixToCS(getTransfoMatrixToCS())
          .setInverseMatrixToCB(getInverseTransfoMatrixToCB())
          .setAreaCB(areas.get(0))
          .setCutPath(null)
          .build();
      subBits.add(subBit2D);
      return;
    }
    if (areas.size() != cutPaths.size()) {
      throw new IllegalArgumentException("SubBit areas and subBit cutPath must have the same size");
    }
    for (int i = 0; i < areas.size(); i++) {
      SubBitBuilder builder = new SubBitBuilder();
      SubBit2D subBit2D = builder.setOriginPositionCS(getOriginCS())
          .setOrientationCS(getOrientation())
          .setTransformMatrixToCS(getTransfoMatrixToCS())
          .setInverseMatrixToCB(getInverseTransfoMatrixToCB())
          .setAreaCB(areas.get(i))
          .setCutPath(cutPaths.get(i))
          .build();
      subBits.add(subBit2D);
    }
    subBits.sort((sub1, sub2) -> {
      Vector2 lift1 = sub1.getLiftPointCB();
      Vector2 lift2 = sub2.getLiftPointCB();
      if(lift1==null)return -1;
      if(lift2==null)return 1;
      return Double.compare(lift1.y, lift2.y) == 0 ?
          Double.compare(lift1.x, lift2.x) :
          Double.compare(lift1.y, lift2.y);
    });
  }

  public Vector<SubBit2D> getSubBits() {
    return new Vector<>(subBits);

  }

  public Vector<SubBit2D> getValidSubBits() {
    return subBits.stream()
        .filter(SubBit2D::isValid)
        .collect(Collectors.toCollection(Vector::new));
  }

  public boolean isValid() {
    return getValidSubBits().size() > 0;
  }


  @Override
  public void updateBoundaries(@NotNull Area transformedArea) {
    super.updateBoundaries(transformedArea);
    calcCutPath();
    buildSubBits(getAreasCB(), getCutPathsCB());
  }

  @Override
  public NewBit2D createTransformedBit(AffineTransform transformation) {
    Vector2 newOrigin = getOriginCS().getTransformed(transformation).getRounded();
    Vector2 newOrientation = getOriginCS().add(getOrientation())
        .getTransformed(transformation)
        .sub(newOrigin)
        .normal()
        .getRounded();
    NewBit2D newBit = new NewBit2D(newOrigin, newOrientation, getLength(), getWidth());
    newBit.updateBoundaries(this.getAreaCS().createTransformedArea(transformation));
    return newBit;
  }

  @Override
  public void resize(double newPercentageLength, double newPercentageWidth) {
    super.resize(newPercentageLength, newPercentageWidth);
    calcCutPath();
    buildSubBits(getAreasCB(), getCutPathsCB());
  }

  @Override
  @SuppressWarnings("all")
  public NewBit2D clone() {
    return new NewBit2D(getOriginCS().clone(),
        getOrientation().clone(),
        getLength(),
        getWidth(),
        (AffineTransform) getTransfoMatrixToCS().clone(),
        (AffineTransform) getInverseTransfoMatrixToCB().clone(),
        (Vector<Path2D>) getCutPathsCB().clone(),
        (Vector<Area>) getAreasCB().clone());
  }

  @Override
  public void calcCutPath() {
    super.calcCutPath();
  }
}

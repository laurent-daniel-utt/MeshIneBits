package meshIneBits;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.LiftPointCalc;
import meshIneBits.util.TwoDistantPointsCalc;
import meshIneBits.util.Vector2;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO define readObject and writeObject for save mesh
public class SubBit2D implements Serializable {

  private final Bit2D parentBit;
  private final Vector2 originPositionCS;
  private final Vector2 orientationCS;
  private Vector2 liftPointCB;

  private Vector2 firstDistantPointCB;
  private Vector2 secondDistantPointCB;

  private final AffineTransform transformMatrixToCS;
  private AffineTransform inverseMatrixToCB;

  private final Area areaCB;
  private final Path2D cutPath;

  public SubBit2D(
      @NotNull Vector2 originPositionCS,
      @NotNull Vector2 orientationCS,
      @NotNull AffineTransform transformMatrixToCS,
      @NotNull AffineTransform inverseMatrixToCB,
      @NotNull Area area,
      @Nullable Path2D cutPath,
      @NotNull Bit2D parentBit) {

    this.originPositionCS = originPositionCS;
    this.orientationCS = orientationCS;
    this.transformMatrixToCS = transformMatrixToCS;
    this.inverseMatrixToCB = inverseMatrixToCB;
    this.areaCB = area;
    this.cutPath = cutPath;
    this.parentBit = parentBit;

    computeDistantPoints();
    computeLiftPoints();
  }

  public Vector2 getOriginPositionCS() {
    return originPositionCS;
  }

  public Vector2 getOrientationCS() {
    return orientationCS;
  }

  public Bit2D getParentBit() {
    return parentBit;
  }

  public Area getAreaCB() {
    return areaCB;
  }

  public Area getAreaCS() {
    Area areaCS = (Area) areaCB.clone();
    areaCS.transform(transformMatrixToCS);
    return areaCS;
  }

  @SuppressWarnings("unused")
  public Path2D getCutPathCB() {
    return cutPath;
  }

  public Vector2 getLiftPointCS() {
    if (liftPointCB == null) {
      return null;
    }
    return liftPointCB.getTransformed(transformMatrixToCS);
  }

  public Vector2 getLiftPointCB() {
    return liftPointCB;
  }


  private void computeDistantPoints() {
    Vector<Vector2> points = TwoDistantPointsCalc.instance.defineTwoPointNearTwoMostDistantPointsInAreaWithRadius(
        areaCB, CraftConfig.suckerDiameter / 4);
    if (points.size() == 2) {
      points.sort((a, b) -> (int) (a.x == b.x ? a.y - b.y : a.x - b.x));
      firstDistantPointCB = points.get(0);
      secondDistantPointCB = points.get(1);
    }
  }

  @SuppressWarnings("unused")
  public Vector<Vector2> getTwoDistantPointsCB() {
    return new Vector<>(Arrays.asList(firstDistantPointCB, secondDistantPointCB));
  }

  public Vector<Vector2> getTwoDistantPointsCS() {
    if (firstDistantPointCB == null || secondDistantPointCB == null) {
      return new Vector<>();
    }
    return new Vector<>(
        Arrays.asList(
            firstDistantPointCB.getTransformed(transformMatrixToCS),
            secondDistantPointCB.getTransformed(transformMatrixToCS)));
  }

  private void computeLiftPoints() {
    liftPointCB = LiftPointCalc.instance.getLiftPoint(areaCB, CraftConfig.suckerDiameter / 2);
  }

  public static class SubBitBuilder {

    private Vector2 iOriginPositionCS;
    private Vector2 iOrientationCS;

    private AffineTransform iTransformMatrixToCS = new AffineTransform();
    private AffineTransform iInverseMatrixToCB = new AffineTransform();

    private Area iAreaCB;
    private Path2D iCutPath;
    private Bit2D parentBit;

    public SubBitBuilder setOriginPositionCS(Vector2 originPositionCS) {
      this.iOriginPositionCS = originPositionCS;
      return this;
    }

    public SubBitBuilder setOrientationCS(Vector2 orientationCS) {
      this.iOrientationCS = orientationCS;
      return this;
    }

    public SubBitBuilder setTransformMatrixToCS(AffineTransform transformMatrixToCS) {
      this.iTransformMatrixToCS = transformMatrixToCS;
      return this;
    }

    public SubBitBuilder setInverseMatrixToCB(AffineTransform inverseMatrixToCB) {
      this.iInverseMatrixToCB = inverseMatrixToCB;
      return this;
    }

    public SubBitBuilder setAreaCB(Area areaCB) {
      this.iAreaCB = areaCB;
      return this;
    }

    public SubBitBuilder setCutPath(Path2D cutPath) {
      this.iCutPath = cutPath;
      return this;
    }

    public SubBitBuilder setParentBit(Bit2D parentBit) {
      this.parentBit = parentBit;
      return this;
    }

    public SubBit2D build() {
      return new SubBit2D(
          iOriginPositionCS,
          iOrientationCS,
          iTransformMatrixToCS,
          iInverseMatrixToCB,
          iAreaCB,
          iCutPath,
          parentBit);
    }
  }

  public boolean isValid() {
    return liftPointCB != null;
  }
}
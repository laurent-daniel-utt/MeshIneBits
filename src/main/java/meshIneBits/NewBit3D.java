package meshIneBits;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;
import meshIneBits.util.Vector2;

public class NewBit3D extends Bit3D {

  NewBit3D(NewBit2D baseBit, Layer layer) {
    super(baseBit, layer);
  }

  @Override
  public NewBit2D getBaseBit() {
    return (NewBit2D) super.getBaseBit();
  }

  @Override
  public Vector<Vector2> getTwoDistantPointsCS() {
    return getBaseBit()
        .getSubBits()
        .stream()
        .map(SubBit2D::getTwoDistantPointsCS)
        .flatMap(Collection::stream)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(Vector::new));
  }

  @Override
  public List<Vector2> getLiftPointsCS() {
    return getBaseBit()
        .getSubBits()
        .stream()
        .map(SubBit2D::getLiftPointCS)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(Vector::new));
  }

  @Override
  public List<Vector2> getLiftPointsCB() {
    return getBaseBit()
        .getSubBits()
        .stream()
        .map(SubBit2D::getLiftPointCB)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(Vector::new));
  }

  @Override
  public Vector<SubBit2D> getSubBits() {
    return getBaseBit().getSubBits();
  }
}

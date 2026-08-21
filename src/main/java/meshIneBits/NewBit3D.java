/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2015-2026 DANIEL Laurent.
 -  Copyright (C) 2015 Cédric Siourakhan
 -  Copyright (C) 2016 Gabriel Magny
 -  Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 -  Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 -  Copyright (C) 2018 VALLON Benjamin.
 -  Copyright (C) 2018 LORIMER Campbell.
 -  Copyright (C) 2018 D'AUTUME Christian.
 -  Copyright (C) 2019 DURINGER Nathan (Tests).
 -  Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 -  Copyright (C) 2020-2021 DO Quang Bao.
 -  Copyright (C) 2021 VANNIYASINGAM Mithulan.
 -  Copyright (C) 2021  Quang Long
 -  Copyright (C) 2022 Mhamad Atlab
 -  Copyright (C) 2022 Majed Hlaihel
 -  Copyright (C) 2026 Aymeric Sirejol
 -  Copyright (C) 2026 Hugo BENOIT
 -
 -  This program is free software: you can redistribute it and/or modify
 -  it under the terms of the GNU General Public License as published by
 -  the Free Software Foundation, either version 3 of the License, or
 -  (at your option) any later version.
 -
 -  This program is distributed in the hope that it will be useful,
 -  but WITHOUT ANY WARRANTY; without even the implied warranty of
 -  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 -  GNU General Public License for more details.
 -
 -  You should have received a copy of the GNU General Public License
 -  along with this program. If not, see <https://www.gnu.org/licenses/>.
 -----------------------------------------------------------------------------*/

package meshIneBits;

import meshIneBits.util.Vector2;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

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





  public Vector<Vector2> getTwoExtremeXPointsCS() {
    return getBaseBit()
            .getSubBits()
            .stream()
            .map(SubBit2D::getTwoExtremeXPointsCS)
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

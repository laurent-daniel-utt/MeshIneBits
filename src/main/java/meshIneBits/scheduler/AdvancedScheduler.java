/*------------------------------------------------------------------------------
 -  MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 -  into a network of standard parts (called "Bits").
 -
 -  Copyright (C) 2016-2026 DANIEL Laurent.
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

package meshIneBits.scheduler;

import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.NewBit2D;
import meshIneBits.SubBit2D;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.CustomLogger;

import java.util.ArrayList;
import java.util.Vector;

public class AdvancedScheduler extends BasicScheduler {

  public static final CustomLogger logger = new CustomLogger(AdvancedScheduler.class);
  private final Vector<SubBit2D> subBit2Ds = new Vector<>();

  @Override
  public boolean schedule() {
    boolean b = super.schedule();
    try { if(!subBit2Ds.isEmpty())subBit2Ds.clear();

      sortedBits.forEach(bit -> {
        //TODO sort subBit here
//        ((NewBit2D) bit.getKey().getBaseBit())
//            .getSubBits()
//            .sort((sub1, sub2) -> {
//              if (sub1 != null && sub2 != null) {
//                return (int) (sub1.getTwoDistantPointsCB().get(0).x
//                    - sub2.getTwoDistantPointsCB().get(0).x);
//              }
//              return 0;
//            });
        subBit2Ds.addAll(((NewBit2D) bit.getKey().getBaseBit()).getValidSubBits());

      });

    } catch (ClassCastException e) {
      e.printStackTrace();
      logger.logDEBUGMessage("AdvancedScheduler is only used with newBit3D and newBit2D!");
    }

    int sum= mesh.getStripes().stream().mapToInt(ArrayList::size).sum();


    logger.logDEBUGMessage("Number of subBit: " + subBit2Ds.size());
    logger.logDEBUGMessage("Number of Stripes: "+sum );

    return b;
  }

  public int getIndexOfSubBit(SubBit2D subBit2D) {
    return subBit2Ds.indexOf(subBit2D);
  }

  public SubBit2D getSubBitByIndex(int subBitId) {
    if (!containsSubBit(subBitId)) {
      return null;
    }
    return subBit2Ds.get(subBitId);
  }

  public boolean containsSubBit(int subBitId) {
    return subBitId < subBit2Ds.size();
  }

  public int getSubBitBatch(SubBit2D subBit2D) {
    if (subBit2Ds.isEmpty()) {
      return 0;
    }
    int index = this.getIndexOfSubBit(subBit2D);
    return index > 0 ? (index / CraftConfig.nbBitesBatch) : -1;
   // return index > 0 ? (index / 72) : -1;
  }

  public int getSubBitPlate(SubBit2D subBit2D) {
    if (subBit2Ds.isEmpty()) {
      return 0;
    }
    return this.getIndexOfSubBit(subBit2D) / CraftConfig.nbBitesByPlat;
  }

  public Bit3D getBit3DFrom(Bit2D bit2D) {
    if (sortedBits.isEmpty()) {
      return null;
    }
    return this.sortedBits.stream()
        .filter(pair -> pair.getKey().getBaseBit().equals(bit2D))
        .findFirst()
        .get()
        .getKey();
  }





}

package meshIneBits.scheduler;

import meshIneBits.Bit2D;
import meshIneBits.Bit3D;
import meshIneBits.NewBit2D;
import meshIneBits.SubBit2D;
import meshIneBits.config.CraftConfig;
import meshIneBits.util.CustomLogger;

import java.util.Vector;

public class AdvancedScheduler extends BasicScheduler {

  public static final CustomLogger logger = new CustomLogger(AdvancedScheduler.class);
  private final Vector<SubBit2D> subBit2Ds = new Vector<>();

  @Override
  public boolean schedule() {
    boolean b = super.schedule();
    try {
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
    logger.logDEBUGMessage("Number of subBit: " + subBit2Ds.size());
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

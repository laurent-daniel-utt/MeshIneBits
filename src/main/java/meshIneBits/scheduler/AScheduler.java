/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2021 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020 CLARIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package meshIneBits.scheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Vector;
import javafx.util.Pair;
import meshIneBits.Bit3D;
import meshIneBits.Mesh;
import meshIneBits.MeshEvents;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

public abstract class AScheduler extends Observable implements Serializable, Runnable {

  protected Mesh mesh = null;
  protected Vector<Pair<Bit3D, Vector2>> sortedBits = new Vector<>();
  //    protected Vector<Bit3D> firstLayerBits = new Vector<>();
  protected Map<Integer, Bit3D> firstLayerBits = new HashMap<>();

  AScheduler() {
  }

  AScheduler(Mesh m) {
    super();
    addObserver(m);
    mesh = m;
  }

  public void setMesh(Mesh m) {
    this.mesh = m;
  }

  /**
   * Function used to return bit index in the ordering process
   *
   * @param bit
   * @return
   */
  public abstract int getBitIndex(Bit3D bit);

  /**
   * Return batch index for a bit
   *
   * @param bit
   * @return
   */
  public abstract int getBitBatch(Bit3D bit);

  /**
   * Return plate index for a bit
   *
   * @param bit
   * @return
   */
  public abstract int getBitPlate(Bit3D bit);

  /**
   * Lauch ordering process
   *
   * @return
   */
  public abstract boolean order();

  public boolean isScheduled() {
    return !this.sortedBits.isEmpty();
  }

  public Vector<Pair<Bit3D, Vector2>> getSortedBits() {
    return this.sortedBits;
  }

  //    public Vector<Bit3D> getFirstLayerBits() { return this.firstLayerBits; }
  public Map<Integer, Bit3D> getFirstLayerBits() {
    return this.firstLayerBits;
  }

  public abstract boolean schedule();

  public void run() {
    notifyObservers(MeshEvents.SCHEDULING);
    Logger.updateStatus("Starting bits cut & place scheduling operation.");
    schedule();
    Logger.updateStatus("Bits cut & place scheduling is over.");
    notifyObservers(MeshEvents.SCHEDULED);
  }

  public static List<Bit3D> getSetBit3DsSortedFrom(Vector<Pair<Bit3D, Vector2>> arg) {
    List<Bit3D> result = new ArrayList<>();
    for (Pair<Bit3D, Vector2> ele : arg) {
      if (!result.contains(ele.getKey())) {
        result.add(ele.getKey());
      }
    }
    return result;
  }

  public abstract Vector<Pair<Bit3D, Vector2>> filterBits(Vector<Pair<Bit3D, Vector2>> bits);


}

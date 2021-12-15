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

package meshIneBits.util.supportUndoRedo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import meshIneBits.Bit3D;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.util.Vector2;

public class ActionOfUserMoveBit implements HandlerRedoUndo.ActionOfUser {

  /**
   * Clone of bits before transformation
   */
  private Set<Bit3D> previousState;
  /**
   * Keys of bits after transformation
   */
  private Set<Vector2> resultKeys;
  private Set<Bit3D> currentSelectedBits;
  private int layerNum;
  private Set<Vector2> previousSelectedBits = new HashSet<>();


  public ActionOfUserMoveBit(Set<Bit3D> previousState, Set<Vector2> previousSelectedBits,
      Set<Vector2> resultKeys, Set<Bit3D> currentSelectedBits, int layerNumber) {
    if (previousState != null) {
      this.previousState = new HashSet<>(previousState);
    }
    if (previousSelectedBits != null) {
      this.previousSelectedBits = previousSelectedBits;
    }
    if (resultKeys != null) {
      this.resultKeys = resultKeys;
    }
    if (currentSelectedBits != null) {
      System.out.println("OKKK");
      this.currentSelectedBits = new HashSet<>(currentSelectedBits);
    }
    this.layerNum = layerNumber;
  }

  public ActionOfUserMoveBit(Set<Vector2> resultKeys, Set<Bit3D> currentSelectedBits,
      int layerNum) {
    this(null, null, resultKeys, currentSelectedBits, layerNum);
  }

  public Collection<Vector2> getResultKeys() {
    return resultKeys;
  }

  public Collection<Bit3D> getPreviousState() {
    return previousState;
  }

  @Override
  public void runUndo(MeshController meshController) {
    meshController.setLayer(this.layerNum);
    // Remove the latest bits with registered result keys
    if (this.currentSelectedBits != null && this.resultKeys != null) {
      meshController.deleteBitsByBitsAndKeys(this.currentSelectedBits, this.resultKeys);
    }
    // Restore deleted bits
    // By adding the previous state
    if (this.previousState != null && this.previousState.size() != 0) {
      meshController.addBit3Ds(this.previousState);
      meshController.setSelectedBitKeys(this.previousSelectedBits);
    }
  }

  @Override
  public void runRedo(MeshController meshController) {
    meshController.setLayer(this.layerNum);
    // Remove the latest bits with registered result keys
    meshController.setSelectedBitKeys(this.previousSelectedBits);
    if (this.previousState != null && this.previousState.size() != 0) {
      meshController.deleteBitsByBitsAndKeys(this.previousState, this.previousSelectedBits);

    }
    // Restore deleted bits
    // By adding the previous state
    if (this.currentSelectedBits != null) {
      meshController.addBit3Ds(this.currentSelectedBits);
      meshController.setSelectedBitKeys(this.resultKeys);
    }
  }
}
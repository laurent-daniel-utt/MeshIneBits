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

import meshIneBits.config.CraftConfig;
import meshIneBits.gui.view2d.MeshController;
import meshIneBits.util.Logger;
import meshIneBits.util.Vector2;

import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Save actions scaling bits
 * */
public class ActionOfUserScaleBit implements HandlerRedoUndo.ActionOfUser {
    private double setPercentageLength;
    private double setPercentageWidth;
    private Map<Vector2,Double[]> keyMapLengthWidth;


    public ActionOfUserScaleBit(Map<Vector2,Double[]> keyMapLengthWidth,double setPercentageLength, double setPercentageWidth) {
        this.setPercentageLength = setPercentageLength;
        this.setPercentageWidth = setPercentageWidth;
        this.keyMapLengthWidth =keyMapLengthWidth;
    }

    @Override
    public void runUndo(MeshController meshController) {
        meshController.setSelectedBitKeys(new HashSet<>(this.keyMapLengthWidth.keySet()));
        if (meshController.getSelectedBitKeys().isEmpty()) {
            Logger.warning("There is no bit selected");
        } else {
            meshController.setSelectedBitKeys(meshController.getSelectedBits().stream()
                    .map(bit -> meshController.getCurrentLayer().scaleBit(bit,(this.keyMapLengthWidth.get(bit.getOrigin())[0]/ CraftConfig.lengthFull)*100, (this.keyMapLengthWidth.get(bit.getOrigin())[1] / CraftConfig.bitWidth)*100))
                    .collect(Collectors.toSet()));
        }
    }

    @Override
    public void runRedo(MeshController meshController) {
        meshController.setSelectedBitKeys(new HashSet<>(this.keyMapLengthWidth.keySet()));
        if (meshController.getSelectedBitKeys().isEmpty()) {
            Logger.warning("There is no bit selected");
        } else {
            meshController.setSelectedBitKeys(meshController.getSelectedBits().stream()
                    .map(bit -> meshController.getCurrentLayer().scaleBit(bit,setPercentageLength, setPercentageWidth))
                    .collect(Collectors.toSet()));
        }
    }
}

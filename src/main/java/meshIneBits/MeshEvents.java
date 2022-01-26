/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
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
 *
 */

package meshIneBits;

import java.io.Serializable;

public enum MeshEvents implements Serializable {
    READY(false, 0),
    IMPORT_FAILED(false, 99),
    IMPORTING(true, 100),
    IMPORTED(false, 101),
    SLICING(true, 200),
    SLICED(false, 201),
    PAVING_MESH(true, 300),
    PAVED_MESH(false, 301),
    PAVING_LAYER(true, 350),
    PAVED_LAYER(false, 351),
    OPTIMIZING_LAYER(true, 400),
    OPTIMIZED_LAYER(false, 401),
    OPTIMIZING_MESH(true, 500),
    OPTIMIZED_MESH(false, 501),
    GLUING(true, 600),
    GLUED(false, 601),
    SCHEDULING(true, 700),
    SCHEDULED(false, 701),
    OPENED(false, 1),
    OPEN_FAILED(false, 2),
    SAVED(false, 50),
    SAVE_FAILED(false, 51),
    EXPORTING(true, 900),
    EXPORTED(false, 901);

    private final boolean working;
    /**
     * Indicate the state in workflow
     */
    private final int code;

    MeshEvents(boolean working, int code) {
        this.working = working;
        this.code = code;
    }

    public boolean isWorking() {
        return working;
    }

    public int getCode() {
        return code;
    }
}

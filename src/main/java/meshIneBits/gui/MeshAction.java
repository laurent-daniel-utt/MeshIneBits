/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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

package meshIneBits.gui;

import javax.swing.*;

public abstract class MeshAction extends AbstractAction {
    public final String uuid;
    public final KeyStroke acceleratorKey;
    public final String combo;

    MeshAction(String uuid,
               String name,
               String iconname,
               String description,
               String acceleratorKey) {
        super(name, IconLoader.get(iconname));
        putValue(SHORT_DESCRIPTION, description);
        this.acceleratorKey = KeyStroke.getKeyStroke(acceleratorKey);
        putValue(ACCELERATOR_KEY, this.acceleratorKey);
        this.uuid = uuid;
        combo = translate(acceleratorKey);
    }

    private String translate(String acceleratorKey) {
        return acceleratorKey
                .trim()
                .replace(" ", " + ")
                .replace("control", "Ctrl")
                .replace("shift", "Shift")
                .replace("alt", "Alt");
    }

    public String getToolTipText() {
        return this.getValue(NAME).toString() +
                (!combo.equals("") ? " (" + combo + ")" : "");
    }
}

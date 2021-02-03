/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas..
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

package meshIneBits.gui.utilities;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class IconLoader {
    private static int defaultScaleAlgorithm = Image.SCALE_AREA_AVERAGING;
    private static int defaultWidth = 36;
    private static int defaultHeight = 36;

    /**
     * Get icon with default width and height
     *
     * @param filename relative path
     * @return scaled icon
     */
    public static ImageIcon get(String filename) {
        return get(filename, defaultWidth, defaultHeight);
    }

    /**
     * Get icon from file
     *
     * @param iconName relative path
     * @param width    0 to get original image
     * @param height   0 to get original image
     * @return scaled icon
     */
    public static ImageIcon get(String iconName, int width, int height) {
        ImageIcon imageIcon = new ImageIcon(
                Objects.requireNonNull(
                        IconLoader.class
                                .getClassLoader()
                                .getResource("resources/" + iconName)));
        if (width == 0 || height == 0) return imageIcon;
        // Fit image into cadre
        int originalWidth = imageIcon.getIconWidth(),
                originalHeight = imageIcon.getIconHeight(),
                newWidth = originalWidth,
                newHeight = originalHeight;
        if (originalWidth > width) {
            newWidth = width;
            newHeight = newWidth * originalHeight / originalWidth;
        }
        if (newHeight > height) {
            newHeight = height;
            newWidth = originalWidth * height / originalHeight;
        }
        // Scale
        Image image = imageIcon.getImage();
        return new ImageIcon(image.getScaledInstance(newWidth, newHeight, defaultScaleAlgorithm));
    }
}
/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
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

package meshIneBits.config;

import java.util.Vector;

import meshIneBits.patterntemplates.PatternTemplate;
import meshIneBits.patterntemplates.UnitSquarePattern;
import meshIneBits.patterntemplates.ClassicBrickPattern;
import meshIneBits.patterntemplates.DiagonalHerringbonePattern;
import meshIneBits.patterntemplates.ImprovedBrickPattern;
import meshIneBits.patterntemplates.EconomicPattern;

/**
 * The CraftConfig class contains the configurable
 * settings for the slicer. Reflection and annotations
 * are used to make it easy to generate the configuration
 * dialog.
 * NOTE: Do not auto format this file. Manual format keeps it readable!
 */
public class CraftConfig {
    public static final String VERSION = "Dev-Prerelease";

    // Slicer options

    /**
     * @deprecated No more multiple slices per layer
     */
    @Setting(title = "Slice height",
            description = "Height of each sliced layer (in mm)",
            minValue = 1.0, maxValue = 1000.0
    )
    public static double sliceHeight = 2;

    @Setting(title = "First slice height",
            description = "Starting height of the first slice in the model (in %). 50% is the default.",
            minValue = 1.0, maxValue = 99.0
    )
    public static double firstSliceHeightPercent = 50;

    @Setting(title = "Minimal line segment cosine value",
            description = "If the cosine of the line angle difference is higher then this value then 2 lines are joined into 1.\nSpeeding up the slicing, and creating less gcode commands. Lower values makes circles less round,\nfor a faster slicing and less GCode. A value of 1.0 leaves every line intact.",
            minValue = 0.95, maxValue = 1.0
    )
    public static double joinMinCosAngle = 0.995;

    // Bits options

    @Setting(title = "Bit thickness",
            description = "Thickness of the bits (in mm)",
            minValue = 1.0, maxValue = 1000.0
    )
    public static double bitThickness = 8.0;

    @Setting(title = "Bit width",
            description = "Width of the bits (in mm)",
            minValue = 1.0, maxValue = 1000.0
    )
    public static double bitWidth = 24.0;

    @Setting(title = "Bit length",
            description = "Length of the bits (in mm)",
            minValue = 1.0, maxValue = 1000.0
    )
    public static double bitLength = 120.0;

    // Pattern choices

    /**
     * @deprecated As of release of 0.2, replaced by {@link #templateChoice}
     */
    @Setting(title = "Pattern number",
            description = "Pattern template you want to apply",
            minValue = 1, maxValue = 4
    )
    public static int patternNumber = 2;
    /**
     * The provided templates
     */
    public static PatternTemplate[] templatesPreloaded = {
            new ClassicBrickPattern(),
            new DiagonalHerringbonePattern(),
            new ImprovedBrickPattern(),
            new EconomicPattern(),
            new UnitSquarePattern()
    };
    /**
     * Includes the provided templates and added lately ones
     */
    public static Vector<PatternTemplate> templatesLoaded;
    /**
     * The chosen pattern
     */
    public static PatternTemplate templateChoice = templatesPreloaded[0];

    // Deprecated pattern parameters

    /**
     * @deprecated
     */
    /*
     * Should be replaced in the next version by 3D-view.
     */
    @Setting(title = "Overall rotation (°)",
            description = "Rotation you want to apply on the whole object (in degrees)",
            minValue = 0.0, maxValue = 360.0
    )
    public static double rotation = 0.0;

    /**
     * @deprecated
     */
    /*
     * Should be replaced in the next version by 3D-view.
     */
    @Setting(title = "Overall X offSet (mm)",
            description = "Offset you want to apply on the whole object in the X direction (mm)",
            minValue = -60.0, maxValue = 90.0
    )
    public static double xOffset = 0.0;

    /**
     * @deprecated
     */
    /*
     * Should be replaced in the next version by 3D-view.
     */
    @Setting(title = "Overall Y offSet (mm)",
            description = "Offset you want to apply on the whole object in the Y direction (mm)",
            minValue = -60.0, maxValue = 90.0
    )
    public static double yOffset = 0.0;

    // Layer computation

    @Setting(title = "Space between layers",
            description = "The vertical gap between each layers (in mm)",
            minValue = 1.0, maxValue = 100.0
    )
    public static double layersOffset = 1;

    @Setting(title = "Min % of slices in a bit3D",
            description = "Minimum percentage of number slices in a bit3D (%)",
            minValue = 1.0, maxValue = 100.0
    )
    public static double minPercentageOfSlices = 50.0;

    @Setting(title = "Default slice to select",
            description = "Default slice to select (in % of bit height) in a bit 3D to become the bit2D to extrude. 0% means the lower slice, 100% the highest.",
            minValue = 0.0, maxValue = 100.0
    )
    public static double defaultSliceToSelect = 50.0;

    @Setting(title = "Suction cup diameter",
            description = "Diameter of the suction cup which lifts the bits (in mm)",
            minValue = 1.0, maxValue = 100.0
    )
    public static double suckerDiameter = 10.0;

    // Other parameters
    /**
     * To know the file in process
     */
    @Setting()
    public static String lastSlicedFile = "";

    /**
     * To know the lastly selected pattern configuration file
     */
    @Setting()
    public static String lastPatternConfigFile = "";

    @Setting(title = "Acceptable error",
            description = "Equivalent to 10^(-errorAccepted). Describing the maximum error accepted for accelerating the calculation")
    public static int errorAccepted = 5;

}
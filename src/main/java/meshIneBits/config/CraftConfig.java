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

package meshIneBits.config;

import meshIneBits.patterntemplates.*;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.scheduler.BasicScheduler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The CraftConfig class contains the configurable
 * settings for the slicer. Reflection and annotations
 * are used to make it easy to generate the configuration
 * dialog.
 * NOTE: Do not auto format this file. Manual format keeps it readable!
 */
public class CraftConfig {
    static final String VERSION = "Dev-Prerelease";

    // Slicer options

    @DoubleSetting(
            title = "Space between layers (mm)",
            description = "The vertical gap between each layers",
            step = 0.01,
            minValue = 0.01,
            maxValue = 100.0,
            defaultValue = 0.25
    )
    @SlicerSetting(
            order = 0
    )
    public static double layersOffset = 0.25;

    @DoubleSetting(
            title = "First slice height (% of a bit's thickness)",
            description = "Starting height of the first slice in the model. 50% is the default.",
            minValue = 1.0,
            maxValue = 99.0,
            defaultValue = 50
    )
    @SlicerSetting(
            order = 1
    )
    public static double firstSliceHeightPercent = 50;

    @DoubleSetting(
            title = "Minimal line segment cosine value",
            description = "If the cosine of the line angle difference is higher then this value then 2 lines are joined into 1.\nSpeeding up the slicing, and creating less gcode commands. Lower values makes circles less round,\nfor a faster slicing and less GCode. A value of 1.0 leaves every line intact.",
            minValue = 0.95,
            maxValue = 1.0,
            defaultValue = 0.995
    )
    @SlicerSetting(
            order = 2
    )
    public static double joinMinCosAngle = 0.995;

    // Bits options

    @DoubleSetting(
            title = "Bit thickness (mm)",
            description = "Thickness of the bits",
            minValue = 1.0,
            maxValue = 1000.0,
            defaultValue = 8.0
    )
    @BitSetting(
            order = 0
    )
    public static double bitThickness = 8.0;

    @DoubleSetting(
            title = "Bit width (mm)",
            description = "Width of the bits",
            minValue = 1.0,
            maxValue = 1000.0,
            defaultValue = 24.0
    )
    @BitSetting(
            order = 1
    )
    public static double bitWidth = 23.0;



    @DoubleSetting(
            title = "Bit full length  (mm)",
            description = "Length full of the bits",
            minValue = 1.0,
            maxValue = 1000.0,
            defaultValue = 160.0
    )
    @BitSetting(
            order = 2
    )
    public static double lengthFull = 160.0;


    @DoubleSetting(
            title = "Bit length (mm)",
            description = "Length of the bits",
            minValue = 1.0,
            maxValue = 1000.0,
            defaultValue = 160.0
    )
    @BitSetting(
            order = 2
    )
    public static double lengthNormal = 150.0;

    @DoubleSetting(
            title = "section holding to cut (mm)",
            description = "section holding to cut",
            minValue = 1.0,
            maxValue = 1000.0,
            defaultValue = 10.0
    )
    @BitSetting(
            order = 2
    )
    public static double sectionHoldingToCut = 10.0;
    
    @DoubleSetting(
            title = "incertitude ",
            description = "incertitude of length bits",
            minValue = 0.0001,
            maxValue = 100.0,
            defaultValue = 1.0
    )
    @BitSetting(
            order = 2
    )
    public static double incertitude = 0.001;
    // Pattern choices

    /**
     * The provided templates
     */
    public static PatternTemplate[] templatesPreloaded = {
            new ManualPattern(),
            new ClassicBrickPattern(),
            new DiagonalHerringbonePattern(),
            new ImprovedBrickPattern(),
            new EconomicPattern(),
            new UnitSquarePattern(),
            new AI_Pavement(),
            new GeneticPavement()
    };

    /**
     * @return new instance of each pattern builder
     */
    public static PatternTemplate[] clonePreloadedPatterns() {
        PatternTemplate[] patternsList = new PatternTemplate[CraftConfig.templatesPreloaded.length];
        for (int i = 0; i < CraftConfig.templatesPreloaded.length; i++) {
            try {
                patternsList[i] = CraftConfig.templatesPreloaded[i].getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return patternsList;
    }

    /**
     * The default chosen pattern
     */
    public static PatternTemplate templateChoice = templatesPreloaded[0];

    @DoubleSetting(
            title = "Suction cup diameter (mm)",
            description = "Diameter of the suction cup which lifts the bits",
            minValue = 1.0,
            maxValue = 100.0,
            defaultValue = 10.0
    )
    @AssemblerSetting(
            order = 0
    )
    public static double suckerDiameter = 10.0;

    // Other parameters
    /**
     * Save the directory of last opened {@link meshIneBits.Model}
     */
    @StringSetting(
            title = "Last Model",
            description = "Path of the last opened model"
    )
    public static String lastModel = "";

    /**
     * To know the lastly selected pattern configuration file
     */
    @StringSetting(
            title = "Last Pattern Config",
            description = "Path of the last opened pattern configuration"
    )
    public static String lastPatternConfigFile = "";

    /**
     * Save the directory of last opened {@link meshIneBits.Mesh}
     */
    @StringSetting(
            title = "Last Mesh",
            description = "Path of the last opened Mesh"
    )
    public static String lastMesh = "";

    @IntegerSetting(
            title = "Acceptable error",
            description = "Equivalent to 10^(-errorAccepted). Describing the maximum error accepted for accelerating the calculation",
            defaultValue = 5
    )
    @AssemblerSetting(
            order = 1
    )
    public static int errorAccepted = 5;

    @FloatSetting(
            title = "Printer X (mm)",
            description = "Length of printer",
            minValue = 0,
            defaultValue = 3000
    )
    @PrinterSetting(
            order = 0
    )
    public static float printerX = 3000f;

    @FloatSetting(
            title = "Printer Y (mm)",
            description = "Width of printer",
            minValue = 0,
            defaultValue = 2000
    )
    @PrinterSetting(
            order = 1
    )
    public static float printerY = 2000f;

    @FloatSetting(
            title = "Printer Z (mm)",
            description = "Height of printer",
            minValue = 0,
            defaultValue = 1500
    )
    @PrinterSetting(
            order = 2
    )
    public static float printerZ = 1500f;

    @FloatSetting(
            title = "Working width (mm)",
            minValue = 0,
            defaultValue = 500
    )
    @PrinterSetting(
            order = 0
    )
    public static float workingWidth = 500;


    @FloatSetting(
            title = "Box width",
            minValue = 0,
            defaultValue = 648
    )
    public static double Box = 648;

    @FloatSetting(
            title = "Y empty Space",
            minValue = 0,
            defaultValue = 60
    )
    @PrinterSetting(
            order = 0
    )
    public static float YEmptySpace= 60;

    // Width taken by the lenght of the rakes  in the box
    @FloatSetting(
            title = "Rake box Width",
            minValue = 0,
            defaultValue = 263
    )
    @PrinterSetting(
            order = 0
    )
    public static double rakeBoxWidth = 263;

    // Width of the Rakes' Table
    @FloatSetting(
            title = "Rake Table Width",
            minValue = 0,
            defaultValue = 280
    )
    @PrinterSetting(
            order = 0
    )
    public static double rakeTableWidth = 280;

    @FloatSetting(
            title = "Gluer",
            minValue = 0,
            defaultValue = 52
    )
    @PrinterSetting()
    public static double gluer= 52;

    @FloatSetting(
            title = "Rake Box and gluer",
            minValue= 0,
            defaultValue =  320
    )
    @PrinterSetting()
    public static double rakeBoxGluerWidth= rakeBoxWidth+gluer+5;

    @FloatSetting(
            title = "Start X Printing Space",
            minValue=0,
            defaultValue = 800
    )
    @PrinterSetting()
    public static double xPrintingSpace = rakeBoxGluerWidth+rakeBoxWidth+rakeTableWidth+10;









    @FloatSetting(
            title = "Margin (mm)",
            minValue = 0,
            defaultValue = 45
    )
    @XMLSetting(
            order = 1
    )
    public static float margin = 0;

    @IntegerSetting(
            title = "Number of bits",
            minValue = 1,
            defaultValue = 50
    )
    @XMLSetting(
            order = 2
    )
    public static int nbBits = 50;

    @IntegerSetting(
            title = "Number of bits on a plate",
            minValue = 1,
            defaultValue = 10
    )
    @PrinterSetting()
    public static int nbBitesByPlat = 10;

    @IntegerSetting(
            title = "Number of bits on a Batch",
            minValue = 1,
            defaultValue = 50
    )
    @PrinterSetting()
    public static int nbBitesBatch = 50;

    @DoubleSetting(
            title = "Plate width",
            minValue = 1,
            defaultValue = 50
    )
    @PrinterSetting()
    public static double plateWidth = 50;

    @DoubleSetting(
            title = "First bit x position",
            minValue = 1,
            defaultValue = 1
    )
    @PrinterSetting()
    public static double firstBitX = 1;

    @DoubleSetting(
            title = "Bit inter space",
            minValue = 1,
            defaultValue = 1
    )
    @PrinterSetting()
    public static double plateBitSpace = 1;

    /**
     * The provided templates
     */
    public static AScheduler[] schedulerPreloaded = {
            new BasicScheduler(),
    };

    public static List<Field> settings = new ArrayList<>();

    public static List<Field> printerSettings = new ArrayList<>();

    public static List<Field> bitSettings = new ArrayList<>();

    public static List<Field> slicerSettings = new ArrayList<>();

    public static List<Field> assemblerSettings = new ArrayList<>();

    public static List<Field> xmlSettings = new ArrayList<>();

    public static List<Field> schedulerSettings = new ArrayList<>();

    static {
        Field[] fields = CraftConfig.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getDeclaredAnnotations().length > 0) {
                settings.add(field);
                // Categorize
                if (field.isAnnotationPresent(PrinterSetting.class))
                    printerSettings.add(field);
                if (field.isAnnotationPresent(BitSetting.class))
                    bitSettings.add(field);
                if (field.isAnnotationPresent(SlicerSetting.class))
                    slicerSettings.add(field);
                if (field.isAnnotationPresent(AssemblerSetting.class))
                    assemblerSettings.add(field);
                if (field.isAnnotationPresent(XMLSetting.class))
                    xmlSettings.add(field);
                if (field.isAnnotationPresent(SchedulerSetting.class))
                    schedulerSettings.add(field);
            }
        }
        printerSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(PrinterSetting.class).order()));
        bitSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(BitSetting.class).order()));
        slicerSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(SlicerSetting.class).order()));
        assemblerSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(AssemblerSetting.class).order()));
        xmlSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(XMLSetting.class).order()));
        schedulerSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(SchedulerSetting.class).order()));
    }
}
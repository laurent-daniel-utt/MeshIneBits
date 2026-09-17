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

package meshIneBits.config;

import meshIneBits.patterntemplates.*;
import meshIneBits.scheduler.AScheduler;
import meshIneBits.scheduler.AdvancedScheduler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The CraftConfig class contains the configurable settings for the slicer. Reflection and
 * annotations are used to make it easy to generate the configuration dialog. NOTE: Do not auto
 * format this file. Manual format keeps it readable!
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
  public static double bitWidth = 24.0;

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
  public static double incertitude = 0.002 ;

  // Pattern choices
  /**
   * The provided templates
   */
  public static PatternTemplate[] templatesPreloaded = {
      new BorderPaverPattern(),
//            new GeneticPavement(),
//            new AI_Pavement(),
            new ManualPattern(),
            new ClassicBrickPattern(),
            new DiagonalHerringbonePattern(),
            new ImprovedBrickPattern(),
            new EconomicPattern(),
            new UnitSquarePattern()
    };

  /**
   * @return new instance of each pattern builder
   */
  public static PatternTemplate[] clonePreloadedPatterns() {
    PatternTemplate[] patternsList = new PatternTemplate[CraftConfig.templatesPreloaded.length];
    for (int i = 0; i < CraftConfig.templatesPreloaded.length; i++) {
      try {
        patternsList[i] = CraftConfig.templatesPreloaded[i].getClass()
            .newInstance();
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

  public static int distantPointCircleDiameter = 2;
  public static double distantPointRectangleWidth = 2;
  public static double distantPointRectangleHeight = 3;

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
  @DoubleSetting(
          title = "Precision",
          description = "the parameter that regulates the precision of 2 distant points,the lesser is the param the higher is the precision",
          defaultValue = 3
  )
  @AssemblerSetting(
          order = 2
  )
  public static double precision = 3;

  @DoubleSetting(
      title = "Time for a subbit (s)",
      description = "Average time to place a subbit during construction, used for time estimation",
      minValue = 1.0,
      maxValue = 3600.0,
      defaultValue = 30.0,
      step = 1.0
  )
  @AssemblerSetting(
      order = 3
  )
  public static double timeForASubbit = 30.0;
  //Printer parameter

  @DoubleSetting(
          title = "Lengthmover",
          description = "the moving distance of a bit in length direction",
          defaultValue =80
  )
  @BitSetting(
          order = 3
  )
  public static double lengthmover = 80;

  @DoubleSetting(
          title = "Widthmover",
          description = "the moving distance of a bit in width direction",
          defaultValue =23/2
  )
  @BitSetting(
          order = 4
  )
  public static double widthmover = 23/2;

  @DoubleSetting(
          title = "safeguardSpace",
          description = "In order to keep bits not overlapping or grazing each other",
          minValue = 1,
          defaultValue =3
  )
  @BitSetting(
          order = 5
  )
  public static double safeguardSpaceParam = 3;


  @FloatSetting(
      title = "Printing area X (mm)",
      description = "Length of printing area",
      minValue = 1,
      defaultValue = 3000
  )
  @PrinterSetting(
      order = 0
  )
  public static float printingAreaX = 3000f;

  @FloatSetting(
      title = "Printing area Y (mm)",
      description = "Width of Printing area",
      minValue = 1,
      defaultValue = 2000
  )
  @PrinterSetting(
      order = 0
  )
  public static float printingAreaY = 2000f;

  @FloatSetting(
      title = "Printing area Z (mm)",
      description = "Height of Printing area",
      minValue = 1,
      defaultValue = 1500
  )
  @PrinterSetting(
      order = 0
  )
  public static float printingAreaZ = 1500f;

  @FloatSetting(
          title = "Working width (mm)",
          description = "width of the working area, represents the usable stroke of sub-x axis",
          minValue = 1,
          defaultValue = 300
  )
  @PrinterSetting(
          order = 0
  )
  public static float workingWidth = 228f;

  @FloatSetting(
          title = "Y Printing Space Origin (mm)",
          description = "represents the starting point of the printing area on the Y axis of the machine coordinate system",
          minValue = 0,
          defaultValue = 60
  )
  @PrinterSetting(
          order = 0
  )
  //Manually set at 80 which is the starting point of the printable area on the y-axis of the coordinate system of the machine
  //Correspond to a minimum position on the Y stroke where subbits can be deposited
  //Todo : remove this parameter and express subbit pos in printable area coordinate system
  public static float printingAreaYStrokeOrigin = 80f;

  @DoubleSetting(
      title = "X Printing Space Origin (mm)",
      description = "represents the starting point of the printing area on the X axis of in the machine coordinate system",
      minValue = 600,
      defaultValue = 600
  )
  @PrinterSetting()
  //Previous value : rakeBoxGluerWidth + rakeBoxWidth + rakeTableWidth + 10.0 = 868 (parameters have been removed)
  //Now manually set at 600 which is the starting point of the printable area on the x-axis of the coordinate system of the machine
  //Correspond to a minimum position on the X stroke where subbits can be deposited
  //Todo : remove this parameter and express subbit pos in printable area coordinate system
  public static double printingAreaXStrokeOrigin = 600;

  @IntegerSetting(
      title = "Number of bits on a plate",
      minValue = 1,
      defaultValue = 8
  )
  @PrinterSetting()
  public static int nbBitsByPlat = 8;

  @IntegerSetting(
      title = "Number of bits on a Batch",
      minValue = 1,
      defaultValue = 72
  )
  @PrinterSetting()
  public  static int nbBitsBatch = 72;

  /**
   * The provided templates
   */
  public static AScheduler[] schedulerPreloaded = {
      new AdvancedScheduler(),
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
        if (field.isAnnotationPresent(PrinterSetting.class)) {
          printerSettings.add(field);
        }
        if (field.isAnnotationPresent(BitSetting.class)) {
          bitSettings.add(field);
        }
        if (field.isAnnotationPresent(SlicerSetting.class)) {
          slicerSettings.add(field);
        }
        if (field.isAnnotationPresent(AssemblerSetting.class)) {
          assemblerSettings.add(field);
        }
        if (field.isAnnotationPresent(XMLSetting.class)) {
          xmlSettings.add(field);
        }
        if (field.isAnnotationPresent(SchedulerSetting.class)) {
          schedulerSettings.add(field);
        }
      }
    }
    printerSettings.sort(
        Comparator.comparingInt(o -> o.getAnnotation(PrinterSetting.class)
            .order()));
    bitSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(BitSetting.class)
        .order()));
    slicerSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(SlicerSetting.class)
        .order()));
    assemblerSettings.sort(
        Comparator.comparingInt(o -> o.getAnnotation(AssemblerSetting.class)
            .order()));
    xmlSettings.sort(Comparator.comparingInt(o -> o.getAnnotation(XMLSetting.class)
        .order()));
    schedulerSettings.sort(
        Comparator.comparingInt(o -> o.getAnnotation(SchedulerSetting.class)
            .order()));
  }
}
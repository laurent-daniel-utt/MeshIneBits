package meshIneBits.Config;

import meshIneBits.Config.Setting;

/**
 * The CraftConfig class contains the configurable
 * settings for the slicer. Reflection and annotations
 * are used to make it easy to generate the configuration
 * dialog.
 * NOTE: Do not auto format this file. Manual format keeps it readable!
 */
public class CraftConfig
{
	public static final String VERSION = "Dev-Prerelease";

	@Setting(title = "Slice height (mm)",
			description = "Height of each sliced layer",
			minValue = 0.0, maxValue = 9999.0)
	public static double sliceHeight = 2;

	@Setting(title = "First slice height (%)",
			description = "Starting height of the first slice in the model. 50% is the default.",
			minValue = 0.0, maxValue = 200.0)
	public static double firstSliceHeightPercent = 50;

	@Setting(title = "Minimal line segment cosinus value",
			description = "If the cosinus of the line angle difference is higher then this value then 2 lines are joined into 1.\nSpeeding up the slicing, and creating less gcode commands. Lower values makes circles less round,\nfor a faster slicing and less GCode. A value of 1.0 leaves every line intact.",
			minValue = 0.95, maxValue = 1.0)
	public static double joinMinCosAngle = 0.995;

	@Setting(title = "Bit thickness (mm)",
			description = "Thickness of the bits",
			minValue = 0.0, maxValue = 100.0)
	public static double bitThickness = 8.0;
	
	@Setting(title = "Bit width (mm)",
			description = "Width of the bits",
			minValue = 0.0, maxValue = 1000.0)
	public static double bitWidth = 24.0;
	
	@Setting(title = "Bit length (mm)",
			description = "Length of the bits",
			minValue = 0.0, maxValue = 1000.0)
	public static double bitLength = 120.0;
	
	@Setting(title = "Pattern number",
			description = "Choose which pattern's template you want to apply",
			minValue = 1, maxValue = 2)
	public static int patternNumber = 2;
	
	@Setting(title = "Rotation (°)",
			description = "Choose which rotation you want to apply on the pattern (in degrees)",
			minValue = 0.0, maxValue = 360.0)
	public static double rotation = 45.0;
	
	@Setting(title = "X offSet (mm)",
			description = "Choose which offset you want to apply on the pattern in the X direction (mm)",
			minValue = -60.0, maxValue = 90.0)
	public static double xOffset = 0.0;
	
	@Setting(title = "Y offSet (mm)",
			description = "Choose which offset you want to apply on the pattern in the Y direction (mm)",
			minValue = -60.0, maxValue = 90.0)
	public static double yOffset = 0.0;
	
	@Setting(title = "OffSet between bits (mm)",
			description = "Choose the horizontal gap between each bits (mm)",
			minValue = 0.0, maxValue = 100.0)
	public static double bitsOffset = 3.0;
	
	@Setting(title = "OffSet between layers (mm)",
			description = "Choose the vertical gap between each layers (mm)",
			minValue = 0.0, maxValue = 3.0)
	public static double layersOffset = 0.5;
	
	@Setting(title = "Min % of slices in a bit3D",
			description = "Minimun percentage of slices in a bit3D (%)",
			minValue = 1.0, maxValue = 100.0)
	public static double minPercentageOfSlices = 50.0;
	
	@Setting(title = "Default slice to select (in % of bit height)",
			description = "Default slice to select in a bit 3D to become the bit2D to extrude. 0% means the lower slice, 100% the highest.",
			minValue = 0.0, maxValue = 100.0)
	public static double defaultSliceToSelect = 50.0;
	
	@Setting(title = "Suction cup diameter (mm)",
			description = "Diameter of the suction cup which lifts the bits (mm)",
			minValue = 1.0, maxValue = 100.0)
	public static double suckerDiameter = 10.0;
	
	@Setting(title = "XML export type",
			description = "1 = new XML type, 2 = BrickRobot XML type",
			minValue = 1.0, maxValue = 2.0)
	public static double xmlExportType = 1.0;
	
	@Setting()
	public static String lastSlicedFile = "";	
}
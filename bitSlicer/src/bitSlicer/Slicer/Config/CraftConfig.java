package bitSlicer.Slicer.Config;


import bitSlicer.Slicer.Config.Setting;
import bitSlicer.util.Vector2;;

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
			minValue = 0.0, maxValue = 10.0)
	public static double sliceHeight = 7.8;

	@Setting(title = "First slice height (%)",
			description = "Starting height of the first slice in the model. 50% is the default.",
			minValue = 0, maxValue = 200)
	public static int firstSliceHeightPercent = 50;

	@Setting(title = "Minimal line segment cosinus value",
			description = "If the cosinus of the line angle difference is higher then this value then 2 lines are joined into 1.\nSpeeding up the slicing, and creating less gcode commands. Lower values makes circles less round,\nfor a faster slicing and less GCode. A value of 1.0 leaves every line intact.",
			minValue = 0.95, maxValue = 1.0)
	public static double joinMinCosAngle = 0.995;

	@Setting(title = "Bit thickness (mm)",
			description = "Thickness of the bits",
			minValue = 0.0, maxValue = 100.0)
	public static double bitThickness = 10.0;
	
	@Setting(title = "Bit width (mm)",
			description = "Width of the bits",
			minValue = 0.0, maxValue = 1000.0)
	public static double bitWidth = 25.0;
	
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
			minValue = 0, maxValue = 360)
	public static double rotation = 45;
	
	@Setting(title = "offSet",
			description = "Choose which offset you want to apply on the pattern")
	public static Vector2 offset = new Vector2(0,0);
	
	@Setting()
	public static String lastSlicedFile = "";	
}
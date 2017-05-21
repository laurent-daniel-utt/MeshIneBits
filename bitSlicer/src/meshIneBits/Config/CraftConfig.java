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
	
	// Slicer options

	@Setting(title = "Slice height (mm)",
			description = "Height of each sliced layer.",
			minValue = 1.0, maxValue = 1000.0
			)
	public static double sliceHeight = 2;

	@Setting(title = "First slice height (%)",
			description = "Starting height of the first slice in the model. 50% is the default.",
			minValue = 0.0, maxValue = 200.0
			)
	public static double firstSliceHeightPercent = 50;

	@Setting(title = "Minimal line segment cosinus value",
			description = "If the cosinus of the line angle difference is higher then this value then 2 lines are joined into 1.\nSpeeding up the slicing, and creating less gcode commands. Lower values makes circles less round,\nfor a faster slicing and less GCode. A value of 1.0 leaves every line intact.",
			minValue = 0.95, maxValue = 1.0
			)
	public static double joinMinCosAngle = 0.995;

	// Bits options
	
	@Setting(title = "Bit thickness (mm)",
			description = "Thickness of the bits",
			minValue = 1.0, maxValue = 1000.0
			)
	public static double bitThickness = 8.0;
	
	@Setting(title = "Bit width (mm)",
			description = "Width of the bits",
			minValue = 1.0, maxValue = 1000.0
			)
	public static double bitWidth = 24.0;
	
	@Setting(title = "Bit length (mm)",
			description = "Length of the bits",
			minValue = 1.0, maxValue = 1000.0
			)
	public static double bitLength = 120.0;
	
	// Pattern options
	
	@Setting(title = "Pattern number",
			description = "Pattern template you want to apply",
			minValue = 1, maxValue = 3
			)
	public static int patternNumber = 2;
	
	// Template options
	
	@Setting(title = "Overall rotation (°)",
			description = "Rotation you want to apply on the whole object (in degrees)",
			minValue = 0.0, maxValue = 360.0
			)
	public static double rotation = 0.0;
	
	@Setting(title = "Differential rotation (°)",
			description = "Rotation of a layer in comparison to the previous one (in degrees)",
			minValue = 0.0, maxValue = 360.0
			)
	public static double diffRotation = 90.0;
	
	@Setting(title = "Overall X offSet (mm)",
			description = "Offset you want to apply on the whole object in the X direction (mm)",
			minValue = -60.0, maxValue = 90.0
			)
	public static double xOffset = 0.0;
	
	@Setting(title = "Differential X offset (mm)",
			description = "Offset in the X direction of a layer in comparison to the previous one(mm)",
			minValue = -60.0, maxValue = 90.0 // these values should be dependant on pattern
			)
	public static double diffxOffset = 0.0;
	
	@Setting(title = "Overall Y offSet (mm)",
			description = "Offset you want to apply on the whole object in the Y direction (mm)",
			minValue = -60.0, maxValue = 90.0
			)
	public static double yOffset = 0.0;
	
	@Setting(title = "Differential Y offset (mm)",
			description = "Offset in the Y direction of a layer in comparison to the previous one(mm)",
			minValue = -60.0, maxValue = 90.0 // these values should be dependant on pattern
			)
	public static double diffyOffset = 0.0;
	
	@Setting(title = "Space between bits' widths (mm)",
 			description = "The gap between two consecutive bits' widths (mm)",
			minValue = 1.0, maxValue = 100.0
			)
	public static double bitsWidthSpace = 5.0;
	
	@Setting(title = "Space between bits' lengths (mm)",
			description = "The gap between two consecutive bits' lengths (mm)",
			minValue = 1.0, maxValue = 100.0
			)
	public static double bitsLengthSpace = 1.0;
	
//	@Setting(title = "OffSet between bits (mm)",
//			description = "Choose the horizontal gap between each bits (mm)",
//			minValue = 1.0, maxValue = 100.0,
//			)
//	public static double bitsOffset = 3.0;
	
	@Setting(title = "Space between layers (mm)",
			description = "The vertical gap between each layers (mm)",
			minValue = 1.0, maxValue = 100.0
			)
	public static double layersOffset = 1;
	
	
	
	// Compute 
	
	@Setting(title = "Min % of slices in a bit3D",
			description = "Minimun percentage of slices in a bit3D (%)",
			minValue = 1.0, maxValue = 100.0
			)
	public static double minPercentageOfSlices = 50.0;
	
	@Setting(title = "Default slice to select (in % of bit height)",
			description = "Default slice to select in a bit 3D to become the bit2D to extrude. 0% means the lower slice, 100% the highest.",
			minValue = 0.0, maxValue = 100.0
			)
	public static double defaultSliceToSelect = 50.0;
	
	@Setting(title = "Suction cup diameter (mm)",
			description = "Diameter of the suction cup which lifts the bits (mm)",
			minValue = 1.0, maxValue = 100.0
			)
	public static double suckerDiameter = 10.0;
	
	@Setting()
	public static String lastSlicedFile = "";	
	
	@Setting(title = "Acceptable error",
			description = "Equivalent to 10^(-errorAccepted). Describing the maximum error accepted for accelerating the calculation")
	public static int errorAccepted = 5;
	
	/**
	 * Only in {@link meshIneBits.PatternTemplates.PatternTemplate4}
	 */
	@Setting(title = "Limit for optimising 1st line's height",
			description = "The maximum number of essays to change 1st line's height by dichotomic algorithm")
	public static int limitForCalculatingHeightOffsets = 3;
	
	/**
	 * Only in {@link meshIneBits.PatternTemplates.PatternTemplate4}
	 */
	@Setting(title = "Limit for optimising layer rotation",
			description = "The maximum number of essays to change the rotation of layer by dichotomic algorithm")
	public static int limitForChangingDiffLayerRotation = 2;
	
	/**
	 * Only in {@link meshIneBits.PatternTemplates.PatternTemplate4}
	 */
	@Setting(title = "Limit for optimising 1st bit's length",
			description = "The maximum number of essays to change the 1st bit's length per line by dichotomic algorithm")
	public static int litmitForCalculatingLengthOffsets = 2;
	
}
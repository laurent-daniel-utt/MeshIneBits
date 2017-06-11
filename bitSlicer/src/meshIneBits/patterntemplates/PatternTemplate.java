package meshIneBits.patterntemplates;

import meshIneBits.GeneratedPart;
import meshIneBits.Layer;
import meshIneBits.Pattern;
import meshIneBits.config.PatternConfig;
import meshIneBits.util.Vector2;

/**
 * This is a factory paving the layers. Use {@link #createPattern(int)} to
 * pave.
 */
public abstract class PatternTemplate {

	/**
	 * Contains all customizable special parameters of the pattern template
	 */
	protected PatternConfig config = new PatternConfig();

	/**
	 * Prepare own parameters (use {@link PatternTemplate#initiateConfig()}
	 * 
	 */
	public PatternTemplate() {
		super();
		initiateConfig();
	}

	/**
	 * Prepare own parameters from a externally loaded configuration.
	 * 
	 * @param patternConfig
	 *            all given parameters will be filtered again before applying.
	 *            Those which do not appear in the default configuration of
	 *            template will be discarded.
	 */
	public PatternTemplate(PatternConfig patternConfig) {
		super();
		initiateConfig();
		// Setup currentValue fields
		for (String configName : config.keySet()) {
			if (patternConfig.containsKey(configName)) {
				Object currentValue = config.get(configName).getCurrentValue(),
						newValue = patternConfig.get(configName).getCurrentValue();
				if (currentValue.getClass().isAssignableFrom(newValue.getClass())) {
					config.get(configName).setCurrentValue(newValue);
				}
			}
		}
	}

	/**
	 * Initialize the specialized configuration if no predefined configuration
	 * found.
	 */
	public abstract void initiateConfig();

	/**
	 * Calculate private parameters, after slicing and before generating bits.
	 * 
	 * @param generatedPart
	 *            the current part in workplace
	 * @return <tt>false</tt> if the preparation fails
	 */
	public abstract boolean ready(GeneratedPart generatedPart);

	/**
	 * Construct the layer based on this pattern
	 * 
	 * @param layerNumber
	 *            an integer not negative
	 * @return a bit-filled pattern
	 */
	public abstract Pattern createPattern(int layerNumber);

	/**
	 * To be called in auto-optimization. Or can be used to perform on a
	 * specific layer
	 * 
	 * @param actualState
	 *            the whole actual bits' placement in layer
	 * @return the number of bits not solved yet
	 */
	public abstract int optimize(Layer actualState);

	/**
	 * Move the bit by the minimum distance automatically calculated.
	 * 
	 * The distance depends on the chosen pattern. Realize the move on the input
	 * pattern.
	 * 
	 * @param actualState
	 *            the actual state of layer which is paved by this pattern
	 *            template
	 * @param bitKey
	 *            the transformed origin of bit
	 * @param localDirection
	 *            the direction in the coordinate system of bit
	 * @return the new origin of the moved bit
	 */
	public abstract Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection);

	/**
	 * Similar to {@link #moveBit(Pattern, Vector2, Vector2)} except the
	 * distance is free to decide.
	 * 
	 * @param actualState
	 *            the actual state of layer which is paved by this pattern
	 *            template
	 * @param bitKey
	 *            the transformed origin of bit
	 * @param localDirection
	 *            the direction in the coordinate system of bit
	 * @param distance
	 *            an positive real number (in double precision)
	 * @return the new origin of the moved bit
	 * @see {@link PatternTemplate#moveBit(Pattern, Vector2, Vector2)}
	 */
	public abstract Vector2 moveBit(Pattern actualState, Vector2 bitKey, Vector2 localDirection, double distance);

	/**
	 * @return the full name of icon representation the template
	 */
	public String getIconName() {
		return "default-template-icon.png";
	}

	/**
	 * @return the common name of the template
	 */
	public String getCommonName() {
		return "An Unknown Template";
	}

	/**
	 * @return a block of text of description about this template
	 */
	public String getDescription() {
		return "A predefined template.";
	}

	/**
	 * @return a block of text about how to use this template
	 */
	public String getHowToUse() {
		return "Customize parameters to reach the desired pattern.";
	}

	public PatternConfig getPatternConfig() {
		return config;
	}
}

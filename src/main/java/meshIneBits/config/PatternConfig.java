/**
 * 
 */
package meshIneBits.config;

import java.util.HashMap;

import meshIneBits.config.patternParameter.PatternParameter;

/**
 * This class is to declare all parameters which are customizable by users. All
 * for the sake of saving configurations.
 * 
 * @author NHATHAN
 *
 */
public class PatternConfig extends HashMap<String, PatternParameter> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2295740737265238707L;

	public void add(PatternParameter paramConf) {
		this.put(paramConf.getCodename(), paramConf);
	}
}

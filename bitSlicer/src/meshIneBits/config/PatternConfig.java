/**
 * 
 */
package meshIneBits.config;

import java.util.HashMap;

/**
 * This class is to declare all parameters which are customizable by users. All
 * for the sake of saving configurations.
 * 
 * @author NHATHAN
 *
 */
public class PatternConfig extends HashMap<String, PatternParameterConfig> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2295740737265238707L;

	public void add(PatternParameterConfig paramConf) {
		this.put(paramConf.uniqueName, paramConf);
	}
}

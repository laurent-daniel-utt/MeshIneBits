/**
 * 
 */
package meshIneBits.config.patternParameter;

import java.io.Serializable;

/**
 * Describe a parameter of pattern <br>
 * 
 * @author NHATHAN
 *
 */
public interface PatternParameter extends Serializable {
	/**
	 * 
	 */
	static final long serialVersionUID = -1032848466522000185L;

	/**
	 * Type of current value depends on sub class
	 * 
	 * @return
	 */
	public Object getCurrentValue();

	/**
	 * This method will be use by interfaces on each change event
	 * 
	 * @param currentValue
	 *            will be filtered before affecting, in the same way of
	 *            <tt>defaultValue</tt>
	 */
	public void setCurrentValue(Object currentValue);

	/**
	 * @return name of parameters. Should be different among parameters.
	 */
	public String getCodename();

	/**
	 * @return human-readable name
	 */
	public String getTitle();

	/**
	 * @return what it is and how to use
	 */
	public String getDescription();

	/**
	 * @return encoded string
	 */
	public String toString();
}

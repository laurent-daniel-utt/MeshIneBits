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
public abstract class PatternParameter implements Serializable {

	/**
	 * 
	 */
	static final long serialVersionUID = -1032848466522000185L;
	protected String title;
	protected String codename;
	protected String description;

	/**
	 * Type of current value depends on sub class
	 * 
	 * @return
	 */
	public abstract Object getCurrentValue();

	/**
	 * This method will be use by interfaces on each change event
	 * 
	 * @param newValue
	 *            will be filtered before affecting, in the same way of
	 *            <tt>defaultValue</tt>
	 */
	public abstract void setCurrentValue(Object newValue);

	/**
	 * @return name of parameters. Should be different among parameters.
	 */
	public String getCodename() {
		return codename;
	}

	/**
	 * @return human-readable name
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return what it is and how to use
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return encoded string
	 */
	public abstract String toString();
}

/**
 * 
 */
package meshIneBits.config.patternParameter;

import meshIneBits.gui.utilities.patternParamRenderer.Checkbox;
import meshIneBits.gui.utilities.patternParamRenderer.Renderer;

/**
 * @author Quoc Nhat Han TRAN
 *
 */
public class BooleanParam extends PatternParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2259784876889236537L;
	private boolean currentValue;
	private boolean defaultValue;

	/**
	 * @param name
	 *            Should be unique among parameters of a pattern
	 * @param title
	 * @param description
	 * @param defaultValue
	 *            which value this parameter should hold at first or when meet a
	 *            wrong setting
	 * @param currentValue
	 *            current state of parameter
	 */
	public BooleanParam(String name, String title, String description, boolean defaultValue, boolean currentValue) {
		this.title = title;
		this.codename = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.currentValue = currentValue;
	}

	@Override
	public Boolean getCurrentValue() {
		return currentValue;
	}

	@Override
	public void setCurrentValue(Object newValue) {
		if (newValue instanceof Boolean)
			this.currentValue = (boolean) newValue;
		else
			this.currentValue = defaultValue;
	}

	@Override
	public String toString() {
		return "Boolean[name=" + codename + ", title=" + title + ", description=" + description + ", defaultValue="
				+ defaultValue + ", currentValue=" + currentValue + "]";
	}

	@Override
	public Renderer getRenderer() {
		return new Checkbox(this);
	}
}

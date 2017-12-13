/**
 * 
 */
package meshIneBits.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Describe a parameter, including uniqueName, title, description (further
 * explain of use), default value (this currently accepts {@link Double},
 * {@link List} of {@link Double}) and step (minimal precision). <br>
 * <br>
 * These attributes <tt>minValue</tt> and <tt>maxValue</tt> define a range of
 * selection.
 * 
 * 
 * @author NHATHAN
 *
 */
public class PatternParameterConfig implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1032848466522000185L;
	final public String uniqueName;
	final public String title;
	final public String description;
	final public Double minValue;
	final public Double maxValue;
	final public Object defaultValue;
	final public Double step;
	private Object currentValue;

	/**
	 * 
	 * @return Either a {@link Double} or a {@link List} of {@link Double} (all
	 *         filtered)
	 */
	public Object getCurrentValue() {
		return currentValue;
	}

	/**
	 * @param currentValue
	 *            will be filtered before affecting, in the same way of
	 *            <tt>defaultValue</tt>
	 */
	public void setCurrentValue(Object currentValue) {
		if (currentValue instanceof Double) {
			// In case of a number
			currentValue = filter((Double) currentValue);
		} else if (defaultValue instanceof List<?>) {
			// In case of a list
			currentValue = filter((List<?>) currentValue);
		} else {
			currentValue = new Vector<Double>();
		}
		this.currentValue = currentValue;
	}

	/**
	 * This will validate each input. If one does not satisfy, it will bring the
	 * default value. Be careful of {@link List}. Should use a real provided class
	 * implementing {@link List}, not by wrapping.
	 * 
	 * @param uniqueName
	 *            no constraint, but should be unique. Otherwise, it will crash
	 *            preceding parameter with same <tt>uniqueName</tt>
	 * @param title
	 *            no constraint
	 * @param description
	 *            no constraint
	 * @param minValue
	 *            defines minimum of range of selection. Default =
	 *            {@link Double#MIN_VALUE}
	 * @param maxValue
	 *            defines maximum of range of selection. Default =
	 *            {@link Double#MAX_VALUE}
	 * @param defaultValue
	 *            should be either a {@link Double} or a {@link List} of
	 *            {@link Double}. <br>
	 *            In case of a number: Default = 0.0 or <tt>minValue</tt> if
	 *            inferior or <tt>maxValue</tt> if superior. <br>
	 *            In case of a list: an empty {@link Vector} of {@link Double}
	 *            or a {@link List} of filtered {@link Double}
	 * @param step
	 *            must not be 0.0 or <tt>null</tt> or negative. Default = 1.0
	 */
	public PatternParameterConfig(String uniqueName, String title, String description, Double minValue, Double maxValue,
			Object defaultValue, Double step) {
		this.uniqueName = uniqueName;
		this.title = title;
		this.description = description;
		// Check inferior border
		if (Double.isInfinite(minValue) || Double.isNaN(minValue)) {
			minValue = Double.MIN_VALUE;
		}
		// Check superior border
		if (Double.isInfinite(maxValue) || Double.isNaN(maxValue)) {
			maxValue = Double.MAX_VALUE;
		}
		// Check logic
		if (minValue > maxValue) {
			// Swap
			double c = minValue;
			minValue = maxValue;
			maxValue = c;
		}
		this.minValue = minValue;
		this.maxValue = maxValue;
		// Validate step
		if (Double.isInfinite(step) || Double.isNaN(step) || step == null || step == 0) {
			step = 1.0;
		}
		if (step < 0) {
			step = -step;
		}
		this.step = step;
		// Check default value
		if (defaultValue instanceof Double) {
			// In case of a number
			defaultValue = filter((Double) defaultValue);
		} else if (defaultValue instanceof List<?>) {
			// In case of a list
			defaultValue = filter((List<?>) defaultValue);
		} else {
			defaultValue = new Vector<Double>();
		}
		this.defaultValue = defaultValue;
		// Setup temporary currentValue
		this.currentValue = this.defaultValue;
	}

	private double filter(Double d) {
		// Check extreme
		if (Double.isInfinite(d) || Double.isNaN(d) || d == null) {
			return 0.0;
		}
		// Check in range
		if (d.doubleValue() < this.minValue) {
			return this.minValue;
		}
		if (d.doubleValue() > this.maxValue) {
			return this.maxValue;
		}
		// Round up
		double sgn = Math.signum(d);
		if (sgn == 0) {
			return d;
		}
		if (sgn < 0) {
			d = d * sgn;
		}
		double residu = d - ((int) (d / step)) * step;
		if (residu < 0.5 * step) {
			d = (d - residu) * sgn;
		} else {
			d = (d - residu + step) * sgn;
		}
		return d;
	}

	private List<Double> filter(List<?> list) {
		List<?> l = list.subList(0, list.size());
		// Check each element
		// Picking all Double elements
		List<Double> nl = new ArrayList<Double>();
		for (Object object : l) {
			if (object instanceof Double){
				nl.add((Double) object);
			}
		}
		// Filter values
		for (int i = 0; i < nl.size(); i++) {
			nl.set(i, filter(nl.get(i)));
		}
		return nl;
	}

	@Override
	public String toString() {
		return "PatternParameterConfig [uniqueName=" + uniqueName + ", title=" + title + ", description=" + description
				+ ", minValue=" + minValue + ", maxValue=" + maxValue + ", defaultValue=" + defaultValue + ", step="
				+ step + ", currentValue=" + currentValue + "]";
	}
}

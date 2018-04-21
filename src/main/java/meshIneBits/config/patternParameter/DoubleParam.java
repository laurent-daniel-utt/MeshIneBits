/**
 * 
 */
package meshIneBits.config.patternParameter;

import java.util.List;

import meshIneBits.gui.utilities.LabeledSpinner;

/**
 * To be used in couple with {@link LabeledSpinner}
 * 
 * @author Quoc Nhat Han TRAN
 *
 */
public class DoubleParam implements PatternParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9178756965959306841L;
	private Double minValue;
	private Double maxValue;
	private Double defaultValue;
	private Double step;
	private Double currentValue;
	private final String TITLE;
	private final String CODENAME;
	private final String DESCRIPTION;

	/**
	 * This will validate each input. If one does not satisfy, it will bring the
	 * default value. Be careful of {@link List}. Should use a real provided class
	 * implementing {@link List}, not by wrapping.
	 * 
	 * @param name
	 *            Should be unique among parameters of a pattern
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
	 *            Round up by {@link #step}
	 *            <ul>
	 *            <li><tt>0.0</tt> if {@link Double#isInfinite()} or
	 *            {@link Double#isNaN()} or <tt>null</tt></li>
	 *            <li>{@link #minValue} if lower than {@link #minValue}</li>
	 *            <li>{@link #maxValue} if higher than {@link #maxValue}</li>
	 *            </ul>
	 * @param step
	 *            if {@link Double#isInfinite()} or {@link Double#isNaN()} or
	 *            <tt>null</tt> or <tt>0</tt>, {@link #step} will be set to
	 *            <tt>1.0</tt><br>
	 *            if negative, {@link #step} will be the opposite value
	 */
	public DoubleParam(String name, String title, String description, Double minValue, Double maxValue,
			Double defaultValue, Double step) {
		this.CODENAME = name;
		this.TITLE = title;
		this.DESCRIPTION = description;
		setMin(minValue);
		setMax(maxValue);
		checkMinMax();
		setStep(step);
		setDefault(defaultValue);
		// Setup temporary currentValue
		this.currentValue = this.defaultValue;
	}

	/**
	 * @param d
	 * @see #filter(Double)
	 */
	private void setDefault(Double d) {
		this.defaultValue = filter(d);
	}

	/**
	 * @param s
	 *            if {@link Double#isInfinite()} or {@link Double#isNaN()} or
	 *            <tt>null</tt> or <tt>0</tt>, {@link #step} will be set to
	 *            <tt>1.0</tt><br>
	 *            if negative, {@link #step} will be the opposite value
	 */
	private void setStep(Double s) {
		if (Double.isInfinite(s) || Double.isNaN(s) || s == null || s == 0) {
			this.step = 1.0;
		} else {
			this.step = (s < 0 ? -s : s);
		}
	}

	/**
	 * @param minValue
	 *            if {@link Double#isInfinite()} or {@link Double#isNaN()} holds
	 *            true, the {@link #minValue} will be set to
	 *            {@link Double#MIN_VALUE}
	 */
	private void setMin(Double minValue) {
		if (Double.isInfinite(minValue) || Double.isNaN(minValue)) {
			this.minValue = Double.MIN_VALUE;
			return;
		} else
			this.minValue = minValue;
	}

	/**
	 * @param maxValue
	 *            if {@link Double#isInfinite()} or {@link Double#isNaN()} holds
	 *            true, the {@link #maxValue} will be set to
	 *            {@link Double#MAX_VALUE}
	 */
	private void setMax(Double maxValue) {
		if (Double.isInfinite(maxValue) || Double.isNaN(maxValue)) {
			this.maxValue = Double.MAX_VALUE;
		} else
			this.maxValue = maxValue;
	}

	/**
	 * Swap min and max if min > max
	 */
	private void checkMinMax() {
		if (minValue > maxValue) {
			double c = minValue;
			minValue = maxValue;
			maxValue = c;
		}
	}

	/**
	 * Filter an input value after setting up {@link #minValue}, {@link #maxValue},
	 * {@link #step}
	 * 
	 * @param d
	 * @return Round up by {@link #step}
	 *         <ul>
	 *         <li><tt>0.0</tt> if {@link Double#isInfinite()} or
	 *         {@link Double#isNaN()} or <tt>null</tt></li>
	 *         <li>{@link #minValue} if lower than {@link #minValue}</li>
	 *         <li>{@link #maxValue} if higher than {@link #maxValue}</li>
	 *         </ul>
	 *
	 */
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

	/**
	 * @param newCurrentValue
	 *            will be filtered before affecting, else <tt>defaultValue</tt>
	 */
	public void setCurrentValue(Object newCurrentValue) {
		if (!(newCurrentValue instanceof Double))
			this.currentValue = this.defaultValue;
		else
			this.currentValue = filter((Double) newCurrentValue);
	}

	@Override
	public String toString() {
		return "PatternParam [name=" + CODENAME + ", title=" + TITLE + ", description=" + DESCRIPTION + ", minValue="
				+ minValue + ", maxValue=" + maxValue + ", defaultValue=" + defaultValue + ", step=" + step
				+ ", currentValue=" + currentValue + "]";
	}

	@Override
	public Double getCurrentValue() {
		return currentValue;
	}

	@Override
	public String getCodename() {
		return CODENAME;
	}

	@Override
	public String getTitle() {
		return TITLE;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	public Double getMinValue() {
		return minValue;
	}

	public Double getMaxValue() {
		return maxValue;
	}

	public Double getStep() {
		return step;
	}
}

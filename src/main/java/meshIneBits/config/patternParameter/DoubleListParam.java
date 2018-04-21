/**
 * 
 */
package meshIneBits.config.patternParameter;

import java.util.ArrayList;
import java.util.List;

import meshIneBits.gui.utilities.LabeledListReceiver;

/**
 * To be used in couple with {@link LabeledListReceiver}
 * 
 * @author Quoc Nhat Han TRAN
 *
 */
public class DoubleListParam implements PatternParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2432538059762679637L;
	private Double minValue;
	private Double maxValue;
	private Double step;
	private List<Double> defaultValue;
	private List<Double> currentValue;
	private final String TITLE;
	private final String CODENAME;
	private final String DESCRIPTION;

	/**
	 * <tt>minValue<tt>, <tt>maxValue</tt> and <tt>step</tt> will be applied to each
	 * element of input list
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
	 *            predefined array of values
	 * @param step
	 *            if {@link Double#isInfinite()} or {@link Double#isNaN()} or
	 *            <tt>null</tt> or <tt>0</tt>, {@link #step} will be set to
	 *            <tt>1.0</tt><br>
	 *            if negative, {@link #step} will be the opposite value
	 */
	public DoubleListParam(String name, String title, String description, Double minValue, Double maxValue,
			List<Double> defaultValue, Double step) {
		this.CODENAME = name;
		this.TITLE = title;
		this.DESCRIPTION = description;
		setMin(minValue);
		setMax(maxValue);
		checkMinMax();
		setStep(step);
		setDefault(defaultValue);
		// Setup temporary currentValue
		this.currentValue = new ArrayList<Double>();
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
		}
		if (s < 0) {
			this.step = -s;
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
	 * @param l
	 *            contains default elements. Each element will be
	 *            {@link #filter(Double)}
	 */
	private void setDefault(List<Double> l) {
		this.defaultValue = new ArrayList<Double>();
		for (Double d : l) {
			this.defaultValue.add(filter(d));
		}
	}

	@Override
	public List<Double> getCurrentValue() {
		return currentValue;
	}

	/**
	 * Restore to the default value if the list does not contain valid
	 * {@link Double}
	 * 
	 * @param newValue
	 *            only accept {@link List} of {@link Double}s. Else, the current
	 *            value will be erased. Each element will be round up by
	 *            {@link #step} and set to:
	 *            <ul>
	 *            <li><tt>0.0</tt> if {@link Double#isInfinite()} or
	 *            {@link Double#isNaN()} or <tt>null</tt></li>
	 *            <li>{@link #minValue} if lower than {@link #minValue}</li>
	 *            <li>{@link #maxValue} if higher than {@link #maxValue}</li>
	 *            </ul>
	 */
	@Override
	public void setCurrentValue(Object newValue) {
		if (newValue instanceof List<?>) {
			this.currentValue.clear();
			for (Object object : (List<?>) newValue) {
				if (object instanceof Double) {
					this.currentValue.add(filter((Double) object));
				}
			}
			if (this.currentValue.isEmpty())
				this.currentValue.addAll(defaultValue);
		}
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

}

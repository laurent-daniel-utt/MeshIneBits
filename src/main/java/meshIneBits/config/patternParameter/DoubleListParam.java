/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

/**
 * 
 */
package meshIneBits.config.patternParameter;

import java.util.ArrayList;
import java.util.List;

import meshIneBits.gui.utilities.patternParamRenderer.LabeledListReceiver;
import meshIneBits.gui.utilities.patternParamRenderer.Renderer;

/**
 * To be used in couple with {@link LabeledListReceiver}
 * 
 * @author Quoc Nhat Han TRAN
 *
 */
public class DoubleListParam extends PatternParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2432538059762679637L;
	private Double minValue;
	private Double maxValue;
	private Double step;
	private List<Double> defaultValue;
	private List<Double> currentValue;

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
		this.codename = name;
		this.title = title;
		this.description = description;
		setMin(minValue);
		setMax(maxValue);
		checkMinMax();
		setStep(step);
		setDefault(defaultValue);
		// Setup temporary currentValue
		this.currentValue = this.defaultValue;
	}

	/**
	 * @param s
	 *            if {@link Double#isInfinite()} or {@link Double#isNaN()} or
	 *            <tt>null</tt> or <tt>0</tt>, {@link #step} will be set to
	 *            <tt>1.0</tt><br>
	 *            if negative, {@link #step} will be the opposite value
	 */
	private void setStep(Double s) {
		if (Double.isInfinite(s) || Double.isNaN(s) || s == 0) {
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
		if (Double.isInfinite(d) || Double.isNaN(d)) {
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
		return codename;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "DoubleList[name=" + codename + ", title=" + title + ", description=" + description + ", minValue="
				+ minValue + ", maxValue=" + maxValue + ", defaultValue=" + defaultValue + ", step=" + step
				+ ", currentValue=" + currentValue + "]";
	}

	@Override
	public Renderer getRenderer() {
		return new LabeledListReceiver(this);
	}

}

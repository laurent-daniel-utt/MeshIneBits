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

import java.util.List;

import meshIneBits.gui.utilities.patternParamRenderer.Selector;
import meshIneBits.gui.utilities.patternParamRenderer.Renderer;

/**
 * List of options to choose one
 * 
 * @author Quoc Nhat Han TRAN
 *
 */
public class OptionParam extends PatternParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5514950897837775399L;
	private List<String> values;
	private String defaultValue;
	private String currentValue;

	/**
	 * @param name
	 *            Should be unique among parameters of a pattern
	 * @param title
	 *            no constraint
	 * @param description
	 *            no constraint, but it should describe meaning of each option in
	 *            <tt>values</tt>
	 * @param values
	 *            a list of choices. Must not be empty
	 * @param defaultValue
	 *            initial value of choice. If not present in <tt>values</tt>, it
	 *            will take the first element's value
	 * @throws IllegalArgumentException
	 *             if <tt>values</tt> is empty
	 */
	public OptionParam(String name, String title, String description, List<String> values, String defaultValue)
			throws IllegalArgumentException {
		this.codename = name;
		this.title = title;
		this.description = description;
		if (values.isEmpty())
			throw new IllegalArgumentException("values must not be empty");
		this.values = values;
		if (values.contains(defaultValue))
			this.defaultValue = defaultValue;
		else
			this.defaultValue = values.get(0);
	}

	@Override
	public Object getCurrentValue() {
		return currentValue;
	}

	@Override
	public void setCurrentValue(Object newValue) {
		String str = newValue.toString();
		if (values.contains(str))
			this.currentValue = str;
		else
			this.currentValue = defaultValue;
	}

	@Override
	public String toString() {
		return "DoubleList[name=" + codename + ", title=" + title + ", description=" + description + ", values="
				+ values + ", defaultValue=" + defaultValue + ", currentValue=" + currentValue + "]";
	}

	/**
	 * @return possible choices
	 */
	public List<String> getChoices() {
		return values;
	}

	/**
	 * @return default value
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	@Override
	public Renderer getRenderer() {
		return new Selector(this);
	}
}
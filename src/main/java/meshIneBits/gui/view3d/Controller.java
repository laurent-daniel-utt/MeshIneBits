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

package meshIneBits.gui.view3d;

import meshIneBits.GeneratedPart;

import java.util.Observable;
import java.util.Observer;

/**
 * Observe {@link GeneratedPart} and
 */
public class Controller extends Observable implements Observer {

	private GeneratedPart part;

	private static Controller instance;

	public static Controller getInstance() {
		if (instance == null)
			instance = new Controller();
		return instance;
	}

	private Controller() {
	}

	GeneratedPart getCurrentPart() {
		return part;
	}

	/**
	 * @param part <tt>null</tt> to delete the current mesh
	 */
	void setCurrentPart(GeneratedPart part) {
		if (part != null) {
			this.part = part;
			part.addObserver(this);
		} else
			this.part = null;

		setChanged();
		notifyObservers();
	}

	@Override
	public void update(Observable o, Object arg) {

	}

	int getCurrentLayerNumber() {
		// TODO Implement toolbox in 3D view
		return part.getLayers().size() - 1;
	}
}

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

package meshIneBits.gui;

import meshIneBits.GeneratedPart;

import java.util.Observable;
import java.util.Observer;

/**
 * Observe {@link GeneratedPart} and observed by {@link Toolbar}
 */

public class MainController extends Observable implements Observer {

	static private MainController instance;
	private GeneratedPart currentPart;

	private MainController() {

	}

	public static MainController getInstance() {
		if (instance == null)
			instance = new MainController();
		return instance;
	}

	void setCurrentPart(GeneratedPart gp) {
		currentPart = gp;
		if (gp != null)
			currentPart.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg == null) return;
		if (o == currentPart) {
			switch ((GeneratedPart.Events) arg) {
				case MODEL_LOADED:
					MainWindow.getInstance().get3DView().setCurrentPart(currentPart);
					break;
				case SLICED:
					MainWindow.getInstance().get2DView().setCurrentPart(currentPart);
					break;
				case PAVED:
					break;
				case AUTO_OPTIMIZED:
					break;
				default:
					MainWindow.getInstance().get2DView().setCurrentPart(null);
					MainWindow.getInstance().get3DView().setCurrentPart(null);
					break;
			}
		}
	}

	public GeneratedPart getCurrentPart() {
		return currentPart;
	}
}

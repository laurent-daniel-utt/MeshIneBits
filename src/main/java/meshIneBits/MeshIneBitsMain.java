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

package meshIneBits;

import java.io.File;

import meshIneBits.config.CraftConfig;
import meshIneBits.config.CraftConfigLoader;
import meshIneBits.gui.MainWindow;
import meshIneBits.gui.view3d.ProcessingModelView;
import meshIneBits.util.Logger;

/**
 * Main class. Call {@link #main(String[])} to start MeshIneBits. Will first
 * load the configuration and then initialize the GUI. Then wait for
 * {@link #sliceModel(String)} to be called to create a {@link GeneratedPart}.
 * 
 * @see CraftConfigLoader
 * @see GeneratedPart
 */
public class MeshIneBitsMain {
	/**
	 * The Main method is the entry point of the program.
	 * 
	 * @param args
	 *            Program start's arguments, not used yet.
	 */
	public static void main(String[] args) {
		// Load the configuration
		CraftConfigLoader.loadConfig(null);

		// Load the graphical interface
		MainWindow.getInstance();
	}
}
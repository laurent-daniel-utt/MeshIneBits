/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016  Thibault Cassard & Nicolas Gouju.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 Vallon BENJAMIN.
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

package meshIneBits.config;

import meshIneBits.util.Logger;

import java.io.*;
import java.lang.reflect.Field;

public class CraftConfigLoader {
	/***************************
	 * Load and save functions
	 ***************************/

	public final static String PATTERN_CONFIG_EXTENSION = "mpconf";

	/**
	 * Loads the configuration from a file, use 'null' for the default config
	 * file.
	 * 
	 * @param filename name of file (no file path)
	 */
	public static void loadConfig(String filename) {
		if (filename == null) {
			filename = System.getProperty("user.home") + "/.MeshIneBits.conf";
			Logger.updateStatus("Loading: " + filename);
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			return;
		}
		String line;
		String section = null;
		try {
			while ((line = br.readLine()) != null) {
				if (line.startsWith(";"))
					continue;
				if (line.startsWith("[") && line.endsWith("]")) {
					section = line;
					continue;
				}
				if (line.indexOf('=') < 0)
					continue;
				String key = line.substring(0, line.indexOf('='));
				String value = line.substring(line.indexOf('=') + 1);
				if ("[MeshIneBits config]".equals(section)) {
					setField(key, value);
				}
			}
		} catch (IOException e) {
			Logger.error("IOException during loading of config file...");
		}
	}

	private static void setField(String key, String value) {
		Class<?> c = CraftConfig.class;
		Field f = null;
		try {
			f = c.getField(key);
			if (f == null)
				return;
			Setting s = f.getAnnotation(Setting.class);
			if (f.getType() == Double.TYPE) {
				double v = Double.parseDouble(value);
				if (s != null && v < s.minValue())
					v = s.minValue();
				if (s != null && v > s.maxValue())
					v = s.maxValue();
				f.setDouble(null, v);
			} else if (f.getType() == Integer.TYPE) {
				int v = Integer.parseInt(value);
				if (s != null && v < s.minValue())
					v = (int) s.minValue();
				if (s != null && v > s.maxValue())
					v = (int) s.maxValue();
				f.setInt(null, v);
			} else if (f.getType() == Boolean.TYPE) {
				f.setBoolean(null, Boolean.parseBoolean(value));
			} else if (f.getType() == String.class) {
				f.set(null, value.toString().replace("\\n", "\n"));
			} else {
				// throw new RuntimeException("Unknown config type: " +
				// f.getType());
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			Logger.warning("Found: " + key + " in the configuration, but I don't know this setting");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Saves the configuration to a file, use 'null' for the default config
	 * file.
	 * 
	 * @param filename name of file (not a file path)
	 */
	public static void saveConfig(String filename) {
		if (filename == null)
			filename = System.getProperty("user.home") + "/.MeshIneBits.conf";
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			bw.write(";Saved with version: " + CraftConfig.VERSION + "\n");
			bw.write("[MeshIneBits config]\n");
			Class<CraftConfig> configClass = CraftConfig.class;
			for (final Field f : configClass.getFields()) {
				Setting s = f.getAnnotation(Setting.class);
				if (s == null)
					continue;
				try {
					bw.write(f.getName() + "=" + f.get(null).toString().replace("\n", "\\n") + "\n");
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			bw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * Save the configuration for the pattern, using {@link Serializable}
	 * 
	 * @param file
	 *            if <tt>null</tt>, program will save to user's home directory.
	 */
	public static void savePatternConfig(File file) {
		if (file == null) {
			file = new File(System.getProperty("user.home") + File.separator + "MeshIneBits.PatternConfig."
					+ PATTERN_CONFIG_EXTENSION);
		}
		Logger.updateStatus("Trying to save your configuration...");
		try (FileOutputStream fos = new FileOutputStream(file)) {
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(CraftConfig.templateChoice.getPatternConfig());
			oos.flush();
			Logger.updateStatus("Saving completes.");
		} catch (Exception e) {
			Logger.message(e.getMessage());
			Logger.updateStatus("Saving fails. Your configuration might not be saved!");
		}
	}

	/**
	 * Load the configuration for the pattern, using {@link Serializable}
	 * 
	 * @param filePath
	 *            if <tt>null</tt>, program will load default file from user's
	 *            home directory
	 * @return <tt>null</tt> when loading fails.
	 */
	public static PatternConfig loadPatternConfig(File filePath) {
		if (filePath == null) {
			filePath = new File(System.getProperty("user.home") + File.separator + ".MeshIneBits.PatternConfig."
					+ PATTERN_CONFIG_EXTENSION);
		}
		Logger.updateStatus("Trying to load your configuration...");
		PatternConfig result = null;
		try (FileInputStream fis = new FileInputStream(filePath)) {
			ObjectInputStream ois = new ObjectInputStream(fis);
			result = (PatternConfig) ois.readObject();
			ois.close();
			Logger.updateStatus("Loading completes.");
		} catch (Exception e) {
			Logger.updateStatus("Loading fails. Your configuration can not be loaded!");
			Logger.message(e.getMessage());
		}
		return result;
	}
}
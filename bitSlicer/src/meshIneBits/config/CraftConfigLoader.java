package meshIneBits.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;

import meshIneBits.util.Logger;

public class CraftConfigLoader {
	/***************************
	 * Load and save functions
	 ***************************/

	public final static String PATTERN_CONFIG_EXTENSION = "mpconf";

	/**
	 * loadConfig
	 * 
	 * Loads the configuration from a file, use 'null' for the default config
	 * file.
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
	 * saveConfig
	 * 
	 * Saves the configuration to a file, use 'null' for the default config
	 * file.
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
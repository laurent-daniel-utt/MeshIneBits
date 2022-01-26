/*
 * MeshIneBits is a Java software to disintegrate a 3d mesh (model in .stl)
 * into a network of standard parts (called "Bits").
 *
 * Copyright (C) 2016-2022 DANIEL Laurent.
 * Copyright (C) 2016  CASSARD Thibault & GOUJU Nicolas.
 * Copyright (C) 2017-2018  TRAN Quoc Nhat Han.
 * Copyright (C) 2018 VALLON Benjamin.
 * Copyright (C) 2018 LORIMER Campbell.
 * Copyright (C) 2018 D'AUTUME Christian.
 * Copyright (C) 2019 DURINGER Nathan (Tests).
 * Copyright (C) 2020-2021 CLAIRIS Etienne & RUSSO André.
 * Copyright (C) 2020-2021 DO Quang Bao.
 * Copyright (C) 2021 VANNIYASINGAM Mithulan.
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
 *
 */

package meshIneBits.config;

import meshIneBits.util.Logger;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Properties;

public class CraftConfigLoader {
    /***************************
     * Load and save functions
     ***************************/

    public final static String PATTERN_CONFIG_EXTENSION = "mpconf";
    public static final String MESH_EXTENSION = "mesh";
    public static final String CRAFT_CONFIG_EXTENSION = ".MeshIneBits.conf";

    /**
     * Loads the configuration from a file, use 'null' for the default config
     * file.
     *
     * @param filename name of file (no file path)
     */
    public static void loadConfig(String filename) {
        if (filename == null) {
            filename = System.getProperty("user.home") + File.separator + CRAFT_CONFIG_EXTENSION;
        }
        Logger.updateStatus("Loading: " + filename);

        Properties craftProperties = new Properties();
        try (FileInputStream fis = new FileInputStream(filename)) {
            craftProperties.load(fis);
        } catch (FileNotFoundException e) {
            return;
        } catch (IOException e) {
            Logger.error("Cannot load craft properties. " + e.getMessage());
            return;
        }

        // Set field
        for (Field field : CraftConfig.settings) {
            try {
                // This is a double field
                DoubleSetting dAnno = field.getAnnotation(DoubleSetting.class);
                if (dAnno != null) {
                    try {
                        double val = Double.parseDouble(craftProperties.getProperty(field.getName()));
//                        System.out.println("Value: " + field.getName() + ":" + val);
                        if (val < dAnno.minValue() || val > dAnno.maxValue()) {
                            val = field.getDouble(null);
                        }
                        field.setDouble(null, val);
                    } catch (NullPointerException | NumberFormatException e) {
                        Logger.error("Cannot read property of " + field.getName() + ". " + e.getMessage());
                    }
                    continue;
                }

                // This is a float field
                FloatSetting fAnno = field.getAnnotation(FloatSetting.class);
                if (fAnno != null) {
                    try {
                        float val = Float.parseFloat(craftProperties.getProperty(field.getName()));
                        if (val < fAnno.minValue() || val > fAnno.maxValue())
                            val = field.getFloat(null);
                        field.setFloat(null, val);
                    } catch (NullPointerException | NumberFormatException e) {
                        Logger.error("Cannot read property of " + field.getName() + ". " + e.getMessage());
                    }
                    continue;
                }

                // This is a string field
                StringSetting strAnno = field.getAnnotation(StringSetting.class);
                if (strAnno != null) {
                    String str = craftProperties.getProperty(field.getName());
                    field.set(null, str);
                    continue;
                }

                // This is an integer field
                IntegerSetting intAnno = field.getAnnotation(IntegerSetting.class);
                if (intAnno != null) {
                    try {
                        int val = Integer.parseInt(craftProperties.getProperty(field.getName()));
                        if (val < intAnno.minValue() || val > intAnno.maxValue())
                            val = field.getInt(null);
                        field.setInt(null, val);
                    } catch (NumberFormatException e) {
                        Logger.error("Cannot read property of " + field.getName() + ". " + e.getMessage());
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
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
            filename = System.getProperty("user.home") + File.separator + CRAFT_CONFIG_EXTENSION;

        Properties craftProperties = new Properties();
        CraftConfig.settings.forEach(field -> {
            try {
                craftProperties.setProperty(field.getName(), String.valueOf(field.get(null)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        try (FileWriter fw = new FileWriter(filename)) {
            craftProperties.store(fw, null);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.error("Cannot save craft config. " + e.getMessage());
        }
    }

    /**
     * Save the configuration for the pattern, using {@link Serializable}
     *
     * @param file if <tt>null</tt>, program will save to user's home directory.
     */
    public static void savePatternConfig(File file) {
        if (file == null) {
            file = new File(System.getProperty("user.home") + File.separator + ".MeshIneBits."
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
     * @param filePath if <tt>null</tt>, program will load default file from user's
     *                 home directory
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

    public static void main(String[] args) {
        CraftConfigLoader.saveConfig(null);
        CraftConfigLoader.loadConfig(null);
        System.out.println("Update new config");
    }
}
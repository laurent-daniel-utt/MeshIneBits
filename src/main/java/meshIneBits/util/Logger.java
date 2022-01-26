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

package meshIneBits.util;

import java.util.Date;
import java.util.HashSet;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Logging class, has static functions for logging.
 * <p>
 * TODO: Different log listeners can connect to this logging service. So the GUI
 * version can show a nice progress dialog.
 */
public class Logger {

    private static HashSet<LoggingInterface> loggers = new HashSet<>();

    public static void error(String error) {
        System.err.println(error);
        for (LoggingInterface li : loggers) {
            li.error(error);
        }
    }

    public static void message(String message) {
        System.out.println(message);
        for (LoggingInterface li : loggers) {
            li.message(message);
        }
    }

    public static void register(LoggingInterface obj) {
        loggers.add(obj);
    }

    public static void setProgress(int value, int max) {
        // System.out.println(value + "/" + max);
        for (LoggingInterface li : loggers) {
            li.setProgress(value, max);
        }
    }

    public static void unRegister(LoggingInterface obj) {
        loggers.remove(obj);
    }

    public static void updateStatus(String status) {
        System.out.println(status);
        for (LoggingInterface li : loggers) {
            li.updateStatus(status);
        }
    }

    public static void warning(String warning) {
        System.err.println(warning);
        for (LoggingInterface li : loggers) {
            li.warning(warning);
        }
    }

    public static java.util.logging.Logger createSimpleInstanceFor(Class<?> cls) {
        final String simpleName = cls.getSimpleName();
        java.util.logging.Logger mainLogger = java.util.logging.Logger.getLogger(simpleName);
        mainLogger.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        mainLogger.setLevel(Level.ALL);
        handler.setLevel(Level.ALL);
        handler.setFormatter(new SimpleFormatter() {
            private static final String format = "[%1$tF %1$tT] [%2$s] [%4$s] %3$s %n";

            @Override
            public synchronized String format(LogRecord lr) {
                return String.format(format, new Date(lr.getMillis()), lr.getLevel().getName(),
                        lr.getMessage(), simpleName);
            }
        });
        mainLogger.addHandler(handler);
        return mainLogger;
    }
}
/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2007-2010 Max Reichardt,
 *   Robotics Research Lab, University of Kaiserslautern
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.rrlib.finroc_core_utils.jc;

import java.io.File;
import java.util.ArrayList;

import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Prefix;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.xml.XMLDocument;

/**
 * @author max
 *
 * Some basic file operations
 * and lookup of files in finroc repositories.
 */
@Inline @NoCpp @Prefix("s")
@Include("<boost/filesystem.hpp>")
public class Files {

    /** Paths in which to search for finroc files */
    private static ArrayList<String> pathsToCheck = new ArrayList<String>();

    static {
        String finrocHome = System.getenv("FINROC_HOME");
        String projectHome = System.getenv("FINROC_PROJECT_HOME");
        pathsToCheck.add(new File(".").getAbsolutePath() + "/");
        if (projectHome != null) {
            pathsToCheck.add(projectHome + "/");
            pathsToCheck.add(projectHome + "/sources/java/");
        }
        if (finrocHome != null) {
            pathsToCheck.add(finrocHome + "/");
        }
    }

    /**
     * Does file with specified name exist?
     *
     * @param filename File name
     * @return Answer
     */
    @InCpp("return boost::filesystem::_exists(filename.getStdString());")
    public static boolean exists(@Const @Ref String filename) {
        return new File(filename).exists();
    }

    /**
     * Lookup file in finroc repository.
     *
     * Searches in current path, $FINROC_PROJECT_HOME, $FINROC_HOME, $FINROC_HOME/sources/java, system installation (in this order).
     *
     * @param rawFilename Raw file name
     * @return Filename to open (can possibly be temp file somewhere). "" if no file was found.
     */
    public static String getFinrocFile(String rawFilename) {
        for (String path : pathsToCheck) {
            if (exists(path + rawFilename)) {
                return path + rawFilename;
            }
        }
        return "";
    }

    /**
     * Open XML document in finroc repository.
     * (when dealing with archives, this can be more efficient than getFinrocFile which might create a temp file)
     *
     * Searches in current path, $FINROC_PROJECT_HOME, $FINROC_HOME, $FINROC_HOME/sources/java, system installation (in this order).
     *
     * @param rawFilename Raw file name.
     * @param validate Whether the validation should be processed or not
     * @return XML Document. Throws exception if file cannot be found.
     */
    public static XMLDocument getFinrocXMLDocument(String rawFilename, boolean validate) throws Exception {
        String file = getFinrocFile(rawFilename);
        if (file.length() == 0) {
            throw new Exception("file not found");
        }
        return new XMLDocument(file);
    }
}

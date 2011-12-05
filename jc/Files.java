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

    /** $FINROC_PROJECT_HOME path if it exists - otherwise "" */
    private static String projectHome;

    /** Current working directory absolute path */
    private static String cwd;

    static {
        String finrocHome = System.getenv("FINROC_HOME");
        projectHome = System.getenv("FINROC_PROJECT_HOME");
        if (projectHome != null) {
            projectHome += "/";
            pathsToCheck.add(projectHome);
        }
        if (finrocHome != null) {
            pathsToCheck.add(finrocHome + "/");
            pathsToCheck.add(finrocHome + "/sources/java/");
        }
        cwd = new File(".").getAbsolutePath() + "/";
        pathsToCheck.add(cwd);
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
     * Does file with specified name exist in finroc repository?
     *
     * @param rawFilename Raw file name
     * @return Answer (true, when getFinrocFile(rawFilename).length() > 0 - but possibly more efficient)
     */
    public static boolean finrocFileExists(String rawFilename) {
        if (rawFilename == null || rawFilename.length() == 0) {
            return false;
        }
        return getFinrocFile(rawFilename).length() != 0;
    }

    /**
     * Lookup file in finroc repository.
     *
     * Searches in $FINROC_PROJECT_HOME, $FINROC_HOME, $FINROC_HOME/sources/java, current path, system installation (in this order).
     *
     * @param rawFilename Raw file name
     * @return Filename to open (can possibly be temp file somewhere). "" if no file was found.
     */
    public static String getFinrocFile(String rawFilename) {
        if (rawFilename.startsWith("/")) {
            return exists(rawFilename) ? rawFilename : "";
        }
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
     * Searches in $FINROC_PROJECT_HOME, $FINROC_HOME, $FINROC_HOME/sources/java, current path, system installation (in this order).
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
        return new XMLDocument(file, validate);
    }

    /**
     * Determine where to save file to.
     * If a suitable file already exists, it is returned (and typically overwritten).
     * Otherwise the most suitable location is returned (paths should alredy exist).
     * Locations are considered in this order: $FINROC_PROJECT_HOME, $FINROC_HOME, $FINROC_HOME/sources/java, current path.
     * If rawFilename has no path, either $FINROC_PROJECT_HOME is returned if set - otherwise the current path.
     *
     * @param rawFilename Raw file name
     * @return File to save to. "" if no location seems suitable
     */
    public static String getFinrocFileToSaveTo(String rawFilename) {
        String file = getFinrocFile(rawFilename);
        if (file.length() > 0) {
            return file;
        }
        if (!rawFilename.contains("/")) {
            return projectHome != null ? (projectHome + rawFilename) : (cwd + rawFilename);
        }
        String rawpath = rawFilename.substring(0, rawFilename.lastIndexOf("/"));
        for (String path : pathsToCheck) {
            if (exists(path + rawpath) && new File(path + rawpath).isDirectory()) {
                return path + rawFilename;
            }
        }
        return "";
    }
}

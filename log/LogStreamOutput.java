/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2010 Robotics Research Lab, University of Kaiserslautern
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
package org.rrlib.finroc_core_utils.log;

import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;

/**
* The enumeration that encodes the streams used by a logging domain.
* Messages can be streams to stdout, stderr, into on file per domain
* or into on combined file for all domains that are recursively
* configured in one subtree of the domain hierarchy.
 */
@JavaOnly
public enum LogStreamOutput {
    LS_STDOUT,          //!< Messages are printed to stdout
    LS_STDERR,          //!< Messages are printed to stderr
    LS_FILE,            //!< Messages are printed to one file per domain
    LS_COMBINED_FILE,   //!< Messages are collected in one file per recursively configured subtree
    LS_DIMENSION        //!< Endmarker and dimension of eLogStream
}

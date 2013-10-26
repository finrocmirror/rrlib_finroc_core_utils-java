//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.log;

import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogDomainRegistry;

/**
 * @author Max Reichardt
 *
 * Log domain definitions "finroc" and "finroc.util" are defined here
 */
public class LogDefinitions {

    /** finroc logging domain */
    public static final LogDomain finroc = LogDomainRegistry.getDefaultDomain().getSubDomain("finroc");

    /** finroc.util logging domain */
    public static final LogDomain finrocUtil = finroc.getSubDomain("util");
}

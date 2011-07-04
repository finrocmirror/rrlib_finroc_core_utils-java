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
package org.rrlib.finroc_core_utils.xml;

/**
 * Exceptions thrown in RRLib XML2 Wrapper are of this type.
 * This exception class is a std::runtime_error and used when invalid
 * XML input is encountered that can not be handled automatically.
 * Thus, catching exceptions of this type distinguishes from other runtime
 * errors.
 */
public class XML2WrapperException extends Exception {

    /** UID */
    private static final long serialVersionUID = -3624705191730900543L;

    /**
     * The ctor of tXML2WrapperException
     *
     * This ctor forwards instantiation of an exception object to
     * std::runtime_error with the given message as error description.
     *
     * @param message   A message that describes the error
     */
    public XML2WrapperException(String message) {
        super(message);
    }

    /**
     * @param e Exception cause
     */
    XML2WrapperException(Exception e) {
        super(e);
    }
}

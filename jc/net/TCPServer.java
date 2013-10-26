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
package org.rrlib.finroc_core_utils.jc.net;

import java.net.Socket;

/**
 * @author Max Reichardt
 *
 * All classes that receive TCP connections should implement this interface
 * to be able to register at the TCPConnectionHandler.
 */
public interface TCPServer {

    /**
     * Does Server handle this kind of connection? (currently decided using
     * the first byte of the input stream)
     *
     * @param firstByte First byte of the input stream
     * @return Returns whether server accepts this connection
     */
    public boolean accepts(byte firstByte);

    /**
     * Accept this connection
     * (is started in a new thread by handler)
     *
     * @param socket Socket
     * @param firstByte First byte of the input stream that was already read
     */
    public void acceptConnection(Socket socket, byte firstByte);
}

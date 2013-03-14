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
package org.rrlib.finroc_core_utils.jc.net;

import java.net.Socket;
import java.net.SocketException;

import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.jc.net.IOException;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;
import org.rrlib.finroc_core_utils.serialization.BufferInfo;
import org.rrlib.finroc_core_utils.serialization.FixedBuffer;
import org.rrlib.finroc_core_utils.serialization.InputStreamSource;
import org.rrlib.finroc_core_utils.serialization.OutputStreamSink;
import org.rrlib.finroc_core_utils.serialization.Sink;
import org.rrlib.finroc_core_utils.serialization.Source;

/**
 * @author Max Reichardt
 *
 * This class wraps a Java TCP Socket
 * and works similarly in C++.
 */
public class NetSocket {

    /** Wrapped Java Socket */
    private final Socket wrapped;

    public NetSocket(Socket socket) {
        wrapped = socket;
    }

    /** Log domain for this class */
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("net");

    private NetSocket(IPSocketAddress isa) throws ConnectException {
        try {
            Socket tmp = new Socket(isa.getAddress().wrapped, isa.getPort());
            wrapped = tmp;
        } catch (java.io.IOException e) {
            throw new ConnectException(e.getMessage());
        }
    }

    /**
     * This function replaces the constructor. This workaround seems necessary
     * to create object in a shared_ptr and use this shared_ptr also in getInputStream
     * and getOutputStream.
     *
     * @param isa IP and socket address of connection partner
     * @return Created & connected NetSocket instance
     */
    public static NetSocket createInstance(IPSocketAddress isa) throws ConnectException {
        NetSocket tmp = new NetSocket(isa);
        return tmp;
    }

    /**
     * @return Remote port
     */
    public int getPort() {
        return wrapped.getPort();
    }

    /**
     * @return Wrapped Java Socket
     */
    public Socket getSocket() {
        return wrapped;
    }

    /**
     * @return Remote IP address
     */
    public IPAddress getIP() {
        return new IPAddress(wrapped.getInetAddress());
    }

    /**
     * @return Returns Source for this socket
     */
    public Source getSource() throws IOException {
        try {
            return new InputStreamSource(wrapped.getInputStream());
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return Returns Sink for this socket
     */
    public Sink getSink() throws IOException {
        try {
            return new OutputStreamSink(wrapped.getOutputStream());
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * Closes network socket
     */
    public void close() throws IOException {
        try {
            shutdownReceive();
            wrapped.close();
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return Return remote socket address as string
     */
    public String getRemoteSocketAddress() {
        return getIP().toString() + ":" + getPort();
    }

    public IPSocketAddress getRemoteIPSocketAddress() {
        return new IPSocketAddress(getIP(), getPort());
    }

    /**
     * Shutdown socket (does nothing if socket is already closed)
     */
    public void shutdownSend() {
        try {
            wrapped.shutdownOutput();
        } catch (Exception e) {
            logDomain.log(LogLevel.LL_ERROR, "NetSocket", e);
        }
    }

    /**
     * Shutdown socket (does nothing if socket is already closed)
     */
    public void shutdownReceive() {
        try {
            wrapped.shutdownInput();
        } catch (SocketException e) {
            // do nothing... can happen if socket is close twice
        } catch (Exception e) {
            logDomain.log(LogLevel.LL_ERROR, "NetSocket", e);
        }
    }
}

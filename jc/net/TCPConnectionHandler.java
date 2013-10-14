//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.rrlib.finroc_core_utils.jc.ArrayWrapper;
import org.rrlib.finroc_core_utils.jc.MutexLockOrder;
import org.rrlib.finroc_core_utils.jc.container.ConcurrentMap;
import org.rrlib.finroc_core_utils.jc.container.SafeConcurrentlyIterableList;
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.jc.thread.ThreadUtil;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;

/**
 * @author Max Reichardt
 *
 * Handles incoming connections on TCP Ports.
 * This class allows using multiple protocols over a single port.
 *
 * An instance of this class listens to a single port
 *
 * TCP Serving class (TCPServer interface) register at this handler.
 */
public class TCPConnectionHandler extends Thread {

    /** keeps track on which ports handlers are already running */
    private static final ConcurrentMap<Integer, TCPConnectionHandler> handlers = new ConcurrentMap<Integer, TCPConnectionHandler>(getNullPtr());

    /** All servers listening on port */
    private final SafeConcurrentlyIterableList<TCPServer> servers = new SafeConcurrentlyIterableList<TCPServer>(3, 5);

    /** Port the Handler is running on */
    private int port;

    /** Close Connection Handler? */
    private boolean close;

    /** Server socket to use */
    private ServerSocket serverSocket = null;

    /** Thread::threadList will be locked afterwards */
    @SuppressWarnings("unused")
    private static MutexLockOrder staticClassMutex = new MutexLockOrder(0x7FFFFFFF - 50);

    /** Log domain for this class */
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("net");

    /**
     * @param port Port the Handler is running on
     */
    private TCPConnectionHandler(int port) {
        this.port = port;
        setName("TCPConnectionHandler on port " + port);
        setDaemon(true);
    }

    /**
     * Create port for server socket
     *
     * @return Did this succeed? (or is port in use)
     */
    public boolean createSocket() {

        try {
            serverSocket = new ServerSocket(port);
            return true;
        } catch (Exception e) {
            //System.err.println("Could not listen on port: " + port + ".");
            logDomain.log(LogLevel.WARNING, getLogDescription(), "Could not listen on port: " + port + ".");
            return false;
        }
    }

    /** Helper for simpler source code conversion */
    private static TCPConnectionHandler getNullPtr() {
        return null;
    }

    // Start server for HTTP and ProtoOmega requests etc.
    public void run() {

        //JavaOnlyBlock
        assert(serverSocket != null);
        while (!close) {
            try {
                handle(serverSocket.accept());
            } catch (IOException e) {
                logDomain.log(LogLevel.WARNING, getLogDescription(), e);
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            logDomain.log(LogLevel.WARNING, getLogDescription(), e);
        }
    }

    public void stopThread() {
        close = true;
    }

    /**
     * "Handles" incoming TCP connection
     *
     * @param socket Socket of incoming connection
     */
    private void handle(Socket socket) throws IOException {

        // read first byte
        byte first = 0;

        socket.setSoTimeout(2000);
        first = (byte)socket.getInputStream().read();
        socket.setSoTimeout(0);

        // look for server that handles connection
        ArrayWrapper<TCPServer> it = servers.getIterable();
        for (int i = 0, n = it.size(); i < n; i++) {
            TCPServer ts = it.get(i);
            if (ts != null && ts.accepts(first)) {
                HandlerThread ht = ThreadUtil.getThreadSharedPtr(new HandlerThread(socket, ts, first));
                ht.start();
                return;
            }
        }

        // no handler
        //System.out.println("No TCP handler found for stream id " + first  + " on port " + port + ". Closing connection.");
        logDomain.log(LogLevel.WARNING, getLogDescription(), "No TCP handler found for stream id " + first  + " on port " + port + ". Closing connection.");
        try {
            socket.getInputStream().close();
            socket.close();
        } catch (IOException e) {
            logDomain.log(LogLevel.WARNING, getLogDescription(), e);
        }
    }

    /** Creates new thread for server request */
    static class HandlerThread extends Thread {

        /** Socket that was accepted */
        private Socket socket;

        /** Server to handler request */
        private TCPServer server;

        /** First byte of request */
        private byte firstByte;

        public HandlerThread(Socket socketX, TCPServer serverX, byte firstByteX) {
            setName("TCP HandlerThread");
            socket = socketX;
            server = serverX;
            firstByte = firstByteX;
        }

        public void run() {
            setName(server.getClass().getSimpleName());

            server.acceptConnection(socket, firstByte);
        }
    }

    /**
     * Remove handler from list of handlers
     *
     * @param handler Handle to remove
     */
    @SuppressWarnings("unused")
    private synchronized static void removeHandler(int port) {
        handlers.remove(port);
    }

    /**
     * Add server to handler.
     * This server will be notified/queried when new connections arive on
     * this port.
     *
     * @param ts Server
     * @param port Port the server listens on
     * @return Did adding server succeed (fails, if port is already used)
     */
    public static synchronized boolean addServer(TCPServer ts, int port) {
        TCPConnectionHandler handler = handlers.get(port);
        if (handler == null) {
            handler = new TCPConnectionHandler(port);
            if (!handler.createSocket()) {
                return false;
            }
            handlers.put(port, handler);
            ThreadUtil.setAutoDelete(handler);
            handler.start();  // start server socket in new Thread
        }
        handler.servers.add(ts, false);
        return true;
    }

    /**
     * Remove server from handler.
     *
     * @param ts Server
     * @param port Port the server listens on
     */
    public static synchronized void removeServer(TCPServer ts, int port) {
        TCPConnectionHandler handler = handlers.get(port);
        if (handler != null) {
            handler.servers.remove(ts);
        }
    }

    /**
     * @return Description for logging
     */
    private String getLogDescription() {
        return "TCPConnectionHandler on port " + port;
    }
}

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
package org.finroc.jc.net;

import java.io.IOException;
import java.net.ServerSocket;

import org.finroc.jc.ArrayWrapper;
import org.finroc.jc.MutexLockOrder;
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.IncludeClass;
import org.finroc.jc.annotation.Init;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.annotation.SizeT;
import org.finroc.jc.annotation.Virtual;
import org.finroc.jc.container.ConcurrentMap;
import org.finroc.jc.container.SafeConcurrentlyIterableList;
import org.finroc.jc.log.LogDefinitions;
import org.finroc.jc.thread.ThreadUtil;
import org.finroc.log.LogDomain;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * Handles incoming connections on TCP Ports.
 * This class allows using multiple protocols over a single port.
 *
 * An instance of this class listens to a single port
 *
 * TCP Serving class (TCPServer interface) register at this handler.
 */
@Ptr
@Include("<boost/asio/ip/tcp.hpp>")
@IncludeClass(SafeConcurrentlyIterableList.class)
public class TCPConnectionHandler extends Thread {

    /*Cpp
    boost::asio::ip::tcp::acceptor acceptor_;
    boost::asio::ip::tcp::acceptor* acceptorPtr;
    */

    /** keeps track on which ports handlers are already running */
    private static final ConcurrentMap<Integer, TCPConnectionHandler> handlers = new ConcurrentMap<Integer, TCPConnectionHandler>(getNullPtr());

    /** All servers listening on port */
    @CppType("SafeConcurrentlyIterableList<TCPServer*, 5, false>")
    private final SafeConcurrentlyIterableList<TCPServer> servers = new SafeConcurrentlyIterableList<TCPServer>(3, 5);

    /** Port the Handler is running on */
    private int port;

    /** Close Connection Handler? */
    private boolean close;

    /** Server socket to use */
    @JavaOnly
    private ServerSocket serverSocket = null;

    /** Thread::threadList will be locked afterwards */
    @SuppressWarnings("unused")
    private static MutexLockOrder staticClassMutex = new MutexLockOrder(0x7FFFFFFF - 50);

    /** Log domain for this class */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"net\");")
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("net");

    /**
     * @param port Port the Handler is running on
     */
    @Init( {"acceptor_(TCPUtil::io_service)", "acceptorPtr(NULL)"})
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

            //JavaOnlyBlock
            serverSocket = new ServerSocket(port);
            return true;

            /*Cpp
            boost::system::error_code ec;
            boost::asio::ip::tcp::_endpoint epoint(boost::asio::ip::tcp::_v4(), port);
            acceptor_._open(epoint._protocol(), ec);
            if (ec) {
                //printf("Could not listen on port: %d.\n", port);
                _FINROC_LOG_STREAM(rrlib::logging::eLL_WARNING, logDomain, "Could not listen on port: ", port, ".");
                return false;
            }
            acceptor_.set_option(boost::asio::ip::tcp::acceptor::reuse_address(true));
            acceptor_._bind(epoint, ec);
            if (ec) {
                //printf("Could not listen on port: %d.\n", port);
                _FINROC_LOG_STREAM(rrlib::logging::eLL_WARNING, logDomain, "Could not listen on port: ", port, ".");
                return false;
            }
            return true;
            */
        } catch (Exception e) {
            //System.err.println("Could not listen on port: " + port + ".");
            logDomain.log(LogLevel.LL_WARNING, getLogDescription(), "Could not listen on port: " + port + ".");
            return false;
        }
    }

    /** Helper for simpler source code conversion */
    @InCpp("return std::tr1::shared_ptr<TCPConnectionHandler>();")
    private static @SharedPtr TCPConnectionHandler getNullPtr() {
        return null;
    }

    /*Cpp
    virtual ~TCPConnectionHandler() {
        removeHandler(port);
        TCPUtil::io_service.stop();
    }
    */

    // Start server for HTTP and ProtoOmega requests etc.
    public void run() {

        //JavaOnlyBlock
        assert(serverSocket != null);
        while (!close) {
            try {
                handle(new NetSocket(serverSocket.accept()));
            } catch (IOException e) {
                logDomain.log(LogLevel.LL_WARNING, getLogDescription(), e);
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            logDomain.log(LogLevel.LL_WARNING, getLogDescription(), e);
        }

        /*Cpp
        assert(acceptor_.is_open());
        try {
            acceptorPtr = &acceptor_;
            boost::system::error_code ec;
            acceptor_._listen();
            while(!close) {
                std::tr1::shared_ptr<NetSocket> s = NetSocket::createInstance();
                acceptor_._accept(s->getSocket(), ec);
                if (ec) {
                    //printf("NetSocket accept error: %s\n", ec._message()._c_str());
                    _FINROC_LOG_STREAM(rrlib::logging::eLL_ERROR, logDomain, "NetSocket accept error: ", ec._message()._c_str());
                } else {
                    handle(s);
                }
            }
        } catch (std::exception& e) {
            std::cerr << e._what() << std::endl;
        }
        acceptorPtr = NULL;
        acceptor_._close();
         */
    }

    @Virtual
    public void stopThread() {
        close = true;

        /*Cpp
        boost::system::error_code ec;
        if (acceptorPtr != NULL) {
            acceptorPtr->_close(ec);
            if (ec) {
                //printf("NetSocket close error: %s\n", ec._message()._c_str());
                _FINROC_LOG_STREAM(rrlib::logging::eLL_ERROR, logDomain, "NetSocket close error: ", ec._message()._c_str());
            }

            // establish connection and close it - this way, we can get thread out of the loop
            try {
                NetSocket::createInstance(IPSocketAddress("localhost", port));
            } catch (util::Exception& e) {
                // do nothing... things should have worked out
            }
        }
         */
    }

    /**
     * "Handles" incoming TCP connection
     *
     * @param s Socket of incoming connection
     */
    private void handle(NetSocket s) throws IOException {

        // read first byte
        byte first = 0;

        //JavaOnlyBlock
        first = (byte)s.getSocket().getInputStream().read();

        /*Cpp
        FixedBuffer tmp(1);
        s->readFully(tmp, 0, 1);
        first = tmp.getByte(0);
         */

        // look for server that handles connection
        @Ptr ArrayWrapper<TCPServer> it = servers.getIterable();
        for (@SizeT int i = 0, n = it.size(); i < n; i++) {
            TCPServer ts = it.get(i);
            if (ts != null && ts.accepts(first)) {
                @SharedPtr HandlerThread ht = ThreadUtil.getThreadSharedPtr(new HandlerThread(s, ts, first));
                ht.start();
                return;
            }
        }

        // no handler
        //System.out.println("No TCP handler found for stream id " + first  + " on port " + port + ". Closing connection.");
        logDomain.log(LogLevel.LL_WARNING, getLogDescription(), "No TCP handler found for stream id " + first  + " on port " + port + ". Closing connection.");
        try {

            // JavaOnlyBlock
            s.getSocket().getInputStream().close();

            s.close();
        } catch (org.finroc.jc.net.IOException e) {
            logDomain.log(LogLevel.LL_WARNING, getLogDescription(), e);
        }
    }

    /** Creates new thread for server request */
    static class HandlerThread extends Thread {

        /** Socket that was accepted */
        private NetSocket socket;

        /** Server to handler request */
        private TCPServer server;

        /** First byte of request */
        private byte firstByte;

        public HandlerThread(NetSocket socketX, TCPServer serverX, byte firstByteX) {
            setName("TCP HandlerThread");
            socket = socketX;
            server = serverX;
            firstByte = firstByteX;
        }

        public void run() {
            //JavaOnlyBlock
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
                //Cpp delete handler;
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

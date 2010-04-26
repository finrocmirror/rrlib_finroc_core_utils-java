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

import java.net.Socket;
import java.net.SocketException;

import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Init;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.SharedPtr;
import org.finroc.jc.annotation.Superclass;
import org.finroc.jc.annotation.Virtual;
import org.finroc.jc.net.IOException;
import org.finroc.jc.stream.InputStreamSource;
import org.finroc.jc.stream.OutputStreamSink;
import org.finroc.jc.stream.Sink;
import org.finroc.jc.stream.Source;

/**
 * @author max
 *
 * This class wraps a Java TCP Socket
 * and works similarly in C++.
 */
@Include( {"<boost/asio/ip/tcp.hpp>", "stream/BufferInfo.h", "stream/FixedBuffer.h", "<boost/asio/error.hpp>",
           "<boost/asio/completion_condition.hpp>", "<boost/asio/read.hpp>"
          })
@SharedPtr @Superclass( {Object.class, Source.class, Sink.class})
public class NetSocket {

    /*Cpp

    // shared pointer to this
    std::tr1::weak_ptr<NetSocket> thizz;

    // has input/output stream been closed?
    //bool inputClosed, outputClosed;

    // buffer size for input and output streams
    static const size_t BUFSIZE = 8192;

    // inputStream and outputStream buffers
    FixedBuffer inputStreamBuf, outputStreamBuf;

     */

    /** Wrapped Java Socket */
    @InCpp("boost::asio::ip::tcp::socket wrapped;")
    private final Socket wrapped;

    @JavaOnly
    public NetSocket(Socket socket) {
        wrapped = socket;
    }

    /*Cpp
    NetSocket() : thizz(), inputStreamBuf(BUFSIZE), outputStreamBuf(BUFSIZE), wrapped(TCPUtil::io_service) {}
     */

    @Init( {"thizz()",
            "wrapped(TCPUtil::io_service)",
            "inputStreamBuf(BUFSIZE)",
            "outputStreamBuf(BUFSIZE)"
           })
    private NetSocket(IPSocketAddress isa) throws ConnectException {

        // JavaOnlyBlock
        try {
            Socket tmp = new Socket(isa.getAddress().wrapped, isa.getPort());
            wrapped = tmp;
        } catch (java.io.IOException e) {
            throw new ConnectException(e.getMessage());
        }

        /*Cpp
        boost::system::error_code ec;
        wrapped._connect(boost::asio::ip::tcp::_endpoint(isa.getAddress().wrapped, isa.getPort()), ec);
        if (ec) {
            throw ConnectException();
        }
         */
    }

    /**
     * This function replaces the constructor. This workaround seems necessary
     * to create object in a shared_ptr and use this shared_ptr also in getInputStream
     * and getOutputStream.
     *
     * @param isa IP and socket address of connection partner
     * @return Created & connected NetSocket instance
     */
    public static @SharedPtr NetSocket createInstance(IPSocketAddress isa) throws ConnectException {
        @SharedPtr NetSocket tmp = new NetSocket(isa);
        //Cpp tmp->thizz = tmp;
        return tmp;
    }

    /*Cpp
    static std::tr1::shared_ptr<NetSocket> createInstance() {
        std::tr1::shared_ptr<NetSocket> tmp(new NetSocket());
        tmp->thizz = tmp;
        return tmp;
    }

    virtual ~NetSocket() {
        //wrapped._shutdown();
        close();
    }

    boost::asio::ip::tcp::socket& getSocket() {
        return wrapped;
    }
     */

    /**
     * @return Remote port
     */
    @InCpp("return wrapped.remote_endpoint().port();")
    public int getPort() {
        return wrapped.getPort();
    }

    /**
     * @return Wrapped Java Socket
     */
    @JavaOnly
    public Socket getSocket() {
        return wrapped;
    }

    /**
     * @return Remote IP address
     */
    @InCpp("return IPAddress(wrapped.remote_endpoint().address());")
    public IPAddress getIP() {
        return new IPAddress(wrapped.getInetAddress());
    }

    /**
     * @return Returns Source for this socket
     */
    @InCpp( {"assert (!thizz._expired());", "return ::std::tr1::shared_ptr<Source>(thizz);"})
    public @SharedPtr Source getSource() throws IOException {
        try {
            return new InputStreamSource(wrapped.getInputStream());
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }
    }

    /**
     * @return Returns Sink for this socket
     */
    @InCpp( {"assert (!thizz._expired());", "return ::std::tr1::shared_ptr<Sink>(thizz);"})
    public @SharedPtr Sink getSink() throws IOException {
        try {
            return new OutputStreamSink(wrapped.getOutputStream());
        } catch (java.io.IOException e) {
            throw new IOException(e);
        }
    }

    /*Cpp

    // Source implementation

    virtual void close(InputStreamBuffer* inputStreamBuffer, BufferInfo& buffer) {
        buffer.reset();
    //      inputClosed = true;
    //      if (inputClosed && outputClosed) {
    //          close();
    //      }
    }

    virtual void directRead(InputStreamBuffer* inputStreamBuffer, FixedBuffer& buffer, size_t offset, size_t len) {
        size_t remaining = buffer.capacity() - offset;
        assert(len <= remaining);
        boost::asio::mutable_buffers_1 buf(buffer.getPointer() + offset, len);
        boost::system::error_code ec;
        __attribute__((unused))
        size_t read = boost::asio::_read(wrapped, buf, boost::asio::transfer_at_least(Math::min(len, remaining)), ec);
        assert(read == len);
        if (ec == boost::asio::error::eof) {
            throw EOFException();
        } else if (ec) {
            throw IOException();
        }
    }

    virtual bool directReadSupport() const {
        return true;
    }

    virtual bool moreDataAvailable(InputStreamBuffer* inputStreamBuffer, BufferInfo& buffer) {
        return wrapped._available() > 0;
    }

    virtual void read(InputStreamBuffer* inputStreamBuffer, BufferInfo& buffer, size_t len = 0) {
        boost::asio::mutable_buffers_1 buf(buffer.buffer->getPointer(), BUFSIZE);
        boost::system::error_code ec;
        size_t read = 0;
        if (len <= 0) {
            read = wrapped._receive(buf, 0, ec);
        } else {
            read = boost::asio::_read(wrapped, buf, boost::asio::transfer_at_least(Math::min(BUFSIZE, len)), ec);
        }
        if (ec == boost::asio::error::eof) {
            throw EOFException();
        } else if (ec) {
            throw Exception();
        }
        buffer.setRange(0, read);
        buffer.position = 0;
    }

    virtual void reset(InputStreamBuffer* inputStreamBuffer, BufferInfo& buffer) {
        buffer.buffer = &inputStreamBuf;
        buffer.position = 0;
        buffer.setRange(0, 0);
    }

    // Sink implementation

    virtual void close(OutputStreamBuffer* outputStreamBuffer, BufferInfo& buffer) {
        buffer.reset();
    //      outputClosed = true;
    //      if (inputClosed && outputClosed) {
    //          close();
    //      }
    }

    virtual void directWrite(OutputStreamBuffer* outputStreamBuffer, const FixedBuffer& buffer, size_t offset, size_t len) {
        while(len > 0) {
            boost::asio::const_buffers_1 buf(buffer.getPointer() + offset, len);
            //boost::system::error_code ec;
            size_t written = wrapped._send(buf);
            offset += written;
            len -= written;
        }
    }

    virtual void flush(OutputStreamBuffer* outputStreamBuffer, const BufferInfo& buffer) {
        // do nothing... flushing should be done automatically by boost
    }

    virtual bool directWriteSupport() {
        return true;
    }

    virtual void reset(OutputStreamBuffer* outputStreamBuffer, BufferInfo& buffer) {
        buffer.buffer = &outputStreamBuf;
        buffer.position = 0;
        buffer.setRange(0, BUFSIZE);
    }

    virtual bool write(OutputStreamBuffer* outputStreamBuffer, BufferInfo& buffer, int writeSizeHint) {
        size_t len = buffer.position;
        size_t offset = 0;
        while(len > 0) {
            boost::asio::const_buffers_1 buf(buffer.buffer->getPointer() + offset, len);
            //boost::system::error_code ec;
            size_t written = wrapped._send(buf);
            offset += written;
            len -= written;
        }
        buffer.position = 0;
        buffer.setRange(0, BUFSIZE);
        return true;
    }

    virtual int read(FixedBuffer& buffer, size_t offset) {
        size_t remaining = buffer.capacity() - offset;
        boost::asio::mutable_buffers_1 buf(buffer.getPointer() + offset, remaining);
        boost::system::error_code ec;
        size_t read = wrapped._receive(buf, 0, ec);
        if (ec == boost::asio::error::eof) {
            return -1;
        } else if (ec) {
            throw Exception();
        }
        return read;
    }

    virtual void readFully(FixedBuffer& buffer, size_t offset, size_t len) {
        size_t remaining = buffer.capacity() - offset;
        assert(len <= remaining);
        boost::asio::mutable_buffers_1 buf(buffer.getPointer() + offset, remaining);
        boost::system::error_code ec;
        //size_t read =
        boost::asio::_read(wrapped, buf, boost::asio::transfer_at_least(len), ec);
        if (ec == boost::asio::error::eof) {
            throw EOFException();
        } else if (ec) {
            throw IOException();
        }
    }

    virtual void write(const FixedBuffer& buffer, size_t offset, size_t length) {
        while(length > 0) {
            boost::asio::const_buffers_1 buf(buffer.getPointer() + offset, length);
            boost::system::error_code ec;
            size_t written = wrapped._send(buf);
            offset += written;
            length -= written;
        }
    }
    */

    /**
     * Closes network socket
     */
    @InCpp( {"shutdownReceive();", "wrapped._close();"})
    @Virtual public void close() throws IOException {
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
    @InCpp( {"boost::system::error_code ec;", "wrapped._shutdown(boost::asio::socket_base::shutdown_send, ec);"})
    public void shutdownSend() {
        try {
            wrapped.shutdownOutput();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Shutdown socket (does nothing if socket is already closed)
     */
    @InCpp( {"boost::system::error_code ec;", "wrapped._shutdown(boost::asio::socket_base::shutdown_receive, ec);"})
    public void shutdownReceive() {
        try {
            wrapped.shutdownInput();
        } catch (SocketException e) {
            // do nothing... can happen if socket is close twice
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

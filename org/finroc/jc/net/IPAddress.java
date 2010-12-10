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

import java.net.InetAddress;

import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.CppInclude;
import org.finroc.jc.annotation.Friend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.log.LogDefinitions;
import org.finroc.jc.stream.InputStreamBuffer;
import org.finroc.jc.stream.OutputStreamBuffer;
import org.finroc.log.LogDomain;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * This class wraps an IP address.
 * It's similar to and inspired from InetAddress in Java.
 */
@PassByValue
@Include( {"<boost/asio/ip/tcp.hpp>", "TCPUtil.h"})
@CppInclude( {"<sstream>", "stream/InputStreamBuffer.h", "stream/OutputStreamBuffer.h"}) @Friend( {NetSocket.class, IPSocketAddress.class})
public class IPAddress {

    /*Cpp
    IPAddress() : wrapped() {}

    IPAddress(const boost::asio::ip::address& w) : wrapped(w) {}
     */

    /** Wrapped ip address */
    @InCpp( {"boost::asio::ip::address wrapped;"})
    protected InetAddress wrapped;

    /** IP Address of local host */
    @JavaOnly
    @Const private static IPAddress LOCAL_HOST;

    /** Log domain for this class */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"net\");")
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("net");

    static {
        try {
            LOCAL_HOST = new IPAddress(InetAddress.getLocalHost());
        } catch (java.net.UnknownHostException e) {
            logDomain.log(LogLevel.LL_ERROR, "IPAdress static init", e);
        }
    }

    @JavaOnly IPAddress(InetAddress wrap) {
        wrapped = wrap;
    }

    /*Cpp
    bool isSet() const {
      boost::asio::ip::address def;
      return def != wrapped;
    }
     */

    /**
     * Resolve IP address by name
     *
     * @param hostname Host name
     * @return Resolved ip address
     */
    public static IPAddress getByName(String hostname) throws UnknownHostException {
        // JavaOnlyBlock
        try {
            return new IPAddress(InetAddress.getByName(hostname));
        } catch (java.net.UnknownHostException e) {
            throw new UnknownHostException(e);
        }

        /*Cpp
        boost::asio::ip::tcp::resolver resolver(TCPUtil::io_service);
        boost::asio::ip::tcp::resolver::query query(boost::asio::ip::tcp::_v4(), hostname.getStdString(), "");
        boost::system::error_code ec;
        boost::asio::ip::tcp::resolver::iterator endpoint_iterator = resolver._resolve(query, ec);
        if (ec) {
            throw UnknownHostException();
        }
        boost::asio::ip::tcp::resolver::iterator end;

        //boost::asio::ip::tcp::socket testsocket(TCPUtil::io_service);
        //boost::system::error_code error = boost::asio::error::host_not_found;
        while (endpoint_iterator != end) {
            //testsocket._connect(*endpoint_iterator++, error);
            //testsocket._close();
            //if (error == 0) {
            return IPAddress((*endpoint_iterator)._endpoint()._address());
            //}
        }
        throw UnknownHostException();
         */
    }

    public String toString() {
        //JavaOnlyBlock
        return wrapped.getHostAddress().toString();

        /*Cpp
        std::ostringstream tmpbuf;
        tmpbuf << wrapped;
        return String(tmpbuf._str());
        */
    }

    public boolean equals(Object other) {
        if (other instanceof IPAddress) {
            // JavaOnlyBlock
            return ((IPAddress)other).wrapped.equals(wrapped);

            //Cpp return (static_cast<const IPAddress*>(&other))->wrapped == wrapped;
        }
        return false;
    }

    /**
     * Serialize address to output stream
     *
     * @param co Output Stream
     */
    public void serialize(OutputStreamBuffer co) {

        //JavaOnlyBlock
        co.write(wrapped.getAddress());

        /*Cpp
        //TODO: ipv6 support
        boost::asio::ip::address_v4::bytes_type raw = wrapped.to_v4().to_bytes();
        for (int i = 0; i < 4; i++) {
            co->writeByte(raw[i]);
        }
         */
    }

    /**
     * Deserialize address from input stream
     *
     * @param ci Input Stream
     * @return Deserialized address
     */
    public static IPAddress deserialize(InputStreamBuffer ci) {

        // JavaOnlyBlock
        byte[] bytes = new byte[4];
        ci.readFully(bytes);
        try {
            return new IPAddress(InetAddress.getByAddress(bytes));
        } catch (java.net.UnknownHostException e) {
            logDomain.log(LogLevel.LL_ERROR, "IPAddress deserialize", e);
            return null;
        }

        /*Cpp
        boost::asio::ip::address_v4::bytes_type raw;
        for (int i = 0; i < 4; i++) {
            raw[i] = ci->readByte();
        }
        return IPAddress(boost::asio::ip::address_v4(raw));
         */

    }

    /**
     * @return IP address of local host
     */
    @InCpp( {"static const IPAddress LOCAL_HOST = getByName(\"localhost\");",
             "return LOCAL_HOST;"
            })
    public static @Const @Ref IPAddress getLocalHost() {
        return LOCAL_HOST;
    }

    public boolean isLocalHost() {
        if (equals(getLocalHost())) {
            return true;
        }

        //JavaOnlyBlock
        byte[] addr = wrapped.getAddress();
        if (addr[0] == 127 && addr[1] == 0 && addr[2] == 0) {
            return true;
        }

        return false;
    }

}

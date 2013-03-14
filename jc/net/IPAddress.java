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

import java.net.InetAddress;

import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;
import org.rrlib.finroc_core_utils.log.LogLevel;
import org.rrlib.finroc_core_utils.serialization.InputStreamBuffer;
import org.rrlib.finroc_core_utils.serialization.OutputStreamBuffer;

/**
 * @author Max Reichardt
 *
 * This class wraps an IP address.
 * It's similar to and inspired from InetAddress in Java.
 */
public class IPAddress {

    /** Wrapped ip address */
    protected InetAddress wrapped;

    /** IP Address of local host */
    private static IPAddress LOCAL_HOST;

    /** Log domain for this class */
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("net");

    static {
        try {
            LOCAL_HOST = new IPAddress(InetAddress.getLocalHost());
        } catch (java.net.UnknownHostException e) {
            logDomain.log(LogLevel.LL_ERROR, "IPAdress static init", e);
        }
    }

    IPAddress(InetAddress wrap) {
        wrapped = wrap;
    }

    /**
     * Resolve IP address by name
     *
     * @param hostname Host name
     * @return Resolved ip address
     */
    public static IPAddress getByName(String hostname) throws UnknownHostException {
        try {
            return new IPAddress(InetAddress.getByName(hostname));
        } catch (java.net.UnknownHostException e) {
            throw new UnknownHostException(e);
        }
    }

    public String toString() {
        return wrapped.getHostAddress().toString();
    }

    public boolean equals(Object other) {
        if (other instanceof IPAddress) {
            return ((IPAddress)other).wrapped.equals(wrapped);
        }
        return false;
    }

    /**
     * Serialize address to output stream
     *
     * @param co Output Stream
     */
    public void serialize(OutputStreamBuffer co) {
        co.write(wrapped.getAddress());
    }

    /**
     * Deserialize address from input stream
     *
     * @param ci Input Stream
     * @return Deserialized address
     */
    public static IPAddress deserialize(InputStreamBuffer ci) {
        byte[] bytes = new byte[4];
        ci.readFully(bytes);
        try {
            return new IPAddress(InetAddress.getByAddress(bytes));
        } catch (java.net.UnknownHostException e) {
            logDomain.log(LogLevel.LL_ERROR, "IPAddress deserialize", e);
            return null;
        }
    }

    /**
     * @return IP address of local host
     */
    public static IPAddress getLocalHost() {
        return LOCAL_HOST;
    }

    public boolean isLocalHost() {
        if (equals(getLocalHost())) {
            return true;
        }

        byte[] addr = wrapped.getAddress();
        if (addr[0] == 127 && addr[1] == 0 && addr[2] == 0) {
            return true;
        }

        return false;
    }

}

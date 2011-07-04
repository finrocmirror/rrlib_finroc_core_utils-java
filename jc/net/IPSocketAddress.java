// Comments, interface and parts of code were generated from InetSocketAddress.java
// from OpenJDK7. TODO: use own implementation only...
// The rest of the implementation is copyright 2009-2010 Max Reichardt.
// InetSocketAddress.java is...
/*
 * Copyright 2000-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package org.rrlib.finroc_core_utils.jc.net;

import org.rrlib.finroc_core_utils.jc.annotation.ConstMethod;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Init;
import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.serialization.InputStreamBuffer;
import org.rrlib.finroc_core_utils.serialization.OutputStreamBuffer;

/**
*
* This class implements an IP Socket Address (IP address + port number)
* It can also be a pair (hostname + port number), in which case an attempt
* will be made to resolve the hostname. If resolution fails then the address
* is said to be <I>unresolved</I> but can still be used on some circumstances
* like connecting through a proxy.
* <p>
* It provides an immutable object used by sockets for binding, connecting, or
* as returned values.
* <p>
* The <i>wildcard</i> is a special local IP address. It usually means "any"
* and can only be used for <code>bind</code> operations.
*
* @see java.net.Socket
* @see java.net.ServerSocket
* @since 1.4
*/
@PassByValue
public class IPSocketAddress {

    /* The hostname of the Socket Address
     * @serial
     */
    private String hostname = "";
    /* The IP address of the Socket Address
     * @serial
     */
    private IPAddress addr;
    /* The port number of the Socket Address
     * @serial
     */
    private int port;

    private static final long serialVersionUID = 5076001401234631237L;

    @Init("hostname()")
    private IPSocketAddress() {
    }

    /**
     *
     * Creates a socket address from an IP address and a port number.
     * <p>
     * A valid port value is between 0 and 65535.
     * A port number of <code>zero</code> will let the system pick up an
     * ephemeral port in a <code>bind</code> operation.
     * <P>
     * A <code>null</code> address will assign the <i>wildcard</i> address.
     * <p>
     * @param   addr    The IP address
     * @param   port    The port number
     * @throws IllegalArgumentException if the port parameter is outside the specified
     * range of valid port values.
     */
    @Init("hostname()")
    public IPSocketAddress(IPAddress addr, int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }
        this.port = port;
        this.addr = addr;
        if (noAddress()) {
            //this.addr = IPAddress.anyLocalAddress();
            throw new IllegalArgumentException("Addr must not be null");
        }
    }

    /**
     *
     * Creates a socket address from a hostname and a port number.
     * <p>
     * An attempt will be made to resolve the hostname into an InetAddress.
     * If that attempt fails, the address will be flagged as <I>unresolved</I>.
     * <p>
     * If there is a security manager, its <code>checkConnect</code> method
     * is called with the host name as its argument to check the permissiom
     * to resolve it. This could result in a SecurityException.
     * <P>
     * A valid port value is between 0 and 65535.
     * A port number of <code>zero</code> will let the system pick up an
     * ephemeral port in a <code>bind</code> operation.
     * <P>
     * @param   hostname the Host name
     * @param   port    The port number
     * @throws IllegalArgumentException if the port parameter is outside the range
     * of valid port values, or if the hostname parameter is <TT>null</TT>.
     * @throws SecurityException if a security manager is present and
     *                           permission to resolve the host name is
     *                           denied.
     * @see     #isUnresolved()
     */
    @Init("hostname()")
    public IPSocketAddress(String hostname, int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }

        //JavaOnlyBlock
        if (hostname == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }

        try {
            addr = IPAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            this.hostname = hostname;

            //JavaOnlyBlock
            if (hostname == null) {
                hostname = "";
            }
            addr = null;

        }
        this.port = port;
    }

    /**
     *
     * Creates an unresolved socket address from a hostname and a port number.
     * <p>
     * No attempt will be made to resolve the hostname into an InetAddress.
     * The address will be flagged as <I>unresolved</I>.
     * <p>
     * A valid port value is between 0 and 65535.
     * A port number of <code>zero</code> will let the system pick up an
     * ephemeral port in a <code>bind</code> operation.
     * <P>
     * @param   host    the Host name
     * @param   port    The port number
     * @throws IllegalArgumentException if the port parameter is outside
     *                  the range of valid port values, or if the hostname
     *                  parameter is <TT>null</TT>.
     * @see     #isUnresolved()
     * @return  a <code>InetSocketAddress</code> representing the unresolved
     *          socket address
     * @since 1.5
     */
    @Init("hostname()")
    public static IPSocketAddress createUnresolved(String host, int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range:" + port);
        }

        //JavaOnlyBlock
        if (host == null) {
            throw new IllegalArgumentException("hostname can't be null");
        }

        IPSocketAddress s = new IPSocketAddress();
        s.port = port;
        s.hostname = host;

        //JavaOnlyBlock
        if (s.hostname == null) {
            s.hostname = "";
        }
        s.addr = null;

        return s;
    }

    /**
     * Gets the port number.
     *
     * @return the port number.
     */
    public final int getPort() {
        return port;
    }

    /**
     *
     * Gets the <code>InetAddress</code>.
     *
     * @return the InetAdress or <code>null</code> if it is unresolved.
     */
    public final IPAddress getAddress() {
        return addr;
    }

    /**
     * Checks whether the address has been resolved or not.
     *
     * @return <code>true</code> if the hostname couldn't be resolved into
     *          an <code>InetAddress</code>.
     */
    @ConstMethod public final boolean isUnresolved() {
        return noAddress();
    }

    /**
     * Constructs a string representation of this InetSocketAddress.
     * This String is constructed by calling toString() on the InetAddress
     * and concatenating the port number (with a colon). If the address
     * is unresolved then the part before the colon will only contain the hostname.
     *
     * @return  a string representation of this object.
     */
    public String toString() {
        if (isUnresolved()) {
            return hostname + ":" + port;
        } else {
            return addr.toString() + ":" + port;
        }
    }

    /**
     * Compares this object against the specified object.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and it represents the same address as
     * this object.
     * <p>
     * Two instances of <code>InetSocketAddress</code> represent the same
     * address if both the InetAddresses (or hostnames if it is unresolved) and port
     * numbers are equal.
     * If both addresses are unresolved, then the hostname & the port number
     * are compared.
     *
     * Note: Hostnames are case insensitive. e.g. "FooBar" and "foobar" are
     * considered equal.
     *
     * @param   obj   the object to compare against.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     * @see java.net.InetAddress#equals(java.lang.Object)
     */
    public final boolean equals(Object obj) {
        //JavaOnlyBlock
        if (obj == null || !(obj instanceof IPSocketAddress))
            return false;

        /*Cpp
        if (!(typeid(obj) == typeid(IPSocketAddress))) {
            return false;
        }
        */

        IPSocketAddress sockAddr = (IPSocketAddress) obj;
        boolean sameIP = false;
        if (!noAddress())
            sameIP = this.addr.equals(sockAddr.addr);
        else if (this.hostname.length() > 0)
            sameIP = (sockAddr.noAddress()) &&
                     this.hostname.equalsIgnoreCase(sockAddr.hostname);
        else
            sameIP = (sockAddr.noAddress()) && (sockAddr.hostname.length() == 0);
        return sameIP && (this.port == sockAddr.port);
    }

    /**
     * Returns a hashcode for this socket address.
     *
     * @return  a hash code value for this socket address.
     */
    public final int hashCode() {
        if (!noAddress())
            return addr.hashCode() + port;

        //JavaOnlyBlock
        if (hostname.length() > 0)
            return hostname.toLowerCase().hashCode() + port;

        /*Cpp
        if (hostname.length() > 0)
            return hostname.toLowerCase().hashCode() + port;
         */

        return port;
    }

    /**
     * Serialize address to output stream
     *
     * @param co Output Stream
     */
    public void serialize(OutputStreamBuffer co) {
        addr.serialize(co);
        co.writeInt(port);
    }

    /**
     * Deserialize address from intput stream
     *
     * @param ci Input Stream
     * @return Deserialized address
     */
    public static IPSocketAddress deserialize(InputStreamBuffer ci) {
        IPAddress addr = IPAddress.deserialize(ci);
        int port = ci.readInt();
        return new IPSocketAddress(addr, port);
    }

    /**
     * @return Has address not been set yet?
     */
    @InCpp("return !addr.isSet();")
    @ConstMethod private boolean noAddress() {
        return addr == null;
    }
}
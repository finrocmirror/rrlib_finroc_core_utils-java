/**
 * You received this file as part of RRLib serialization
 *
 * Copyright (C) 2008-2011 Max Reichardt,
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
package org.rrlib.finroc_core_utils.serialization;

import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.ConstMethod;
import org.rrlib.finroc_core_utils.jc.annotation.CppDelegate;
import org.rrlib.finroc_core_utils.jc.annotation.CppName;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.rtti.DataType;
import org.rrlib.finroc_core_utils.rtti.DataTypeBase;
import org.rrlib.finroc_core_utils.xml.XMLNode;

/**
 * @author max
 *
 * Classes that implement this interface can be serialized and deserialized from
 * a binary stream very efficiently (without unnecessary object allocation)
 *
 * Classes that cannot implement this interface should overload the >> and << operators.
 */
@JavaOnly @CppDelegate(RRLibSerializableImpl.class) @CppName("Serializable")
public interface RRLibSerializable {

    /** Data type of this class */
    @Const public final static DataTypeBase TYPE = new DataType<RRLibSerializable>(RRLibSerializable.class);

    /**
     * @param os Stream to serialize object to
     */
    @ConstMethod public void serialize(@Ref OutputStreamBuffer os);

    /**
     * Deserialize object. Object has to exists already.
     * Should be suitable for reusing old objects.
     *
     * @param is Stream to deserialize from
     */
    public void deserialize(@Ref InputStreamBuffer is);

    /**
     * Serialize object as string (e.g. for xml output)
     *
     * @param os String output stream
     */
    @ConstMethod public void serialize(@Ref StringOutputStream os);

    /**
     * Deserialize object. Object has to exists already.
     * Should be suitable for reusing old objects.
     *
     * Parsing errors should throw an Exception - and set object to
     * sensible (default?) value
     *
     * @param s String stream to deserialize from
     */
    public void deserialize(@Ref StringInputStream is) throws Exception;

    /**
     * Serialize object to XML
     *
     * @param node XML node (name shouldn't be changed, attributes "name" and "type" neither)
     */
    @ConstMethod public void serialize(@Ref XMLNode node) throws Exception;

    /**
     * Deserialize from XML Node
     *
     * @param node Node to deserialize from
     */
    public void deserialize(@Const @Ref XMLNode node) throws Exception;

    /**
     * Empty String List
     */
    @JavaOnly
    public class EmptySerialiazble extends RRLibSerializableImpl {

        public final static DataType<EmptySerialiazble> TYPE = new DataType<EmptySerialiazble>(EmptySerialiazble.class, "Empty Serializable");

        @Override
        public void serialize(OutputStreamBuffer os) {
        }

        @Override
        public void deserialize(InputStreamBuffer is) {
        }
    }
}

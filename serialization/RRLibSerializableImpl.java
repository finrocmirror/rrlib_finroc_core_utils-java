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
import org.rrlib.finroc_core_utils.jc.annotation.CppFilename;
import org.rrlib.finroc_core_utils.jc.annotation.CppName;
import org.rrlib.finroc_core_utils.jc.annotation.InCppFile;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.Superclass;
import org.rrlib.finroc_core_utils.jc.annotation.Virtual;
import org.rrlib.finroc_core_utils.jc.log.LogUser;
import org.rrlib.finroc_core_utils.xml.XMLNode;

/**
 * @author max
 *
 * Default implementation of Serializable
 */
@CppName("Serializable") @CppFilename("Serializable")
@Superclass( {})
public abstract class RRLibSerializableImpl extends LogUser implements RRLibSerializable {

    /**
     * @param os Stream to serialize object to
     */
    @Override @Virtual @ConstMethod
    public abstract void serialize(@Ref OutputStreamBuffer os);

    //Cpp virtual ~Serializable() {}

    /**
     * Deserialize object. Object has to already exists.
     * Should be suited for reusing old objects.
     *
     * @param readView Stream to deserialize from
     */
    @Override @Virtual
    public abstract void deserialize(@Ref InputStreamBuffer is);

    /**
     * Serialize object as string (e.g. for xml output)
     *
     * @param os String output stream
     */
    @Override @InCppFile @Virtual @ConstMethod
    public void serialize(@Ref StringOutputStream os) {
        Serialization.serializeToHexString(this, os);
    }

    /**
     * Deserialize object. Object has to already exists.
     * Should be suited for reusing old objects.
     *
     * Parsing errors should throw an Exception - and set object to
     * sensible (default?) value
     *
     * @param s String to deserialize from
     */
    @Override @InCppFile @Virtual
    public void deserialize(@Ref StringInputStream s) throws Exception {
        Serialization.deserializeFromHexString(this, s);
    }

    /**
     * Serialize object to XML
     *
     * @param node Empty XML node (name shouldn't be changed)
     */
    @Override @InCppFile @Virtual @ConstMethod
    public void serialize(@Ref XMLNode node) throws Exception {
        node.setContent(Serialization.serialize(this));
    }

    /**
     * Deserialize from XML Node
     *
     * @param node Node to deserialize from
     */
    @Override @InCppFile @Virtual
    public void deserialize(@Const @Ref XMLNode node) throws Exception {
        StringInputStream is = new StringInputStream(node.hasTextContent() ? node.getTextContent() : "");
        deserialize(is);
    }
}

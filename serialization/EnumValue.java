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
package org.rrlib.finroc_core_utils.serialization;

import org.finroc.core.portdatabase.CCType;
import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.ConstMethod;
import org.rrlib.finroc_core_utils.jc.annotation.HAppend;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.PostInclude;
import org.rrlib.finroc_core_utils.jc.annotation.Ref;
import org.rrlib.finroc_core_utils.jc.annotation.Superclass;
import org.rrlib.finroc_core_utils.jc.log.LogDefinitions;
import org.rrlib.finroc_core_utils.log.LogDomain;

/**
 * @author max
 *
 * Generic enum value.
 * Currently only meant for use in structure parameters.
 * (In port-classes it's probably better to wrap port classes)
 */
@JavaOnly
@Superclass( {RRLibSerializable.class, CCType.class})
@PostInclude("rrlib/serialization/DataType.h")
@HAppend( {"extern template class ::rrlib::serialization::DataType<finroc::core::EnumValue>;"})
public class EnumValue extends RRLibSerializableImpl implements Copyable<EnumValue> {

    /** Data Type of current enum value */
    public Class <? extends Enum<? >> enumClass = null;

    /** Current wrapped enum value */
    private Enum<?> value;

    /** Log domain for serialization */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"enum\");")
    public static final LogDomain logDomain = LogDefinitions.finroc.getSubDomain("enum");

    public EnumValue(Class <? extends Enum<? >> enumClass) {
        this.enumClass = enumClass;
    }

    public EnumValue() {}

    @Override
    public void serialize(OutputStreamBuffer os) {
        os.writeInt(value.ordinal());
    }

    @Override
    public void deserialize(InputStreamBuffer is) {
        value = (Enum<?>)enumClass.getEnumConstants()[is.readInt()];
    }

    @Override
    public void serialize(StringOutputStream sb) {
        sb.append(value);
    }

    @Override
    public void deserialize(StringInputStream is) throws Exception {
        value = (Enum<?>)is.readEnum(enumClass);
    }

    /**
     * @param e new Value (as integer)
     */
    public void set(Enum<?> e) {
        value = e;
    }

    /**
     * @return current value
     */
    @ConstMethod public Enum<?> get() {
        return value;
    }

    @Override
    public void copyFrom(@Const @Ref EnumValue source) {
        value = source.value;
        enumClass = source.enumClass;
    }

    /**
     * Perform "natural" formatting on enum name (see C++ enum_strings_builder)
     *
     * @param enumConstant Enum constant string to format
     * @return Naturally formatted string
     */
    public static String doNaturalFormatting(String enumConstant) {
        String[] parts = enumConstant.split("_");
        String result = "";
        for (String part : parts) {
            part = part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase();
            result += part + " ";
        }
        return result.trim();
    }

}

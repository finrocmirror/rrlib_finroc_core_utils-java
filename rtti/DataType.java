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
package org.rrlib.finroc_core_utils.rtti;

import java.lang.reflect.Modifier;

import org.rrlib.finroc_core_utils.jc.annotation.AtFront;
import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.CppInclude;
import org.rrlib.finroc_core_utils.jc.annotation.HAppend;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.InCppFile;
import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.IncludeClass;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.Ptr;
import org.rrlib.finroc_core_utils.jc.annotation.RawTypeArgs;
import org.rrlib.finroc_core_utils.jc.annotation.SkipArgs;
import org.rrlib.finroc_core_utils.serialization.EnumValue;
import org.rrlib.finroc_core_utils.serialization.InputStreamBuffer;
import org.rrlib.finroc_core_utils.serialization.OutputStreamBuffer;
import org.rrlib.finroc_core_utils.serialization.PortDataListImpl;
import org.rrlib.finroc_core_utils.serialization.RRLibSerializable;
import org.rrlib.finroc_core_utils.serialization.Serialization;

/**
 * @author max
 *
 * Objects of this class contain info about the data type T
 */
@Include( {"detail/tListInfo.h", "<boost/type_traits/has_virtual_destructor.hpp>", "<boost/type_traits/remove_reference.hpp>", "CustomTypeInitialization.h", "StlContainerSuitable.h", "<cstring>", "sStaticFactory.h"})
@IncludeClass(GenericObjectManager.class)
@CppInclude("GenericObjectInstance.h")
@RawTypeArgs
@HAppend( {
    "template <>",
    "class DataType<detail::Nothing> : public DataTypeBase {",
    "  public:",
    "  DataType() : DataTypeBase(NULL) {}",
    "  static DataTypeInfoRaw* getDataTypeInfo() { return NULL; }",
    "};\n",
    "extern template class DataType<MemoryBuffer>;",
    "extern template class DataType<int8_t>;",
    "extern template class DataType<int16_t>;",
    "extern template class DataType<int>;",
    "extern template class DataType<long int>;",
    "extern template class DataType<long long int>;",
    "extern template class DataType<uint8_t>;",
    "extern template class DataType<uint16_t>;",
    "extern template class DataType<unsigned int>;",
    "extern template class DataType<unsigned long int>;",
    "extern template class DataType<unsigned long long int>;",
    "extern template class DataType<double>;",
    "extern template class DataType<float>;",
    "extern template class DataType<bool>;",
})
public class DataType<T> extends DataTypeBase {

    /**
     * Data type info with factory functions
     */
    @AtFront
    static class DataTypeInfo extends DataTypeInfoRaw {

        @JavaOnly DataTypeBase dataType;

        @JavaOnly public DataTypeInfo(Class<?> c) {
            type = Type.PLAIN;
            javaClass = c;
            name = c.getSimpleName();
        }

        @JavaOnly public DataTypeInfo(DataTypeBase e, Type type) {
            this.type = type;
            this.elementType = e;
            if (type == Type.LIST) {
                name = "List<" + e.getName() + ">";
            } else if (type == Type.PTR_LIST) {
                name = "List<" + e.getName() + "*>";
            } else {
                throw new RuntimeException("Unsupported");
            }
        }

        public DataTypeInfo() {

            //JavaOnlyBlock
            throw new RuntimeException("do not call in Java");

            /*Cpp
            type = detail::ListInfo<T>::type;
            rttiName = typeid(T).name();
            size = sizeof(T);
            name = detail::ListInfo<T>::getName();
            */
        }

        /*Cpp
        template <typename Q = T>
        void initImpl(typename boost::enable_if_c<boost::is_base_of<CustomTypeInitialization, Q>::value, CustomTypeInitialization*>::type d) {
            T::customTypeInitialization(DataTypeBase(this), (T*)NULL);
        }

        void initImpl(void* d) {}

        template <bool B>
        typename boost::enable_if_c<B, DataTypeBase::DataTypeInfoRaw*>::type getListTypeInfo() {
            return DataType<typename detail::ListInfo<T>::ListType>::getDataTypeInfo();
        }

        template <bool B>
        typename boost::disable_if_c<B, DataTypeBase::DataTypeInfoRaw*>::type getListTypeInfo() {
            return NULL;
        }

        template <bool B>
        typename boost::enable_if_c<B, DataTypeBase::DataTypeInfoRaw*>::type getSharedPtrListTypeInfo() {
            return DataType<typename detail::ListInfo<T>::SharedPtrListType>::getDataTypeInfo();
        }

        template <bool B>
        typename boost::disable_if_c<B, DataTypeBase::DataTypeInfoRaw*>::type getSharedPtrListTypeInfo() {
            return NULL;
        }


        virtual void init()
        {
            if (type == ePLAIN) {
                listType = getListTypeInfo<StlContainerSuitable<T>::value >();
                sharedPtrListType = getSharedPtrListTypeInfo<CreateSharedPtrListType<T>::value >();
            } else {
                elementType = DataType<typename detail::ListInfo<T>::ElementType>::getDataTypeInfo();
            }
            initImpl((T*)NULL);
        }
        */

        @SuppressWarnings( { "rawtypes", "unchecked" })
        @Override
        @InCpp( {
            "if (placement == NULL) {",
            "    placement = operator _new(sizeof(T));",
            "}",
            "_memset(placement, 0, sizeof(T)); // set memory to 0 so that memcmp on class T can be performed cleanly for certain types",
            "return _sStaticFactory<T>::create(placement);"
        })
        public Object createInstance(int placement) {
            Object result = null;
            if (dataType.getType() == Type.LIST || dataType.getType() == Type.PTR_LIST) {
                return new PortDataListImpl(dataType.getElementType());
            }

            try {
                if (javaClass.isEnum()) {
                    return new EnumValue((Class <? extends Enum<? >>)javaClass);
                } else if (!(javaClass.isInterface() || Modifier.isAbstract(javaClass.getModifiers()))) {
                    result = javaClass.newInstance();
                } else { // whoops we have an interface - look for inner class that implements interface
                    for (Class<?> cl : javaClass.getDeclaredClasses()) {
                        if (javaClass.isAssignableFrom(cl)) {
                            result = cl.newInstance();
                            break;
                        }
                    }
                    if (result == null) {
                        throw new RuntimeException("Interface and no suitable inner class");
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result;
        }

        @SuppressWarnings( { "unchecked", "rawtypes" })
        @Override @InCppFile
        @InCpp( {
            "assert(sizeof(GenericObjectInstance<T>) <= GenericObject::MANAGER_OFFSET);",
            "while (managerSize %8 != 0) { managerSize++; }",
            "size_t obj_offset = GenericObject::MANAGER_OFFSET + managerSize;",
            "size_t size = obj_offset + sizeof(T);",
            "if (placement == NULL) {",
            "  placement = operator _new(size);",
            "}",
            "char* obj_addr = ((char*)placement) + obj_offset;",
            "_memset(obj_addr, 0, sizeof(T)); // set memory to 0 so that memcmp on class T can be performed cleanly for certain types",
            "T* data_new = _sStaticFactory<T>::create(obj_addr);",
            "return _new (placement) GenericObjectInstance<T>(data_new);"
        })
        public GenericObject createInstanceGeneric(int placement, int managerSize) {
            return new GenericObjectInstance((RRLibSerializable)createInstance(placement), dataType, null);
        }

        @Override
        public void deepCopy(Object src, Object dest, @Ptr Factory f) {

            @Const @Ptr @InCpp("const T* s = static_cast<const T*>(src);")
            RRLibSerializable s = (RRLibSerializable)src;
            @Ptr @InCpp("T* d = static_cast<T*>(dest);")
            RRLibSerializable d = (RRLibSerializable)dest;

            /*Cpp
            if (boost::has_virtual_destructor<T>::value) {
              assert(typeid(*s).name() == typeid(T).name());
              assert(typeid(*d).name() == typeid(T).name());
            }
            */

            Serialization.deepCopy(s, d, f);
        }

        @InCpp( {"const T* s = static_cast<const T*>(obj);",
                 "if (boost::has_virtual_destructor<T>::value) { assert(typeid(*s).name() == typeid(T).name()); }",
                 "os << *s;"
                })
        @Override
        public void serialize(OutputStreamBuffer os, Object obj) {
            ((RRLibSerializable)obj).serialize(os);
        }

        @InCpp( {"T* s = static_cast<T*>(obj);",
                 "if (boost::has_virtual_destructor<T>::value) { assert(typeid(*s).name() == typeid(T).name()); }",
                 "is >> *s;"
                })
        @Override
        public void deserialize(InputStreamBuffer is, Object obj) {
            ((RRLibSerializable)obj).deserialize(is);
        }
    }

    @JavaOnly @SkipArgs( {"1"})
    public DataType(Class<T> c) {
        this(c, null);
    }

    @JavaOnly @SkipArgs( {"1"})
    public DataType(Class<?> c, String name) {
        this(c, name, true);
    }

    @SuppressWarnings("rawtypes")
    @JavaOnly @SkipArgs( {"1"})
    public DataType(Class<?> c, String name, boolean createListTypes) {
        super(getDataTypeInfo(c));
        if (name != null) {
            info.setName(name);
        }
        ((DataTypeInfo)info).dataType = this;

        if (createListTypes && ((DataTypeInfo)info).type == Type.PLAIN && info.listType == null) {
            info.listType = new DataType(this, Type.LIST);
            info.sharedPtrListType = new DataType(this, Type.PTR_LIST);
        }
    }

    /**
     * Constructor for list types
     */
    @JavaOnly
    private DataType(DataTypeBase e, Type t) {
        super(new DataTypeInfo(e, t));
        ((DataTypeInfo)info).dataType = this;
    }

    @JavaOnly
    private static DataTypeInfoRaw getDataTypeInfo(Class<?> c) {
        DataTypeBase dt = findType(c);
        if (dt != null) {
            return dt.info;
        }
        return new DataTypeInfo(c);
    }

    /*Cpp

    public:
    DataType() : DataTypeBase(getDataTypeInfo()) {
        this->getElementType();
        this->getListType();
        this->getSharedPtrListType();
    }

    // \param name Name data type should get (if different from default)
    DataType(const std::string& name) : DataTypeBase(getDataTypeInfo()) {
        getDataTypeInfo()->setName(name);
        this->getElementType();
        this->getListType();
        this->getSharedPtrListType();
    }

    // Lookup data type by rtti name
    // Tries T first
    //
    // \param rttiName rtti name
    // \return Data type with specified name (== NULL if it could not be found)
    static DataTypeBase findTypeByRtti(const char* rttiName) {
        if (rttiName == getDataTypeInfo()->rttiName) {
            return DataType();
        }
        return DataTypeBase::findTypeByRtti(rttiName);
    }

    // \return DataTypeInfo for this type T
    static DataTypeInfoRaw* getDataTypeInfo() {
        static DataTypeInfo info;
        return &info;
    }
    */

}

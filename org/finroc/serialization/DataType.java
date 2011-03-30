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
package org.finroc.serialization;

import java.lang.reflect.Modifier;

import org.finroc.jc.annotation.AtFront;
import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.CppInclude;
import org.finroc.jc.annotation.CppPrepend;
import org.finroc.jc.annotation.HAppend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.InCppFile;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.IncludeClass;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.RawTypeArgs;
import org.finroc.jc.annotation.SkipArgs;

/**
 * @author max
 *
 * Objects of this class contain info about the data type T
 */
@Include( {"detail/tListInfo.h", "<boost/type_traits/has_virtual_destructor.hpp>", "<boost/type_traits/remove_reference.hpp>", "CustomTypeInitialization.h", "StlContainerSuitable.h"})
@IncludeClass(GenericObjectManager.class)
@CppInclude("GenericObjectInstance.h")
@RawTypeArgs
@HAppend( {
    "template <>",
    "class DataType<detail::Nothing> : public DataTypeBase {",
    "  public:",
    "  DataType() : DataTypeBase(NULL) {}",
    "  static DataTypeInfoRaw* getDataTypeInfo() { return NULL; }",
    "};"
})
@CppPrepend( {
    "namespace detail {",
    "template <typename T, size_t _MSIZE>",
    "GenericObject* createInstanceGeneric(void* placement) {",
    "  size_t size = sizeof(GenericObjectInstance<T, GenericObjectManagerPlaceHolder<_MSIZE> >);",
    "  if (placement == NULL) {",
    "    placement = operator _new(size);",
    "  }",
    "  _memset(placement, 0, size); // set memory to 0 so that memcmp on class T can be performed cleanly for certain types",
    "  return _new (placement) GenericObjectInstance<T, GenericObjectManagerPlaceHolder<_MSIZE> >();",
    "}}"
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
        void initImpl(CustomTypeInitialization* d) {
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

        virtual void init()
        {
            if (type == ePLAIN) {
                listType = getListTypeInfo<StlContainerSuitable<T>::value >();
                sharedPtrListType = DataType<typename detail::ListInfo<T>::SharedPtrListType>::getDataTypeInfo();
            } else {
                elementType = DataType<typename detail::ListInfo<T>::ElementType>::getDataTypeInfo();
            }
            initImpl((T*)NULL);
        }
        */

        @SuppressWarnings("rawtypes")
        @Override
        @InCpp( {
            "if (placement == NULL) {",
            "    placement = operator _new(sizeof(T));",
            "}",
            "_memset(placement, 0, sizeof(T)); // set memory to 0 so that memcmp on class T can be performed cleanly for certain types",
            "return _new (placement) T();"
        })
        public Object createInstance(int placement) {
            Object result = null;
            if (dataType.getType() == Type.LIST || dataType.getType() == Type.PTR_LIST) {
                return new PortDataListImpl(dataType.getElementType());
            }

            try {
                if (!(javaClass.isInterface() || Modifier.isAbstract(javaClass.getModifiers()))) {
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
            "if (managerSize <= 8) { return detail::createInstanceGeneric<T, 8>(placement); }",
            "else if (managerSize <= 16) { return detail::createInstanceGeneric<T, 16>(placement); }",
            "else if (managerSize <= 24) { return detail::createInstanceGeneric<T, 24>(placement); }",
            "else if (managerSize <= 32) { return detail::createInstanceGeneric<T, 32>(placement); }",
            "else if (managerSize <= 40) { return detail::createInstanceGeneric<T, 40>(placement); }",
            "else if (managerSize <= 48) { return detail::createInstanceGeneric<T, 48>(placement); }",
            "else if (managerSize <= 56) { return detail::createInstanceGeneric<T, 56>(placement); }",
            "else if (managerSize <= 64) { return detail::createInstanceGeneric<T, 64>(placement); }",
            "else if (managerSize <= 72) { return detail::createInstanceGeneric<T, 72>(placement); }",
            "else if (managerSize <= 80) { return detail::createInstanceGeneric<T, 80>(placement); }",
            "else if (managerSize <= 88) { return detail::createInstanceGeneric<T, 88>(placement); }",
            "else if (managerSize <= 96) { return detail::createInstanceGeneric<T, 96>(placement); }",
            "else if (managerSize <= 104) { return detail::createInstanceGeneric<T, 104>(placement); }",
            "else if (managerSize <= 112) { return detail::createInstanceGeneric<T, 112>(placement); }",
            "else if (managerSize <= 120) { return detail::createInstanceGeneric<T, 120>(placement); }",
            "else { throw std::invalid_argument(\"Management info larger than 120 bytes not allowed\"); }",
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

    @SuppressWarnings("rawtypes")
    @JavaOnly @SkipArgs( {"1"})
    public DataType(Class<?> c, String name) {
        super(getDataTypeInfo(c));
        if (name != null) {
            info.setName(name);
        }
        ((DataTypeInfo)info).dataType = this;

        if (((DataTypeInfo)info).type == Type.PLAIN && info.listType == null) {
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
    DataType() : DataTypeBase(getDataTypeInfo()) {}

    // \param name Name data type should get (if different from default)
    DataType(const std::string& name) : DataTypeBase(getDataTypeInfo()) {
        getDataTypeInfo()->setName(name);
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

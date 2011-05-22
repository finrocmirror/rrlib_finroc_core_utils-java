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

import java.util.ArrayList;
import java.util.HashMap;

import org.finroc.jc.AtomicInt;
import org.finroc.jc.HasDestructor;
import org.finroc.jc.annotation.AtFront;
import org.finroc.jc.annotation.Const;
import org.finroc.jc.annotation.ConstMethod;
import org.finroc.jc.annotation.CppDefault;
import org.finroc.jc.annotation.CppInclude;
import org.finroc.jc.annotation.CppType;
import org.finroc.jc.annotation.ForwardDecl;
import org.finroc.jc.annotation.HAppend;
import org.finroc.jc.annotation.InCpp;
import org.finroc.jc.annotation.InCppFile;
import org.finroc.jc.annotation.Include;
import org.finroc.jc.annotation.Inline;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.Managed;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.Ptr;
import org.finroc.jc.annotation.Ref;
import org.finroc.jc.annotation.SizeT;
import org.finroc.jc.annotation.SkipArgs;
import org.finroc.jc.annotation.VoidPtr;
import org.finroc.jc.log.LogDefinitions;
import org.finroc.log.LogDomain;
import org.finroc.log.LogLevel;

/**
 * @author max
 *
 * Untyped base class for all data types.
 *
 * Assigns unique type-id to each data type.
 * Can be used as factory for data types (necessary for deserializing)
 * vectors containing pointers).
 *
 * A instance of DataType<T> must be created for each type T
 * this mechanism should work with.
 *
 * This class is passed by value
 */
@PassByValue
@ForwardDecl( {GenericObjectManager.class, DataTypeAnnotation.class})
@Include( {"<boost/type_traits/is_base_of.hpp>", "<mutex>", "rrlib/logging/definitions.h"})
@CppInclude( {"<cstring>", "sSerialization.h", "DataTypeAnnotation.h"})
@HAppend( {"template <typename T>",
           "int DataTypeBase::AnnotationIndex<T>::index;"
          })
public class DataTypeBase {

    /** type of data type */
    @AtFront
    enum Type {
        PLAIN, // Plain type
        LIST, // List of objects of same type
        PTR_LIST, // List of objects with possibly objects of different types
        NULL, // Null type
        OTHER, // Other data type
        UNKNOWN // Unknown data type in current process
    }

    /**
     * Maximum number of annotations
     */
    @JavaOnly
    private static final @SizeT int MAX_ANNOTATIONS = 10;

    //Cpp static const size_t MAX_ANNOTATIONS = 10;

    /** Data type info */
    @AtFront @Ptr
    static public class DataTypeInfoRaw implements HasDestructor {

        /** Type of data type */
        public Type type;

        /** Name of data type */
        public @CppType("std::string") String name;

        /*Cpp
        // RTTI name
        const char* rttiName;

        // sizeof(T)
        size_t size;
        */

        /** New info? */
        boolean newInfo = true;

        /*! Is this the default name? - then it may be changed */
        public boolean defaultName = true;

        /** Data type uid */
        public short uid = -1;

        /** Java Class */
        @JavaOnly
        public Class<?> javaClass;

        /** In case of list: type of elements */
        @CppType("DataTypeInfoRaw") @Ptr
        public DataTypeBase elementType;

        /** In case of element: list type (std::vector<T>) */
        @CppType("DataTypeInfoRaw") @Ptr
        public DataTypeBase listType;

        /** In case of element: shared pointer list type (std::vector<std::shared_ptr<T>>) */
        @CppType("DataTypeInfoRaw") @Ptr
        public DataTypeBase sharedPtrListType;

        /** Annotations to data type */
        @InCpp("DataTypeAnnotation* annotations[MAX_ANNOTATIONS];")
        public DataTypeAnnotation[] annotations = new DataTypeAnnotation[MAX_ANNOTATIONS];

        @JavaOnly
        public DataTypeInfoRaw() {
            defaultName = true;
        }

        /*Cpp
        DataTypeInfoRaw() :
            type(ePLAIN),
            name(),
            rttiName(NULL),
            size(-1),
            newInfo(true),
            defaultName(true),
            uid(-1),
            elementType(NULL),
            listType(NULL),
            sharedPtrListType(NULL)
        {
            for (size_t i = 0; i < MAX_ANNOTATIONS; i++) {
                annotations[i] = NULL;
            }
        }

        virtual void init() {}
         */

        /**
         * Set name of data type
         * (only valid if still default == not set before)
         *
         * @param newName New name of type
         */
        public void setName(@Const @Ref @CppType("std::string") String newName) {
            if (!defaultName) {

                //Cpp assert(name._compare(newName) == 0 && "Name already set");

                //JavaOnlyBlock
                assert(name.equals(newName)) : "Name already set";

                return;
            }

            defaultName = false;
            name = newName;

            /*Cpp
            if (listType != NULL) {
                listType->name = std::_string("List<") + name + ">";
            }
            if (sharedPtrListType != NULL) {
                sharedPtrListType->name = std::_string("List<") + name + "*>";
            }
             */
        }

        /**
         * @param placement (Optional) Destination for placement new
         * @return Instance of Datatype T casted to void*
         */
        @ConstMethod public @VoidPtr Object createInstance(@VoidPtr @CppDefault("NULL") int placement) {
            return null;
        }

        /**
         * @param placement (Optional) Destination for placement new
         * @param managerSize Size of management info
         * @return Instance of Datatype as Generic object
         */
        @ConstMethod public @Ptr GenericObject createInstanceGeneric(@VoidPtr @CppDefault("NULL") int placement, @CppDefault("0") @SizeT int managerSize) {
            return null;
        }

        /**
         * Deep copy objects
         *
         * @param src Src object
         * @param dest Destination object
         * @param f Factory to use
         */
        @ConstMethod public void deepCopy(@Const @VoidPtr Object src, @VoidPtr Object dest, @Ptr Factory f) {}

        /**
         * Serialize object to output stream
         *
         * @param os OutputStream
         * @param obj Object to serialize
         */
        @ConstMethod public void serialize(@Ref OutputStreamBuffer os, @Const @VoidPtr Object obj) {}

        /**
         * Deserialize object from input stream
         *
         * @param os InputStream
         * @param obj Object to deserialize
         */
        @ConstMethod public void deserialize(@Ref InputStreamBuffer is, @VoidPtr Object obj) {}

        @Override @InCppFile
        public void delete() {
            /*Cpp
            for (size_t i = 0; i < MAX_ANNOTATIONS; i++) {
                delete annotations[i];
            }
             */
        }
    }

//    /** Maximum number of types */
//    public static final int MAX_TYPES = 2000;

    /** Pointer to data type info (should not be copied every time for efficiency reasons) */
    @Const protected final @Ptr DataTypeInfoRaw info;

    /** List with all registered data types */
    @JavaOnly private static ArrayList<DataTypeBase> types = new ArrayList<DataTypeBase>();

    /** Null type */
    @JavaOnly private static DataTypeBase NULL_TYPE = new DataTypeBase(null);

    /** Log domain for this class */
    @InCpp("_RRLIB_LOG_CREATE_NAMED_DOMAIN(logDomain, \"serialization\");")
    private static final LogDomain logDomain = LogDefinitions.finrocUtil.getSubDomain("serialization");

    /** Lookup for data type annotation index */
    @JavaOnly
    private static final HashMap < Class<?>, Integer > annotationIndexLookup = new HashMap < Class<?>, Integer > ();

    /** Last annotation index that was used */
    @JavaOnly
    private static final AtomicInt lastAnnotationIndex = new AtomicInt(0);

    /**
     * @param name Name of data type
     */
    public DataTypeBase(@Ptr @CppDefault("NULL") DataTypeInfoRaw info) {
        this.info = info;

        if (info != null && info.newInfo == true) {

            //JavaOnlyBlock
            synchronized (types) {
                addType(info);
            }

            /*Cpp
            std::unique_lock<std::recursive_mutex>(getMutex());
            addType(info_);
            info_->init();
             */
        }
    }

    /**
     * Helper for constructor (needs to be called in synchronized context)
     */
    private void addType(@Ptr DataTypeInfoRaw nfo) {
        nfo.uid = (short)getTypes().size();
        getTypes().add(this);
        nfo.newInfo = false;
        @InCpp("std::string msg(\"Adding data type \"); msg += getName();")
        String msg = "Adding data type " + getName();
        logDomain.log(LogLevel.LL_DEBUG_VERBOSE_1, getLogDescription(), msg);
    }

    /*Cpp
    template <typename T>
    struct AnnotationIndex {
        static int index;
    };

    template <typename T>
    bool annotationIndexValid(bool setValid = false) {
        static bool valid = false;
        if (setValid) {
            assert(!valid);
            valid = true;
        }
        return valid;
    }
     */

    private @PassByValue @CppType("const char*") String getLogDescription() {
        return "DataTypeBase";
    }

    /**
     * @return Name of data type
     */
    @ConstMethod @Inline public @Const @Ref @CppType("std::string") String getName() {
        @CppType("static const std::string")
        String unknown = "NULL";
        if (info != null) {
            return info.name;
        }
        return unknown;
    }

    /**
     * @return Number of registered types
     */
    public static short getTypeCount() {
        return (short)getTypes().size();
    }

    /**
     * @return uid of data type
     */
    @ConstMethod @Inline public short getUid() {
        if (info != null) {
            return info.uid;
        }
        return -1;
    }

    /**
     * @return return "Type" of data type (see enum)
     */
    @ConstMethod @Inline public Type getType() {
        if (info != null) {
            return info.type;
        }
        return Type.NULL;
    }

    /**
     * @return In case of element: list type (std::vector<T>)
     */
    @ConstMethod @Inline public DataTypeBase getListType() {
        if (info != null) {

            //JavaOnlyBlock
            return info.listType;

            //Cpp return DataTypeBase(info->listType);
        }
        return getNullType();
    }

    /**
     * @return In case of list: type of elements
     */
    @ConstMethod @Inline public DataTypeBase getElementType() {
        if (info != null) {

            //JavaOnlyBlock
            return info.elementType;

            //Cpp return DataTypeBase(info->elementType);
        }
        return getNullType();
    }

    /**
     * @return In case of element: shared pointer list type (std::vector<std::shared_ptr<T>>)
     */
    @ConstMethod @Inline public DataTypeBase getSharedPtrListType() {
        if (info != null) {

            //JavaOnlyBlock
            return info.sharedPtrListType;

            //Cpp return DataTypeBase(info->sharedPtrListType);
        }
        return getNullType();
    }

    /**
     * @return Java Class of data type
     */
    @JavaOnly @ConstMethod
    public Class<?> getJavaClass() {
        if (info != null) {
            return info.javaClass;
        } else {
            return null;
        }
    }

    /*Cpp
    // \return rtti name of data type
    const char* getRttiName() const {
        if (info != NULL) {
            return info->rttiName;
        } else {
            return typeid(void).name();
        }
    }

    // \return size of data type (as returned from sizeof(T))
    size_t getSize() const {
        if (info != NULL) {
            return info->size;
        } else {
            return 0;
        }
    }

    // for checks against NULL (if (type == NULL) {...} )
    bool operator== (void* infoPtr) const {
        return info == infoPtr;
    }

    bool operator== (const DataTypeBase& other) const {
        return info == other.info;
    }

    bool operator!= (void* infoPtr) const {
        return info != infoPtr;
    }

    bool operator!= (const DataTypeBase& other) const {
        return info != other.info;
    }


    */

    /**
     * Deep copy objects
     *
     * @param src Src object
     * @param dest Destination object
     * @param f Factory to use
     */
    @Inline @ConstMethod public void deepCopy(@Const @VoidPtr Object src, @VoidPtr Object dest, @CppDefault("NULL") @Ptr Factory f) {
        if (info == null) {
            return;
        }
        info.deepCopy(src, dest, f);
    }

    /**
     * Serialize object to output stream
     *
     * @param os OutputStream
     * @param obj Object to serialize
     */
    @Inline @ConstMethod public void serialize(@Ref OutputStreamBuffer os, @Const @VoidPtr Object obj) {
        if (info == null) {
            return;
        }
        info.serialize(os, obj);
    }

    /**
     * Deserialize object from input stream
     *
     * @param os InputStream
     * @param obj Object to deserialize
     */
    @Inline @ConstMethod public void deserialize(@Ref InputStreamBuffer is, @VoidPtr Object obj) {
        if (info == null) {
            return;
        }
        info.deserialize(is, obj);
    }

    /*Cpp
    static std::recursive_mutex& getMutex() {
        static std::recursive_mutex mutex;
        return mutex;
    }
     */

    /**
     * Helper method that safely provides static data type list
     */
    static private @Ref @CppType("std::vector<DataTypeBase>") ArrayList<DataTypeBase> getTypes() {
        //Cpp static std::vector<DataTypeBase> types;
        return types;
    }

    /**
     * Lookup data type for class c
     *
     * @param c Class
     * @return Data type object - null if there's none
     */
    @JavaOnly
    static public DataTypeBase findType(Class<?> c) {
        for (DataTypeBase db : types) {
            if (db.info.javaClass == c) {
                return db;
            }
        }
        return null;
    }

    /**
     * @param uid Data type uid
     * @return Data type with specified uid
     */
    @Inline
    static public DataTypeBase getType(short uid) {
        if (uid == -1) {
            return getNullType();
        }
        return getTypes().get(uid);
    }

    /**
     * Lookup data type by name
     *
     * @param name Data Type name
     * @return Data type with specified name (NULL if it could not be found)
     */
    static public DataTypeBase findType(@Const @Ref @CppType("std::string") String name) {
        @InCpp("bool nulltype = _strcmp(name.c_str(), \"NULL\") == 0;")
        boolean nulltype = name.equals("NULL");
        if (nulltype) {
            return getNullType();
        }

        for (@SizeT int i = 0; i < getTypes().size(); i++) {
            DataTypeBase dt = getTypes().get(i);
            @InCpp("bool eq = name._compare(dt.getName()) == 0;")
            boolean eq = name.equals(dt.getName());
            if (eq) {
                return dt;
            }
        }

        //JavaOnlyBlock
        return null;

        //Cpp return DataTypeBase(NULL);
    }

    /*Cpp
    // Lookup data type by rtti name
    //
    // \param rttiName rtti name
    // \return Data type with specified name (== NULL if it could not be found)
    static DataTypeBase findTypeByRtti(const char* rttiName) {
        for (size_t i = 0; i < getTypes()._size(); i++) {
            if (getTypes()[i].info->rttiName == rttiName) {
                return getTypes()[i];
            }
        }
        return DataTypeBase(NULL);
    }
    */

    /**
     * @param placement (Optional) Destination for placement new
     * @return Instance of Datatype T casted to void*
     */
    @ConstMethod public @VoidPtr Object createInstance(@VoidPtr @CppDefault("NULL") int placement) {
        if (info == null) {
            return null;
        }
        return info.createInstance(placement);
    }

    //Cpp template <typename M = GenericObjectManager>
    /**
     * @param placement (Optional) Destination for placement new
     * @return Instance of Datatype as Generic object
     */
    @Inline
    @ConstMethod @Ptr GenericObject createInstanceGeneric(@VoidPtr @CppDefault("NULL") int placement) {
        if (info == null) {
            return null;
        }

        //JavaOnlyBlock
        return info.createInstanceGeneric(placement, 0);

        /*Cpp
        static const size_t MANAGER_OFFSET = (sizeof(void*) == 4) ? 16 : 24; // must be identical to MANAGER_OFFSET in GenericObject

        static_assert(boost::is_base_of<GenericObjectManager, M>::value, "only GenericObjectManagers allowed as M");
        GenericObject* result = info->createInstanceGeneric(placement, sizeof(M));
        _new (reinterpret_cast<char*>(result) + MANAGER_OFFSET) M();
        return result;
        */
    }

    /**
     * @return Instance of Datatype T casted to void*
     */
    @JavaOnly
    @ConstMethod public @VoidPtr Object createInstance() {
        if (info == null) {
            return null;
        }
        return createInstance(0);
    }

    /**
     * @param manager Manager for generic object
     * @return Instance of Datatype as Generic object
     */
    @JavaOnly
    @ConstMethod public @Ptr GenericObject createInstanceGeneric(GenericObjectManager manager) {
        GenericObject result = createInstanceGeneric(0);
        result.jmanager = manager;

        //JavaOnlyBlock
        if (manager != null) {
            manager.setObject(result);
        }

        return result;
    }

    /**
     * Get uniform data type name from rtti type name
     *
     * @param rtti mangled rtti type name
     * @return Uniform data type name
     */
    public static @CppType("std::string") String getDataTypeNameFromRtti(@PassByValue @CppType("char*") String rtti) {

        //JavaOnlyBlock
        return null;

        /*Cpp
        std::string demangled = _sSerialization::demangle(rtti);

        // remove ::
        long int lastPos = -1;
        for (long int i = demangled._size() - 1; i >= 0; i--) {
            char c = demangled[i];
            if (lastPos == -1) {
                if (c == ':') {
                    lastPos = i + 1;

                    // possibly cut off s or t prefix
                    if (_islower(demangled[lastPos]) && _isupper(demangled[lastPos + 1])) {
                        lastPos++;
                    }
                }
            } else {
                if ((!_isalnum(c)) && c != ':' && c != '_') {
                    // ok, cut off here
                    demangled = demangled._substr(0, i + 1) + demangled._substr(lastPos, demangled._size() - lastPos);
                    lastPos = -1;
                }
            }
        }

        // ok, cut off rest
        if (lastPos > 0) {
            demangled = demangled._substr(lastPos, demangled._size() - lastPos);
        }

        // possibly cut off s or t prefix
        if (_islower(demangled[0]) && _isupper(demangled[1])) {
            demangled._erase(0, 1);
        }

        return demangled;

         */
    }

    /**
     * @return Nulltype
     */
    @InCpp("return DataTypeBase(NULL);")
    public static DataTypeBase getNullType() {
        return NULL_TYPE;
    }

    /**
     * @return DataTypeInfo object
     */
    @ConstMethod @Const public DataTypeInfoRaw getInfo() {
        return info;
    }

    /**
     * Can object of this data type be converted to specified type?
     * (In C++ currently only returns true, when types are equal)
     *
     * @param dataType Other type
     * @return Answer
     */
    @InCpp("return dataType == *this;")
    @ConstMethod public boolean isConvertibleTo(@Const @Ref DataTypeBase dataType) {

        if (dataType == this) {
            return true;
        }
        if (info == null || dataType.info == null) {
            return false;
        }
        if (getType() == Type.UNKNOWN || dataType.getType() == Type.UNKNOWN) {
            return false;
        }
        if (getType() == Type.LIST && dataType.getType() == Type.LIST) {
            return getElementType().isConvertibleTo(dataType.getElementType());
        }
        if ((info.javaClass != null) && (dataType.info.javaClass != null)) {
            return dataType.getInfo().javaClass.isAssignableFrom(info.javaClass);
        }
        return false;
    }

    /**
     * Add annotation to this data type
     *
     * @param ann Annotation
     */
    @Inline
    public <T extends DataTypeAnnotation> void addAnnotation(@Managed T ann) {
        //Cpp std::unique_lock<std::recursive_mutex>(getMutex());
        //Cpp static size_t lastAnnotationIndex = 0;
        if (info != null) {

            assert(ann.annotatedType == null) : "Already used as annotation in other object. Not allowed (double deleteting etc.)";
            ann.annotatedType = this;
            @SizeT int annIndex = -1;

            /*Cpp
            if (!annotationIndexValid<T>()) {
                lastAnnotationIndex++;
                assert(lastAnnotationIndex < MAX_ANNOTATIONS);
                AnnotationIndex<T>::index = lastAnnotationIndex;
                annotationIndexValid<T>(true);
            }
            annIndex = AnnotationIndex<T>::index;
             */

            //JavaOnlyBlock
            synchronized (types) {
                Integer i = annotationIndexLookup.get(ann.getClass());
                if (i == null) {
                    i = lastAnnotationIndex.incrementAndGet();
                    annotationIndexLookup.put(ann.getClass(), i);
                }
                annIndex = i;
            }

            assert(annIndex > 0 && annIndex < MAX_ANNOTATIONS);
            assert(info.annotations[annIndex] == null);

            //JavaOnlyBlock
            info.annotations[annIndex] = ann;

            //Cpp const_cast<DataTypeInfoRaw*>(info)->annotations[annIndex] = ann;
        } else {

            //JavaOnlyBlock
            throw new RuntimeException("Nullptr");

            //Cpp throw std::runtime_error("Null pointer !?");
        }
    }

    /**
     * Get annotation of specified class
     *
     * @param c Class of annotation we're looking for
     * @return Annotation. Null if data type has no annotation of this type.
     */
    @SuppressWarnings("unchecked")
    @SkipArgs("1") @Inline @Ptr
    public <T extends DataTypeAnnotation> T getAnnotation(Class<T> c) {
        if (info != null) {
            //JavaOnlyBlock
            return (T)info.annotations[annotationIndexLookup.get(c)];

            //Cpp return static_cast<T*>(info->annotations[AnnotationIndex<T>::index]);
        } else {

            //JavaOnlyBlock
            throw new RuntimeException("Nullptr");

            //Cpp throw std::runtime_error("Null pointer !?");
        }
    }
}

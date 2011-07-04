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
package org.rrlib.finroc_core_utils.jc.container;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rrlib.finroc_core_utils.jc.annotation.AtFront;
import org.rrlib.finroc_core_utils.jc.annotation.Const;
import org.rrlib.finroc_core_utils.jc.annotation.ConstMethod;
import org.rrlib.finroc_core_utils.jc.annotation.InCpp;
import org.rrlib.finroc_core_utils.jc.annotation.Include;
import org.rrlib.finroc_core_utils.jc.annotation.Init;
import org.rrlib.finroc_core_utils.jc.annotation.Inline;
import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.rrlib.finroc_core_utils.jc.annotation.NoCpp;
import org.rrlib.finroc_core_utils.jc.annotation.NoOuterClass;
import org.rrlib.finroc_core_utils.jc.annotation.PassByValue;
import org.rrlib.finroc_core_utils.jc.annotation.SizeT;

/**
 * @author max
 *
 * This is a concurrent map, available in Java and C++ (intel tbb implementation).
 *
 * An arbitrary number of get operations can be performed in parallel to adding objects.
 */
@Inline @NoCpp
@Include("<tbb/concurrent_hash_map.h>")
/*@CppPrepend({"template<typename K, typename V>",
             "__thread bool ConcurrentMap<K, V>::success = false;"})*/
public class ConcurrentMap<K, V> {

    /*Cpp
    // Did last get operation return something valid ?
    //static __thread bool success;

    // Intel tbb requires a class like that
    class HashCompare {
    public:
        inline bool _equal(const Object& j, const Object& k) const {
            return j.hashCode() == k.hashCode();
        }

        inline size_t _hash(const Object& k) const {
            return k.hashCode();
        }

        inline bool _equal(const int& j, const int& k) const {
            return j == k;
        }

        inline size_t _hash(const int& k) const {
            return k;
        }
    };

    typedef typename ::tbb::concurrent_hash_map<K, V, HashCompare> maptype;
    typedef typename maptype::accessor acctype;
    typedef typename maptype::const_accessor const_acctype;
    typedef typename maptype::const_iterator itertype;
     */
    /**
     * Used to iterate over entries of ConcurrentMap
     */
    @Inline @NoCpp @NoOuterClass @AtFront @PassByValue
    public class MapIterator {

        @JavaOnly
        final Iterator<Map.Entry<K, V>> wrapped;

        @JavaOnly
        Map.Entry<K, V> current = null;

        @JavaOnly
        public MapIterator() {
            wrapped = map.entrySet().iterator();
        }

        /*Cpp
        itertype wrapped;
        const maptype& map;
        bool start;
        bool empty;

        public:
        MapIterator(const maptype& map_) :
            wrapped(map_._begin()),
            map(map_),
            start(true),
            empty(map_._empty())
        {}
        */

        /*Cpp
        inline const K& getKey()
        {
          return (*wrapped).first;
        }

        inline const V& getValue()
        {
          return (*wrapped).second;
        }
        */

        public boolean next() {
            //JavaOnlyBlock
            if (wrapped.hasNext()) {
                current = wrapped.next();
                return true;
            }
            return false;

            /*Cpp
            if (empty) {
                return false;
            } else if(start) {
                start = false;
            } else if(wrapped != map._end()) {
                wrapped++;
            }
            return true;
             */
        }

        @JavaOnly
        public K getKey() {
            return current.getKey();
        }

        @JavaOnly
        public V getValue() {
            return current.getValue();
        }
    }

    /** wrapped internal implementation */
    @InCpp("maptype map;")
    private final ConcurrentHashMap<K, V> map;

    /** Value to return when get() is called with a key that doesn' exist */
    private final V nullValue;

    /*public ConcurrentMap(int initialSize) {
        map = new ConcurrentHashMap<K, V>(initialSize);
    }*/

    @Init("map()")
    public ConcurrentMap(@Const V nullValue) {
        map = new ConcurrentHashMap<K, V>();
        this.nullValue = nullValue;
    }

    /**
     * Retrieve Value with specified Key
     * (getSuccessful may need to be called)
     *
     * @param key Key
     * @return value
     */
    public V get(@Const K key) {
        //JavaOnlyBlock
        V result = map.get(key);
        return result == null ? nullValue : result;

        /*Cpp
        acctype acc;
        bool success = map._find(acc, key);
        return success ? (*acc).second : nullValue;
         */
    }

    /**
     * @param key Key
     * @param value New Value
     */
    public void put(@Const K key, @Const V value) {
        //JavaOnlyBlock
        map.put(key, value);

        /*Cpp
        acctype acc;
        map._insert(acc, key);
        (*acc).second = value;
         */
    }

    /**
     * Delete Object with specified key
     *
     * @param key Key
     * @return The previous value
     */
    public V remove(@Const K key) {
        //JavaOnlyBlock
        return map.remove(key);

        /*Cpp
        acctype acc;
        bool found = map._find(acc, key);
        if (found) {
            V ret = (*acc).second;
            map._erase(acc);
            return ret;
        }
        return NULL;
         */
    }

    /**
     * @return Size of map
     */
    @InCpp("return map._size();")
    @ConstMethod public @SizeT int size() {
        return map.size();
    }

    /**
     * @return Is map empty?
     */
    @InCpp("return map._empty();")
    @ConstMethod public boolean empty() {
        return map.isEmpty();
    }

    /**
     * Does map contain entry with specified key?
     *
     * @param key Key
     * @return Answer
     */
    @ConstMethod public boolean contains(@Const K key) {
        //JavaOnlyBlock
        return map.contains(key);

        /*Cpp
        acctype acc;
        return map._find(acc, key);
         */
    }

    @InCpp("return MapIterator(map);")
    public MapIterator getIterator() {
        return new MapIterator();
    }
}

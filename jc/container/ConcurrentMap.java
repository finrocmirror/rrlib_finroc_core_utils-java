//
// You received this file as part of RRLib
// Robotics Research Library
//
// Copyright (C) Finroc GbR (finroc.org)
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
//
//----------------------------------------------------------------------
package org.rrlib.finroc_core_utils.jc.container;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Max Reichardt
 *
 * This is a concurrent map, available in Java and C++ (intel tbb implementation).
 *
 * An arbitrary number of get operations can be performed in parallel to adding objects.
 */
public class ConcurrentMap<K, V> {

    /**
     * Used to iterate over entries of ConcurrentMap
     */
    public class MapIterator {

        final Iterator<Map.Entry<K, V>> wrapped;

        Map.Entry<K, V> current = null;

        public MapIterator() {
            wrapped = map.entrySet().iterator();
        }

        public boolean next() {
            if (wrapped.hasNext()) {
                current = wrapped.next();
                return true;
            }
            return false;
        }

        public K getKey() {
            return current.getKey();
        }

        public V getValue() {
            return current.getValue();
        }
    }

    /** wrapped internal implementation */
    private final ConcurrentHashMap<K, V> map;

    /** Value to return when get() is called with a key that doesn' exist */
    private final V nullValue;

    public ConcurrentMap(V nullValue) {
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
    public V get(K key) {
        V result = map.get(key);
        return result == null ? nullValue : result;
    }

    /**
     * @param key Key
     * @param value New Value
     */
    public void put(K key, V value) {
        map.put(key, value);
    }

    /**
     * Delete Object with specified key
     *
     * @param key Key
     * @return The previous value
     */
    public V remove(K key) {
        return map.remove(key);
    }

    /**
     * @return Size of map
     */
    public int size() {
        return map.size();
    }

    /**
     * @return Is map empty?
     */
    public boolean empty() {
        return map.isEmpty();
    }

    /**
     * Does map contain entry with specified key?
     *
     * @param key Key
     * @return Answer
     */
    public boolean contains(K key) {
        return map.contains(key);
    }

    public MapIterator getIterator() {
        return new MapIterator();
    }
}

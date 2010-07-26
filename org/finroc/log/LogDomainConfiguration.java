/**
 * You received this file as part of an advanced experimental
 * robotics framework prototype ('finroc')
 *
 * Copyright (C) 2010 Robotics Research Lab, University of Kaiserslautern
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

package org.finroc.log;

import org.finroc.jc.AtomicInt;
import org.finroc.jc.annotation.JavaOnly;

/**
 * tLoggingDomainConfiguration encapsulates the configuration of logging
 * domains in the RRLib logging facility. It therefore stores settings
 * like enabled output fields, min. message level, etc.
 * This class encapsulates the configuration of logging domains
 * Each logging domain has its own configuration consisting of settings
 * like enabled output fields, min. message level, etc.
 * It is a implemented common structure that can be used by instances of
 * tLoggingDomain and tLoggingDomainRegistry. Thus, it should not be
 * instantiated or used by other classes and is declared totally private
 * with the mentioned classes as friends.
 *
 * @author Max Reichardt
 * @author Tobias Föhst
 */
@JavaOnly
public class LogDomainConfiguration {

    final LogLevel DEFAULT_MAX_LOG_LEVEL = LogLevel.LL_DEBUG;   //!< Default max log level for reduced output mode
    final boolean DEFAULT_PRINT_TIME = true;              //!< Default print time setting for reduced output mode
    final boolean DEFAULT_PRINT_NAME = false;              //!< Default print name setting for reduced output mode
    final boolean DEFAULT_PRINT_LEVEL = false;             //!< Default print level setting for reduced output mode
    final boolean DEFAULT_PRINT_LOCATION = false;          //!< Default print location setting for reduced output mode


    String name;
    boolean configureSubTree;

    boolean enabled = true;
    boolean printTime = DEFAULT_PRINT_TIME;
    boolean printName = DEFAULT_PRINT_NAME;
    boolean printLevel = DEFAULT_PRINT_LEVEL;
    boolean printLocation = DEFAULT_PRINT_LOCATION;
    LogLevel maxMessageLevel = DEFAULT_MAX_LOG_LEVEL;
    LogStreamOutput[] streamMask = new LogStreamOutput[] {LogStreamOutput.LS_STDOUT};

    final static AtomicInt streamMaskRevisionGen = new AtomicInt(0);
    volatile int streamMaskRevision = streamMaskRevisionGen.incrementAndGet();

    LogDomainConfiguration(String name) {
        this.name = name;
    }

    LogDomainConfiguration(String name, LogDomainConfiguration other) {
        this.name = name;
        configureSubTree = other.configureSubTree;
        enabled = other.enabled;
        printTime = other.printTime;
        printName = other.printName;
        printLevel = other.printLevel;
        printLocation = other.printLocation;
        maxMessageLevel = other.maxMessageLevel;
        streamMask = other.streamMask;
        //name = other.name;
    }

    /*tLoggingDomainConfiguration &operator = (const tLoggingDomainConfiguration other)
    {
      this->configure_sub_tree = other.configure_sub_tree;
      this->enabled = other.enabled;
      this->print_time = other.print_time;
      this->print_name = other.print_name;
      this->print_level = other.print_level;
      this->print_location = other.print_location;
      this->min_message_level = other.min_message_level;
      this->stream_mask = other.stream_mask;
      return *this;
    }*/
}

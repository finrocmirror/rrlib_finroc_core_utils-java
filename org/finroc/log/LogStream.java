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

import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.PassByValue;
import org.finroc.jc.annotation.PostProcess;

/**
 * Java variant of C++ LogStream
 */
@JavaOnly @PassByValue
public class LogStream {

    private LogLevel level;
    private String description;
    private LogDomain domain;
    private StringBuilder buffer = new StringBuilder();

    LogStream(LogLevel level, LogDomain domain, String description) {
        this.level = level;
        this.domain = domain;
        this.description = description;
    }

    @JavaOnly @PostProcess("org.finroc.j2c.LogStream")
    public LogStream append(String s) {
        buffer.append(s);
        return this;
    }

    @JavaOnly @PostProcess("org.finroc.j2c.LogStream")
    public LogStream append(short s) {
        buffer.append(s);
        return this;
    }

    @JavaOnly @PostProcess("org.finroc.j2c.LogStream")
    public LogStream appendln(String s) {
        buffer.append(s);
        buffer.append('\n');
        return this;
    }

    @JavaOnly @PostProcess("org.finroc.j2c.LogStream")
    public void close() {
        domain.log(level, description, buffer.toString(), null, 2);
    }
}

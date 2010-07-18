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

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.finroc.jc.Time;
import org.finroc.jc.annotation.JavaOnly;
import org.finroc.jc.annotation.PostProcess;

/**
 * The RRLib logging system is structured into hierarchical domains that
 * can be created and configured via LoggingDomainRegistry. That given,
 * in the program implementation instances of the class LoggingDomain
 * wrap the stream that can be access either in C++ iostream style via
 * operator << or in good old-fashioned C style using printf formatting.
 *
 * This class implements messaging via a specific logging domain
 * The RRLib logging system is structured into hierarchical domains that
 * can be created and configured via LoggingDomainRegistry. That given,
 * in the program implementation instances of this class wrap the stream
 * that can be access either in C++ iostream style via operator << or
 * in the good old-fashioned C style using printf formatting.
 *
 * @author Max Reichardt
 * @author Tobias Föhst
 */
@JavaOnly
public class LoggingDomain {

    LoggingDomain parent;
    ArrayList<LoggingDomain> children = new ArrayList<LoggingDomain>();

    private LoggingDomainConfiguration configuration;

    //private PrintStream streamBuffer;
    //private OutputStream stream;
    class FileStream extends PrintStream {
        public FileStream(String fileName) throws FileNotFoundException {
            super(new BufferedOutputStream(new FileOutputStream(fileName)));
        }
    }

    private FileStream fileStream;

    private int outputStreamsRevision = -1;
    private ArrayList<PrintStream> outputStreams = new ArrayList<PrintStream>();

    public static final DateFormat format = DateFormat.getTimeInstance();

    private StringBuilder buffer = new StringBuilder(); // temporary buffer for output string (only use with lock on domain)

    /** The ctor of a top level domain
     *
     * This ctor is to be called by the registry that creates the top level
     * domain.
     *
     * @param configuration   The configuration for the new domain
     */
    LoggingDomain(LoggingDomainConfiguration configuration) {
        this.configuration = configuration;
        //streamBuffer = new PrintStream
    }

    /** The ctor for a new sub domain
     *
     * This ctor is to be called by the registry to create a new subdomain
     * with a given configuration
     *
     * @param configuration   The configuration for the new domain
     * @param parent          The parent domain
     */
    LoggingDomain(LoggingDomainConfiguration configuration, LoggingDomain parent) {
        this(configuration);
        this.parent = parent;
        parent.children.add(this);
        configureSubTree();
    }

    /** Recursively configure the subtree that begins in this domain
     *
     * If the domain is configured by its parent, the configuration is
     * copied and propagated to this domain's children
     */
    void configureSubTree() {
        if (parent != null && parent.configuration.configureSubTree) {
            configuration = new LoggingDomainConfiguration(configuration.name, parent.configuration);
            for (LoggingDomain ld : children) {
                ld.configureSubTree();
            }
        }
    }

    /** Open the file stream for file output
     *
     * This method creates a new file which name is build using a prefix
     * and the full qualified domain name.
     * If the file already exists, it will be truncated.
     *
     * @return Whether the file stream could be opened or not
     */
    private boolean openFileOutputStream() {
        if (fileStream != null) {
            return true;
        }
        String fileNamePrefix = LoggingDomainRegistry.getInstance().getOutputFileNamePrefix();
        if (fileNamePrefix.length() == 0) {
            System.err.println("RRLib Logging >> Prefix for file names not set. Can not use eMS_FILE.");
            System.err.println("Consider calling tMessageDomainRegistry::GetInstance().SetOutputFileNamePrefix(basename(argv[0])) for example.");
            return false;
        }
        String fileName = fileNamePrefix + getName() + ".log";
        try {
            fileStream = new FileStream(fileName);
        } catch (Exception e) {
            System.err.println("RRLib Logging >> Could not open file `" + fileName + "'!");
            return false;
        }
        return true;
    }

    /** Setup the output stream to be used in this domain
     *
     * A domain can stream its input to stdout, stderr, an own file and/or its parent's file.
     *
     *@param outputs   Streams to output to
     */
    private void setupOutputStream(LogStream... outputs)  {
        if (outputStreamsRevision == configuration.streamMaskRevision) {
            return;
        }

        synchronized (configuration) {
            outputStreams.clear();
            Set<PrintStream> tmp = new HashSet<PrintStream>();
            for (LogStream ls : outputs) {
                if (ls == LogStream.eLS_STDOUT) {
                    tmp.add(System.out);
                } else if (ls == LogStream.eLS_STDERR) {
                    tmp.add(System.err);
                } else if (ls == LogStream.eLS_FILE) {
                    tmp.add(openFileOutputStream() ? fileStream : System.err);
                } else if (ls == LogStream.eLS_COMBINED_FILE) {
                    LoggingDomain domain = this;
                    for (; domain.parent != null && domain.parent.configuration.configureSubTree; domain = domain.parent) {}
                    tmp.add(domain.openFileOutputStream() ? fileStream : System.err);
                }
            }
            outputStreams.addAll(tmp);
            outputStreamsRevision = configuration.streamMaskRevision;
        }
    }

    /** Get the current time as string for internal use in messages
     *
     * This method formats the current time as string that can be used in
     * messages.
     *
     * @return The current time as string
     */
    private String getTimeString() {
        return format.format(Time.getPrecise());
    }

    /** Get the domain's name as string for internal use in messages
     *
     * This method formats the name as string that can be used in
     * messages. This string is padded with spaces to the length of the
     * longest domain name
     *
     * @return The padded name as string
     */
    private String getNameString() {
        // TODO: maybe do this properly with padding
        return configuration.name;
    }

    /** Get the given message level as string for internal use in messages
     *
     * This method formats the given level as string that can be used in
     * messages.
     *
     * @param level   The level that should be represented as string
     *
     * @return The given level as padded string
     */
    private String getLevelString(LogLevel level) {
        switch (level) {
        case eLL_VERBOSE:
            return "[verbose] ";
        case eLL_LOW:
            return "[low]     ";
        case eLL_MEDIUM:
            return "[medium]  ";
        case eLL_HIGH:
            return "[high]    ";
        default:
            return "          ";
        }
    }

    /** Get the given location as string for internal use in messages
     *
     * This method formats given location consisting of a file name and a
     * line number as string that can be used in messages.
     *
     * @param file   The file name (e.g. from __FILE__)
     * @param line   The line number (e.g. from __LINE__)
     *
     * @return The given location as string
     */
    private String getLocationString(String file, int line) {
        return file + ":" + line;
    }

    /** Get a string to setup colored output in a terminal
     *
     * This method creates a string that contains the control sequence to
     * setup colored output according to the given level.
     *
     * @param level   The according log level
     *
     * @return The string containing the control sequence
     */
    private String getControlStringForColoredOutput(LogLevel level) {
        switch (level) {
        case eLL_VERBOSE:
            return "\033[;2;32m";
        case eLL_LOW:
            return "\033[;2;33m";
        case eLL_MEDIUM:
            return "\033[;1;34m";
        case eLL_HIGH:
            return "\033[;1;31m";
        default:
            return "\033[;0m";
        }
    }

//----------------------------------------------------------------------
// Public methods
//----------------------------------------------------------------------

//  /** The dtor of LoggingDomain
//   */
//  protected void finalize() {
//
//  }

    /** Get the full qualified name of this domain
     *
     * Each domain has a full qualified name consisting of its parent's name
     * and the local part that was given at creation time.
     *
     * @return The full qualified domain name
     */
    public String getName() {
        return configuration.name;
    }

    /** Get configuration status of this domain's enabled flag
     *
     * If a domain is enabled it processes log messages that are not below a
     * specified min level. Otherwise it is totally quite.
     *
     * @return Whether the domain is enabled or not
     */
    boolean isEnabled() {
        return configuration.enabled;
    }

    /** Get configuration status of this domain's print_time flag
     *
     * The current time is prepended to messages of this domain if the
     * print_time flag is set.
     *
     * @return Whether the print_time flag is set or not.
     */
    boolean getPrintTime() {
        return configuration.printTime;
    }

    /** Get configuration status of this domain's print_name flag
     *
     * The name of this domain prepended to messages of this domain if its
     * print_name flag is set.
     *
     * @return Whether the print_name flag is set or not.
     */
    boolean getPrintName() {
        return configuration.printName;
    }

    /** Get configuration status of this domain's print_level flag
     *
     * The level of each message is contained in the output of this domain
     * if the print_level flag is set.
     *
     * @return Whether the print_level flag is set or not.
     */
    boolean getPrintLevel() {
        return configuration.printLevel;
    }

    /** Get configuration status of this domain's print_location flag
     *
     * The location given to each message is contained in the output of this
     * domain if the print_location flag is set.
     *
     * @return Whether the print_location flag is set or not.
     */
    boolean getPrintLocation() {
        return configuration.printLocation;
    }

    /** Get the minimal log level a message must have to be processed
     *
     * Each message has a log level that must not below the configured limit to be processed.
     *
     * @return The configured minimal log level
     */
    LogLevel getMinMessageLevel() {
        return configuration.minMessageLevel;
    }

    /** Get the mask representing which streams are used for message output
     *
     * For message output several streams can be used. This bitmask configures
     * which of them are enabled.
     *
     * @return The bitmask that contains the enabled message streams
     */
    /*inline const eLogStreamMask GetStreamMask() const
    {
      return this->configuration->stream_mask;
    }*/

//  /** Get a message stream from this domain
//   *
//   * This method is the streaming interface to this logging domain.
//   * It must be used for every output using operator <<.
//   * The method then depending on the domain's configuration chooses
//   * a stream, prints the prefix that should be prepended to every
//   * message and returns the stream to process further input given as
//   * operator << cascade in the user's program.
//   * To properly specify the arguments of this method consider using
//   * the macros defined in rrlib/logging/definitions.h
//   *
//   * @param description   A string that describes the global context of the message
//   * @param function      The name of the function that contains the message (__FUNCTION__)
//   * @param file          The file that contains the message
//   * @param line          The line that contains the message
//   * @param level         The log level of the message
//   *
//   * @return A reference to the stream that can be used for the remaining message parts
//   */
//  inline tLoggingStreamProxy GetMessageStream(const char *description, const char *function, const char *file, unsigned int line, eLogLevel level) const
//  {
//    tLoggingStreamProxy stream_proxy(this->stream);
//    this->streamBuffer.Clear();
//    if (level < this->GetMinMessageLevel() || !this->IsEnabled())
//    {
//      return stream_proxy;
//    }
//    this->SetupOutputStream(this->configuration->stream_mask);
//
//    if (this->GetPrintTime())
//    {
//      this->stream << this->GetTimeString();
//    }
//    this->SetupOutputStream(this->configuration->stream_mask & ~(eLSM_FILE | eLSM_COMBINED_FILE));
//    this->stream << this->GetControlStringForColoredOutput(level);
//    this->SetupOutputStream(this->configuration->stream_mask);
//
//#ifndef _RRLIB_LOGGING_LESS_OUTPUT_
//    if (this->GetPrintName())
//    {
//      this->stream << this->GetNameString();
//    }
//    if (this->GetPrintLevel())
//    {
//      this->stream << this->GetLevelString(level);
//    }
//#endif
//    this->stream << description << "::" << function << " ";
//#ifndef _RRLIB_LOGGING_LESS_OUTPUT_
//    if (this->GetPrintLocation())
//    {
//      this->stream << this->GetLocationString(file, line);
//    }
//#endif
//    this->stream << ">> ";
//    this->SetupOutputStream(this->configuration->stream_mask & ~(eLSM_FILE | eLSM_COMBINED_FILE));
//    this->stream << "\033[;0m";
//    this->SetupOutputStream(this->configuration->stream_mask);
//
//    return stream_proxy;
//  }
//
    /** A printf like variant of using logging domains for message output
    *
    * Instead of using operator << to output messages this method can be
    * used. It then itself uses printf to format the given message and
    * streams the result through the result obtained from GetMessageStream.
    * That way the message prefix is only generated in one place and - more
    * important - the underlying technique is the more sane one from
    * iostreams instead of file descriptors.
    * Apart from that: iostreams and file descriptors can not be mixed. So
    * a decision had to be made.
    *
    * @param level         The log level of the message
    * @param callerDescription Description of calling object or context
    * @param msg           The message to output
    */
    @PostProcess("org.finroc.j2c.LogMessage")
    public void message(LogLevel level, String callerDescription, String msg) {
        message(level, callerDescription, msg, 1);
    }

    /** A printf like variant of using logging domains for message output
     *
     * Instead of using operator << to output messages this method can be
     * used. It then itself uses printf to format the given message and
     * streams the result through the result obtained from GetMessageStream.
     * That way the message prefix is only generated in one place and - more
     * important - the underlying technique is the more sane one from
     * iostreams instead of file descriptors.
     * Apart from that: iostreams and file descriptors can not be mixed. So
     * a decision had to be made.
     *
     * @param level         The log level of the message
     * @param callerDescription Description of calling object or context
     * @param msg           The message to output
     * @param callerStackIndex Stack index of caller (advanced feature)
     */
    void message(LogLevel level, String callerDescription, String msg, int callerStackIndex) {

        if (level.ordinal() < getMinMessageLevel().ordinal() || !isEnabled()) {
            return;
        }

        // extract data from caller
        StackTraceElement caller = new Throwable().getStackTrace()[callerStackIndex];
        String file = caller.getFileName();
        int line = caller.getLineNumber();
        String function = caller.getMethodName();

        // produce string to output
        synchronized (this) {
            setupOutputStream();
            buffer.delete(0, buffer.length());
            if (getPrintTime()) {
                buffer.append(getTimeString());
            }

            if (getPrintName()) {
                buffer.append(getNameString());
            }
            if (getPrintLevel()) {
                buffer.append(getLevelString(level));
            }
            buffer.append(callerDescription).append("::").append(function).append(" ");
            if (getPrintLocation()) {
                buffer.append(getLocationString(file, line));
            }
            buffer.append(">> ");

            String nonColoredOutput = buffer.toString();
            String coloredOutput = getControlStringForColoredOutput(level) + nonColoredOutput + "\033[;0m";

            for (PrintStream ps : outputStreams) {
                if (ps instanceof FileStream) {
                    ps.println(nonColoredOutput);
                } else {
                    ps.println(coloredOutput);
                }
            }
        }
    }

}

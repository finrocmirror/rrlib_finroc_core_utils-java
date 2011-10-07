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
package org.rrlib.finroc_core_utils.xml;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.rrlib.finroc_core_utils.jc.annotation.JavaOnly;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * This class wraps creation and accessing the DOM tree of an XML document.
 *
 * If an XML document is loaded for full access to its content, a DOM
 * tree is generated consisting of nodes with attributes. This class
 * implements parsing and validating an XML file as well as accessing
 * the DOM tree through instances of tXMLNode, featuring lazy evaluation.
 * That means wrapping instances are not created before they are used.
 */
public class XMLDocument {

    /** Java API XML document */
    private Document document;

    /** root node */
    private XMLNode rootNode;

    /**
     * The ctor of an empty tXMLDocument
     *
     * This ctor creates a new xml document
     */
    public XMLDocument() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dbuilder = factory.newDocumentBuilder();
            document = dbuilder.newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * The ctor of tXMLDocument from a given file
     *
     * This ctor reads and parses a file with given name into a XML DOM
     * representation.
     * If needed, the XML document is also validated using an included
     * DTD specification.
     *
     * @exception tXML2WrapperException is thrown if the file was not found or could not be parsed
     *
     * @param fileName   The name of the file to load
     * @param validate    Whether the validation should be processed or not
     */
    public XMLDocument(String fileName, boolean validate) throws XML2WrapperException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dbuilder = factory.newDocumentBuilder();
            document = dbuilder.parse(fileName);
            rootNode = new XMLNode(document, document.getDocumentElement());
        } catch (Exception e) {
            throw new XML2WrapperException(e);
        }
    }

    /**
     * The ctor of tXMLDocument from a given file
     *
     * This ctor reads and parses a file with given name into a XML DOM
     * representation.
     * If needed, the XML document is also validated using an included
     * DTD specification.
     *
     * @exception tXML2WrapperException is thrown if the file was not found or could not be parsed
     *
     * @param fileName   The name of the file to load
     */
    public XMLDocument(String fileName) throws XML2WrapperException {
        this(fileName, true);
    }

    /** The ctor of tXMLDocument from a InputSource
     *
     * This ctor reads and parses XML content given in a InputSource into a XML DOM
     * representation.
     * If needed, the XML document is also validated using an included
     * DTD specification.
     *
     * @exception tXML2WrapperException is thrown if the memory buffer could not be parsed
     *
     * @param input       InputSource
     * @param validate    Whether the validation should be processed or not
     */
    public XMLDocument(InputSource input, boolean validate) throws XML2WrapperException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dbuilder = factory.newDocumentBuilder();
            document = dbuilder.parse(input);
            rootNode = new XMLNode(document, document.getDocumentElement());
        } catch (Exception e) {
            throw new XML2WrapperException(e);
        }
    }

    /**
     * Get the root node of the DOM tree stored for this document
     *
     * The XML document is stored as DOM tree in memory. This method
     * provides node-wise access to this tree starting at its root.
     *
     * @return A reference to the root node
     */
    public XMLNode getRootNode() {
        return rootNode;
    }

    /**
     *  Add a root node to a new document in DOM representation
     *
     * If you create a new XML document the first thing to do is to
     * add a root node with a specified name. After that, the root node
     * is fixed and additional calls to this method will throw an exception.
     *
     * @exception XML2WrapperException is thrown if the document already had a root node
     *
     * @param name   The name of the root node
     *
     * @return A reference to the newly created root node
     */
    public XMLNode addRootNode(String name) throws XML2WrapperException {
        if (rootNode != null) {
            throw new XML2WrapperException("Root node already exists");
        }
        Element root = document.createElement("root");
        document.appendChild(root);
        rootNode = new XMLNode(document, root);
        return rootNode;
    }

    /**
     * Write the XML document to a file
     *
     * This method creates or truncates a file with the given name and writes
     * the documents XML representation into it.
     *
     * @param fileName     The name of the file to use
     * @param compression   Compression level [0-9] where 0 is "no compression"
     */
    public void writeToFile(String fileName, int compression) throws Exception {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(fileName));
        writeToStream(new StreamResult(os), compression);
        os.close();
    }

    /**
     * Write the XML document to a file
     *
     * This method creates or truncates a file with the given name and writes
     * the documents XML representation into it.
     *
     * @param fileName     The name of the file to use
     */
    public void writeToFile(String fileName) throws Exception {
        writeToFile(fileName, 0);
    }

    /**
     * Write the XML document to a stream
     *
     * @param result            StreamResult
     * @param compression   Compression level [0-9] where 0 is "no compression"
     */
    @JavaOnly
    public void writeToStream(StreamResult result, int compression) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        factory.setAttribute("indent-number", 2);
        Transformer transformer = factory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
    }

    /**
     * @return XML dump of XML file (as written to file)
     */
    public String getXMLDump() throws Exception {
        StringWriter sw = new StringWriter();
        writeToStream(new StreamResult(sw), 0);
        return sw.toString();
    }
};

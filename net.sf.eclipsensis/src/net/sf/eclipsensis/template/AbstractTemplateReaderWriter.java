/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public abstract class AbstractTemplateReaderWriter
{
    protected static final String TEMPLATE_ROOT = "templates"; //$NON-NLS-1$
    protected static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
    protected static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
    protected static final String DESCRIPTION_NODE= "description"; //$NON-NLS-1$

    /**
     * Reads templates from a stream and adds them to the templates.
     *
     * @param stream the byte stream to read templates from
     * @return the read templates
     * @throws IOException if reading from the stream fails
     */
    public Collection import$(InputStream stream) throws IOException
    {
        return import$(new InputSource(stream));
    }

    public Collection import$(InputSource source) throws IOException
    {
        try {
            Collection templates= new HashSet();

            DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
            DocumentBuilder parser= factory.newDocumentBuilder();
            Document document= parser.parse(source);

            NodeList elements= document.getElementsByTagName(TEMPLATE_ELEMENT);

            int count= elements.getLength();
            for (int i= 0; i != count; i++) {
                Node node= elements.item(i);
                NamedNodeMap attributes= node.getAttributes();

                if (attributes == null) {
                    continue;
                }

                String name= XMLUtil.getStringValue(attributes, NAME_ATTRIBUTE);
                if (name == null) {
                    throw new IOException(EclipseNSISPlugin.getFormattedString("template.readerwriter.error.missing.attribute", new Object[]{NAME_ATTRIBUTE})); //$NON-NLS-1$
                }

                AbstractTemplate template;
                template = createTemplate(name);

                template.setDeleted(false);
                template.setEnabled(true);

                NodeList children = node.getChildNodes();
                if(children != null) {
                    for (int j= 0; j != children.getLength(); j++) {
                        Node item = children.item(j);
                        if(item.getNodeName().equals(DESCRIPTION_NODE)) {
                            template.setDescription(XMLUtil.readTextNode(item));
                        }
                        else if(item.getNodeName().equals(getContentsNodeName())) {
                            importContents(template, item);
                        }
                    }
                }

                templates.add(template);
            }

            return templates;
        }
        catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        }
        catch (SAXException e) {
            Throwable t= e.getCause();
            if (t instanceof IOException) {
                throw (IOException) t;
            }
            else {
                throw new IOException(t.getMessage());
            }
        }
    }

    /**
     * Saves the templates as XML, encoded as UTF-8 onto the given byte stream.
     *
     * @param templates the templates to save
     * @param stream the byte output to write the templates to in XML
     * @throws IOException if writing the templates fails
     */
    public void export(Collection templates, OutputStream stream) throws IOException
    {
        export(templates, new StreamResult(stream));
    }

    /**
     * Saves the templates as XML.
     *
     * @param templates the templates to save
     * @param result the stream result to write to
     * @throws IOException if writing the templates fails
     */
    public void export(Collection templates, StreamResult result) throws IOException
    {
        try {
            DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
            DocumentBuilder builder= factory.newDocumentBuilder();
            Document document= builder.newDocument();

            Node root= document.createElement(TEMPLATE_ROOT);
            document.appendChild(root);

            for (Iterator iter=templates.iterator(); iter.hasNext(); ) {
                AbstractTemplate template= (AbstractTemplate)iter.next();

                Node node= document.createElement(TEMPLATE_ELEMENT);
                root.appendChild(node);

                XMLUtil.addAttribute(document, node, NAME_ATTRIBUTE, template.getName());

                Element description = document.createElement(DESCRIPTION_NODE);
                Text data= document.createTextNode(template.getDescription());
                description.appendChild(data);
                node.appendChild(description);

                node.appendChild(exportContents(template, document));
            }


            Transformer transformer=TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
            DOMSource source = new DOMSource(document);

            transformer.transform(source, result);
        }
        catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        }
        catch (TransformerException e) {
            if (e.getException() instanceof IOException) {
                throw (IOException) e.getException();
            }
            else {
                throw new IOException(e.getMessage());
            }
        }
        catch (Exception e) {
            throw new IOException(e.getMessage());
        }
    }

    protected abstract String getContentsNodeName();
    protected abstract Node exportContents(AbstractTemplate template, Document doc);
    protected abstract void importContents(AbstractTemplate template, Node item);
    protected abstract AbstractTemplate createTemplate(String name);
}

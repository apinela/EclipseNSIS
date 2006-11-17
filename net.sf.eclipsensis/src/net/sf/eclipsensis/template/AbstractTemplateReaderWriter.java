/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.template;

import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.XMLUtil;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public abstract class AbstractTemplateReaderWriter
{
    protected static final String TEMPLATE_ROOT = "templates"; //$NON-NLS-1$
    protected static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
    protected static final String ID_ATTRIBUTE= "id"; //$NON-NLS-1$
    protected static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
    protected static final String DESCRIPTION_NODE= "description"; //$NON-NLS-1$

    /**
     * Reads templates from a stream and adds them to the templates.
     *
     * @param stream the file to read templates from
     * @return the read templates
     * @throws IOException if reading from the stream fails
     */
    public Collection import$(File file) throws IOException
    {
        try {
            Collection templates= new HashSet();

            Document document= XMLUtil.loadDocument(file);

            NodeList elements= document.getElementsByTagName(TEMPLATE_ELEMENT);

            int count= elements.getLength();
            for (int i= 0; i != count; i++) {
                Node node= elements.item(i);
                NamedNodeMap attributes= node.getAttributes();

                if (attributes == null) {
                    continue;
                }

                String id= XMLUtil.getStringValue(attributes, ID_ATTRIBUTE);

                String name= XMLUtil.getStringValue(attributes, NAME_ATTRIBUTE);
                if (name == null) {
                    throw new IOException(EclipseNSISPlugin.getFormattedString("template.readerwriter.error.missing.attribute", new Object[]{NAME_ATTRIBUTE})); //$NON-NLS-1$
                }

                AbstractTemplate template;
                template = createTemplate((Common.isEmpty(id)?null:id), name);

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
                template.afterImport();
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
     * @param file the file to write the templates to in XML
     * @throws IOException if writing the templates fails
     */
    public void export(Collection templates, File file) throws IOException
    {
        try {
            Document document= XMLUtil.newDocument();

            Node root= document.createElement(TEMPLATE_ROOT);
            document.appendChild(root);

            for (Iterator iter=templates.iterator(); iter.hasNext(); ) {
                AbstractTemplate template= (AbstractTemplate)iter.next();
                template.beforeExport();
                Node node= document.createElement(TEMPLATE_ELEMENT);
                root.appendChild(node);

                if (!Common.isEmpty(template.getId())) {
                    XMLUtil.addAttribute(document, node, ID_ATTRIBUTE, template.getId());
                }
                XMLUtil.addAttribute(document, node, NAME_ATTRIBUTE, template.getName());

                Element description = document.createElement(DESCRIPTION_NODE);
                Text data= document.createTextNode(template.getDescription());
                description.appendChild(data);
                node.appendChild(description);

                node.appendChild(exportContents(template, document));
            }

            XMLUtil.saveDocument(document, file);
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
    protected abstract AbstractTemplate createTemplate(String id, String name);
}

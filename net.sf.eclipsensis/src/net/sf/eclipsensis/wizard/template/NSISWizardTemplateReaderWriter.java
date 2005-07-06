/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.template;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NSISWizardTemplateReaderWriter
{
    private static final String TEMPLATE_ROOT = "templates"; //$NON-NLS-1$
    private static final String TEMPLATE_ELEMENT = "template"; //$NON-NLS-1$
    private static final String NAME_ATTRIBUTE= "name"; //$NON-NLS-1$
    private static final String DESCRIPTION_NODE= "description"; //$NON-NLS-1$
    private static final String SETTINGS_NODE= "settings"; //$NON-NLS-1$
    
    /**
     * Reads templates from a stream and adds them to the templates.
     * 
     * @param stream the byte stream to read templates from
     * @return the read templates
     * @throws IOException if reading from the stream fails 
     */ 
    public Collection import_(InputStream stream) throws IOException 
    {
        return import_(new InputSource(stream));
    }
    
    public Collection import_(InputSource source) throws IOException 
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
                
                String name= getStringValue(attributes, NAME_ATTRIBUTE);
                if (name == null) {
                    throw new IOException(EclipseNSISPlugin.getResourceString("wizard.template.readerwriter.error.missing_attribute")); //$NON-NLS-1$
                }

                NSISWizardTemplate template;
                template = new NSISWizardTemplate(name);

                template.setDeleted(false);
                template.setEnabled(true);
                
                NodeList children = node.getChildNodes();
                if(children != null) {
                    for (int j= 0; j != children.getLength(); j++) {
                        Node item = children.item(j);
                        if(item.getNodeName().equals(DESCRIPTION_NODE)) {
                            StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                            NodeList children2 = item.getChildNodes();
                            if(children2 != null) {
                                for (int k = 0; k < children2.getLength(); k++) {
                                   Node item2 = children2.item(k);
                                   if(item2 != null) {
                                       buf.append(item2.getNodeValue());
                                   }
                                }
                            }
                            template.setDescription(buf.toString());
                        }
                        else if(item.getNodeName().equals(SETTINGS_NODE)) {
                            NSISWizardSettings settings = new NSISWizardSettings(true);
                            settings.fromNode(item);
                            template.setSettings(settings);
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

            Node root= document.createElement(TEMPLATE_ROOT); //$NON-NLS-1$
            document.appendChild(root);
            
            for (Iterator iter=templates.iterator(); iter.hasNext(); ) {
                NSISWizardTemplate template= (NSISWizardTemplate)iter.next();
                
                Node node= document.createElement(TEMPLATE_ELEMENT);
                root.appendChild(node);
                
                Common.addAttribute(document, node, NAME_ATTRIBUTE, template.getName());
    
                Element description = document.createElement(DESCRIPTION_NODE);
                Text data= document.createTextNode(template.getDescription());
                description.appendChild(data);
                node.appendChild(description);
                
                node.appendChild(template.getSettings().toNode(document));
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

//    private boolean getBooleanValue(NamedNodeMap attributes, String attribute, boolean defaultValue) throws SAXException 
//    {
//        Node enabledNode= attributes.getNamedItem(attribute);
//        if (enabledNode == null)
//            return defaultValue;
//        else if (enabledNode.getNodeValue().equals(Boolean.toString(true)))
//            return true;
//        else if (enabledNode.getNodeValue().equals(Boolean.toString(false))) {
//            return false;
//        }
//        else {
//            throw new SAXException(EclipseNSISPlugin.getResourceString("wizard.template.readerwriter.error.illegal.boolean.attribute")); //$NON-NLS-1$
//        }
//    }
    
    private String getStringValue(NamedNodeMap attributes, String name) throws SAXException 
    {
        String val= getStringValue(attributes, name, null);
        if (val == null) {
            throw new SAXException(EclipseNSISPlugin.getResourceString("wizard.template.readerwriter.error.missing_attribute")); //$NON-NLS-1$
        }
        return val;
    }

    private String getStringValue(NamedNodeMap attributes, String name, String defaultValue) 
    {
        Node node= attributes.getNamedItem(name);
        return node == null ? defaultValue : node.getNodeValue();
    }
}

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
    private static final String FILENAME_ATTRIBUTE= "fileName"; //$NON-NLS-1$
    private static final String ENABLED_ATTRIBUTE= "enabled"; //$NON-NLS-1$
    private static final String DELETED_ATTRIBUTE= "deleted"; //$NON-NLS-1$
    
    private boolean mExportImportMode = false;
    
    /**
     * @param withSettings
     */
    public NSISWizardTemplateReaderWriter(boolean exportImportMode)
    {
        super();
        mExportImportMode = exportImportMode;
    }
    
    /**
     * 
     */
    public NSISWizardTemplateReaderWriter()
    {
        this(false);
    }
    
    /**
     * Reads templates from a reader and returns them. The reader must present
     * a serialized form as produced by the <code>save</code> method.
     * 
     * @param reader the reader to read templates from
     * @return the read templates
     * @throws IOException if reading from the stream fails 
     */ 
    public Collection read(Reader reader) throws IOException 
    {
        return read(new InputSource(reader));
    }
    
    /**
     * Reads templates from a stream and adds them to the templates.
     * 
     * @param stream the byte stream to read templates from
     * @return the read templates
     * @throws IOException if reading from the stream fails 
     */ 
    public Collection read(InputStream stream) throws IOException 
    {
        return read(new InputSource(stream));
    }
    
    private Collection read(InputSource source) throws IOException 
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
                String fileName= null;
                if(!mExportImportMode) {
                    fileName = getStringValue(attributes, FILENAME_ATTRIBUTE, ""); //$NON-NLS-1$
                    if (fileName == null) {
                        throw new IOException(EclipseNSISPlugin.getResourceString("wizard.template.readerwriter.error.missing_attribute")); //$NON-NLS-1$
                    }
                }

                boolean deleted = (mExportImportMode?false:getBooleanValue(attributes, DELETED_ATTRIBUTE, false));
                boolean enabled = (mExportImportMode?true:getBooleanValue(attributes, ENABLED_ATTRIBUTE, true));
                
                String description = ""; //$NON-NLS-1$
                String settingsText = ""; //$NON-NLS-1$
                NodeList children = node.getChildNodes();
                for (int j= 0; j != children.getLength(); j++) {
                    Node item = children.item(j);
                    if(item.getNodeName().equals(DESCRIPTION_NODE) && description.length() == 0) {
                        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                        NodeList children2 = item.getChildNodes();
                        for (int k = 0; k < children2.getLength(); k++) {
                           Node item2 = children2.item(k);
                           if(item2 != null) {
                               buf.append(item2.getNodeValue());
                           }
                        }
                        description = buf.toString();
                    }
                    else if(mExportImportMode && item.getNodeName().equals(SETTINGS_NODE) && settingsText.length() == 0) {
                        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
                        NodeList children2 = item.getChildNodes();
                        for (int k = 0; k < children2.getLength(); k++) {
                           Node item2 = children2.item(k);
                           if(item2 != null) {
                               buf.append(item2.getNodeValue());
                           }
                        }
                        settingsText = buf.toString();
                    }
                }

                NSISWizardTemplate template;
                if(mExportImportMode) {
                    template = new NSISWizardTemplate(name,description);
                    NSISWizardSettings settings = (NSISWizardSettings)Common.fromXML(settingsText);
                    template.setSettings(settings);
                }
                else {
                    template = new NSISWizardTemplate();
                    template.setName(name);
                    template.setDescription(description);
                    template.setFileName(fileName);
                }
                template.setDeleted(deleted);
                template.setEnabled(enabled);
                
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
    public void save(Collection templates, OutputStream stream) throws IOException 
    {
        save(templates, new StreamResult(stream));
    }
    
    /**
     * Saves the templates as XML.
     * 
     * @param templates the templates to save
     * @param writer the writer to write the templates to in XML
     * @throws IOException if writing the templates fails 
     */
    public void save(Collection templates, Writer writer) throws IOException 
    {
        save(templates, new StreamResult(writer));
    }
    
    /**
     * Saves the templates as XML.
     * 
     * @param templates the templates to save
     * @param result the stream result to write to
     * @throws IOException if writing the templates fails 
     */
    private void save(Collection templates, StreamResult result) throws IOException 
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
                
                NamedNodeMap attributes= node.getAttributes();
                
                Attr name= document.createAttribute(NAME_ATTRIBUTE);
                name.setValue(template.getName());
                attributes.setNamedItem(name);
    
                if(!mExportImportMode) {
                    Attr fileName= document.createAttribute(FILENAME_ATTRIBUTE);
                    fileName.setValue(template.getFileName());
                    attributes.setNamedItem(fileName);
        
                    Attr enabled= document.createAttribute(ENABLED_ATTRIBUTE);
                    enabled.setValue(template.isEnabled() ? Boolean.toString(true) : Boolean.toString(false)); //$NON-NLS-1$ //$NON-NLS-2$
                    attributes.setNamedItem(enabled);
                    
                    Attr deleted= document.createAttribute(DELETED_ATTRIBUTE);
                    deleted.setValue(template.isDeleted() ? Boolean.toString(true) : Boolean.toString(false)); //$NON-NLS-1$ //$NON-NLS-2$
                    attributes.setNamedItem(deleted);
                }
                
                Element description = document.createElement(DESCRIPTION_NODE);
                Text data= document.createTextNode(template.getDescription());
                description.appendChild(data);
                node.appendChild(description);
                
                if(mExportImportMode) {
                    Element settings = document.createElement(SETTINGS_NODE);
                    data= document.createTextNode(Common.toXML(template.getSettings()));
                    settings.appendChild(data);
                    node.appendChild(settings);
                }
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
    }

    private boolean getBooleanValue(NamedNodeMap attributes, String attribute, boolean defaultValue) throws SAXException 
    {
        Node enabledNode= attributes.getNamedItem(attribute);
        if (enabledNode == null)
            return defaultValue;
        else if (enabledNode.getNodeValue().equals(Boolean.toString(true)))
            return true;
        else if (enabledNode.getNodeValue().equals(Boolean.toString(false))) {
            return false;
        }
        else {
            throw new SAXException(EclipseNSISPlugin.getResourceString("wizard.template.readerwriter.error.illegal.boolean.attribute")); //$NON-NLS-1$
        }
    }
    
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

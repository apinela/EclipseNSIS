/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.text.MessageFormat;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.settings.NSISWizardSettings;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public abstract class AbstractNSISWizardPage extends WizardPage implements INSISWizardConstants
{
    protected NSISWizardSettings mSettings = null;
    protected static final int WIDTH_HINT;

    static {
        int widthHint;
        try {
            widthHint = Integer.parseInt(EclipseNSISPlugin.getResourceString("wizard.width.hint"));
        }
        catch(Exception ex) {
            widthHint = 500;
        }
        WIDTH_HINT = widthHint;
    }

    protected VerifyListener mNumberVerifyListener = new VerifyListener() {
        public void verifyText(VerifyEvent e) 
        {
            char[] chars = e.text.toCharArray();
            for(int i=0; i< chars.length; i++) {
                if(!Character.isDigit(chars[i])) {
                    e.doit = false;
                    return;
                }
            }
        }
    };
    
    private static ImageDescriptor cImage = ImageManager.getImageDescriptor(EclipseNSISPlugin.getResourceString("wizard.title.image")); //$NON-NLS-1$
    
	public AbstractNSISWizardPage(NSISWizardSettings settings, String pageName, String title, String description) 
    {
		super(pageName,title,cImage);
        setDescription(description);
		mSettings = settings;
	}
    
    protected String getArrayStringResource(String[] array, int index, String defaultString)
    {
        return EclipseNSISPlugin.getResourceString(Common.isEmptyArray(array) || array.length <= index || Common.isEmpty(array[index])?defaultString:array[index]);
    }

    protected String getFormattedArrayStringResource(String[] array, int index, String defaultString, Object[] params)
    {
        return MessageFormat.format(getArrayStringResource(array, index, defaultString), params);
    }

    protected boolean validateEmptyOrValidURL(String url, String messageResource)
    {
        if(!Common.isEmpty(url) && !Common.isValidURL(url)) {
            setErrorMessage(getFormattedArrayStringResource(new String[]{messageResource},0,"invalid.url.error",new String[]{url})); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    protected boolean validatePathName(String pathname, String[] messageResources)
    {
        return validateEmptyOrValidPathName(pathname, false, messageResources);
    }

    protected boolean validateEmptyOrValidPathName(String pathname, String messageResource)
    {
        return validateEmptyOrValidPathName(pathname, true, new String[]{null,messageResource});
    }

    protected boolean validateNSISPrefixedPathName(String pathname, String[] messageResources)
    {
        if(Common.isEmpty(pathname)) {
            setErrorMessage(getArrayStringResource(messageResources,0,"empty.pathname.error")); //$NON-NLS-1$
            return false;
        }
        else if(!Common.isValidNSISPrefixedPathName(pathname)) {
            setErrorMessage(getFormattedArrayStringResource(messageResources,1,"invalid.nsis.pathname.error",new String[]{pathname})); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private boolean validateEmptyOrValidPathName(String pathname, boolean emptyOK, String[] messageResources)
    {
        if(Common.isEmpty(pathname)) {
            if(!emptyOK) {
                setErrorMessage(getArrayStringResource(messageResources,0,"empty.pathname.error")); //$NON-NLS-1$
                return false;
            }
        }
        else if(!Common.isValidPathName(pathname)) {
            setErrorMessage(getFormattedArrayStringResource(messageResources,1,"invalid.pathname.error",new String[]{pathname})); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    protected boolean validateFileName(String filename, String[] messageResources)
    {
        if(Common.isEmpty(filename)) {
            setErrorMessage(getArrayStringResource(messageResources,0,"empty.filename.error")); //$NON-NLS-1$
            return false;
        }
        else if(!Common.isValidFileName(filename)) {
            setErrorMessage(getFormattedArrayStringResource(messageResources,1,"invalid.filename.error",new String[]{filename})); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    protected boolean validateFile(String filename, String[] messageResources)
    {
        return validateEmptyOrValidFile(filename, false, messageResources);
    }

    protected boolean validateEmptyOrValidFile(String filename, String messageResource)
    {
        return validateEmptyOrValidFile(filename, true, new String[]{null,messageResource});
    }

    protected boolean validateEmptyOrValidFile(String filename, boolean emptyOK, String[] messageResources)
    {

        if(Common.isEmpty(filename)) {
            if(!emptyOK) {
                setErrorMessage(getArrayStringResource(messageResources,0,"empty.file.error")); //$NON-NLS-1$
                return false;
            }
        }
        else if(!Common.isValidFile(filename)) {
            setErrorMessage(getFormattedArrayStringResource(messageResources,1,"invalid.file.error",new String[]{filename})); //$NON-NLS-1$
            return false;
        }
        return true;
    }
    
    protected class NSISWizardAdapter implements INSISWizardListener
    {
        /* (non-Javadoc)
         * @see net.sf.eclipsensis.wizard.INSISWizardListener#aboutToEnter(org.eclipse.jface.wizard.IWizardPage, boolean)
         */
        public void aboutToEnter(IWizardPage page, boolean forward)
        {
        }

        /* (non-Javadoc)
         * @see net.sf.eclipsensis.wizard.INSISWizardListener#aboutToLeave(org.eclipse.jface.wizard.IWizardPage, boolean)
         */
        public void aboutToLeave(IWizardPage page, boolean forward)
        {
        }
    }
}
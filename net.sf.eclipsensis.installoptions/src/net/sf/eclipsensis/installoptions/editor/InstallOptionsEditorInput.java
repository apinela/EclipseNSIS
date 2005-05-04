/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;

public class InstallOptionsEditorInput implements IFileEditorInput
{
    private TextFileDocumentProvider mDocumentProvider;
    private IFileEditorInput mInput;
    /**
     * @throws CoreException
     * 
     */
    public InstallOptionsEditorInput(IFileEditorInput input)
    {
        mInput = input;
        mDocumentProvider = new TextFileDocumentProvider();
        try {
            mDocumentProvider.connect(this);
        }
        catch (CoreException e) {
            mDocumentProvider = null;
            e.printStackTrace();
        }
    }

    public TextFileDocumentProvider getDocumentProvider()
    {
        return mDocumentProvider;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IFileEditorInput#getFile()
     */
    public IFile getFile()
    {
        return mInput.getFile();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IStorageEditorInput#getStorage()
     */
    public IStorage getStorage() throws CoreException
    {
        return mInput.getStorage();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists()
    {
        return mInput.exists();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor()
    {
        return mInput.getImageDescriptor();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName()
    {
        return mInput.getName();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable()
    {
        return mInput.getPersistable();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText()
    {
        return mInput.getToolTipText();
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter)
    {
        if(adapter == TextFileDocumentProvider.class) {
            return mDocumentProvider;
        }
        return mInput.getAdapter(adapter);
    }

}

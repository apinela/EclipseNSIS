/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISEditorUtilities;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.wizard.template.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;

public class NSISScriptWizard extends NSISWizard
{
    private boolean mSaveAsTemplate = false;
    private boolean mCheckOverwrite = false;
    private NSISWizardTemplateManager mTemplateManager = new NSISWizardTemplateManager();
    /**
     *
     */
    public NSISScriptWizard()
    {
        super();
        setNeedsProgressMonitor(true);
        setWindowTitle(EclipseNSISPlugin.getResourceString("wizard.window.title")); //$NON-NLS-1$
    }

    public String getHelpContextId()
    {
        return INSISConstants.PLUGIN_CONTEXT_PREFIX+"nsis_wizard_context"; //$NON-NLS-1$
    }

    /**
     * @return Returns the templateManager.
     */
    public NSISWizardTemplateManager getTemplateManager()
    {
        return mTemplateManager;
    }

    public boolean isCheckOverwrite()
    {
        return mCheckOverwrite;
    }

    public void setCheckOverwrite(boolean checkOverwrite)
    {
        mCheckOverwrite = checkOverwrite;
    }

    private boolean saveTemplate()
    {
        NSISWizardTemplate template;
        if(getTemplate() != null) {
            template = (NSISWizardTemplate)getTemplate().clone();
        }
        else {
            template = new NSISWizardTemplate(""); //$NON-NLS-1$
            setTemplate(template);
        }
        NSISWizardTemplateDialog dialog = new NSISWizardTemplateDialog(getShell(),getTemplateManager(), template, getSettings());
        return(dialog.open() == Window.OK);
    }

    void loadTemplate(NSISWizardTemplate template)
    {
        setTemplate(template);
        setSettings(template.getSettings());
    }

    public boolean performFinish()
    {
        IPath path = new Path(getSettings().getSavePath());
        if(Common.isEmpty(path.getFileExtension())) {
            path = path.addFileExtension(INSISConstants.NSI_EXTENSION);
        }
        if(!path.isAbsolute()) {
            Common.openError(getShell(),EclipseNSISPlugin.getResourceString("absolute.save.path.error"),EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
            return false;
        }
        final boolean saveExternal = getSettings().isSaveExternal();
        String pathString = saveExternal?path.toOSString():path.toString();
        final boolean exists;
        final File file;
        final IFile ifile;
        if(saveExternal) {
            ifile = null;
            file = new File(pathString);
            exists = file.exists();
        }
        else {
            file = null;
            ifile = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            exists = ifile != null && ifile.exists();
            path = ifile.getLocation();
            if(path == null) {
                Common.openError(getShell(),EclipseNSISPlugin.getResourceString("local.filesystem.error"),EclipseNSISPlugin.getShellImage()); //$NON-NLS-1$
                return false;
            }
        }
        if(exists && mCheckOverwrite) {
            if(!Common.openQuestion(getShell(), EclipseNSISPlugin.getResourceString("question.title"), //$NON-NLS-1$
                    EclipseNSISPlugin.getFormattedString("save.path.question",new String[] {pathString}),  //$NON-NLS-1$
                    EclipseNSISPlugin.getShellImage())) {
                return false;
            }
            mCheckOverwrite = false;
        }
        getSettings().setSavePath(pathString);
        if(mSaveAsTemplate) {
            if(!saveTemplate()) {
                return false;
            }
        }
        java.util.List editors = NSISEditorUtilities.findEditors(path);
        if(!Common.isEmptyCollection(editors)) {
            java.util.List dirtyEditors = new ArrayList();
            for (Iterator iter = editors.iterator(); iter.hasNext();) {
                IEditorPart editor = (IEditorPart)iter.next();
                if(editor.isDirty()) {
                    dirtyEditors.add(editor);
                }
            }
            if(dirtyEditors.size() > 0) {
                if(!Common.openConfirm(getShell(), EclipseNSISPlugin.getFormattedString("save.dirty.editor.confirm",new String[] {pathString}),  //$NON-NLS-1$
                    EclipseNSISPlugin.getShellImage())) {
                    return false;
                }
                for (Iterator iter = dirtyEditors.iterator(); iter.hasNext();) {
                    IEditorPart editor = (IEditorPart)iter.next();
                    editor.getSite().getPage().closeEditor(editor,false);
                    editors.remove(editor);
                }

                if(saveExternal) {
                    for (Iterator iter = editors.iterator(); iter.hasNext();) {
                        IEditorPart editor = (IEditorPart)iter.next();
                        editor.getSite().getPage().closeEditor(editor,false);
                    }
                }
            }
        }
    	IRunnableWithProgress op = new IRunnableWithProgress() {
    		public void run(IProgressMonitor monitor) throws InvocationTargetException
            {
    			try {
                    if(exists) {
                        if(saveExternal) {
                            file.delete();
                        }
                        else {
                            ifile.delete(true,true,null);
                        }
                    }
                    new NSISWizardScriptGenerator(getSettings()).generate(getShell(),monitor);
                }
    			catch (Exception e) {
    			    throw new InvocationTargetException(e);
                }
    		}
    	};
    	try {
    		getContainer().run(true, false, op);
    	}
        catch (InterruptedException e) {
    		return false;
    	}
        catch (InvocationTargetException e) {
    		Throwable realException = e.getTargetException();
    		Common.openError(getShell(), realException.getLocalizedMessage(), EclipseNSISPlugin.getShellImage());
    		return false;
    	}
    	return true;
    }

    public boolean performCancel()
    {
        if(Common.openQuestion(getShell(),EclipseNSISPlugin.getResourceString("wizard.cancel.question"), //$NON-NLS-1$
                EclipseNSISPlugin.getShellImage())) {
            return super.performCancel();
        }
        else {
            return false;
        }
    }

    /**
     * @return Returns the saveAsTemplate.
     */
    public boolean isSaveAsTemplate()
    {
        return mSaveAsTemplate;
    }

    /**
     * @param saveAsTemplate The saveAsTemplate to set.
     */
    public void setSaveAsTemplate(boolean saveAsTemplate)
    {
        mSaveAsTemplate = saveAsTemplate;
    }

    /**
     *
     */
    protected void addStartPage()
    {
        addPage(new NSISWizardWelcomePage());
    }

    /**
     * @return Returns the template.
     */
    protected NSISWizardTemplate getTemplate()
    {
        return mTemplate;
    }

    /**
     * @param template The template to set.
     */
    protected void setTemplate(NSISWizardTemplate template)
    {
        mTemplate = template;
    }
}

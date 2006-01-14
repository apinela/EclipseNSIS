/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.codeassist.*;
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;
import net.sf.eclipsensis.makensis.MakeNSISResults;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.script.NSISScriptProblem;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.source.*;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;

public class NSISEditorUtilities
{
    private NSISEditorUtilities()
    {
    }
    
    static InformationPresenter createStickyHelpInformationPresenter()
    {
        boolean browserAvailable = false;
        if(Display.getCurrent() != null) {
            boolean newShell = false;
            Shell shell = Display.getCurrent().getActiveShell();
            if(shell == null) {
                newShell = true;
                shell = new Shell(Display.getCurrent());
            }
            browserAvailable = NSISBrowserInformationControl.isAvailable(shell);
            if(newShell) {
                shell.dispose();
            }
        }

        IInformationControlCreator informationControlCreator;
        NSISInformationProvider informationProvider;
        InformationPresenter informationPresenter;
        if(browserAvailable) {
            informationControlCreator = new NSISBrowserInformationControlCreator(SWT.V_SCROLL|SWT.H_SCROLL);
            informationProvider = new NSISBrowserInformationProvider();
            informationPresenter = new InformationPresenter(informationControlCreator);
        }
        else {
            informationControlCreator= new NSISHelpInformationControlCreator(new String[]{INSISConstants.GOTO_HELP_COMMAND_ID},SWT.V_SCROLL|SWT.H_SCROLL);
            informationProvider = new NSISInformationProvider();
            informationPresenter = new InformationPresenter(informationControlCreator);
        }
        informationProvider.setInformationPresenterControlCreator(informationControlCreator);
        informationPresenter.setInformationProvider(informationProvider,NSISPartitionScanner.NSIS_STRING);
        informationPresenter.setInformationProvider(informationProvider,IDocument.DEFAULT_CONTENT_TYPE);
        informationPresenter.setSizeConstraints(60, (browserAvailable?12:6), true, true);
        return informationPresenter;
    }
    
    public static void clearMarkers(final IPath path)
    {
        if(path != null && (!MakeNSISRunner.isCompiling() || !path.equals(MakeNSISRunner.getScript()))) {
            if(path.getDevice() == null) {
                final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                if(file.exists()) {
                    WorkspaceModifyOperation op = new WorkspaceModifyOperation(file)
                    {
                        protected void execute(IProgressMonitor monitor)throws CoreException
                        {
                            try {
                                file.deleteMarkers(INSISConstants.PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
                            }
                            catch (CoreException ex) {
                                EclipseNSISPlugin.getDefault().log(ex);
                            }
                            finally {
                                monitor.done();
                            }
                        }
                    };
                    try {
                        op.run(null);
                    }
                    catch (Exception ex) {
                        EclipseNSISPlugin.getDefault().log(ex);
                    }
                }
            }
            else {
                final File file = new File(path.toOSString());
                if(IOUtility.isValidFile(file)) {
                    NSISCompileTestUtility.INSTANCE.removeCachedResults(file);
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run()
                        {
                            updateAnnotations(file, null);
                        }
                    });
                }
            }
        }
    }

    public static boolean hasMarkers(IPath path)
    {
        if(path != null && (!MakeNSISRunner.isCompiling() || !path.equals(MakeNSISRunner.getScript()))) {
            if(path.getDevice() == null) {
                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                if(file.exists()) {
                    try {
                        IMarker[] markers = file.findMarkers(INSISConstants.PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
                        return !Common.isEmptyArray(markers);
                    }
                    catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
            }
            else {
                MakeNSISResults results = NSISCompileTestUtility.INSTANCE.getCachedResults(new File(path.toOSString()));
                if(results != null) {
                    return results.getProblems().size() > 0;
                }
            }
        }
        return false;
    }

    public static void gotoLine(IPath path, int lineNum)
    {
        if(path != null && lineNum > 0) {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if(window != null) {
                IWorkbenchPage page = window.getActivePage();
                if(page != null) {
                    IEditorReference[] editorRefs = page.getEditorReferences();
                    if (!Common.isEmptyArray(editorRefs)) {
                        for (int i = 0; i < editorRefs.length; i++) {
                            IEditorPart editor = editorRefs[i].getEditor(false);
                            if(editor != null) {
                                IEditorInput input = editor.getEditorInput();
                                if (path.getDevice() == null && input instanceof IFileEditorInput) {
                                    if (path.equals(((IFileEditorInput) input).getFile().getFullPath())) {
                                        page.activate(editor);
                                        IGotoMarker igm = (IGotoMarker)editor.getAdapter(IGotoMarker.class);
                                        if(igm != null) {
                                            igm.gotoMarker(new DummyMarker(ResourcesPlugin.getWorkspace().getRoot().getFile(path),lineNum));
                                        }
                                        return;
                                    }
                                }
                                else if(path.getDevice() != null) {
                                    gotoEditorLine(editor, lineNum);
                                }
                            }
                        }
                    }
                    try {
                        if (path.getDevice() == null) {
                            IDE.openEditor(page, new DummyMarker(ResourcesPlugin.getWorkspace().getRoot().getFile(path),lineNum), OpenStrategy.activateOnOpen());
                        }
                        else if(path.getDevice() != null) {
                            File file = new File(path.toOSString());
                            IEditorPart editor = IDE.openEditor(page,new NSISExternalFileEditorInput(file), INSISConstants.EDITOR_ID);
                            gotoEditorLine(editor, lineNum);
                        }
                    }
                    catch (PartInitException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
        }
    }

    public static void gotoEditorLine(IEditorPart editor, int lineNum)
    {
        if(editor instanceof TextEditor) {
            IDocument doc = ((TextEditor)editor).getDocumentProvider().getDocument(editor.getEditorInput());
            if(doc.getNumberOfLines() >= lineNum) {
                IRegion region;
                try {
                    region = doc.getLineInformation(lineNum-1);
                    String delim = doc.getLineDelimiter(lineNum-1);
                    ((TextEditor)editor).getSelectionProvider().setSelection(new TextSelection(doc,region.getOffset(),region.getLength()+(delim==null?0:delim.length())));
                }
                catch (BadLocationException e) {
                    EclipseNSISPlugin.getDefault().log(e);
                }
            }
        }
    }

    public static class DummyMarker implements IMarker
    {
        private IResource mResource = null;
        int mLine = -1;
    
        /**
         * @param resource
         * @param line
         */
        public DummyMarker(IResource resource, int line)
        {
            mResource = resource;
            mLine = line;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#delete()
         */
        public void delete() throws CoreException
        {
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#exists()
         */
        public boolean exists()
        {
            return true;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getAttribute(java.lang.String, boolean)
         */
        public boolean getAttribute(String attributeName, boolean defaultValue)
        {
            return defaultValue;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getAttribute(java.lang.String, int)
         */
        public int getAttribute(String attributeName, int defaultValue)
        {
            if(attributeName.equals(IMarker.LINE_NUMBER)) {
                return mLine;
            }
            else {
                return defaultValue;
            }
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getAttribute(java.lang.String, java.lang.String)
         */
        public String getAttribute(String attributeName, String defaultValue)
        {
            return defaultValue;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getAttribute(java.lang.String)
         */
        public Object getAttribute(String attributeName) throws CoreException
        {
            if(attributeName.equals(IMarker.LINE_NUMBER)) {
                return new Integer(mLine);
            }
            else {
                return null;
            }
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getAttributes()
         */
        public Map getAttributes() throws CoreException
        {
            return null;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getAttributes(java.lang.String[])
         */
        public Object[] getAttributes(String[] attributeNames)
                throws CoreException
        {
            Object[] values = new Object[attributeNames.length];
            for (int i = 0; i < values.length; i++) {
                values[i] = getAttribute(attributeNames[i]);
            }
            return values;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getCreationTime()
         */
        public long getCreationTime() throws CoreException
        {
            return System.currentTimeMillis();
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getId()
         */
        public long getId()
        {
            return 0;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getResource()
         */
        public IResource getResource()
        {
            return mResource;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getType()
         */
        public String getType() throws CoreException
        {
            return INSISConstants.PROBLEM_MARKER_ID;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#isSubtypeOf(java.lang.String)
         */
        public boolean isSubtypeOf(String superType) throws CoreException
        {
            return false;
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String, boolean)
         */
        public void setAttribute(String attributeName, boolean value)
                throws CoreException
        {
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String, int)
         */
        public void setAttribute(String attributeName, int value)
                throws CoreException
        {
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String, java.lang.Object)
         */
        public void setAttribute(String attributeName, Object value)
                throws CoreException
        {
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttributes(java.util.Map)
         */
        public void setAttributes(Map attributes) throws CoreException
        {
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttributes(java.lang.String[], java.lang.Object[])
         */
        public void setAttributes(String[] attributeNames, Object[] values)
                throws CoreException
        {
        }
    
        /* (non-Javadoc)
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        public Object getAdapter(Class adapter)
        {
            return null;
        }
    }

    public static void updateAnnotations(NSISEditor editor, MakeNSISResults results)
    {
        IAnnotationModel model = editor.getAnnotationModel();
        if(model instanceof AnnotationModel) {
            AnnotationModel annotationModel = (AnnotationModel)model;
            annotationModel.removeAllAnnotations();
            if (results != null) {
                List problems = results.getProblems();
                if (!Common.isEmptyCollection(problems)) {
                    IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
                    for (Iterator iter = problems.iterator(); iter.hasNext();) {
                        NSISScriptProblem problem = (NSISScriptProblem)iter.next();
                        int line = problem.getLine();
                        if (line >= 0) {
                            try {
                                String name;
                                if (problem.getType() == NSISScriptProblem.TYPE_ERROR) {
                                    name = INSISConstants.ERROR_ANNOTATION_NAME;
                                }
                                else if (problem.getType() == NSISScriptProblem.TYPE_WARNING) {
                                    name = INSISConstants.WARNING_ANNOTATION_NAME;
                                }
                                else {
                                    continue;
                                }
                                IRegion region = doc.getLineInformation(line > 0?line - 1:1);
                                annotationModel.addAnnotation(new Annotation(name, false, problem.getText()), new Position(region.getOffset(), (line > 0?region.getLength():0)));
                            }
                            catch (BadLocationException e) {
                                EclipseNSISPlugin.getDefault().log(e);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void updatePresentations()
    {
        final Collection editors = new ArrayList();
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int j = 0; j < pages.length; j++) {
                IEditorReference[] editorRefs = pages[i].getEditorReferences();
                for (int k = 0; k < editorRefs.length; k++) {
                    if(INSISConstants.EDITOR_ID.equals(editorRefs[k].getId())) {
                        NSISEditor editor = (NSISEditor)editorRefs[k].getEditor(false);
                        if(editor != null) {
                            editors.add(editor);
                        }
                    }
                }
            }
        }
        if(editors.size() > 0) {
            final IRunnableWithProgress op = new IRunnableWithProgress(){
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    monitor.beginTask(EclipseNSISPlugin.getResourceString("updating.presentation.message"),editors.size()); //$NON-NLS-1$
                    for(Iterator iter=editors.iterator(); iter.hasNext(); ) {
                        ((NSISEditor)iter.next()).updatePresentation();
                        monitor.worked(1);
                    }
                }
            };
            EclipseNSISPlugin.getDefault().run(false,false, op);
        }
    }

    public static void updateAnnotations(MakeNSISResults results)
    {
        updateAnnotations(results.getScriptFile(), results);
    }

    private static void updateAnnotations(File script, MakeNSISResults results)
    {
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int j = 0; j < pages.length; j++) {
                IEditorReference[] editorRefs = pages[j].getEditorReferences();
                for (int k = 0; k < editorRefs.length; k++) {
                    if(INSISConstants.EDITOR_ID.equals(editorRefs[k].getId())) {
                        NSISEditor editor = (NSISEditor)editorRefs[k].getEditor(false);
                        if(editor != null) {
                            IEditorInput input = editor.getEditorInput();
                            if(!(input instanceof IFileEditorInput) && input instanceof IPathEditorInput) {
                                if(script.getAbsolutePath().equalsIgnoreCase(((IPathEditorInput)input).getPath().toOSString())) {
                                    updateAnnotations(editor, results);
                                    editor.updateActionsState();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.io.File;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.console.NSISConsoleLine;
import net.sf.eclipsensis.editor.codeassist.*;
import net.sf.eclipsensis.editor.text.NSISPartitionScanner;
import net.sf.eclipsensis.makensis.*;
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
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.*;
import org.eclipse.ui.part.FileEditorInput;

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
            browserAvailable = NSISBrowserUtility.isBrowserAvailable(shell);
            if(newShell) {
                shell.dispose();
            }
        }

        final IInformationControlCreator informationControlCreator;
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
        informationPresenter.setSizeConstraints(60, (browserAvailable?14:6), true, true);
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
                        protected void execute(IProgressMonitor monitor)
                        {
                            try {
                                file.deleteMarkers(INSISConstants.PROBLEM_MARKER_ID, false, IResource.DEPTH_ZERO);
                            }
                            catch (CoreException ex) {
                                EclipseNSISPlugin.getDefault().log(ex);
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

    public static void gotoConsoleLineProblem(NSISConsoleLine line)
    {
        IPath path = line.getSource();
        int lineNum = line.getLineNum();
        if(path != null && lineNum > 0) {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if(window != null) {
                IWorkbenchPage page = window.getActivePage();
                if(page != null) {
                    IMarker marker = null;
                    NSISScriptProblem problem = line.getProblem();
                    if(problem != null) {
                        marker = problem.getMarker();
                    }
                    IEditorReference[] editorRefs = page.getEditorReferences();
                    if (!Common.isEmptyArray(editorRefs)) {
                        for (int i = 0; i < editorRefs.length; i++) {
                            IEditorPart editor = editorRefs[i].getEditor(false);
                            if(editor != null) {
                                IEditorInput input = editor.getEditorInput();
                                if (path.getDevice() == null && input instanceof IFileEditorInput) {
                                    if (path.equals(((IFileEditorInput) input).getFile().getFullPath())) {
                                        page.activate(editor);
                                        if(marker != null) {
                                            IGotoMarker igm = (IGotoMarker)editor.getAdapter(IGotoMarker.class);
                                            if(igm != null) {
                                                igm.gotoMarker(marker);
                                            }
                                        }
                                        else {
                                            setEditorSelection(editor, null, lineNum);
                                        }
                                    }
                                }
                                else if(path.getDevice() != null) {
                                    Position pos = null;
                                    if(marker instanceof PositionMarker) {
                                        pos = ((PositionMarker)marker).getPosition();
                                    }
                                    setEditorSelection(editor, pos, lineNum);
                                }
                            }
                        }
                    }
                    try {
                        if (path.getDevice() == null) {
                            if(marker != null) {
                                IDE.openEditor(page, marker, OpenStrategy.activateOnOpen());
                            }
                            else {
                                IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
                                if(file != null) {
                                    IEditorPart editor = IDE.openEditor(page,file,true);
                                    setEditorSelection(editor, null, lineNum);
                                }
                            }
                        }
                        else if(path.getDevice() != null) {
                            File file = new File(path.toOSString());
                            IEditorPart editor = IDE.openEditor(page,new NSISExternalFileEditorInput(file), INSISConstants.EDITOR_ID);
                            Position pos = null;
                            if(marker instanceof PositionMarker) {
                                pos = ((PositionMarker)marker).getPosition();
                            }
                            setEditorSelection(editor, pos, lineNum);
                        }
                    }
                    catch (PartInitException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
        }
    }

    private static void setEditorSelection(IEditorPart editor, Position pos, int lineNum)
    {
        if(editor instanceof TextEditor) {
            int offset = -1;
            int length = 0;
            IDocument doc = ((TextEditor)editor).getDocumentProvider().getDocument(editor.getEditorInput());
            if(pos != null) {
                if(pos.getOffset() >= 0 && doc.getLength() >= pos.getOffset()+pos.getLength()) {
                    offset = pos.getOffset();
                    length = pos.getLength();
                }
            }
            else {
                if(doc.getNumberOfLines() >= lineNum) {
                    try {
                        IRegion region = doc.getLineInformation(lineNum-1);
                        offset = region.getOffset();
                        length = region.getLength();
                    }
                    catch (BadLocationException e) {
                        EclipseNSISPlugin.getDefault().log(e);
                    }
                }
            }
            ((TextEditor)editor).getSelectionProvider().setSelection(new TextSelection(doc,offset,length));
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
                    IEditorInput editorInput = editor.getEditorInput();
                    IFile file = null;
                    if(editorInput instanceof IFileEditorInput) {
                        file = ((IFileEditorInput)editorInput).getFile();
                    }
                    IDocument doc = editor.getDocumentProvider().getDocument(editorInput);
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

                                Position position = new Position(region.getOffset(), (line > 0?region.getLength():0));
                                problem.setMarker(new PositionMarker(file,position));
                                annotationModel.addAnnotation(new Annotation(name, false, problem.getText()), position);
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
                public void run(IProgressMonitor monitor)
                {
                    try {
                        monitor.beginTask(EclipseNSISPlugin.getResourceString("updating.presentation.message"),editors.size()); //$NON-NLS-1$
                        for(Iterator iter=editors.iterator(); iter.hasNext(); ) {
                            ((NSISEditor)iter.next()).updatePresentation();
                            monitor.worked(1);
                        }
                    }
                    finally {
                        monitor.done();
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

    public static List findEditors(IPath path)
    {
        List editors = new ArrayList();
        IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < windows.length; i++) {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int j = 0; j < pages.length; j++) {
                IEditorReference[] editorRefs = pages[j].getEditorReferences();
                for (int k = 0; k < editorRefs.length; k++) {
                    IEditorPart editor = editorRefs[k].getEditor(false);
                    if(editor != null) {
                        IEditorInput input = editor.getEditorInput();
                        if(input instanceof IPathEditorInput && path.equals(((IPathEditorInput)input).getPath())) {
                            editors.add(editor);
                        }
                    }
                }
            }
        }
        return editors;
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

    public static void openAssociatedFiles(final IWorkbenchPage page, final IFile file)
    {
        if(file != null) {
            Runnable r = new Runnable() {
                public void run()
                {
                    IWorkbenchPage p = null;
                    if(page == null) {
                        IWorkbench workbench = PlatformUI.getWorkbench();
                        if (workbench != null) {
                            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                            if (window != null) {
                                p = window.getActivePage();
                            }
                        }
                    }
                    else {
                        p = page;
                    }
                    if(p == null) {
                        return;
                    }
                    IFile[] files = null;
                    String ext = file.getFileExtension();
                    if(Common.stringsAreEqual(INSISConstants.NSI_EXTENSION,ext,true)) {
                        files = (IFile[])NSISHeaderAssociationManager.getInstance().getAssociatedHeaders(file).toArray(new IFile[0]);
                    }
                    else if(Common.stringsAreEqual(INSISConstants.NSH_EXTENSION,ext,true)) {
                        files = new IFile[] {NSISHeaderAssociationManager.getInstance().getAssociatedScript(file)};
                    }

                    if(!Common.isEmptyArray(files)) {
                        for (int i = 0; i < files.length; i++) {
                            if(!files[i].exists()) {
                                IProject proj = files[i].getProject();
                                if(!proj.isOpen() && proj.exists()) {
                                    try {
                                        proj.open(new NullProgressMonitor());
                                    }
                                    catch (CoreException e) {
                                        EclipseNSISPlugin.getDefault().log(e);
                                        continue;
                                    }
                                }
                                else {
                                    continue;
                                }
                            }
                            try {
                                p.openEditor(new FileEditorInput(files[i]),INSISConstants.EDITOR_ID,false,IWorkbenchPage.MATCH_ID|IWorkbenchPage.MATCH_INPUT);
                            }
                            catch (PartInitException e) {
                                EclipseNSISPlugin.getDefault().log(e);
                            }
                        }
                    }
                }
            };
            if(Display.getCurrent() != null) {
                r.run();
            }
            else {
                Display.getDefault().asyncExec(r);
            }
        }
    }

    public static class PositionMarker implements IMarker
    {
        private IResource mResource = null;
        private Position mPosition = null;

        public PositionMarker(IResource resource, Position position)
        {
            mResource = resource;
            mPosition = position;
        }

        public Position getPosition()
        {
            return mPosition;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#delete()
         */
        public void delete()
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
            if(attributeName.equals(IMarker.CHAR_START)) {
                return (mPosition==null?defaultValue:mPosition.getOffset());
            }
            else if(attributeName.equals(IMarker.CHAR_END)) {
                return (mPosition==null?defaultValue:mPosition.getOffset()+mPosition.getLength());
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
        public Object getAttribute(String attributeName)
        {
            if(attributeName.equals(IMarker.CHAR_START)) {
                return (mPosition==null?null:new Integer(mPosition.getOffset()));
            }
            else if(attributeName.equals(IMarker.CHAR_END)) {
                return (mPosition==null?null:new Integer(mPosition.getOffset()+mPosition.getLength()));
            }
            else {
                return null;
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getAttributes()
         */
        public Map getAttributes()
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#getAttributes(java.lang.String[])
         */
        public Object[] getAttributes(String[] attributeNames)
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
        public long getCreationTime()
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
        public String getType()
        {
            return INSISConstants.PROBLEM_MARKER_ID;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#isSubtypeOf(java.lang.String)
         */
        public boolean isSubtypeOf(String superType)
        {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String, boolean)
         */
        public void setAttribute(String attributeName, boolean value)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String, int)
         */
        public void setAttribute(String attributeName, int value)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttribute(java.lang.String, java.lang.Object)
         */
        public void setAttribute(String attributeName, Object value)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttributes(java.util.Map)
         */
        public void setAttributes(Map attributes)
        {
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.resources.IMarker#setAttributes(java.lang.String[], java.lang.Object[])
         */
        public void setAttributes(String[] attributeNames, Object[] values)
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
}

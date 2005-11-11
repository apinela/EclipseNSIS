/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.io.File;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.actions.NSISCancelAction;
import net.sf.eclipsensis.console.model.*;
import net.sf.eclipsensis.editor.NSISExternalFileEditorInput;
import net.sf.eclipsensis.makensis.IMakeNSISRunListener;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.text.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.ViewPart;

public class NSISConsole extends ViewPart implements INSISConstants, IMakeNSISRunListener,
                                                     INSISPreferenceConstants, IPropertyChangeListener,
                                                     INSISConsoleModelListener

{
    private Color mInfoColor = null;
    private Color mWarningColor = null;
    private Color mErrorColor = null;
    private Image mErrorImage = null;
    private Image mWarningImage = null;
    private Image mInfoImage = null;

    private Font mFont = null;
    private Font mUnderlineFont = null;

	private TableViewer mViewer;
	private Action mCopyAction;
    private Action mSelectAllAction;
	private Action mClearAction;
    private Action mCancelAction;
    private boolean mDisposed = true;
    private Clipboard mClipboard = null;
    private Display mDisplay = null;
    private boolean mIsCompiling = false;
    private ColorRegistry mColorRegistry = null;
    private FontRegistry mFontRegistry = null;
    private NSISConsoleMouseListener mMouseListener = null;
    public static final NSISConsoleModel MODEL = new NSISConsoleModel();

    public static void autoShowConsole()
    {
        if(NSISPreferences.INSTANCE.isAutoShowConsole()) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run()
                {
                    try {
                        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IViewPart view = activePage.findView(CONSOLE_ID);
                        if(view == null) {
                            activePage.showView(CONSOLE_ID, null, IWorkbenchPage.VIEW_VISIBLE);
                        }
                        else {
                            activePage.bringToTop(view);
                        }
                    }
                    catch(PartInitException pie) {
                        EclipseNSISPlugin.getDefault().log(pie);
                    }
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        String property = event.getProperty();
        int type = -1;
        if(property.equals(CONSOLE_INFO_COLOR)) {
            mInfoColor = mColorRegistry.get(property);
            type = NSISConsoleLine.TYPE_INFO;
        }
        else if(property.equals(CONSOLE_WARNING_COLOR)) {
            mWarningColor = mColorRegistry.get(property);
            type = NSISConsoleLine.TYPE_WARNING;
        }
        else if(property.equals(CONSOLE_ERROR_COLOR)) {
            mErrorColor = mColorRegistry.get(property);
            type = NSISConsoleLine.TYPE_ERROR;
        }
        else if(property.equals(CONSOLE_FONT)) {
            mFont = mFontRegistry.get(CONSOLE_FONT);
            makeUnderlineFont();
            mViewer.refresh(true);
            return;
        }
        if(type != -1) {
            for (Iterator iter = NSISConsole.MODEL.getContents().iterator(); iter.hasNext();) {
                NSISConsoleLine element = (NSISConsoleLine)iter.next();
                if(element.getType() == type) {
                    mViewer.refresh(element, true);
                }
            }
        }
    }

    /**
     *
     */
    private void makeUnderlineFont()
    {
        FontData fontData = mFont.getFontData()[0];
        fontData.data.lfUnderline = 1;
        if(mUnderlineFont != null) {
            mUnderlineFont.dispose();
        }
        mUnderlineFont = new Font(mDisplay, fontData);
    }

    private void processModelEvent(NSISConsoleModelEvent event)
    {
        int type = event.getType();
        switch(type) {
            case NSISConsoleModelEvent.ADD:
                add(event.getLine());
                break;
            case NSISConsoleModelEvent.REMOVE:
                remove(event.getLine());
                break;
            case NSISConsoleModelEvent.CLEAR:
                clear();
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.console.model.INSISConsoleModelListener#modelChanged(net.sf.eclipsensis.console.model.NSISConsoleModelEvent)
     */
    public void modelChanged(final NSISConsoleModelEvent event)
    {
        if(Thread.currentThread() == mDisplay.getThread()) {
            processModelEvent(event);
        }
        else {
            mDisplay.asyncExec(
                    new Runnable() {
                        public void run()
                        {
                            try {
                                processModelEvent(event);
                            }
                            catch(Exception ex) {
                                EclipseNSISPlugin.getDefault().log(ex);
                            }
                        }
                    }
            );
        }
    }

    private void remove(NSISConsoleLine line)
    {
        mViewer.refresh();
        boolean state = (mViewer.getTable().getItemCount() > 0);
        mClearAction.setEnabled(state && !mIsCompiling);
        mSelectAllAction.setEnabled(state);
    }

    private void clear()
    {
        mViewer.refresh();
        mClearAction.setEnabled(false);
        mSelectAllAction.setEnabled(false);
    }

    private void add(NSISConsoleLine line)
    {
        mViewer.add(line);
        int index = NSISConsole.MODEL.getContents().size()-1;
        WinAPI.SendMessage (mViewer.getTable().handle,
                            0x1013, // LVM_ENSUREVISIBLE
                            index,
                            0 // FALSE
                            );
        mClearAction.setEnabled(!mIsCompiling);
        mSelectAllAction.setEnabled(true);
    }

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent)
    {
        mDisplay = parent.getDisplay();
        mColorRegistry = JFaceResources.getColorRegistry();
        mInfoColor = mColorRegistry.get(CONSOLE_INFO_COLOR);
        mWarningColor = mColorRegistry.get(CONSOLE_WARNING_COLOR);
        mErrorColor = mColorRegistry.get(CONSOLE_ERROR_COLOR);
        mColorRegistry.addListener(this);
        mErrorImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("error.icon")); //$NON-NLS-1$
        mWarningImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("warning.icon")); //$NON-NLS-1$
        int width = Math.max(mErrorImage.getBounds().width,mWarningImage.getBounds().width);
        int height = Math.max(mErrorImage.getBounds().height,mWarningImage.getBounds().height);
        Image tempImage = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("transparent.icon")); //$NON-NLS-1$
        ImageData imageData = tempImage.getImageData();
        imageData = imageData.scaledTo(width, height);
        mInfoImage = new Image(mDisplay,imageData);

        mFontRegistry = JFaceResources.getFontRegistry();
        mFont = mFontRegistry.get(CONSOLE_FONT);
        makeUnderlineFont();
        mFontRegistry.addListener(this);

        mClipboard = new Clipboard(mDisplay);
        Table table = new Table(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(table,PLUGIN_CONTEXT_PREFIX + "nsis_console_context"); //$NON-NLS-1$
		mViewer = new TableViewer(table);
        mMouseListener = new NSISConsoleMouseListener();

        table.setFont(mFont);
        table.addMouseListener(mMouseListener);
        table.addMouseMoveListener(mMouseListener);
		mViewer.setContentProvider(new NSISConsoleContentProvider());
		mViewer.setLabelProvider(new NSSConsoleLabelProvider());
		mViewer.setInput(NSISConsole.MODEL);
        NSISConsole.MODEL.addModelListener(this);
		makeActions();
		hookContextMenu();
        hookSelectionChangedAction();
		contributeToActionBars();
        MakeNSISRunner.addListener(this);
        mDisposed = false;
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose()
    {
        if(mInfoImage != null && !mInfoImage.isDisposed()) {
            mInfoImage.dispose();
        }
        NSISConsole.MODEL.removeModelListener(this);
        mColorRegistry.removeListener(this);
        mColorRegistry = null;
        mFontRegistry.removeListener(this);
        mFontRegistry = null;
        MakeNSISRunner.removeListener(this);
        if(mClipboard != null) {
            mClipboard.dispose();
            mClipboard = null;
        }
        super.dispose();
        mDisposed = true;
    }

	private void hookContextMenu()
    {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				NSISConsole.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(mViewer.getControl());
		mViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, mViewer);
	}

	private void contributeToActionBars()
    {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager)
    {
        manager.add(mCancelAction);
        manager.add(mCopyAction);
        manager.add(mSelectAllAction);
        manager.add(mClearAction);
	}

	private void fillContextMenu(IMenuManager manager)
    {
        fillLocalPullDown(manager);

		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager)
    {
        manager.add(mCancelAction);
        manager.add(mCopyAction);
        manager.add(mSelectAllAction);
        manager.add(mClearAction);
	}

    public void started()
    {
        setIsCompiling(true);
    }

    public void stopped()
    {
        setIsCompiling(false);
    }

    private synchronized void setIsCompiling(boolean isCompiling)
    {
        if(mIsCompiling != isCompiling) {
            if(!mDisposed) {
                mCancelAction.setEnabled(isCompiling);
                boolean b = NSISConsole.MODEL.getContents().size() > 0;
                mClearAction.setEnabled(!isCompiling && b);
                mSelectAllAction.setEnabled(b);
            }
            mIsCompiling = isCompiling;
        }
    }

    private Action makeAction(Action action, String text, String tooltipText, String image, String disabledImage,
                              ActionFactory globalActionFactory, boolean enabled)
    {
        if(!Common.isEmpty(text)) {
            action.setText(text);
        }
        if(!Common.isEmpty(tooltipText)) {
            action.setToolTipText(tooltipText);
        }
        if(!Common.isEmpty(image)) {
            action.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(image));
        }
        if(!Common.isEmpty(disabledImage)) {
            action.setDisabledImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(disabledImage));
        }
        action.setEnabled(enabled);
        if(globalActionFactory != null)  {
            getViewSite().getActionBars().setGlobalActionHandler(globalActionFactory.getId(),
                                                                 action);
        }
        return action;
    }

	private void makeActions()
    {
		mCopyAction = makeAction(
                                new Action() {
                        			public void run() {
                                        IStructuredSelection selection = (IStructuredSelection)mViewer.getSelection();
                                        if(selection != null) {
                                            String ls = System.getProperty("line.separator"); //$NON-NLS-1$
                                            StringBuffer buf = new StringBuffer();
                                            for(Iterator iter = selection.iterator(); iter.hasNext();) {
                                                buf.append(iter.next()).append(ls);
                                            }
                                            if(buf.length() > 0) {
                                                mClipboard.setContents(new Object[]{buf.toString()},
                                                                       new Transfer[]{TextTransfer.getInstance()});
                                            }
                                        }
                        			}
                        		},EclipseNSISPlugin.getResourceString("copy.action.name"),EclipseNSISPlugin.getResourceString("copy.action.tooltip"),EclipseNSISPlugin.getResourceString("copy.action.icon"),EclipseNSISPlugin.getResourceString("copy.action.disabled.icon"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                                ActionFactory.COPY,false);

        mSelectAllAction = makeAction(
                            new Action() {
                                public void run() {
                                    mViewer.setSelection(new StructuredSelection(NSISConsole.MODEL.getContents()));
                                }
                            },EclipseNSISPlugin.getResourceString("selectall.action.name"),EclipseNSISPlugin.getResourceString("selectall.action.tooltip"),EclipseNSISPlugin.getResourceString("selectall.action.icon"),null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            ActionFactory.SELECT_ALL,false);

		mClearAction = makeAction(
                            new Action() {
                    			public void run() {
                    				NSISConsole.MODEL.clear();
                    			}
                    		},EclipseNSISPlugin.getResourceString("clear.action.name"),EclipseNSISPlugin.getResourceString("clear.action.tooltip"),EclipseNSISPlugin.getResourceString("clear.action.icon"),EclipseNSISPlugin.getResourceString("clear.action.disabled.icon"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                            null,false);

        final NSISCancelAction cancelActionDelegate = new NSISCancelAction();
        mCancelAction = makeAction(
                new Action() {
                    public void run() {
                        cancelActionDelegate.init(this);
                        cancelActionDelegate.run(this);
                    }
                },EclipseNSISPlugin.getResourceString("cancel.action.name"),EclipseNSISPlugin.getResourceString("cancel.action.tooltip"),EclipseNSISPlugin.getResourceString("cancel.action.icon"),EclipseNSISPlugin.getResourceString("cancel.action.disabled.icon"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                null,false);
        cancelActionDelegate.init(mCancelAction);
        mViewer.getTable().addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                cancelActionDelegate.dispose();
            }
        });
	}

    private void gotoMarker(NSISConsoleLine line)
    {
        IPath path = line.getSource();
        int lineNum = line.getLineNum();
        if(path != null && lineNum > 0) {
            IEditorReference[] editorRefs = getSite().getPage().getEditorReferences();
            if (Common.isEmptyArray(editorRefs)) {
                for (int i = 0; i < editorRefs.length; i++) {
                    IEditorPart editor = editorRefs[i].getEditor(false);
                    if(editor != null) {
                        IEditorInput input = editor.getEditorInput();
                        if (path.getDevice() == null && input instanceof IFileEditorInput) {
                            if (path.equals(((IFileEditorInput) input).getFile().getFullPath())) {
                                getSite().getPage().activate(editor);
                                IGotoMarker igm = (IGotoMarker)editor.getAdapter(IGotoMarker.class);
                                if(igm != null) {
                                    igm.gotoMarker(new DummyMarker(ResourcesPlugin.getWorkspace().getRoot().getFile(path),lineNum));
                                }
                                return;
                            }
                        }
                        else if(path.getDevice() != null) {
                            gotoFileMarker(lineNum, editor);
                        }
                    }
                }
            }
            try {
                if (path.getDevice() == null) {
                    IDE.openEditor(getSite().getPage(), new DummyMarker(ResourcesPlugin.getWorkspace().getRoot().getFile(path),lineNum), OpenStrategy.activateOnOpen());
                }
                else if(path.getDevice() != null) {
                    File file = new File(path.toOSString());
                    IEditorPart editor = IDE.openEditor(getSite().getPage(),new NSISExternalFileEditorInput(file), INSISConstants.EDITOR_ID);
                    gotoFileMarker(lineNum, editor);
                }
            }
            catch (PartInitException e) {
                EclipseNSISPlugin.getDefault().log(e);
            }
        }
    }

    private void gotoFileMarker(int lineNum, IEditorPart editor)
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

    private void hookSelectionChangedAction()
    {
        mViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                mCopyAction.setEnabled((selection != null && !selection.isEmpty()));
            }
        });
    }

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus()
    {
		mViewer.getControl().setFocus();
	}

    /**
     * @return Returns the disposed.
     */
    public boolean isDisposed()
    {
        return mDisposed;
    }

    /**
     * @param table
     */
    private synchronized int selectItems(Table table, int startIndex, int oldEndIndex, int newEndIndex)
    {
        if(oldEndIndex != newEndIndex) {
            int d1 = oldEndIndex - startIndex;
            int d2 = newEndIndex - startIndex;
            if(d1 == 0) {
                if(d2 > 0) {
                    table.select(startIndex+1,newEndIndex);
                }
                else {
                    table.select(newEndIndex,startIndex-1);
                }
            }
            else {
                if(d2 == 0) {
                    if(d1 > 0) {
                        table.deselect(startIndex+1,oldEndIndex);
                    }
                    else {
                        table.deselect(oldEndIndex,startIndex-1);
                    }
                }
                else {
                    if(d1/d2 < 0) {
                        if(d1 > 0) {
                            table.deselect(startIndex+1,oldEndIndex);
                            table.select(newEndIndex,startIndex-1);
                        }
                        else {
                            table.deselect(oldEndIndex,startIndex-1);
                            table.select(startIndex+1,newEndIndex);
                        }
                    }
                    else {
                        if(d1 > 0) {
                            if(d1 > d2) {
                                table.deselect(newEndIndex+1,oldEndIndex);
                            }
                            else {
                                table.select(oldEndIndex+1,newEndIndex);
                            }
                        }
                        else {
                            if(d1 > d2) {
                                table.select(newEndIndex,oldEndIndex-1);
                            }
                            else {
                                table.deselect(oldEndIndex,newEndIndex-1);
                            }
                        }
                    }
                }
            }
        }
        return newEndIndex;
    }

    private class NSISConsoleContentProvider implements IStructuredContentProvider
    {
        private Object mInput = null;

        public void inputChanged(Viewer v, Object oldInput, Object newInput)
        {
            mInput = newInput;
        }

        public void dispose()
        {
        }

        public Object[] getElements(Object parent) {
            if(mInput instanceof NSISConsoleModel) {
                return ((NSISConsoleModel)mInput).getContents().toArray();
            }
            else {
                return null;
            }
        }
    }

    private class NSSConsoleLabelProvider extends LabelProvider implements IFontProvider, ITableLabelProvider, IColorProvider
    {
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element)
        {
            Font font = mFont;
            if(element instanceof NSISConsoleLine) {
                NSISConsoleLine line = (NSISConsoleLine)element;
                if(line.getSource() != null && line.getLineNum() >= 0) {
                    font = mUnderlineFont;
                }
            }
            return font;
        }

        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }

        public Image getColumnImage(Object obj, int index) {
            if(obj instanceof NSISConsoleLine) {
                NSISConsoleLine line = (NSISConsoleLine)obj;
                switch(line.getType()) {
                    case NSISConsoleLine.TYPE_ERROR:
                        return mErrorImage;
                    case NSISConsoleLine.TYPE_WARNING:
                        return mWarningImage;
                }
            }
            return mInfoImage;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
         */
        public Color getBackground(Object element)
        {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
         */
        public Color getForeground(Object element)
        {
            Color color;
            if(element instanceof NSISConsoleLine) {
                NSISConsoleLine line = (NSISConsoleLine)element;
                switch(line.getType()) {
                    case NSISConsoleLine.TYPE_INFO:
                        color = mInfoColor;
                        break;
                    case NSISConsoleLine.TYPE_WARNING:
                        color = mWarningColor;
                        break;
                    case NSISConsoleLine.TYPE_ERROR:
                        color = mErrorColor;
                        break;
                    default:
                        color = mInfoColor;
                }
            }
            else {
                color = mInfoColor;
            }
            return color;
        }
    }

    private class DummyMarker implements IMarker
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
            return PROBLEM_MARKER_ID;
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

    private class NSISConsoleMouseListener extends MouseAdapter implements MouseMoveListener
    {
        private boolean mMouseDown = false;
        private boolean mDragging = false;
        private int mStartIndex = -1;
        private int mEndIndex = -1;
        private int mStartX = 0;
        private NSISConsoleScroller mScroller = null;

        public void mouseDown(MouseEvent e)
        {
            if (e.button != 1) {
                return;
            }
            mMouseDown = true;
            if(isEscaped(e)) {
                return;
            }
            mStartIndex = mEndIndex = getItemIndex(new Point(e.x,e.y));
            if(mStartIndex >= 0) {
                Table table = mViewer.getTable();
                mStartX = table.getItem(mStartIndex).getBounds(0).x;
                if(mStartIndex > 0) {
                    table.deselect(0,mStartIndex-1);
                }
                if(mStartIndex < (table.getItemCount()-1)) {
                    table.deselect(mStartIndex+1,table.getItemCount()-1);
                }
            }
        }

        /**
         * @param e
         * @return
         */
        private boolean isEscaped(MouseEvent e)
        {
            return (e.stateMask & SWT.CTRL) > 0 || (e.stateMask & SWT.SHIFT) > 0;
        }

        private int getItemIndex(Point p)
        {
            TableItem item = mViewer.getTable().getItem(p);
            if(item != null) {
                return mViewer.getTable().indexOf(item);
            }

            return -1;
        }

        private NSISConsoleLine getLine(Point p)
        {
            NSISConsoleLine line = null;
            int i = getItemIndex(p);
            List contents = NSISConsole.MODEL.getContents();
            if(i >= 0 && i < contents.size()) {
                line = (NSISConsoleLine)contents.get(i);
            }

            return line;
        }

        public void mouseUp(MouseEvent e)
        {
            if (e.button != 1) {
                return;
            }
            mMouseDown = false;
            mStartIndex = mEndIndex = -1;
            mStartX = 0;
            Table table = mViewer.getTable();

            NSISConsoleLine line = getLine(new Point(e.x,e.y));
            if (mDragging) {
                mDragging = false;
                if(mScroller != null) {
                    mScroller.stop();
                    mScroller = null;
                }
                if (line != null) {
                    table.setCursor(mDisplay.getSystemCursor(SWT.CURSOR_HAND));
                }
            }
            else if(line != null && line.getSource() != null && line.getLineNum() >= 0) {
                table.setCursor(mDisplay.getSystemCursor(SWT.CURSOR_WAIT));
                gotoMarker(line);
                table.setCursor(null);
            }
        }

        public void mouseMove(final MouseEvent e)
        {
            Table table = mViewer.getTable();
            if (mMouseDown) {
                if (!mDragging) {
                    table.setCursor(null);
                }
                mDragging = true;
                if(isEscaped(e)) {
                    return;
                }

                boolean upwards = e.y < 0;
                if(upwards || e.y >= table.getClientArea().height) {
                    if(mScroller != null) {
                        if(mScroller.isRunning() && mScroller.isUpwards() == upwards) {
                            return;
                        }
                        mScroller.stop();
                        mEndIndex = mScroller.getEndIndex();
                        mScroller = null;
                    }
                    mScroller = new NSISConsoleScroller(mStartIndex, mEndIndex, upwards);
                    new Thread(mScroller).start();
                }
                else {
                    if(mScroller != null) {
                        mScroller.stop();
                        mEndIndex = mScroller.getEndIndex();
                        mScroller = null;
                    }
                    int index = getItemIndex(new Point(mStartX,e.y));
                    if(index >= 0 && mStartIndex >= 0) {
                        mEndIndex = selectItems(table, mStartIndex, mEndIndex, index);
                    }
                }
            }
            NSISConsoleLine line = getLine(new Point(e.x,e.y));
            if (line != null && line.getSource() != null && line.getLineNum() >= 0) {
                table.setCursor(mDisplay.getSystemCursor(SWT.CURSOR_HAND));
            }
            else {
                table.setCursor(null);
            }
        }
    }

    private class NSISConsoleScroller implements Runnable
    {
        private int mStartIndex = 0;
        private int mEndIndex = 0;

        private int mTopIndex = 0;
        private boolean mUpwards = false;
        private boolean mRunning = true;

        public NSISConsoleScroller(int startIndex, int endIndex, boolean upwards)
        {
            mStartIndex = startIndex;
            mUpwards = upwards;

            Table table = mViewer.getTable();
            mTopIndex = table.getTopIndex();
            TableItem item = table.getItem(new Point(table.getItem(mTopIndex).getBounds(0).x,table.getClientArea().height-2));
            int bottomIndex = (item == null?table.getItemCount()-1:table.indexOf(item));
            mEndIndex = selectItems(table,mStartIndex,endIndex,(upwards?mTopIndex:bottomIndex));
        }

        public boolean isRunning()
        {
            return mRunning;
        }

        public int getEndIndex()
        {
            return mEndIndex;
        }

        public boolean isUpwards()
        {
            return mUpwards;
        }

        /* (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            final Table table = mViewer.getTable();
            if(mUpwards) {
                while(mRunning && mTopIndex >= 0) {
                    mTopIndex = --mTopIndex;
                    mDisplay.syncExec(new Runnable() {
                        public void run()
                        {
                            if(isRunning() && mTopIndex >= 0) {
                                table.setTopIndex(mTopIndex);
                                mEndIndex = selectItems(table,mStartIndex,mEndIndex,mTopIndex);
                                return;
                            }
                            stop();
                        }
                    });
                }
            }
            else {
                while(mRunning && mTopIndex < NSISConsole.MODEL.getContents().size()) {
                    mTopIndex = ++mTopIndex;
                    mDisplay.syncExec(new Runnable() {
                        public void run()
                        {
                            if(isRunning() && mTopIndex < NSISConsole.MODEL.getContents().size()) {
                                table.setTopIndex(mTopIndex);
                                TableItem item = table.getItem(new Point(table.getItem(mTopIndex).getBounds(0).x,table.getClientArea().height-2));
                                if(item != null) {
                                    table.showItem(item);
                                    mTopIndex = table.getTopIndex();
                                    int bottomIndex = table.indexOf(item);
                                    mEndIndex = selectItems(table, mStartIndex, mEndIndex, bottomIndex);
                                    return;
                                }
                            }
                            stop();
                        }
                    });
                }
            }
            mRunning = false;
        }

        public void stop()
        {
            mRunning = false;
        }
    }
}
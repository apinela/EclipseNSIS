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

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.actions.NSISCancelAction;
import net.sf.eclipsensis.console.model.*;
import net.sf.eclipsensis.makensis.IMakeNSISRunListener;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.settings.INSISPreferenceConstants;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.util.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
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
    
    private Font mFont = null;
    private Font mUnderlineFont = null;

    private NSISConsoleModel mModel;
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

    public static void autoShowConsole()
    {
        if(NSISPreferences.getPreferences().isAutoShowConsole()) {
            PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                public void run()
                {
                    try {
                        IViewPart view = null;
                        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                        IViewReference[] viewRefs = activePage.getViewReferences(); 
                        for (int i = 0; i < viewRefs.length; i++) {
                            if(viewRefs[i].getId().equals(CONSOLE_ID)) {
                                view = viewRefs[i].getView(true);
                                break;
                            }
                        }
                        if(view == null) {
                            activePage.showView(CONSOLE_ID);
                        }
                        else {
                            activePage.activate(view);
                        }
                    }
                    catch(PartInitException pie) {
                        pie.printStackTrace();
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
        boolean changed = false;
        int type = -1;
        if(property.equals(CONSOLE_INFO_COLOR)) {
            mInfoColor = mColorRegistry.get(property);
            type = NSISConsoleLine.INFO;
        }
        else if(property.equals(CONSOLE_WARNING_COLOR)) {
            mWarningColor = mColorRegistry.get(property);
            type = NSISConsoleLine.WARNING;
        }
        else if(property.equals(CONSOLE_ERROR_COLOR)) {
            mErrorColor = mColorRegistry.get(property);
            type = NSISConsoleLine.ERROR;
        }
        else if(property.equals(CONSOLE_FONT)) {
            mFont = mFontRegistry.get(CONSOLE_FONT);
            makeUnderlineFont();
            mViewer.refresh(true);
            return;
        }
        if(type != -1) {
            for (Iterator iter = mModel.getContents().iterator(); iter.hasNext();) {
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
                            processModelEvent(event);
                        }
                    }
            );
        }
    }
    
    private void remove(NSISConsoleLine line)
    {
        mViewer.refresh();
        boolean state = (mViewer.getTable().getItemCount() > 0);
        mClearAction.setEnabled(state);
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
        mViewer.refresh(false);
        mViewer.reveal(line);
        mClearAction.setEnabled(true);
        mSelectAllAction.setEnabled(true);
    }
    
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) 
    {
        mDisplay = parent.getDisplay();
        mModel = NSISConsoleModel.getInstance();
        
        mColorRegistry = JFaceResources.getColorRegistry();
        mInfoColor = mColorRegistry.get(CONSOLE_INFO_COLOR);
        mWarningColor = mColorRegistry.get(CONSOLE_WARNING_COLOR);
        mErrorColor = mColorRegistry.get(CONSOLE_ERROR_COLOR);
        mColorRegistry.addListener(this);
        
        mFontRegistry = JFaceResources.getFontRegistry();
        mFont = mFontRegistry.get(CONSOLE_FONT);
        makeUnderlineFont();
        mFontRegistry.addListener(this);
        
        mClipboard = new Clipboard(mDisplay);
		mViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        mMouseListener = new NSISConsoleMouseListener(mViewer);
        
        Table table = mViewer.getTable();
        table.setFont(mFont);
        table.addMouseListener(mMouseListener);
        table.addMouseMoveListener(mMouseListener);
		mViewer.setContentProvider(new NSISConsoleContentProvider());
		mViewer.setLabelProvider(new NSSConsoleLabelProvider());
		mViewer.setInput(mModel);
        mModel.addModelListener(this);
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
        mModel.removeModelListener(this);
        mModel = null;
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
        manager.add(mCopyAction);
        manager.add(mSelectAllAction);
        if(mIsCompiling) {
            manager.add(mCancelAction);
        }
        else {
            manager.add(mClearAction);
        }
	}

	private void fillContextMenu(IMenuManager manager) 
    {
        fillLocalPullDown(manager);
        
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) 
    {
        manager.add(mCopyAction);
        manager.add(mSelectAllAction);
        if(mIsCompiling) {
            manager.add(mCancelAction);
        }
        else {
            manager.add(mClearAction);
        }
	}

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.makensis.IMakeNSISRunListener#started()
     */
    public void started()
    {
        setIsCompiling(true);
    }
    
    /* (non-Javadoc)
     * @see net.sf.eclipsensis.makensis.IMakeNSISRunListener#stopped()
     */
    public void stopped()
    {
        setIsCompiling(false);
    }
    
    private synchronized void setIsCompiling(boolean isCompiling)
    {
        if(mIsCompiling != isCompiling) {
            if(!mDisposed) {
                IActionBars bars = getViewSite().getActionBars();
                final IToolBarManager toolBarManager = bars.getToolBarManager();
                IMenuManager menuManager = bars.getMenuManager();
                if(isCompiling) {
                    mCancelAction.setEnabled(true);
                    ActionContributionItem actionContributionItem = new ActionContributionItem(mClearAction);
                    menuManager.remove(actionContributionItem);
                    toolBarManager.remove(actionContributionItem);
                    menuManager.add(mCancelAction);
                    toolBarManager.add(mCancelAction);
                }
                else {
                    mCancelAction.setEnabled(false);
                    ActionContributionItem actionContributionItem = new ActionContributionItem(mCancelAction);
                    menuManager.remove(actionContributionItem);
                    toolBarManager.remove(actionContributionItem);
                    menuManager.add(mClearAction);
                    toolBarManager.add(mClearAction);
                }
                mDisplay.asyncExec(new Runnable() {
                    public void run()
                    {
                        toolBarManager.update(true);
                    }
                });
            }
            mIsCompiling = isCompiling;
        }
    }
    
    private Action makeAction(Action action, String text, String tooltipText, String image, String disabledImage,
                              ActionFactory globalActionFactory, boolean enabled)
    {
        if(!Common.isEmpty(text)) action.setText(text);
        if(!Common.isEmpty(tooltipText)) action.setToolTipText(tooltipText);
        if(!Common.isEmpty(image)) action.setImageDescriptor(ImageManager.getImageDescriptor(image));
        if(!Common.isEmpty(disabledImage)) action.setDisabledImageDescriptor(ImageManager.getImageDescriptor(disabledImage));
        action.setEnabled(enabled);
        if(globalActionFactory != null)  getViewSite().getActionBars().setGlobalActionHandler(
                                                                        globalActionFactory.getId(),
                                                                        action);
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
                                    mViewer.setSelection(new StructuredSelection(mModel.getContents()));
                                }
                            },EclipseNSISPlugin.getResourceString("selectall.action.name"),EclipseNSISPlugin.getResourceString("selectall.action.tooltip"),EclipseNSISPlugin.getResourceString("selectall.action.icon"),null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            ActionFactory.SELECT_ALL,false);
        
		mClearAction = makeAction( 
                            new Action() {
                    			public void run() {
                    				mModel.clear();
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
                    
                    public void dispose() {
                        cancelActionDelegate.dispose();
                    }
                },EclipseNSISPlugin.getResourceString("cancel.action.name"),EclipseNSISPlugin.getResourceString("cancel.action.tooltip"),EclipseNSISPlugin.getResourceString("cancel.action.icon"),EclipseNSISPlugin.getResourceString("cancel.action.disabled.icon"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                null,false);
        cancelActionDelegate.init(mCancelAction);
	}

    private void gotoMarker(NSISConsoleLine line)
    {
        IFile file = line.getFile();
        int lineNum = line.getLineNum();
        if(file != null && lineNum > 0) {
            IEditorReference[] editorRefs = getSite().getPage().getEditorReferences();
            if (Common.isEmptyArray(editorRefs)) {
                for (int i = 0; i < editorRefs.length; i++) {
                    IEditorPart editor = editorRefs[i].getEditor(false);
                    if(editor != null) {
                        IEditorInput input = editor.getEditorInput();
                        if (input instanceof IFileEditorInput) {
                            if (file.equals(((IFileEditorInput) input).getFile())) {
                                getSite().getPage().activate(editor);
                                IGotoMarker igm = (IGotoMarker)editor.getAdapter(IGotoMarker.class);
                                if(igm != null) {
                                    igm.gotoMarker(new DummyMarker(file,lineNum));
                                }
                                return;
                            }
                        }
                    }
                }
            }
            try {
                IDE.openEditor(getSite().getPage(), new DummyMarker(file,lineNum), OpenStrategy.activateOnOpen());
            }
            catch (PartInitException e) {
                e.printStackTrace();
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

    private void showMessage(String message) 
    {
		MessageDialog.openInformation(
			mViewer.getControl().getShell(),
			getTitle(),
			message);
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
            if(mInput != null && mInput instanceof NSISConsoleModel) {
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
                if(line.getFile() != null && line.getLineNum() >= 0) {
                    font = mUnderlineFont;
                }
            }
            return font;
        }
        
        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }
        public Image getColumnImage(Object obj, int index) {
            return null;
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
                    case NSISConsoleLine.INFO:
                        color = mInfoColor;
                        break;
                    case NSISConsoleLine.WARNING:
                        color = mWarningColor;
                        break;
                    case NSISConsoleLine.ERROR:
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
            return PROBLEM_ID;
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
        private TableViewer mTableViewer = null;
        
        public NSISConsoleMouseListener(TableViewer tableViewer)
        {
            mTableViewer = tableViewer;
        }
        
        public void mouseDown(MouseEvent e)
        {
            if (e.button != 1) {
                return;
            }
            mMouseDown = true;
        }
        
        private NSISConsoleLine getLine(Point p)
        {
            NSISConsoleLine line = null;
            TableItem item = mTableViewer.getTable().getItem(p);
            if(item != null) {
                List contents = mModel.getContents();
                int i = mTableViewer.getTable().indexOf(item);
                if(i >= 0 && i < contents.size()) {
                    line = (NSISConsoleLine)contents.get(i);
                }
            }
            
            return line;
        }
        
        public void mouseUp(MouseEvent e) 
        {
            mMouseDown = false;
            Table table = mTableViewer.getTable();

            NSISConsoleLine line = getLine(new Point(e.x,e.y));
            if (mDragging) {
                mDragging = false;
                if (line != null) {
                    table.setCursor(mDisplay.getSystemCursor(SWT.CURSOR_HAND));
                }
            } 
            else if(line != null && line.getFile() != null && line.getLineNum() >= 0) {
                table.setCursor(mDisplay.getSystemCursor(SWT.CURSOR_WAIT));
                gotoMarker(line);
                table.setCursor(null);
            }
        }
        
        public void mouseMove(MouseEvent e) 
        {
            Table table = mTableViewer.getTable();
            if (mMouseDown) {
                if (!mDragging) {
                    table.setCursor(null);
                }
                mDragging = true;
                return;
            }
            NSISConsoleLine line = getLine(new Point(e.x,e.y));
            if (line != null && line.getFile() != null && line.getLineNum() >= 0) {
                table.setCursor(mDisplay.getSystemCursor(SWT.CURSOR_HAND));
            }
            else {
                table.setCursor(null);
            }
        }
    }
}
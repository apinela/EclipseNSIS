/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;


import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.actions.NSISCancelAction;
import net.sf.eclipsensis.makensis.IMakeNSISRunListener;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

public class NSISConsole extends ViewPart implements INSISConstants, IMakeNSISRunListener
{
    private static NSISConsole cConsole = null;
    
	private TableViewer mViewer;
	private Action mCopyAction;
    private Action mSelectAllAction;
	private Action mClearAction;
    private Action mCancelAction;
	private Action mDoubleClickAction;
    private ArrayList mContent = new ArrayList();
    private ArrayList mErrors = new ArrayList();
    private HashSet mListeners = new HashSet();
    private boolean mDisposed = false;
    private Clipboard mClipboard = null;
    private Display mDisplay = null;
    private boolean mIsCompiling = false;

    public static NSISConsole getConsole()
    {
        if(cConsole == null || cConsole.isDisposed()) {
            synchronized(NSISConsole.class) {
                if(cConsole == null || cConsole.isDisposed()) {
                    PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
                        public void run()
                        {
                            try {
                                PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(CONSOLE_ID);
                            }
                            catch(PartInitException pie) {
                            }
                        }
                    });
                }
            }
        }
        return cConsole;
    }
    
	/**
	 * The constructor.
	 */
	public NSISConsole() 
    {
        cConsole = this;
	}
    
    public void clear()
    {
        mContent.clear();
        mErrors.clear();
        if(Thread.currentThread() == mDisplay.getThread()) {
            _clear();
        }
        else {
            mDisplay.asyncExec(
                    new Runnable() {
                        public void run()
                        {
                            _clear();
                        }
                    }
            );
        }
    }
    
    private void _clear()
    {
        mViewer.refresh();
        mClearAction.setEnabled(false);
        mSelectAllAction.setEnabled(false);
    }
    
    public void add(final NSISConsoleLine line)
    {
        mContent.add(line);
        if(line.getType() == NSISConsoleLine.ERROR) {
            mErrors.add(line);
        }
        if(Thread.currentThread() == mDisplay.getThread()) {
            _add(line);
        }
        else {
            mDisplay.asyncExec(
                    new Runnable() {
                        public void run()
                        {
                            _add(line);
                        }
                    }
            );
        }
    }
    
    private void _add(final NSISConsoleLine line)
    {
        mViewer.add(line);
        mViewer.reveal(line);
        mClearAction.setEnabled(true);
        mSelectAllAction.setEnabled(true);
    }
    
    /**
     * @return Returns the errors.
     */
    public List getErrors()
    {
        return (mErrors==null?null:Collections.unmodifiableList(mErrors));
    }

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
        mDisplay = parent.getDisplay();
        mClipboard = new Clipboard(mDisplay);
		mViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		mViewer.setContentProvider(new NSISConsoleContentProvider());
		mViewer.setLabelProvider(new NSSConsoleLabelProvider());
		mViewer.setInput(getViewSite());
		makeActions();
		hookContextMenu();
        hookSelectionChangedAction();
		hookDoubleClickAction();
		contributeToActionBars();
        MakeNSISRunner.addListener(this);
	}

	private void hookContextMenu() {
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

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
        manager.add(mCopyAction);
        manager.add(mSelectAllAction);
        if(mIsCompiling) {
            manager.add(mCancelAction);
        }
        else {
            manager.add(mClearAction);
        }
	}

	private void fillContextMenu(IMenuManager manager) {
        fillLocalPullDown(manager);
        
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
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
    
	private void makeActions() {
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
                                    mViewer.setSelection(new StructuredSelection(mContent));
                                }
                            },EclipseNSISPlugin.getResourceString("selectall.action.name"),EclipseNSISPlugin.getResourceString("selectall.action.tooltip"),EclipseNSISPlugin.getResourceString("selectall.action.icon"),null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            ActionFactory.SELECT_ALL,false);
        
		mClearAction = makeAction( 
                            new Action() {
                    			public void run() {
                    				clear();
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

		mDoubleClickAction = new Action() {
			public void run() {
				ISelection selection = mViewer.getSelection();
                if(selection instanceof IStructuredSelection) {
    				Object obj = ((IStructuredSelection)selection).getFirstElement();
                    if(obj !=null && obj instanceof NSISConsoleLine) {
                        NSISConsoleLine line = (NSISConsoleLine)obj;
                        IFile file = line.getFile();
                        int lineNum = line.getLineNum();
                        if(file != null && lineNum > 0) {
                            IEditorPart editor = getSite().getPage().getActiveEditor();
                            if (editor != null) {
                                IEditorInput input = editor.getEditorInput();
                                if (input instanceof IFileEditorInput) {
                                    if (file.equals(((IFileEditorInput) input).getFile())) {
                                        getSite().getPage().activate(editor);
                                    }
                                }
                            }
                            else {
                                try {
                                    IDE.openEditor(getSite().getPage(), new DummyMarker(file,lineNum), OpenStrategy.activateOnOpen());
                                }
                                catch (PartInitException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
			}
		};
	}

	private void hookDoubleClickAction() {
		mViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				mDoubleClickAction.run();
			}
		});
	}

    private void hookSelectionChangedAction() {
        mViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                mCopyAction.setEnabled((selection != null && !selection.isEmpty()));
            }
        });
    }

	private void showMessage(String message) {
		MessageDialog.openInformation(
			mViewer.getControl().getShell(),
			"NSIS Console", //$NON-NLS-1$
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		mViewer.getControl().setFocus();
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose()
    {
        super.dispose();
        MakeNSISRunner.removeListener(this);
        cConsole = null;
        if(mClipboard != null) {
            mClipboard.dispose();
            mClipboard = null;
        }
        mDisposed = true;
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
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }
        
        public void dispose() 
        {
        }
        
        public Object[] getElements(Object parent) {
            return mContent.toArray();
        }
    }
    
    private class NSSConsoleLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider 
    {
        private Color blue = null;
        private Color orange = null;
        private Color red = null;
        private Color white = null;

        /**
         * 
         */
        public NSSConsoleLabelProvider()
        {
            super();
            blue = new Color(mDisplay, 0,0,255);
            orange = new Color(mDisplay, 255,128,0);
            red = new Color(mDisplay,255,0,0);
            white = new Color(mDisplay, 255,255,255);
        }
        
        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }
        public Image getColumnImage(Object obj, int index) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        public void dispose()
        {
            super.dispose();
            blue.dispose();
            orange.dispose();
            red.dispose();
            white.dispose();
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
         */
        public Color getBackground(Object element)
        {
            if(element instanceof NSISConsoleLine) {
                NSISConsoleLine line = (NSISConsoleLine)element;
                if(line.getType() == NSISConsoleLine.ERROR && line.getFile() != null && line.getLineNum() > 0) {
                    return red;
                }
            }
            return white;
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
                        color = blue;
                        break;
                    case NSISConsoleLine.WARNING:
                        color = orange;
                        break;
                    case NSISConsoleLine.ERROR:
                        if(line.getFile() != null && line.getLineNum() > 0) {
                            color = white;
                        }
                        else {
                            color = red;
                        }
                        break;
                    default:
                        color = blue;
                }
            }
            else {
                color = blue;
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
}
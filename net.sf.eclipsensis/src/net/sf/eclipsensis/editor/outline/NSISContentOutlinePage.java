/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.dialogs.NSISOutlineFilterDialog;
import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.CommonImages;

import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

public class NSISContentOutlinePage extends ContentOutlinePage
{
    private Object mInput;
    private NSISEditor mEditor;
    private boolean mDisposed = false;

    /**
     * Creates a content outline page using the given provider and the given
     * editor.
     */
    public NSISContentOutlinePage(NSISEditor editor)
    {
        super();
        mEditor = editor;
    }

    void refresh()
    {
        TreeViewer viewer = getTreeViewer();
        if(viewer != null) {
            Object input = viewer.getInput();
            viewer.setInput(null);
            viewer.setInput(input);
            viewer.expandAll();
        }
    }

    private void revealSelection()
    {
        IStructuredSelection sel = (IStructuredSelection)getTreeViewer().getSelection();
        if(!sel.isEmpty()) {
            getTreeViewer().reveal(sel.getFirstElement());
        }
    }

    private void makeActions()
    {
        IAction collapseAllAction;
        IAction expandAllAction;
        IAction filterAction;

        IToolBarManager tbm = getSite().getActionBars().getToolBarManager();
        IMenuManager mm = getSite().getActionBars().getMenuManager();

        expandAllAction = new Action() {
            public void run()
            {
                getTreeViewer().expandAll();
                revealSelection();
            }
        };
        expandAllAction.setText(EclipseNSISPlugin.getResourceString("expandall.text")); //$NON-NLS-1$
        expandAllAction.setToolTipText(EclipseNSISPlugin.getResourceString("expandall.tooltip")); //$NON-NLS-1$
        expandAllAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPANDALL_ICON));
        expandAllAction.setDisabledImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPANDALL_DISABLED_ICON));
        expandAllAction.setEnabled(true);

        collapseAllAction = new Action() {
            public void run()
            {
                getTreeViewer().collapseAll();
                revealSelection();
            }
        };
        collapseAllAction.setText(EclipseNSISPlugin.getResourceString("collapseall.text")); //$NON-NLS-1$
        collapseAllAction.setToolTipText(EclipseNSISPlugin.getResourceString("collapseall.tooltip")); //$NON-NLS-1$
        collapseAllAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.COLLAPSEALL_ICON));
        collapseAllAction.setDisabledImageDescriptor(ImageDescriptor.createFromImage(CommonImages.COLLAPSEALL_DISABLED_ICON));
        collapseAllAction.setEnabled(true);

        filterAction = new Action() {
            public void run()
            {
                List oldList = mEditor.getOutlineContentProvider().getFilteredTypes();
                List newList = new ArrayList(oldList);
                NSISOutlineFilterDialog dialog = new NSISOutlineFilterDialog(getSite().getShell(),newList);
                if(dialog.open() == Window.OK) {
                    Collections.sort(newList);
                    if(!Common.objectsAreEqual(oldList, newList)) {
                        oldList.clear();
                        oldList.addAll(newList);
                        getTreeViewer().refresh();
                    }
                }
            }
        };
        filterAction.setText(EclipseNSISPlugin.getResourceString("filter.action.label")); //$NON-NLS-1$
        filterAction.setToolTipText(EclipseNSISPlugin.getResourceString("filter.action.tooltip")); //$NON-NLS-1$
        filterAction.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("filter.action.icon"))); //$NON-NLS-1$
        filterAction.setEnabled(true);

        mm.add(expandAllAction);
        mm.add(collapseAllAction);
        mm.add(new Separator());
        mm.add(filterAction);

        tbm.add(expandAllAction);
        tbm.add(collapseAllAction);
        tbm.add(new Separator());
        tbm.add(filterAction);
    }

    /*
     * (non-Javadoc) Method declared on ContentOutlinePage
     */
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        if(mEditor != null) {
            NSISOutlineContentResources.getInstance().connect(this);
            NSISOutlineContentProvider contentProvider = mEditor.getOutlineContentProvider();
            if(contentProvider != null) {
                TreeViewer viewer = getTreeViewer();
                viewer.setContentProvider(contentProvider);
                viewer.setLabelProvider(new NSISOutlineLabelProvider());
                viewer.addSelectionChangedListener(mEditor);
                if (mInput != null) {
                    viewer.setInput(mInput);
                    viewer.expandAll();
                }
                Point sel = mEditor.getSelectedRange();
                NSISOutlineElement element = contentProvider.findElement(sel.x,sel.y);
                if(element != null) {
                    setSelection(new StructuredSelection(element));
                }
            }
        }
        makeActions();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_outline_context"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.IPage#dispose()
     */
    public void dispose()
    {
        super.dispose();
        if(mEditor != null) {
            TreeViewer tv = getTreeViewer();
            if(tv != null) {
                tv.removeSelectionChangedListener(mEditor);
            }
            NSISOutlineContentProvider provider = mEditor.getOutlineContentProvider();
            if(provider != null) {
                provider.inputChanged(null, mEditor.getEditorInput());
            }
        }
        NSISOutlineContentResources.getInstance().disconnect(this);
        mDisposed = true;
    }

    public boolean isDisposed()
    {
        return mDisposed;
    }

    /**
     * Sets the input of the outline page
     */
    public void setInput(Object input)
    {
        mInput = input;
        update();
    }

    /**
     * Updates the outline page.
     */
    public void update()
    {
        TreeViewer viewer = getTreeViewer();

        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
                control.setRedraw(false);
                viewer.setInput(null);
                viewer.setInput(mInput);
//                viewer.expandAll();
                control.setRedraw(true);
            }
        }
    }

    /**
     * @return Returns the textEditor.
     */
    ITextEditor getTextEditor()
    {
        return mEditor;
    }
}
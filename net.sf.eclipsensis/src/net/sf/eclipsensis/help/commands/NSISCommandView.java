/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.NSISEditor;
import net.sf.eclipsensis.settings.*;
import net.sf.eclipsensis.util.*;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class NSISCommandView extends ViewPart implements INSISHomeListener
{
    private static final Image CATEGORY_IMAGE;
    private static final Image COMMAND_IMAGE;
    private static final String DEFAULT_CATEGORY = EclipseNSISPlugin.getResourceString("other.category"); //$NON-NLS-1$
    private static Comparator cComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            TreeNode node1 = (TreeNode)o1;
            TreeNode node2 = (TreeNode)o2;
            return node1.getName().compareTo(node2.getName());
        }
    };

    private TreeNode mHierarchicalRootNode;
    private TreeNode mFlatRootNode;
    private TreeViewer mViewer;
    private IAction mFlatLayoutAction;
    private IAction mHierarchicalLayoutAction;
    private IAction mCollapseAllAction;
    private IAction mExpandAllAction;
    
    private boolean mFlatMode = false;

    static {
        final Image[] images = new Image[2];
        Display.getDefault().syncExec(new Runnable() {
            public void run()
            {
                images[0] = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("category.icon")); //$NON-NLS-1$
                images[1] = EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("command.icon")); //$NON-NLS-1$
            }
        });
        CATEGORY_IMAGE = images[0];
        COMMAND_IMAGE = images[1];
    }

    public void dispose()
    {
        NSISPreferences.INSTANCE.removeListener(this);
        super.dispose();
    }

    private void makeActions()
    {
        IToolBarManager tbm = getViewSite().getActionBars().getToolBarManager();
        IMenuManager menu = getViewSite().getActionBars().getMenuManager();
        mHierarchicalLayoutAction = new Action() {
            public void run()
            {
                setFlatMode(false);
            }
        };
        mHierarchicalLayoutAction.setText(EclipseNSISPlugin.getResourceString("hierarchical.layout.action.text")); //$NON-NLS-1$
        mHierarchicalLayoutAction.setToolTipText(EclipseNSISPlugin.getResourceString("hierarchical.layout.action.tooltip")); //$NON-NLS-1$
        mHierarchicalLayoutAction.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("hierarchical.layout.icon"))); //$NON-NLS-1$
        mHierarchicalLayoutAction.setChecked(!mFlatMode);
        tbm.add(mHierarchicalLayoutAction);
        menu.add(mHierarchicalLayoutAction);

        mFlatLayoutAction = new Action() {
            public void run()
            {
                setFlatMode(true);
            }
        };
        mFlatLayoutAction.setText(EclipseNSISPlugin.getResourceString("flat.layout.action.text")); //$NON-NLS-1$
        mFlatLayoutAction.setToolTipText(EclipseNSISPlugin.getResourceString("flat.layout.action.tooltip")); //$NON-NLS-1$
        mFlatLayoutAction.setImageDescriptor(EclipseNSISPlugin.getImageManager().getImageDescriptor(EclipseNSISPlugin.getResourceString("flat.layout.icon"))); //$NON-NLS-1$
        mFlatLayoutAction.setChecked(mFlatMode);
        tbm.add(mFlatLayoutAction);
        menu.add(mFlatLayoutAction);
        
        mExpandAllAction = new Action() {
            public void run()
            {
                mViewer.expandAll();
                revealSelection();
            }
        };
        mExpandAllAction.setText(EclipseNSISPlugin.getResourceString("expandall.text")); //$NON-NLS-1$
        mExpandAllAction.setToolTipText(EclipseNSISPlugin.getResourceString("expandall.tooltip")); //$NON-NLS-1$
        mExpandAllAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPANDALL_ICON));
        mExpandAllAction.setDisabledImageDescriptor(ImageDescriptor.createFromImage(CommonImages.EXPANDALL_DISABLED_ICON));
        mExpandAllAction.setEnabled(!mFlatMode);
        tbm.add(mExpandAllAction);
        
        mCollapseAllAction = new Action() {
            public void run()
            {
                mViewer.collapseAll();
                revealSelection();
            }
        };
        mCollapseAllAction.setText(EclipseNSISPlugin.getResourceString("collapseall.text")); //$NON-NLS-1$
        mCollapseAllAction.setToolTipText(EclipseNSISPlugin.getResourceString("collapseall.tooltip")); //$NON-NLS-1$
        mCollapseAllAction.setImageDescriptor(ImageDescriptor.createFromImage(CommonImages.COLLAPSEALL_ICON));
        mCollapseAllAction.setDisabledImageDescriptor(ImageDescriptor.createFromImage(CommonImages.COLLAPSEALL_DISABLED_ICON));
        mCollapseAllAction.setEnabled(!mFlatMode);
        tbm.add(mCollapseAllAction);
    }

    public void createPartControl(Composite parent)
    {
        mFlatMode = NSISPreferences.INSTANCE.getBoolean(INSISPreferenceConstants.NSIS_COMMAND_VIEW_FLAT_MODE);
        
        Tree tree = new Tree(parent,SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.H_SCROLL|SWT.HIDE_SELECTION);
        tree.setLinesVisible(false);
        mViewer = new TreeViewer(tree);
        mViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        mViewer.setContentProvider(new TreeContentProvider());
        mViewer.setLabelProvider(new LabelProvider() {
            public String getText(Object element)
            {
                if(element instanceof TreeNode) {
                    return ((TreeNode)element).getName();
                }
                return super.getText(element);
            }

            public Image getImage(Object element)
            {
                if(element instanceof TreeNode) {
                    if(((TreeNode)element).getCommand() != null) {
                        return COMMAND_IMAGE;
                    }
                    else {
                        return CATEGORY_IMAGE;
                    }
                }
                return super.getImage(element);
            }
        });
        NSISPreferences.INSTANCE.addListener(this);
        mViewer.addDragSupport(DND.DROP_COPY, 
            new Transfer[]{NSISCommandTransfer.INSTANCE},
            new DragSourceAdapter() {
                public void dragStart(DragSourceEvent e)
                {
                    IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
                    if (!(editor instanceof NSISEditor)) {
                        e.doit = false;
                    }
                    IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
                    if(sel == null || sel.isEmpty() || !(sel.getFirstElement() instanceof TreeNode) || 
                            ((TreeNode)sel.getFirstElement()).getCommand() == null) {
                        e.doit = false;
                    }
                }
    
                public void dragSetData(DragSourceEvent e)
                {
                    IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
                    if(sel != null && !sel.isEmpty() && sel.getFirstElement() instanceof TreeNode && 
                            ((TreeNode)sel.getFirstElement()).getCommand() != null) {
                        e.data = ((TreeNode)sel.getFirstElement()).getCommand();
                    }
                    else {
                        e.data = null;
                    }
                }
            }
        );
        mViewer.getTree().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e)
            {
                if( (e.character == SWT.CR || e.character == SWT.LF) && e.stateMask == 0) {
                    insertCommand(mViewer.getSelection());
                }
            }
        });
        mViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                if(!insertCommand(event.getSelection())) {
                    if(event.getSelection() instanceof IStructuredSelection && !event.getSelection().isEmpty()) {
                        Object element = ((IStructuredSelection)event.getSelection()).getFirstElement();
                        if (element instanceof TreeNode) {
                            TreeNode node = (TreeNode)element;
                            mViewer.setExpandedState(node, !mViewer.getExpandedState(node));
                        }                            
                    }
                }
            }
        });
        makeActions();
        updateInput();
        PlatformUI.getWorkbench().getHelpSystem().setHelp(mViewer.getControl(),INSISConstants.PLUGIN_CONTEXT_PREFIX + "nsis_cmdview_context"); //$NON-NLS-1$
    }

    public void setFocus()
    {
        mViewer.getControl().setFocus();
    }

    public void nsisHomeChanged(IProgressMonitor monitor, String oldHome, String newHome)
    {
        Display.getDefault().asyncExec(new Runnable() {
            public void run()
            {
                updateInput();
            }
        });
    }

    private void updateInput()
    {
        if(mViewer != null) {
            Tree tree = mViewer.getTree();
            if(tree != null && !tree.isDisposed()) {
                NSISCommand[] commands;
                try {
                    commands = NSISCommandManager.getCommands();
                }
                catch (Exception e) {
                    EclipseNSISPlugin.getDefault().log(e);
                    commands = null;
                }
                mFlatRootNode = (mFlatMode?new TreeNode(""):null); //$NON-NLS-1$
                mHierarchicalRootNode = (mFlatMode?null:new TreeNode("")); //$NON-NLS-1$
                TreeNode rootNode = (mFlatMode?mFlatRootNode:mHierarchicalRootNode);
                for (int i = 0; i < commands.length; i++) {
                    TreeNode parent = rootNode;
                    
                    if(!mFlatMode) {
                        parent = findParent(parent, commands[i]);
                    }                    
                    parent.addChild(new TreeNode(commands[i].getName(),commands[i]));
                }
                rootNode.sort();
                updateInput(rootNode);
            }
        }
    }

    /**
     * @param rootNode
     */
    private void updateInput(TreeNode rootNode)
    {
        ISelection sel = mViewer.getSelection();
        Tree tree = mViewer.getTree();
        if(mFlatMode) {
            WinAPI.SetWindowLong(tree.handle, WinAPI.GWL_STYLE, WinAPI.GetWindowLong(tree.handle, WinAPI.GWL_STYLE) ^ (WinAPI.TVS_HASLINES  | WinAPI.TVS_HASBUTTONS));
        }
        else {
            WinAPI.SetWindowLong(tree.handle, WinAPI.GWL_STYLE, WinAPI.GetWindowLong(tree.handle, WinAPI.GWL_STYLE) | WinAPI.TVS_HASLINES  | WinAPI.TVS_HASBUTTONS);
        }
        mViewer.setInput(rootNode);
        if(sel != null && !sel.isEmpty()) {
            mViewer.setSelection(sel);
            sel = mViewer.getSelection();
            if(!sel.isEmpty()) {
                return;
            }
        }
        if(mViewer.getTree().getItemCount() > 0) {
            mViewer.getTree().showItem(mViewer.getTree().getItem(0));
        }
    }
    
    private void setFlatMode(boolean flatMode)
    {
        if(mFlatMode != flatMode) {
            TreeNode rootNode;
            if(mFlatMode) {
                boolean isNew = false;
                if(mHierarchicalRootNode == null) {
                    mHierarchicalRootNode = new TreeNode(""); //$NON-NLS-1$
                    isNew = true;
                }
                for(int i=0; i<mFlatRootNode.getChildren().size(); i++) {
                    TreeNode child = (TreeNode)mFlatRootNode.getChildren().get(i);
                    mFlatRootNode.removeChild(child);
                    TreeNode parent = findParent(mHierarchicalRootNode, child.getCommand());
                    parent.addChild(child);
                    i--;
                }
                if (isNew) {
                    mHierarchicalRootNode.sort();
                }            
                rootNode = mHierarchicalRootNode;
            }
            else {
                if(mFlatRootNode == null) {
                    mFlatRootNode = new TreeNode(""); //$NON-NLS-1$
                }
                moveCommandChild(mFlatRootNode,mHierarchicalRootNode);
                mFlatRootNode.sort();
                rootNode = mFlatRootNode;
            }
            mFlatMode = !mFlatMode;
            mFlatLayoutAction.setChecked(mFlatMode);
            mHierarchicalLayoutAction.setChecked(!mFlatMode);
            mExpandAllAction.setEnabled(!mFlatMode);
            mCollapseAllAction.setEnabled(!mFlatMode);
            NSISPreferences.INSTANCE.setValue(INSISPreferenceConstants.NSIS_COMMAND_VIEW_FLAT_MODE, mFlatMode);
            updateInput(rootNode);
        }
    }
    
    private boolean moveCommandChild(TreeNode target, TreeNode source)
    {
        if(source.getChildren().size() > 0) {
            for(int i=0; i<source.getChildren().size(); i++) {
                TreeNode child = (TreeNode)source.getChildren().get(i);
                if(moveCommandChild(target, child)) {
                    i--;
                }
            }
            return false;
        }
        else {
            target.addChild(source);
            return true;
        }
    }

    private TreeNode findParent(TreeNode parent, NSISCommand cmd)
    {
        String category = cmd.getCategory();
        if(Common.isEmpty(category)) {
            category = DEFAULT_CATEGORY;
        }
        String[] cats = Common.tokenize(category, '/');
        for(int j=0; j<cats.length; j++) {
            for (Iterator iter = parent.getChildren().iterator(); iter.hasNext();) {
                TreeNode node = (TreeNode)iter.next();
                if(node.getName().equals(cats[j])) {
                    parent = node;
                    break;
                }
            }
            if(!parent.getName().equals(cats[j])) {
                TreeNode node = new TreeNode(cats[j]);
                parent.addChild(node);
                parent = node;
            }
        }
        return parent;
    }

    /**
     * @param sel
     */
    private boolean insertCommand(ISelection sel)
    {
        if(sel instanceof IStructuredSelection && !sel.isEmpty()) {
            try {
                IEditorPart editor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
                if(editor instanceof NSISEditor) {
                    Object element = ((IStructuredSelection)sel).getFirstElement();
                    if(element instanceof TreeNode && ((TreeNode)element).getCommand() != null) {
                        ((NSISEditor)editor).insertCommand(((TreeNode)element).getCommand());
                        return true;
                    }
                }
            }
            catch(Exception ex) {
                EclipseNSISPlugin.getDefault().log(ex);
            }
        }
        return false;
    }
    
    /**
     * 
     */
    private void revealSelection()
    {
        IStructuredSelection sel = (IStructuredSelection)mViewer.getSelection();
        if(!sel.isEmpty()) {
            mViewer.reveal(sel.getFirstElement());
        }
    }

    private class TreeNode
    {
        private TreeNode mParent;
        private String mName;
        private NSISCommand mCommand;
        private List mChildren;
        
        public TreeNode(String name)
        {
            this(name, null);
        }
        
        public TreeNode(String name, NSISCommand data)
        {
            mName = name;
            mCommand = data;
        }
        
        public List getChildren()
        {
            return (mChildren==null?Collections.EMPTY_LIST:mChildren);
        }

        public NSISCommand getCommand()
        {
            return mCommand;
        }

        public String getName()
        {
            return mName;
        }

        public TreeNode getParent()
        {
            return mParent;
        }

        public void setParent(TreeNode parent)
        {
            if(mParent != null) {
                TreeNode oldParent = mParent;
                mParent = null;
                oldParent.removeChild(this);
            }
            mParent = parent;
            if(mParent != null) {
                mParent.addChild(this);
            }
        }
        
        public void addChild(TreeNode child)
        {
            if(mChildren == null) {
                mChildren = new ArrayList();
            }
            if(!mChildren.contains(child)) {
                mChildren.add(child);
                child.setParent(this);
            }
        }
        
        public void removeChild(TreeNode child)
        {
            if(mChildren != null && mChildren.remove(child)) {
                child.setParent(null);
            }
        }
        
        public void sort()
        {
            if(mChildren != null) {
                Collections.sort(mChildren, cComparator);
                for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                    ((TreeNode)iter.next()).sort();
                }
            }
        }
    }

    private class TreeContentProvider implements ITreeContentProvider
    {
        public Object[] getChildren(Object parentElement)
        {
            if(parentElement instanceof TreeNode) {
                List children = ((TreeNode)parentElement).getChildren();
                return (children==null?null:children.toArray());
            }
            return null;
        }

        public Object getParent(Object element)
        {
            if(element instanceof TreeNode) {
                return ((TreeNode)element).getParent();
            }
            return null;
        }

        public boolean hasChildren(Object element)
        {
            return !Common.isEmptyArray(getChildren(element));
        }

        public Object[] getElements(Object inputElement)
        {
            if(inputElement == (mFlatMode?mFlatRootNode:mHierarchicalRootNode)) {
               return getChildren(inputElement);
            }
            return null;
        }

        public void dispose()
        {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
        }
    }
}

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
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.settings.*;
import net.sf.eclipsensis.wizard.util.NSISWizardDialogUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISWizardContentsPage extends AbstractNSISWizardPage
{
    public static final String NAME = "nsisWizardContents"; //$NON-NLS-1$

    private static final int ALL_CHECK=0;

    private static final String[] cDeleteConfirmButtonLabels = {IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL};

    private static final int[] cDeleteConfirmButtonIds = {IDialogConstants.YES_ID, IDialogConstants.YES_TO_ALL_ID, IDialogConstants.NO_ID, IDialogConstants.NO_TO_ALL_ID};

    private static final String cDeleteConfirmTitle = EclipseNSISPlugin.getResourceString("delete.confirmation.title");

    private static final String cDeleteConfirmMessageFormat = EclipseNSISPlugin.getResourceString("delete.confirmation.message");
    
    /**
     * @param settings
     * @param pageName
     * @param title
     */
    public NSISWizardContentsPage(NSISWizardSettings settings)
    {
        super(settings, NAME, EclipseNSISPlugin.getResourceString("wizard.contents.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.contents.description")); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
    
        final GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
    
        Label l = NSISWizardDialogUtil.createLabel(composite,"wizard.contents.text",true,null,false);
        final GridData gridData = (GridData)l.getLayoutData();
        gridData.widthHint = WIDTH_HINT;
        composite.addListener (SWT.Resize,  new Listener () {
            boolean init = false;
            
            public void handleEvent (Event e) {
                if(init) {
                    Point size = composite.getSize();
                    gridData.widthHint = size.x - 2*layout.marginWidth;
                    composite.layout();
                }
                else {
                    init=true;
                }
            }
        });
        
        Group group = NSISWizardDialogUtil.createGroup(composite,1,"",null,false);
        GridData gd = (GridData)group.getLayoutData();
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = GridData.FILL;
        
        final ToolBar toolbar = createToolBar(group, bundle);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        toolbar.setLayoutData(gd);
    
        final ToolItem addToolItem = toolbar.getItem(0);
        final ToolItem editToolItem = toolbar.getItem(1);
        final ToolItem deleteToolItem = toolbar.getItem(2);
    
        final Tree tree = new Tree(group,SWT.MULTI|SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH);
        tree.setLayoutData(gd);
        final TreeViewer tv = new TreeViewer(tree);
        tv.setLabelProvider(new NSISInstallElementLabelProvider());
        tv.setContentProvider(new NSISInstallElementTreeContentProvider(mSettings));
        tv.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event) 
            {
                enableItems(event.getSelection(),addToolItem,editToolItem,deleteToolItem);
            }
        });
        tv.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        tv.setInput(mSettings);
    
        SelectionAdapter editSelectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                ISelection sel = tv.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)sel).getFirstElement();
                    if(obj instanceof INSISInstallElement) {
                        editElement(composite, tv, (INSISInstallElement)obj);
                    }
                }
            }
        };
        
        SelectionAdapter deleteSelectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                ISelection sel = tv.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    deleteElements(tv, sel);
                }
            }
        };
        
        final SelectionAdapter addSelectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                MenuItem mi = (MenuItem)e.widget;
                String text = mi.getText();
                INSISInstallElement element = NSISInstallElementFactory.create(mSettings,text);
                if(element != null) {
                    if(element.isEditable()) {
                        ISelection se = tv.getSelection();
                        if(!se.isEmpty() && se instanceof IStructuredSelection) {
                            Object obj = ((IStructuredSelection)se).getFirstElement();
                            if(obj instanceof INSISInstallElement) {
                                INSISInstallElement parent = (INSISInstallElement)obj;
                                if(element.edit(composite)) {
                                    parent.addChild(element);
                                    tv.refresh(parent,true);
                                    tv.reveal(element);
                                    if(element.hasChildren()) {
                                        tv.expandToLevel(element,TreeViewer.ALL_LEVELS);
                                    }
                                    setPageComplete(validatePage(ALL_CHECK));
                                }
                            }
                        }
                    }
                }
            }
        };

        final Menu addPopupMenu = new Menu(getShell(),SWT.POP_UP);
        addToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Rectangle rect = addToolItem.getBounds();
                Point pt = new Point (rect.x, rect.y + rect.height);
                pt = toolbar.toDisplay (pt);
                updateAddMenu(addPopupMenu, tv.getSelection(),addSelectionAdapter);
                addPopupMenu.setLocation(pt.x, pt.y);
                addPopupMenu.setVisible(true);
            }
        });
        editToolItem.addSelectionListener(editSelectionAdapter);
        deleteToolItem.addSelectionListener(deleteSelectionAdapter);
        
        final Menu itemPopupMenu = createMenu(bundle);
        final Menu addDropdownMenu = new Menu(getShell(),SWT.DROP_DOWN);

        final MenuItem addMenuItem = itemPopupMenu.getItem(0);
        final MenuItem editMenuItem = itemPopupMenu.getItem(1);
        final MenuItem deleteMenuItem = itemPopupMenu.getItem(2);

        addMenuItem.setMenu(addDropdownMenu);
        editMenuItem.addSelectionListener(editSelectionAdapter);
        deleteMenuItem.addSelectionListener(deleteSelectionAdapter);

        tree.addMouseListener(new MouseAdapter() {
            public void mouseDoubleClick(MouseEvent e) {
                TreeItem ti = tree.getItem(new Point(e.x,e.y));
                if(ti != null) {
                    Object obj = ti.getData();
                    if(obj instanceof INSISInstallElement) {
                        editElement(composite, tv, (INSISInstallElement)obj);
                    }
                }
            }

            public void mouseUp(MouseEvent e) {
                if(e.button == 3) {
                    TreeItem ti = tree.getItem(new Point(e.x,e.y));
                    if(ti != null) {
                        ISelection sel = tv.getSelection();
                        enableItems(sel,addMenuItem,editMenuItem,deleteMenuItem);
                        updateAddMenu(addDropdownMenu, sel,addSelectionAdapter);
                        Point pt = tree.toDisplay(e.x,e.y);
                        itemPopupMenu.setLocation(pt.x, pt.y);
                        itemPopupMenu.setVisible(true);
                    }
                }
            }
        });
        
        tree.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if(e.character == SWT.DEL) {
                    if(e.stateMask == 0) {
                        ISelection sel = tv.getSelection();
                        if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                            if(canDeleteElements((IStructuredSelection)sel)) {
                                deleteElements(tv, sel);
                            }
                        }
                        
                    }
                }
            }
        });
        setPageComplete(validatePage(ALL_CHECK));
    }

    private void editElement(Composite composite, TreeViewer tv, INSISInstallElement element)
    {
        if(element.edit(composite)) {
            tv.refresh(element, true);
            if(element.hasChildren()) {
                tv.expandToLevel(element,TreeViewer.ALL_LEVELS);
            }
            setPageComplete(validatePage(ALL_CHECK));
        }
    }


    /**
     * @param tv
     * @param sel
     */
    private void deleteElements(TreeViewer tv, ISelection sel)
    {
        if(sel instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection)sel;
            if(!ssel.isEmpty()) {
                int buttonId = -1;
                for(Iterator iter = (ssel).iterator(); iter.hasNext(); ) {
                    Object obj = iter.next();
                    if(obj instanceof INSISInstallElement) {
                        INSISInstallElement element = (INSISInstallElement)obj;
                        if(element.hasChildren()) {
                            if(buttonId == IDialogConstants.NO_TO_ALL_ID) {
                                continue;
                            }
                            else if(buttonId != IDialogConstants.YES_TO_ALL_ID) {
                                buttonId = cDeleteConfirmButtonIds[new MessageDialog(getShell(),cDeleteConfirmTitle,null,
                                        MessageFormat.format(cDeleteConfirmMessageFormat,new String[]{element.getDisplayName()}), MessageDialog.QUESTION, 
                                        new String[]{IDialogConstants.YES_LABEL, IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.NO_TO_ALL_LABEL}, 0).open()];

                                if(buttonId == IDialogConstants.NO_ID || buttonId == IDialogConstants.NO_TO_ALL_ID) {
                                    continue;
                                }
                            }
                        }
                        INSISInstallElement parent = element.getParent();
                        if(parent != null) {
                            parent.removeChild(element);
                        }
                    }
                }
                tv.refresh(false);
                setPageComplete(validatePage(ALL_CHECK));
            }
        }
    }

    private void enableItem(Item item, boolean state)
    {
        if(item instanceof ToolItem) {
            ((ToolItem)item).setEnabled(state);
        }
        else if(item instanceof MenuItem) {
            ((MenuItem)item).setEnabled(state);
        }
    }

    private void enableItems(ISelection sel, Item addItem, Item editItem, Item deleteItem)
    {
        if(sel.isEmpty()) {
            enableItem(addItem, false);
            enableItem(editItem, false);
            enableItem(deleteItem, false);
        }
        else if(sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)sel;
            if(selection.size() > 1) {
                enableItem(addItem, false);
                enableItem(editItem, false);
            }
            else {
                Object obj = selection.getFirstElement();
                if(obj instanceof INSISInstallElement) {
                    INSISInstallElement element = (INSISInstallElement)obj;
                    enableItem(addItem, !Common.isEmptyArray(element.getChildTypes()));
                    enableItem(editItem, element.isEditable());
                }
            }
            enableItem(deleteItem, canDeleteElements(selection));
        }
    }

    private boolean isAncestorOf(INSISInstallElement first, INSISInstallElement second)
    {
        while(!first.equals(second)) {
            second = second.getParent();
            if(second == null) {
                return false;
            }
        }
        return true;
    }
    /**
     * @param selection
     * @return
     */
    private boolean canDeleteElements(IStructuredSelection selection)
    {
        HashSet elements = new HashSet();
        if(selection.size() > 0) {
            if(selection.size() > 1) {
                for (Iterator iter = selection.iterator(); iter.hasNext();) {
                    Object obj = iter.next();
                    if(obj instanceof INSISInstallElement) {
                        INSISInstallElement element = (INSISInstallElement)obj;
                        if(!element.isRemovable()) {
                            return false;
                        }
                        else {
                            for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
                                INSISInstallElement element2 = (INSISInstallElement) iterator.next();
                                if(isAncestorOf(element,element2) || isAncestorOf(element2, element)) {
                                   return false;
                                }
                            }
                            elements.add(element);
                        }
                    }
                }
                return true;
            }
            else {
                Object obj = selection.getFirstElement();
                if(obj instanceof INSISInstallElement) {
                    return ((INSISInstallElement)obj).isRemovable();
                }
            }
        }
        return false;
    }

    private void updateAddMenu(Menu addPopupMenu, ISelection selection, SelectionAdapter adapter)
    {
        if(!selection.isEmpty()) {
            if(selection instanceof IStructuredSelection) {
                IStructuredSelection ssel = (IStructuredSelection)selection;
                if(ssel.size() == 1) {
                    Object obj = ssel.getFirstElement();
                    if(obj instanceof INSISInstallElement) {
                        INSISInstallElement element = (INSISInstallElement)obj;
                        String[] childTypes = element.getChildTypes();
                        if(!Common.isEmptyArray(childTypes)) {
                            int n = addPopupMenu.getItemCount();
                            for(int i=0; i<n; i++) {
                                addPopupMenu.getItem(0).dispose();
                            }

                            for (int i = 0; i < childTypes.length; i++) {
                                MenuItem mi = new MenuItem(addPopupMenu,SWT.PUSH);
                                mi.addSelectionListener(adapter);
                                mi.setText(childTypes[i]);
                                mi.setImage(NSISInstallElementFactory.getImage(childTypes[i]));
                            }
                            
                        }
                    }
                }
            }
        }
    }

    /**
     * @param composite
     * @param bundle
     */
    private ToolBar createToolBar(Composite parent, ResourceBundle bundle)
    {
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT); //$NON-NLS-1$
        
        int[] styles = {SWT.DROP_DOWN,SWT.PUSH,SWT.PUSH};
        String[] images = {"installitem.add.icon","installitem.edit.icon","installitem.delete.icon"};
        String[] disabledImages = {"installitem.add.disabledicon","installitem.edit.disabledicon","installitem.delete.disabledicon"};
        String[] tooltips = {"installitem.add.tooltip","installitem.edit.tooltip","installitem.delete.tooltip"};
        for (int i = 0; i < styles.length; i++) {
            ToolItem ti = new ToolItem(toolbar, styles[i]);
            ti.setImage(ImageManager.getImage(bundle.getString(images[i])));
            ti.setDisabledImage(ImageManager.getImage(bundle.getString(disabledImages[i])));
            ti.setToolTipText(bundle.getString(tooltips[i]));
            ti.setEnabled(false);
        }
        return toolbar;
    }

    /**
     * @param composite
     * @param bundle
     */
    private Menu createMenu(ResourceBundle bundle)
    {
        Menu menu = new Menu(getShell(), SWT.POP_UP); //$NON-NLS-1$
        
        int[] styles = {SWT.CASCADE,SWT.PUSH,SWT.PUSH};
        String[] images = {"installitem.add.icon","installitem.edit.icon","installitem.delete.icon"};
        String[] tooltips = {"installitem.add.tooltip","installitem.edit.tooltip","installitem.delete.tooltip"};
        for (int i = 0; i < styles.length; i++) {
            MenuItem mi = new MenuItem(menu, styles[i]);
            mi.setImage(ImageManager.getImage(bundle.getString(images[i])));
            mi.setText(bundle.getString(tooltips[i]));
            mi.setEnabled(false);
        }
        return menu;
    }
    
    private boolean validateInstallElement(INSISInstallElement installElement)
    {
        if(!Common.isEmptyArray(installElement.getChildTypes())) { 
            if(installElement.hasChildren()) {
                INSISInstallElement[] children = installElement.getChildren();
                for (int i = 0; i < children.length; i++) {
                    if(!validateInstallElement(children[i])) {
                        return false;
                    }
                }
            }
            else {
                setErrorMessage(MessageFormat.format(EclipseNSISPlugin.getResourceString("empty.contents.error"),new Object[]{installElement.getDisplayName()}));
                return false;
            }
        }
        return true;
    }

    private boolean validatePage(int flag)
    {
        boolean b;
        
        if((b = validateInstallElement(mSettings.getInstaller()))) {
            setErrorMessage(null);
        }
        setPageComplete(b);
        return b;
    }
}

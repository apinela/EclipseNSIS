/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import java.text.MessageFormat;
import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;
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

    private static final String cDeleteConfirmTitle = EclipseNSISPlugin.getResourceString("delete.confirmation.title"); //$NON-NLS-1$

    private static final String cDeleteConfirmMessageFormat = EclipseNSISPlugin.getResourceString("delete.confirmation.message"); //$NON-NLS-1$
    
    /**
     * @param pageName
     * @param title
     */
    public NSISWizardContentsPage()
    {
        super(NAME, EclipseNSISPlugin.getResourceString("wizard.contents.title"), //$NON-NLS-1$
              EclipseNSISPlugin.getResourceString("wizard.contents.description")); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        final Composite composite = new Composite(parent, SWT.NONE);
        setControl(composite);
    
        final GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);
        
        ResourceBundle bundle = EclipseNSISPlugin.getDefault().getResourceBundle();
    
        Label l = NSISWizardDialogUtil.createLabel(composite,"wizard.contents.text",true,null,false); //$NON-NLS-1$
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
        
        Group group = NSISWizardDialogUtil.createGroup(composite,1,"",null,false); //$NON-NLS-1$
        GridData gd = (GridData)group.getLayoutData();
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = GridData.FILL;
        
        final ToolBar toolbar = createToolBar(group, bundle);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        toolbar.setLayoutData(gd);
    
        final ToolItem addToolItem = toolbar.getItem(0);
        final ToolItem editToolItem = toolbar.getItem(1);
        final ToolItem deleteToolItem = toolbar.getItem(2);

        Composite composite2 = new Composite(group,SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 1;
        composite2.setLayoutData(gd);
        
        GridLayout layout2 = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout2);
        
        final Tree tree = new Tree(composite2,SWT.MULTI|SWT.BORDER);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 1;
        tree.setLayoutData(gd);

        updateSelectComponents();
        final TreeViewer tv = new TreeViewer(tree);
        tv.setLabelProvider(new NSISInstallElementLabelProvider());
        tv.setContentProvider(new NSISInstallElementTreeContentProvider(settings));
        tv.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        tv.setInput(settings);
    
        final UpDownMover mover = new UpDownMover() {
            private TreeViewer mTreeViewer = null;
            
            public void setInput(Object input)
            {
                mTreeViewer = (TreeViewer)input;
            }

            public Object getInput()
            {
                return mTreeViewer;
            }

            private List getSelectionList()
            {
                List list = null;
                ISelection sel = mTreeViewer.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    list = ((IStructuredSelection)sel).toList();
                }
                return list;
            }

            private INSISInstallElement getSelectionParent(List list)
            {
                INSISInstallElement parent = null;
                if(!Common.isEmptyCollection(list)) {
                    for(Iterator iter=list.iterator(); iter.hasNext(); ) {
                        Object obj = iter.next();
                        if(obj instanceof INSISInstallElement) {
                            INSISInstallElement element = (INSISInstallElement)obj;
                            if(parent != null) {
                                if(!parent.equals(element.getParent())) {
                                    return null;
                                }
                            }
                            else {
                                parent = element.getParent();
                                if(parent == null) {
                                    return null;
                                }
                            }
                        }
                        else {
                            return null;
                        }
                    }
                }
                return parent;
            }
            
            protected int[] getSelectedIndices()
            {
                List list = getSelectionList();
                INSISInstallElement parent = getSelectionParent(list);
                if(parent != null) {
                    List allElements = getAllElements(parent);
                    int[] selectedIndices = new int[list.size()];
                    int i=0;
                    for(Iterator iter=list.iterator(); iter.hasNext(); ) {
                        selectedIndices[i++] = allElements.indexOf(iter.next());
                    }
                    
                    return selectedIndices;
                }
                return new int[0];
            }

            protected int getSize()
            {
                List list = getSelectionList();
                INSISInstallElement parent = getSelectionParent(list);
                if(parent != null) {
                    INSISInstallElement[] children = parent.getChildren();
                    if(!Common.isEmptyArray(children)) {
                        return children.length;
                    }
                }
                return 0;
            }

            private List getAllElements(INSISInstallElement parent)
            {
                if(parent != null) {
                    INSISInstallElement[] children = parent.getChildren();
                    if(!Common.isEmptyArray(children)) {
                        return Arrays.asList(children);
                    }
                }
                return Collections.EMPTY_LIST;
            }

            protected List getAllElements()
            {
                List list = getSelectionList();
                return getAllElements(getSelectionParent(getSelectionList()));
            }

            private List getMoveElements(INSISInstallElement parent, List selectionList)
            {
                if(parent != null) {
                    return selectionList;
                }
                else {
                    return Collections.EMPTY_LIST;
                }
            }

            protected List getMoveElements()
            {
                List list = getSelectionList();
                return getMoveElements(getSelectionParent(list), list);
            }

            protected void updateElements(List elements, List move, boolean isDown)
            {
                INSISInstallElement parent = getSelectionParent(move);
                if(parent != null && parent instanceof AbstractNSISInstallGroup) {
                    parent.removeAllChildren();
                    for (Iterator iter = elements.iterator(); iter.hasNext();) {
                        INSISInstallElement element = (INSISInstallElement)iter.next();
                        parent.addChild(element);
                    }
                    mTreeViewer.refresh(parent,true);
                    expandGroup((AbstractNSISInstallGroup)parent);
//                    mTreeViewer.expandToLevel(parent, TreeViewer.ALL_LEVELS);
                    if(!Common.isEmptyCollection(move)) {
                        mTreeViewer.setSelection(new StructuredSelection(move));
                        mTreeViewer.reveal(move.get(isDown?move.size()-1:0));
                    }
                }
            }
            
            private void expandGroup(AbstractNSISInstallGroup group)
            {
                if(mTreeViewer.getExpandedState(group) != group.isExpanded()) {
                    mTreeViewer.setExpandedState(group,group.isExpanded());
                }
                INSISInstallElement[] children = group.getChildren();
                if(!Common.isEmptyArray(children)) {
                    for (int i = 0; i < children.length; i++) {
                        if(children[i] instanceof AbstractNSISInstallGroup) {
                            expandGroup((AbstractNSISInstallGroup)children[i]);
                        }
                    }
                }
            }
        };
        mover.setInput(tv);
        
        Composite composite3 = new Composite(composite2,SWT.NONE);
        gd = new GridData(GridData.VERTICAL_ALIGN_CENTER);
        gd.horizontalSpan = 1;
        gd.grabExcessHorizontalSpace = false;
        composite3.setLayoutData(gd);
        
        layout2 = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite3.setLayout(layout2);
        
        final Button upButton = new Button(composite3,SWT.PUSH);
        upButton.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString("up.icon"))); //$NON-NLS-1$
        upButton.setToolTipText(EclipseNSISPlugin.getResourceString("up.tooltip")); //$NON-NLS-1$
        upButton.setEnabled(mover.canMoveUp());
        upButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mover.moveUp();
            }
        });
        
        final Button downButton = new Button(composite3,SWT.PUSH);
        downButton.setImage(ImageManager.getImage(EclipseNSISPlugin.getResourceString("down.icon"))); //$NON-NLS-1$
        downButton.setToolTipText(EclipseNSISPlugin.getResourceString("down.tooltip")); //$NON-NLS-1$
        downButton.setEnabled(mover.canMoveDown());
        downButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mover.moveDown();
            }
        });
        
        tv.addSelectionChangedListener(new ISelectionChangedListener(){
            public void selectionChanged(SelectionChangedEvent event) 
            {
                enableItems(event.getSelection(),addToolItem,editToolItem,deleteToolItem);
                upButton.setEnabled(mover.canMoveUp());
                downButton.setEnabled(mover.canMoveDown());
            }
        });
        
        tv.addTreeListener(new ITreeViewerListener() {
            public void setState(TreeExpansionEvent event, boolean state)
            {
                Object element = event.getElement();
                if(element instanceof AbstractNSISInstallGroup) {
                    ((AbstractNSISInstallGroup)element).setExpanded(state);
                }
            }
            
            public void treeCollapsed(TreeExpansionEvent event)
            {
                setState(event, false);
            }

            public void treeExpanded(TreeExpansionEvent event)
            {
                setState(event, true);
            }
        });
        
        mWizard.addSettingsListener(new INSISWizardSettingsListener() {
            public void settingsChanged()
            {
                NSISWizardSettings settings2 = mWizard.getSettings();
                tv.setInput(settings2);
                tv.expandToLevel(settings2.getInstaller(), TreeViewer.ALL_LEVELS);
            }});

        SelectionAdapter editSelectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                ISelection sel = tv.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    Object obj = ((IStructuredSelection)sel).getFirstElement();
                    if(obj instanceof INSISInstallElement) {
                        editElement(composite, tv, (INSISInstallElement)obj);
                        updateSelectComponents();
                        checkUnselectedSections();
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
                    updateSelectComponents();
                }
            }
        };
        
        final SelectionAdapter addSelectionAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                    MenuItem mi = (MenuItem)e.widget;
                    String text = mi.getText();
                    INSISInstallElement element = NSISInstallElementFactory.create(mWizard.getSettings(),text);
                    if(element != null) {
                        try {
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
                                            updateSelectComponents();
                                            setPageComplete(validatePage(ALL_CHECK));
                                        }
                                    }
                                }
                            }
                        }
                        catch(Exception ex) {
                            delayedValidateAfterError(ex.getMessage(),2000);
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
        toolbar.getItem(3).addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                tv.expandAll();
            }
        });
        toolbar.getItem(4).addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                tv.collapseAll();
            }
        });
        
        final Menu itemPopupMenu = createMenu(bundle);
        final Menu addDropdownMenu = new Menu(getShell(),SWT.DROP_DOWN);

        final MenuItem addMenuItem = itemPopupMenu.getItem(0);
        final MenuItem editMenuItem = itemPopupMenu.getItem(1);
        final MenuItem deleteMenuItem = itemPopupMenu.getItem(2);

        addMenuItem.setMenu(addDropdownMenu);
        editMenuItem.addSelectionListener(editSelectionAdapter);
        deleteMenuItem.addSelectionListener(deleteSelectionAdapter);

        tree.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent e) {
                ISelection sel = tv.getSelection();
                if(!sel.isEmpty() && sel instanceof IStructuredSelection) {
                    IStructuredSelection ssel = (IStructuredSelection)sel;
                    Object obj = ssel.getFirstElement();
                    if(obj instanceof INSISInstallElement) {
                        editElement(composite, tv, (INSISInstallElement)obj);
                        updateSelectComponents();
                        checkUnselectedSections();
                    }
                }
            }
        });
        
        tree.addMouseListener(new MouseAdapter() {
//            public void mouseDoubleClick(MouseEvent e) {
//                TreeItem ti = tree.getItem(new Point(e.x,e.y));
//                if(ti != null) {
//                    Object obj = ti.getData();
//                    if(obj instanceof INSISInstallElement) {
//                        editElement(composite, tv, (INSISInstallElement)obj);
//                        updateSelectComponents();
//                        checkUnselectedSections();
//                    }
//                }
//            }

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
                                updateSelectComponents();
                            }
                        }
                        
                    }
                }
            }
        });

        addPageListener(new NSISWizardPageAdapter() {
            public void aboutToShow()
            {
                checkUnselectedSections();
            }
        });

        setPageComplete(validatePage(ALL_CHECK));
    }

    private void updateSelectComponents()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        if(settings.getInstallerType() != INSISWizardConstants.INSTALLER_TYPE_SILENT) {
            INSISInstallElement[] items = settings.getInstaller().getChildren();
            if(!Common.isEmptyArray(items)) {
                for (int i = 0; i < items.length; i++) {
                    if(items[i] instanceof NSISSection) {
                        if(!((NSISSection)items[i]).isHidden()) {
                            settings.setSelectComponents(true);
                            return;
                        }
                    }
                    else if(items[i] instanceof NSISSectionGroup) {
                        INSISInstallElement[] items2 = items[i].getChildren();
                        if(!Common.isEmptyArray(items2)) {
                            for (int j = 0; j < items2.length; j++) {
                                if(items2[j] instanceof NSISSection) {
                                    if(!((NSISSection)items2[j]).isHidden()) {
                                        settings.setSelectComponents(true);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        settings.setSelectComponents(false);
    }

    private void editElement(Composite composite, TreeViewer tv, INSISInstallElement element)
    {
        try {
            if(element.edit(composite)) {
                tv.refresh(element, true);
                if(element.hasChildren()) {
                    tv.expandToLevel(element,TreeViewer.ALL_LEVELS);
                }
                setPageComplete(validatePage(ALL_CHECK));
            }
        }
        catch(Exception ex) {
            delayedValidateAfterError(ex.getMessage(),2000);
        }
    }

    /**
     * @param tv
     * @param sel
     */
    private void deleteElements(final TreeViewer tv, ISelection sel)
    {
        if(sel instanceof IStructuredSelection) {
            IStructuredSelection ssel = (IStructuredSelection)sel;
            if(!ssel.isEmpty()) {
                try {
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
                    setPageComplete(validatePage(ALL_CHECK));
                }
                catch(Exception ex) {
                    delayedValidateAfterError(ex.getMessage(),2000);
                }
                finally {
                    tv.refresh(false);
                }
            }
        }
    }

    /**
     * @param errorMessage
     */
    private void delayedValidateAfterError(String errorMessage, final int delay)
    {
        setErrorMessage(errorMessage);
        final Display display = getShell().getDisplay();
        new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(delay);
                }
                catch(InterruptedException ie) {
                }
                display.asyncExec(new Runnable() {
                    public void run()
                    {
                        setPageComplete(validatePage(ALL_CHECK));
                    }
                 });
            }
        }).start();
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
        
        int[] styles = {SWT.DROP_DOWN,SWT.PUSH,SWT.PUSH,SWT.PUSH,SWT.PUSH};
        String[] images = {"add.icon","edit.icon","delete.icon","installitem.expandall.icon","installitem.collapseall.icon"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        String[] disabledImages = {"add.disabledicon","edit.disabledicon","delete.disabledicon","installitem.expandall.disabledicon","installitem.collapseall.disabledicon"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        String[] tooltips = {"add.tooltip","edit.tooltip","delete.tooltip","installitem.expandall.tooltip","installitem.collapseall.tooltip"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        boolean[] state = {false,false,false,true,true};
        for (int i = 0; i < styles.length; i++) {
            ToolItem ti = new ToolItem(toolbar, styles[i]);
            ti.setImage(ImageManager.getImage(bundle.getString(images[i])));
            ti.setDisabledImage(ImageManager.getImage(bundle.getString(disabledImages[i])));
            ti.setToolTipText(bundle.getString(tooltips[i]));
            ti.setEnabled(state[i]);
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
        String[] images = {"add.icon","edit.icon","delete.icon"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String[] tooltips = {"add.tooltip","edit.tooltip","delete.tooltip"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
                setErrorMessage(MessageFormat.format(EclipseNSISPlugin.getResourceString("empty.contents.error"),new Object[]{installElement.getDisplayName()})); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }
    
    private void checkUnselectedSections()
    {
        NSISWizardSettings settings = mWizard.getSettings();

        if(Common.isEmpty(getErrorMessage()) && settings.getInstallerType() == INSISWizardConstants.INSTALLER_TYPE_SILENT) {
            INSISInstallElement[] items = settings.getInstaller().getChildren();
            if(!Common.isEmptyArray(items)) {
                for (int i = 0; i < items.length; i++) {
                    if(items[i] instanceof NSISSection) {
                        if(((NSISSection)items[i]).isDefaultUnselected()) {
                            setMessage(EclipseNSISPlugin.getResourceString("silent.unselected.sections.warning"),WARNING); //$NON-NLS-1$
                            return;
                        }
                    }
                    else if(items[i] instanceof NSISSectionGroup) {
                        INSISInstallElement[] items2 = items[i].getChildren();
                        if(!Common.isEmptyArray(items2)) {
                            for (int j = 0; j < items2.length; j++) {
                                if(items2[j] instanceof NSISSection) {
                                    if(((NSISSection)items2[j]).isDefaultUnselected()) {
                                        setMessage(EclipseNSISPlugin.getResourceString("silent.unselected.sections.warning"),WARNING); //$NON-NLS-1$
                                        return;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        else {
            setMessage(EclipseNSISPlugin.getResourceString("wizard.contents.description")); //$NON-NLS-1$
        }
    }

    public boolean validatePage(int flag)
    {
        NSISWizardSettings settings = mWizard.getSettings();

        boolean b;
        
        if((b = validateInstallElement(settings.getInstaller()))) {
            setErrorMessage(null);
        }
        setPageComplete(b);
        checkUnselectedSections();
        return b;
    }
}

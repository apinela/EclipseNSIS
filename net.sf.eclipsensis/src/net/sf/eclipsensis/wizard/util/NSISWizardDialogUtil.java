/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.util;

import java.io.File;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.ColorEditor;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.ImageManager;
import net.sf.eclipsensis.wizard.NSISWizard;
import net.sf.eclipsensis.wizard.settings.*;
import net.sf.eclipsensis.wizard.settings.dialogs.NSISContentBrowserDialog;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISWizardDialogUtil
{
    public static final String LABEL = "LABEL"; //$NON-NLS-1$

    private static void addSlave(MasterSlaveController masterSlaveController, Control slave)
    {
        if(masterSlaveController != null) {
            masterSlaveController.addSlave(slave);
        }
    }

    public static Label createLabel(Composite parent, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Label l = new Label(parent,SWT.LEFT|SWT.WRAP);
        if(labelResource != null) {
            l.setText(EclipseNSISPlugin.getResourceString(labelResource));
        }
        if(isRequired) {
            setRequiredElementFont(l);
        }
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        l.setLayoutData(data);
        l.setEnabled(enabled);
        
        addSlave(masterSlaveController, l);
        return l;
    }

    /**
     * @param control
     */
    public static void setRequiredElementFont(Control control)
    {
        Font f = control.getFont();
        FontData[] fd = f.getFontData();
        fd[0].setStyle(SWT.BOLD);
        final Font f2 = new Font(control.getDisplay(),fd);
        control.setFont(f2);
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e)
            {
                f2.dispose();
            }
        });
    }

    public static Composite checkParentLayoutColumns(Composite parent, int numColumns)
    {
        GridLayout layout = (GridLayout)parent.getLayout();
        if(layout.numColumns < numColumns) {
            parent = new Composite(parent, SWT.NONE);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = layout.numColumns;
            parent.setLayoutData(data);
            
            layout = new GridLayout(numColumns,layout.makeColumnsEqualWidth);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            parent.setLayout(layout);
        }
        return parent;
    }

    public static Text createText(Composite parent, String value, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        GridLayout layout = (GridLayout)parent.getLayout();
        parent = checkParentLayoutColumns(parent, 2);
        Label l = createLabel(parent, labelResource, enabled, masterSlaveController, isRequired);
        Text t = createText(parent, value, layout.numColumns - 1, enabled, masterSlaveController);
        t.setData(LABEL,l);
        return t;
    }

    public static Text createText(Composite parent, String value, int horizontalSpan, boolean enabled, MasterSlaveController masterSlaveController)
    {
        return createText(parent, value, SWT.SINGLE | SWT.BORDER, horizontalSpan, enabled, masterSlaveController);
    }

    public static Text createText(Composite parent, String value, int style, int horizontalSpan, boolean enabled, MasterSlaveController masterSlaveController)
    {
        Text t = new Text(parent, style);
        t.setText(value);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = horizontalSpan;
        t.setLayoutData(data);
        t.setEnabled(enabled);
        addSlave(masterSlaveController, t);
        
        return t;
    }

    public static Text createDirectoryBrowser(Composite parent, String value, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        parent = checkParentLayoutColumns(parent, 3);
        GridLayout layout = (GridLayout)parent.getLayout();
        final Text t = createText(parent, value, labelResource, enabled, masterSlaveController, isRequired);
        ((GridData)t.getLayoutData()).horizontalSpan = layout.numColumns - 2;
    
        final Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
        button.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        button.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Shell shell = button.getShell();
                DirectoryDialog dialog = new DirectoryDialog(shell, SWT.NONE);
                String directory = dialog.open();
                if (!Common.isEmpty(directory)) { 
                    t.setText(directory);
                }
            }
        });
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        button.setLayoutData(data);
        button.setEnabled(enabled);
        addSlave(masterSlaveController, button);
        
        return t;
    }

    public static Text createFileBrowser(Composite parent, String value, final boolean isSave, final String[] filterNames, final String[] filterExtensions, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        parent = checkParentLayoutColumns(parent, 3);
        GridLayout layout = (GridLayout)parent.getLayout();
        final Text t = createText(parent, value, labelResource, enabled, masterSlaveController, isRequired);
        ((GridData)t.getLayoutData()).horizontalSpan = layout.numColumns - 2;
    
        createFileBrowserButton(parent, isSave, filterNames, filterExtensions, t, enabled, masterSlaveController);
        
        return t;
    }

    /**
     * @param parent
     * @param isSave
     * @param filterNames
     * @param filterExtensions
     * @param masterSlaveController
     * @param t
     */
    public static Button createFileBrowserButton(Composite parent, final boolean isSave, final String[] filterNames, final String[] filterExtensions, final Text t, boolean enabled, MasterSlaveController masterSlaveController)
    {
        final Button button = new Button(parent, SWT.PUSH | SWT.CENTER);
        button.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        button.setToolTipText(EclipseNSISPlugin.getResourceString("browse.tooltip")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                Shell shell = button.getShell();
                FileDialog dialog = new FileDialog(shell, (isSave?SWT.SAVE:SWT.OPEN));
                dialog.setFileName(t.getText());
                dialog.setFilterNames(filterNames);
                dialog.setFilterExtensions(filterExtensions);
                String file = dialog.open();
                if (!Common.isEmpty(file)) { 
                    t.setText(file);
                }
            }
        });
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        button.setLayoutData(data);
        button.setEnabled(enabled);
        addSlave(masterSlaveController, button);
        
        return button;
    }

    public static Text createImageBrowser(Composite parent, String value, Point size, final String[] filterNames, final String[] filterExtensions, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
        {
            parent = checkParentLayoutColumns(parent, 2);
            GridLayout layout = (GridLayout)parent.getLayout();
            Label l = createLabel(parent, labelResource, enabled, masterSlaveController, isRequired); 
            parent = new Composite(parent,SWT.NONE);
            GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
            data.horizontalSpan = layout.numColumns - 1;
            parent.setLayoutData(data);
            layout = new GridLayout(3,false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            parent.setLayout(layout);
            final Text t = createText(parent, value, 1, enabled, masterSlaveController);
            t.setData(LABEL,l);
    
            createFileBrowserButton(parent, false, filterNames, filterExtensions, t, enabled, masterSlaveController);
            
            final Label l2 = new Label(parent, SWT.BORDER | SWT.SHADOW_IN | SWT.CENTER);
            data = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
            if(size != null) {
                if(size.x != SWT.DEFAULT) {
                    data.widthHint = size.x;
                }
                if(size.y != SWT.DEFAULT) {
                    data.heightHint = size.y;
                }
            }
            l2.setLayoutData(data);
            l2.setEnabled(enabled);
            addSlave(masterSlaveController, l2);
            
            t.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    String fileName = ((Text)e.widget).getText();
                    Image image = null;;
                    if(!Common.isEmpty(fileName) && Common.isValidFile(fileName)) {
                        try {
                            image = ImageManager.getImage(new File(fileName).toURL());
                        }
                        catch (Exception ex) {
                            image = null;
    //                        e.printStackTrace();
                        }
                    }
                    l2.setImage(image);
                    l2.setData((image==null?null:image.getImageData()));
                }
             });
            
            return t;
        }

    public static Combo createCombo(Composite parent, String[] items, int selectedItem, boolean isReadOnly, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        return createCombo(parent, items, items[selectedItem],isReadOnly,labelResource, enabled, masterSlaveController, isRequired);
    }

    public static Combo createCombo(Composite parent, String[] items, String selectedItem, boolean isReadOnly, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        parent = checkParentLayoutColumns(parent, 2);
        Label l = createLabel(parent, labelResource, enabled, masterSlaveController, isRequired);
    
        Combo c = createCombo(parent, ((GridLayout)parent.getLayout()).numColumns - 1, items, selectedItem, isReadOnly, enabled, masterSlaveController);
        c.setData(LABEL,l);
        
        return c;
    }

    /**
     * @param parent
     * @param items
     * @param selectedItem
     * @param isReadOnly
     * @param enabled
     * @param masterSlaveController
     * @return
     */
    public static Combo createCombo(Composite parent, int horizontalSpan, String[] items, String selectedItem, boolean isReadOnly, boolean enabled, MasterSlaveController masterSlaveController)
    {
        GridLayout layout = (GridLayout)parent.getLayout();
        Combo c = new Combo(parent, SWT.DROP_DOWN | (isReadOnly?SWT.READ_ONLY:SWT.NONE));
        for (int i = 0; i < items.length; i++) {
            c.add(items[i]);
        }
        c.setText(selectedItem);
    
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        data.horizontalSpan = horizontalSpan;
        c.setLayoutData(data);
        c.setEnabled(enabled);
        addSlave(masterSlaveController, c);
        return c;
    }

    public static ColorEditor createColorEditor(Composite parent, RGB value, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        parent = checkParentLayoutColumns(parent, 2);
        Label l = createLabel(parent, labelResource, enabled, masterSlaveController, isRequired);
    
        GridLayout layout = (GridLayout)parent.getLayout();
        ColorEditor ce = new ColorEditor(parent);
        ce.setRGB(value);
        Button b = ce.getButton();
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = layout.numColumns - 1;
        b.setLayoutData(data);
        b.setEnabled(enabled);
        addSlave(masterSlaveController, b);
        b.setData(LABEL,l);
        
        return ce;
    }

    public static Button[] createRadioGroup(Composite parent, String[] items, int selectedItem, String labelResource, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        parent = checkParentLayoutColumns(parent, 2);
        Label l = createLabel(parent, labelResource, enabled, masterSlaveController, isRequired);
    
        GridLayout layout = (GridLayout)parent.getLayout();
        parent = new Composite(parent,SWT.NONE);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING                      );
        gd.horizontalSpan = layout.numColumns - 1;
        parent.setLayoutData(gd);
        layout = new GridLayout(items.length,true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent.setLayout(layout);
        
        Button[] buttons = new Button[items.length];
        for (int i = 0; i < items.length; i++) {
            buttons[i] = new Button(parent, SWT.RADIO | SWT.LEFT);
            buttons[i].setText(items[i]);
            buttons[i].setData(new Integer(i));
            if(i == selectedItem) {
                buttons[i].setSelection(true);
            }
            buttons[i].setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            buttons[i].setEnabled(enabled);
            addSlave(masterSlaveController, buttons[i]);
            buttons[i].setData(LABEL,l);
        }
        
        return buttons;
    }

    public static Group createGroup(Composite parent, int numColumns, String labelResource, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString(labelResource));
        if(isRequired) {
            setRequiredElementFont(group);
        }
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout(numColumns,false);
        group.setLayout(layout);
        addSlave(masterSlaveController, group);
        
        return group;
    }

    public static Button createCheckBox(Composite parent, String labelResource, boolean state, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
        if(labelResource != null) {
            button.setText(EclipseNSISPlugin.getResourceString(labelResource));
        }
        button.setSelection(state);
        button.setEnabled(enabled);
        addSlave(masterSlaveController, button);
        
        if(isRequired) {
            setRequiredElementFont(button);
        }
        GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
        button.setLayoutData(data);
        return button;
    }

    /**
     * @param composite
     * @param labelResource
     * @param value
     * @param settings
     * @param enabled
     * @param m2
     * @param isRequired
     * @return
     */
    public static Combo createContentBrowser(Composite parent, String labelResource, String value, final NSISWizard wizard, boolean enabled, MasterSlaveController masterSlaveController, boolean isRequired)
    {
        parent = checkParentLayoutColumns(parent,3);
        int numColumns = ((GridLayout)parent.getLayout()).numColumns;
        GridData gd;
        Label l = createLabel(parent,labelResource,enabled,masterSlaveController,isRequired);
        Composite composite = new Composite(parent,SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = numColumns-1;
        composite.setLayoutData(gd);
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        
        final Combo c2 = createCombo(composite,1,NSISKeywords.PREDEFINED_PATH_VARIABLES,value,false,enabled,masterSlaveController);
        gd = (GridData)c2.getLayoutData();
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        c2.setData(LABEL, l);
        
        final Button b = new Button(composite, SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
        gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        b.setLayoutData(gd);
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                NSISContentBrowserDialog dialog = new NSISContentBrowserDialog(b.getShell(),wizard.getSettings());
                if(dialog.open() == Window.OK) {
                    INSISInstallElement element = dialog.getSelectedElement();
                    StringBuffer text = new StringBuffer(""); //$NON-NLS-1$
                    if(element instanceof NSISInstallFiles.FileItem) {
                        String destination = ((NSISInstallFiles)element.getParent()).getDestination();
                        text.append(destination);
                        if(!destination.endsWith("\\")) { //$NON-NLS-1$
                            text.append("\\"); //$NON-NLS-1$
                        }
                        text.append(new File(((NSISInstallFiles.FileItem)element).getName()).getName());
                    }
                    else if(element instanceof NSISInstallFile) {
                        String destination = ((NSISInstallFile)element).getDestination();
                        text.append(destination);
                        if(!destination.endsWith("\\")) { //$NON-NLS-1$
                            text.append("\\"); //$NON-NLS-1$
                        }
                        text.append(new File(((NSISInstallFile)element).getName()).getName());
                    }
                    else if(element instanceof NSISInstallDirectory) {
                        String destination = ((NSISInstallDirectory)element).getDestination();
                        text.append(destination);
                        if(!destination.endsWith("\\")) { //$NON-NLS-1$
                            text.append("\\"); //$NON-NLS-1$
                        }
                        text.append(new File(((NSISInstallDirectory)element).getName()).getName());
                    }
                    c2.setText(text.toString());
                }
            }
        });
        addSlave(masterSlaveController,b);
        return c2;
    }
}

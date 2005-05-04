/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.DialogSize;
import net.sf.eclipsensis.installoptions.model.DialogSizeManager;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.CollectionContentProvider;

import org.eclipse.draw2d.ScalableFigure;
import org.eclipse.draw2d.Viewport;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;

public class InstallOptionsPreferencePage extends PropertyPage implements IWorkbenchPreferencePage, IInstallOptionsConstants
{
    private Map mGeneralSettingsMap = new HashMap();;
    private Map mGridSettingsMap = new HashMap();;
    private Map mSnapGlueSettingsMap = new HashMap();
    private Map mDialogSizesMap = new LinkedHashMap();
    private DialogSize mDefaultDialogSize = null;
    private CheckboxTableViewer mDialogSizeViewer;
    private Button mAddDialogSize;
    private Button mEditDialogSize;
    private Button mRemoveDialogSize;
    private Button mShowRulers;
    private Button mShowGrid;
    private Button mShowGuides;
    private Button mShowDialogSize;
    private Combo mZoom;
    private GridSettings mGridSettings;
    private SnapGlueSettings mSnapGlueSettings;

    /**
     * 
     */
    public InstallOptionsPreferencePage()
    {
        super();
        setDescription(InstallOptionsPlugin.getResourceString("preference.page.description")); //$NON-NLS-1$
        loadPreferences();
    }

    private void loadPreferences()
    {
        // Ruler preference
        loadPreference(mGeneralSettingsMap, PREFERENCE_SHOW_RULERS,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_RULERS_DEFAULT);

        // Snap to Geometry preference
        loadPreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GEOMETRY,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GEOMETRY_DEFAULT);

        // Grid preferences
        loadPreference(mGeneralSettingsMap, PREFERENCE_SHOW_GRID,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_GRID_DEFAULT);
        loadPreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GRID,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GRID_DEFAULT);
        loadPreference(mGridSettingsMap, PREFERENCE_GRID_STYLE,TypeConverter.STRING_CONVERTER,
                GRID_STYLE_DEFAULT);
        loadPreference(mGridSettingsMap, PREFERENCE_GRID_ORIGIN,TypeConverter.POINT_CONVERTER,
                GRID_ORIGIN_DEFAULT);
        loadPreference(mGridSettingsMap, PREFERENCE_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER,
                GRID_SPACING_DEFAULT);

        // Guides preferences
        loadPreference(mGeneralSettingsMap, PREFERENCE_SHOW_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_GUIDES_DEFAULT);
        loadPreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GUIDES_DEFAULT);
        loadPreference(mSnapGlueSettingsMap, PREFERENCE_GLUE_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                GLUE_TO_GUIDES_DEFAULT);

        // Dialog size preferences
        loadPreference(mGeneralSettingsMap, PREFERENCE_SHOW_DIALOG_SIZE,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_DIALOG_SIZE_DEFAULT);
        
        // Zoom
        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            loadPreference(mGeneralSettingsMap, PREFERENCE_ZOOM,TypeConverter.STRING_CONVERTER,
                    ZOOM_DEFAULT);
        }
    }

    private void loadPreference(Map map, String name, TypeConverter converter, Object defaultValue)
    {
        Object o = null;
        try {
            IPreferenceStore store = getPreferenceStore();
            if(store.contains(name) || store.isDefault(name)) {
                o = converter.asType(store.getString(name));
            }
        }
        catch(Exception ex) {
            o = null;
        }
        if(o == null) {
            o = converter.makeCopy(defaultValue);
        }
        map.put(name,o);
    }

    private void savePreferences()
    {
        // Ruler preference
        savePreference(mGeneralSettingsMap, PREFERENCE_SHOW_RULERS,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_RULERS_DEFAULT);

        // Snap to Geometry preference
        savePreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GEOMETRY,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GEOMETRY_DEFAULT);

        // Grid preferences
        savePreference(mGeneralSettingsMap, PREFERENCE_SHOW_GRID,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_GRID_DEFAULT);
        savePreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GRID,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GRID_DEFAULT);
        savePreference(mGridSettingsMap, PREFERENCE_GRID_STYLE,TypeConverter.STRING_CONVERTER,
                GRID_STYLE_DEFAULT);
        savePreference(mGridSettingsMap, PREFERENCE_GRID_ORIGIN,TypeConverter.POINT_CONVERTER,
                GRID_ORIGIN_DEFAULT);
        savePreference(mGridSettingsMap, PREFERENCE_GRID_SPACING,TypeConverter.DIMENSION_CONVERTER,
                GRID_SPACING_DEFAULT);

        // Guides preferences
        savePreference(mGeneralSettingsMap, PREFERENCE_SHOW_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_GUIDES_DEFAULT);
        savePreference(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                SNAP_TO_GUIDES_DEFAULT);
        savePreference(mSnapGlueSettingsMap, PREFERENCE_GLUE_TO_GUIDES,TypeConverter.BOOLEAN_CONVERTER,
                GLUE_TO_GUIDES_DEFAULT);

        // Dialog size preferences
        savePreference(mGeneralSettingsMap, PREFERENCE_SHOW_DIALOG_SIZE,TypeConverter.BOOLEAN_CONVERTER,
                SHOW_DIALOG_SIZE_DEFAULT);
        
        // Zoom
        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            savePreference(mGeneralSettingsMap, PREFERENCE_ZOOM,TypeConverter.STRING_CONVERTER,
                    ZOOM_DEFAULT);
        }
        
        List list = DialogSizeManager.getDialogSizes();
        list.clear();
        list.addAll(mDialogSizesMap.values());
        DialogSizeManager.storeDialogSizes();
    }

    private void savePreference(Map map, String name, TypeConverter converter, Object defaultValue)
    {
        Object o = map.get(name);
        if(o == null) {
            o = defaultValue;
        }
        getPreferenceStore().putValue(name,converter.asString(o));
    }

    protected IPreferenceStore doGetPreferenceStore()
    {
        return InstallOptionsPlugin.getDefault().getPreferenceStore();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent)
    {
        loadPreferences();
        loadDialogSizes();
        parent = new Composite(parent,SWT.NONE);
        parent.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        parent.setLayout(layout);
        
        Composite composite = new Composite(parent,SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        createGeneralGroup(composite);

        createDialogSizesGroup(composite);
        
        composite = new Composite(parent,SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);
        mGridSettings = new GridSettings(composite,mGridSettingsMap);
        mSnapGlueSettings = new SnapGlueSettings(composite, mSnapGlueSettingsMap);
        return parent;
    }

    private void loadDialogSizes()
    {
        Collection coll = DialogSizeManager.getDialogSizes();
        for (Iterator iter = coll.iterator(); iter.hasNext();) {
            DialogSize element;
            try {
                element = (DialogSize)((DialogSize)iter.next()).clone();
                mDialogSizesMap.put(element.getName().toLowerCase(),element);
            }
            catch (CloneNotSupportedException e) {
            }
        }
    }

    /**
     * @param composite
     */
    private void createDialogSizesGroup(final Composite composite)
    {
        final Group group = new Group(composite,SWT.SHADOW_ETCHED_IN);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        group.setLayoutData(gridData);
        group.setLayout(new GridLayout(1,false));
        group.setText(InstallOptionsPlugin.getResourceString("dialog.sizes.group.name")); //$NON-NLS-1$
        
        Label l = new Label(group,SWT.WRAP);
        l.setLayoutData(new GridData());
        l.setText(InstallOptionsPlugin.getResourceString("dialog.size.group.description")); //$NON-NLS-1$

        final Composite composite2 = new Composite(group,SWT.NONE);
        composite2.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);
        
        final Table table= new Table(composite2, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        FontData fontData = table.getFont().getFontData()[0];
        fontData.setStyle(SWT.BOLD);
        final Font boldFont = new Font(table.getShell().getDisplay(),fontData);
        table.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                boldFont.dispose();
            }
        });
        
        table.setHeaderVisible(true);
        table.setLinesVisible(true);        

        TableLayout tableLayout= new TableLayout();
        table.setLayout(tableLayout);

        final TableColumn[] columns = new TableColumn[3];
        columns[0] = new TableColumn(table, SWT.NONE);      
        columns[0].setText(EclipseNSISPlugin.getResourceString("Name")); //$NON-NLS-1$
        
        columns[1] = new TableColumn(table, SWT.NONE);      
        columns[1].setText(EclipseNSISPlugin.getResourceString("Width")); //$NON-NLS-1$
        
        columns[2] = new TableColumn(table, SWT.NONE);      
        columns[2].setText(EclipseNSISPlugin.getResourceString("Height")); //$NON-NLS-1$
        
        mDialogSizeViewer = new CheckboxTableViewer(table);
        DialogSizeLabelProvider provider = new DialogSizeLabelProvider();
        provider.setDefaultFont(boldFont);
        mDialogSizeViewer.setLabelProvider(provider);
        mDialogSizeViewer.setContentProvider(new CollectionContentProvider());
        mDialogSizeViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                editDialogSize();
            }
        });

        final Composite buttons= new Composite(composite2, SWT.NONE);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        layout= new GridLayout();
        layout.marginHeight= 0;
        layout.marginWidth= 0;
        buttons.setLayout(layout);

        mAddDialogSize = new Button(buttons, SWT.PUSH);
        mAddDialogSize.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("add.icon"))); //$NON-NLS-1$
        mAddDialogSize.setToolTipText(EclipseNSISPlugin.getResourceString("new.tooltip")); //$NON-NLS-1$
        mAddDialogSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mAddDialogSize.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                new DialogSizeDialog(getShell(),null).open();
            }
        });

        mEditDialogSize = new Button(buttons, SWT.PUSH);
        mEditDialogSize.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("edit.icon"))); //$NON-NLS-1$
        mEditDialogSize.setToolTipText(EclipseNSISPlugin.getResourceString("edit.tooltip")); //$NON-NLS-1$
        mEditDialogSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mEditDialogSize.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                editDialogSize();
            }
        });

        mRemoveDialogSize = new Button(buttons, SWT.PUSH);
        mRemoveDialogSize.setImage(EclipseNSISPlugin.getImageManager().getImage(EclipseNSISPlugin.getResourceString("delete.icon"))); //$NON-NLS-1$
        mRemoveDialogSize.setToolTipText(EclipseNSISPlugin.getResourceString("remove.tooltip")); //$NON-NLS-1$
        mRemoveDialogSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mRemoveDialogSize.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                IStructuredSelection selection= (IStructuredSelection) mDialogSizeViewer.getSelection();
                if(!selection.isEmpty()) {
                    Collection coll = (Collection)mDialogSizeViewer.getInput();
                    for(Iterator iter=selection.toList().iterator(); iter.hasNext(); ) {
                        DialogSize ds = (DialogSize)iter.next();
                        coll.remove(ds);
                        if(mDefaultDialogSize.equals(ds)) {
                            mDefaultDialogSize = null;
                        }
                    }
                    if(mDefaultDialogSize == null && coll.size() > 0) {
                        mDefaultDialogSize = (DialogSize)coll.iterator().next();
                        mDefaultDialogSize.setDefault(true);
                    }
                    mDialogSizeViewer.refresh();
                    mDialogSizeViewer.setAllChecked(false);
                    mDialogSizeViewer.setChecked(mDefaultDialogSize,true);
                }
            }
        });

        mDialogSizeViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                DialogSize dialogSize= (DialogSize)event.getElement();
                boolean checked = event.getChecked();
                Collection dialogSizes = (Collection)mDialogSizeViewer.getInput();
                if(dialogSizes.size() == 1) {
                    checked = true;
                }
                else {
                    for(Iterator iter=dialogSizes.iterator(); iter.hasNext(); ) {
                        DialogSize ds = (DialogSize)iter.next();
                        if(!ds.equals(dialogSize) && ds.isDefault() == checked) {
                            ds.setDefault(!checked);
                            mDialogSizeViewer.setChecked(ds,!checked);
                            mDialogSizeViewer.refresh(ds,true);
                            break;
                        }
                    }
                }
                dialogSize.setDefault(checked);
                mDialogSizeViewer.setChecked(dialogSize,checked);
                mDialogSizeViewer.refresh(dialogSize,true);
                updateButtons();
            }
        });
        
        mDialogSizeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
                updateButtons();
            }
        });

        updateDialogSizeViewerInput();

        composite2.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                Rectangle area= composite2.getClientArea();
                Point preferredSize= table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                int width= area.width - 2 * table.getBorderWidth();
                if (preferredSize.y > area.height) {
                    Point vBarSize = table.getVerticalBar().getSize();
                    width -= vBarSize.x;
                }
                width -= buttons.getSize().x;
                width -= ((GridLayout)composite2.getLayout()).horizontalSpacing;
                int columnWidth = width/4;
                Point oldSize= table.getSize();
                if (oldSize.x <= width) {
                    table.setSize(width, area.height);
                }
                
                columns[0].setWidth(width - 2*columnWidth);
                for (int i = 1; i < columns.length; i++) {
                    columns[i].setWidth(columnWidth);
                }
                if (oldSize.x > width) {
                    table.setSize(width, area.height);
                }
            }
        });
    }

    private void updateDialogSizeViewerInput()
    {
        mDialogSizeViewer.setInput(mDialogSizesMap.values());
        mDialogSizeViewer.setAllChecked(false);
        boolean foundDefault = false;
        for (Iterator iter=mDialogSizesMap.values().iterator(); iter.hasNext(); ) {
            DialogSize ds = (DialogSize)iter.next();
            if(ds.isDefault()) {
                if(!foundDefault) {
                    mDialogSizeViewer.setChecked(ds,true);
                    mDefaultDialogSize = ds;
                    foundDefault = true;
                }
                else {
                    ds.setDefault(false);
                }
            }
        }

        updateButtons();
    }

    /**
     * @param tv
     */
    private void editDialogSize()
    {
        IStructuredSelection sel = (IStructuredSelection)mDialogSizeViewer.getSelection();
        if(!sel.isEmpty() && sel.size() == 1) {
            DialogSize ds = (DialogSize)sel.getFirstElement();
            new DialogSizeDialog(getShell(),ds).open();
        }
    }

    protected void updateButtons() 
    {
        IStructuredSelection selection= (IStructuredSelection) mDialogSizeViewer.getSelection();
        int selectionCount= selection.size();
        int itemCount= mDialogSizeViewer.getTable().getItemCount();
        mEditDialogSize.setEnabled(selectionCount == 1);
        mRemoveDialogSize.setEnabled(selectionCount > 0 && selectionCount <= itemCount);
    }

    /**
     * @param composite
     */
    private void createGeneralGroup(Composite composite)
    {
        Group group = new Group(composite,SWT.SHADOW_ETCHED_IN);
        GridData gridData = new GridData(GridData.FILL_VERTICAL);
        group.setLayoutData(gridData);
        group.setLayout(new GridLayout(2,false));
        group.setText(InstallOptionsPlugin.getResourceString("general.group.name")); //$NON-NLS-1$
        
        mShowRulers = new Button(group,SWT.CHECK);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        mShowRulers.setLayoutData(gridData);
        mShowRulers.setText(InstallOptionsPlugin.getResourceString("show.rulers.label")); //$NON-NLS-1$
        mShowRulers.setSelection(((Boolean)mGeneralSettingsMap.get(PREFERENCE_SHOW_RULERS)).booleanValue());
        mShowRulers.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mGeneralSettingsMap.put(PREFERENCE_SHOW_RULERS, Boolean.valueOf(((Button)e.widget).getSelection()));
            }
        });
        
        mShowGrid = new Button(group,SWT.CHECK);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        mShowGrid.setLayoutData(gridData);
        mShowGrid.setText(InstallOptionsPlugin.getResourceString("show.grid.label")); //$NON-NLS-1$
        mShowGrid.setSelection(((Boolean)mGeneralSettingsMap.get(PREFERENCE_SHOW_GRID)).booleanValue());
        mShowGrid.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mGeneralSettingsMap.put(PREFERENCE_SHOW_GRID, Boolean.valueOf(((Button)e.widget).getSelection()));
            }
        });
        
        mShowGuides = new Button(group,SWT.CHECK);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        mShowGuides.setLayoutData(gridData);
        mShowGuides.setText(InstallOptionsPlugin.getResourceString("show.guides.label")); //$NON-NLS-1$
        mShowGuides.setSelection(((Boolean)mGeneralSettingsMap.get(PREFERENCE_SHOW_GUIDES)).booleanValue());
        mShowGuides.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mGeneralSettingsMap.put(PREFERENCE_SHOW_GUIDES, Boolean.valueOf(((Button)e.widget).getSelection()));
            }
        });
        
        mShowDialogSize = new Button(group,SWT.CHECK);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = 2;
        mShowDialogSize.setLayoutData(gridData);
        mShowDialogSize.setText(InstallOptionsPlugin.getResourceString("show.dialog.size.label")); //$NON-NLS-1$
        mShowDialogSize.setSelection(((Boolean)mGeneralSettingsMap.get(PREFERENCE_SHOW_DIALOG_SIZE)).booleanValue());
        mShowDialogSize.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) 
            {
                mGeneralSettingsMap.put(PREFERENCE_SHOW_DIALOG_SIZE, Boolean.valueOf(((Button)e.widget).getSelection()));
            }
        });
        
        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            Label l = new Label(group,SWT.NONE);
            l.setText(InstallOptionsPlugin.getResourceString("default.zoom.label")); //$NON-NLS-1$
            l.setLayoutData(new GridData());
            final ZoomManager zoomManager = new ZoomManager((ScalableFigure)null, (Viewport)null);
            zoomManager.setZoomLevelContributions(Arrays.asList(ZOOM_LEVEL_CONTRIBUTIONS));
            mZoom = new Combo(group,SWT.DROP_DOWN);
            String[] zoomLevels = zoomManager.getZoomLevelsAsText();
            for (int i = 0; i < zoomLevels.length; i++) {
                mZoom.add(zoomLevels[i]);
            }
            mZoom.setText((String)mGeneralSettingsMap.get(PREFERENCE_ZOOM));
            mZoom.addSelectionListener(new SelectionListener(){
                public void widgetSelected(SelectionEvent e)
                {
                    mGeneralSettingsMap.put(PREFERENCE_ZOOM,mZoom.getText());
                }
    
                public void widgetDefaultSelected(SelectionEvent e)
                {
                    widgetSelected(e);
                }
            });
            mZoom.addFocusListener(new FocusAdapter(){
                public void focusLost(FocusEvent e)
                {
                    String text = mZoom.getText();
                    if (!text.equalsIgnoreCase(ZoomManager.FIT_HEIGHT) &&
                        !text.equalsIgnoreCase(ZoomManager.FIT_ALL) &&
                        !text.equalsIgnoreCase(ZoomManager.FIT_WIDTH)) {
                        try {
                            //Trim off the '%'
                            if (text.charAt(text.length() - 1) == '%') {
                                text = text.substring(0, text.length() - 1);
                            }
                            double zoom = Double.parseDouble(text) / (100.0*zoomManager.getUIMultiplier());
                            zoom = Math.min(zoomManager.getMaxZoom(), zoom);
                            zoom = Math.max(zoomManager.getMinZoom(), zoom);
                            zoom = zoom*100*zoomManager.getUIMultiplier();
                            int zoom2 = (int)zoom;
                            if(zoom == zoom2) {
                                text = zoom2+"%"; //$NON-NLS-1$
                            }
                            else {
                                text = zoom+"%"; //$NON-NLS-1$
                            }
                        } catch (Exception ex) {
                            Display.getCurrent().beep();
                            mZoom.setText((String)mGeneralSettingsMap.get(PREFERENCE_ZOOM));
                            return;
                        }
                    }
                    mGeneralSettingsMap.put(PREFERENCE_ZOOM,text);
                }
            });
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
    }

    protected void performDefaults()
    {
        mGeneralSettingsMap.put(PREFERENCE_SHOW_RULERS,SHOW_RULERS_DEFAULT);
        mShowRulers.setSelection(SHOW_RULERS_DEFAULT.booleanValue());

        mGeneralSettingsMap.put(PREFERENCE_SHOW_GRID,SHOW_GRID_DEFAULT);
        mShowGrid.setSelection(SHOW_GRID_DEFAULT.booleanValue());

        mGeneralSettingsMap.put(PREFERENCE_SHOW_DIALOG_SIZE,SHOW_DIALOG_SIZE_DEFAULT);
        mShowDialogSize.setSelection(SHOW_DIALOG_SIZE_DEFAULT.booleanValue());

        mGeneralSettingsMap.put(PREFERENCE_SHOW_GUIDES,SHOW_GUIDES_DEFAULT);
        mShowGuides.setSelection(SHOW_GUIDES_DEFAULT.booleanValue());

        if(InstallOptionsPlugin.getDefault().isZoomSupported()) {
            mGeneralSettingsMap.put(PREFERENCE_ZOOM,ZOOM_DEFAULT);
            mZoom.setText(ZOOM_DEFAULT);
        }
        
        mDialogSizesMap.clear();
        List list = DialogSizeManager.getPresetDialogSizes();
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            DialogSize element = (DialogSize)iter.next();
            try {
                mDialogSizesMap.put(element.getName().toLowerCase(),element.clone());
            }
            catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        updateDialogSizeViewerInput();
        
        mGridSettingsMap.put(PREFERENCE_GRID_STYLE, GRID_STYLE_DEFAULT);
        mGridSettingsMap.put(PREFERENCE_GRID_ORIGIN, new org.eclipse.draw2d.geometry.Point(GRID_ORIGIN_DEFAULT));
        mGridSettingsMap.put(PREFERENCE_GRID_SPACING, new Dimension(GRID_SPACING_DEFAULT));
        mGridSettings.setSettings(mGeneralSettingsMap);
        
        mSnapGlueSettingsMap.put(PREFERENCE_SNAP_TO_GEOMETRY, SNAP_TO_GEOMETRY_DEFAULT);
        mSnapGlueSettingsMap.put(PREFERENCE_SNAP_TO_GRID, SNAP_TO_GRID_DEFAULT);
        mSnapGlueSettingsMap.put(PREFERENCE_SNAP_TO_GUIDES, SNAP_TO_GUIDES_DEFAULT);
        mSnapGlueSettingsMap.put(PREFERENCE_GLUE_TO_GUIDES, GLUE_TO_GUIDES_DEFAULT);
        mSnapGlueSettings.setSettings(mSnapGlueSettingsMap);
        super.performDefaults();
    }

    public boolean performOk()
    {
        savePreferences();
        return super.performOk();
    }
    
    private class DialogSizeLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider 
    {
        private Font mDefaultFont;
        
        
        public void setDefaultFont(Font defaultFont)
        {
            mDefaultFont = defaultFont;
        }

        /*
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex) 
        {
            return null;
        }
    
        /*
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex) 
        {
            DialogSize ds = (DialogSize) element;
            
            switch (columnIndex) {
                case 0:
                    return ds.getName();
                case 1:
                    return Integer.toString(ds.getSize().width);
                case 2:
                    return Integer.toString(ds.getSize().height);
                default:
                    return ""; //$NON-NLS-1$
            }
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
         */
        public Font getFont(Object element)
        {
            if(element instanceof DialogSize) {
                if(((DialogSize)element).isDefault()) {
                    return mDefaultFont;
                }
            }
            return null;
        }
    }

    public class DialogSizeDialog extends Dialog
    {
        private DialogSize mOriginal;
        private DialogSize mCurrent;
        
        private VerifyListener mNumberVerifyListener = new VerifyListener() {
            public void verifyText(VerifyEvent e)
            {
                char[] chars = e.text.toCharArray();
                for(int i=0; i< chars.length; i++) {
                    if(!Character.isDigit(chars[i])) {
                        e.doit = false;
                        return;
                    }
                }
            }
        };

        /**
         * @param parentShell
         */
        public DialogSizeDialog(Shell parentShell, DialogSize dialogSize)
        {
            super(parentShell);
            mOriginal = dialogSize;
            try {
                mCurrent = (dialogSize==null?new DialogSize("",false,new Dimension()): //$NON-NLS-1$
                                                (DialogSize)dialogSize.clone());
            }
            catch (CloneNotSupportedException e) {
            }
        }

        protected void configureShell(Shell newShell)
        {
            newShell.setText((Common.isEmpty(mCurrent.getName())?InstallOptionsPlugin.getResourceString("dialog.size.dialog.add.title"):InstallOptionsPlugin.getResourceString("dialog.size.dialog.edit.title"))); //$NON-NLS-1$ //$NON-NLS-2$
            super.configureShell(newShell);
        }
        
        public void create()
        {
            super.create();
            updateOKButton();
        }

        /**
         * 
         */
        private void updateOKButton()
        {
            getButton(IDialogConstants.OK_ID).setEnabled(!Common.isEmpty(mCurrent.getName()) && mCurrent.getSize().width > 0 && mCurrent.getSize().height > 0);
        }

        protected void okPressed()
        {
            String oldName = (mOriginal == null?"":mOriginal.getName().toLowerCase()); //$NON-NLS-1$
            String newName = mCurrent.getName().toLowerCase();
            if(((mOriginal == null || !oldName.equals(newName)) && mDialogSizesMap.containsKey(newName))) {
                if(MessageDialog.openQuestion(getShell(),InstallOptionsPlugin.getResourceString("confirm.overwrite.title"), //$NON-NLS-1$
                        InstallOptionsPlugin.getFormattedString("dialog.size.overwrite.message",new Object[]{mCurrent.getName()}))) { //$NON-NLS-1$
                    DialogSize old = (DialogSize)mDialogSizesMap.remove(newName);
                    if(old.equals(mDefaultDialogSize)) {
                        mDefaultDialogSize = null;
                    }
                }
            }
            if(mOriginal == null) {
                mDialogSizesMap.put(newName,mCurrent);
            }
            else {
                mOriginal.setSize(mCurrent.getSize());
                mOriginal.setName(mCurrent.getName());
                if(!oldName.equals(newName)) {
                    mDialogSizesMap.remove(oldName);
                    mDialogSizesMap.put(newName,mOriginal);
                }
                mCurrent = mOriginal;
            }
            if(mDefaultDialogSize == null) {
                mDefaultDialogSize = mCurrent;
                mCurrent.setDefault(true);
                Collection dialogSizes = (Collection)mDialogSizeViewer.getInput();
                for(Iterator iter=dialogSizes.iterator(); iter.hasNext(); ) {
                    DialogSize ds = (DialogSize)iter.next();
                    if(!ds.equals(mDefaultDialogSize)) {
                        ds.setDefault(false);
                    }
                }
            }
            mDialogSizeViewer.refresh(true);
            mDialogSizeViewer.setAllChecked(false);
            mDialogSizeViewer.setChecked(mDefaultDialogSize,true);
            updateButtons();
            super.okPressed();
        }

        protected Control createDialogArea(Composite parent)
        {
            parent = (Composite)super.createDialogArea(parent);
            Composite composite = new Composite(parent,SWT.NONE);
            composite.setLayoutData(new GridData(GridData.FILL_BOTH));
            GridLayout gridLayout = new GridLayout(2,false);
            gridLayout.marginWidth = 0;
            gridLayout.marginHeight = 0;
            composite.setLayout(gridLayout);
            Label l = new Label(composite,SWT.NONE);
            l.setText(InstallOptionsPlugin.getResourceString("dialog.size.dialog.name.label")); //$NON-NLS-1$
            l.setLayoutData(new GridData());
            
            final Text name = new Text(composite,SWT.BORDER);
            name.setText(mCurrent.getName());
            name.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    mCurrent.setName(name.getText());
                    updateOKButton();
                }}
            );
            initializeDialogUnits(name);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.widthHint = convertWidthInCharsToPixels(50);
            name.setLayoutData(data);
            
            l = new Label(composite,SWT.NONE);
            l.setText(InstallOptionsPlugin.getResourceString("dialog.size.dialog.width.label")); //$NON-NLS-1$
            l.setLayoutData(new GridData());
            
            final Text width = new Text(composite,SWT.BORDER);
            width.setText(Integer.toString(mCurrent.getSize().width));
            width.addVerifyListener(mNumberVerifyListener);
            width.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    String text = width.getText();
                    mCurrent.getSize().width = (Common.isEmpty(text)?0:Integer.parseInt(text));
                    updateOKButton();
                }}
            );
            width.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e)
                {
                    String text = width.getText();
                    if(Common.isEmpty(text)) {
                        width.setText(Integer.toString(mCurrent.getSize().width));
                    }
                }
            });
            data = new GridData();
            data.widthHint = convertWidthInCharsToPixels(5);
            width.setLayoutData(data);
            
            l = new Label(composite,SWT.NONE);
            l.setText(InstallOptionsPlugin.getResourceString("dialog.size.dialog.height.label")); //$NON-NLS-1$
            l.setLayoutData(new GridData());
            
            final Text height = new Text(composite,SWT.BORDER);
            height.setText(Integer.toString(mCurrent.getSize().height));
            height.addVerifyListener(mNumberVerifyListener);
            height.addModifyListener(new ModifyListener(){
                public void modifyText(ModifyEvent e)
                {
                    String text = height.getText();
                    mCurrent.getSize().height = (Common.isEmpty(text)?0:Integer.parseInt(text));
                    updateOKButton();
                }}
            );
            height.addFocusListener(new FocusAdapter() {
                public void focusLost(FocusEvent e)
                {
                    String text = height.getText();
                    if(Common.isEmpty(text)) {
                        height.setText(Integer.toString(mCurrent.getSize().height));
                    }
                }
            });
            data = new GridData();
            data.widthHint = convertWidthInCharsToPixels(5);
            height.setLayoutData(data);
            
            return parent;
        }
    }
}

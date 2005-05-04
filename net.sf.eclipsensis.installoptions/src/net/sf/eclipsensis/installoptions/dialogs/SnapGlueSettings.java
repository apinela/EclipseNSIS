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

import java.util.Map;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class SnapGlueSettings extends Composite implements IInstallOptionsConstants
{
    private Map mSettings = null;
    private Button mSnapToGrid;
    private Button mSnapToGeometry;
    private Button mSnapToGuides;
    private Button mGlueToGuides;

    /**
     * @param parent
     * @param style
     */
    public SnapGlueSettings(Composite parent, Map settings)
    {
        super(parent, SWT.NONE);
		initialize();
        setSettings(settings);
    }
    
    private boolean getSetting(String name, Boolean defaultSetting)
    {
        boolean b = defaultSetting.booleanValue();
        try {
            b = ((Boolean)mSettings.get(name)).booleanValue();
        }
        catch(Exception ex) {
            b = defaultSetting.booleanValue();
        }
        return b;
    }

    public void setSettings(Map settings)
    {
        mSettings = settings;
        mSnapToGrid.setSelection(getSetting(PREFERENCE_SNAP_TO_GRID,SNAP_TO_GRID_DEFAULT));
        mSnapToGeometry.setSelection(getSetting(PREFERENCE_SNAP_TO_GEOMETRY,SNAP_TO_GEOMETRY_DEFAULT));
        mSnapToGuides.setSelection(getSetting(PREFERENCE_SNAP_TO_GUIDES,SNAP_TO_GUIDES_DEFAULT));
        mGlueToGuides.setSelection(getSetting(PREFERENCE_GLUE_TO_GUIDES,GLUE_TO_GUIDES_DEFAULT));
    }

    private void initialize() 
    {
        setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        setLayout(gridLayout);

        Group group = new Group(this,SWT.SHADOW_ETCHED_IN);
        group.setText(InstallOptionsPlugin.getResourceString("snap.glue.settings.group.name")); //$NON-NLS-1$
        gridLayout = new GridLayout(1,true);
        group.setLayout(gridLayout);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));

        mSnapToGrid = new Button(group, SWT.CHECK);
        mSnapToGrid.setText(InstallOptionsPlugin.getResourceString("snap.glue.settings.snap.grid.label")); //$NON-NLS-1$
        mSnapToGrid.setToolTipText(InstallOptionsPlugin.getResourceString("snap.glue.settings.snap.grid.tooltip")); //$NON-NLS-1$
        mSnapToGrid.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSnapToGrid.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mSettings.put(PREFERENCE_SNAP_TO_GRID,Boolean.valueOf(mSnapToGrid.getSelection()));
            }
        });
        
        mSnapToGeometry = new Button(group, SWT.CHECK);
        mSnapToGeometry.setText(InstallOptionsPlugin.getResourceString("snap.glue.settings.snap.geometry.label")); //$NON-NLS-1$
        mSnapToGeometry.setToolTipText(InstallOptionsPlugin.getResourceString("snap.glue.settings.snap.geometry.tooltip")); //$NON-NLS-1$
        mSnapToGeometry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSnapToGeometry.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mSettings.put(PREFERENCE_SNAP_TO_GEOMETRY,Boolean.valueOf(mSnapToGeometry.getSelection()));
            }
        });
        
        mSnapToGuides = new Button(group, SWT.CHECK);
        mSnapToGuides.setText(InstallOptionsPlugin.getResourceString("snap.glue.settings.snap.guides.label")); //$NON-NLS-1$
        mSnapToGuides.setToolTipText(InstallOptionsPlugin.getResourceString("snap.glue.settings.snap.guides.tooltip")); //$NON-NLS-1$
        mSnapToGuides.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mSnapToGuides.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mSettings.put(PREFERENCE_SNAP_TO_GUIDES,Boolean.valueOf(mSnapToGuides.getSelection()));
            }
        });
        
        mGlueToGuides = new Button(group, SWT.CHECK);
        mGlueToGuides.setText(InstallOptionsPlugin.getResourceString("snap.glue.settings.glue.guides.label")); //$NON-NLS-1$
        mGlueToGuides.setToolTipText(InstallOptionsPlugin.getResourceString("snap.glue.settings.glue.guides.tooltip")); //$NON-NLS-1$
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalIndent = 16;
        mGlueToGuides.setLayoutData(data);
        mGlueToGuides.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                mSettings.put(PREFERENCE_GLUE_TO_GUIDES,Boolean.valueOf(mGlueToGuides.getSelection()));
            }
        });
	}
}
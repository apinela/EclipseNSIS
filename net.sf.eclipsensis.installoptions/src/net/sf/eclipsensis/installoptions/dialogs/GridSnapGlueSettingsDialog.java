/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dialogs;

import java.util.HashMap;
import java.util.Map;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsGridLayer;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

public class GridSnapGlueSettingsDialog extends Dialog implements IInstallOptionsConstants
{
    private GraphicalViewer mViewer;
    private Map mGridSettingsMap = new HashMap();
    private Map mSnapGlueSettingsMap = new HashMap();

    /**
     * @param parentShell
     */
    public GridSnapGlueSettingsDialog(Shell parentShell, GraphicalViewer viewer)
    {
        super(parentShell);
        mViewer = viewer;
        loadViewerProperty(mGridSettingsMap, PREFERENCE_GRID_ORIGIN, SnapToGrid.PROPERTY_GRID_ORIGIN, GRID_ORIGIN_DEFAULT);
        loadViewerProperty(mGridSettingsMap, PREFERENCE_GRID_SPACING, SnapToGrid.PROPERTY_GRID_SPACING, GRID_SPACING_DEFAULT);
        loadViewerProperty(mGridSettingsMap, PREFERENCE_GRID_STYLE, InstallOptionsGridLayer.PROPERTY_GRID_STYLE, GRID_STYLE_DEFAULT);
        loadViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GRID, SnapToGrid.PROPERTY_GRID_ENABLED, SNAP_TO_GRID_DEFAULT);
        loadViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GEOMETRY, SnapToGeometry.PROPERTY_SNAP_ENABLED, SNAP_TO_GEOMETRY_DEFAULT);
        loadViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GUIDES, PROPERTY_SNAP_TO_GUIDES, SNAP_TO_GUIDES_DEFAULT);
        loadViewerProperty(mSnapGlueSettingsMap, PREFERENCE_GLUE_TO_GUIDES, PROPERTY_GLUE_TO_GUIDES, GLUE_TO_GUIDES_DEFAULT);
    }

    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(InstallOptionsPlugin.getResourceString("grid.snap.glue.settings.dialog.name")); //$NON-NLS-1$
        newShell.setImage(InstallOptionsPlugin.getShellImage());
    }

    private Object makeCopy(Object o)
    {
        if(o instanceof Point) {
            o = new Point((Point)o);
        }
        else if(o instanceof Dimension) {
            o = new Dimension((Dimension)o);
        }
        return o;
    }

    private void loadViewerProperty(Map map, String mapName, String name, Object defaultValue)
    {
        Object o = null;
        try {
            o = mViewer.getProperty(name);
        }
        catch(Exception e) {
        }
        if(o == null) {
            o = defaultValue;
        }
        map.put(mapName,makeCopy(o));
    }

    private void saveViewerProperty(Map map, String mapName, String name, Object defaultValue)
    {
        Object o = null;
        try {
            o = map.get(mapName);
        }
        catch(Exception e) {
        }
        if(o == null) {
            o = defaultValue;
        }
        mViewer.setProperty(name,o);
    }

    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);

        new GridSettings(composite,mGridSettingsMap);
        new SnapGlueSettings(composite,mSnapGlueSettingsMap);

        initializeDialogUnits(composite);
        GridData data = (GridData)composite.getLayoutData();
        data.widthHint = convertWidthInCharsToPixels(50);
        return composite;
    }

    protected void okPressed()
    {
        saveViewerProperty(mGridSettingsMap, PREFERENCE_GRID_ORIGIN, SnapToGrid.PROPERTY_GRID_ORIGIN, GRID_ORIGIN_DEFAULT);
        saveViewerProperty(mGridSettingsMap, PREFERENCE_GRID_SPACING, SnapToGrid.PROPERTY_GRID_SPACING, GRID_SPACING_DEFAULT);
        saveViewerProperty(mGridSettingsMap, PREFERENCE_GRID_STYLE, InstallOptionsGridLayer.PROPERTY_GRID_STYLE, GRID_STYLE_DEFAULT);
        saveViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GRID, SnapToGrid.PROPERTY_GRID_ENABLED, SNAP_TO_GRID_DEFAULT);
        saveViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GEOMETRY, SnapToGeometry.PROPERTY_SNAP_ENABLED, SNAP_TO_GEOMETRY_DEFAULT);
        saveViewerProperty(mSnapGlueSettingsMap, PREFERENCE_SNAP_TO_GUIDES, PROPERTY_SNAP_TO_GUIDES, SNAP_TO_GUIDES_DEFAULT);
        saveViewerProperty(mSnapGlueSettingsMap, PREFERENCE_GLUE_TO_GUIDES, PROPERTY_GLUE_TO_GUIDES, GLUE_TO_GUIDES_DEFAULT);
        super.okPressed();
    }
}

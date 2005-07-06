/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.swt.graphics.Color;

public interface IInstallOptionsConstants
{
    public static final String PLUGIN_NAME = InstallOptionsPlugin.getDefault().getBundle().getSymbolicName();

    public static final String RESOURCE_BUNDLE = "net.sf.eclipsensis.installoptions.InstallOptionsPluginResources"; //$NON-NLS-1$
    public static final String MESSAGE_BUNDLE = "net.sf.eclipsensis.installoptions.InstallOptionsPluginMessages"; //$NON-NLS-1$

    public static final String INSTALLOPTIONS_DESIGN_EDITOR_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.design.editor.id"); //$NON-NLS-1$
    public static final String INSTALLOPTIONS_SOURCE_EDITOR_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.source.editor.id"); //$NON-NLS-1$

    public static final String SWITCH_EDITOR_COMMAND_ID = InstallOptionsPlugin.getBundleResourceString("%switch.editor.command.id"); //$NON-NLS-1$
    public static final String EDITING_INSTALLOPTIONS_SOURCE_CONTEXT_ID = InstallOptionsPlugin.getBundleResourceString("%editing.installoptions.source.id"); //$NON-NLS-1$
    public static final String EDITING_INSTALLOPTIONS_DESIGN_CONTEXT_ID = InstallOptionsPlugin.getBundleResourceString("%editing.installoptions.design.id"); //$NON-NLS-1$
    
    public static final String INSTALLOPTIONS_PROBLEM_MARKER_ID = InstallOptionsPlugin.getBundleResourceString("%installoptions.qualified.problem.marker.id"); //$NON-NLS-1$

    public static final String GRID_STYLE_LINES="GridStyleLines"; //$NON-NLS-1$
    public static final String GRID_STYLE_DOTS="GridStyleDots"; //$NON-NLS-1$
    
    public static Boolean SHOW_GRID_DEFAULT = Boolean.FALSE;
    public static Boolean SHOW_RULERS_DEFAULT = Boolean.FALSE;
    public static Boolean SHOW_GUIDES_DEFAULT = Boolean.TRUE;
    public static Boolean SHOW_DIALOG_SIZE_DEFAULT = Boolean.TRUE;
    public static Boolean SNAP_TO_GRID_DEFAULT = Boolean.TRUE;
    public static Boolean SNAP_TO_GEOMETRY_DEFAULT = Boolean.TRUE;
    public static Boolean SNAP_TO_GUIDES_DEFAULT = Boolean.TRUE;
    public static Boolean GLUE_TO_GUIDES_DEFAULT = Boolean.TRUE;
    public static Dimension GRID_SPACING_DEFAULT = new Dimension(10,10);
    public static Point GRID_ORIGIN_DEFAULT = new Point(0, 0);
    public static String GRID_STYLE_DEFAULT = GRID_STYLE_LINES;
    public static String ZOOM_DEFAULT = "100%"; //$NON-NLS-1$
    public static Dimension DIALOG_SIZE_DEFAULT = new Dimension(300,140); //$NON-NLS-1$
   
    public static final String PREFERENCE_SHOW_GRID = "ShowGrid"; //$NON-NLS-1$
    public static final String PREFERENCE_SHOW_RULERS = "ShowRulers"; //$NON-NLS-1$
    public static final String PREFERENCE_SHOW_GUIDES = "ShowGuides"; //$NON-NLS-1$
    public static final String PREFERENCE_SHOW_DIALOG_SIZE = "ShowDialogSize"; //$NON-NLS-1$
    public static final String PREFERENCE_SNAP_TO_GRID = "SnapToGrid"; //$NON-NLS-1$
    public static final String PREFERENCE_SNAP_TO_GEOMETRY = "SnapToGeometry"; //$NON-NLS-1$
    public static final String PREFERENCE_SNAP_TO_GUIDES = "SnapToGuides"; //$NON-NLS-1$
    public static final String PREFERENCE_GLUE_TO_GUIDES = "GlueToGuides"; //$NON-NLS-1$
    public static final String PREFERENCE_GRID_SPACING = "GridSpacing"; //$NON-NLS-1$
    public static final String PREFERENCE_GRID_ORIGIN = "GridOrigin"; //$NON-NLS-1$
    public static final String PREFERENCE_GRID_STYLE = "GridStyle"; //$NON-NLS-1$
    public static final String PREFERENCE_ZOOM = "Zoom"; //$NON-NLS-1$

    public static final String PREFERENCE_SYNTAX_STYLES = "SyntaxStyles";
    public static final String SECTION_STYLE = "SectionStyle";
    public static final String COMMENT_STYLE = "CommentStyle";    
    public static final String KEY_STYLE = "KeyStyle";    
    public static final String KEY_VALUE_DELIM_STYLE = "KeyValueDelimStyle";    
    public static final String NUMBER_STYLE = "NumberStyle";    
    
    public static final String QUALIFIED_NAME_PREFIX = PLUGIN_NAME;
    
    public static final QualifiedName FILEPROPERTY_SHOW_GRID = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SHOW_GRID);
    public static final QualifiedName FILEPROPERTY_SHOW_RULERS = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SHOW_RULERS);
    public static final QualifiedName FILEPROPERTY_SHOW_GUIDES = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SHOW_GUIDES);
    public static final QualifiedName FILEPROPERTY_SHOW_DIALOG_SIZE = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SHOW_DIALOG_SIZE);
    public static final QualifiedName FILEPROPERTY_SNAP_TO_GRID = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SNAP_TO_GRID);
    public static final QualifiedName FILEPROPERTY_SNAP_TO_GEOMETRY = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SNAP_TO_GEOMETRY);
    public static final QualifiedName FILEPROPERTY_SNAP_TO_GUIDES = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_SNAP_TO_GUIDES);
    public static final QualifiedName FILEPROPERTY_GLUE_TO_GUIDES = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_GLUE_TO_GUIDES);
    public static final QualifiedName FILEPROPERTY_GRID_SPACING = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_GRID_SPACING);
    public static final QualifiedName FILEPROPERTY_GRID_ORIGIN = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_GRID_ORIGIN);
    public static final QualifiedName FILEPROPERTY_GRID_STYLE = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_GRID_STYLE);
    public static final QualifiedName FILEPROPERTY_ZOOM = new QualifiedName(QUALIFIED_NAME_PREFIX,PREFERENCE_ZOOM);
    public static final QualifiedName FILEPROPERTY_DIALOG_SIZE = new QualifiedName(QUALIFIED_NAME_PREFIX,"DialogSize"); //$NON-NLS-1$
    public static final QualifiedName FILEPROPERTY_PROBLEM_MARKERS = new QualifiedName(QUALIFIED_NAME_PREFIX,"ProblemMarkers"); //$NON-NLS-1$
    
    public static final String PROPERTY_SNAP_TO_GUIDES = "net.sf.eclipsensis.installoptions.snap_to_guides"; //$NON-NLS-1$
    public static final String PROPERTY_GLUE_TO_GUIDES = "net.sf.eclipsensis.installoptions.glue_to_guides"; //$NON-NLS-1$
    public static final String PROPERTY_DIALOG_SIZE = "net.sf.eclipsensis.installoptions.dialog_size"; //$NON-NLS-1$
    public static final String PROPERTY_SHOW_DIALOG_SIZE = "net.sf.eclipsensis.installoptions.show_dialog_size"; //$NON-NLS-1$
    
    public static final String GRID_SNAP_GLUE_SETTINGS_ACTION_ID = "net.sf.eclipsensis.installoptions.grid_snap_glue_settings"; //$NON-NLS-1$
    public static final String[] ZOOM_LEVEL_CONTRIBUTIONS = new String[] {   
        ZoomManager.FIT_ALL, 
        ZoomManager.FIT_HEIGHT, 
        ZoomManager.FIT_WIDTH
    };

    public static final Color GHOST_FILL_COLOR = new Color(null, 31, 31, 31);
    
    public static final String REQ_REORDER_PART="reorder part"; //$NON-NLS-1$
    public static final String REQ_EXTENDED_EDIT="extended edit"; //$NON-NLS-1$
    
    public static final int SEND_BACKWARD = 1;
    public static final int SEND_TO_BACK = 2;
    public static final int BRING_FORWARD = 3;
    public static final int BRING_TO_FRONT = 4;

    public static final char LIST_SEPARATOR = '|';
}

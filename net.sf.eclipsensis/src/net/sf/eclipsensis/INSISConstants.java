/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis;

import org.eclipse.core.runtime.QualifiedName;

public interface INSISConstants
{
    public static final String PLUGIN_ID = EclipseNSISPlugin.getDefault().getBundle().getSymbolicName();

    public static final String MAKENSIS_EXE = "makensis.exe"; //$NON-NLS-1$

    public static final String PLUGIN_CONTEXT_PREFIX = PLUGIN_ID + "."; //$NON-NLS-1$
    
    public static final String PREFERENCE_PAGE_ID = EclipseNSISPlugin.getBundleResourceString("%preference.page.id"); //$NON-NLS-1$
    public static final String EDITOR_PREFERENCE_PAGE_ID = EclipseNSISPlugin.getBundleResourceString("%editor.preference.page.id"); //$NON-NLS-1$
    public static final String TEMPLATES_PREFERENCE_PAGE_ID = EclipseNSISPlugin.getBundleResourceString("%template.preference.page.id"); //$NON-NLS-1$
    public static final String TASKTAGS_PREFERENCE_PAGE_ID = EclipseNSISPlugin.getBundleResourceString("%task.tags.preference.page.id"); //$NON-NLS-1$
    public static final String HTMLHELP_ID = EclipseNSISPlugin.getBundleResourceString("%htmlhelp.id"); //$NON-NLS-1$
    public static final String CONSOLE_ID = EclipseNSISPlugin.getBundleResourceString("%console.id"); //$NON-NLS-1$
    public static final String PROBLEM_MARKER_ID = EclipseNSISPlugin.getBundleResourceString("%compile.problem.marker.id"); //$NON-NLS-1$
    public static final String TASK_MARKER_ID = EclipseNSISPlugin.getBundleResourceString("%task.marker.id"); //$NON-NLS-1$ //$NON-NLS-2$
    public static final String NSIS_EDITOR_CONTEXT_ID = EclipseNSISPlugin.getBundleResourceString("%context.editingNSISSource.id"); //$NON-NLS-1$
    public static final String COMPILE_ACTION_ID = EclipseNSISPlugin.getBundleResourceString("%compile.action.id"); //$NON-NLS-1$
    public static final String COMPILE_TEST_ACTION_ID = EclipseNSISPlugin.getBundleResourceString("%compile.test.action.id"); //$NON-NLS-1$
    public static final String INSERT_TEMPLATE_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.template.command.id"); //$NON-NLS-1$
    public static final String GOTO_HELP_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%goto.help.command.id"); //$NON-NLS-1$
    public static final String STICKY_HELP_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%sticky.help.command.id"); //$NON-NLS-1$
    public static final String INSERT_FILE_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.file.command.id"); //$NON-NLS-1$
    public static final String INSERT_DIRECTORY_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.directory.command.id"); //$NON-NLS-1$
    public static final String INSERT_COLOR_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%insert.color.command.id"); //$NON-NLS-1$
    public static final String TABS_TO_SPACES_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%tabs.to.spaces.command.id"); //$NON-NLS-1$
    public static final String TOGGLE_COMMENT_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%toggle.comment.command.id"); //$NON-NLS-1$
    public static final String ADD_BLOCK_COMMENT_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%add.block.comment.command.id"); //$NON-NLS-1$
    public static final String REMOVE_BLOCK_COMMENT_COMMAND_ID = EclipseNSISPlugin.getBundleResourceString("%remove.block.comment.command.id"); //$NON-NLS-1$
    
    public static final String PLUGIN_HELP_LOCATION_PREFIX = "help/"; //$NON-NLS-1$
    public static final String DOCS_LOCATION_PREFIX = "Docs/"; //$NON-NLS-1$
    public static final String CACHED_HELP_LOCATION = PLUGIN_HELP_LOCATION_PREFIX+DOCS_LOCATION_PREFIX; //$NON-NLS-1$
    public static final String NSIS_HELP_PREFIX = PLUGIN_HELP_LOCATION_PREFIX+"NSIS/"; //$NON-NLS-1$
    public static final String NSIS_CHM_HELP_FILE = "NSIS.chm"; //$NON-NLS-1$
    public static final String LANGUAGE_FILES_LOCATION = "Contrib\\Language files"; //$NON-NLS-1$
    public static final String MUI_LANGUAGE_FILES_LOCATION = "Contrib\\Modern UI\\Language files"; //$NON-NLS-1$
    public static final String LANGUAGE_FILES_EXTENSION = ".nlf"; //$NON-NLS-1$
    public static final String MUI_LANGUAGE_FILES_EXTENSION = ".nsh"; //$NON-NLS-1$
    public static final String RESOURCE_BUNDLE = "net.sf.eclipsensis.EclipseNSISPluginResources"; //$NON-NLS-1$
    public static final String MESSAGE_BUNDLE = "net.sf.eclipsensis.EclipseNSISPluginMessages"; //$NON-NLS-1$
    
    public static final QualifiedName NSIS_COMPILE_TIMESTAMP = new QualifiedName(PLUGIN_ID,"nsisCompileTimestamp"); //$NON-NLS-1$
    public static final QualifiedName NSIS_EXE_NAME = new QualifiedName(PLUGIN_ID,"nsisEXEName"); //$NON-NLS-1$
    public static final QualifiedName NSIS_EXE_TIMESTAMP = new QualifiedName(PLUGIN_ID,"nsisEXETimestamp"); //$NON-NLS-1$
    
    public static final char LINE_CONTINUATION_CHAR = '\\';
    public static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
    public static final char[][] QUOTE_ESCAPE_SEQUENCES = {{'$','\\','"'},{'$','\\','\''},{'$','\\','`'}};
    public static final char[][] WHITESPACE_ESCAPE_SEQUENCES = {{'$','\\','r'},{'$','\\','n'},{'$','\\','t'}};
    
    public static final int DIALOG_TEXT_LIMIT = 100;
    public static final int DEFAULT_NSIS_TEXT_LIMIT = 1024;
    
    public static final String UNINSTALL_SECTION_NAME = "Uninstall"; //$NON-NLS-1$
    
    public static final String NSIS_PLUGINS_LOCATION = "Plugins"; //$NON-NLS-1$
    public static final String NSIS_PLUGINS_EXTENSION = ".dll"; //$NON-NLS-1$
}

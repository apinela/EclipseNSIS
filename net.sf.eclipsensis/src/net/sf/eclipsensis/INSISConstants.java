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
    public static final String PLUGIN_NAME = EclipseNSISPlugin.getDefault().getBundle().getSymbolicName();

    public static final String MAKENSIS_EXE = "makensis.exe"; //$NON-NLS-1$

    public static final String PLUGIN_CONTEXT_PREFIX = PLUGIN_NAME + "."; //$NON-NLS-1$
    public static final String NSIS_EDITOR_CONTEXT = PLUGIN_CONTEXT_PREFIX + "nsis_editor_context"; //$NON-NLS-1$
    
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
    public static final String CONSOLE_ID = "net.sf.eclipsensis.console.NSISConsole"; //$NON-NLS-1$
    public static final String PROBLEM_ID = "net.sf.eclipsensis.NSISCompileProblem"; //$NON-NLS-1$
    public static final String NSIS_EDITOR_CONTEXT_ID = "net.sf.eclipsensis.NSISEditorScope"; //$NON-NLS-1$
    public static final String COMPILE_ACTION_ID = "net.sf.eclipsensis.actions.NSISCompileAction"; //$NON-NLS-1$
    public static final String COMPILE_TEST_ACTION_ID = "net.sf.eclipsensis.actions.NSISCompileTestAction"; //$NON-NLS-1$

    public static final String GOTO_HELP_COMMAND_ID = "net.sf.eclipsensis.commands.NSISGotoHelp"; //$NON-NLS-1$
    public static final String STICKY_HELP_COMMAND_ID = "net.sf.eclipsensis.commands.NSISStickyHelp"; //$NON-NLS-1$
    public static final String INSERT_FILE_COMMAND_ID = "net.sf.eclipsensis.commands.NSISInsertFile"; //$NON-NLS-1$
    public static final String INSERT_DIRECTORY_COMMAND_ID = "net.sf.eclipsensis.commands.NSISInsertDirectory"; //$NON-NLS-1$
    public static final String INSERT_COLOR_COMMAND_ID = "net.sf.eclipsensis.commands.NSISInsertColor"; //$NON-NLS-1$
    public static final String TABS_TO_SPACES_COMMAND_ID = "net.sf.eclipsensis.commands.NSISTabsToSpaces"; //$NON-NLS-1$
    public static final String TOGGLE_COMMENT_COMMAND_ID = "net.sf.eclipsensis.commands.NSISToggleComment"; //$NON-NLS-1$
    public static final String ADD_BLOCK_COMMENT_COMMAND_ID = "net.sf.eclipsensis.commands.NSISAddBlockComment"; //$NON-NLS-1$
    public static final String REMOVE_BLOCK_COMMENT_COMMAND_ID = "net.sf.eclipsensis.commands.NSISRemoveBlockComment"; //$NON-NLS-1$
    
    public static final QualifiedName NSIS_COMPILE_TIMESTAMP = new QualifiedName(PLUGIN_NAME,"nsisCompileTimestamp"); //$NON-NLS-1$
    public static final QualifiedName NSIS_EXE_NAME = new QualifiedName(PLUGIN_NAME,"nsisEXEName"); //$NON-NLS-1$
    public static final QualifiedName NSIS_EXE_TIMESTAMP = new QualifiedName(PLUGIN_NAME,"nsisEXETimestamp"); //$NON-NLS-1$
    
    public static final char LINE_CONTINUATION_CHAR = '\\';
    public static final char[][] QUOTE_ESCAPE_SEQUENCES = {{'$','\\','"'},{'$','\\','\''},{'$','\\','`'}};
    public static final char[][] WHITESPACE_ESCAPE_SEQUENCES = {{'$','\\','r'},{'$','\\','n'},{'$','\\','t'}};
    
    public static final int DIALOG_TEXT_LIMIT = 100;
    public static final int DEFAULT_NSIS_TEXT_LIMIT = 1024;
    
    public static final String UNINSTALL_SECTION_NAME = "Uninstall"; //$NON-NLS-1$
}

/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import net.sf.eclipsensis.EclipseNSISPlugin;

public interface INSISPreferenceConstants
{
    public static final String CUSTOM_TEMPLATES = "customTemplates"; //$NON-NLS-1$
    public static final String NSIS_HOME = "nsisHome"; //$NON-NLS-1$
    public static final String USE_ECLIPSE_HELP = "useEclipseHelp"; //$NON-NLS-1$
    public static final String USE_GLOBALS = "useGlobals"; //$NON-NLS-1$
    public static final String HDRINFO = "hdrInfo"; //$NON-NLS-1$
    public static final String VERBOSITY = "verbosity"; //$NON-NLS-1$
    public static final String LICENSE = "license"; //$NON-NLS-1$
    public static final String NOCONFIG = "noConfig"; //$NON-NLS-1$
    public static final String NOCD = "noCD"; //$NON-NLS-1$
    public static final String COMPRESSOR = "compressor"; //$NON-NLS-1$
    public static final String INSTRUCTIONS = "instructions"; //$NON-NLS-1$
    public static final String SYMBOLS = "symbols"; //$NON-NLS-1$
    public static final String TASK_TAGS = "taskTags"; //$NON-NLS-1$
    public static final String CASE_SENSITIVE_TASK_TAGS = "caseSensitiveTaskTags"; //$NON-NLS-1$
    public static final int VERBOSITY_NONE = 0;
    public static final int VERBOSITY_ERRORS = 1;
    public static final int VERBOSITY_WARNINGS = 2;
    public static final int VERBOSITY_INFO = 3;
    public static final int VERBOSITY_ALL = 4;
    public static final String[] VERBOSITY_ARRAY = new String[]{EclipseNSISPlugin.getResourceString("verbosity.none.text"), //$NON-NLS-1$
                                                                EclipseNSISPlugin.getResourceString("verbosity.errors.text"), //$NON-NLS-1$
                                                                EclipseNSISPlugin.getResourceString("verbosity.warnings.text"), //$NON-NLS-1$
                                                                EclipseNSISPlugin.getResourceString("verbosity.info.text"), //$NON-NLS-1$
                                                                EclipseNSISPlugin.getResourceString("verbosity.all.text")}; //$NON-NLS-1$

    public static final String USE_SPACES_FOR_TABS = "useSpacesForTabs"; //$NON-NLS-1$
    
    public final static String MATCHING_DELIMITERS = "matchingDelimiters"; //$NON-NLS-1$
    public final static String MATCHING_DELIMITERS_COLOR = "matchingDelimitersColor"; //$NON-NLS-1$

    public static final String COMMENTS_STYLE = "commentsStyle"; //$NON-NLS-1$
    public static final String COMPILETIME_COMMANDS_STYLE = "compiletimeCommandsStyle"; //$NON-NLS-1$
    public static final String INSTALLER_ATTRIBUTES_STYLE = "installerAttributesStyle"; //$NON-NLS-1$
    public static final String COMMANDS_STYLE = "commandsStyle"; //$NON-NLS-1$
    public static final String INSTRUCTIONS_STYLE = "instructionsStyle"; //$NON-NLS-1$
    public static final String INSTRUCTION_PARAMETERS_STYLE = "instructionParametersStyle"; //$NON-NLS-1$
    public static final String INSTRUCTION_OPTIONS_STYLE = "instructionOptionsStyle";; //$NON-NLS-1$
    public static final String PREDEFINED_VARIABLES_STYLE = "predefinedVariablesStyle"; //$NON-NLS-1$
    public static final String USERDEFINED_VARIABLES_STYLE = "userdefinedVariablesStyle"; //$NON-NLS-1$
    public static final String SYMBOLS_STYLE = "symbolsStyle"; //$NON-NLS-1$
    public static final String CALLBACKS_STYLE = "callbacksStyle"; //$NON-NLS-1$
    public static final String STRINGS_STYLE = "stringsStyle"; //$NON-NLS-1$
    public static final String NUMBERS_STYLE = "numbersStyle"; //$NON-NLS-1$
    public static final String LANGSTRINGS_STYLE = "langstringsStyle"; //$NON-NLS-1$
    public static final String TASK_TAGS_STYLE = "taskTagsStyle"; //$NON-NLS-1$
    
    public static final String AUTO_SHOW_CONSOLE = "autoShowConsole"; //$NON-NLS-1$
    public static final String CONSOLE_FONT = "net.sf.eclipsensis.console.Font"; //$NON-NLS-1$
    public static final String CONSOLE_INFO_COLOR = "net.sf.eclipsensis.console.InfoColor"; //$NON-NLS-1$
    public static final String CONSOLE_WARNING_COLOR = "net.sf.eclipsensis.console.WarningColor"; //$NON-NLS-1$
    public static final String CONSOLE_ERROR_COLOR = "net.sf.eclipsensis.console.ErrorColor"; //$NON-NLS-1$
    public static final String TEMPLATE_VARIABLE_COLOR = "net.sf.eclipsensis.template.TemplateVariableColor"; //$NON-NLS-1$
}

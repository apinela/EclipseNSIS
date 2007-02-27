/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import net.sf.eclipsensis.console.NSISConsoleLine;


public interface INSISPreferenceConstants extends INSISSettingsConstants
{
    public static final String CUSTOM_TEMPLATES = "customTemplates"; //$NON-NLS-1$
    public static final String NSIS_HOME = "nsisHome"; //$NON-NLS-1$
    public static final String NOTIFY_MAKENSIS_CHANGED = "notifyMakeNSISChanged"; //$NON-NLS-1$
    public static final String USE_ECLIPSE_HELP = "useEclipseHelp"; //$NON-NLS-1$
    public static final String TASK_TAGS = "taskTags"; //$NON-NLS-1$
    public static final String CASE_SENSITIVE_TASK_TAGS = "caseSensitiveTaskTags"; //$NON-NLS-1$

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
    public static final String PLUGINS_STYLE = "pluginsStyle"; //$NON-NLS-1$

    public static final String WARN_PROCESS_PRIORITY = "warnProcessPriority"; //$NON-NLS-1$

    public static final String AUTO_SHOW_CONSOLE = "autoShowConsole"; //$NON-NLS-1$
    public static final int AUTO_SHOW_CONSOLE_NEVER = 0;
    public static final int AUTO_SHOW_CONSOLE_ERROR = NSISConsoleLine.TYPE_ERROR;
    public static final int AUTO_SHOW_CONSOLE_WARNING = AUTO_SHOW_CONSOLE_ERROR|NSISConsoleLine.TYPE_WARNING;
    public static final int AUTO_SHOW_CONSOLE_ALWAYS = AUTO_SHOW_CONSOLE_WARNING|NSISConsoleLine.TYPE_INFO;
    public static final int[] AUTO_SHOW_CONSOLE_ARRAY = {AUTO_SHOW_CONSOLE_ALWAYS, AUTO_SHOW_CONSOLE_WARNING,
                                                         AUTO_SHOW_CONSOLE_ERROR,AUTO_SHOW_CONSOLE_NEVER};
    public static final String CONSOLE_FONT = "net.sf.eclipsensis.console.Font"; //$NON-NLS-1$
    public static final String CONSOLE_INFO_COLOR = "net.sf.eclipsensis.console.InfoColor"; //$NON-NLS-1$
    public static final String CONSOLE_WARNING_COLOR = "net.sf.eclipsensis.console.WarningColor"; //$NON-NLS-1$
    public static final String CONSOLE_ERROR_COLOR = "net.sf.eclipsensis.console.ErrorColor"; //$NON-NLS-1$
    public static final String TEMPLATE_VARIABLE_COLOR = "net.sf.eclipsensis.template.TemplateVariableColor"; //$NON-NLS-1$

    public static final String REGEDIT_LOCATION = "regeditLocation"; //$NON-NLS-1$
    public static final String NSIS_COMMAND_VIEW_FLAT_MODE = "nsisCommandViewFlatMode"; //$NON-NLS-1$
    public static final String NSIS_HELP_VIEW_SHOW_NAV = "nsisHelpViewShowNav"; //$NON-NLS-1$
    public static final String NSIS_HELP_VIEW_SYNCHED = "nsisHelpViewSynched"; //$NON-NLS-1$

    public static final String BEFORE_COMPILE_SAVE = "beforeCompileSave"; //$NON-NLS-1$
    public static final int BEFORE_COMPILE_SAVE_CURRENT_CONFIRM = 0;
    public static final int BEFORE_COMPILE_SAVE_ALL_CONFIRM = 1;
    public static final int BEFORE_COMPILE_SAVE_CURRENT_AUTO = 2;
    public static final int BEFORE_COMPILE_SAVE_ALL_AUTO = 3;
    public static final int BEFORE_COMPILE_SAVE_DEFAULT = BEFORE_COMPILE_SAVE_CURRENT_CONFIRM;
}

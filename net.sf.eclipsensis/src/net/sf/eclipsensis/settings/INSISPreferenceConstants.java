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

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;

public interface INSISPreferenceConstants
{
    public static final String CUSTOM_TEMPLATES = "customTemplates"; //$NON-NLS-1$
    public static final String NSIS_HOME = "nsisHome"; //$NON-NLS-1$
    public static final String USE_INTEGRATED_HELP = "useIntegratedHelp"; //$NON-NLS-1$
    public static final String USE_DEFAULTS = "useDefaults"; //$NON-NLS-1$
    public static final String HDRINFO = "hdrInfo"; //$NON-NLS-1$
    public static final String VERBOSITY = "verbosity"; //$NON-NLS-1$
    public static final String LICENSE = "license"; //$NON-NLS-1$
    public static final String NOCONFIG = "noConfig"; //$NON-NLS-1$
    public static final String NOCD = "noCD"; //$NON-NLS-1$
    public static final String COMPRESSOR = "compressor"; //$NON-NLS-1$
    public static final String INSTRUCTIONS = "instructions"; //$NON-NLS-1$
    public static final String SYMBOLS = "symbols"; //$NON-NLS-1$
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

    public static final String CURRENT_LINE_COLOR=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
    public static final String CURRENT_LINE=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;

    public static final String TAB_WIDTH=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH;

    public static final String PRINT_MARGIN_COLOR=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR;
    public static final String PRINT_MARGIN_COLUMN=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN;
    public static final String PRINT_MARGIN=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN;
            
    public static final String OVERVIEW_RULER=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER;
            
    public static final String LINE_NUMBER_RULER_COLOR=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
    public static final String LINE_NUMBER_RULER=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
    public static final String USE_CUSTOM_CARETS=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_USE_CUSTOM_CARETS;
    public static final String WIDE_CARET=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_WIDE_CARET;
            
    public static final String SELECTION_FOREGROUND_COLOR=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_COLOR;
    public static final String SELECTION_FOREGROUND_DEFAULT_COLOR=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_FOREGROUND_DEFAULT_COLOR;
    public static final String SELECTION_BACKGROUND_COLOR=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_COLOR;
    public static final String SELECTION_BACKGROUND_DEFAULT_COLOR=AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SELECTION_BACKGROUND_DEFAULT_COLOR;
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
    
    public static final String AUTO_SHOW_CONSOLE = "autoShowConsole"; //$NON-NLS-1$
    public static final String EDITOR_FONT = "net.sf.eclipsensis.editor.Font"; //$NON-NLS-1$
    public static final String CONSOLE_FONT = "net.sf.eclipsensis.console.Font"; //$NON-NLS-1$
    public static final String CONSOLE_INFO_COLOR = "net.sf.eclipsensis.console.InfoColor"; //$NON-NLS-1$
    public static final String CONSOLE_WARNING_COLOR = "net.sf.eclipsensis.console.WarningColor"; //$NON-NLS-1$
    public static final String CONSOLE_ERROR_COLOR = "net.sf.eclipsensis.console.ErrorColor"; //$NON-NLS-1$
    public static final String TEMPLATE_VARIABLE_COLOR = "net.sf.eclipsensis.template.TemplateVariableColor"; //$NON-NLS-1$
}

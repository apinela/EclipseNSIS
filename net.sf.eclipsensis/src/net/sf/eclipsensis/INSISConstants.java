/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis;

import org.eclipse.core.runtime.QualifiedName;

/**
 * @author Sunil.Kamath
 */
public interface INSISConstants
{
    public static final String RESOURCE_BUNDLE = "net.sf.eclipsensis.EclipseNSISPluginResources"; //$NON-NLS-1$
    public static final String CONSOLE_ID = "net.sf.eclipsensis.console.NSISConsole"; //$NON-NLS-1$
    
    public static final String NSIS_HOME = "nsisHome"; //$NON-NLS-1$
    public static final String USE_DEFAULTS = "useDefaults"; //$NON-NLS-1$
    public static final String HDRINFO = "hdrInfo"; //$NON-NLS-1$
    public static final String VERBOSITY = "verbosity"; //$NON-NLS-1$
    public static final String LICENSE = "license"; //$NON-NLS-1$
    public static final String NOCONFIG = "noConfig"; //$NON-NLS-1$
    public static final String NOCD = "noCD"; //$NON-NLS-1$
    public static final String COMPRESSOR = "compressor"; //$NON-NLS-1$
    public static final String INSTRUCTIONS = "instructions"; //$NON-NLS-1$
    public static final String SYMBOLS = "symbols"; //$NON-NLS-1$

    public static final String PLUGIN_NAME = EclipseNSISPlugin.getDefault().getBundle().getSymbolicName();
    public static final QualifiedName NSIS_COMPILE_TIMESTAMP = new QualifiedName(PLUGIN_NAME,"nsisCompileTimestamp"); //$NON-NLS-1$
    public static final QualifiedName NSIS_EXE_NAME = new QualifiedName(PLUGIN_NAME,"nsisEXEName"); //$NON-NLS-1$
    public static final QualifiedName NSIS_EXE_TIMESTAMP = new QualifiedName(PLUGIN_NAME,"nsisEXETimestamp"); //$NON-NLS-1$
    
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
    
    public static final int COMPRESSOR_DEFAULT = 0;
    public static final int COMPRESSOR_ZLIB = 1;
    public static final int COMPRESSOR_BZIP2 = 2;
    public static final int COMPRESSOR_LZMA = 3;
    public static final int COMPRESSOR_BEST = 4;
    public static final String[] COMPRESSOR_DISPLAY_ARRAY = new String[]{EclipseNSISPlugin.getResourceString("compressor.default.text"), //$NON-NLS-1$
                                                                 EclipseNSISPlugin.getResourceString("compressor.zlib.text"), //$NON-NLS-1$
                                                                 EclipseNSISPlugin.getResourceString("compressor.bzip2.text"), //$NON-NLS-1$
                                                                 EclipseNSISPlugin.getResourceString("compressor.lzma.text"), //$NON-NLS-1$
                                                                 EclipseNSISPlugin.getResourceString("compressor.best.text")}; //$NON-NLS-1$
    public static final String[] COMPRESSOR_NAME_ARRAY = new String[]{"","zlib","bzip2","lzma","best"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
}

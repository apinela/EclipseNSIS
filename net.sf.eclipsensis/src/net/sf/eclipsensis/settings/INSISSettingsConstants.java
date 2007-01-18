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

import net.sf.eclipsensis.EclipseNSISPlugin;

public interface INSISSettingsConstants
{
    public static final String HDRINFO = "hdrInfo"; //$NON-NLS-1$
    public static final String VERBOSITY = "verbosity"; //$NON-NLS-1$
    public static final String LICENSE = "license"; //$NON-NLS-1$
    public static final String NOCONFIG = "noConfig"; //$NON-NLS-1$
    public static final String NOCD = "noCD"; //$NON-NLS-1$
    public static final String COMPRESSOR = "compressor"; //$NON-NLS-1$
    public static final String SOLID_COMPRESSION = "solidCompression"; //$NON-NLS-1$
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
}

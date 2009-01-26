/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.makensis;

import java.util.ArrayList;

public interface IMakeNSISDelegate
{
    public void startup();

    public void shutdown();

    public boolean isUnicode();

    public long getHwnd();

    void reset();

    String getOutputFileName();

    String getScriptFileName();

    ArrayList getErrors();

    ArrayList getWarnings();
}

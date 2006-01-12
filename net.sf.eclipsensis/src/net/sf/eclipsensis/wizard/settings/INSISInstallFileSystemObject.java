/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

public interface INSISInstallFileSystemObject
{
    /**
     * @return Returns the destination.
     */
    public String getDestination();

    /**
     * @param destination The destination to set.
     */
    public void setDestination(String destination);

    /**
     * @return Returns the overwriteMode.
     */
    public int getOverwriteMode();

    /**
     * @param overwriteMode The overwriteMode to set.
     */
    public void setOverwriteMode(int overwriteMode);
}
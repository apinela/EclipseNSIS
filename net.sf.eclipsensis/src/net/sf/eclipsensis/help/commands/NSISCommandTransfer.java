/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public class NSISCommandTransfer extends ByteArrayTransfer
{
    private static final String[] TYPE_NAMES = {"nsis-command" + System.currentTimeMillis()}; //$NON-NLS-1$
    private static final int[] TYPEIDS = {registerType(TYPE_NAMES[0])};

    private static final NSISCommandTransfer cInstance = new NSISCommandTransfer();
    
    public static NSISCommandTransfer getInstance()
    {
        return cInstance;
    }

    private NSISCommandTransfer()
    {
    }

    protected int[] getTypeIds()
    {
        return TYPEIDS;
    }

    protected String[] getTypeNames()
    {
        return TYPE_NAMES;
    }

    protected void javaToNative(Object data, TransferData transferData) 
    {
        if (data instanceof String) {
            super.javaToNative(((String) data).getBytes(), transferData);
        }
    }

    protected Object nativeToJava(TransferData transferData) {
        byte[] bytes = (byte[]) super.nativeToJava(transferData);
        if (bytes != null) {
            return new String(bytes);
        }
        return null;
    }
}

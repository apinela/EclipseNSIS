/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dnd;

import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.TransferData;

public abstract class InstallOptionsObjectTransfer extends ByteArrayTransfer
{
    private Object mObject;
    private long mStartTime;

    public Object getObject() {
        return mObject;
    }

    public void javaToNative(Object object, TransferData transferData)
    {
        setObject(object);
        mStartTime = System.currentTimeMillis();
        if (transferData != null) {
            super.javaToNative(String.valueOf(mStartTime).getBytes(), transferData);
        }
    }

    public Object nativeToJava(TransferData transferData)
    {
        byte[] bytes = (byte[])super.nativeToJava(transferData);
        //Now, only retain numeric bytes
        //This is a hack for Windows 98
        int i = 0;
        for ( ; i < bytes.length; i++) {
            if(!Character.isDigit((char)bytes[i])) {
                break;
            }
        }
        bytes = (byte[])Common.resizeArray(bytes,i);
        long startTime = Long.parseLong(new String(bytes));
        return (this.mStartTime == startTime?getObject():null);
    }

    public void setObject(Object obj)
    {
        mObject = obj;
    }
}

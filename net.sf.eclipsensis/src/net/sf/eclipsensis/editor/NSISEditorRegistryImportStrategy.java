/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.help.NSISKeywords.ShellConstant;
import net.sf.eclipsensis.settings.NSISPreferences;
import net.sf.eclipsensis.util.*;

class NSISEditorRegistryImportStrategy implements RegistryImporter.IRegistryImportStrategy
{
    private static MessageFormat cCreateRegKeyFormat=new MessageFormat("Push $0\r\nPush $1\r\n;{0} = {1}, REG_CREATE_SUBKEY = 0x0004\r\nSystem::Call \"Advapi32::RegCreateKeyExA(i, t, i, t, i, i, i, *i, i) i({1}, ''{2}'', 0, '''', 0, 0x0004, 0, .r0, 0) .r1\"\r\nStrCmp $1 0 +2\r\nSetErrors\r\nStrCmp $0 0 +2\r\nSystem::Call \"Advapi32::RegCloseKey(i) i(r0) .r1\"\r\nPop $1\r\nPop $0"); //$NON-NLS-1$
    private static MessageFormat cDeleteRegKeyFormat=new MessageFormat("{0} {1} {2}"); //$NON-NLS-1$
    private static MessageFormat cDeleteRegValueFormat=new MessageFormat("{0} {1} {2} {3}"); //$NON-NLS-1$
    private static MessageFormat cWriteRegValueFormat=new MessageFormat("{0} {1} {2} {3} {4}"); //$NON-NLS-1$
    private static MessageFormat cCommentFormat=new MessageFormat(EclipseNSISPlugin.getResourceString("regfile.comment.format")); //$NON-NLS-1$
    private String mDeleteRegKey;
    private String mWriteRegStr;
    private String mDeleteRegValue;
    private String mWriteRegDWORD;
    private String mWriteRegExpandStr;
    private String mWriteRegBin;
    private StringBuffer mBuffer;
    private String mContext;
    private List mShellConstants;
    private int mTextLimit;

    public NSISEditorRegistryImportStrategy()
    {
        mDeleteRegKey = NSISKeywords.getInstance().getKeyword("DeleteRegKey");
        mWriteRegStr = NSISKeywords.getInstance().getKeyword("WriteRegStr");
        mDeleteRegValue = NSISKeywords.getInstance().getKeyword("DeleteRegValue");
        mWriteRegDWORD = NSISKeywords.getInstance().getKeyword("WriteRegDWORD");
        mWriteRegExpandStr = NSISKeywords.getInstance().getKeyword("WriteRegExpandStr");
        mWriteRegBin = NSISKeywords.getInstance().getKeyword("WriteRegBin");
        mBuffer = new StringBuffer("");
        mShellConstants = NSISKeywords.getInstance().getShellConstants();
        try {
            mTextLimit = Integer.parseInt(NSISPreferences.INSTANCE.getNSISDefaultSymbol("NSIS_MAX_STRLEN")); //$NON-NLS-1$
        }
        catch(Exception e){
            mTextLimit = INSISConstants.DEFAULT_NSIS_TEXT_LIMIT;
        }
    }

    public void reset()
    {
        mContext = ShellConstant.CONTEXT_GENERAL;
        mBuffer.setLength(0);
    }

    public String getText()
    {
        return mBuffer.toString();
    }

    public void beginRegistryKeySection(String rootKey, String subKey)
    {
        if(mBuffer.length() > 0) {
            mBuffer.append(INSISConstants.LINE_SEPARATOR);
        }
        mBuffer.append(cCommentFormat.format(new String[] {rootKey,subKey})).append(
                INSISConstants.LINE_SEPARATOR);
    }

    public void addRegistryKey(String rootKey, String subKey)
    {
        addLineToBuf(cCreateRegKeyFormat.format(new String[]{rootKey.toUpperCase(), RegistryImporter.rootKeyNameToHandle(rootKey), subKey})); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void addRegistryValue(String rootKey, String subKey, String value, int type, String data)
    {
        String command;
        switch(type) {
            case WinAPI.REG_BINARY:
                command = mWriteRegBin;
                break;
            case WinAPI.REG_DWORD:
                command = mWriteRegDWORD;
                if(!data.regionMatches(true, 0, "0x", 0, 2)) { //$NON-NLS-1$
                    data = "0x" + data; //$NON-NLS-1$
                }
                break;
            case WinAPI.REG_EXPAND_SZ:
                command = mWriteRegExpandStr; //$NON-NLS-1$ //$NON-NLS-2$
                break;
            case WinAPI.REG_SZ:
                command = mWriteRegStr;
                break;
            default:
                return;
        }
        addLineToBuf(makeRegCommand(cWriteRegValueFormat, new String[]{command, rootKey, subKey, value, data}));
    }

    public void deleteRegistryKey(String rootKey, String subKey)
    {
        addLineToBuf(makeRegCommand(cDeleteRegKeyFormat, new String[] {mDeleteRegKey,rootKey,subKey}));
    }

    public void deleteRegistryValue(String rootKey, String subKey, String value)
    {
        addLineToBuf(makeRegCommand(cDeleteRegValueFormat, new String[]{mDeleteRegValue, rootKey, subKey, value}));
    }
    
    private void addLineToBuf(String line)
    {
        String newContext = ShellConstant.CONTEXT_GENERAL;
        for (Iterator iter = mShellConstants.iterator(); iter.hasNext();) {
            ShellConstant constant = (ShellConstant)iter.next();
            if(constant.value.length() <= line.length()) {
                if(!ShellConstant.CONTEXT_GENERAL.equals(constant.context) &&
                   !newContext.equals(constant.context) &&
                   !ShellConstant.CONTEXT_GENERAL.equals(newContext)){
                    continue;
                }
                String newLine = Common.replaceAll(line, constant.value, constant.name, true);
                if(!newLine.equals(line) && !ShellConstant.CONTEXT_GENERAL.equals(constant.context)) {
                    newContext = constant.context;    
                }
                line = newLine;
            }
        }
        if(!newContext.equals(ShellConstant.CONTEXT_GENERAL) && !newContext.equals(mContext)) {
            mBuffer.append(NSISKeywords.getInstance().getKeyword("SetShellVarContext")).append( //$NON-NLS-1$
                    " ").append(newContext).append(INSISConstants.LINE_SEPARATOR); //$NON-NLS-1$
            mContext = newContext;
        }
        while(line.length() > mTextLimit) {
            mBuffer.append(line.substring(0, mTextLimit-1)).append(
                    INSISConstants.LINE_CONTINUATION_CHAR).append(
                    INSISConstants.LINE_SEPARATOR);
            line = line.substring(mTextLimit-1);
        }
        mBuffer.append(line).append(INSISConstants.LINE_SEPARATOR);
    }

    private static String makeRegCommand(MessageFormat format, String[] args)
    {
        for(int i=0; i<args.length; i++) {
            args[i] = Common.maybeQuote(args[i]==null?"":args[i]); //$NON-NLS-1$
        }
        if(args[args.length-1].endsWith("\\")) { //$NON-NLS-1$
            args[args.length-1] = Common.quote(args[args.length-1]);
        }
        return format.format(args);
    }
}
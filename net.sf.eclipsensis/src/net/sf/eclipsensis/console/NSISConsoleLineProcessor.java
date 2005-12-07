/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.console;

import java.util.regex.Matcher;

import net.sf.eclipsensis.makensis.MakeNSISRunner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class NSISConsoleLineProcessor implements INSISConsoleLineProcessor
{
    private int mWarningCount = 0;
    private boolean mErrorMode = false;
    private IPath mScript = null;

    public NSISConsoleLineProcessor(IPath script)
    {
        mScript = script;
    }

    public NSISConsoleLine processText(String text)
    {
        NSISConsoleLine line;
        text = text.trim();

        String lText = text.toLowerCase();
        if(lText.startsWith("error")) { //$NON-NLS-1$
            Matcher matcher = MakeNSISRunner.MAKENSIS_ERROR_PATTERN.matcher(text);
            if(matcher.matches()) {
                line = NSISConsoleLine.error(text);
                setLineInfo(line, new Path(matcher.group(1)), Integer.parseInt(matcher.group(2)));
                return line;
            }
        }
        if(lText.startsWith("error ") || lText.startsWith("error:") || //$NON-NLS-1$ //$NON-NLS-2$
           lText.startsWith("!include: error ") || lText.startsWith("!include: error:")) { //$NON-NLS-1$ //$NON-NLS-2$
            line = NSISConsoleLine.error(text);
        }
        else if(lText.startsWith("warning ") || lText.startsWith("warning:") || lText.startsWith("invalid ")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            line = NSISConsoleLine.warning(text);
        }
        else if(lText.endsWith(" warning:") || lText.endsWith(" warnings:")) { //$NON-NLS-1$ //$NON-NLS-2$
            Matcher matcher = MakeNSISRunner.MAKENSIS_WARNINGS_PATTERN.matcher(text);
            if(matcher.matches()) {
                mWarningCount = Integer.parseInt(matcher.group(1));
            }
            line = NSISConsoleLine.warning(text);
        }
        else if(MakeNSISRunner.MAKENSIS_SYNTAX_ERROR_PATTERN.matcher(lText).matches()) {
            mErrorMode = true;
            line = NSISConsoleLine.error(text);
        }
        else if(mErrorMode) {
            line = NSISConsoleLine.error(text);
        }
        else if(mWarningCount > 0) {
            mWarningCount--;
            line = NSISConsoleLine.warning(text);
        }
        else {
            line = NSISConsoleLine.info(text);
        }
        if(line.getType() == NSISConsoleLine.TYPE_WARNING) {
            Matcher matcher = MakeNSISRunner.MAKENSIS_WARNING_PATTERN.matcher(text);
            if(matcher.matches()) {
                setLineInfo(line, new Path(matcher.group(1)), Integer.parseInt(matcher.group(2)));
            }
            else if(!text.endsWith("warnings:") && !text.endsWith("warning:")) { //$NON-NLS-1$ //$NON-NLS-2$
                setLineInfo(line, (mScript.getDevice() != null?mScript:null), 1);
            }
        }

        return line;
    }

    private void setLineInfo(NSISConsoleLine line, IPath path, int lineNum)
    {
        if(mScript.getDevice() == null) {
            if(path == null) {
                path = mScript;
            }
            else {
                if(!path.isAbsolute()) {
                    path = ResourcesPlugin.getWorkspace().getRoot().getFile(mScript).getParent().getLocation().append(path);
                }
                else {
                    IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
                    if(file != null) {
                        path = file.getFullPath();
                    }
                }
            }
        }
        else {
            if(path != null) {
                if(!path.isAbsolute()) {
                    path = mScript.removeLastSegments(1).append(path);
                }
            }
            else {
                path = mScript;
            }
        }
        line.setSource(path);
        line.setLineNum(lineNum);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.console.INSISConsoleLineProcessor#reset()
     */
    public void reset()
    {
        mWarningCount = 0;
        mErrorMode = false;
    }
}
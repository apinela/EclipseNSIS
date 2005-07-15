/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import java.util.*;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;

public class INILine
{
    private String mText = ""; //$NON-NLS-1$
    private String mDelimiter = INSISConstants.LINE_SEPARATOR;
    private IINIContainer mParent;
    private List mErrors = new ArrayList();
    private List mWarnings = new ArrayList();
    
    void setText(String text)
    {
        mText = text;
    }

    public String getText()
    {
        return mText;
    }
    
    public String getDelimiter()
    {
        return mDelimiter;
    }
    
    public void setDelimiter(String delimiter)
    {
        mDelimiter = delimiter;
    }
    
    public int getLength()
    {
        String text = getText();
        return (text != null?text.length():0)+(mDelimiter != null?mDelimiter.length():0);
    }

    public IINIContainer getParent()
    {
        return mParent;
    }
    
    public void setParent(IINIContainer parent)
    {
        mParent = parent;
    }
    
    public void update()
    {
    }

    public String toString()
    {
        return (mText != null?mText:"")+(mDelimiter != null?mDelimiter:""); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    public boolean hasErrors()
    {
        return mErrors.size() > 0;
    }

    public boolean hasWarnings()
    {
        return mWarnings.size() > 0;
    }
    
    final void validate()
    {
        mErrors.clear();
        mWarnings.clear();
        checkProblems();
    }
    
    protected void checkProblems()
    {
        if(!Common.isEmpty(getText())) {
            addProblem(INIProblem.TYPE_WARNING,InstallOptionsPlugin.getResourceString("line.ignored.warning")); //$NON-NLS-1$
        }
    }
    
    public List getErrors()
    {
        return (mErrors == null?Collections.EMPTY_LIST:mErrors);
    }
    
    public List getWarnings()
    {
        return (mWarnings == null?Collections.EMPTY_LIST:mWarnings);
    }
    
    public void addProblem(int type, String problem)
    {
        switch(type) {
            case INIProblem.TYPE_WARNING:
                if(!mWarnings.contains(problem)) {
                    mWarnings.add(problem);
                }
                break;
            case INIProblem.TYPE_ERROR:
                if(!mErrors.contains(problem)) {
                    mErrors.add(problem);
                }
        }
    }
}
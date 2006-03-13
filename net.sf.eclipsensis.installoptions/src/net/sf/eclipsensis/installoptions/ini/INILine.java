/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.ini;

import java.io.Serializable;
import java.util.*;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.util.Common;

public class INILine implements Cloneable, Serializable
{
    private static final long serialVersionUID = -1038711652231662150L;

    public static final int VALIDATE_FIX_NONE = 0;
    public static final int VALIDATE_FIX_ERRORS = 1;
    public static final int VALIDATE_FIX_WARNINGS = 2;
    public static final int VALIDATE_FIX_ALL = VALIDATE_FIX_ERRORS|VALIDATE_FIX_WARNINGS;
    
    private String mText = ""; //$NON-NLS-1$
    private String mDelimiter = INSISConstants.LINE_SEPARATOR;
    private IINIContainer mParent;
    private List mErrors = new ArrayList();
    private List mWarnings = new ArrayList();

    public INILine(String text, String delimiter)
    {
        this(text);
        mDelimiter = delimiter;
    }

    public INILine(String text)
    {
        super();
        mText = text;
    }

    public void setText(String text)
    {
        if(!Common.stringsAreEqual(mText, text)) {
            mText = text;
            setDirty(true);
        }
    }
    
    protected void setDirty(boolean dirty)
    {
        if(getParent() != null) {
            getParent().setDirty(dirty);
        }
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
        if(!Common.stringsAreEqual(mDelimiter, delimiter)) {
            mDelimiter = delimiter;
            setDirty(true);
        }
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

    public final void validate()
    {
        validate(VALIDATE_FIX_NONE);
    }

    public void validate(int fixFlag)
    {
        mErrors.clear();
        mWarnings.clear();
        checkProblems(fixFlag);
    }

    protected void checkProblems(int fixFlag)
    {
        if(!Common.isEmpty(getText())) {
            if((fixFlag & INILine.VALIDATE_FIX_WARNINGS)> 0) {
                getParent().removeChild(this);
            }
            else {
                addProblem(INIProblem.TYPE_WARNING,InstallOptionsPlugin.getResourceString("line.ignored.warning")); //$NON-NLS-1$
            }
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
    
    public INILine copy()
    {
        return (INILine)clone();
    }

    public Object clone()
    {
        try {
            INILine line = (INILine)super.clone();
            line.mErrors = new ArrayList();
            line.mWarnings = new ArrayList();
            line.mParent = null;
            return line;
        }
        catch (CloneNotSupportedException e) {
            InstallOptionsPlugin.getDefault().log(e);
            return null;
        }
    }
}

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

import java.util.Arrays;
import java.util.Comparator;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.*;

public class INIProblem
{
    public static final String TYPE_WARNING=IInstallOptionsConstants.INSTALLOPTIONS_WARNING_ANNOTATION_NAME;
    public static final String TYPE_ERROR=IInstallOptionsConstants.INSTALLOPTIONS_ERROR_ANNOTATION_NAME;
    private static final Comparator cReversePositionComparator = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            Position p1 = ((INIProblemFix)o1).getPosition();
            Position p2 = ((INIProblemFix)o2).getPosition();
            int n = p2.offset-p1.offset;
            if(n == 0) {
                n = p2.length-p1.length;
            }
            return n;
        }
    };
    
    private int mLine = 0;
    private String mMessage;
    private String mType;
    private String mFixDescription = null;
    private INIProblemFix[] mFixes = null;

    public INIProblem(String type, String message)
    {
        super();
        mMessage = message;
        mType = type;
    }
    
    public String getFixDescription()
    {
        return mFixDescription;
    }

    void setFix(String description, INIProblemFix fix)
    {
        setFix(description, new INIProblemFix[] {fix});
    }
    
    void setFix(String description, INIProblemFix[] fixes)
    {
        mFixDescription = description;
        mFixes = fixes;
    }
    
    public void fix(IDocument document)
    {
        if(!Common.isEmptyArray(mFixes)) {
            try {
                for (int i=0; i<mFixes.length; i++) {
                    mFixes[i].setPosition(mFixes[i].getLine().getParent().getChildPosition(mFixes[i].getLine()));
                }
                Arrays.sort(mFixes,cReversePositionComparator);
                for (int i=0; i<mFixes.length; i++) {
                    document.replace(mFixes[i].getPosition().offset,mFixes[i].getPosition().length,mFixes[i].getText());
                }
            }
            catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean canFix()
    {
        return mFixDescription != null && !Common.isEmptyArray(mFixes);
    }

    void setLine(int line)
    {
        mLine = line;
    }

    public int getLine()
    {
        return mLine;
    }

    public String getType()
    {
        return mType;
    }

    public String getMessage()
    {
        return mMessage;
    }

    public int hashCode()
    {
        return (mMessage==null?0:mMessage.hashCode())+(mType==null?0:mType.hashCode());
    }
    
    public boolean equals(Object o)
    {
        if(o != this) {
            if(o instanceof INIProblem) {
                INIProblem p = (INIProblem)o;
                return Common.stringsAreEqual(getType(),p.getType()) && 
                       Common.stringsAreEqual(getMessage(),p.getMessage());
            }
            return false;
        }
        return true;
    }
}

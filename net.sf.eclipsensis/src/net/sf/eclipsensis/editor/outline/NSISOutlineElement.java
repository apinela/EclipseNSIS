/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.outline;

import java.util.ArrayList;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Image;

/**
 * An outline element.
 */
public class NSISOutlineElement
{
    static final String ROOT = "ROOT"; //$NON-NLS-1$

    private String mType;
    private String mName;
    private Position mSelectPosition;
    private Position mPosition;
    private ArrayList mChildren = new ArrayList();
    private NSISOutlineElement mParent = null;

    public NSISOutlineElement(String type, Position position)
    {
        this(type,null,position);
    }

    /**
     * @param type
     * @param name
     * @param position
     */
    public NSISOutlineElement(String type, String name, Position position)
    {
        mType = type;
        mName = name;
        setPosition(position);
    }

    void setName(String name)
    {
        mName = name;
    }

    public String toString()
    {
        if(isRoot()) {
            if(!Common.isEmpty(mName)) {
                return mName;
            }
            else {
                return EclipseNSISPlugin.getResourceString("outline.root.label"); //$NON-NLS-1$
            }
        }
        else {
            if(!Common.isEmpty(mName)) {
                return new StringBuffer(getTypeName()).append(" ").append(mName).toString();
            }
            else {
                return getTypeName();
            }
        }
    }

    public boolean isRoot()
    {
        return mParent == null && ROOT.equals(mType);
    }

    public boolean hasChildren()
    {
        return !Common.isEmptyCollection(mChildren);
    }

    /**
     * @return Returns the children.
     */
    public ArrayList getChildren()
    {
        return mChildren;
    }
    /**
     * @return Returns the icon.
     */
    public Image getIcon()
    {
        return NSISOutlineContentResources.getInstance().getImage(mType);
    }

    public String getType()
    {
        return mType;
    }

    /**
     * @return Returns the name.
     */
    public String getTypeName()
    {
        return NSISOutlineContentResources.getInstance().getTypeName(mType);
    }
    /**
     * @return Returns the position.
     */
    public Position getPosition()
    {
        return mPosition;
    }

    /**
     * @param position The position to set.
     */
    void setPosition(Position position)
    {
        mPosition = position;
    }

    public void setSelectPosition(Position selectPosition)
    {
        mSelectPosition = selectPosition;
    }

    public Position getSelectPosition()
    {
        return mSelectPosition;
    }

    public void setParent(NSISOutlineElement parent)
    {
        if(mParent != null) {
            mParent.removeChild(this);
        }
        mParent = parent;
        if(mParent != null && !mParent.mChildren.contains(this)) {
            mParent.addChild(this);
        }
    }

    /**
     * @return Returns the parent.
     */
    public NSISOutlineElement getParent()
    {
        return mParent;
    }

    public void removeChild(NSISOutlineElement child)
    {
        if(child != null && mChildren.contains(child)) {
            if(child.mParent == this) {
                child.mParent = null;
            }
            mChildren.remove(child);
        }
    }

    public void addChild(NSISOutlineElement child)
    {
        if(child !=null && !mChildren.contains(child)) {
            mChildren.add(child);
            child.setParent(this);
        }
    }

    public void merge(Position position)
    {
        if(position != null) {
            int start = Math.min(mPosition.getOffset(),position.getOffset());
            int end = Math.max(mPosition.getOffset()+mPosition.getLength(),
                               position.getOffset()+position.getLength());
            mPosition.setOffset(start);
            mPosition.setLength(end-start);
        }
    }
}
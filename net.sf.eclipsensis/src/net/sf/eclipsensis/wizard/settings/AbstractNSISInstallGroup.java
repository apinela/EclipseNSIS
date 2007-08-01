/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard.settings;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.Common;

import org.eclipse.swt.widgets.Display;
import org.w3c.dom.*;

public abstract class AbstractNSISInstallGroup extends AbstractNSISInstallElement
{
	private static final long serialVersionUID = 6871218426689788748L;

    private Set mChildTypes = new LinkedHashSet();
    private ArrayList mChildren = new ArrayList();
    private transient boolean mExpanded = true;

    /**
     *
     */
    public AbstractNSISInstallGroup()
    {
        super();
        setChildTypes();
    }

    public Object clone() throws CloneNotSupportedException
    {
        AbstractNSISInstallGroup group = (AbstractNSISInstallGroup)super.clone();
        group.mChildren = new ArrayList();
        for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
            INSISInstallElement element = (INSISInstallElement)((INSISInstallElement)iter.next()).clone();
            group.addChild(element);
        }
        group.mChildTypes = new LinkedHashSet();
        group.mChildTypes.addAll(mChildTypes);
        return group;
    }

    protected void addSkippedProperties(Collection skippedProperties)
    {
        super.addSkippedProperties(skippedProperties);
        skippedProperties.add("expanded"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#hasChildren()
     */
    public final boolean hasChildren()
    {
        return mChildren.size() > 0;
    }

    public final int getChildCount()
    {
        return mChildren.size();
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildren()
     */
    public final INSISInstallElement[] getChildren()
    {
        return (INSISInstallElement[])mChildren.toArray(new INSISInstallElement[0]);
    }

    public final void setChildren(INSISInstallElement[] children)
    {
        removeAllChildren();
        for (int i = 0; i < children.length; i++) {
            addChild(children[i]);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#getChildTypes()
     */
    public String[] getChildTypes()
    {
        return (String[])mChildTypes.toArray(new String[0]);
    }

    protected final void clearChildTypes()
    {
        mChildTypes.clear();
        setDirty();
    }

    protected final void addChildType(String childType)
    {
        if(NSISInstallElementFactory.isValidType(childType)) {
            mChildTypes.add(childType);
            setDirty();
        }
    }

    protected final Iterator getChildrenIterator()
    {
        return mChildren.iterator();
    }

    public final boolean acceptsChildType(String type)
    {
        return NSISInstallElementFactory.isValidType(type) && mChildTypes.contains(type);
    }

    public final boolean canAddChild(INSISInstallElement child)
    {
        if(child != null && acceptsChildType(child.getType()) && !mChildren.contains(child)) {
            return true;
        }
        return false;
    }

    public final boolean addChild(int index, INSISInstallElement child)
    {
        if(child != null && acceptsChildType(child.getType())) {
            if(!mChildren.contains(child)) {
                INSISInstallElement oldParent = child.getParent();
                if(oldParent != null) {
                    oldParent.removeChild(child);
                }
                mChildren.add(index, child);
                child.setParent(this);
                child.setSettings(getSettings());
                setDirty();
                return true;
            }
            else {
                if(Display.getCurrent() != null) {
                    Common.openError(Display.getCurrent().getActiveShell(),
                            EclipseNSISPlugin.getFormattedString("duplicate.child.error", new Object[] {getDisplayName(),child.getDisplayName()}),  //$NON-NLS-1$
                            EclipseNSISPlugin.getShellImage());
                }
            }
        }
        return false;
    }

    public final int indexOf(INSISInstallElement child)
    {
        return mChildren.indexOf(child);
    }

    public final boolean removeChild(int index)
    {
        return removeChild((INSISInstallElement)mChildren.get(index));
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#addChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public final boolean addChild(INSISInstallElement child)
    {
        return addChild(mChildren.size(),child);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeChild(net.sf.eclipsensis.wizard.settings.INSISInstallElement)
     */
    public final boolean removeChild(INSISInstallElement child)
    {
        if(child != null && mChildTypes.contains(child.getType()) && mChildren.contains(child)) {
            mChildren.remove(child);
            child.setParent(null);
            child.setSettings(null);
            setDirty();
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.wizard.settings.INSISInstallElement#removeAllChildren()
     */
    public final boolean removeAllChildren()
    {
        if(mChildren.size() > 0) {
            for(Iterator iter=mChildren.iterator(); iter.hasNext(); ) {
                INSISInstallElement child = (INSISInstallElement)iter.next();
                iter.remove();
                child.setParent(null);
                child.setSettings(null);
            }
            setDirty();
            return true;
        }
        return false;
    }

    /**
     * @return Returns the expanded.
     */
    public final boolean isExpanded()
    {
        return mExpanded;
    }

    /**
     * @param expanded The expanded to set.
     */
    public final void setExpanded(boolean expanded)
    {
        setExpanded(expanded, false);
    }

    /**
     * @param expanded The expanded to set.
     * @param recursive Perform recursively
     */
    public final void setExpanded(boolean expanded, boolean recursive)
    {
        mExpanded = expanded;
        if(recursive) {
            if(!Common.isEmptyCollection(mChildren)) {
                for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                    INSISInstallElement child = (INSISInstallElement)iter.next();
                    if(child instanceof AbstractNSISInstallGroup) {
                        ((AbstractNSISInstallGroup)child).setExpanded(expanded, recursive);
                    }
                }
            }
        }
    }

    public final void setSettings(NSISWizardSettings settings)
    {
        super.setSettings(settings);
        if(!Common.isEmptyCollection(mChildren)) {
            for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                ((INSISInstallElement)iter.next()).setSettings(settings);
            }
        }
    }

    public final void resetChildTypes(boolean recursive)
    {
        setChildTypes();
        if(recursive) {
            if(!Common.isEmptyCollection(mChildren)) {
                for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                    INSISInstallElement child = (INSISInstallElement)iter.next();
                    if(child instanceof AbstractNSISInstallGroup) {
                        ((AbstractNSISInstallGroup)child).resetChildTypes(recursive);
                    }
                }
            }
        }
        setDirty();
    }

    public final void resetChildren(boolean recursive)
    {
        INSISInstallElement[] children = getChildren();
        if(!Common.isEmptyArray(children)) {
            removeAllChildren();
            for (int i = 0; i < children.length; i++) {
                addChild(children[i]);
            }
            if(recursive) {
                if(!Common.isEmptyCollection(mChildren)) {
                    for (Iterator iter = mChildren.iterator(); iter.hasNext();) {
                        INSISInstallElement child = (INSISInstallElement)iter.next();
                        if(child instanceof AbstractNSISInstallGroup) {
                            ((AbstractNSISInstallGroup)child).resetChildren(recursive);
                        }
                    }
                }
            }
        }
    }

    protected Node createChildNode(Document document, String name, Object value)
    {
        if(name.equals("children")) { //$NON-NLS-1$
            final Node[] children;
            if(!Common.isEmptyArray(value)) {
                INSISInstallElement[] elements = (INSISInstallElement[])value;
                children = new Node[elements.length];
                for (int i=0; i<elements.length; i++) {
                    children[i] = elements[i].toNode(document);
                }
            }
            else {
                children = new Node[0];
            }
            value = new NodeList() {
                public int getLength()
                {
                    return children.length;
                }

                public Node item(int index)
                {
                    return children[index];
                }
            };
        }
        return super.createChildNode(document, name, value);
    }

    protected Object getNodeValue(Node node, String name, Class clasz)
    {
        if(name.equals("children")) { //$NON-NLS-1$
            NodeList children = node.getChildNodes();
            ArrayList elements = new ArrayList();
            if(children != null) {
                int n = children.getLength();
                for(int i=0; i<n; i++) {
                    Node child = children.item(i);
                    if(child.getNodeName().equals(getNodeName())) {
                        INSISInstallElement installElement = NSISInstallElementFactory.createFromNode(child);
                        if(installElement != null) {
                            addChild(installElement);
                            elements.add(installElement);
                        }
                    }
                }
            }
            return elements.toArray(new INSISInstallElement[elements.size()]);
        }
        else {
            return super.getNodeValue(node, name, clasz);
        }
    }

    public String validate(Collection changedElements)
    {
        String error = super.validate(changedElements);
        if(hasChildren()) {
            String childError;
            INSISInstallElement[] children = getChildren();
            for (int i = 0; i < children.length; i++) {
                childError = children[i].validate(changedElements);
                if(error == null && childError != null) {
                    error = childError;
                }
            }
        }
        return error;
    }

    public String doValidate()
    {
        if(!hasChildren()) {
            return EclipseNSISPlugin.getFormattedString("empty.contents.error",new Object[]{getDisplayName()}); //$NON-NLS-1$
        }
        return super.doValidate();
    }

    public int hashCode()
    {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((mChildren == null)?0:mChildren.hashCode());
        return result;
    }

    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractNSISInstallGroup other = (AbstractNSISInstallGroup)obj;
        if (mChildren == null) {
            if (other.mChildren != null) {
                return false;
            }
        }
        else if (!mChildren.equals(other.mChildren)) {
            return false;
        }
        return true;
    }

    public void setTargetPlatform(int targetPlatform)
    {
        int oldTargetPlatform = getTargetPlatform();
        super.setTargetPlatform(targetPlatform);
        if(oldTargetPlatform != targetPlatform) {
            if(hasChildren()) {
                INSISInstallElement[] children = getChildren();
                for (int i = 0; i < children.length; i++) {
                    children[i].setTargetPlatform(targetPlatform);
                }
            }
        }
	}

	public abstract void setChildTypes();
}

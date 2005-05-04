/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.InstallOptionsElement;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class InstallOptionsTreeEditPart extends AbstractTreeEditPart implements PropertyChangeListener
{
    /**
     * Constructor initializes this with the given model.
     * 
     * @param model
     *            Model for this.
     */
    public InstallOptionsTreeEditPart(Object model)
    {
        super(model);
    }

    public void activate()
    {
        super.activate();
        getInstallOptionsElement().addPropertyChangeListener(this);
    }

    /**
     * Creates and installs pertinent EditPolicies for this.
     */
    protected void createEditPolicies()
    {
        EditPolicy component = new InstallOptionsEditPolicy();
        installEditPolicy(EditPolicy.COMPONENT_ROLE, component);
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new InstallOptionsTreeEditPolicy());
    }

    public void deactivate()
    {
        getInstallOptionsElement().removePropertyChangeListener(this);
        super.deactivate();
    }

    /**
     * Returns the model of this as a InstallOptionsElement.
     * 
     * @return Model of this.
     */
    protected InstallOptionsElement getInstallOptionsElement()
    {
        return (InstallOptionsElement)getModel();
    }

    /**
     * Returns <code>null</code> as a Tree EditPart holds no children under
     * it.
     * 
     * @return <code>null</code>
     */
    protected List getModelChildren()
    {
        return Collections.EMPTY_LIST;
    }

    public void propertyChange(PropertyChangeEvent change)
    {
        if (change.getPropertyName().equals(InstallOptionsDialog.PROPERTY_CHILDREN)) {
            if (change.getOldValue() instanceof Integer) {
                // new child
                addChild(createChild(change.getNewValue()), ((Integer)change.getOldValue()).intValue());
            }
            else {
                // remove child
                removeChild((EditPart)getViewer().getEditPartRegistry().get(change.getOldValue()));
            }
        }
        refreshVisuals();
    }

    /**
     * Refreshes the Widget of this based on the property given to update. All
     * major properties are updated irrespective of the property input.
     * 
     * @param property
     *            Property to be refreshed.
     */
    protected void refreshVisuals()
    {
        if (getWidget() instanceof Tree) {
            for(Iterator iter = getChildren().iterator(); iter.hasNext();) {
                ((InstallOptionsTreeEditPart)iter.next()).refreshVisuals();
            }
        }
        else {
            TreeItem item = (TreeItem)getWidget();
            InstallOptionsElement element = getInstallOptionsElement();
            Image image = element.getIcon();
            Image itemImage = item.getImage();
            if (image != itemImage) {
                if(image != null) {
                    image.setBackground(item.getParent().getBackground());
                }
                setWidgetImage(image);
            }
            String string = getInstallOptionsElement().toString();
            if(string != null && !string.equals(item.getText())) {
                setWidgetText(string);
            }
        }
    }
}
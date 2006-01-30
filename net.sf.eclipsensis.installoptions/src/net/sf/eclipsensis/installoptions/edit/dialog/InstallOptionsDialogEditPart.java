/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.model.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.tools.DeselectAllTracker;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.accessibility.AccessibleEvent;

public class InstallOptionsDialogEditPart extends InstallOptionsEditPart implements LayerConstants, IInstallOptionsConstants
{
    private FreeformLayout mLayout = new FreeformLayout();
    private PropertyChangeListener mPropertyChangeListener = new PropertyChangeListener(){
        public void propertyChange(PropertyChangeEvent event)
        {
            String property = event.getPropertyName();
            if(property.equals(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE)) {
                DialogSize d = (DialogSize)event.getNewValue();
                getInstallOptionsDialog().setDialogSize(d);
                InstallOptionsDialogLayer fig = (InstallOptionsDialogLayer)getFigure();
                if(fig != null) {
                    fig.setDialogSize(d.getSize());
                }
                List children = getChildren();
                for (Iterator iter = children.iterator(); iter.hasNext();) {
                    InstallOptionsWidgetEditPart element = (InstallOptionsWidgetEditPart)iter.next();
                    IFigure figure = element.getFigure();
                    if(figure != null) {
                        InstallOptionsWidget widget = element.getInstallOptionsWidget();
                        Rectangle bounds = widget.toGraphical(widget.getPosition(),d.getSize()).getBounds();
                        setLayoutConstraint(element, figure, bounds);
                    }
                }
            }
            else if(property.equals(IInstallOptionsConstants.PROPERTY_SHOW_DIALOG_SIZE)) {
                Boolean d = (Boolean)event.getNewValue();
                getInstallOptionsDialog().setShowDialogSize(d.booleanValue());
                InstallOptionsDialogLayer fig = (InstallOptionsDialogLayer)getFigure();
                if(fig != null) {
                    fig.setShowDialogSize(d.booleanValue());
                }
            }
        }
    };

    protected InstallOptionsDialog getInstallOptionsDialog()
    {
        return (InstallOptionsDialog)getModel();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if (InstallOptionsDialog.PROPERTY_SELECTION.equals(prop)) {
            List modelSelection = (List)evt.getNewValue();
            List selection = new ArrayList();
            for (Iterator iter = modelSelection.iterator(); iter.hasNext();) {
                InstallOptionsWidget element = (InstallOptionsWidget)iter.next();
                selection.add(getViewer().getEditPartRegistry().get(element));
            }
            getViewer().setSelection(new StructuredSelection(selection));
        }
        else if (InstallOptionsModel.PROPERTY_RTL.equals(prop)) {
            refreshDiagram();
        }
        else if (InstallOptionsModel.PROPERTY_CHILDREN.equals(prop)) {
            //This bit is in here to correct z-ordering of children.
            List modelChildren = getModelChildren();
            List children = getChildren();
            int n = Math.min(modelChildren.size(), children.size());
            int i=0;
            for(; i<n; i++) {
                Object model = modelChildren.get(i);
                EditPart part = (EditPart)children.get(i);
                if(model != part.getModel()) {
                    break;
                }
            }
            refreshChildren();
            if(i < n) {
                for(int j=i; j<children.size(); j++) {
                    GraphicalEditPart part = (GraphicalEditPart)children.get(j);
                    IFigure fig = part.getFigure();
                    LayoutManager layout = getContentPane().getLayoutManager();
                    Object constraint = null;
                    if (layout != null) {
                        constraint = layout.getConstraint(fig);
                    }
                    getContentPane().remove(fig);
                    getContentPane().add(fig);
                    setLayoutConstraint(part, fig, constraint);
                }
            }
        }
    }

    /**
     * Returns the Children of this through the model.
     *
     * @return Children of this as a List.
     */
    protected List getModelChildren()
    {
        List list = new ArrayList(getInstallOptionsDialog().getChildren());
        Collections.reverse(list);
        return list;
    }

    protected AccessibleEditPart createAccessible()
    {
        return new AccessibleGraphicalEditPart() {
            public void getName(AccessibleEvent e)
            {
                e.result = InstallOptionsPlugin.getResourceString("install.options.dialog.name"); //$NON-NLS-1$
            }
        };
    }

    /**
     * Installs EditPolicies specific to this.
     */
    protected void createEditPolicies()
    {
        super.createEditPolicies();

        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.COMPONENT_ROLE,
                new RootComponentEditPolicy());
        installEditPolicy(EditPolicy.CONTAINER_ROLE, new InstallOptionsDialogEditPolicy());
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new InstallOptionsXYLayoutEditPolicy((XYLayout)getContentPane().getLayoutManager()));

        installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
    }

    public void refreshDiagram()
    {
        InstallOptionsWidgetEditPart child;

        for (Iterator iter = getChildren().iterator(); iter.hasNext(); ) {
            child = (InstallOptionsWidgetEditPart)iter.next();
            ((IInstallOptionsFigure)child.getFigure()).refresh();
        }
    }

    /**
     * Returns a Figure to represent this.
     *
     * @return Figure.
     */
    protected IFigure createFigure()
    {
        InstallOptionsDialogLayer f = new InstallOptionsDialogLayer();
        f.setDialogSize(getInstallOptionsDialog().getDialogSize().getSize());
        f.setShowDialogSize(getInstallOptionsDialog().isShowDialogSize());
        f.setLayoutManager(mLayout);
        f.setBorder(new MarginBorder(5));
        return f;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter)
    {
        if (adapter == SnapToHelper.class) {
            List snapStrategies = new ArrayList();

            Boolean val = (Boolean)getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
            if (val != null && val.booleanValue()) {
                val = (Boolean)getViewer().getProperty(PROPERTY_SNAP_TO_GUIDES);
                if (val != null && val.booleanValue()) {
                    snapStrategies.add(new SnapToGuides(this));
                }
            }

            val = (Boolean)getViewer().getProperty(SnapToGeometry.PROPERTY_SNAP_ENABLED);
            if (val != null && val.booleanValue()) {
                snapStrategies.add(new SnapToGeometry(this));
            }

            val = (Boolean)getViewer().getProperty(SnapToGrid.PROPERTY_GRID_VISIBLE);
            if (val != null && val.booleanValue()) {
                val = (Boolean)getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
                if (val != null && val.booleanValue()) {
                    snapStrategies.add(new InstallOptionsSnapToGrid(this));
                }
            }

            if (snapStrategies.size() == 0) {
                return null;
            }
            if (snapStrategies.size() == 1) {
                return snapStrategies.get(0);
            }

            SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++) {
                ss[i] = (SnapToHelper)snapStrategies.get(i);
            }
            return new CompoundSnapToHelper(ss);
        }
        return super.getAdapter(adapter);
    }

    public void addNotify()
    {
        super.addNotify();
        getViewer().setProperty(IInstallOptionsConstants.PROPERTY_DIALOG_SIZE,
                    getInstallOptionsDialog().getDialogSize().getCopy());
        getViewer().setProperty(IInstallOptionsConstants.PROPERTY_SHOW_DIALOG_SIZE,
                    Boolean.valueOf(getInstallOptionsDialog().isShowDialogSize()));
        getViewer().addPropertyChangeListener(mPropertyChangeListener);
    }

    public void removeNotify()
    {
        getViewer().removePropertyChangeListener(mPropertyChangeListener);
        super.removeNotify();
    }

    public DragTracker getDragTracker(Request req)
    {
        if (req instanceof SelectionRequest
                && ((SelectionRequest)req).getLastButtonPressed() == 3) {
            return new DeselectAllTracker(this);
        }
        return new MarqueeDragTracker();
    }

    protected void refreshVisuals()
    {
        getFigure().setLayoutManager(mLayout);
        List children = getChildren();
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            EditPart part = (EditPart)iter.next();
            part.refresh();
        }
    }

    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("install.options.dialog.name"); //$NON-NLS-1$
    }
}
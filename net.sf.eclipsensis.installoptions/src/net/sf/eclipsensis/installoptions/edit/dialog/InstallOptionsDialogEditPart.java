/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.beans.PropertyChangeEvent;
import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.*;
import net.sf.eclipsensis.installoptions.figures.ComboboxFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsDialog;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.draw2d.*;
import org.eclipse.gef.*;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.SnapFeedbackPolicy;
import org.eclipse.gef.requests.SelectionRequest;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.tools.DeselectAllTracker;
import org.eclipse.gef.tools.MarqueeDragTracker;
import org.eclipse.swt.accessibility.AccessibleEvent;

public class InstallOptionsDialogEditPart extends InstallOptionsEditPart implements LayerConstants, IInstallOptionsConstants
{
    protected InstallOptionsDialog getInstallOptionsDialog()
    {
        return (InstallOptionsDialog)getModel();
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if (InstallOptionsModel.PROPERTY_CHILDREN.equals(prop)) {
            //This bit is in here to correct z-ordering of children.
            List modelChildren = getModelChildren();
            List children = getChildren();
            HashSet oldChildren = new HashSet(children);
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
            //This is a stupid hack for Combobox figures
            UpdateManager updateManager = ((FigureCanvas)getViewer().getControl()).getLightweightSystem().getUpdateManager();
            for(int j=0; j<children.size(); j++) {
                GraphicalEditPart part = (GraphicalEditPart)children.get(j);
                IFigure fig = part.getFigure();
                if(fig instanceof ComboboxFigure && !oldChildren.contains(part)) {
                    updateManager.performUpdate(fig.getBounds());
                }
            }
        }
        else if (InstallOptionsModel.PROPERTY_SIZE.equals(prop)) {
            refreshVisuals();
        }
    }

    /**
     * Returns the Children of this through the model.
     * 
     * @return Children of this as a List.
     */
    protected List getModelChildren()
    {
        return getInstallOptionsDialog().getChildren();
    }

    private FreeformLayout mLayout = new FreeformLayout();

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
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new InstallOptionsXYLayoutEditPolicy(
                (XYLayout)getContentPane().getLayoutManager()));

        installEditPolicy("Snap Feedback", new SnapFeedbackPolicy()); //$NON-NLS-1$
    }

    /**
     * Returns a Figure to represent this.
     * 
     * @return Figure.
     */
    protected IFigure createFigure()
    {
        InstallOptionsDialogLayer f = new InstallOptionsDialogLayer();
        f.setLayoutManager(mLayout);
        f.setBorder(new MarginBorder(5));
        return f;
    }

    public void activate()
    {
        super.activate();
        ((InstallOptionsDialogLayer)getFigure()).setInstallOptionsDialog((InstallOptionsDialog)getModel());
    }

    public void deactivate()
    {
        ((InstallOptionsDialogLayer)getFigure()).setInstallOptionsDialog(null);
        super.deactivate();
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
                return (SnapToHelper)snapStrategies.get(0);
            }

            SnapToHelper ss[] = new SnapToHelper[snapStrategies.size()];
            for (int i = 0; i < snapStrategies.size(); i++) {
                ss[i] = (SnapToHelper)snapStrategies.get(i);
            }
            return new CompoundSnapToHelper(ss);
        }
        return super.getAdapter(adapter);
    }

    public DragTracker getDragTracker(Request req)
    {
        if (req instanceof SelectionRequest
                && ((SelectionRequest)req).getLastButtonPressed() == 3)
            return new DeselectAllTracker(this);
        return new MarqueeDragTracker();
    }

    /**
     * Returns <code>NULL</code> as it does not hold any connections.
     * 
     * @return ConnectionAnchor
     */
    public ConnectionAnchor getSourceConnectionAnchor(
            ConnectionEditPart editPart)
    {
        return null;
    }

    /**
     * Returns <code>NULL</code> as it does not hold any connections.
     * 
     * @return ConnectionAnchor
     */
    public ConnectionAnchor getSourceConnectionAnchor(int x, int y)
    {
        return null;
    }

    /**
     * Returns <code>NULL</code> as it does not hold any connections.
     * 
     * @return ConnectionAnchor
     */
    public ConnectionAnchor getTargetConnectionAnchor(
            ConnectionEditPart editPart)
    {
        return null;
    }

    /**
     * Returns <code>NULL</code> as it does not hold any connections.
     * 
     * @return ConnectionAnchor
     */
    public ConnectionAnchor getTargetConnectionAnchor(int x, int y)
    {
        return null;
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
}
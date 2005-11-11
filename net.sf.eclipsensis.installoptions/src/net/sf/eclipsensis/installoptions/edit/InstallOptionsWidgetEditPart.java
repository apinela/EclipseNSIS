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
import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.requests.ExtendedEditRequest;
import net.sf.eclipsensis.installoptions.requests.ReorderPartRequest;
import net.sf.eclipsensis.util.Common;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.tools.*;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Display;

public abstract class InstallOptionsWidgetEditPart extends InstallOptionsEditPart implements IDirectEditLabelProvider, IExtendedEditLabelProvider
{
    private DirectEditManager mManager;
    private String mDirectEditLabel;
    private String mExtendedEditLabel;
    private boolean mNeedsRefresh = false;
    private Label mToolTip;

    private Label getToolTip()
    {
        if(mToolTip == null) {
            mToolTip = new Label();
            mToolTip.setBorder(new MarginBorder(1,2,1,2));
            mToolTip.setOpaque(true);
            resetToolTipText();
        }
        return mToolTip;
    }

    protected void resetToolTipText()
    {
        if(mToolTip != null) {
            mToolTip.setText(getTypeName());
            mToolTip.setBackgroundColor(ColorConstants.tooltipBackground);
            mToolTip.setForegroundColor(ColorConstants.tooltipForeground);
            Dimension dim = FigureUtilities.getStringExtents(mToolTip.getText(),Display.getDefault().getSystemFont());
            dim.expand(8,6);
            mToolTip.setSize(dim);
        }
    }

    public InstallOptionsWidgetEditPart()
    {
        super();
        setDirectEditLabel(InstallOptionsPlugin.getResourceString(getDirectEditLabelProperty()));
        setExtendedEditLabel(InstallOptionsPlugin.getResourceString(getExtendedEditLabelProperty()));
    }

    public DragTracker getDragTracker(Request req)
    {
        return new InstallOptionsDragEditPartsTracker(this);
    }

    protected String getDirectEditLabelProperty()
    {
        return "direct.edit.label"; //$NON-NLS-1$
    }

    protected String getExtendedEditLabelProperty()
    {
        return ""; //$NON-NLS-1$
    }

    public Object getAdapter(Class key)
    {
        if(IDirectEditLabelProvider.class.equals(key)) {
            if(!Common.isEmpty(getDirectEditLabel())) {
                return this;
            }
        }
        else if(IExtendedEditLabelProvider.class.equals(key)) {
            if(!Common.isEmpty(getExtendedEditLabel())) {
                return this;
            }
        }
        return super.getAdapter(key);
    }

    public String getDirectEditLabel()
    {
        return mDirectEditLabel;
    }

    public void setDirectEditLabel(String directEditLabel)
    {
        mDirectEditLabel = directEditLabel;
    }

    public String getExtendedEditLabel()
    {
        return mExtendedEditLabel;
    }

    public void setExtendedEditLabel(String extendedEditLabel)
    {
        mExtendedEditLabel = extendedEditLabel;
    }

    protected final AccessibleEditPart createAccessible() {
        return new AccessibleGraphicalEditPart(){
            public void getValue(AccessibleControlEvent e) {
                e.result = getAccessibleControlEventResult();
            }

            public void getName(AccessibleEvent e) {
                e.result = getTypeName();
            }
        };
    }

    public boolean isNeedsRefresh()
    {
        return mNeedsRefresh;
    }

    public void setNeedsRefresh(boolean needsRefresh)
    {
        mNeedsRefresh = needsRefresh;
    }

    public final void propertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if (InstallOptionsModel.PROPERTY_POSITION.equals(prop)) {
            refreshVisuals();
        }
        else if (InstallOptionsModel.PROPERTY_INDEX.equals(prop)) {
            Command command = getParent().getCommand(new ReorderPartRequest(this,((Integer)evt.getNewValue()).intValue()));
            if(command != null) {
                getViewer().getEditDomain().getCommandStack().execute(command);
            }
        }
        else {
            doPropertyChange(evt);
            synchronized(this) {
                if(isNeedsRefresh()) {
                    IInstallOptionsFigure figure = (IInstallOptionsFigure)getFigure();
                    figure.refresh();
                    setNeedsRefresh(false);
                }
            }
        }
    }

    protected void doPropertyChange(PropertyChangeEvent evt)
    {
        String prop = evt.getPropertyName();
        if(InstallOptionsModel.PROPERTY_FLAGS.equals(prop)) {
            List oldFlags = new ArrayList((List)evt.getOldValue());
            oldFlags.removeAll((List)evt.getNewValue());
            for (Iterator iter = oldFlags.iterator(); iter.hasNext();) {
                handleFlagRemoved((String)iter.next());
            }
            List newFlags = new ArrayList((List)evt.getNewValue());
            newFlags.removeAll((List)evt.getOldValue());
            for (Iterator iter = newFlags.iterator(); iter.hasNext();) {
                handleFlagAdded((String)iter.next());
            }
        }
    }

    protected void handleFlagRemoved(String flag)
    {
        if(flag.equals(InstallOptionsModel.FLAGS_DISABLED)) {
            ((IInstallOptionsFigure)getFigure()).setDisabled(false);
            setNeedsRefresh(true);
        }
        else if(supportsScrolling()) {
            if(flag.equals(InstallOptionsModel.FLAGS_HSCROLL)) {
                ((IInstallOptionsFigure)getFigure()).setHScroll(false);
                setNeedsRefresh(true);
            }
            else if(flag.equals(InstallOptionsModel.FLAGS_VSCROLL)) {
                ((IInstallOptionsFigure)getFigure()).setVScroll(false);
                setNeedsRefresh(true);
            }
        }
    }

    protected void handleFlagAdded(String flag)
    {
        if(flag.equals(InstallOptionsModel.FLAGS_DISABLED)) {
            ((IInstallOptionsFigure)getFigure()).setDisabled(true);
            setNeedsRefresh(true);
        }
        else if(supportsScrolling()) {
            if(flag.equals(InstallOptionsModel.FLAGS_HSCROLL)) {
                ((IInstallOptionsFigure)getFigure()).setHScroll(true);
                setNeedsRefresh(true);
            }
            else if(flag.equals(InstallOptionsModel.FLAGS_VSCROLL)) {
                ((IInstallOptionsFigure)getFigure()).setVScroll(true);
                setNeedsRefresh(true);
            }
        }
    }

    protected final IFigure createFigure()
    {
        IInstallOptionsFigure figure2 = createInstallOptionsFigure();
        figure2.setFocusTraversable(true);
        figure2.setToolTip(getToolTip());
        return figure2;
    }

    /**
     * Updates the visual aspect of this.
     */
    protected void refreshVisuals()
    {
        InstallOptionsWidget widget = (InstallOptionsWidget)getInstallOptionsElement();
        Position pos = widget.getPosition();
        pos = widget.toGraphical(pos);
        Rectangle r = new Rectangle(pos.left,pos.top,(pos.right-pos.left)+1,(pos.bottom-pos.top)+1);

        ((GraphicalEditPart)getParent()).setLayoutConstraint(this, getFigure(), r);
    }

    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, new InstallOptionsSelectionEditPolicy(this));
    }


    public void performRequest(Request request)
    {
        if(request.getType().equals(IInstallOptionsConstants.REQ_EXTENDED_EDIT)) {
            performExtendedEdit(request);
        }
        else if(request.getType().equals(RequestConstants.REQ_OPEN)) {
            performExtendedEdit(new ExtendedEditRequest(this));
        }
        else if (request.getType() == RequestConstants.REQ_DIRECT_EDIT) {
            performDirectEdit();
        }
        super.performRequest(request);
    }

    protected void performExtendedEdit(Request request)
    {
        InstallOptionsExtendedEditPolicy policy = (InstallOptionsExtendedEditPolicy)getEditPolicy(InstallOptionsExtendedEditPolicy.ROLE);
        if(policy != null) {
            IExtendedEditSupport support = (IExtendedEditSupport)getAdapter(IExtendedEditSupport.class);
            if(support != null) {
                if(support.performExtendedEdit()) {
                    ((ExtendedEditRequest)request).setNewValue(support.getNewValue());
                    Command command = policy.getCommand(request);
                    if(command != null) {
                        getViewer().getEditDomain().getCommandStack().execute(command);
                    }
                }
            }
        }
    }

    protected void performDirectEdit()
    {
        if(mManager == null) {
            mManager = creatDirectEditManager(this, getCellEditorClass(),
                            createCellEditorLocator((IInstallOptionsFigure)getFigure()));
        }
        if(mManager != null) {
            mManager.show();
        }
    }

    protected Class getCellEditorClass()
    {
        return TextCellEditor.class;
    }

    public InstallOptionsWidget getInstallOptionsWidget()
    {
        return (InstallOptionsWidget)getModel();
    }

    public void addNotify()
    {
        super.addNotify();
    }

    public void removeNotify()
    {
        super.removeNotify();
    }

    protected boolean supportsScrolling()
    {
        return true;
    }

    private static class InstallOptionsDragEditPartsTracker extends DragEditPartsTracker
    {
        public InstallOptionsDragEditPartsTracker(EditPart sourceEditPart)
        {
            super(sourceEditPart);
        }

        protected boolean handleDragStarted()
        {
            List list = getSourceEditPart().getViewer().getSelectedEditParts();
            if(!Common.isEmptyCollection(list)) {
                for (Iterator iter = list.iterator(); iter.hasNext();) {
                    EditPart part = (EditPart)iter.next();
                    if(part instanceof InstallOptionsWidgetEditPart) {
                        if(((InstallOptionsWidget)part.getModel()).isLocked()) {
                            return false;
                        }
                    }
                }
            }
            return super.handleDragStarted();
        }
    }

    protected abstract DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator);
    protected abstract CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure);
    protected abstract IInstallOptionsFigure createInstallOptionsFigure();
    protected abstract String getAccessibleControlEventResult();
}

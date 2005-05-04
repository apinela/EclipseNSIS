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

import java.util.Iterator;
import java.util.List;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.*;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.rulers.RulerProvider;

public class InstallOptionsXYLayoutEditPolicy extends XYLayoutEditPolicy implements IInstallOptionsConstants
{

    public InstallOptionsXYLayoutEditPolicy(XYLayout layout)
    {
        super();
        setXyLayout(layout);
    }

    protected Command chainGuideAttachmentCommand(Request request,
            InstallOptionsWidget part, Command cmd, boolean horizontal)
    {
        Command result = cmd;

        // Attach to guide, if one is given
        Integer guidePos = (Integer)request.getExtendedData().get(
                horizontal?SnapToGuides.KEY_HORIZONTAL_GUIDE
                        :SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos != null) {
            int alignment = ((Integer)request.getExtendedData().get(
                    horizontal?SnapToGuides.KEY_HORIZONTAL_ANCHOR
                            :SnapToGuides.KEY_VERTICAL_ANCHOR)).intValue();
            ChangeGuideCommand cgm = new ChangeGuideCommand(part, horizontal);
            cgm.setNewGuide(findGuideAt(guidePos.intValue(), horizontal),
                    alignment);
            result = result.chain(cgm);
        }

        return result;
    }

    protected Command chainGuideDetachmentCommand(Request request,
            InstallOptionsWidget part, Command cmd, boolean horizontal)
    {
        Command result = cmd;

        // Detach from guide, if none is given
        Integer guidePos = (Integer)request.getExtendedData().get(
                horizontal?SnapToGuides.KEY_HORIZONTAL_GUIDE
                        :SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos == null)
            result = result.chain(new ChangeGuideCommand(part, horizontal));

        return result;
    }

    protected Command createAddCommand(EditPart child, Object constraint)
    {
        return null;
    }

    protected Command createAddCommand(Request request, EditPart childEditPart, Object constraint)
    {
        InstallOptionsWidget part = (InstallOptionsWidget)childEditPart.getModel();
        Position pos = (Position)constraint;

        AddCommand add = new AddCommand();
        add.setParent((InstallOptionsDialog)getHost().getModel());
        add.setChild(part);
        add.setLabel(InstallOptionsPlugin.getResourceString("add.command.label")); //$NON-NLS-1$
        add.setDebugLabel("InstallOptionsXYEP add subpart");//$NON-NLS-1$

        SetConstraintCommand setConstraint = new SetConstraintCommand();
        setConstraint.setPosition(pos);
        setConstraint.setPart(part);
        setConstraint.setLabel(InstallOptionsPlugin.getResourceString("set.constaint.command.label")); //$NON-NLS-1$
        setConstraint.setDebugLabel("InstallOptionsXYEP setConstraint");//$NON-NLS-1$

        Command cmd = add.chain(setConstraint);
        cmd = chainGuideAttachmentCommand(request, part, cmd, true);
        cmd = chainGuideAttachmentCommand(request, part, cmd, false);
        cmd = chainGuideDetachmentCommand(request, part, cmd, true);
        return chainGuideDetachmentCommand(request, part, cmd, false);
    }

    /**
     * @see org.eclipse.gef.editpolicies.ConstrainedLayoutEditPolicy#createChangeConstraintCommand(org.eclipse.gef.EditPart,
     *      java.lang.Object)
     */
    protected Command createChangeConstraintCommand(EditPart child,
            Object constraint)
    {
        return null;
    }

    protected Command createChangeConstraintCommand(ChangeBoundsRequest request, EditPart child, Object constraint)
    {
        SetConstraintCommand cmd = new SetConstraintCommand();
        InstallOptionsWidget part = (InstallOptionsWidget)child.getModel();
        cmd.setPart(part);
        cmd.setPosition((Position)constraint);
        Command result = cmd;

        Boolean val = (Boolean)child.getViewer().getProperty(RulerProvider.PROPERTY_RULER_VISIBILITY);
        if (val != null && val.booleanValue()) {
            val = (Boolean)child.getViewer().getProperty(PROPERTY_SNAP_TO_GUIDES);
            if (val != null && val.booleanValue()) {
                val = (Boolean)child.getViewer().getProperty(PROPERTY_GLUE_TO_GUIDES);
                if (val != null && val.booleanValue()) {
                    if ((request.getResizeDirection() & PositionConstants.NORTH_SOUTH) != 0) {
                        Integer guidePos = (Integer)request.getExtendedData().get(
                                SnapToGuides.KEY_HORIZONTAL_GUIDE);
                        if (guidePos != null) {
                            result = chainGuideAttachmentCommand(request, part, result,
                                    true);
                        }
                        else if (part.getHorizontalGuide() != null) {
                            // SnapToGuides didn't provide a horizontal guide, but this part
                            // is attached
                            // to a horizontal guide. Now we check to see if the part is
                            // attached to
                            // the guide along the edge being resized. If that is the case,
                            // we need to
                            // detach the part from the guide; otherwise, we leave it alone.
                            int alignment = part.getHorizontalGuide().getAlignment(part);
                            int edgeBeingResized = 0;
                            if ((request.getResizeDirection() & PositionConstants.NORTH) != 0)
                                edgeBeingResized = -1;
                            else
                                edgeBeingResized = 1;
                            if (alignment == edgeBeingResized)
                                result = result.chain(new ChangeGuideCommand(part, true));
                        }
                    }
            
                    if ((request.getResizeDirection() & PositionConstants.EAST_WEST) != 0) {
                        Integer guidePos = (Integer)request.getExtendedData().get(
                                SnapToGuides.KEY_VERTICAL_GUIDE);
                        if (guidePos != null) {
                            result = chainGuideAttachmentCommand(request, part, result,
                                    false);
                        }
                        else if (part.getVerticalGuide() != null) {
                            int alignment = part.getVerticalGuide().getAlignment(part);
                            int edgeBeingResized = 0;
                            if ((request.getResizeDirection() & PositionConstants.WEST) != 0)
                                edgeBeingResized = -1;
                            else
                                edgeBeingResized = 1;
                            if (alignment == edgeBeingResized)
                                result = result.chain(new ChangeGuideCommand(part, false));
                        }
                    }
            
                    if (request.getType().equals(REQ_MOVE_CHILDREN)
                            || request.getType().equals(REQ_ALIGN_CHILDREN)) {
                        result = chainGuideAttachmentCommand(request, part, result, true);
                        result = chainGuideAttachmentCommand(request, part, result, false);
                        result = chainGuideDetachmentCommand(request, part, result, true);
                        result = chainGuideDetachmentCommand(request, part, result, false);
                    }
                }
            }
        }
        return result;
    }

    protected EditPolicy createChildEditPolicy(EditPart child)
    {
        return new InstallOptionsResizableEditPolicy();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#createSizeOnDropFeedback(org.eclipse.gef.requests.CreateRequest)
     */
    protected IFigure createSizeOnDropFeedback(CreateRequest createRequest)
    {
        IFigure figure;

        figure = new RectangleFigure();
        ((RectangleFigure)figure).setXOR(true);
        ((RectangleFigure)figure).setFill(true);
        figure.setBackgroundColor(IInstallOptionsConstants.GHOST_FILL_COLOR);
        figure.setForegroundColor(ColorConstants.white);

        addFeedback(figure);

        return figure;
    }

    protected InstallOptionsGuide findGuideAt(int pos, boolean horizontal)
    {
        RulerProvider provider = ((RulerProvider)getHost().getViewer()
                .getProperty(
                        horizontal?RulerProvider.PROPERTY_VERTICAL_RULER
                                :RulerProvider.PROPERTY_HORIZONTAL_RULER));
        return (InstallOptionsGuide)provider.getGuideAt(pos);
    }

    protected Command getAddCommand(Request generic)
    {
        ChangeBoundsRequest request = (ChangeBoundsRequest)generic;
        List editParts = request.getEditParts();
        CompoundCommand command = new CompoundCommand();
        command.setDebugLabel("Add in ConstrainedLayoutEditPolicy");//$NON-NLS-1$
        GraphicalEditPart childPart;
        Rectangle r;
        Object constraint;

        for (int i = 0; i < editParts.size(); i++) {
            childPart = (GraphicalEditPart)editParts.get(i);
            r = childPart.getFigure().getBounds().getCopy();
            //convert r to absolute from childpart figure
            childPart.getFigure().translateToAbsolute(r);
            r = request.getTransformedRectangle(r);
            //convert this figure to relative
            getLayoutContainer().translateToRelative(r);
            getLayoutContainer().translateFromParent(r);
            r.translate(getLayoutOrigin().getNegated());
            constraint = getConstraintFor(r);
            command.add(createAddCommand(generic, childPart, translateToModelConstraint(constraint)));
        }
        return command.unwrap();
    }

    protected Object translateToModelConstraint(Object figureConstraint)
    {
        Rectangle r= (Rectangle)figureConstraint;
        Position p = new Position(r.x,r.y,r.x+r.width-1,r.y+r.height-1);
        return p;
    }

    /**
     * Override to return the <code>Command</code> to perform an {@link
     * RequestConstants#REQ_CLONE CLONE}. By default, <code>null</code> is
     * returned.
     * 
     * @param request
     *            the Clone Request
     * @return A command to perform the Clone.
     */
    protected Command getCloneCommand(ChangeBoundsRequest request)
    {
        CloneCommand clone = new CloneCommand();

        clone.setParent((InstallOptionsDialog)getHost().getModel());

        Iterator i = request.getEditParts().iterator();
        GraphicalEditPart currPart = null;

        while (i.hasNext()) {
            currPart = (GraphicalEditPart)i.next();
            clone.addPart((InstallOptionsWidget)currPart.getModel(),
                    (Rectangle)getConstraintForClone(currPart, request));
        }

        // Attach to horizontal guide, if one is given
        Integer guidePos = (Integer)request.getExtendedData().get(
                SnapToGuides.KEY_HORIZONTAL_GUIDE);
        if (guidePos != null) {
            int hAlignment = ((Integer)request.getExtendedData().get(
                    SnapToGuides.KEY_HORIZONTAL_ANCHOR)).intValue();
            clone.setGuide(findGuideAt(guidePos.intValue(), true), hAlignment,
                    true);
        }

        // Attach to vertical guide, if one is given
        guidePos = (Integer)request.getExtendedData().get(
                SnapToGuides.KEY_VERTICAL_GUIDE);
        if (guidePos != null) {
            int vAlignment = ((Integer)request.getExtendedData().get(
                    SnapToGuides.KEY_VERTICAL_ANCHOR)).intValue();
            clone.setGuide(findGuideAt(guidePos.intValue(), false), vAlignment,
                    false);
        }

        return clone;
    }

    protected Command getCreateCommand(CreateRequest request)
    {
        CreateCommand create = new CreateCommand();
        create.setParent((InstallOptionsDialog)getHost().getModel());
        InstallOptionsWidget newPart = (InstallOptionsWidget)request
                .getNewObject();
        create.setChild(newPart);
        Rectangle constraint = (Rectangle)getConstraintFor(request);
        create.setLocation(constraint);
        create.setLabel(InstallOptionsPlugin.getResourceString("create.command.label")); //$NON-NLS-1$

        Command cmd = chainGuideAttachmentCommand(request, newPart, create,
                true);
        return chainGuideAttachmentCommand(request, newPart, cmd, false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editpolicies.LayoutEditPolicy#getCreationFeedbackOffset(org.eclipse.gef.requests.CreateRequest)
     */
    protected Insets getCreationFeedbackOffset(CreateRequest request)
    {
        return new Insets();
    }

    protected Command getDeleteDependantCommand(Request request)
    {
        return null;
    }

    /**
     * Returns the layer used for displaying feedback.
     *  
     * @return the feedback layer
     */
    protected IFigure getFeedbackLayer()
    {
        return getLayer(LayerConstants.SCALED_FEEDBACK_LAYER);
    }

    protected Command getOrphanChildrenCommand(Request request)
    {
        return null;
    }

}
/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.unknown;

import java.beans.PropertyChangeEvent;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsUnknown;
import net.sf.eclipsensis.installoptions.model.commands.InstallOptionsDirectEditCommand;
import net.sf.eclipsensis.installoptions.util.FontUtility;
import net.sf.eclipsensis.util.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.DirectEditPolicy;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertySource;

public class InstallOptionsUnknownEditPart extends InstallOptionsWidgetEditPart
{
    protected static boolean cIsNT = "Windows NT".equals(System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$

    protected String getAccessibleControlEventResult()
    {
        return getInstallOptionsUnknown().getType();
    }

    protected InstallOptionsUnknown getInstallOptionsUnknown()
    {
        return (InstallOptionsUnknown)getModel();
    }

    protected void createEditPolicies()
    {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, new DirectEditPolicy(){
            protected Command getDirectEditCommand(DirectEditRequest request)
            {
                String type = (String)request.getCellEditor().getValue();
                Command command = new InstallOptionsDirectEditCommand(getInstallOptionsUnknown(),InstallOptionsModel.PROPERTY_TYPE,
                                                                     (type==null?"":type)); //$NON-NLS-1$
                command.setLabel(InstallOptionsPlugin.getResourceString("unknown.command.label"));  //$NON-NLS-1$
                return command;
            }

            protected void showCurrentEditValue(DirectEditRequest request)
            {
            }
        });
    }

    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsUnknownEditManager(part, clasz, locator);
    }

    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new UnknownCellEditorLocator((IUnknownFigure)figure);
    }

    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        if(cIsNT) {
            //This is a hack because Windows NT Labels don't seem to respond to the
            //WM_PRINT message (see SWTControl.getImage(Control)
            //XXX Remove once the cause (and fix) is known.
            return new NTUnknownFigure(getInstallOptionsWidget());
        }
        else {
            return new UnknownFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        }
    }

    public void doPropertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equalsIgnoreCase(InstallOptionsModel.PROPERTY_TYPE)) {
            IUnknownFigure figure2 = (IUnknownFigure)getFigure();
            figure2.setType((String)evt.getNewValue());
            resetToolTipText();
            setNeedsRefresh(true);
        }
        else {
            super.doPropertyChange(evt);
        }
    }

    protected String getDirectEditLabelProperty()
    {
        return "unknown.direct.edit.label"; //$NON-NLS-1$
    }

    protected String getTypeName()
    {
        String type = getInstallOptionsWidget().getType();
        return (Common.isEmpty(type)?InstallOptionsPlugin.getResourceString("unknown.type.name"):type); //$NON-NLS-1$
    }

    public static interface IUnknownFigure extends IInstallOptionsFigure
    {
        public void setType(String type);
    }

    //This is a hack because Windows NT Labels don't seem to respond to the
    //WM_PRINT message (see SWTControl.getImage(Control)
    //XXX Remove once the cause (and fix) is known.
    private static class NTUnknownFigure extends NTFigure implements IUnknownFigure
    {
        private String mType;
        private Figure mFigure;

        public NTUnknownFigure(IPropertySource propertySource)
        {
            super(propertySource);
        }

        protected void init(IPropertySource propertySource)
        {
            super.init(propertySource);
            setType((String)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TYPE));
        }

        protected void createChildFigures()
        {
            mFigure = new Figure() {
                public void paintClientArea(Graphics graphics)
                {
                    graphics.pushState();

                    super.paintClientArea(graphics);
                    String text = getType();
                    Rectangle bounds = getBounds();
                    Dimension d = FigureUtilities.getTextExtents(text,graphics.getFont());
                    int x = (bounds.width-d.width)/2;
                    int y = (bounds.height-d.height)/2;

                    graphics.translate(bounds.x, bounds.y);
                    if (isDisabled()) {
                        graphics.translate(1, 1);
                        graphics.setForegroundColor(ColorManager.getSystemColor(WinAPI.COLOR_3DHILIGHT));
                        graphics.setFont(FontUtility.getInstallOptionsFont());
                        graphics.drawText(text, x, y);
                        graphics.translate(-1, -1);
                        graphics.setForegroundColor(ColorManager.getSystemColor(WinAPI.COLOR_GRAYTEXT));
                    }
                    else {
                        graphics.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
                    }
                    graphics.drawText(text,x,y);
                    graphics.translate(-bounds.x, -bounds.y);

                    graphics.popState();
                    graphics.restoreState();
                }

                public void setBounds(Rectangle rect)
                {
                    super.setBounds(rect);
                    repaint();
                }
            };
            mFigure.setOpaque(true);
            mFigure.setBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
            mFigure.setBorder(new DashedLineBorder());
            add(mFigure);
        }

        protected void setChildConstraints(Rectangle bounds)
        {
            setConstraint(mFigure, bounds);
        }

        public String getType()
        {
            return mType==null?"":mType; //$NON-NLS-1$
        }

        public void setType(String type)
        {
            if(!Common.stringsAreEqual(mType, type)) {
                mType = type;
                refresh();
            }
        }
    }
}

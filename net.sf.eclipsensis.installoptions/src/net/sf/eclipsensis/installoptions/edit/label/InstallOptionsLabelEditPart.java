/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.label;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.UneditableElementDirectEditPolicy;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.figures.FigureUtility.NTFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.*;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.gef.requests.DirectEditRequest;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertySource;

public class InstallOptionsLabelEditPart extends InstallOptionsUneditableElementEditPart
{
    protected static boolean cIsNT = "Windows NT".equals(System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$

    protected String getDirectEditLabelProperty()
    {
        return "label.direct.edit.label"; //$NON-NLS-1$
    }

    protected IInstallOptionsFigure createInstallOptionsFigure()
    {
        if(cIsNT) {
            //This is a hack because Windows NT Labels don't seem to respond to the
            //WM_PRINT message (see SWTControl.getImage(Control)
            //XXX Remove once the cause (and fix) is known.
            return new NTLabelFigure(getInstallOptionsWidget());
        }
        else {
            return new LabelFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        }
    }

    protected UneditableElementDirectEditPolicy createDirectEditPolicy()
    {
        return new UneditableElementDirectEditPolicy() {
            protected String getDirectEditValue(DirectEditRequest edit)
            {
                return (String)TypeConverter.ESCAPED_STRING_CONVERTER.asType(super.getDirectEditValue(edit));
            }
        };
    }

    /**
     * @return
     */
    protected String getTypeName()
    {
        return InstallOptionsPlugin.getResourceString("label.type.name"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart#creatDirectEditManager(net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart, java.lang.Class, org.eclipse.gef.tools.CellEditorLocator)
     */
    protected DirectEditManager creatDirectEditManager(InstallOptionsWidgetEditPart part, Class clasz, CellEditorLocator locator)
    {
        return new InstallOptionsLabelEditManager(part, clasz, locator);
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart#createCellEditorLocator(net.sf.eclipsensis.installoptions.figures.UneditableElementFigure)
     */
    protected CellEditorLocator createCellEditorLocator(IInstallOptionsFigure figure)
    {
        return new LabelCellEditorLocator((ILabelFigure)getFigure());
    }

    public static interface ILabelFigure extends IUneditableElementFigure
    {
        public boolean isMultiLine();
    }

    //This is a hack because Windows NT Labels don't seem to respond to the
    //WM_PRINT message (see SWTControl.getImage(Control)
    //XXX Remove once the cause (and fix) is known.
    protected class NTLabelFigure extends NTFigure implements ILabelFigure
    {
        protected FlowPage mFlowPage;
        protected FlowPage mShadowFlowPage;
        protected TextFlow mShadowTextFlow;
        protected TextFlow mTextFlow;
        private String mText;

        public NTLabelFigure(IPropertySource propertySource)
        {
            super(propertySource);
        }

        public boolean isMultiLine()
        {
            return true;
        }

        protected void createChildFigures()
        {
            mShadowTextFlow = new TextFlow(""); //$NON-NLS-1$
            mShadowFlowPage = new FlowPage();
            mShadowFlowPage.setVisible(false);
            mShadowFlowPage.add(mShadowTextFlow);
            add(mShadowFlowPage);

            mTextFlow = new TextFlow(""); //$NON-NLS-1$
            mFlowPage = new FlowPage();
            mFlowPage.add(mTextFlow);
            add(mFlowPage);
        }

        protected void init(IPropertySource propertySource)
        {
            super.init(propertySource);
            mTextFlow.setForegroundColor(ColorManager.getColor(getTxtColor()));
            mShadowTextFlow.setForegroundColor(ColorManager.getSystemColor(WinAPI.COLOR_3DHILIGHT));
            setText((String)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TEXT));
        }

        public String getText()
        {
            return mText==null?"":mText; //$NON-NLS-1$
        }

        public void setText(String text)
        {
            if(!Common.stringsAreEqual(mText, text)) {
                mText = text;
                refresh();
            }
        }

        public RGB getTxtColor()
        {
            return Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB();
        }

        protected void setChildConstraints(Rectangle rect)
        {
            setConstraint(mFlowPage, new Rectangle(0,0,rect.width,rect.height));
            setConstraint(mShadowFlowPage, new Rectangle(1,1,rect.width,rect.height));
        }

        public void refresh()
        {
            super.refresh();
            String text = isMultiLine()?TypeConverter.ESCAPED_STRING_CONVERTER.asString(getText()):getText();
            mTextFlow.setText(text);
            mShadowTextFlow.setText(text);
            mTextFlow.setForegroundColor((isDisabled()?ColorManager.getSystemColor(WinAPI.COLOR_GRAYTEXT):ColorManager.getColor(getTxtColor())));
            mShadowFlowPage.setVisible(isDisabled());
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.label;

import java.util.List;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.edit.InstallOptionsWidgetEditPart;
import net.sf.eclipsensis.installoptions.edit.uneditable.InstallOptionsUneditableElementEditPart;
import net.sf.eclipsensis.installoptions.figures.*;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertySource;

public class InstallOptionsLabelEditPart extends InstallOptionsUneditableElementEditPart
{
    protected static boolean cIsNT = "Windows NT".equals(System.getProperty("os.name"));
    
    protected String getDirectEditLabelProperty()
    {
        return "label.direct.edit.label"; //$NON-NLS-1$
    }
    
    protected IInstallOptionsFigure createInstallOptionsFigure() 
    {
        if(cIsNT) {
            return new NTLabelFigure(getInstallOptionsWidget());
        }
        else {
            return new LabelFigure((Composite)getViewer().getControl(), getInstallOptionsWidget());
        }
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
        return new LabelCellEditorLocator((LabelFigure)getFigure());
    }
    
    protected class NTLabelFigure extends Figure implements IUneditableElementFigure
    {
        protected FlowPage mFlowPage;
        protected FlowPage mShadowFlowPage;
        protected TextFlow mShadowTextFlow;
        protected TextFlow mTextFlow;
        private String mText;
        private boolean mDisabled;
        
        public NTLabelFigure(IPropertySource propertySource)
        {
            super();
            setOpaque(true);
            setLayoutManager(new XYLayout());
            mShadowTextFlow = new TextFlow("");
            mShadowFlowPage = new FlowPage();
            mShadowFlowPage.setVisible(false);
            mShadowFlowPage.add(mShadowTextFlow);
            add(mShadowFlowPage);
            
            mTextFlow = new TextFlow("");
            mFlowPage = new FlowPage();
            mFlowPage.add(mTextFlow);
            add(mFlowPage);
            List flags = (List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
            setDisabled(flags != null && flags.contains(InstallOptionsModel.FLAGS_DISABLED));
            setBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
            mTextFlow.setForegroundColor(ColorManager.getColor(getTxtColor()));
            mShadowTextFlow.setForegroundColor(ColorManager.getSystemColor(WinAPI.COLOR_3DHILIGHT));
            setText((String)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TEXT));
        }
        
        public RGB getTxtColor()
        {
            return Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB();
        }

        public String getText()
        {
            return mText==null?"":mText;
        }

        public void setText(String text)
        {
            mText = text;
            refresh();
        }

        public void setDisabled(boolean disabled)
        {
            mDisabled = disabled;
            refresh();
        }

        protected boolean isDisabled()
        {
            return mDisabled;
        }

        public void setBounds(Rectangle rect)
        {
            Dimension oldDim = getBounds().getSize();
            super.setBounds(rect);
            if(!rect.getSize().equals(oldDim)) {
                setConstraint(mFlowPage, new Rectangle(0,0,rect.width,rect.height));
                setConstraint(mShadowFlowPage, new Rectangle(1,1,rect.width,rect.height));
            }
        }

        public final void refresh()
        {
            mTextFlow.setText(getText());
            mShadowTextFlow.setText(getText());
            mTextFlow.setForegroundColor((isDisabled()?ColorManager.getSystemColor(WinAPI.COLOR_GRAYTEXT):ColorManager.getColor(getTxtColor())));
            mShadowFlowPage.setVisible(isDisabled());
        }
    }
}

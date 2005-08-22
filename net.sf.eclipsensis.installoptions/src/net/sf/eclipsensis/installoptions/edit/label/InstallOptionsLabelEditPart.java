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
import net.sf.eclipsensis.util.*;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.draw2d.text.FlowPage;
import org.eclipse.draw2d.text.TextFlow;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.internal.win32.OS;
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
            //This is a hack because Windows NT Labels don't seem to respond to the 
            //WM_PRINT message (see SWTControl.getImage(Control)
            //TODO Remove once the cause (and fix) is known.
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
        return new LabelCellEditorLocator((ILabelFigure)getFigure());
    }
    
    public static interface ILabelFigure extends IUneditableElementFigure
    {
//    Marker interface
    }

    //This is a hack because Windows NT Labels don't seem to respond to the 
    //WM_PRINT message (see SWTControl.getImage(Control)
    //TODO Remove once the cause (and fix) is known.
    protected abstract class NTFigure extends Figure implements ILabelFigure
    {
        private boolean mDisabled = false;
        private boolean mHScroll = false;
        private boolean mVScroll = false;
        private ScrollBar mHScrollBar;
        private ScrollBar mVScrollBar;
        private Label mGlassPanel;
        
        private Rectangle mChildBounds = new Rectangle(0,0,0,0);
        private String mText;
        
        public NTFigure(IPropertySource propertySource)
        {
            super();
            setOpaque(true);
            setLayoutManager(new XYLayout());
            mHScrollBar = new ScrollBar();
            mHScrollBar.setHorizontal(true);
            mHScrollBar.setVisible(false);
            add(mHScrollBar);
            mVScrollBar = new ScrollBar();
            mVScrollBar.setHorizontal(false);
            add(mVScrollBar);
            mGlassPanel = new Label();
            mGlassPanel.setOpaque(false);
            add(mGlassPanel);
            createChildFigures();
            init(propertySource);
        }

        protected void init(IPropertySource propertySource)
        {
            List flags = (List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS);
            setDisabled(flags != null && flags.contains(InstallOptionsModel.FLAGS_DISABLED));
            setHScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_HSCROLL));
            setVScroll(flags != null && flags.contains(InstallOptionsModel.FLAGS_VSCROLL));
            setBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS));
        }

        public void setDisabled(boolean disabled)
        {
            if(mDisabled != disabled) {
                mDisabled = disabled;
                refresh();
            }
        }

        protected boolean isDisabled()
        {
            return mDisabled;
        }

        public void setHScroll(boolean hScroll)
        {
            if(mHScroll != hScroll) {
                mHScroll = hScroll;
                refresh();
            }
        }

        public void setVScroll(boolean vScroll)
        {
            if(mVScroll != vScroll) {
                mVScroll = vScroll;
                refresh();
            }
        }

        public void refresh()
        {
            updateBounds(bounds);
            layout();
        }

        private void updateBounds(Rectangle newBounds)
        {
            Rectangle childBounds = new Rectangle(0,0,newBounds.width,newBounds.height);
            setConstraint(mGlassPanel, childBounds.getCopy());
            int hbarHeight = OS.GetSystemMetrics (OS.SM_CYHSCROLL);
            int vbarWidth = OS.GetSystemMetrics (OS.SM_CXVSCROLL);
            mHScrollBar.setVisible(mHScroll);
            if(mHScroll) {
                setConstraint(mHScrollBar, new Rectangle(0,newBounds.height-hbarHeight,
                                                        newBounds.width-(mVScroll?vbarWidth:0), hbarHeight));
                childBounds.height -= hbarHeight;
            }
            mVScrollBar.setVisible(mVScroll);
            if(mVScroll) {
                setConstraint(mVScrollBar, new Rectangle(newBounds.width-vbarWidth,0,
                                                         vbarWidth, newBounds.height-(mHScroll?hbarHeight:0)));
                childBounds.width -= vbarWidth;
            }
            if(!mChildBounds.equals(childBounds)) {
                setChildConstraints(childBounds);
                mChildBounds = childBounds;
            }
        }
        
        public void setBounds(Rectangle newBounds)
        {
            if(!bounds.getSize().equals(newBounds.getSize())) {
                updateBounds(newBounds);
            }
            super.setBounds(newBounds);
        }
        
        protected abstract void setChildConstraints(Rectangle bounds);
        protected abstract void createChildFigures();

        public String getText()
        {
            return mText==null?"":mText;
        }

        public void setText(String text)
        {
            if(!Common.stringsAreEqual(mText, text)) {
                mText = text;
                refresh();
            }
        }
    }

    //This is a hack because Windows NT Labels don't seem to respond to the 
    //WM_PRINT message (see SWTControl.getImage(Control)
    //TODO Remove once the cause (and fix) is known.
    protected class NTLabelFigure extends NTFigure
    {
        protected FlowPage mFlowPage;
        protected FlowPage mShadowFlowPage;
        protected TextFlow mShadowTextFlow;
        protected TextFlow mTextFlow;
        public NTLabelFigure(IPropertySource propertySource)
        {
            super(propertySource);
        }
        
        protected void createChildFigures()
        {
            mShadowTextFlow = new TextFlow("");
            mShadowFlowPage = new FlowPage();
            mShadowFlowPage.setVisible(false);
            mShadowFlowPage.add(mShadowTextFlow);
            add(mShadowFlowPage);
            
            mTextFlow = new TextFlow("");
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
            mTextFlow.setText(getText());
            mShadowTextFlow.setText(getText());
            mTextFlow.setForegroundColor((isDisabled()?ColorManager.getSystemColor(WinAPI.COLOR_GRAYTEXT):ColorManager.getColor(getTxtColor())));
            mShadowFlowPage.setVisible(isDisabled());
        }
    }
}

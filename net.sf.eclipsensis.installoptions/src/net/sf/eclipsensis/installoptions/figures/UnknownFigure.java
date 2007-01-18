/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.installoptions.edit.unknown.InstallOptionsUnknownEditPart.IUnknownFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;
import net.sf.eclipsensis.installoptions.model.InstallOptionsWidget;
import net.sf.eclipsensis.installoptions.properties.PropertySourceWrapper;
import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.IPropertySource;

public class UnknownFigure extends AbstractInstallOptionsFigure implements IUnknownFigure
{
    private String mType;
    private LabelFigure mOuterLabelFigure;
    private IFigure mFigure;
    private LabelFigure mInnerLabelFigure;

    public UnknownFigure(Composite parent, final IPropertySource propertySource, int style)
    {
        super();
        setOpaque(true);
        setLayoutManager(new XYLayout());

        final List flags = new ArrayList((List)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_FLAGS));
        boolean hScroll = flags.remove(InstallOptionsModel.FLAGS_HSCROLL);
        boolean vScroll = flags.remove(InstallOptionsModel.FLAGS_VSCROLL);

        final Rectangle[] childBounds = calculateBounds((Rectangle)propertySource.getPropertyValue(InstallOptionsWidget.PROPERTY_BOUNDS), hScroll, vScroll);
        mOuterLabelFigure = new LabelFigure(parent, new PropertySourceWrapper(propertySource){
            public Object getPropertyValue(Object id)
            {
                if(InstallOptionsWidget.PROPERTY_BOUNDS.equals(id)) {
                    return childBounds[0];
                }
                else if(InstallOptionsModel.PROPERTY_TEXT.equals(id)) {
                    return ""; //$NON-NLS-1$
                }
                else {
                    return super.getPropertyValue(id);
                }
            }
        }, (style < 0?getDefaultStyle():style));
        add(mOuterLabelFigure);
        mFigure = new Figure();
        mFigure.setOpaque(true);
        mFigure.setBorder(new DashedLineBorder());
        mFigure.setBackgroundColor(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        add(mFigure);
        mInnerLabelFigure = new LabelFigure(parent, new PropertySourceWrapper(propertySource){
            public Object getPropertyValue(Object id)
            {
                if(InstallOptionsWidget.PROPERTY_BOUNDS.equals(id)) {
                    return childBounds[2];
                }
                else if(InstallOptionsModel.PROPERTY_TEXT.equals(id)) {
                    return super.getPropertyValue(InstallOptionsModel.PROPERTY_TYPE);
                }
                else if(InstallOptionsModel.PROPERTY_FLAGS.equals(id)) {
                    return flags;
                }
                else {
                    return super.getPropertyValue(id);
                }
            }
        }, SWT.CENTER|SWT.SINGLE) {
            protected Control createUneditableSWTControl(Composite parent, int style)
            {
                Control control = super.createUneditableSWTControl(parent, style);
                control.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
                return control;
            }
        };
        add(mInnerLabelFigure);
        setType((String)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TYPE));
    }

    public UnknownFigure(Composite parent, IPropertySource propertySource)
    {
        this(parent, propertySource, -1);
    }

    private Rectangle[] calculateBounds(Rectangle rect, boolean hScroll, boolean vScroll)
    {
        Rectangle copy = new Rectangle(new Point(0,0),rect.getSize());
        Rectangle[] childBounds = {copy, copy.getCopy(),null};
        if(hScroll) {
            childBounds[1].height -= WinAPI.GetSystemMetrics(WinAPI.SM_CYHSCROLL);
        }
        if(vScroll) {
            childBounds[1].width -= WinAPI.GetSystemMetrics(WinAPI.SM_CXVSCROLL);
        }
        childBounds[2] = childBounds[1].getCopy().shrink(1,1);
        int height = Display.getDefault().getSystemFont().getFontData()[0].height+6;
        childBounds[2].y += (childBounds[2].height-height)/2;
        childBounds[2].height = height;
        return childBounds;
    }

    public void setBounds(Rectangle rect)
    {
        Dimension oldSize = bounds.getSize();
        super.setBounds(rect);
        if(!oldSize.equals(rect.getSize())) {
            updateBounds(rect);
        }
    }

    private void updateBounds(Rectangle rect)
    {
        Rectangle[] childBounds = calculateBounds(rect, isHScroll(), isVScroll());
        setConstraint(mOuterLabelFigure, childBounds[0]);
        setConstraint(mFigure, childBounds[1]);
        setConstraint(mInnerLabelFigure, childBounds[2]);
    }

    public int getDefaultStyle()
    {
        return SWT.CENTER;
    }

    public String getType()
    {
        return mType==null?"":mType; //$NON-NLS-1$
    }

    public void setType(String type)
    {
        mType = type;
        mInnerLabelFigure.setText(mType);
    }

    public void setDisabled(boolean disabled)
    {
        mInnerLabelFigure.setDisabled(disabled);
        mOuterLabelFigure.setDisabled(disabled);
    }

    public void setHScroll(boolean hScroll)
    {
        mOuterLabelFigure.setHScroll(hScroll);
        updateBounds(bounds);
    }

    public void setVScroll(boolean vScroll)
    {
        mOuterLabelFigure.setVScroll(vScroll);
        updateBounds(bounds);
    }

    public boolean isDisabled()
    {
        return mInnerLabelFigure.isDisabled();
    }

    public boolean isHScroll()
    {
        return mOuterLabelFigure.isHScroll();
    }

    public boolean isVScroll()
    {
        return mOuterLabelFigure.isVScroll();
    }

    public void refresh()
    {
        mInnerLabelFigure.refresh();
        mOuterLabelFigure.refresh();
    }

    public Rectangle getDirectEditArea()
    {
        return mInnerLabelFigure.getDirectEditArea();
    }
}

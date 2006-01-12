/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import net.sf.eclipsensis.installoptions.edit.link.InstallOptionsLinkEditPart.ILinkFigure;
import net.sf.eclipsensis.installoptions.model.InstallOptionsLink;
import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.IPropertySource;

public class LinkFigure extends LabelFigure implements ILinkFigure
{
    private RGB mTxtColor;

    public LinkFigure(Composite parent, IPropertySource propertySource, int style)
    {
        super(parent, propertySource, style);
    }

    public LinkFigure(Composite parent, IPropertySource propertySource)
    {
        super(parent, propertySource);
    }

    protected void init(IPropertySource propertySource)
    {
        setTxtColor((RGB)propertySource.getPropertyValue(InstallOptionsModel.PROPERTY_TXTCOLOR));
        super.init(propertySource);
   }

    public RGB getTxtColor()
    {
        return mTxtColor==null?InstallOptionsLink.DEFAULT_TXTCOLOR:mTxtColor;
    }

    public void setTxtColor(RGB txtColor)
    {
        mTxtColor = txtColor;
    }

    /**
     * @return
     */
    protected Control createUneditableSWTControl(Composite parent, int style)
    {
        Control label = super.createUneditableSWTControl(parent, style);
        final Color color = new Color(label.getDisplay(),getTxtColor());
        label.setForeground(color);
        label.addDisposeListener(new DisposeListener(){
            public void widgetDisposed(DisposeEvent e)
            {
                color.dispose();
            }
        });
        return label;
    }

    /**
     * @return
     */
    public int getDefaultStyle()
    {
        return SWT.LEFT;
    }
}

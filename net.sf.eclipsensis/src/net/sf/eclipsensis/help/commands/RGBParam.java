/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.util.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.w3c.dom.Node;

public class RGBParam extends HexStringParam
{
    public RGBParam(Node node)
    {
        super(node);
    }

    protected PrefixableParamEditor createPrefixableParamEditor()
    {
        return new RGBParamEditor();
    }

    protected String getValidateErrorMessage()
    {
        return EclipseNSISPlugin.getResourceString("rgb.param.error"); //$NON-NLS-1$
    }

    protected String getRegexp()
    {
        StringBuffer buf = new StringBuffer(""); //$NON-NLS-1$
        if(isAcceptVar()) {
            buf.append(getVarRegexp()).append("|"); //$NON-NLS-1$
        }
        if(isAcceptSymbol()) {
            buf.append(getSymbolRegexp()).append("|"); //$NON-NLS-1$
        }
        buf.append("[0-9a-f]{6}"); //$NON-NLS-1$
        return buf.toString();
    }

    protected class RGBParamEditor extends StringParamEditor
    {
        protected Control createParamControl(Composite parent)
        {
            parent = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(2,false);
            layout.marginHeight = layout.marginWidth = 0;
            parent.setLayout(layout);
            Text text = (Text)super.createParamControl(parent);
            if(isValid(text)) {
                text.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
                final Button b = new Button(parent,SWT.PUSH);
                b.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
                b.setText(EclipseNSISPlugin.getResourceString("browse.text")); //$NON-NLS-1$
                b.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e)
                    {
                        ColorDialog dialog = new ColorDialog(b.getShell());
                        String text = IOUtility.decodePath(mText.getText());
                        if(!Common.isEmpty(text)) {
                            try {
                                dialog.setRGB(ColorManager.hexToRGB(text));
                            }
                            catch(Exception ex) {
                                EclipseNSISPlugin.getDefault().log(ex);
                            }
                        }
                        RGB rgb = dialog.open();
                        if(rgb != null) {
                            mText.setText(ColorManager.rgbToHex(rgb));
                        }
                    }
                });
            }
            return parent;
        }
    }
}

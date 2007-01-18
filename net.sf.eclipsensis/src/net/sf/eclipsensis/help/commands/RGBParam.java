/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.ColorEditor;
import net.sf.eclipsensis.util.ColorManager;
import net.sf.eclipsensis.util.Common;

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

    protected PrefixableParamEditor createPrefixableParamEditor(INSISParamEditor parentEditor)
    {
        return new RGBParamEditor(parentEditor);
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
        public RGBParamEditor(INSISParamEditor parentEditor)
        {
            super(parentEditor);
        }

        public void clear()
        {
            if(Common.isValid(mText)) {
                mText.setText(""); //$NON-NLS-1$
            }
            super.clear();
        }

        protected void updateState(boolean state)
        {
            if(Common.isValid(mText)) {
                mText.setEnabled(state);
                Button b = (Button)mText.getData(DATA_BUTTON);
                if(Common.isValid(b)) {
                    b.setEnabled(state);
                }
            }
            super.updateState(state);
        }

        protected Control createParamControl(Composite parent)
        {
            parent = new Composite(parent,SWT.NONE);
            GridLayout layout = new GridLayout(2,false);
            layout.marginHeight = layout.marginWidth = 0;
            parent.setLayout(layout);
            mText = (Text)super.createParamControl(parent);
            if(Common.isValid(mText)) {
                mText.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
                final ColorEditor editor = new ColorEditor(parent);
                Button b = editor.getButton();
                final RGB bgColor = b.getBackground().getRGB();
                b.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
                mText.addModifyListener(new ModifyListener() {
                    public void modifyText(ModifyEvent e)
                    {
                        RGB rgb = bgColor;
                        String text = mText.getText();
                        if(text.length() == 6) {
                            rgb = ColorManager.hexToRGB(text);
                        }
                        if(!rgb.equals(editor.getRGB())) {
                            editor.setRGB(rgb);
                        }
                    }
                });
                b.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e)
                    {
                        mText.setText(ColorManager.rgbToHex(editor.getRGB()));
                    }
                });
                mText.setData(DATA_BUTTON,b);
            }
            return parent;
        }
    }
}

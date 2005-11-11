/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/

package net.sf.eclipsensis.installoptions.properties.editors;

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class CustomComboBoxCellEditor extends CellEditor
{
    private List mItems = new ArrayList();
    private String mSelection;
    private CCombo mCombo;
    private static final int DEFAULT_STYLE = SWT.NONE;

    public CustomComboBoxCellEditor(Composite parent, List items) {
        this(parent, items, DEFAULT_STYLE);
    }

    public CustomComboBoxCellEditor(Composite parent, List items, int style) {
        super(parent, style);
        setItems(items);
    }

    public List getItems() {
        return mItems;
    }

    public void setItems(List items)
    {
        mItems.clear();
        mItems.addAll(items);
        populateComboBoxItems();
    }

    /* (non-Javadoc)
     * Method declared on CellEditor.
     */
    protected Control createControl(Composite parent)
    {
        mCombo = new CCombo(parent, getStyle());
        mCombo.setFont(parent.getFont());

        mCombo.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                keyReleaseOccured(e);
            }
        });

        mCombo.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mSelection = mCombo.getText();
            }
        });

        mCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event)
            {
                applyEditorValueAndDeactivate();
            }
//
//            public void widgetSelected(SelectionEvent event) {
//                mSelection = mCombo.getSelectionIndex();
//            }
        });

        mCombo.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e)
            {
                if (e.detail == SWT.TRAVERSE_ESCAPE
                        || e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                }
            }
        });

        mCombo.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e)
            {
                CustomComboBoxCellEditor.this.focusLost();
            }
        });
        return mCombo;
    }

    protected Object doGetValue()
    {
        return mSelection;
    }

    protected void doSetFocus()
    {
        mCombo.setFocus();
    }

    public LayoutData getLayoutData()
    {
        LayoutData layoutData = super.getLayoutData();
        if ((mCombo == null) || mCombo.isDisposed()) {
            layoutData.minimumWidth = 60;
        }
        else {
            // make the comboBox 10 characters wide
            GC gc = new GC(mCombo);
            layoutData.minimumWidth = (gc.getFontMetrics()
                    .getAverageCharWidth() * 10) + 10;
            gc.dispose();
        }
        return layoutData;
    }

    protected void doSetValue(Object value)
    {
        mSelection = (String)value;
        int n = mItems.indexOf(mSelection);
        if(n >= 0) {
            mCombo.select(n);
        }
        else {
            mCombo.deselectAll();
        }
        mCombo.setText(mSelection);
    }

    private void populateComboBoxItems()
    {
        if (mCombo != null && mItems != null) {
            mCombo.removeAll();
            for (Iterator iter = mItems.iterator(); iter.hasNext(); ) {
                mCombo.add((String)iter.next());
            }

            setValueValid(true);
            mSelection = ""; //$NON-NLS-1$
        }
    }

    private void applyEditorValueAndDeactivate()
    {
        //  must set the selection before getting value
        mSelection = mCombo.getText();
        Object newValue = doGetValue();
        markDirty();
        boolean isValid = isCorrect(newValue);
        setValueValid(isValid);
        if (!isValid) {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(),
                    new Object[] { mSelection }));
        }
        fireApplyEditorValue();
        deactivate();
    }

    protected void focusLost()
    {
        if (isActivated()) {
            applyEditorValueAndDeactivate();
        }
    }

    protected void keyReleaseOccured(KeyEvent keyEvent)
    {
        if (keyEvent.character == '\u001b') { // Escape character
            fireCancelEditor();
        } else if (keyEvent.character == '\t') { // tab key
            applyEditorValueAndDeactivate();
        }
    }
}

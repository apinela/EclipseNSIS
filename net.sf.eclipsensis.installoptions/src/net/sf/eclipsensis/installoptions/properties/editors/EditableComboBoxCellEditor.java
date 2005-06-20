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
import java.util.Collections;
import java.util.List;

import net.sf.eclipsensis.util.WinAPI;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.*;

public class EditableComboBoxCellEditor extends CellEditor
{
    private static final int defaultStyle = SWT.DROP_DOWN;

    private List mItems;
    private String mSelection;
    private Combo mCombo;
    private boolean mDownArrowPressed = false;
    private boolean mAutoDropDown = false;
    private boolean mAutoApplyEditorValue = false;
    private FocusAdapter mAutoDropDownFocusAdapter = new FocusAdapter(){
        public void focusGained(FocusEvent e) {
            WinAPI.SendMessage(mCombo.handle, WinAPI.CB_SHOWDROPDOWN,1,0);
        }
    };

    public EditableComboBoxCellEditor()
    {
        setStyle(defaultStyle);
    }

    public EditableComboBoxCellEditor(Composite parent, List items)
    {
        this(parent, items, defaultStyle);
    }

    public EditableComboBoxCellEditor(Composite parent, List items, int style)
    {
        super(parent, style);
        setItems(items);
    }

    public void deactivate()
    {
        super.deactivate();
        mDownArrowPressed = false;
    }
    
    public boolean isAutoApplyEditorValue()
    {
        return mAutoApplyEditorValue;
    }
    
    public void setAutoApplyEditorValue(boolean autoApplyEditorValue)
    {
        mAutoApplyEditorValue = autoApplyEditorValue;
    }
    
    public boolean isAutoDropDown()
    {
        return mAutoDropDown;
    }
    
    public void setAutoDropDown(boolean autoDropDown)
    {
        if(mAutoDropDown != autoDropDown) {
            mAutoDropDown = autoDropDown;
            if(mAutoDropDown) {
                mCombo.addFocusListener(mAutoDropDownFocusAdapter);
            }
            else {
                mCombo.removeFocusListener(mAutoDropDownFocusAdapter);
            }
        }
    }
    
    public List getItems()
    {
        return (mItems == null?Collections.EMPTY_LIST:mItems);
    }

    public void setItems(List items)
    {
        mItems = items;
        populateComboBoxItems();
    }

    /*
     * (non-Javadoc) Method declared on CellEditor.
     */
    protected Control createControl(Composite parent)
    {
        mCombo = new Combo(parent, getStyle());
        mCombo.setFont(parent.getFont());

        mCombo.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                if(e.keyCode == SWT.ARROW_DOWN) {
                    mDownArrowPressed = true;
                }
                keyReleaseOccured(e);
            }

            public void keyReleased(KeyEvent e) {
                if(e.keyCode == SWT.ARROW_DOWN) {
                    mDownArrowPressed = false;
                }
            }
        });

        mCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event)
            {
                applyEditorValueAndDeactivate();
            }

            public void widgetSelected(SelectionEvent event)
            {
                System.out.println(event.stateMask);
                computeSelection();
                if(isAutoApplyEditorValue()) {
                    int n = WinAPI.SendMessage(mCombo.handle,WinAPI.CB_GETDROPPEDSTATE,0,0);
                    if(n == 0 && !mDownArrowPressed) {
                        fireApplyEditorValue();
                    }
                }
            }
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
                EditableComboBoxCellEditor.this.focusLost();
            }
        });
        return mCombo;
    }


    /**
     * 
     */
    private void computeSelection()
    {
        String oldSelection = mSelection;
        boolean oldIsValid = isValueValid();
        if( (getStyle() & SWT.READ_ONLY) > 0) {
            int index = mCombo.getSelectionIndex();
            if(index < 0 || index >= mCombo.getItemCount()) {
                mSelection = ""; //$NON-NLS-1$
            }
            else {
                mSelection = mCombo.getItem(index);
            }
        }
        else {
            mSelection = mCombo.getText();
        }
        boolean newIsValid = isCorrect(mSelection);
        if(!mSelection.equals(oldSelection)) {
            valueChanged(oldIsValid,newIsValid);
        }
    }

    protected Object doGetValue()
    {
        return mSelection;
    }

    /*
     * (non-Javadoc) Method declared on CellEditor.
     */
    protected void doSetFocus()
    {
        mCombo.setFocus();
    }

    public LayoutData getLayoutData()
    {
        LayoutData layoutData = super.getLayoutData();
        if ((mCombo == null) || mCombo.isDisposed())
            layoutData.minimumWidth = 60;
        else {
            // make the comboBox 10 characters wide
            GC gc = new GC(mCombo);
            layoutData.minimumWidth = (gc.getFontMetrics().getAverageCharWidth() * 10) + 10;
            gc.dispose();
        }
        return layoutData;
    }

    protected void doSetValue(Object value)
    {
        mSelection = (String)value;
        if( (getStyle()&SWT.READ_ONLY) > 0) {
            if(!getItems().contains(value)) {
                mSelection = ""; //$NON-NLS-1$
            }
        }
        mCombo.setText(mSelection);
    }

    /**
     * Updates the list of choices for the combo box for the current control.
     */
    private void populateComboBoxItems()
    {
        if (mCombo != null && mItems != null) {
            mCombo.removeAll();
            for (int i = 0; i < mItems.size(); i++) {
                mCombo.add((String)mItems.get(i), i);
            }
            
            if(mSelection == null) {
                setValueValid(true);
                mSelection = ""; //$NON-NLS-1$
            }
            else {
                setValue(mSelection);
            }
        }
    }

    /**
     * Applies the currently selected value and deactiavates the cell editor
     */
    void applyEditorValueAndDeactivate()
    {
        //  must set the selection before getting value
        computeSelection();
        Object newValue = doGetValue();
        markDirty();
        boolean isValid = isCorrect(newValue);
        setValueValid(isValid);
        if (!isValid) {
            // try to insert the current value into the error message.
            setErrorMessage(MessageFormat.format(getErrorMessage(),
                    new Object[]{newValue}));
        }
        fireApplyEditorValue();
        deactivate();
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#focusLost()
     */
    protected void focusLost()
    {
        if (isActivated()) {
            applyEditorValueAndDeactivate();
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellEditor#keyReleaseOccured(org.eclipse.swt.events.KeyEvent)
     */
    protected void keyReleaseOccured(KeyEvent keyEvent)
    {
        if (keyEvent.character == '\u001b') { // Escape character
            fireCancelEditor();
        }
        else if (keyEvent.character == '\t') { // tab key
            applyEditorValueAndDeactivate();
        }
    }
}

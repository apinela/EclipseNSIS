/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
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
import java.util.List;

import net.sf.eclipsensis.util.*;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.*;

public class EditableComboBoxCellEditor extends CellEditor
{
    private List<String> mItems;
    private String mSelection;
    private String mNewSelection;
    private Combo mCombo;
    private boolean mDownArrowPressed = false;
    private boolean mAutoDropDown = false;
    private boolean mAutoApplyEditorValue = false;
    private boolean mCaseInsensitive = false;
    private FocusAdapter mAutoDropDownFocusAdapter = new FocusAdapter(){
        @Override
		public void focusGained(FocusEvent e) {
            WinAPI.SendMessage(mCombo.handle, WinAPI.CB_SHOWDROPDOWN,1,0);
        }
    };

    public EditableComboBoxCellEditor(Composite parent, List<String> items, int style)
    {
        super(parent, style);
        setItems(items);
    }

    public boolean isCaseInsensitive()
    {
        return mCaseInsensitive;
    }

    public void setCaseInsensitive(boolean caseInsensitive)
    {
        mCaseInsensitive = caseInsensitive;
    }

    @Override
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

    public List<String> getItems()
    {
        return (mItems == null?Collections.<String>emptyList():mItems);
    }

    public void setItems(List<String> items)
    {
        mItems = items;
        populateComboBoxItems();
    }

    /*
     * (non-Javadoc) Method declared on CellEditor.
     */
    @Override
	protected Control createControl(Composite parent)
    {
        mCombo = new Combo(parent, getStyle());
        mCombo.setFont(parent.getFont());

        mCombo.addModifyListener(new ModifyListener(){
            public void modifyText(ModifyEvent e)
            {
                mNewSelection = mCombo.getText();
            }
        });

        mCombo.addKeyListener(new KeyAdapter() {
            @Override
			public void keyPressed(KeyEvent e)
            {
                if(e.keyCode == SWT.ARROW_DOWN) {
                    mDownArrowPressed = true;
                }
                keyReleaseOccured(e);
            }

            @Override
			public void keyReleased(KeyEvent e) {
                if(e.keyCode == SWT.ARROW_DOWN) {
                    mDownArrowPressed = false;
                }
            }
        });

        mCombo.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetDefaultSelected(SelectionEvent event)
            {
                applyEditorValueAndDeactivate();
            }

            @Override
			public void widgetSelected(SelectionEvent event)
            {
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
            @Override
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
        mSelection = mNewSelection;
        boolean newIsValid = isCorrect(mSelection);
        if(!Common.stringsAreEqual(mSelection,oldSelection,mCaseInsensitive)) {
            valueChanged(oldIsValid,newIsValid);
        }
    }

    @Override
	protected Object doGetValue()
    {
        return mSelection;
    }

    /*
     * (non-Javadoc) Method declared on CellEditor.
     */
    @Override
	protected void doSetFocus()
    {
        mCombo.setFocus();
    }

    @Override
	public LayoutData getLayoutData()
    {
        LayoutData layoutData = super.getLayoutData();
        if ((mCombo == null) || mCombo.isDisposed()) {
            layoutData.minimumWidth = 60;
        }
        else {
            // make the comboBox 10 characters wide
            GC gc = new GC(mCombo);
            layoutData.minimumWidth = (gc.getFontMetrics().getAverageCharWidth() * 10) + 10;
            gc.dispose();
        }
        return layoutData;
    }

    private String checkValue(String value)
    {
        for (Iterator<String> iter = getItems().iterator(); iter.hasNext();) {
            String item = iter.next();
            if(Common.stringsAreEqual(item, value, mCaseInsensitive)) {
                return item;
            }
        }
        return null;
    }

    @Override
	protected void doSetValue(Object value)
    {
        String val = (String)value;
        String selection = checkValue(val);
        if(selection == null) {
            if(val != null) {
                if( (getStyle()&SWT.READ_ONLY) > 0) {
                    selection = ""; //$NON-NLS-1$
                }
                else {
                    selection = val;
                }
            }
            else {
                selection = ""; //$NON-NLS-1$
            }
        }
        mSelection = selection;
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
                mCombo.add(mItems.get(i), i);
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
    @Override
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
    @Override
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

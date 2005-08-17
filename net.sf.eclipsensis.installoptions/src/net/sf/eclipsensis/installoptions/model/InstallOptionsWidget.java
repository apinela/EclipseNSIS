/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.installoptions.properties.PositionPropertySource;
import net.sf.eclipsensis.installoptions.properties.labelproviders.ListLabelProvider;
import net.sf.eclipsensis.installoptions.properties.validators.NSISStringLengthValidator;
import net.sf.eclipsensis.installoptions.rulers.InstallOptionsGuide;
import net.sf.eclipsensis.installoptions.util.TypeConverter;
import net.sf.eclipsensis.util.Common;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.views.properties.*;

public abstract class InstallOptionsWidget extends InstallOptionsElement
{
    public static final String PROPERTY_BOUNDS = "Bounds"; //$NON-NLS-1$
    private static final Position cDefaultPosition = new Position(0,0,63,35);
    
    private static LabelProvider cPositionLabelProvider = new LabelProvider(){
        public String getText(Object element)
        {
            if(element instanceof Position) {
                Position pos = (Position)element;
                return new StringBuffer("(").append(pos.left).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
                        pos.top).append(",").append(pos.right).append(",").append( //$NON-NLS-1$ //$NON-NLS-2$
                        pos.bottom).append(")").toString(); //$NON-NLS-1$
            }
            return super.getText(element);
        }
    };

    private static LabelProvider cFlagsLabelProvider = new ListLabelProvider();
    private static final String MISSING_DISPLAY_NAME = InstallOptionsPlugin.getResourceString("missing.widget.display.name"); //$NON-NLS-1$

    private InstallOptionsModelTypeDef mTypeDef;
    protected InstallOptionsDialog mParent;
    protected int mIndex;
    protected Position mPosition;
    protected InstallOptionsGuide mVerticalGuide;
    protected InstallOptionsGuide mHorizontalGuide;
    private List mFlags;

    protected InstallOptionsWidget(INISection section)
    {
        super(section);
        mTypeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(getType());
    }
    
    protected void init()
    {
        super.init();
        mIndex = -1;
        mPosition = new Position();
    }
    
    protected void setDefaults()
    {
        super.setDefaults();
        mPosition = getDefaultPosition();
    }

    /**
     * 
     */
    protected final Collection getPropertyNames()
    {
        List list = new ArrayList();
        list.add(InstallOptionsModel.PROPERTY_INDEX);
        list.add(InstallOptionsModel.PROPERTY_POSITION);
        list.addAll(super.getPropertyNames());
        return list;
    }

    /**
     * 
     */
    protected final Collection doGetPropertyNames()
    {
        List list = new ArrayList();
        list.add(InstallOptionsModel.PROPERTY_TYPE);
        InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(getType());
        if(typeDef != null) {
            Collection settings = typeDef.getSettings();
            for (Iterator iter=settings.iterator(); iter.hasNext(); ) {
                addPropertyName(list, (String)iter.next());
            }
        }
        return list;
    }
    
    protected void addPropertyName(List list, String setting)
    {
        if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_LEFT)) {
            list.add(InstallOptionsModel.PROPERTY_LEFT);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_RIGHT)) {
            list.add(InstallOptionsModel.PROPERTY_RIGHT);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_TOP)) {
            list.add(InstallOptionsModel.PROPERTY_TOP);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_BOTTOM)) {
            list.add(InstallOptionsModel.PROPERTY_BOTTOM);
        }
        else if(setting.equalsIgnoreCase(InstallOptionsModel.PROPERTY_FLAGS)) {
            list.add(InstallOptionsModel.PROPERTY_FLAGS);
        }
    }

    protected IPropertyDescriptor createPropertyDescriptor(String name)
    {
        if(name.equals(InstallOptionsModel.PROPERTY_INDEX)) {
            return new IndexPropertyDescriptor();
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_POSITION)) {
            PropertyDescriptor positionPropertyDescriptor = new PropertyDescriptor(InstallOptionsModel.PROPERTY_POSITION, InstallOptionsPlugin.getResourceString("position.property.name")); //$NON-NLS-1$
            positionPropertyDescriptor.setLabelProvider(cPositionLabelProvider);
            return positionPropertyDescriptor;
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_TYPE)) {
            return new PropertyDescriptor(InstallOptionsModel.PROPERTY_TYPE, InstallOptionsPlugin.getResourceString("type.property.name")); //$NON-NLS-1$
        }
        else if(name.equals(InstallOptionsModel.PROPERTY_FLAGS)) {
            return new FlagsPropertyDescriptor();
        }
        else {
            return null;
        }
    }

    public InstallOptionsGuide getHorizontalGuide()
    {
        return mHorizontalGuide;
    }

    public InstallOptionsGuide getVerticalGuide()
    {
        return mVerticalGuide;
    }

    public void setHorizontalGuide(InstallOptionsGuide hGuide)
    {
        mHorizontalGuide = hGuide;
    }

    public void setVerticalGuide(InstallOptionsGuide vGuide)
    {
        mVerticalGuide = vGuide;
    }

    public Object getPropertyValue(Object id)
    {
        if (PROPERTY_BOUNDS.equals(id)) {
            return toGraphical(getPosition()).getBounds();
        }
        else if (InstallOptionsModel.PROPERTY_LEFT.equals(id)) {
            return new Integer(getPosition().left);
        }
        else if (InstallOptionsModel.PROPERTY_TOP.equals(id)) {
            return new Integer(getPosition().top);
        }
        else if (InstallOptionsModel.PROPERTY_RIGHT.equals(id)) {
            return new Integer(getPosition().right);
        }
        else if (InstallOptionsModel.PROPERTY_BOTTOM.equals(id)) {
            return new Integer(getPosition().bottom);
        }
        else if (InstallOptionsModel.PROPERTY_POSITION.equals(id)) {
            return new PositionPropertySource(this);
        }
        if (InstallOptionsModel.PROPERTY_INDEX.equals(id)) {
            return new Integer(getIndex());
        }
        if (InstallOptionsModel.PROPERTY_FLAGS.equals(id)) {
            return getFlags();
        }
        return super.getPropertyValue(id);
    }
    
    protected TypeConverter getTypeConverter(String property)
    {
        if (InstallOptionsModel.PROPERTY_LEFT.equals(property) ||
            InstallOptionsModel.PROPERTY_TOP.equals(property) ||
            InstallOptionsModel.PROPERTY_RIGHT.equals(property) ||
            InstallOptionsModel.PROPERTY_BOTTOM.equals(property)) {
            return TypeConverter.INTEGER_CONVERTER;
        }
        else if(InstallOptionsModel.PROPERTY_FLAGS.equals(property)) {
            return TypeConverter.STRING_LIST_CONVERTER;
        }
        else {
            return super.getTypeConverter(property);
        }
    }
    
    public void setPropertyValue(Object id, Object value)
    {
        if(InstallOptionsModel.PROPERTY_POSITION.equals(id)) {
            setPosition((Position)value);
        }
        else if (InstallOptionsModel.PROPERTY_LEFT.equals(id)) {
            getPosition().left = ((Integer)value).intValue();
        }
        else if (InstallOptionsModel.PROPERTY_TOP.equals(id)) {
            getPosition().top = ((Integer)value).intValue();
        }
        else if (InstallOptionsModel.PROPERTY_RIGHT.equals(id)) {
            getPosition().right = ((Integer)value).intValue();
        }
        else if (InstallOptionsModel.PROPERTY_BOTTOM.equals(id)) {
            getPosition().bottom = ((Integer)value).intValue();
        }
        else if(InstallOptionsModel.PROPERTY_INDEX.equals(id)) {
            if(mIndex != ((Integer)value).intValue()) {
                firePropertyChange(InstallOptionsModel.PROPERTY_INDEX,new Integer(mIndex), value);
                setDirty(true);
            }
        }
        else if(InstallOptionsModel.PROPERTY_FLAGS.equals(id)) {
            setFlags((List)value);
        }
        else {
            super.setPropertyValue(id,value);
        }
    }

    public InstallOptionsDialog getParent()
    {
        return mParent;
    }

    void setParent(InstallOptionsDialog parent)
    {
        mParent = parent;
        setDirty(true);
    }
    
    public int getIndex()
    {
        return mIndex;
    }
    
    void setIndex(int index)
    {
        mIndex = index;
        setDirty(true);
    }

    public final String toString()
    {
        String displayName = getDisplayName();
        return InstallOptionsPlugin.getFormattedString("widget.field.format", //$NON-NLS-1$
                new Object[]{new Integer(getIndex()+1), getType(), 
                            (Common.isEmpty(displayName)?MISSING_DISPLAY_NAME:displayName)});
    }

    public List getFlags()
    {
        if(mFlags == null) {
            mFlags = new ArrayList();
        }
        return mFlags;
    }

    public void setFlags(List flags)
    {
        List oldFlags = getFlags();
        if(!oldFlags.equals(flags)) {
            mFlags = flags;
            firePropertyChange(InstallOptionsModel.PROPERTY_FLAGS,oldFlags,mFlags);
            setDirty(true);
        }
    }
    
    private int toGraphical(int value, int refValue)
    {
        if(value < 0) {
            value = Math.max(value,refValue+value);
        }
        return value;
    }

    public Position toGraphical(Position p)
    {
        return toGraphical(p, true);
    }

    public Position toGraphical(Position p, boolean toPixels)
    {
        InstallOptionsDialog dialog = getParent();
        return toGraphical(p, (dialog==null?null:dialog.getDialogSize()), toPixels);
    }

    public Position toGraphical(Position p, Dimension size)
    {
        return toGraphical(p, size, true);
    }

    public Position toGraphical(Position p, Dimension size, boolean toPixels)
    {
        p = p.getCopy();
        if(size == null) {
            p.set(0,0,0,0);
        }
        else {
            p.left = toGraphical(p.left,size.width);
            p.top = toGraphical(p.top,size.height);
            p.right = toGraphical(p.right,size.width);
            p.bottom = toGraphical(p.bottom,size.height);
        }
        if(toPixels) {
            p = FigureUtility.dialogUnitsToPixels(p,Display.getDefault().getSystemFont());
        }
        return p;
    }

    private int toModel(int value, int localValue, int refValue)
    {
        if(localValue < 0 && value < refValue) {
            value = Math.max(0,value) - refValue;
        }
        return value;
    }

    public Position toModel(Position p)
    {
        return toModel(p, true);
    }

    public Position toModel(Position p, boolean fromPixels)
    {
        InstallOptionsDialog dialog = getParent();
        return toModel(p, (dialog==null?null:dialog.getDialogSize()), fromPixels);
    }

    public Position toModel(Position p, Dimension size)
    {
        return toModel(p, size, true);
    }

    public Position toModel(Position p, Dimension size, boolean fromPixels)
    {
        if(fromPixels) {
            p = FigureUtility.pixelsToDialogUnits(p,Display.getDefault().getSystemFont());
        }
        if(size == null) {
            p.set(0,0,0,0);
        }
        else {
            p.left = toModel(p.left, mPosition.left, size.width);
            p.top = toModel(p.top, mPosition.top, size.height);
            p.right = toModel(p.right, mPosition.right, size.width);
            p.bottom = toModel(p.bottom, mPosition.bottom, size.height);
        }
        return p;
    }

    public Position getPosition()
    {
        return mPosition;
    }
    
    public void setPosition(Position position)
    {
        Position mOldPosition = mPosition;
        mPosition = position;
        if(!mPosition.equals(mOldPosition)) {
            firePropertyChange(InstallOptionsModel.PROPERTY_POSITION,mOldPosition,mPosition);
            setDirty(true);
        }
    }

    public Object clone()
    {
        InstallOptionsWidget element = (InstallOptionsWidget)super.clone();
        element.setParent(null);
        element.setHorizontalGuide(null);
        element.setVerticalGuide(null);
        element.setPosition(mPosition.getCopy());
        element.setFlags(new ArrayList(getFlags()));
        element.setIndex(-1);
        return element;
    }
    
    protected Position getDefaultPosition()
    {
        return cDefaultPosition.getCopy();
    }
    
    protected String getDisplayName()
    {
        return ""; //$NON-NLS-1$
    }
    
    protected String getSectionName()
    {
        return InstallOptionsModel.SECTION_FIELD_FORMAT.format(new Object[]{new Integer(getIndex()+1)});
    }

    public final Image getIconImage()
    {
        return InstallOptionsPlugin.getImageManager().getImage(mTypeDef.getSmallIcon());
    }
    
    private class IndexPropertyDescriptor extends ComboBoxPropertyDescriptor
    {
        public IndexPropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_INDEX, InstallOptionsPlugin.getResourceString("index.property.name"), new String[0]); //$NON-NLS-1$
            setLabelProvider(new LabelProvider(){
                public String getText(Object element)
                {
                    return Integer.toString(((Integer)element).intValue()+1);
                }
            });
        }

        public CellEditor createPropertyEditor(Composite parent)
        {
            String[] values = new String[getParent().getChildren().size()];
            for (int i = 0; i < values.length; i++) {
                values[i]=Integer.toString(i+1);
            }
            CellEditor editor = new ComboBoxCellEditor(parent, values, SWT.READ_ONLY);
            return editor;
        }
    }
    
    private class FlagsPropertyDescriptor extends PropertyDescriptor
    {
        public FlagsPropertyDescriptor()
        {
            super(InstallOptionsModel.PROPERTY_FLAGS, InstallOptionsPlugin.getResourceString("flags.property.name")); //$NON-NLS-1$
            setLabelProvider(cFlagsLabelProvider);
            setValidator(new NSISStringLengthValidator(InstallOptionsModel.PROPERTY_FLAGS));
        }

        public CellEditor createPropertyEditor(Composite parent)
        {
            final FlagsCellEditor cellEditor = new FlagsCellEditor(parent);
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                cellEditor.setValidator(validator);
            }
            return cellEditor;
        }
    }
        
    private final class FlagsCellEditor extends DialogCellEditor
    {
        private FlagsCellEditor(Composite parent)
        {
            super(parent);
        }
        protected void updateContents(Object value) 
        {
            Label label = getDefaultLabel();
            if (label != null) {
                label.setText(cFlagsLabelProvider.getText(value));
            }
        }

        protected Object openDialogBox(Control cellEditorWindow)
        {
            FlagsDialog dialog = new FlagsDialog(cellEditorWindow.getShell(), (List)getValue(), getType());
            dialog.setValidator(getValidator());
            dialog.open();
            return dialog.getValues();
        }
    }

    private class FlagsDialog extends Dialog
    {
        private ICellEditorValidator mValidator;
        private List mValues;
        private Table mTable;
        private String mType;
        private Collection mAvailableFlags;
        
        public FlagsDialog(Shell parent, List values, String type)
        {
            super(parent);
            mValues = new ArrayList(values);
            mType = type;
            InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(getType());
            mAvailableFlags = (typeDef==null?Collections.EMPTY_SET:typeDef.getFlags());
        }

        public ICellEditorValidator getValidator()
        {
            return mValidator;
        }
        
        public void setValidator(ICellEditorValidator validator)
        {
            mValidator = validator;
        }
        
        protected void configureShell(Shell newShell)
        {
            super.configureShell(newShell);
            newShell.setText(InstallOptionsPlugin.getFormattedString("flags.dialog.name", new String[]{mType})); //$NON-NLS-1$
            newShell.setImage(InstallOptionsPlugin.getImageManager().getImage(InstallOptionsPlugin.getResourceString("installoptions.icon"))); //$NON-NLS-1$
        }

        public List getValues()
        {
            return mValues;
        }

        protected Control createDialogArea(Composite parent)
        {
            Composite composite = (Composite)super.createDialogArea(parent);
            Composite composite2 = new Composite(composite,SWT.NONE);
            composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            GridLayout layout = new GridLayout(2,false);
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite2.setLayout(layout);
            
            mTable = new Table(composite2,SWT.CHECK| SWT.BORDER | SWT.MULTI | SWT.HIDE_SELECTION | SWT.V_SCROLL);
            mTable.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected(SelectionEvent e)
                {
                    mTable.deselectAll();
                }
            });
            initializeDialogUnits(mTable);
            GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
            data.widthHint = convertWidthInCharsToPixels(40);
            data.heightHint = convertHeightInCharsToPixels(10);
            mTable.setLayoutData(data);
            for (Iterator iter=mAvailableFlags.iterator(); iter.hasNext(); ) {
                TableItem ti = new TableItem(mTable,SWT.NONE);
                String flag = (String)iter.next();
                ti.setText(flag);
                if(mValues != null) {
                    ti.setChecked(mValues.contains(flag));
                }
            }
            composite2 = new Composite(composite2,SWT.NONE);
            composite2.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
            layout = new GridLayout(1,false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            composite2.setLayout(layout);
            Button b = new Button(composite2,SWT.PUSH);
            b.setText(InstallOptionsPlugin.getResourceString("select.all.label")); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected(SelectionEvent e) 
                {
                    selectAll(true);
                }
            });
            b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            b = new Button(composite2,SWT.PUSH);
            b.setText(InstallOptionsPlugin.getResourceString("deselect.all.label")); //$NON-NLS-1$
            b.addSelectionListener(new SelectionAdapter(){
                public void widgetSelected(SelectionEvent e) 
                {
                    selectAll(false);
                }
            });
            b.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            return composite;
        }

        private void selectAll(boolean state)
        {
            TableItem[] items = mTable.getItems();
            for (int i = 0; i < items.length; i++) {
                items[i].setChecked(state);
            }
        }

        protected void okPressed()
        {
            if(mTable != null) {
                mValues.removeAll(mAvailableFlags);
                TableItem[] items = mTable.getItems();
                int counter=0;
                for (int i = 0; i < items.length; i++) {
                    if(items[i].getChecked()) {
                        mValues.add(counter++,items[i].getText());
                    }
                }
            }
            ICellEditorValidator validator = getValidator();
            if(validator != null) {
                String error = validator.isValid(mValues);
                if(!Common.isEmpty(error)) {
                    MessageDialog.openError(getShell(),EclipseNSISPlugin.getResourceString("error.title"),error); //$NON-NLS-1$
                    return;
                }
            }
            super.okPressed();
        }
    }


}

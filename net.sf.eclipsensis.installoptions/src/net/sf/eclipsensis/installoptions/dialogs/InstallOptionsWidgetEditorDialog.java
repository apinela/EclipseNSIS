/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.dialogs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;

import net.sf.eclipsensis.dialogs.StatusMessageDialog;
import net.sf.eclipsensis.installoptions.ini.*;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.installoptions.model.commands.IModelCommandListener;
import net.sf.eclipsensis.installoptions.model.commands.ModelCommandEvent;
import net.sf.eclipsensis.installoptions.properties.CustomPropertySheetPage;
import net.sf.eclipsensis.installoptions.properties.PropertySourceWrapper;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.views.properties.*;

public class InstallOptionsWidgetEditorDialog extends StatusMessageDialog implements PropertyChangeListener, IModelCommandListener, IPropertySourceProvider
{
    private PropertySheetPage mPage = new CustomPropertySheetPage();
    private InstallOptionsDialog mDialog;
    private InstallOptionsWidget mCurrentWidget;
    private INISection mSection;
    private boolean mCreateMode = false;
    
    public InstallOptionsWidgetEditorDialog(Shell parent, INIFile iniFile)
    {
        this(parent, iniFile, null);
    }

    public InstallOptionsWidgetEditorDialog(Shell parent, INIFile iniFile, INISection section)
    {
        super(parent);
        mDialog = InstallOptionsDialog.loadINIFile(iniFile);
        mSection = section;
        mCurrentWidget = (InstallOptionsWidget)mDialog.getElement(mSection);
        mCreateMode = (mCurrentWidget==null);
        setTitle(mCreateMode?"Create Control":"Edit Control");
    }

    public IPropertySource getPropertySource(Object object)
    {
        if(object instanceof IPropertySource) {
            return new PropertySourceWrapper((IPropertySource)object) {

                public void setPropertyValue(Object id, Object value)
                {
                    if(InstallOptionsModel.PROPERTY_TYPE.equals(id)) {
                        propertyChange(new PropertyChangeEvent(getDelegate(),(String)id,getPropertyValue(id),value));
                    }
                    else {
                        super.setPropertyValue(id, value);
                    }
                }

            };
        }
        else if(object instanceof IPropertySourceProvider && !(this.getClass().equals(object.getClass()))) {
            return getPropertySource(((IPropertySourceProvider)object).getPropertySource(object));
        }
        return null;
    }

    public void executeModelCommand(ModelCommandEvent event)
    {
        Object obj = event.getModel();
        if(Common.objectsAreEqual(obj, mCurrentWidget)) {
            event.getCommand().execute();
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_INDEX)) {
            mDialog.moveChild(mCurrentWidget, ((Integer)evt.getNewValue()).intValue());
        }
        else if(evt.getPropertyName().equals(InstallOptionsModel.PROPERTY_TYPE)) {
            INISection section = mCurrentWidget.updateSection();
            INIKeyValue[] keyValues = section.findKeyValues(InstallOptionsModel.PROPERTY_TYPE);
            String oldType = (String)evt.getOldValue();
            String newType = (String)evt.getNewValue();
            if(Common.isEmptyArray(keyValues)) {
                INIKeyValue keyValue = new INIKeyValue(InstallOptionsModel.PROPERTY_TYPE);
                keyValue.setValue(newType);
                section.addChild(keyValue);
            }
            else {
                keyValues[0].setValue(newType);
            }
            InstallOptionsElementFactory oldFactory = InstallOptionsElementFactory.getFactory(oldType);
            InstallOptionsElementFactory newFactory = InstallOptionsElementFactory.getFactory(newType);
            if(oldFactory != newFactory) {
                section.validate(mCreateMode?INILine.VALIDATE_FIX_ALL:INILine.VALIDATE_FIX_ERRORS);
                InstallOptionsWidget widget = (InstallOptionsWidget)newFactory.getNewObject(section);
                mCurrentWidget.removeModelCommandListener(InstallOptionsWidgetEditorDialog.this);
                mCurrentWidget.removePropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
                mDialog.replaceChild(mCurrentWidget, widget);
                mCurrentWidget = widget;
                mCurrentWidget.addModelCommandListener(InstallOptionsWidgetEditorDialog.this);
                mCurrentWidget.addPropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
                Display.getDefault().asyncExec(new Runnable() {
                    public void run()
                    {
                        mPage.selectionChanged(null, new StructuredSelection(mCurrentWidget));
                    }
                });                
            }
        }
    }

    protected Control createControl(Composite parent)
    {
        Composite propertyComposite = new Composite(parent,SWT.BORDER);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = layout.marginHeight = 0;
        propertyComposite.setLayout(layout);
        mPage.setPropertySourceProvider(this);
        mPage.createControl(propertyComposite);
        mPage.setActionBars(new DummyActionBars());
        Control control = mPage.getControl();
        GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
        if(control instanceof Tree) {
            final Tree tree = (Tree)control;
            data.heightHint = tree.getItemHeight()*13+(tree.getLinesVisible()?12*tree.getGridLineWidth():0)+
                             (tree.getHeaderVisible()?tree.getHeaderHeight():0)+2*tree.getBorderWidth()+
                             (tree.getHorizontalBar() != null?tree.getHorizontalBar().getSize().x:0);
            tree.addControlListener(new ControlAdapter() {
                public void controlResized(ControlEvent e) {
                    Rectangle area = tree.getClientArea();
                    TreeColumn[] columns = tree.getColumns();
                    if (area.width > 0) {
                        columns[0].setWidth(area.width * 40 / 100);
                        columns[1].setWidth(area.width - columns[0].getWidth() - 4);
                    }
                }
            });
        }
        control.setLayoutData(data);
        ISelection selection;
        if(mCurrentWidget == null) {
            Collection typeDefs = InstallOptionsModel.INSTANCE.getControlTypeDefs();
            if(typeDefs.size() > 0) {
                InstallOptionsModelTypeDef typeDef = (InstallOptionsModelTypeDef)typeDefs.iterator().next();
                InstallOptionsElementFactory factory = InstallOptionsElementFactory.getFactory(typeDef.getType());
                mCurrentWidget = (InstallOptionsWidget)factory.getNewObject();
                mDialog.addChild(mCurrentWidget);
                mCurrentWidget.addModelCommandListener(InstallOptionsWidgetEditorDialog.this);
                mCurrentWidget.addPropertyChangeListener(InstallOptionsWidgetEditorDialog.this);
                selection = new StructuredSelection(mCurrentWidget);
            }
            else {
                selection = StructuredSelection.EMPTY;
            }
        }
        else {
            selection = new StructuredSelection(mCurrentWidget);
        }
        mPage.selectionChanged(null, selection);

        return propertyComposite;
    }

    protected void okPressed()
    {
        if(mDialog.isDirty() ) {
            mDialog.updateINIFile();
            if(mSection == null) {
                mSection = mCurrentWidget.getSection();
            }
            super.okPressed();
        }
        else {
            super.cancelPressed();
        }
    }

    public INISection getSection()
    {
        return mSection;
    }
    
    private class DummyActionBars implements IActionBars
    {
        private IMenuManager mMenuManager = null;
        private IToolBarManager mToolBarManager = null;
        private IStatusLineManager mStatusLineManager = null;
        
        public void clearGlobalActionHandlers()
        {
        }

        public IAction getGlobalActionHandler(String actionId)
        {
            return null;
        }

        public IMenuManager getMenuManager()
        {
            if(mMenuManager == null) {
                mMenuManager = new MenuManager();
            }
            return mMenuManager;
        }

        public IStatusLineManager getStatusLineManager()
        {
            if(mStatusLineManager == null) {
                mStatusLineManager = new StatusLineManager() {

                    public void setErrorMessage(Image image, String message)
                    {
                        if(Common.isEmpty(message)) {
                            setMessage(image,message);
                        }
                        else {
                            updateStatus(new DialogStatus(IStatus.ERROR,message,image));
                        }
                    }

                    public void setErrorMessage(String message)
                    {
                        setErrorMessage(null, message);
                    }

                    public void setMessage(Image image, String message)
                    {
                        updateStatus(new DialogStatus(IStatus.OK,message,image));
                    }

                    public void setMessage(String message)
                    {
                        setMessage(null,message);
                    }
                };
            }
            return mStatusLineManager;
        }

        public IToolBarManager getToolBarManager()
        {
            if(mToolBarManager == null) {
                mToolBarManager = new ToolBarManager();
            }
            return mToolBarManager;
        }

        public void setGlobalActionHandler(String actionId, IAction handler)
        {
        }

        public void updateActionBars()
        {
        }
    }
}

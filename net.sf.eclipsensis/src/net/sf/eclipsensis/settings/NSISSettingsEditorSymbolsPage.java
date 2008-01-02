/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.dialogs.NSISSymbolDialog;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.*;

public class NSISSettingsEditorSymbolsPage extends NSISSettingsEditorPage
{
    protected TableViewer mSymbols = null;

    public NSISSettingsEditorSymbolsPage(NSISSettings settings)
    {
        super("symbols",settings); //$NON-NLS-1$
    }

    public void enableControls(boolean state)
    {
        if(state) {
            //Hack to properly enable the buttons
            mSymbols.setSelection(mSymbols.getSelection());
        }
    }

    protected Control createControl(final Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        SelectionAdapter addAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditSymbol(parent.getShell(),"",""); //$NON-NLS-1$ //$NON-NLS-2$
            }
        };
        SelectionAdapter editAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Map.Entry entry = (Map.Entry)((IStructuredSelection)mSymbols.getSelection()).getFirstElement();
                addOrEditSymbol(parent.getShell(),(String)entry.getKey(),(String)entry.getValue());
            }
        };
        SelectionAdapter removeAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Map map = (Map)mSymbols.getInput();
                IStructuredSelection selection = (IStructuredSelection)mSymbols.getSelection();
                for(Iterator iter = selection.iterator(); iter.hasNext(); ) {
                    map.remove(((Map.Entry)iter.next()).getKey());
                    fireChanged();
                }
                mSymbols.refresh();
            }
        };

        TableViewerUpDownMover mover = new TableViewerUpDownMover() {

            protected List getAllElements()
            {
                return new ArrayList(((LinkedHashMap)((TableViewer)getViewer()).getInput()).entrySet());
            }

            protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
            {
                ((LinkedHashMap)input).clear();
                for(Iterator iter=elements.iterator(); iter.hasNext(); ) {
                    Map.Entry entry = (Map.Entry)iter.next();
                    ((LinkedHashMap)input).put(entry.getKey(),entry.getValue());
                    fireChanged();
                }
            }

        };

        IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {
                Map.Entry entry = (Map.Entry)((IStructuredSelection)event.getSelection()).getFirstElement();
                addOrEditSymbol(parent.getShell(),(String)entry.getKey(),(String)entry.getValue());
            }
        };

        mSymbols = createTableViewer(composite, mSettings.getSymbols(), new MapContentProvider(), new MapLabelProvider(),
                                     EclipseNSISPlugin.getResourceString("symbols.description"), //$NON-NLS-1$
                                     new String[] {
                                         EclipseNSISPlugin.getResourceString("symbols.name.text"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.value.text")}, //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.add.tooltip"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.edit.tooltip"), //$NON-NLS-1$
                                         EclipseNSISPlugin.getResourceString("symbols.remove.tooltip"), //$NON-NLS-1$
                                     addAdapter,editAdapter,removeAdapter, doubleClickListener,
                                     mover);
        return composite;
    }

    protected boolean performApply(NSISSettings settings)
    {
        if (getControl() != null) {
            mSettings.setSymbols((LinkedHashMap)mSymbols.getInput());
        }
        return true;
    }

    public void reset()
    {
        mSymbols.setInput(mSettings.getSymbols());
    }

    public void setDefaults()
    {
        mSymbols.setInput(mSettings.getDefaultSymbols());
    }

    private void addOrEditSymbol(Shell shell, String oldName, String oldValue)
    {
        Map map = (Map)mSymbols.getInput();
        NSISSymbolDialog dialog = new NSISSymbolDialog(shell,oldName, oldValue);
        Collection coll = new HashSet(map.keySet());
        coll.remove(oldName);
        dialog.setExistingSymbols(coll);
        if(dialog.open() == Window.OK) {
            String newName = dialog.getName();
            boolean dirty = false;
            if(!Common.isEmpty(oldName)) {
                if(!oldName.equals(newName)) {
                    map.remove(oldName);
                    dirty = true;
                }
            }
            else {
                dirty = true;
            }
            String newValue = dialog.getValue();
            if(!oldValue.equals(newValue)) {
                dirty = true;
            }
            map.put(newName,newValue);
            mSymbols.refresh(true);
            if(dirty) {
                fireChanged();
            }
        }
    }

    public boolean canEnableControls()
    {
        return true;
    }
}

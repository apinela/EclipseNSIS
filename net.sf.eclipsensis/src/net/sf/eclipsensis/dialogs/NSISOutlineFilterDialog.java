/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.editor.outline.NSISOutlineContentResources;
import net.sf.eclipsensis.help.NSISKeywords;
import net.sf.eclipsensis.viewer.CollectionContentProvider;
import net.sf.eclipsensis.viewer.CollectionLabelProvider;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class NSISOutlineFilterDialog extends StatusMessageDialog
{
    private static final int MAX_WIDTH = 300;
    private CheckboxTableViewer mViewer;
    private NSISOutlineContentResources mResources = NSISOutlineContentResources.getInstance();
    private List mFilteredTypes;

    public NSISOutlineFilterDialog(Shell parent, List filteredTypes)
    {
        super(parent);
        mFilteredTypes = filteredTypes;
        setHelpAvailable(false);
        setTitle(EclipseNSISPlugin.getResourceString("filter.dialog.title")); //$NON-NLS-1$
    }

    protected Point getInitialSize()
    {
        Point result = super.getInitialSize();
        if(result.x > MAX_WIDTH) {
            result.x = MAX_WIDTH;
        }
        return result;
    }

    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginWidth = layout.marginHeight = 0;
        composite.setLayout(layout);

        Label l = new Label(composite,SWT.NONE);
        l.setText(EclipseNSISPlugin.getResourceString("filter.dialog.caption")); //$NON-NLS-1$
        l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));

        Composite buttons = new Composite(parent,SWT.NONE);
        layout = new GridLayout(2,true);
        layout.marginWidth = layout.marginHeight = 0;
        buttons.setLayout(layout);
        buttons.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));

        final Table table = new Table(composite,SWT.CHECK | SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION | SWT.V_SCROLL);
        table.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        table.addListener(SWT.EraseItem, new Listener() {
            public void handleEvent(Event event) {
                event.detail &= ~SWT.SELECTED;
            }
        });

        mViewer = new CheckboxTableViewer(table);
        mViewer.setContentProvider(new CollectionContentProvider());
        mViewer.setLabelProvider(new CollectionLabelProvider() {
            public Image getColumnImage(Object element, int columnIndex)
            {
                if(element instanceof String && columnIndex == 0) {
                    return mResources.getImage((String)element);
                }
                return super.getColumnImage(element, columnIndex);
            }

            public String getColumnText(Object element, int columnIndex)
            {
                if(element instanceof String && columnIndex == 0) {
                    return mResources.getTypeName((String)element);
                }
                return super.getColumnText(element, columnIndex);
            }
        });
        mViewer.setComparator(new ViewerComparator());
        final List types = new ArrayList(mResources.getTypes());
        for (Iterator iter = types.iterator(); iter.hasNext();) {
            String type = (String)iter.next();
            if(mResources.isClosingType(type)) {
                iter.remove();
                continue;
            }
            String keyword = NSISKeywords.getInstance().getKeyword(type);
            if(!NSISKeywords.getInstance().isValidKeyword(keyword)) {
                iter.remove();
            }
        }
        mViewer.setInput(types);
        mViewer.setCheckedElements(mFilteredTypes.toArray());
        mViewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event)
            {
                String type = (String)event.getElement();
                if(event.getChecked()) {
                    mFilteredTypes.add(type);
                }
                else {
                    mFilteredTypes.remove(type);
                }
            }
        });

        Button b = new Button(buttons,SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("filter.dialog.select.all.label")); //$NON-NLS-1$
        b.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mFilteredTypes.addAll(types);
                mViewer.setAllChecked(true);
            }
        });

        b = new Button(buttons,SWT.PUSH);
        b.setText(EclipseNSISPlugin.getResourceString("filter.dialog.deselect.all.label")); //$NON-NLS-1$
        b.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
        b.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                mFilteredTypes.clear();
                mViewer.setAllChecked(false);
            }
        });

        return composite;
    }
}

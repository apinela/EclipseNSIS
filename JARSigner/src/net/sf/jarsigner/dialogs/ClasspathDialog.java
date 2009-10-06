/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.dialogs;

import java.util.*;
import java.util.List;

import net.sf.eclipsensis.utilities.util.Common;
import net.sf.jarsigner.JARSignerPlugin;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

public class ClasspathDialog extends Dialog
{
    private static final String FILE_SEPARATOR = System.getProperty("file.separator"); //$NON-NLS-1$

    private List<String> mClasspath = null;
    private TableViewer mTableViewer;

    public ClasspathDialog(Shell parentShell, List<String> classpath)
    {
        super(parentShell);
        mClasspath = new ArrayList<String>(classpath);
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(JARSignerPlugin.getResourceString("edit.classpath.label")); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite parent2 = (Composite)super.createDialogArea(parent);
        Composite composite = new Composite(parent2, SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Group group = new Group(composite,SWT.NONE);
        group.setText(JARSignerPlugin.getResourceString("classpath.entries.label")); //$NON-NLS-1$
        group.setLayout(new GridLayout(1,false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Control control = createControlContents(group);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = convertWidthInCharsToPixels(80);
        control.setLayoutData(gd);

        Dialog.applyDialogFont(composite);

        return parent2;
    }

    protected Control createControlContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        Dialog.applyDialogFont(composite);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.widthHint = Common.calculateControlSize(composite, 60, 0).x;
        composite.setLayoutData(gd);

        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite.setLayout(layout);

        Composite composite2 = new Composite(composite, SWT.NONE);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        gd.heightHint = convertHeightInCharsToPixels(20);
        composite2.setLayoutData(gd);

        layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        Table table = new Table(composite2, SWT.FULL_SELECTION | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        table.setLayoutData(gd);

        TableColumn[] columns = { new TableColumn(table, SWT.LEFT, 0) };
        columns[0].setText(JARSignerPlugin.getResourceString("file.folder.label")); //$NON-NLS-1$

        mTableViewer = new TableViewer(table);
        mTableViewer.setContentProvider(ArrayContentProvider.getInstance());
        mTableViewer.setLabelProvider(new LabelProvider());
        mTableViewer.setInput(mClasspath);

        composite2 = new Composite(composite2, SWT.NONE);
        gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
        gd.horizontalSpan = 1;
        composite2.setLayoutData(gd);

        layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        composite2.setLayout(layout);

        final Button addFileButton = new Button(composite2, SWT.PUSH);
        addFileButton.setText(JARSignerPlugin.getResourceString("add.file.label")); //$NON-NLS-1$
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        addFileButton.setLayoutData(gd);

        final Button addFolderButton = new Button(composite2, SWT.PUSH);
        addFolderButton.setText(JARSignerPlugin.getResourceString("add.folder.label")); //$NON-NLS-1$
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        addFolderButton.setLayoutData(gd);

        final Button removeButton = new Button(composite2, SWT.PUSH);
        removeButton.setText(JARSignerPlugin.getResourceString("remove.label")); //$NON-NLS-1$
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        removeButton.setLayoutData(gd);
        removeButton.setEnabled(false);

        final Button topButton = new Button(composite2, SWT.PUSH);
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        topButton.setLayoutData(gd);
        topButton.setText(JARSignerPlugin.getResourceString("move.to.top.label")); //$NON-NLS-1$
        topButton.setEnabled(canMoveUp());

        final Button upButton = new Button(composite2, SWT.PUSH);
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        upButton.setLayoutData(gd);
        upButton.setText(JARSignerPlugin.getResourceString("move.up.label")); //$NON-NLS-1$
        upButton.setEnabled(canMoveUp());

        final Button downButton = new Button(composite2, SWT.PUSH);
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        downButton.setLayoutData(gd);
        downButton.setText(JARSignerPlugin.getResourceString("move.down.label")); //$NON-NLS-1$
        downButton.setEnabled(canMoveDown());

        final Button bottomButton = new Button(composite2, SWT.PUSH);
        gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
        bottomButton.setLayoutData(gd);
        bottomButton.setText(JARSignerPlugin.getResourceString("move.to.bottom.label")); //$NON-NLS-1$
        bottomButton.setEnabled(canMoveDown());

        addFileButton.addSelectionListener(new SelectionAdapter() {
            String[] filterNames = {JARSignerPlugin.getResourceString("java.archives.label"),JARSignerPlugin.getResourceString("all.files.label")}; //$NON-NLS-1$ //$NON-NLS-2$
            String[] filters = {JARSignerPlugin.getResourceString("java.archives.filter"),JARSignerPlugin.getResourceString("all.files.filter")}; //$NON-NLS-1$ //$NON-NLS-2$
            String filterPath = ""; //$NON-NLS-1$

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                FileDialog dialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI | SWT.PRIMARY_MODAL);
                dialog.setText(JARSignerPlugin.getResourceString("choose.classpath.files.label")); //$NON-NLS-1$
                dialog.setFilterNames(filterNames);
                dialog.setFilterExtensions(filters);
                if (!Common.isEmpty(filterPath))
                {
                    dialog.setFilterPath(filterPath);
                }
                if (dialog.open() != null)
                {
                    filterPath = dialog.getFilterPath();
                    String[] fileNames = dialog.getFileNames();
                    for (int i = 0; i < fileNames.length; i++)
                    {
                        StringBuilder buf = new StringBuilder(filterPath);
                        if(!filterPath.endsWith(FILE_SEPARATOR))
                        {
                            buf.append(FILE_SEPARATOR);
                        }
                        buf.append(fileNames[i]);
                        String file = buf.toString();
                        if (!mClasspath.contains(file))
                        {
                            mClasspath.add(file);
                        }
                    }
                    mTableViewer.refresh();
                }
            }
        });

        addFolderButton.addSelectionListener(new SelectionAdapter() {
            String filterPath = ""; //$NON-NLS-1$

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
                dialog.setFilterPath(filterPath);
                String directory = dialog.open();
                if (!Common.isEmpty(directory)) {
                    filterPath = directory;
                    if(!mClasspath.contains(directory))
                    {
                        mClasspath.add(directory);
                        mTableViewer.refresh();
                    }
                }
            }
        });

        removeButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                IStructuredSelection selection = (IStructuredSelection) mTableViewer.getSelection();
                for (Iterator<?> iter = selection.iterator(); iter.hasNext();)
                {
                    mClasspath.remove(iter.next());
                }
                mTableViewer.refresh();
            }
        });

        topButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                moveToTop();
            }
        });

        upButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                moveUp();
            }
        });

        downButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                moveDown();
            }
        });

        bottomButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent se)
            {
                moveToBottom();
            }
        });

        mTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                removeButton.setEnabled((selection != null && !selection.isEmpty()));
                topButton.setEnabled(canMoveUp());
                upButton.setEnabled(canMoveUp());
                downButton.setEnabled(canMoveDown());
                bottomButton.setEnabled(canMoveDown());
            }
        });
        table.addControlListener(new ControlAdapter() {
            @Override
            public void controlResized(ControlEvent e)
            {
                Table table = (Table)e.widget;
                int width = table.getClientArea().width;
                if((table.getStyle() & SWT.V_SCROLL) > 0) {
                    width -= table.getVerticalBar().getSize().x;
                }
                int lineWidth = table.getGridLineWidth();
                TableColumn[] columns = table.getColumns();
                if(!Common.isEmptyArray(columns)) {
                    int n = columns.length-1;
                    width -= n*lineWidth;

                    int[] minWidths = null;
                    final boolean headerVisible = table.getHeaderVisible();
                    if(headerVisible) {
                        GC gc = new GC(table);
                        minWidths = new int[columns.length];
                        for (int i = 0; i < columns.length; i++) {
                            minWidths[i] = gc.stringExtent(columns[i].getText()).x+16;
                            if(table.getSortColumn() == columns[i] && table.getSortDirection() != SWT.NONE) {
                                minWidths[i] += 26;
                            }
                        }
                        gc.dispose();
                    }

                    int sumWidth = 0;
                    for(int i=0; i<n; i++) {
                        int width2 = width;
                        if(headerVisible && minWidths != null) {
                            width2 = Math.max(minWidths[i], width2);
                        }
                        sumWidth += width2;
                        columns[i].setWidth(width2);
                    }
                    width = width-sumWidth;
                    if(headerVisible && minWidths != null) {
                        width = Math.max(width, minWidths[n]);
                    }
                    columns[n].setWidth(width);
                    table.redraw();
                }
            }
        });

        return composite;
    }

    public java.util.List<String> getClasspath()
    {
        return mClasspath;
    }

    private boolean canMoveUp()
    {
        int[] selectedIndices = mTableViewer.getTable().getSelectionIndices();
        if(!Common.isEmptyArray(selectedIndices)) {
            for (int i= 0; i < selectedIndices.length; i++) {
                if (selectedIndices[i] != i) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean canMoveDown()
    {
        int[] selectedIndices = mTableViewer.getTable().getSelectionIndices();
        int size = mClasspath.size();
        if(!Common.isEmptyArray(selectedIndices) && size > 1) {
            int k= size - 1;
            for (int i= selectedIndices.length - 1; i >= 0 ; i--, k--) {
                if (selectedIndices[i] != k) {
                    return true;
                }
            }
        }
        return false;
    }

    private void moveToTop()
    {
        List<String> move = getMoveElements();
        List<String> elements = new ArrayList<String>(mClasspath);
        elements.removeAll(move);
        elements.addAll(0,move);
        updateElements(elements, move, false);
    }

    private void moveToBottom()
    {
        List<String> move = getMoveElements();
        List<String> elements = new ArrayList<String>(mClasspath);
        elements.removeAll(move);
        elements.addAll(move);
        updateElements(elements, move, true);
    }

    private void moveDown()
    {
        List<String> move = getMoveElements();
        Collections.reverse(mClasspath);
        mClasspath = move(mClasspath,move);
        Collections.reverse(mClasspath);
        updateElements(mClasspath, move, true);
    }

    private void moveUp()
    {
        List<String> move = getMoveElements();
        updateElements(move(mClasspath, move), move, false);
    }

    private List<String> move(List<String> elements, List<String> move)
    {
        int size= elements.size();
        List<String> res= new ArrayList<String>(size);
        String floating= null;
        for (int i= 0; i < size; i++) {
            String curr= elements.get(i);
            if (move.contains(curr)) {
                res.add(curr);
            } else {
                if (floating != null) {
                    res.add(floating);
                }
                floating= curr;
            }
        }
        if (floating != null) {
            res.add(floating);
        }
        return res;
    }

    private List<String> getMoveElements()
    {
        List<String> moveElements = new ArrayList<String>();
        if(!Common.isEmptyCollection(mClasspath)) {
            int[] selectedIndices = mTableViewer.getTable().getSelectionIndices();

            if(!Common.isEmptyArray(selectedIndices)) {
                for (int i = 0; i < selectedIndices.length; i++) {
                    moveElements.add(mClasspath.get(selectedIndices[i]));
                }
            }
        }

        return moveElements;
    }

    private void updateElements(List<String> elements, List<String> move, boolean isDown)
    {
        mClasspath.clear();
        mClasspath.addAll(elements);
        mTableViewer.refresh();
        if(!Common.isEmptyCollection(move)) {
            mTableViewer.setSelection(new StructuredSelection(move));
            mTableViewer.reveal(move.get(isDown?move.size()-1:0));
        }
    }
}

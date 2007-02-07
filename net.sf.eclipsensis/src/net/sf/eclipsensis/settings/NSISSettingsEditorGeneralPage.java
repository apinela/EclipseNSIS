/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
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
import net.sf.eclipsensis.dialogs.NSISInstructionDialog;
import net.sf.eclipsensis.makensis.MakeNSISRunner;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.viewer.*;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public abstract class NSISSettingsEditorGeneralPage extends NSISSettingsEditorPage
{
    private static final String LABELS = "LABELS";

    protected Button mHdrInfo = null;
    protected Button mLicense = null;
    protected Button mNoConfig = null;
    protected Button mNoCD = null;
    protected Combo mVerbosity = null;
    protected Combo mProcessPriority = null;
    protected Combo mCompressor = null;
    protected Button mSolidCompression = null;
    protected TableViewer mInstructions = null;

    private Group mGroup = null;

    public NSISSettingsEditorGeneralPage(NSISSettings settings)
    {
        super(settings);
    }

    protected Control createControl(Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        GridLayout layout = new GridLayout(1,false);
        composite.setLayout(layout);

        Composite child = createMasterControl(composite);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        child.setLayoutData(data);

        mGroup = createNSISOptionsGroup(composite);
        data = new GridData(SWT.FILL, SWT.FILL, true, false);
        mGroup.setLayoutData(data);

        createInstructionsViewer(composite);
        return composite;
    }

    public void enableControls(boolean state)
    {
        if(mGroup != null) {
            enableComposite(mGroup,state);
            if(state) {
                //Hack to properly enable the buttons
                setSolidCompressionState();
            }
        }
        if(mInstructions != null) {
            enableComposite(mInstructions.getControl().getParent(),state);
            if(state) {
                //Hack to properly enable the buttons
                mInstructions.setSelection(mInstructions.getSelection());
            }
        }
    }

    private Group createNSISOptionsGroup(Composite parent)
    {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(EclipseNSISPlugin.getResourceString("nsis.options.group.text")); //$NON-NLS-1$
        GridLayout layout = new GridLayout(2,false);
        group.setLayout(layout);

        mHdrInfo = createCheckBox(group, EclipseNSISPlugin.getResourceString("hdrinfo.text"), //$NON-NLS-1$
                                  EclipseNSISPlugin.getResourceString("hdrinfo.tooltip"), //$NON-NLS-1$
                                  mSettings.getHdrInfo());

        mLicense = createCheckBox(group, EclipseNSISPlugin.getResourceString("license.text"), //$NON-NLS-1$
                                  EclipseNSISPlugin.getResourceString("license.tooltip"), //$NON-NLS-1$
                                  mSettings.getLicense());

        mNoConfig = createCheckBox(group, EclipseNSISPlugin.getResourceString("noconfig.text"), //$NON-NLS-1$
                                   EclipseNSISPlugin.getResourceString("noconfig.tooltip"), //$NON-NLS-1$
                                   mSettings.getNoConfig());

        mNoCD = createCheckBox(group, EclipseNSISPlugin.getResourceString("nocd.text"), //$NON-NLS-1$
                               EclipseNSISPlugin.getResourceString("nocd.tooltip"), //$NON-NLS-1$
                               mSettings.getNoCD());

        Composite composite  = new Composite(group,SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.horizontalSpan = 2;
        composite.setLayoutData(data);

        layout = new GridLayout(3, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        mVerbosity = createCombo(composite,EclipseNSISPlugin.getResourceString("verbosity.text"),EclipseNSISPlugin.getResourceString("verbosity.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                                 INSISSettingsConstants.VERBOSITY_ARRAY, mSettings.getVerbosity()+1);
        Label l = new Label(composite,SWT.None);
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        mCompressor = createCombo(composite,EclipseNSISPlugin.getResourceString("compressor.text"),EclipseNSISPlugin.getResourceString("compressor.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                                 MakeNSISRunner.COMPRESSOR_DISPLAY_ARRAY,mSettings.getCompressor());
        mSolidCompression = createCheckBox(composite, EclipseNSISPlugin.getResourceString("solid.compression.text"), //$NON-NLS-1$
                                          EclipseNSISPlugin.getResourceString("solid.compression.tooltip"), //$NON-NLS-1$
                                          mSettings.getSolidCompression());
        mSolidCompression.setVisible(NSISPreferences.INSTANCE.isSolidCompressionSupported());
        mCompressor.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e)
            {
                setSolidCompressionState();
            }
        });

        mProcessPriority = createCombo(composite,EclipseNSISPlugin.getResourceString("process.priority.text"),EclipseNSISPlugin.getResourceString("process.priority.tooltip"), //$NON-NLS-1$ //$NON-NLS-2$
                INSISSettingsConstants.PROCESS_PRIORITY_ARRAY, mSettings.getProcessPriority()+1);
        l = new Label(composite,SWT.None);
        l.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        mProcessPriority.setData(LABELS,new Object[] {mProcessPriority.getData(LABEL),l});
        internalSetProcessPriorityVisible(NSISPreferences.INSTANCE.isProcessPrioritySupported());
        return group;
    }

    protected void setProcessPriorityVisible(boolean visible)
    {
        if(mProcessPriority != null && !mProcessPriority.isDisposed()) {
            boolean oldVisible = mProcessPriority.isVisible();
            if(oldVisible != visible) {
                internalSetProcessPriorityVisible(visible);
                mProcessPriority.getShell().layout(new Control[] {mProcessPriority.getParent()});
            }
        }
    }

    /**
     * @param visible
     */
    private void internalSetProcessPriorityVisible(boolean visible)
    {
        mProcessPriority.setVisible(visible);
        ((GridData)mProcessPriority.getLayoutData()).exclude = !visible;
        Object[] data = (Object[])mProcessPriority.getData(LABELS);
        if(!Common.isEmptyArray(data)) {
            for (int i = 0; i < data.length; i++) {
                if(data[i] instanceof Control && !((Control)data[i]).isDisposed()) {
                    ((Control)data[i]).setVisible(visible);
                    ((GridData)((Control)data[i]).getLayoutData()).exclude = !visible;
                }
            }
        }
    }

    private void setSolidCompressionState()
    {
        int n = mCompressor.getSelectionIndex();
        mSolidCompression.setEnabled(n != MakeNSISRunner.COMPRESSOR_DEFAULT && n != MakeNSISRunner.COMPRESSOR_BEST);
    }

    private Control createInstructionsViewer(final Composite parent)
    {
        Composite composite = new Composite(parent,SWT.NONE);
        SelectionAdapter addAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditInstruction(parent.getShell(),""); //$NON-NLS-1$
            }
        };
        SelectionAdapter editAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                addOrEditInstruction(parent.getShell(),((String)((IStructuredSelection)mInstructions.getSelection()).getFirstElement()).trim());
            }
        };
        SelectionAdapter removeAdapter = new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e)
            {
                Collection collection = (Collection)mInstructions.getInput();
                IStructuredSelection selection = (IStructuredSelection)mInstructions.getSelection();
                for(Iterator iter = selection.iterator(); iter.hasNext(); ) {
                    collection.remove(iter.next());
                    fireChanged();
                }
                mInstructions.refresh();
            }
        };

        TableViewerUpDownMover mover = new TableViewerUpDownMover() {

            protected List getAllElements()
            {
                return (ArrayList)((TableViewer)getViewer()).getInput();
            }

            protected void updateStructuredViewerInput(Object input, List elements, List move, boolean isDown)
            {
                ((ArrayList)input).clear();
                ((ArrayList)input).addAll(elements);
                fireChanged();
            }
        };

        IDoubleClickListener doubleClickListener = new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event)
            {

                addOrEditInstruction(parent.getShell(),((String)((IStructuredSelection)event.getSelection()).getFirstElement()).trim());
            }
        };

        mInstructions = createTableViewer(composite, mSettings.getInstructions(),
                                      new CollectionContentProvider(), new CollectionLabelProvider(),
                                      EclipseNSISPlugin.getResourceString("instructions.description"), //$NON-NLS-1$
                                      new String[]{EclipseNSISPlugin.getResourceString("instructions.instruction.text")}, //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.add.tooltip"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.edit.tooltip"), //$NON-NLS-1$
                                      EclipseNSISPlugin.getResourceString("instructions.remove.tooltip"), //$NON-NLS-1$
                                      addAdapter,editAdapter,removeAdapter, doubleClickListener, mover);
        ((GridLayout)composite.getLayout()).marginWidth = 0;
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(data);

        return composite;
    }

    protected boolean performApply(NSISSettings settings)
    {
        if(getControl() != null) {
            settings.setHdrInfo(mHdrInfo.getSelection());
            settings.setLicense(mLicense.getSelection());
            settings.setNoConfig(mNoConfig.getSelection());
            settings.setNoCD(mNoCD.getSelection());
            settings.setVerbosity(mVerbosity.getSelectionIndex()-1);
            settings.setCompressor(mCompressor.getSelectionIndex());
            settings.setSolidCompression(mSolidCompression.getSelection());
            if (NSISPreferences.INSTANCE.isProcessPrioritySupported()) {
                settings.setProcessPriority(mProcessPriority.getSelectionIndex()-1);
            }
            settings.setInstructions((ArrayList)mInstructions.getInput());
        }
        return true;
    }

    public void reset()
    {
        mHdrInfo.setSelection(mSettings.getHdrInfo());
        mLicense.setSelection(mSettings.getLicense());
        mNoConfig.setSelection(mSettings.getNoConfig());
        mNoCD.setSelection(mSettings.getNoCD());
        mVerbosity.select(mSettings.getVerbosity()+1);
        mCompressor.select(mSettings.getCompressor());
        mSolidCompression.setSelection(mSettings.getSolidCompression());
        if (NSISPreferences.INSTANCE.isProcessPrioritySupported()) {
            mProcessPriority.select(mSettings.getProcessPriority()+1);
        }
        mInstructions.setInput(mSettings.getInstructions());
    }

    public void setDefaults()
    {
        mHdrInfo.setSelection(mSettings.getDefaultHdrInfo());
        mLicense.setSelection(mSettings.getDefaultLicense());
        mNoConfig.setSelection(mSettings.getDefaultNoConfig());
        mNoCD.setSelection(mSettings.getDefaultNoCD());
        mVerbosity.select(mSettings.getDefaultVerbosity()+1);
        mCompressor.select(mSettings.getDefaultCompressor());
        mSolidCompression.setSelection(mSettings.getDefaultSolidCompression());
        if (NSISPreferences.INSTANCE.isProcessPrioritySupported()) {
            mProcessPriority.select(mSettings.getDefaultProcessPriority()+1);
        }
        mInstructions.setInput(mSettings.getDefaultInstructions());
    }

    private void addOrEditInstruction(Shell shell, String oldInstruction)
    {
        NSISInstructionDialog dialog = new NSISInstructionDialog(shell,oldInstruction);
        if(dialog.open() == Window.OK) {
            String newInstruction = dialog.getInstruction();
            Collection collection = (Collection)mInstructions.getInput();
            if(!Common.isEmpty(oldInstruction)) {
                if(!oldInstruction.equals(newInstruction)) {
                    collection.remove(oldInstruction);
                    fireChanged();
                }
            }
            else {
                fireChanged();
            }
            collection.add(newInstruction);
            mInstructions.refresh(true);
        }
    }

    protected abstract Composite createMasterControl(Composite parent);
}

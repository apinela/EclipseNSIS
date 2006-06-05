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

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class MinimalProgressMonitorDialog extends ProgressMonitorDialog
{
    public static final int MINIMUM_WIDTH = 500;
    public static final int MAXIMUM_WIDTH = 800;
    public static final int VERTICAL_OFFSET = 85;

    private static final int BAR_DLUS = 9;

    private int mMinimumWidth;
    private int mMaximumWidth;
    
    private String mCaption = EclipseNSISPlugin.getDefault().getName();

    /**
     * Construct an instance of this dialog.
     *
     * @param parent
     */
    public MinimalProgressMonitorDialog(Shell parent)
    {
        this(parent, MINIMUM_WIDTH, MAXIMUM_WIDTH);
    }

    public MinimalProgressMonitorDialog(Shell parent, int minimumWidth, int maximumWidth)
    {
        super(parent);
        mMaximumWidth = maximumWidth;
        mMinimumWidth = minimumWidth;
        setShellStyle(SWT.NONE);
    }

    public String getCaption()
    {
        return mCaption;
    }

    public void setCaption(String caption)
    {
        mCaption = caption;
    }

    protected Control createContents(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        container.setLayoutData(gridData);
        GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        container.setLayout(gridLayout);

        Composite progressArea = new Composite(container, SWT.NONE);
        initializeDialogUnits(progressArea);
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 5;//convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
        layout.marginHeight = gridLayout.marginWidth;
        layout.verticalSpacing = 2;//convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
        layout.horizontalSpacing = 2;//convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING) * 2;
        progressArea.setLayout(layout);
        progressArea.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
        createDialogAndButtonArea(progressArea);

        return container;
    }

    protected Image getImage()
    {
        return null;
    }

    protected Control createMessageArea(Composite composite) 
    {
        // create message
        if (message != null) {
            messageLabel = new Label(composite, SWT.NONE);
            messageLabel.setText(message);
            GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
            data.horizontalSpan = 2;
            data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
            messageLabel.setLayoutData(data);
            taskLabel = messageLabel;
        }
        return composite;
    }

    protected Control createDialogArea(Composite parent)
    {
        // task label
        message = "";
        createMessageArea(parent);
        
        // progress indicator
        progressIndicator = new ProgressIndicator(parent);
        GridData gd = new GridData(SWT.FILL,SWT.CENTER,true,false);
        gd.heightHint = convertVerticalDLUsToPixels(BAR_DLUS);
        gd.horizontalSpan = 2;
        progressIndicator.setLayoutData(gd);

        // label showing sub task
        subTaskLabel = new Label(parent, SWT.LEFT);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.minimumWidth = mMinimumWidth / 2;
        subTaskLabel.setLayoutData(gd);
        subTaskLabel.setFont(parent.getFont());

        Label label = new Label(parent, SWT.RIGHT);
        label.moveBelow(subTaskLabel);
        gd = new GridData(SWT.RIGHT);
        label.setLayoutData(gd);
        label.setFont(parent.getFont());
        label.setText(mCaption);
        return parent;
    }

    /*
     * see org.eclipse.jface.Window.getInitialLocation()
     */
    protected Point getInitialLocation(Point initialSize) {
        Composite parent = getShell().getParent();

        if (parent == null) {
            return super.getInitialLocation(initialSize);
        }

        Rectangle bounds = null;
        IWorkbench workbench = PlatformUI.getWorkbench();
        if(workbench != null) {
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            if(window != null && window.getShell().isVisible()) {
                bounds = window.getShell().getBounds();
            }
        }
        if(bounds == null) {
            Monitor monitor = parent.getMonitor();
            bounds = monitor.getBounds();
        }
        Point center = Geometry.centerPoint(bounds);

        return new Point(center.x - (initialSize.x / 2),
                Math.max(bounds.y, Math.min(center.y +VERTICAL_OFFSET, bounds.y+ bounds.height - initialSize.y)));
    }

    protected Point getInitialSize()
    {
        Point calculatedSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT,true);
        if (calculatedSize.x < mMinimumWidth) {
            calculatedSize.x = mMinimumWidth;
        }
        if (calculatedSize.x > mMaximumWidth) {
            calculatedSize.x = mMaximumWidth;
        }
        return calculatedSize;
    }

    protected Control createButtonBar(Composite parent)
    {
        return null;
    }
}

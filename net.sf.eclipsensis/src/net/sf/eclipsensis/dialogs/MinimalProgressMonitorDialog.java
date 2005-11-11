/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.dialogs;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;

public class MinimalProgressMonitorDialog extends ProgressMonitorDialog
{
    private static final int MINIMUM_WIDTH = 500;
    private static final int BAR_DLUS = 9;
    private static final int VERTICAL_OFFSET = 85;

    private String mCaption = EclipseNSISPlugin.getDefault().getName();

    /**
     * Construct an instance of this dialog.
     *
     * @param parent
     */
    public MinimalProgressMonitorDialog(Shell parent)
    {
        super(parent);
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
        super.createContents(progressArea);

        // making the margins the same in each direction
        gridLayout = (GridLayout) progressArea.getLayout();
        gridLayout.marginHeight = gridLayout.marginWidth;

        gridData = (GridData) progressArea.getLayoutData();
        gridData.verticalAlignment = SWT.CENTER;

        return container;
    }

    protected Image getImage()
    {
        return null;
    }

    protected Control createDialogArea(Composite parent)
    {
        // progress indicator
        progressIndicator = new ProgressIndicator(parent);
        GridData gd = new GridData(SWT.FILL,SWT.CENTER,true,false);
        gd.heightHint = convertVerticalDLUsToPixels(BAR_DLUS);
        gd.horizontalSpan = 2;
        progressIndicator.setLayoutData(gd);

        // label showing current task
        subTaskLabel = new Label(parent, SWT.LEFT);
        gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd.minimumWidth = MINIMUM_WIDTH / 2;
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
        if (calculatedSize.x < MINIMUM_WIDTH) {
            calculatedSize.x = MINIMUM_WIDTH;
        }
        return calculatedSize;
    }

    protected Control createButtonBar(Composite parent)
    {
        return null;
    }
}

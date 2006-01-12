/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit.dialog;

import java.util.*;

import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.figures.IInstallOptionsFigure;

import org.eclipse.draw2d.*;
import org.eclipse.draw2d.geometry.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

public class InstallOptionsDialogLayer extends FreeformLayer implements IInstallOptionsConstants
{
    private List mChildren = new ArrayList();
    private Dimension mDialogSize = new Dimension(100,100);
    private boolean mShowDialogSize = false;

    private Dimension dialogUnitsToPixels(Dimension d)
    {
        Font f = Display.getDefault().getSystemFont();
        return FigureUtility.dialogUnitsToPixels(d,f);
    }

    protected void paintFigure(Graphics graphics)
    {
        super.paintFigure(graphics);
        if(mShowDialogSize && !mDialogSize.equals(0,0)) {
            graphics.pushState();
            graphics.setForegroundColor(ColorConstants.blue);
            Dimension d = dialogUnitsToPixels(mDialogSize);
            graphics.drawRectangle(0,0,d.width,d.height);
            graphics.popState();
            graphics.restoreState();
        }
    }

    public void add(IFigure child, Object constraint, int index)
    {
        mChildren.add(child);
        super.add(child, constraint, index);
    }

    public void remove(IFigure child)
    {
        if ((child.getParent() == this) && getChildren().contains(child)) {
            mChildren.remove(child);
        }
        super.remove(child);
    }

    protected void paintChildren(Graphics graphics)
    {
        IFigure child;

        Rectangle clip = Rectangle.SINGLETON;
        for (Iterator iter = mChildren.iterator(); iter.hasNext(); ) {
            child = (IFigure)iter.next();
            if (child.isVisible() && child.intersects(graphics.getClip(clip))) {
                graphics.pushState();
                graphics.clipRect(child.getBounds());
                child.paint(graphics);
                graphics.popState();
                graphics.restoreState();
            }
        }
    }

    public Dimension getDialogSize()
    {
        return mDialogSize;
    }

    public void setDialogSize(Dimension size)
    {
        if(!mDialogSize.equals(size)) {
            mDialogSize = size;
            repaint();
        }
    }

    public boolean isShowDialogSize()
    {
        return mShowDialogSize;
    }

    public void setShowDialogSize(boolean showDialogSize)
    {
        if(mShowDialogSize != showDialogSize) {
            mShowDialogSize = showDialogSize;
            repaint();
        }
    }

    public IFigure findFigureAt(int x, int y, TreeSearch search)
    {
        IFigure figure = super.findFigureAt(x, y, search);
        if(figure instanceof IInstallOptionsFigure) {
            IInstallOptionsFigure ioFigure = ((IInstallOptionsFigure)figure);
            if(ioFigure.isClickThrough()) {
                Point p = new Point(x,y);
                translateToAbsolute(p);
                Point p2 = new Point();

                p2.setLocation(p);
                ioFigure.translateToRelative(p2);
                if(!ioFigure.hitTest(p2.x,p2.y)) {
                    figure = findFigureAt(x, y, new WrappedExclusionSearch(search, Collections.singleton(ioFigure)));
                    if(figure == null || figure == this) {
                        if(ioFigure.isDefaultClickThroughFigure()) {
                            figure = ioFigure;
                        }
                        else {
                            figure = null;
                        }
                    }
                }
            }
        }
        return figure;
    }

    private class WrappedExclusionSearch extends ExclusionSearch
    {
        private TreeSearch mDelegate;

        public WrappedExclusionSearch(TreeSearch delegate, Collection exclusions)
        {
            super(exclusions);
            mDelegate = delegate;
        }

        public boolean prune(IFigure f)
        {
            if(!mDelegate.prune(f)) {
                return super.prune(f);
            }
            return true;
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.edit;

import java.util.List;

import org.eclipse.draw2d.Locator;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.gef.*;
import org.eclipse.gef.handles.MoveHandle;
import org.eclipse.gef.handles.ResizeHandle;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.swt.graphics.Cursor;

public class InstallOptionsHandleKit
{
    public static void addResizableHandle(GraphicalEditPart part, List handles, int direction) 
    {
        handles.add(createResizableHandle(part, direction)); 
    }

    public static void addResizableHandle(GraphicalEditPart part, List handles, int direction, 
                                 DragTracker tracker, Cursor cursor) 
    {
        handles.add(createResizableHandle(part, direction, tracker, cursor));    
    }
        
    public static void addResizableHandles(GraphicalEditPart part, List handles) 
    {
        addMoveHandle(part, handles);
        handles.add(createResizableHandle(part, PositionConstants.EAST));
        handles.add(createResizableHandle(part, PositionConstants.SOUTH_EAST));
        handles.add(createResizableHandle(part, PositionConstants.SOUTH));
        handles.add(createResizableHandle(part, PositionConstants.SOUTH_WEST));
        handles.add(createResizableHandle(part, PositionConstants.WEST));
        handles.add(createResizableHandle(part, PositionConstants.NORTH_WEST));
        handles.add(createResizableHandle(part, PositionConstants.NORTH));
        handles.add(createResizableHandle(part, PositionConstants.NORTH_EAST));
    }

    static Handle createResizableHandle(GraphicalEditPart owner, int direction) 
    {
        ResizeHandle handle = new InstallOptionsResizeHandle(owner, direction);
        return handle;
    }

    static Handle createResizableHandle(GraphicalEditPart owner, int direction, DragTracker tracker,
                               Cursor cursor) 
    {
        ResizeHandle handle = new InstallOptionsResizeHandle(owner, direction);
        handle.setDragTracker(tracker);
        handle.setCursor(cursor);
        return handle;
    }

    public static void addCornerHandles(GraphicalEditPart part, List handles,
            DragTracker tracker, Cursor cursor)
    {
        handles.add(createNonResizableHandle(part, PositionConstants.SOUTH_EAST, tracker, cursor));
        handles.add(createNonResizableHandle(part, PositionConstants.SOUTH_WEST, tracker, cursor));
        handles.add(createNonResizableHandle(part, PositionConstants.NORTH_WEST, tracker, cursor));
        handles.add(createNonResizableHandle(part, PositionConstants.NORTH_EAST, tracker, cursor));
    }

    public static void addCornerHandles(GraphicalEditPart part, List handles)
    {
        handles.add(createNonResizableHandle(part, PositionConstants.SOUTH_EAST));
        handles.add(createNonResizableHandle(part, PositionConstants.SOUTH_WEST));
        handles.add(createNonResizableHandle(part, PositionConstants.NORTH_WEST));
        handles.add(createNonResizableHandle(part, PositionConstants.NORTH_EAST));
    }

    public static void addNonResizableHandle(GraphicalEditPart part, List handles, int direction)
    {
        handles.add(createNonResizableHandle(part, direction));
    }

    public static void addNonResizableHandle(GraphicalEditPart part, List handles, int direction, DragTracker tracker, Cursor cursor)
    {
        handles.add(createNonResizableHandle(part, direction, tracker, cursor));
    }

    public static void addNonResizableHandles(GraphicalEditPart part, List handles)
    {
        addMoveHandle(part, handles);
        addCornerHandles(part, handles);
    }

    public static void addNonResizableHandles(GraphicalEditPart part, List handles, DragTracker tracker, Cursor cursor)
    {
        addMoveHandle(part, handles, tracker, cursor);
        addCornerHandles(part, handles, tracker, cursor);
    }

    static Handle createNonResizableHandle(GraphicalEditPart owner, int direction)
    {
        ResizeHandle handle = new InstallOptionsResizeHandle(owner, direction);
        handle.setCursor(SharedCursors.SIZEALL);
        handle.setDragTracker(new DragEditPartsTracker(owner));
        return handle;
    }

    static Handle createNonResizableHandle(GraphicalEditPart owner, int direction, DragTracker tracker, Cursor cursor)
    {
        ResizeHandle handle = new InstallOptionsResizeHandle(owner, direction);
        handle.setCursor(cursor);
        handle.setDragTracker(tracker);
        return handle;
    }

    public static void addMoveHandle(GraphicalEditPart f, List handles) 
    {
        handles.add(moveHandle(f));
    }

    public static void addMoveHandle(GraphicalEditPart f, List handles, DragTracker tracker,
                                     Cursor cursor) 
    {
        handles.add(moveHandle(f, tracker, cursor));
    }

    public static Handle moveHandle(GraphicalEditPart owner) 
    {
        return new InstallOptionsMoveHandle(owner);
    }

    public static Handle moveHandle(GraphicalEditPart owner, DragTracker tracker, 
                                    Cursor cursor) 
    {
        MoveHandle moveHandle = new InstallOptionsMoveHandle(owner);
        moveHandle.setDragTracker(tracker);
        moveHandle.setCursor(cursor);
        return moveHandle;      
    }
    
    private static class InstallOptionsMoveHandle extends MoveHandle
    {
        public InstallOptionsMoveHandle(GraphicalEditPart owner)
        {
            super(owner);
        }
        
        public InstallOptionsMoveHandle(GraphicalEditPart owner, Locator loc)
        {
            super(owner, loc);
        }

        public Cursor getCursor()
        {
            if(((InstallOptionsEditDomain)getOwner().getViewer().getEditDomain()).isReadOnly()) {
                return null;
            }
            else {
                return super.getCursor();
            }
        }
    }
    
    private static class InstallOptionsResizeHandle extends ResizeHandle
    {

        public InstallOptionsResizeHandle(GraphicalEditPart owner, int direction)
        {
            super(owner, direction);
        }

        public InstallOptionsResizeHandle(GraphicalEditPart owner, Locator loc, Cursor c)
        {
            super(owner, loc, c);
        }

        public Cursor getCursor()
        {
            if(((InstallOptionsEditDomain)getOwner().getViewer().getEditDomain()).isReadOnly()) {
                return null;
            }
            else {
                return super.getCursor();
            }
        }
    }
}
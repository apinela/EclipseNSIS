/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class UpDownMover
{
    public boolean canMoveUp()
    {
        int[] selectedIndices = getSelectedIndices();
        if(!Common.isEmptyArray(selectedIndices)) {
            for (int i= 0; i < selectedIndices.length; i++) {
                if (selectedIndices[i] != i) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean canMoveDown()
    {
        int[] selectedIndices = getSelectedIndices();
        int size = getSize();
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

    public void moveDown() 
    {
        List elements = getAllElements();
        List move = getMoveElements();
        Collections.reverse(elements);
        elements = move(elements,move);
        Collections.reverse(elements);
        updateElements(elements, move, true);
    }

    public void moveUp() 
    {
        List move = getMoveElements();
        updateElements(move(getAllElements(), move), move, false);
    }
    
    private List move(List elements, List move) 
    {
        int nElements= elements.size();
        List res= new ArrayList(nElements);
        Object floating= null;
        for (int i= 0; i < nElements; i++) {
            Object curr= elements.get(i);
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
    
    public abstract void setInput(Object input);
    public abstract Object getInput();
    protected abstract int[] getSelectedIndices();
    protected abstract int getSize();
    protected abstract List getAllElements();
    protected abstract List getMoveElements();
    protected abstract void updateElements(List elements, List move, boolean isDown);
}

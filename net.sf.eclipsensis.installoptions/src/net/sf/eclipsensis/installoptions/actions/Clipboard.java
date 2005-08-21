/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.beans.*;

import net.sf.eclipsensis.installoptions.dnd.InstallOptionsObjectTransfer;

import org.eclipse.swt.dnd.Transfer;

public class Clipboard
{
    /**
     * The event name used for {@link Clipboard#fireContentsSet()}
     */
    public static final String CONTENTS_SET_EVENT = "ContentsSet"; //$NON-NLS-1$

    protected static Clipboard cInstance = new Clipboard();
       
    private static final InstallOptionsObjectTransfer TRANSFER = new InstallOptionsObjectTransfer() {
        private final String TYPE_NAME = "net.sf.eclipsensis.clipboard.transfer"; //$NON-NLS-1$
        private final int TYPE_ID = registerType(TYPE_NAME);
        protected int[] getTypeIds() {
            return new int[] {TYPE_ID};
        }
        protected String[] getTypeNames() {
            return new String[] {TYPE_NAME};
        }
    };
    private PropertyChangeSupport mListeners = new PropertyChangeSupport(this );

    /**
     * Do not allow direct instantiation of a Clipboard
     */
    private Clipboard() 
    {
        super();
    }

    /**
     * Get the default Clipboard
     * @return - The default Clipboard
     */
    public static Clipboard getDefault() 
    {
        return cInstance;
    }
   
    /**
     * Add a {@link PropertyChangeListener} to this Clipboard
     * @param l
     */
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        mListeners.addPropertyChangeListener(l);
    }

    /**
     * Remove a {@link PropertyChangeListener} to this Clipboard
     * @param l
     */
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        mListeners.removePropertyChangeListener(l);
    }

    /**
     * Fires a {@link PropertyChangeEvent} anytime the contents of the 
<code>Clipboard</code> are set.
     *
     */
    protected void fireContentsSet() 
    {
        PropertyChangeEvent event = new PropertyChangeEvent(this, CONTENTS_SET_EVENT, null, getContents() );
        mListeners.firePropertyChange( event );
    }
   
    public Object getContents() 
    {
        org.eclipse.swt.dnd.Clipboard cb = new org.eclipse.swt.dnd.Clipboard(null);
        Object contents = cb.getContents(TRANSFER);
        cb.dispose();
        return contents;
    }

    public void setContents(Object contents) 
    {
        org.eclipse.swt.dnd.Clipboard cb = new org.eclipse.swt.dnd.Clipboard(null);
        if(contents != null) {
            cb.setContents(new Object[] {contents}, new Transfer[] {TRANSFER});
        }
        else {
            cb.clearContents();
        }
        cb.dispose();
        fireContentsSet();
    }
}



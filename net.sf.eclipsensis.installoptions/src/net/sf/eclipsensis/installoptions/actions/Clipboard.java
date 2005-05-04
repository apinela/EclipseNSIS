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

public class Clipboard extends org.eclipse.gef.ui.actions.Clipboard 
{
    /**
     * The event name used for {@link Clipboard#fireContentsSet()}
     */
    public static final String CONTENTS_SET_EVENT = "ContentsSet"; //$NON-NLS-1$

    protected static Clipboard cInstance = new Clipboard();
       
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
    public static org.eclipse.gef.ui.actions.Clipboard getDefault() 
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
   
    /* (non-Javadoc)
     * @see org.eclipse.gef.ui.actions.Clipboard#setContents(java.lang.Object)
     */
    public void setContents(Object contents) 
    {
        super.setContents(contents);
        fireContentsSet();
    }
}



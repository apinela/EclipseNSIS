/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import net.sf.eclipsensis.settings.INSISPreferenceConstants;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;

public abstract class NSISAutoEditStrategy implements IAutoEditStrategy
{
    protected boolean mUseSpacesForTabs;
    protected int mTabWidth;
    protected IPreferenceStore mPreferenceStore;
    
    public NSISAutoEditStrategy(IPreferenceStore preferenceStore)
    {
        super();
        mPreferenceStore = preferenceStore;
        updateFromPreferences();
    }
    
    public void updateFromPreferences()
    {
        mUseSpacesForTabs = mPreferenceStore.getBoolean(INSISPreferenceConstants.USE_SPACES_FOR_TABS);
        mTabWidth = mPreferenceStore.getInt(INSISPreferenceConstants.TAB_WIDTH);
    }

}

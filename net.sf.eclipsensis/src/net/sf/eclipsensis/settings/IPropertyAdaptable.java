/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.settings;

import org.eclipse.jface.preference.IPreferenceStore;

public interface IPropertyAdaptable
{
    public boolean canAdaptToProperty(IPreferenceStore store, String property);
    public void adaptToProperty(IPreferenceStore store, String property);
}

/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.wizard;

import org.eclipse.jface.wizard.IWizardPage;

public interface INSISWizardListener
{
    public void aboutToEnter(IWizardPage page, boolean forward);
    
    public void aboutToLeave(IWizardPage page, boolean forward);
}

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

import net.sf.eclipsensis.editor.text.NSISTextUtility;
import net.sf.eclipsensis.settings.IPropertyAdaptable;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;

public class NSISDamagerRepairer extends DefaultDamagerRepairer implements IPropertyAdaptable
{
    /**
     * @param scanner
     */
    public NSISDamagerRepairer(ITokenScanner scanner)
    {
        super(scanner);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.presentation.IPresentationDamager#getDamageRegion(org.eclipse.jface.text.ITypedRegion, org.eclipse.jface.text.DocumentEvent, boolean)
     */
    public IRegion getDamageRegion(ITypedRegion partition, DocumentEvent e,
            boolean documentPartitioningChanged)
    {
        if (!documentPartitioningChanged) {
            ITypedRegion[][] regions = NSISTextUtility.getNSISLines(e.getDocument(),e.getOffset());
            int startOffset;
            if(!Common.isEmptyArray(regions) && !Common.isEmptyArray(regions[0])) {
                startOffset = regions[0][0].getOffset();
            }
            else {
                startOffset = e.getOffset();
            }
            String text = e.getText();
            regions = NSISTextUtility.getNSISLines(e.getDocument(),e.getOffset()+Math.max((text != null?text.length():0),e.getLength()));
            if(!Common.isEmptyArray(regions) && !Common.isEmptyArray(regions[0])) {
                IRegion lastRegion = regions[0][regions[0].length-1];
                return new Region(startOffset,lastRegion.getOffset()+lastRegion.getLength()-startOffset);
            }
            else {
                return super.getDamageRegion(partition, e, documentPartitioningChanged);
            }
        }
        
        return partition;
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#adaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public void adaptToProperty(IPreferenceStore store, String property)
    {
        if(fScanner instanceof IPropertyAdaptable) {
            ((IPropertyAdaptable)fScanner).adaptToProperty(store, property);
        }
    }

    /* (non-Javadoc)
     * @see net.sf.eclipsensis.settings.IPropertyAdaptable#canAdaptToProperty(org.eclipse.jface.preference.IPreferenceStore, java.lang.String)
     */
    public boolean canAdaptToProperty(IPreferenceStore store, String property)
    {
        if(fScanner instanceof IPropertyAdaptable) {
            return ((IPropertyAdaptable)fScanner).canAdaptToProperty(store, property);
        }
        return false;
    }
}

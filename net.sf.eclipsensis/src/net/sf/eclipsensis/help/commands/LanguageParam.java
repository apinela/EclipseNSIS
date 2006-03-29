/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.*;

import net.sf.eclipsensis.lang.NSISLanguage;
import net.sf.eclipsensis.lang.NSISLanguageManager;
import net.sf.eclipsensis.util.Common;

import org.w3c.dom.Node;

public class LanguageParam extends ComboParam
{
    public LanguageParam(Node node)
    {
        super(node);
    }

    protected ComboEntry[] getComboEntries()
    {
        ComboEntry[] entries = EMPTY_COMBO_ENTRIES;
        List languages = NSISLanguageManager.getInstance().getLanguages();
        if(!Common.isEmptyCollection(languages)) {
            entries = new ComboEntry[languages.size()];
            Collections.sort(languages, new Comparator() {
                public int compare(Object o1, Object o2)
                {
                    NSISLanguage lang1 = (NSISLanguage)o1;
                    NSISLanguage lang2 = (NSISLanguage)o2;
                    return lang1.getLangDef().compareTo(lang2.getLangDef());
                }
            });
            int i=0;
            for (Iterator iter = languages.iterator(); iter.hasNext();) {
                NSISLanguage lang = (NSISLanguage)iter.next();
                String def = lang.getLangDef();
                entries[i++] = new ComboEntry(def, def);
            }
        }
        return entries;
    }
}

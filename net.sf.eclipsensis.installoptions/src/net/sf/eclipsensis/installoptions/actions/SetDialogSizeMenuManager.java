/*******************************************************************************
 * Copyright (c) 2004-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.actions;

import java.util.*;

import net.sf.eclipsensis.installoptions.InstallOptionsPlugin;
import net.sf.eclipsensis.installoptions.model.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.jface.action.*;
import org.eclipse.jface.util.*;
import org.eclipse.ui.IEditorPart;

public class SetDialogSizeMenuManager extends MenuManager implements IPropertyChangeListener
{
    private IEditorPart mEditor = null;
    private Map<DialogSize, SetDialogSizeAction> mActionMap = new LinkedHashMap<DialogSize, SetDialogSizeAction>();
    private boolean mNeedsRebuild = true;
    private static final Action DUMMY_ACTION = new SetDialogSizeAction(null);

    public SetDialogSizeMenuManager(IMenuManager parent)
    {
        this(parent, null);
    }

    public SetDialogSizeMenuManager(IMenuManager parent, String id)
    {
        super(InstallOptionsPlugin.getResourceString("set.dialog.size.menu.name"), id); //$NON-NLS-1$
        rebuild();
        InstallOptionsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
        parent.addMenuListener(new IMenuListener(){
            public void menuAboutToShow(IMenuManager manager)
            {
                rebuild();
            }
        });
    }

    public void setEditor(IEditorPart editor)
    {
        mEditor = editor;
        for(Iterator<SetDialogSizeAction> iter=mActionMap.values().iterator(); iter.hasNext(); ) {
            iter.next().setEditor(mEditor);
        }
    }

    public void updateActions()
    {
        for(Iterator<SetDialogSizeAction> iter=mActionMap.values().iterator(); iter.hasNext(); ) {
            iter.next().update();
        }
    }

    public synchronized void rebuild()
    {
        if(mNeedsRebuild) {
            List<DialogSize> dialogSizes = DialogSizeManager.getDialogSizes();
            mActionMap.keySet().retainAll(dialogSizes);
            for (Iterator<DialogSize> iter = dialogSizes.iterator(); iter.hasNext();) {
                DialogSize element = iter.next();
                if(!mActionMap.containsKey(element)) {
                    SetDialogSizeAction action = new SetDialogSizeAction(element);
                    action.setEditor(mEditor);
                    mActionMap.put(element,action);
                }
            }

            IContributionItem[] items = getItems();
            for (int i = 0; i < items.length; i++) {
                remove(items[i]);
                items[i].dispose();
            }
            if(Common.isEmptyCollection(dialogSizes)) {
                add(DUMMY_ACTION);
            }
            else {
                for (Iterator<DialogSize> iter = dialogSizes.iterator(); iter.hasNext();) {
                    add(mActionMap.get(iter.next()));
                }
            }
            mNeedsRebuild = false;
        }
        else {
            updateActions();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event)
    {
        if(event.getProperty().startsWith(DialogSizeManager.PROPERTY_DIALOGSIZES_PREFIX)) {
            mNeedsRebuild = true;
        }
    }

    public boolean isNeedsRebuild()
    {
        return mNeedsRebuild;
    }

    @Override
	public boolean isDynamic()
    {
        return true;
    }
}

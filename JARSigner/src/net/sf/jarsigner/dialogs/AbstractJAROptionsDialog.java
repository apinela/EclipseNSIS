/*******************************************************************************
 * Copyright (c) 2005-2009 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.jarsigner.dialogs;

import java.io.File;
import java.security.KeyStoreException;
import java.util.List;

import net.sf.eclipsensis.utilities.dialogs.AbstractToolsUtilityDialog;
import net.sf.eclipsensis.utilities.util.Common;
import net.sf.jarsigner.JARSignerPlugin;
import net.sf.jarsigner.util.AbstractJARUtil;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Version;

public abstract class AbstractJAROptionsDialog extends AbstractToolsUtilityDialog
{
    protected static final String KEY_STORE = "key.store"; //$NON-NLS-1$
    protected static final Version cMinJDKVersion = new Version(1,2,0);

    /**
     * @param parentShell
     * @throws KeyStoreException
     */
    public AbstractJAROptionsDialog(Shell parentShell, List<Object> selection)
    {
        super(parentShell, selection);
    }

    protected void init()
    {
        super.init();
        String keyStore = getStringDialogSetting(KEY_STORE);
        if(Common.isEmpty(keyStore)) {
            setValue(KEY_STORE,getDefaultKeyStore());
        }
        else {
            setValue(KEY_STORE,keyStore);
        }
    }

    protected String getDefaultKeyStore()
    {
        String userHome = System.getProperty("user.home"); //$NON-NLS-1$
        File storePath = new File(userHome,".keystore"); //$NON-NLS-1$
        if(Common.isValidFile(storePath)) {
            return storePath.getAbsolutePath();
        }
        return ""; //$NON-NLS-1$
    }

    protected void okPressed()
    {
       getDialogSettings().put(KEY_STORE,getKeyStore());
       super.okPressed();
    }

    public final String getKeyStore()
    {
        return (String)getValues().get(KEY_STORE);
    }

    protected AbstractUIPlugin getPlugin()
    {
        return JARSignerPlugin.getDefault();
    }

    protected Version getMinJDKVersion()
    {
        return cMinJDKVersion;
    }

    protected String getToolsMainClassName(Version toolsJarVersion)
    {
        return AbstractJARUtil.JAR_SIGNER_MAIN_CLASS_NAME;
    }

    protected boolean isValid()
    {
        if(super.isValid()) {
            boolean state = true;

            if(!Common.isEmpty(getKeyStore())) {
                File f = new File(getKeyStore());
                state = Common.isValidFile(f);
            }

            return state;
        }
        return false;
    }
}

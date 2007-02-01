/*******************************************************************************
 * Copyright (c) 2004-2007 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.startup;

import java.text.MessageFormat;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.dynamichelpers.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.registry.EditorRegistry;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.*;

public class FileAssociationChecker implements IStartup,  IExtensionChangeHandler
{
    private static final IPreferenceStore PREFERENCES = EclipseNSISStartup.getDefault().getPreferenceStore();
    private static final String EXTENSION_POINT = "fileAssociations"; //$NON-NLS-1$
    private static final String ELEM_ASSOCIATION = "association"; //$NON-NLS-1$
    private static final String ELEM_ENABLEMENT = "enablement"; //$NON-NLS-1$
    private static final String ELEM_FILETYPES = "fileTypes"; //$NON-NLS-1$
    private static final String ELEM_FILETYPE = "fileType"; //$NON-NLS-1$
    private static final String ELEM_EDITORS = "editors"; //$NON-NLS-1$
    private static final String ELEM_EDITOR = "editor"; //$NON-NLS-1$
    private static final String ATTR_DEFAULT_VALUE = "defaultValue"; //$NON-NLS-1$
    private static final String ATTR_INIT_PREFERENCE = "initPreference"; //$NON-NLS-1$
    private static final String ATTR_NAME = "name"; //$NON-NLS-1$
    private static final String ATTR_ID = "id"; //$NON-NLS-1$

    private static FileAssociationChecker cInstance = null;
    private static Collection cCheckedAssociations = new HashSet();

    private static final Object JOB_FAMILY = new Object();

    private Map mFileAssociationMap = new LinkedHashMap();
    private IEditorRegistry mEditorRegistry;
    private String mJobName;
    private MessageFormat mDialogTitleFormat;
    private MessageFormat mDialogMessageFormat;
    private MessageFormat mDialogToggleMessageFormat;
    private static final ISchedulingRule cSchedulingRule = new ISchedulingRule() {
        public boolean contains(ISchedulingRule rule)
        {
            return rule == this;
        }

        public boolean isConflicting(ISchedulingRule rule)
        {
            return rule == this;
        }
    };

    public FileAssociationChecker()
    {
        mEditorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
        mJobName = EclipseNSISStartup.getResourceString("job.name"); //$NON-NLS-1$
        mDialogTitleFormat = new MessageFormat(EclipseNSISStartup.getResourceString("dialog.title.format")); //$NON-NLS-1$
        mDialogMessageFormat = new MessageFormat(EclipseNSISStartup.getResourceString("dialog.message.format")); //$NON-NLS-1$
        mDialogToggleMessageFormat = new MessageFormat(EclipseNSISStartup.getResourceString("dialog.toggle.message.format")); //$NON-NLS-1$
        final IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
        loadExtensions(tracker);
        tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));
        final BundleContext bundleContext = EclipseNSISStartup.getDefault().getBundleContext();
        bundleContext.addBundleListener(new BundleListener() {
            public void bundleChanged(BundleEvent event)
            {
                if(event.getType() == BundleEvent.STOPPED ) {
                    bundleContext.removeBundleListener(this);
                }
                tracker.unregisterHandler(FileAssociationChecker.this);
                Platform.getJobManager().cancel(JOB_FAMILY);
            }
        });
    }

    public void earlyStartup()
    {
        cInstance = this;
        for (Iterator iter = mFileAssociationMap.values().iterator(); iter.hasNext();) {
            scheduleJob((FileAssociationDef)iter.next());
        }
    }

    private void loadExtensions(IExtensionTracker tracker)
    {
        IExtensionPoint point = getExtensionPointFilter();
        if (point != null) {
            IExtension[] extensions = point.getExtensions();
            for (int i = 0; i < extensions.length; i++) {
                addExtension(tracker, extensions[i]);
            }
            EclipseNSISStartup.getDefault().savePluginPreferences();
        }
    }

    private IExtensionPoint getExtensionPointFilter()
    {
        return Platform.getExtensionRegistry().getExtensionPoint(EclipseNSISStartup.PLUGIN_ID,EXTENSION_POINT);
    }

    public void addExtension(IExtensionTracker tracker, IExtension extension)
    {
        IConfigurationElement[] associations = extension.getConfigurationElements();
        String bundleId = extension.getNamespaceIdentifier();
        for (int i = 0; i < associations.length; i++) {
            if (ELEM_ASSOCIATION.equals(associations[i].getName())) {
                List fileTypesList = null;
                List editorIdsList = null;
                String id = associations[i].getAttribute(ATTR_ID);
                String fileTypesName = ""; //$NON-NLS-1$
                String editorsName = ""; //$NON-NLS-1$
                IConfigurationElement[] children = associations[i].getChildren();
                boolean enablement = true;
                String enablementPref = null;
                for (int j = 0; j < children.length; j++) {
                    String name = children[j].getName();
                    if(fileTypesList == null && ELEM_FILETYPES.equals(name)) {
                        fileTypesName = children[j].getAttribute(ATTR_NAME);
                        fileTypesList = new ArrayList();
                        IConfigurationElement[] fileTypes = children[j].getChildren();
                        for (int k = 0; k < fileTypes.length; k++) {
                            if(ELEM_FILETYPE.equals(fileTypes[k].getName())) {
                                fileTypesList.add(fileTypes[k].getAttribute(ATTR_NAME));
                            }
                        }
                    }
                    else if(editorIdsList == null && ELEM_EDITORS.equals(name)) {
                        editorsName = children[j].getAttribute(ATTR_NAME);
                        editorIdsList = new ArrayList();
                        IConfigurationElement[] editors = children[j].getChildren();
                        for (int k = 0; k < editors.length; k++) {
                            if(ELEM_EDITOR.equals(editors[k].getName())) {
                                editorIdsList.add(editors[k].getAttribute(ATTR_ID));
                            }
                        }
                    }
                    else if(ELEM_ENABLEMENT.equals(name)) {
                        String value = children[j].getAttribute(ATTR_DEFAULT_VALUE);
                        if(value != null) {
                            enablement = Boolean.valueOf(value).booleanValue();
                        }
                        enablementPref = children[j].getAttribute(ATTR_INIT_PREFERENCE);
                    }
                }
                if(fileTypesList != null && editorIdsList != null) {
                    initializePreference(id, enablement, bundleId, enablementPref);
                    mFileAssociationMap.put(id, new FileAssociationDef(id, fileTypesName, (String[])fileTypesList.toArray(new String[fileTypesList.size()]),
                            editorsName, (String[])editorIdsList.toArray(new String[editorIdsList.size()])));
                }
            }
        }
    }

    private void initializePreference(String associationId, boolean enablement, String bundleId, String enablementPref)
    {
        if(!PREFERENCES.contains(associationId)) {
            if(enablementPref != null) {
                Bundle bundle = Platform.getBundle(bundleId);
                if(bundle != null) {
                    IPreferenceStore prefs = new ScopedPreferenceStore(new InstanceScope(), bundle.getSymbolicName());
                    if(prefs != null) {
                        if(prefs.contains(enablementPref)) {
                            enablement = prefs.getBoolean(enablementPref);
                        }
                    }
                }
            }
            PREFERENCES.setValue(associationId,Boolean.toString(enablement));
        }
    }

    private void scheduleJob(final FileAssociationDef def)
    {
        if (def != null) {
            Job job = new UIJob(mJobName) {
                private boolean isValidEditor(String[] editorIds, IEditorDescriptor descriptor)
                {
                    if (descriptor != null) {
                        String id = descriptor.getId();
                        for (int i = 0; i < editorIds.length; i++) {
                            if (editorIds[i].equals(id)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }

                public IStatus runInUIThread(final IProgressMonitor monitor)
                {
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    if (!cCheckedAssociations.contains(def.getAssociationId())) {
                        final boolean toggleState = getFileAssociationChecking(def.getAssociationId());
                        if (toggleState) {
                            cCheckedAssociations.add(def.getAssociationId());
                            final String[] fileTypes = def.getFileTypes();
                            for (int i = 0; i < fileTypes.length; i++) {
                                if (monitor.isCanceled()) {
                                    return Status.CANCEL_STATUS;
                                }
                                if (!isValidEditor(def.getEditorIds(), mEditorRegistry.getDefaultEditor(fileTypes[i]))) {
                                    if (!monitor.isCanceled()) {
                                        String[] args = new String[]{def.getFileTypesName(), def.getEditorsName()};
                                        MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoCancelQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                                                mDialogTitleFormat.format(args), mDialogMessageFormat.format(args), mDialogToggleMessageFormat.format(args), !toggleState, null, null);
                                        int rc = dialog.getReturnCode();
                                        switch (rc)
                                        {
                                            case IDialogConstants.YES_ID:
                                            {
                                                for (int j = 0; j < fileTypes.length; j++) {
                                                    mEditorRegistry.setDefaultEditor(fileTypes[j], def.getEditorIds()[0]); 
                                                }
                                                //Cast to inner class because otherwise it cannot be saved.
                                                ((EditorRegistry)mEditorRegistry).saveAssociations();
                                            }
                                            case IDialogConstants.NO_ID:
                                            {
                                                boolean newToggleState = !dialog.getToggleState();
                                                if (toggleState != newToggleState) {
                                                    setFileAssociationChecking(def.getAssociationId(), newToggleState);
                                                }
                                                break;
                                            }
                                            case IDialogConstants.CANCEL_ID:
                                                return Status.CANCEL_STATUS;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    if (monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    else {
                        return Status.OK_STATUS;
                    }
                }
            };
            job.setRule(cSchedulingRule);
            job.schedule(5000);
        }
    }

    public void removeExtension(IExtension extension, Object[] objects)
    {
    }

    public static final boolean getFileAssociationChecking(String associationId)
    {
        String value = PREFERENCES.getString(associationId);
        if(value != null) {
            return Boolean.valueOf(value).booleanValue();
        }
        return false;
    }

    public static final void setFileAssociationChecking(String associationId, boolean flag)
    {
        if(getFileAssociationChecking(associationId) != flag) {
            PREFERENCES.setValue(associationId,Boolean.toString(flag));
            EclipseNSISStartup.getDefault().savePluginPreferences();
        }
    }

    public static final void checkFileAssociation(String associationId)
    {
        if(cInstance == null) {
            cInstance = new FileAssociationChecker();
        }
        if(!cCheckedAssociations.contains(associationId)) {
            cInstance.scheduleJob((FileAssociationDef)cInstance.mFileAssociationMap.get(associationId));
        }
    }

    private class FileAssociationDef
    {
        String mAssociationId;
        String mFileTypesName;
        String[] mFileTypes;
        String mEditorsName;
        String[] mEditorIds;

        public FileAssociationDef(String associationId, String fileTypesName, String[] fileTypes, String editorsName, String[] editorIds)
        {
            mAssociationId = associationId;
            mFileTypesName = fileTypesName;
            mFileTypes = fileTypes;
            mEditorsName = editorsName;
            mEditorIds = editorIds;
        }

        public String getAssociationId()
        {
            return mAssociationId;
        }

        public String[] getEditorIds()
        {
            return mEditorIds;
        }

        public String getEditorsName()
        {
            return mEditorsName;
        }

        public String[] getFileTypes()
        {
            return mFileTypes;
        }

        public String getFileTypesName()
        {
            return mFileTypesName;
        }
    }
}

/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.commands;

import java.util.*;
import java.util.List;

import org.eclipse.swt.widgets.*;

public interface INSISParamEditor
{
    public Control createControl(Composite parent);
    public Control getControl();
    public String validate();
    public void appendText(StringBuffer buf);
    public void setDependents(List dependents);
    public List getDependents();
    public void setEnabled(boolean enabled);
    public boolean isSelected();
    public NSISParam getParam();
    public Map getSettings();
    public void setSettings(Map settings);
    public void saveSettings();
    public void initEditor();
    public INSISParamEditor getParentEditor();
    public List getChildEditors();
    public void dispose();
    public void clear();
    public void reset();
    public boolean hasRequiredFields();
}

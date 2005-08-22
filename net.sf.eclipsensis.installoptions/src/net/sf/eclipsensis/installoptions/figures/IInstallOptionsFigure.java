/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.figures;

import java.util.*;

import net.sf.eclipsensis.installoptions.model.InstallOptionsModel;

import org.eclipse.draw2d.IFigure;

public interface IInstallOptionsFigure extends IFigure
{
    public static final List SCROLL_FLAGS = Collections.unmodifiableList(Arrays.asList(
                                                    new String[]{InstallOptionsModel.FLAGS_HSCROLL,
                                                                 InstallOptionsModel.FLAGS_VSCROLL}));
    
    public void setDisabled(boolean disabled);
    public void setHScroll(boolean hScroll);
    public void setVScroll(boolean vScroll);
    public void refresh();
}

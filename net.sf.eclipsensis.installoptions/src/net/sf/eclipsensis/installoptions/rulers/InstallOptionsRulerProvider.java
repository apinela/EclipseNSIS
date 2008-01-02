/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import java.beans.*;
import java.util.*;

import net.sf.eclipsensis.installoptions.figures.FigureUtility;
import net.sf.eclipsensis.installoptions.model.commands.*;
import net.sf.eclipsensis.installoptions.util.FontUtility;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.rulers.*;
import org.eclipse.swt.graphics.Font;

public class InstallOptionsRulerProvider extends RulerProvider
{
    public static final int UNIT_DLU = 3;

    private InstallOptionsRuler mRuler;

    private PropertyChangeListener mRulerListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals(InstallOptionsRuler.PROPERTY_CHILDREN)) {
                InstallOptionsGuide guide = (InstallOptionsGuide)evt.getNewValue();
                if (getGuides().contains(guide)) {
                    guide.addPropertyChangeListener(mGuideListener);
                }
                else {
                    guide.removePropertyChangeListener(mGuideListener);
                }
                for (int i = 0; i < listeners.size(); i++) {
                    ((RulerChangeListener)listeners.get(i))
                            .notifyGuideReparented(guide);
                }
            }
            else {
                for (int i = 0; i < listeners.size(); i++) {
                    ((RulerChangeListener)listeners.get(i))
                            .notifyUnitsChanged(mRuler.getUnit());
                }
            }
        }
    };

    private PropertyChangeListener mGuideListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt)
        {
            if (evt.getPropertyName().equals(InstallOptionsGuide.PROPERTY_CHILDREN)) {
                for (int i = 0; i < listeners.size(); i++) {
                    ((RulerChangeListener)listeners.get(i))
                            .notifyPartAttachmentChanged(evt.getNewValue(), evt
                                    .getSource());
                }
            }
            else {
                for (int i = 0; i < listeners.size(); i++) {
                    ((RulerChangeListener)listeners.get(i))
                            .notifyGuideMoved(evt.getSource());
                }
            }
        }
    };

    public InstallOptionsRulerProvider(InstallOptionsRuler ruler)
    {
        this.mRuler = ruler;
        this.mRuler.addPropertyChangeListener(mRulerListener);
        List guides = getGuides();
        for (int i = 0; i < guides.size(); i++) {
            ((InstallOptionsGuide)guides.get(i)).addPropertyChangeListener(mGuideListener);
        }
    }

    public List getAttachedModelObjects(Object guide)
    {
        return new ArrayList(((InstallOptionsGuide)guide).getWidgets());
    }

    public Command getCreateGuideCommand(int position)
    {
        return new CreateGuideCommand(mRuler, position);
    }

    public Command getDeleteGuideCommand(Object guide)
    {
        return new DeleteGuideCommand((InstallOptionsGuide)guide, mRuler);
    }

    public Command getMoveGuideCommand(Object guide, int pDelta)
    {
        return new MoveGuideCommand((InstallOptionsGuide)guide, pDelta);
    }

    private int convertGuidePosition(Font f, InstallOptionsGuide guide)
    {
        return (guide.isHorizontal()?FigureUtility.dialogUnitsToPixelsY(guide.getPosition(),f):FigureUtility.dialogUnitsToPixelsX(guide.getPosition(),f));
    }

    public int[] getGuidePositions()
    {
        List guides = getGuides();
        Font f = FontUtility.getInstallOptionsFont();
        int[] result = new int[guides.size()];
        for (int i = 0; i < guides.size(); i++) {
            result[i] = convertGuidePosition(f,(InstallOptionsGuide)guides.get(i));
        }
        return result;
    }

    public Object getRuler()
    {
        return mRuler;
    }

    public int getUnit()
    {
        return mRuler.getUnit();
    }

    public void setUnit(int newUnit)
    {
        mRuler.setUnit(newUnit);
    }

    public int getGuidePosition(Object guide)
    {
        return convertGuidePosition(FontUtility.getInstallOptionsFont(),(InstallOptionsGuide)guide);
    }

    public List getGuides()
    {
        return mRuler.getGuides();
    }

}
/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.rulers;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import net.sf.eclipsensis.installoptions.model.commands.*;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.rulers.RulerChangeListener;
import org.eclipse.gef.rulers.RulerProvider;

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

    public int[] getGuidePositions()
    {
        List guides = getGuides();
        int[] result = new int[guides.size()];
        for (int i = 0; i < guides.size(); i++) {
            result[i] = ((InstallOptionsGuide)guides.get(i)).getPosition();
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
        return ((InstallOptionsGuide)guide).getPosition();
    }

    public List getGuides()
    {
        return mRuler.getGuides();
    }

}
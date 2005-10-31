/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.model;

import java.util.Map;

import net.sf.eclipsensis.installoptions.ini.INISection;
import net.sf.eclipsensis.util.CaseInsensitiveMap;

import org.eclipse.gef.requests.CreationFactory;

public class InstallOptionsElementFactory implements CreationFactory
{
    private static Map cCachedFactories = new CaseInsensitiveMap();

    public static InstallOptionsElementFactory getFactory(String type)
    {
        InstallOptionsModelTypeDef typeDef = InstallOptionsModel.INSTANCE.getControlTypeDef(type);
        if(typeDef != null) {
            synchronized(typeDef) {
                InstallOptionsElementFactory factory = (InstallOptionsElementFactory)cCachedFactories.get(typeDef.getType());
                if(factory == null) {
                    factory = new InstallOptionsElementFactory(typeDef);
                    cCachedFactories.put(typeDef.getType(), factory);
                }
                return factory;
            }
        }
        else {
            return getFactory(InstallOptionsModel.TYPE_UNKNOWN);
        }
    }
    
    private InstallOptionsModelTypeDef mTypeDef;

    /**
     * 
     */
    private InstallOptionsElementFactory(InstallOptionsModelTypeDef typeDef)
    {
        super();
        mTypeDef = typeDef;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject()
    {
        return getNewObject(null);
    }

    public Object getNewObject(INISection section)
    {
        return mTypeDef.createModel(section);
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
     */
    public Object getObjectType() {
        return mTypeDef.getType();
    }
}

/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template.resolvers;

import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;

public class NSISPercentVariableResolver extends SimpleTemplateVariableResolver
{
    /**
     * @param type
     * @param description
     */
    public NSISPercentVariableResolver(String type, String description)
    {
        super(type, description);
        setEvaluationString("%"); //$NON-NLS-1$
    }
}

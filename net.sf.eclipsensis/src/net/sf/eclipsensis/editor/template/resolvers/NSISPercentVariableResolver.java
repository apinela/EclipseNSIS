/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template.resolvers;

import net.sf.eclipsensis.EclipseNSISPlugin;

import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;

public class NSISPercentVariableResolver extends SimpleTemplateVariableResolver
{
    /**
     * @param type
     * @param description
     */
    public NSISPercentVariableResolver()
    {
        super("percent", EclipseNSISPlugin.getResourceString("nsis.resolvers.percent.description")); //$NON-NLS-1$ //$NON-NLS-2$
        setEvaluationString("%"); //$NON-NLS-1$
    }
}

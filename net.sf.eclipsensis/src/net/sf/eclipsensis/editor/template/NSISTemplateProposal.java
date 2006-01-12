/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.template;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.*;
import org.eclipse.swt.graphics.Image;

public class NSISTemplateProposal extends TemplateProposal
{
    private int mRelevance = 0;
    /**
     * @param template
     * @param context
     * @param region
     * @param image
     */
    public NSISTemplateProposal(Template template, TemplateContext context,
            IRegion region, Image image)
    {
        this(template, context, region, image, 0);
    }

    /**
     * @param template
     * @param context
     * @param region
     * @param image
     * @param relevance
     */
    public NSISTemplateProposal(Template template, TemplateContext context, IRegion region, Image image, int relevance)
    {
        super(template, context, region, image, relevance);
        mRelevance = relevance;
    }

    public int getRelevance()
    {
        return mRelevance;
    }
}
/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.installoptions.editor;

import net.sf.eclipsensis.editor.codeassist.NSISAnnotationHover;
import net.sf.eclipsensis.installoptions.IInstallOptionsConstants;
import net.sf.eclipsensis.installoptions.editor.text.*;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.*;

public class InstallOptionsSourceViewerConfiguration extends SourceViewerConfiguration
{
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer)
    {
        return new NSISAnnotationHover(new String[]{IInstallOptionsConstants.INSTALLOPTIONS_ANNOTATION_ERROR_NAME,
                                                    IInstallOptionsConstants.INSTALLOPTIONS_ANNOTATION_WARNING_NAME});
    }
    
    /*
     * (non-Javadoc) Method declared on SourceViewerConfiguration
     */
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
    {
        PresentationReconciler reconciler = new PresentationReconciler();
        reconciler.setDocumentPartitioning(InstallOptionsPartitionScanner.INSTALLOPTIONS_PARTITIONING);
        DefaultDamagerRepairer dr = new InstallOptionsDamagerRepairer(new InstallOptionsCommentScanner());
        reconciler.setDamager(dr, InstallOptionsPartitionScanner.INSTALLOPTIONS_COMMENT);
        reconciler.setRepairer(dr, InstallOptionsPartitionScanner.INSTALLOPTIONS_COMMENT);
        
        dr = new InstallOptionsDamagerRepairer(new InstallOptionsRuleBasedScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        return reconciler;
    }
}

/*******************************************************************************
 * Copyright (c) 2004 Sunil Kamath (IcemanK).
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.util.Iterator;

import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/** 
 * The NSISAnnotationHover provides the hover support for NSIS editors.
 */
public class NSISAnnotationHover implements IAnnotationHover, INSISConstants, IAnnotationHoverExtension
{
    private IInformationControlCreator mHoverControlCreator = new IInformationControlCreator(){
        public IInformationControl createInformationControl(Shell parent)
        {
            return new DefaultInformationControl(parent);
        }
    };
    
	/* (non-Javadoc)
	 * Method declared on IAnnotationHover
	 */
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) 
    {
		try {
            IAnnotationModel model = sourceViewer.getAnnotationModel();
            IDocument document= sourceViewer.getDocument();
			IRegion info= document.getLineInformation(lineNumber);
            
            if (model != null) {
                for(Iterator e= model.getAnnotationIterator(); e.hasNext(); ) {
                    Annotation a= (Annotation) e.next();
                    Position p= model.getPosition(a);
                    if (p != null && p.overlapsWith(info.getOffset(), info.getLength())) {
                        if(a instanceof MarkerAnnotation) {
                            IMarker marker = ((MarkerAnnotation)a).getMarker();
                            if(marker.getType().equals(PROBLEM_ID)) {
                                String msg= a.getText();
                                if (!Common.isEmpty(msg)) {
                                    return msg;
                                }
                            }
                        }
                    }
                }
            }
		} 
        catch (Exception ex) {
		}

		return null;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#canHandleMouseCursor()
     */
    public boolean canHandleMouseCursor()
    {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverControlCreator()
     */
    public IInformationControlCreator getHoverControlCreator()
    {
        return mHoverControlCreator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverInfo(org.eclipse.jface.text.source.ISourceViewer, org.eclipse.jface.text.source.ILineRange, int)
     */
    public Object getHoverInfo(ISourceViewer sourceViewer, ILineRange lineRange, int visibleNumberOfLines)
    {
        return getHoverInfo(sourceViewer, lineRange.getStartLine());
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.source.IAnnotationHoverExtension#getHoverLineRange(org.eclipse.jface.text.source.ISourceViewer, int)
     */
    public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber)
    {
        return new LineRange(lineNumber,1);
    }
}

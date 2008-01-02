/*******************************************************************************
 * Copyright (c) 2004-2008 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor.codeassist;

import java.util.*;

import net.sf.eclipsensis.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.source.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.MarkerAnnotation;

/**
 * The NSISAnnotationHover provides the hover support for NSIS editors.
 */
public class NSISAnnotationHover implements IAnnotationHover, INSISConstants, IAnnotationHoverExtension, ITextHover, ITextHoverExtension
{
    private Set mAnnotationTypes;

    private IInformationControlCreator mHoverControlCreator = new IInformationControlCreator(){
        public IInformationControl createInformationControl(Shell parent)
        {
            return new DefaultInformationControl(parent, new WrappingInformationPresenter());
        }
    };

    public NSISAnnotationHover(String[] annotationTypes)
    {
        super();
        mAnnotationTypes = new HashSet(Arrays.asList(annotationTypes));
    }

    protected boolean isSupported(Annotation a) throws CoreException
    {
        return mAnnotationTypes.contains(a.getType()) ||
        (a instanceof MarkerAnnotation && mAnnotationTypes.contains((((MarkerAnnotation)a).getMarker()).getType()));
    }

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
                ArrayList messages = new ArrayList();
                for(Iterator e= model.getAnnotationIterator(); e.hasNext(); ) {
                    Annotation a= (Annotation) e.next();
                    if(isSupported(a)) {
                        Position p= model.getPosition(a);
                        if (p != null && p.overlapsWith(info.getOffset(), info.getLength())) {
                            String msg = null;
                            msg= a.getText();
                            if (!Common.isEmpty(msg)) {
                                messages.add(msg);
                            }
                        }
                    }
                }
                if(messages.size() == 1) {
                    return (String)messages.get(0);
                }
                else if(messages.size() > 1) {
                    StringBuffer buf = new StringBuffer(EclipseNSISPlugin.getResourceString("multiple.markers.message")); //$NON-NLS-1$
                    for (Iterator iter = messages.iterator(); iter.hasNext();) {
                        buf.append(LINE_SEPARATOR).append("\t- ").append(iter.next()); //$NON-NLS-1$
                    }
                    return buf.toString();
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

    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
    {
        if(textViewer instanceof ISourceViewer) {
            try {
                ISourceViewer sourceViewer = (ISourceViewer)textViewer;
                IAnnotationModel model = sourceViewer.getAnnotationModel();

                if (model != null) {
                    ArrayList messages = new ArrayList();
                    for(Iterator e= model.getAnnotationIterator(); e.hasNext(); ) {
                        Annotation a= (Annotation) e.next();
                        if(isSupported(a)) {
                            Position p= model.getPosition(a);
                            if (p != null && p.overlapsWith(hoverRegion.getOffset(), hoverRegion.getLength())) {
                                String msg = null;
                                msg= a.getText();
                                if (!Common.isEmpty(msg)) {
                                    messages.add(msg);
                                }
                            }
                        }
                    }
                    if(messages.size() == 1) {
                        return (String)messages.get(0);
                    }
                    else if(messages.size() > 1) {
                        StringBuffer buf = new StringBuffer(EclipseNSISPlugin.getResourceString("multiple.markers.message")); //$NON-NLS-1$
                        for (Iterator iter = messages.iterator(); iter.hasNext();) {
                            buf.append(LINE_SEPARATOR).append("\t- ").append(iter.next()); //$NON-NLS-1$
                        }
                        return buf.toString();
                    }
                }
            }
            catch (Exception ex) {
            }
        }
        return null;
    }

    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {
        if(textViewer instanceof ISourceViewer) {
            try {
                ISourceViewer sourceViewer = (ISourceViewer)textViewer;
                IAnnotationModel model = sourceViewer.getAnnotationModel();

                if (model != null) {
                    for(Iterator e= model.getAnnotationIterator(); e.hasNext(); ) {
                        Annotation a= (Annotation) e.next();
                        if(isSupported(a)) {
                            Position p= model.getPosition(a);
                            if (p != null && p.includes(offset)) {
                                return new Region(p.getOffset(),p.getLength());
                            }
                        }
                    }
                }
            }
            catch (Exception ex) {
            }
        }
        return null;
    }
}

/*******************************************************************************
 * Copyright (c) 2004, 2005 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.editor;

import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.editor.text.*;
import net.sf.eclipsensis.util.Common;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;

public class NSISTaskTagUpdater implements INSISConstants
{
    private FileDocumentProvider mDocumentProvider = new FileDocumentProvider();
    
    public void updateTaskTags(IFile file)
    {
        try {
            FileEditorInput input = new FileEditorInput(file);
            mDocumentProvider.connect(input);
            updateTaskTags(file, mDocumentProvider.getDocument(input));
            mDocumentProvider.disconnect(input);
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
    }
    
    public void updateTaskTags(IFile file, IDocument document)
    {
        try {
            file.getWorkspace().deleteMarkers(file.findMarkers(TASK_MARKER_ID,true,IResource.DEPTH_ZERO));
            ITypedRegion[] typedRegions = NSISTextUtility.getNSISPartitions(document);
            if(!Common.isEmptyArray(typedRegions)) {
                NSISRegionScanner scanner = new NSISRegionScanner(document);
                NSISTaskTagRule taskTagRule = new NSISTaskTagRule();
                for (int i = 0; i < typedRegions.length; i++) {
                    scanner.setRegion(typedRegions[i]);
                    LinkedHashMap map = new LinkedHashMap();
                    while(true) {
                        int offset = scanner.getOffset();
                        IToken token = taskTagRule.evaluate(scanner);
                        if(token.isUndefined()) {
                            int c = scanner.read();
                            if(c == ICharacterScanner.EOF) {
                                break;
                            }
                        }
                        else {
                            map.put(new Region(offset,(scanner.getOffset()-offset)),token);
                        }
                    }
                    if(map.size() > 0) {
                        Region[] regions = (Region[])map.keySet().toArray(new Region[map.size()]);
                        for (int j = 0; j < regions.length; j++) {
                            try {
                                int line = document.getLineOfOffset(regions[j].getOffset());
                                IRegion lineRegion = NSISTextUtility.intersection(typedRegions[i],document.getLineInformation(line));
                                int start = regions[j].getOffset();
                                int lineEnd = lineRegion.getOffset()+lineRegion.getLength();
    
                                while(j < (regions.length-1) && (lineEnd > regions[j+1].getOffset())) {
                                    createTaskMarker(map, regions[j], file, document, line, start,regions[j+1].getOffset()-start);
                                    j++;
                                    start = regions[j].getOffset();
                                }
                                createTaskMarker(map, regions[j], file, document, line, start,lineEnd-start);
                        }
                            catch (BadLocationException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        catch (CoreException e) {
            e.printStackTrace();
        }
    }

    
    private void createTaskMarker(Map regionMap, IRegion region, IFile file, IDocument document, int line, int start, int length) throws BadLocationException, CoreException
    {
        IToken token = (IToken)regionMap.get(region);
        NSISTaskTag taskTag = (NSISTaskTag)token.getData();
        String message = document.get(start,length).trim();

        IMarker marker = file.createMarker(TASK_MARKER_ID);
        marker.setAttribute(IMarker.LINE_NUMBER,line+1);
        marker.setAttribute(IMarker.PRIORITY,taskTag.getPriority());
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.USER_EDITABLE,false);
        marker.setAttribute(IMarker.CHAR_START,start);
        marker.setAttribute(IMarker.CHAR_END,start+message.length());
    }
    
    public void updateTaskTags()
    {
        final String taskName = EclipseNSISPlugin.getResourceString("task.tags.job.title"); //$NON-NLS-1$
        Job job = new Job(taskName) {
            public IStatus run(IProgressMonitor monitor)
            {
                try {
                    monitor.beginTask(taskName,2);
                    monitor.setTaskName(EclipseNSISPlugin.getResourceString("task.tags.scan.task.name")); //$NON-NLS-1$
                    final String[] extensions = Common.loadArrayProperty(EclipseNSISPlugin.getDefault().getResourceBundle(),"nsis.extensions"); //$NON-NLS-1$
                    for (int i = 0; i < extensions.length; i++) {
                        extensions[i]=extensions[i].toLowerCase();
                    }
                    final HashMap filesMap = new HashMap();
                    Collection coll = NSISEditor.getEditors();
                    for (Iterator iter = coll.iterator(); iter.hasNext();) {
                        NSISEditor editor = (NSISEditor)iter.next();
                        if(!editor.isDirty()) {
                            IEditorInput editorInput = editor.getEditorInput();
                            if(editorInput instanceof IFileEditorInput) {
                                IFile file = ((IFileEditorInput)editorInput).getFile();
                                IDocument document = editor.getDocumentProvider().getDocument(editorInput);
                                filesMap.put(file,document);
                            }
                        }
                    }
                    ResourcesPlugin.getWorkspace().getRoot().accept(new IResourceVisitor() {

                        public boolean visit(IResource resource) throws CoreException
                        {
                            if(resource instanceof IFile) {
                                String ext = resource.getFileExtension();
                                if(!Common.isEmpty(ext)) {
                                    for (int i = 0; i < extensions.length; i++) {
                                        if(ext.toLowerCase().equals(extensions[i])) {
                                            filesMap.put(resource,null);
                                            break;
                                        }
                                    }
                                }
                            }
                            else if(resource instanceof IContainer) {
                                return true;
                            }
                            return false;
                        }
                        
                    });
                    
                    monitor.setTaskName(taskName);
                    String taskName2 = EclipseNSISPlugin.getResourceString("task.tags.update.task.name"); //$NON-NLS-1$
                    SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,1);
                    try {
                        subMonitor.beginTask(taskName2,filesMap.size());
                        for (Iterator iter = filesMap.keySet().iterator(); iter.hasNext();) {
                            IFile file = (IFile)iter.next();
                            IDocument document = (IDocument)filesMap.get(file);
                            subMonitor.setTaskName(EclipseNSISPlugin.getFormattedString("task.tags.update.file.task.name",new String[]{file.getFullPath().toString()})); //$NON-NLS-1$
                            if(document == null) {
                                updateTaskTags(file);
                            }
                            else {
                                updateTaskTags(file,document);
                            }
                            subMonitor.worked(1);
                        }
                    }
                    finally {
                        subMonitor.done();
                    }
                    monitor.worked(1);
                }
                catch(CoreException ce) {
                    ce.printStackTrace();
                }
                finally {
                    monitor.done();
                }
                return Status.OK_STATUS;
            }
        };
        job.schedule();
    }
}

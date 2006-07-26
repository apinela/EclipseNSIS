/*******************************************************************************
 * Copyright (c) 2004-2006 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.search;

import java.io.IOException;
import java.util.*;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.search.parser.NSISHelpSearchQueryParser;
import net.sf.eclipsensis.help.search.parser.ParseException;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.eclipse.core.runtime.*;

public class NSISHelpSearcher implements INSISHelpSearchConstants
{
    final Object JOB_FAMILY = new Object();
    private NSISHelpIndexer mIndexer;

    public NSISHelpSearcher(NSISHelpIndexer indexer)
    {
        mIndexer = indexer;
    }

    public void stopSearching()
    {
        mIndexer.getScheduler().cancelJobs(JOB_FAMILY);
    }

    public void search(String field, INSISHelpSearchRequester requester)
    {
        mIndexer.getScheduler().scheduleJob(JOB_FAMILY, EclipseNSISPlugin.getResourceString("nsis.help.searcher.job.title"), new NSISHelpSearcherJob(field, requester)); //$NON-NLS-1$
    }
    
    private class NSISHelpSearcherJob implements IJobStatusRunnable
    {
        private INSISHelpSearchRequester mRequester;
        private String mField = null;
        private IndexSearcher mSearcher = null;
        private List mHits = null;
        
        public NSISHelpSearcherJob(String field, INSISHelpSearchRequester requester)
        {
            mField = field;
            mRequester = requester;
        }

        public IStatus run(final IProgressMonitor monitor)
        {
            List results = Collections.EMPTY_LIST;
            Collection highlightTerms = Collections.EMPTY_SET;
            try {
                monitor.beginTask(EclipseNSISPlugin.getResourceString("searching.help.task.name"),IProgressMonitor.UNKNOWN); //$NON-NLS-1$
                if(!IOUtility.isValidDirectory(mIndexer.getIndexLocation())) {
                    mIndexer.indexHelp();
                }
                try {
                    Platform.getJobManager().join(mIndexer.JOB_FAMILY, monitor);
                }
                catch (OperationCanceledException e) {
                    return Status.CANCEL_STATUS;
                }
                catch (InterruptedException e) {
                }
                if(!IOUtility.isValidDirectory(mIndexer.getIndexLocation())) {
                    return new Status(IStatus.ERROR,INSISConstants.PLUGIN_ID,IStatus.ERROR,EclipseNSISPlugin.getResourceString("search.index.not.created"),null); //$NON-NLS-1$
                }
                if(checkCanceled(monitor)) {
                    return Status.CANCEL_STATUS;
                }
                Query query = null;
                try {
                    mSearcher = new IndexSearcher(mIndexer.getIndexLocation().getAbsolutePath());
                    if(checkCanceled(monitor)) {
                        return Status.CANCEL_STATUS;
                    }
                    query = QueryParser.parse(mRequester.getSearchText(), mField==null?INDEX_FIELD_CONTENTS:mField, mIndexer.getAnalyzer());
                    if(checkCanceled(monitor)) {
                        return Status.CANCEL_STATUS;
                    }
                    Filter filter = mRequester.getFilter();
                    HitCollector collector = new HitCollector() {
                        public void collect(int doc, float score)
                        {
                            checkCanceled(monitor);
                            if(!monitor.isCanceled()) {
                                mHits.add(new HitDoc(score, doc));
                            }
                            else {
                                try {
                                    mSearcher.close();
                                }
                                catch (IOException e) {
                                }
                            }
                        }
                    };
                    mHits = new ArrayList();
                    if(filter != null) {
                        mSearcher.search(query, filter, collector);
                    }
                    else {
                        mSearcher.search(query, collector);
                    }
                }
                catch (Exception e) {
                    if(monitor.isCanceled()) {
                        return Status.CANCEL_STATUS;
                    }
                    EclipseNSISPlugin.getDefault().log(e);
                    return new Status(IStatus.ERROR,INSISConstants.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
                }
                finally {
                    if(!monitor.isCanceled() && !Common.isEmptyCollection(mHits)) {
                        Collections.sort(mHits);
                        int n = mHits.size();
                        results = new ArrayList(n);
                        int rank = 1;
                        for(int i=0; i<n; i++) {
                            try {
                                Document doc = mSearcher.doc(((HitDoc)mHits.get(i)).id);
                                results.add(new NSISHelpSearchResult(doc.get(INDEX_FIELD_TITLE),doc.get(INDEX_FIELD_URL),rank++));
                            }
                            catch (IOException e) {
                                EclipseNSISPlugin.getDefault().log(e);
                            }
                        }
                    }
                    if(mSearcher != null) {
                        try {
                            mSearcher.close();
                        }
                        catch (IOException e) {
                            EclipseNSISPlugin.getDefault().log(e);
                        }
                        mSearcher = null;
                    }
                    if(mHits != null) {
                        mRequester.queryParsed(query);
                        try {
                            highlightTerms = NSISHelpSearchQueryParser.parse(mField, mIndexer.getAnalyzer(), mRequester.getSearchText());
                        }
                        catch (ParseException e) {
                            EclipseNSISPlugin.getDefault().log(e);
                            highlightTerms = Collections.EMPTY_SET;
                        }
                    }
                }
                return Status.OK_STATUS;
            }
            finally {
                monitor.done();
                mRequester.searchCompleted((NSISHelpSearchResult[])results.toArray(new NSISHelpSearchResult[results.size()]), highlightTerms);
            }
        }
        
        private boolean checkCanceled(IProgressMonitor monitor)
        {
            if(monitor.isCanceled()) {
                return true;
            }
            if(mRequester.isCanceled()) {
                monitor.setCanceled(true);
                return true;
            }
            return false;
        }
    }

    private class HitDoc implements Comparable
    {
        float score;
        int id;
        
        public HitDoc(float s, int i) 
        {
            score = s;
            id = i;
        }

        public int compareTo(Object o)
        {
            float diff = ((HitDoc)o).score - score;
            return (diff >0?1:diff <0?-1:0);
        }

        public boolean equals(Object obj)
        {
            if(this != obj) {
                if(obj instanceof HitDoc) {
                    return (score == ((HitDoc)obj).score);
                }
                return false;
            }
            return true;
        }

        public int hashCode()
        {
            return (int)score;
        }
    }
}
/*******************************************************************************
 * Copyright (c) 2004-2010 Sunil Kamath (IcemanK).
 * All rights reserved.
 * This program is made available under the terms of the Common Public License
 * v1.0 which is available at http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * Sunil Kamath (IcemanK) - initial API and implementation
 *******************************************************************************/
package net.sf.eclipsensis.help.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.ExitableDirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.SimpleFSDirectory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;
import net.sf.eclipsensis.help.search.parser.NSISHelpSearchQueryParser;
import net.sf.eclipsensis.help.search.parser.ParseException;
import net.sf.eclipsensis.job.IJobStatusRunnable;
import net.sf.eclipsensis.util.Common;
import net.sf.eclipsensis.util.IOUtility;

public class NSISHelpSearcher implements INSISHelpSearchConstants {

    final Object JOB_FAMILY = new Object();
    private NSISHelpIndexer mIndexer;

    public NSISHelpSearcher(NSISHelpIndexer indexer) {

        mIndexer = indexer;
    }

    public void stopSearching() {

        mIndexer.getScheduler().cancelJobs(JOB_FAMILY);
    }

    public void search(String field, INSISHelpSearchRequester requester) {

        mIndexer.getScheduler().scheduleJob(JOB_FAMILY, EclipseNSISPlugin.getResourceString("nsis.help.searcher.job.title"), //$NON-NLS-1$
                new NSISHelpSearcherJob(field, requester));
    }

    private class NSISHelpSearcherJob implements IJobStatusRunnable {

        private INSISHelpSearchRequester mRequester;
        private String mField = null;
        private IndexSearcher mSearcher = null;
        private List<HitDoc> mHits = null;

        public NSISHelpSearcherJob(String field, INSISHelpSearchRequester requester) {

            mField = field;
            mRequester = requester;
        }

        @Override
        public IStatus run(final IProgressMonitor monitor) {

            List<NSISHelpSearchResult> results = Collections.emptyList();
            Collection<String> highlightTerms = Collections.emptySet();

            try {

                monitor.beginTask(EclipseNSISPlugin.getResourceString("searching.help.task.name"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

                if (!IOUtility.isValidDirectory(mIndexer.getIndexLocation())) {

                    mIndexer.indexHelp();
                }

                try {

                    Job.getJobManager().join(mIndexer.JOB_FAMILY, monitor);
                } catch (OperationCanceledException e) {

                    return Status.CANCEL_STATUS;
                } catch (InterruptedException e) {

                }

                if (!IOUtility.isValidDirectory(mIndexer.getIndexLocation())) {

                    return new Status(IStatus.ERROR, INSISConstants.PLUGIN_ID, IStatus.ERROR, EclipseNSISPlugin.getResourceString("search.index.not.created"), //$NON-NLS-1$
                            null);
                }

                if (checkCanceled(monitor)) {

                    return Status.CANCEL_STATUS;
                }
                Query query = null;

                try {

                    mSearcher = new IndexSearcher(ExitableDirectoryReader.open(new SimpleFSDirectory(mIndexer.getIndexLocation().toPath())));

                    if (checkCanceled(monitor)) {

                        return Status.CANCEL_STATUS;
                    }
                    QueryParser parser = new QueryParser(mField == null ? INDEX_FIELD_CONTENTS : mField, mIndexer.getAnalyzer());
                    query = parser.parse(mRequester.getSearchText());

                    if (checkCanceled(monitor)) {

                        return Status.CANCEL_STATUS;
                    }
                    Filter filter = mRequester.getFilter();
                    Collector collector = TopScoreDocCollector.create(10);
                    mHits = new ArrayList<HitDoc>();

                    if (filter != null) {

                        mSearcher.search(query, filter, collector);
                    }
                    else {

                        mSearcher.search(query, collector);
                    }
                } catch (Exception e) {

                    if (monitor.isCanceled()) {

                        return Status.CANCEL_STATUS;
                    }
                    EclipseNSISPlugin.getDefault().log(e);
                    return new Status(IStatus.ERROR, INSISConstants.PLUGIN_ID, IStatus.ERROR, e.getMessage(), e);
                } finally {

                    if (!monitor.isCanceled() && !Common.isEmptyCollection(mHits)) {

                        Collections.sort(mHits);
                        int n = mHits.size();
                        results = new ArrayList<NSISHelpSearchResult>(n);
                        int rank = 1;

                        for (int i = 0; i < n; i++) {

                            try {

                                Document doc = mSearcher.doc(mHits.get(i).id);
                                results.add(new NSISHelpSearchResult(doc.get(INDEX_FIELD_TITLE), doc.get(INDEX_FIELD_URL), rank++));
                            } catch (IOException e) {

                                EclipseNSISPlugin.getDefault().log(e);
                            }
                        }
                    }

                    if (mSearcher != null) {

                        mSearcher = null;
                    }

                    if (mHits != null) {

                        mRequester.queryParsed(query);

                        try {

                            highlightTerms = NSISHelpSearchQueryParser.parse(mField, mIndexer.getAnalyzer(), mRequester.getSearchText());
                        } catch (ParseException e) {

                            EclipseNSISPlugin.getDefault().log(e);
                            highlightTerms = Collections.emptySet();
                        }
                    }
                }
                return Status.OK_STATUS;
            } finally {

                monitor.done();
                mRequester.searchCompleted(results.toArray(new NSISHelpSearchResult[results.size()]), highlightTerms);
            }
        }

        private boolean checkCanceled(IProgressMonitor monitor) {

            if (monitor.isCanceled()) {

                return true;
            }

            if (mRequester.isCanceled()) {

                monitor.setCanceled(true);
                return true;
            }
            return false;
        }
    }

    private class HitDoc implements Comparable<HitDoc> {

        float score;
        int id;

        @Override
        public int compareTo(HitDoc o) {

            float diff = o.score - score;
            return (diff > 0 ? 1 : diff < 0 ? -1 : 0);
        }

        @Override
        public boolean equals(Object obj) {

            if (this != obj) {

                if (obj instanceof HitDoc) {

                    return (score == ((HitDoc) obj).score);
                }
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {

            return (int) score;
        }
    }
}

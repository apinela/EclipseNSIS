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

import java.io.File;

import net.sf.eclipsensis.EclipseNSISPlugin;
import net.sf.eclipsensis.INSISConstants;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

public class NSISHelpSearchManager implements INSISHelpSearchConstants
{
    private NSISHelpIndexer mStandardIndexer = null;
    private NSISHelpIndexer mStemmedIndexer = null;
    
    public NSISHelpSearchManager(File documentRoot)
    {
        File helpLocation = new File(EclipseNSISPlugin.getPluginStateLocation(),INSISConstants.PLUGIN_HELP_LOCATION_PREFIX);
        mStandardIndexer = new NSISHelpIndexer(new File(helpLocation, STANDARD_INDEX_LOCATION),documentRoot, new StandardAnalyzer());
        mStemmedIndexer = new NSISHelpIndexer(new File(helpLocation, STEMMED_INDEX_LOCATION),documentRoot, new StemmingAnalyzer());
    }

    public void stop()
    {
        stopIndexing();
        stopSearching();
    }

    public void search(INSISHelpSearchRequester requester)
    {
        search(INSISHelpSearchConstants.INDEX_FIELD_CONTENTS, requester);
    }

    public void search(String field, INSISHelpSearchRequester requester)
    {
        NSISHelpSearcher searcher = (requester.useStemming()?mStemmedIndexer.getSearcher():mStandardIndexer.getSearcher());
        searcher.search(field, requester);
    }

    public void stopSearching()
    {
        mStandardIndexer.getSearcher().stopSearching();
        mStemmedIndexer.getSearcher().stopSearching();
    }

    public void stopIndexing()
    {
        mStandardIndexer.stopIndexing();
        mStemmedIndexer.stopIndexing();
    }
 
    public void indexHelp()
    {
        mStandardIndexer.indexHelp();
        mStemmedIndexer.indexHelp();
    }
}

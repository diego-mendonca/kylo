package com.thinkbiganalytics.metadata.modeshape.feed;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thinkbiganalytics.metadata.api.category.Category;
import com.thinkbiganalytics.metadata.api.category.CategoryNotFoundException;
import com.thinkbiganalytics.metadata.api.datasource.Datasource;
import com.thinkbiganalytics.metadata.api.feed.Feed;
import com.thinkbiganalytics.metadata.api.feed.FeedDestination;
import com.thinkbiganalytics.metadata.api.feed.FeedPrecondition;
import com.thinkbiganalytics.metadata.api.feed.FeedSource;
import com.thinkbiganalytics.metadata.api.feedmgr.template.FeedManagerTemplate;
import com.thinkbiganalytics.metadata.modeshape.JcrVersionable;
import com.thinkbiganalytics.metadata.modeshape.MetadataRepositoryException;
import com.thinkbiganalytics.metadata.modeshape.category.JcrCategory;
import com.thinkbiganalytics.metadata.modeshape.common.AbstractJcrAuditableSystemEntity;
import com.thinkbiganalytics.metadata.modeshape.common.JcrEntity;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrDestination;
import com.thinkbiganalytics.metadata.modeshape.datasource.JcrSource;
import com.thinkbiganalytics.metadata.modeshape.support.JcrUtil;
import com.thinkbiganalytics.metadata.modeshape.template.JcrFeedTemplate;

import java.io.Serializable;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Created by sr186054 on 6/4/16.
 */
@JcrVersionable(nodeType = "tba:feed")
public class JcrFeed<C extends Category> extends AbstractJcrAuditableSystemEntity implements Feed<C> {

    public static final String NODE_TYPE = "tba:feed";
    public static final String SOURCE_NAME = "tba:sources";
    public static final String DESTINATION_NAME = "tba:destinations";
    public static final String CATEGORY = "tba:category";

    public static final String STATE = "tba:state";

    public static final String TEMPLATE = "tba:template";
    public static final String SCHEDULE_PERIOD = "tba:schedulingPeriod"; // Cron expression, or Timer Expression
    public static final String SCHEDULE_STRATEGY = "tba:schedulingStrategy"; //CRON_DRIVEN, TIMER_DRIVEN


    public JcrFeed(Node node) {
        super(node);
    }

    public JcrFeed(Node node, JcrCategory category) {
        super(node);
        setProperty(CATEGORY, category);
    }

    @Override
    public FeedId getId() {
        try {
            return new JcrFeed.FeedId(getObjectId());
        } catch (RepositoryException e) {
            throw new MetadataRepositoryException("Failed to retrieve the entity id", e);
        }
    }
    public static class FeedId extends JcrEntity.EntityId implements Feed.ID {

        public FeedId(Serializable ser) {
            super(ser);
        }
    }

    protected C getCategory(Class<? extends JcrCategory> categoryClass) {
        C category = null;
        try {
            category = (C) getProperty(JcrFeed.CATEGORY, categoryClass);
        } catch (Exception e) {
            if (category == null) {
                try {
                    category = (C) JcrUtil.constructNodeObject(node.getParent(), categoryClass, null);
                } catch (Exception e2) {
                    throw new CategoryNotFoundException("Unable to find category on Feed for category type  " + categoryClass + ". Exception: " + e.getMessage(), null);
                }
            }
        }
        if (category == null) {
            throw new CategoryNotFoundException("Unable to find category on Feed ", null);
        }
        return category;

    }

    public C getCategory() {

        return (C) getCategory(JcrCategory.class);
    }

    public FeedManagerTemplate getTemplate() {
        return getProperty(TEMPLATE, JcrFeedTemplate.class);
    }

    public void setTemplate(FeedManagerTemplate template) {
        setProperty(TEMPLATE, template);
    }

    public List<? extends FeedSource> getSources() {
        return JcrUtil.getNodes(this.node, SOURCE_NAME, JcrSource.class);
    }

    public List<? extends FeedDestination> getDestinations() {
        return JcrUtil.getNodes(this.node, DESTINATION_NAME, JcrDestination.class);
    }


    @Override
    public String getName() {
        return getSystemName();
    }

    @Override
    public String getDisplayName() {
        return getTitle();
    }

    @Override
    public State getState() {
        return null;
        //return
        //   getProperty(STATE);
    }

    @Override
    public boolean isInitialized() {
        return false;
    }

    @Override
    public FeedPrecondition getPrecondition() {
        return null;
    }

    @Override
    public FeedSource getSource(final Datasource.ID id) {
        FeedSource source = null;
        List<FeedSource> sources = (List<FeedSource>) getSources();
        if (sources != null && !sources.isEmpty()) {
            source = Iterables.tryFind(sources, new Predicate<FeedSource>() {
                @Override
                public boolean apply(FeedSource jcrSource) {
                    return jcrSource.getDatasource().equals(id);
                }
            }).orNull();
        }
        return source;
    }

    @Override
    public FeedSource getSource(final FeedSource.ID id) {
        FeedSource source = null;
        List<FeedSource> sources = (List<FeedSource>) getSources();
        if (sources != null && !sources.isEmpty()) {
            source = Iterables.tryFind(sources, new Predicate<FeedSource>() {
                @Override
                public boolean apply(FeedSource jcrSource) {
                    return jcrSource.getId().equals(id);
                }
            }).orNull();
        }
        return source;

    }

    @Override
    public FeedDestination getDestination(final Datasource.ID id) {
        FeedDestination destination = null;
        List<FeedDestination> destinations = (List<FeedDestination>) getDestinations();
        if (destinations != null && !destinations.isEmpty()) {
            destination = Iterables.tryFind(destinations, new Predicate<FeedDestination>() {
                @Override
                public boolean apply(FeedDestination jcrDestination) {
                    return jcrDestination.getDatasource().getId().equals(id);
                }
            }).orNull();
        }
        return destination;
    }

    @Override
    public FeedDestination getDestination(final FeedDestination.ID id) {

        FeedDestination destination = null;
        List<FeedDestination> destinations = (List<FeedDestination>) getDestinations();
        if (destinations != null && !destinations.isEmpty()) {
            destination = Iterables.tryFind(destinations, new Predicate<FeedDestination>() {
                @Override
                public boolean apply(FeedDestination jcrDestination) {
                    return jcrDestination.getId().equals(id);
                }
            }).orNull();
        }
        return destination;
    }

    @Override
    public void setInitialized(boolean flag) {

    }

    @Override
    public void setDisplayName(String name) {
        setTitle(name);
    }

    @Override
    public void setState(State state) {

    }


    public String getSchedulePeriod(){
        return getProperty(SCHEDULE_PERIOD,String.class);
    }
    public void setSchedulePeriod(String schedulePeriod){
        setProperty(SCHEDULE_PERIOD,schedulePeriod);
    }

    public String getScheduleStrategy(){
        return getProperty(SCHEDULE_STRATEGY,String.class);
    }
    public void setScheduleStrategy(String scheduleStrategy){
        setProperty(SCHEDULE_STRATEGY,scheduleStrategy);
    }

}

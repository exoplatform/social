package org.exoplatform.social.core.search;

import java.util.Collection;

import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.chromattic.entity.ProviderRootEntity;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.storage.query.JCRProperties;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public abstract class AbstractSocialSearchConnector extends SearchServiceConnector {

  private static final Log LOG = ExoLogger.getLogger(AbstractSocialSearchConnector.class);
  
  protected final class Range {

    public final int offset;
    public final int limit;

    public Range(int offset, int limit) {

      if (offset < 0) {
        throw new SocialSearchConnectorException("minimum offset is 0");
      }
      if (limit < 1) {
        throw new SocialSearchConnectorException("minimum limit is 1");
      }

      this.offset = offset;
      this.limit = limit;
    }

  }
  
  public AbstractSocialSearchConnector(InitParams initParams) {
    super(initParams);
  }

  @Override
  public final Collection<SearchResult> search(SearchContext context, String query, Collection<String> sites, int offset, int limit, String sort, String order) {

    //
    SortBy sortBy = null;
    if ("relevancy".equalsIgnoreCase(sort)) {
      sortBy = SortBy.RELEVANCY;
    } else if ("date".equalsIgnoreCase(sort)) {
      sortBy = SortBy.DATE;
    } else if ("title".equalsIgnoreCase(sort)) {
      sortBy = SortBy.TITLE;
    } else {
      throw new SocialSearchConnectorException("sort must be relevancy, date or title but is : " + sort);
    }

    //
    OrderBy orderBy = null;
    if ("ASC".equalsIgnoreCase(order)) {
      orderBy = OrderBy.ASC;
    } else if ("DESC".equalsIgnoreCase(order)) {
      orderBy = OrderBy.DESC;
    } else {
      throw new SocialSearchConnectorException("sort must be ASC or DESC but is : " + order);
    }

    return search(context, query, new Range(offset, limit), new Sorting(sortBy, orderBy));
  }
  
  /**
   * Gets RowIterator base on Statement
   * @param query
   * @param offset
   * @param limit
   * @return
   */
  protected RowIterator rows(String statement) throws Exception {
    QueryManager queryMgr = getJCRSession().getWorkspace().getQueryManager();
    Query query = queryMgr.createQuery(statement, Query.SQL);
    return query.execute().getRows();
  }
  
  /**
   * 
   * @return
   */
  private SocialChromatticLifeCycle lifecycleLookup() {

    PortalContainer container = PortalContainer.getInstance();
    ChromatticManager manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    return (SocialChromatticLifeCycle) manager.getLifeCycle(SocialChromatticLifeCycle.SOCIAL_LIFECYCLE_NAME);

  }
  
  private Session getJCRSession() {
    return lifecycleLookup().getSession().getJCRSession();
  }
  
  private ChromatticSession getSession() {
    return lifecycleLookup().getSession();
  }
  
  /**
   * Gets RowIterator with Statement with offset and limit
   * 
   * @param statement
   * @param offset
   * @param limit
   * @return
   * @throws Exception
   */
  protected RowIterator rows(String statement, long offset, long limit) {
    //
    if (statement == null) return null;
    
    //
    try {
      QueryManager queryMgr = getJCRSession().getWorkspace().getQueryManager();
      Query query = queryMgr.createQuery(statement, Query.SQL);
      if (query instanceof QueryImpl) {
        QueryImpl impl = (QueryImpl) query;
        
        //
        impl.setOffset(offset);
        impl.setLimit(limit);
        
        return impl.execute().getRows();
      }
      
      //
      return query.execute().getRows();
    } catch (Exception ex) {
      LOG.error(ex);
    }
    
    return null;
  }
  
  /**
   * Gets root path
   * @return
   */
  protected ProviderRootEntity getProviderRoot() {
    
    SocialChromatticLifeCycle lifeCycle = lifecycleLookup();
    if (lifeCycle.getProviderRoot().get() == null) {
      lifeCycle.getProviderRoot().set(getRoot("soc:providers", ProviderRootEntity.class));
    }
    return (ProviderRootEntity) lifeCycle.getProviderRoot().get();
  }
  
  /**
   * Get root
   * @param nodetypeName
   * @param t
   * @return
   */
  private <T> T getRoot(String nodetypeName, Class<T> t) {
    T got = getSession().findByPath(t, nodetypeName);
    if (got == null) {
      got = getSession().insert(t, nodetypeName);
    }
    return got;
  }
  /**
   * Gets Excerpt value from specified Row
   * @param row
   * @return Excerpt value if found, otherwise return NULL
   */
  protected String getExcerpt(Row row) {
    if (row == null) return null;
    
    try {
      Value value = row.getValue(JCRProperties.JCR_EXCERPT.getName());
      return value.getString() ;
    } catch (Exception ex) {
      
      LOG.warn("Excerpt is not found!");
      return null;
    }
    
  }
  
  /**
   * Gets Score value from specified Row
   * @param row
   * @return Score value
   */
  protected long getRelevancy(Row row) {
    if (row == null) return 0;
    
    try {
      Value value = row.getValue(JCRProperties.JCR_RELEVANCY.getName());
      return value.getLong();
    } catch (Exception ex) {
      
      LOG.warn("Relevancy is not found!");
      return 0;
    }
    
  }
  
  protected abstract Collection<SearchResult> search(SearchContext context, String query, Range range, Sorting sort);
}

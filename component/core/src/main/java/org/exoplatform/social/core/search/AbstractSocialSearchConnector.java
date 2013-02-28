package org.exoplatform.social.core.search;

import java.util.Collection;

import org.exoplatform.commons.api.search.SearchServiceConnector;
import org.exoplatform.commons.api.search.data.SearchContext;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.search.Sorting.OrderBy;
import org.exoplatform.social.core.search.Sorting.SortBy;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public abstract class AbstractSocialSearchConnector extends SearchServiceConnector {

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

  protected abstract Collection<SearchResult> search(SearchContext context, String query, Range range, Sorting sort);
}

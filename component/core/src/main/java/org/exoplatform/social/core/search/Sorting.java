package org.exoplatform.social.core.search;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class Sorting {

  public static enum OrderBy {
    ASC, DESC
  }

  public static enum SortBy {
    RELEVANCY, DATE, TITLE
  }

  public final SortBy sortBy;
  public final OrderBy orderBy;

  public Sorting(SortBy sortBy, OrderBy orderBy) {

    if (sortBy == null) {
      throw new SocialSearchConnectorException("sortBy cannot be null");
    }
    if (sortBy == null) {
      throw new SocialSearchConnectorException("orderBy cannot be null");
    }

    this.sortBy = sortBy;
    this.orderBy = orderBy;
  }

}

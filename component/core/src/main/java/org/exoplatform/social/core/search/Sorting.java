package org.exoplatform.social.core.search;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class Sorting implements Serializable {

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Sorting)) return false;

    Sorting sorting = (Sorting) o;

    if (orderBy != sorting.orderBy) return false;
    if (sortBy != sorting.sortBy) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = sortBy != null ? sortBy.hashCode() : 0;
    result = 31 * result + (orderBy != null ? orderBy.hashCode() : 0);
    return result;
  }

}

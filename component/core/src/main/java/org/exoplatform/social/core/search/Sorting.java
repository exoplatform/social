package org.exoplatform.social.core.search;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class Sorting implements Serializable, Cloneable {

  private static final long serialVersionUID = 5457261377640398889L;

  public static enum OrderBy {
    ASC, DESC
  }

  public static enum SortBy {
    RELEVANCY("relevancy"), DATE("date"), TITLE("title"), LASTNAME("lastName"), FIRSTNAME("firstName"), FULLNAME("fullName");

    private String fieldName;

    SortBy(String fieldName) {
      this.fieldName = fieldName;
    }

    public String getFieldName() {
      return fieldName;
    }
  }

  public final SortBy sortBy;
  public final OrderBy orderBy;

  public Sorting(SortBy sortBy, OrderBy orderBy) {

    if (sortBy == null) {
      throw new SocialSearchConnectorException("sortBy cannot be null");
    }
    if (orderBy == null) {
      throw new SocialSearchConnectorException("orderBy cannot be null");
    }

    this.sortBy = sortBy;
    this.orderBy = orderBy;
  }

  public static final Sorting valueOf(String sortByField, String orderByField) {
    if (StringUtils.isNotBlank(sortByField)) {
      SortBy sortBy = SortBy.valueOf(sortByField.toUpperCase());
      OrderBy orderBy = StringUtils.isBlank(orderByField) ? OrderBy.valueOf(orderByField) : OrderBy.ASC;
      return new Sorting(sortBy, orderBy);
    }
    return null;
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

  @Override
  public Sorting clone() throws CloneNotSupportedException {
    return (Sorting) super.clone();
  }
}

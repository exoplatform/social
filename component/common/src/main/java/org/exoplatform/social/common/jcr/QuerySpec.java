/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.common.jcr;

import java.util.ArrayList;
import java.util.Collection;

public class QuerySpec {

  public enum Operation {
    contains, equals, startsWith, present, greaterThan
  }

  public enum SortOrder {
    asc, desc
  }

  private int                   first;

  private int                   max;

  private Collection<Condition> conditions = new ArrayList<Condition>();

  private Collection<SortSpec>  sorts      = new ArrayList<SortSpec>();

  class SortSpec {
    private String    sortBy;

    private SortOrder sortOrder;

    public SortSpec(String sortBy, SortOrder sortOrder) {
      this.sortBy = sortBy;
      this.sortOrder = sortOrder;
    }

    public String toString() {
      return sortBy + " " + sortOrder;
    }
  }

  public void addSort(String sortBy, SortOrder sortOrder) {
    sorts.add(new SortSpec(sortBy, sortOrder));
  }

  class Condition {
    String    field;

    Operation operation;

    String    value;

    public Condition(String field, Operation operation, String value) {
      this.field = field;
      this.operation = operation;
      this.value = value;
    }

    public String toString() {
      switch (operation) {
      case contains:
        return "CONTAINS($" + field + "'" + value + "')";
      case equals:
        return "$" + field + "='" + value + "'";
      case startsWith:
        return "$" + field + " LIKE '" + value + "%'";
      case present:
        return "$" + field + " IS NOT NULL";
      case greaterThan:
        return "$" + field + ">'" + value + "'";
      default:
        return "";
      }
    }
  }

  public void addCondition(String field, Operation operation, String value) {
    conditions.add(new Condition(field, operation, value));
  }

  public String getJCRCondition() {
    StringBuilder sb = new StringBuilder();
    for (Condition condition : conditions) {
      if (sb.length() == 0) {
        sb.append(" AND ").append(condition);
      } else {
        sb.append(condition);
      }
    }

    return sb.toString();

  }

  public String getOrderByClause() {
    StringBuilder sbs = new StringBuilder();
    for (SortSpec sort : sorts) {
      if (sbs.length() == 0) {
        sbs.append(",").append(sort);
      } else {
        sbs.append(sort);
      }
    }
    return sbs.toString();
  }

  /**
   * When paginating, the index of the first item to fetch.
   *
   * @return the value of first
   */
  public int getFirst() {
    return first;
  }

  public void setFirst(int first) {
    this.first = first;
  }

  /**
   * The maximum number of items to fetch; defaults to 20. If set to a larger
   * number, a container may honor the request, or may limit the number to a
   * container-specified limit of at least 20.
   *
   * @return the value of max
   */
  public int getMax() {
    return max;
  }

  public void setMax(int max) {
    this.max = max;
  }

}

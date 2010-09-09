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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.impl.core.query.QueryImpl;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 15, 2010
 * Time: 5:42:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class QueryBuilder {
  private StringBuilder queryBuilder;
  private long limit = -1;
  private long offset= -1;
  private Session session;
  private final String WHERE = " WHERE";
  public static final String DESC = "DESC";
  public static final String ASC = "ASC";

  public QueryBuilder(Session session) {
    this.session = session;
  }

  public QueryBuilder select(String selector) {
    queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT * FROM ").append(selector).append(WHERE);
    return this;
  }

  public QueryBuilder select(String selector, long offset, long count) {
    this.offset = offset;
    this.limit = count;
    return select(selector);
  }

  public String getSQL() {
    assert queryBuilder != null;
    revalidateQueryBuilder();
    return queryBuilder.toString();
  }

  public long count() throws RepositoryException {
    assert queryBuilder != null;

    List<Node> nodes = new ArrayList<Node>();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) queryManager.createQuery(getSQL(), Query.SQL);

    //set paging
    if(offset != -1 & limit != -1){
      query.setOffset(offset);
      query.setLimit(limit);
    }

    QueryResult result = query.execute();
    return result.getNodes().getSize();
  }

  public List<Node> exec() throws RepositoryException {
    assert queryBuilder != null;

    List<Node> nodes = new ArrayList<Node>();
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) queryManager.createQuery(getSQL(), Query.SQL);

    //set paging
    if(offset != -1 & limit != -1){
      query.setOffset(offset);
      query.setLimit(limit);
    }

    QueryResult result = query.execute();
    NodeIterator nodeIterator = result.getNodes();

    while (nodeIterator.hasNext()){
      nodes.add(nodeIterator.nextNode());
    }

    return nodes;
  }

  public QueryBuilder and() {
    queryBuilder.append(" ").append("AND");
    return this;
  }

  public QueryBuilder or() {
    queryBuilder.append(" ").append("OR");
    return this;
  }

  public QueryBuilder group() {
    queryBuilder.append(" ").append("(");
    return this;
  }

  public QueryBuilder endGroup() {
    queryBuilder.append(" ").append(")");
    return this;
  }

  public QueryBuilder not() {
    queryBuilder.append(" ").append("NOT");
    return this;
  }

  public QueryBuilder contains(String property, String value) {
    queryBuilder.append(" ").append("CONTAINS(").append(property).append(", ").append("'").append(value).append("'").append(")");
    return this;
  }

  public QueryBuilder like(String property, String value) {
    queryBuilder.append(" ").append(property).append(" LIKE ").append("'").append(value).append("'");
    return this;
  }
  public QueryBuilder equal(String property, boolean value) {
    return equal(property, String.valueOf(value));
  }

  public QueryBuilder equal(String property, String value) {
    return appendComparison(property, "'"+value+"'", "=");
  }

  public QueryBuilder equal(String property, int value) {
    return appendComparison(property, value, "=");
  }

  public QueryBuilder greater(String property, int value) {
    return appendComparison(property, value, ">");
  }

  public QueryBuilder greaterEqual(String property, int value) {
    return appendComparison(property, value, ">=");
  }

  public QueryBuilder less(String property, int value) {
    return appendComparison(property, value, "<");
  }

  public QueryBuilder lessEqual(String property, int value) {
    return appendComparison(property, value, "<=");
  }

  public QueryBuilder notEqual(String property, int value) {
    return appendComparison(property, value, "<>");
  }

  private QueryBuilder appendComparison(String property, Object value, String comparator) {
    queryBuilder.append(" ").append(property).append(comparator).append(value);
    return this;
  }

  public String lower(String property) {
    return "LOWER("+property+")";
  }

  public String upper(String property) {
    return "UPPER("+property+")";
  }

  public QueryBuilder isNull(String property) {
    return appendComparison(property, "NULL", " IS ");
  }

  public QueryBuilder isNotNull(String property) {
    return appendComparison(property, "NULL", " IS NOT ");
  }

  public QueryBuilder orderBy(String property, String orderType) {
    revalidateQueryBuilder();
    queryBuilder.append(" ").append("ORDER BY ").append(property).append(" ").append(orderType);
    return this;
  }

  public void setOffset(long offset) {
    this.offset = offset;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  private void revalidateQueryBuilder() {
    String query = queryBuilder.toString();
    if(query.endsWith(WHERE)){
      query = query.replace(WHERE,"");
    }
    queryBuilder = new StringBuilder(query);
  }
}
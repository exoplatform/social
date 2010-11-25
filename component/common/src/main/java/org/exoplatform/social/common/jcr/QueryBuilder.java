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
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.exoplatform.services.jcr.impl.core.query.QueryImpl;

/**
 * Query Builder
 *
 * @author Zun
 * @since Nov 10, 2010
 */
public class QueryBuilder {
  private StringBuilder queryBuilder;
  private long limit = -1;
  private long offset= -1;
  private String[] propertyNames;
  private Session session;
  private final String WHERE = " WHERE";
  public static final String DESC = "DESC";
  public static final String ASC = "ASC";

  /**
   * Constructor
   * @param session
   */
  public QueryBuilder(Session session) {
    this.session = session;
  }

  /**
   * Set query selector. Use this for retrieving all data of that selector
   * @param selector node type string                                     .
   * @return QueryBuilder instance
   */
  public QueryBuilder select(String selector) {
    queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT * FROM ").append(selector).append(WHERE);
    return this;
  }

  /**
   * Set the selector that query will select data from and the paging configs
   * @param selector the node type querying from
   * @param offset offset for paging
   * @param count paging size
   * @return QueryBuilder instance
   */
  public QueryBuilder select(String selector, long offset, long count) {
    this.offset = offset;
    this.limit = count;
    return select(selector);
  }

  /**
   * Set the query selector and set property for retrieving data partly.
   * @param selector node type string
   * @param property property of that node type
   * @return QueryBuilder instance
   */
  public QueryBuilder select(String selector, String property) throws Exception {
    return select(selector, new String[]{property});
  }

  /**
   * Set the query selector and set property for retrieving data partly.
   * @param selector node type string
   * @param property property of that node type
   * @param offset offset for paging
   * @param count paging size
   * @return QueryBuilder instance
   */
  public QueryBuilder select(String selector, String property, long offset, long count) throws Exception {
    this.offset = offset;
    this.limit = count;
    return select(selector, new String[]{property});
  }

  /**
   * Set the query selector and set properties for retrieving data partly.
   * @param selector node type string
   * @param properties properties of that node type
   * @return QueryBuilder instance
   */
  public QueryBuilder select(String selector, String... properties) throws Exception {
    if (properties == null || properties.length == 0) {
      throw new Exception("Properties must be not null or empty");
    }
    this.propertyNames = properties;
    queryBuilder = new StringBuilder();
    queryBuilder.append("SELECT ").append(properties[0]);
    for (int i = 1; i < properties.length; i++) {
      queryBuilder.append(",").append(properties[i]);
    }
    queryBuilder.append(" FROM ").append(selector).append(WHERE);
    return this;
  }

  /**
   * Set the query selector and set properties for retrieving data partly.
   * @param selector node type string
   * @param properties properties of that node type
   * @param offset offset for paging
   * @param count paging size
   * @return QueryBuilder instance
   */
  public QueryBuilder select(String selector, long offset, long count, String... properties) throws Exception {
    this.offset = offset;
    this.limit = count;
    return select(selector, properties);
  }
  
  /**
   * Open group of operators
   * @return QueryBuilder instance
   */
  public QueryBuilder group() {
    queryBuilder.append(" (");
    return this;
  }

  /**
   * Close group of operators
   * @return QueryBuilder instance
   */
  public QueryBuilder endGroup() {
    queryBuilder.append(" )");
    return this;
  }

  /**
   * appends a CONTAINS predicate in the form : CONTAINS(property, 'value')
   * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder contains(String property, String value) {
    queryBuilder.append(" CONTAINS(").append(property).append(", ").append("'").append(value).append("'").append(")");
    return this;
  }

  /**
   * Appends a LIKE predicate in the form : property LIKE 'value'
   * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder like(String property, String value) {
    queryBuilder.append(" ").append(property).append(" LIKE ").append("'").append(value).append("'");
    return this;
  }

  /**
   * AND operator
   * @return QueryBuilder instance
   */
  public QueryBuilder and() {
    queryBuilder.append(" AND");
    return this;
  }

  /**
   * OR operator
   * @return QueryBuilder instance
   */
  public QueryBuilder or() {
    queryBuilder.append(" OR");
    return this;
  }

  /**
   * NOT operator
   * @return QueryBuilder instance
   */
  public QueryBuilder not() {
    queryBuilder.append(" NOT");
    return this;
  }

  /**
   * EQUAL operator
    * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder equal(String property, boolean value) {
    return equal(property, String.valueOf(value));
  }

  /**
   * EQUAL operator
   * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder equal(String property, String value) {
    return appendComparison(property, "'"+value+"'", "=");
  }

  /**
   * EQUAL operator
    * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder equal(String property, int value) {
    return appendComparison(property, value, "=");
  }

  /**
   * GREATER operator
   * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder greater(String property, int value) {
    return appendComparison(property, value, ">");
  }

  /**
   * GREATER OR EQUAL operator
   * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder greaterEqual(String property, int value) {
    return appendComparison(property, value, ">=");
  }

  /**
   * LESS operator
   * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder less(String property, int value) {
    return appendComparison(property, value, "<");
  }

  /**
   * LESS OR EQUAL operator
   * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder lessEqual(String property, int value) {
    return appendComparison(property, value, "<=");
  }

  /**
   * NOT EQUAL operator
   * @param property name of property
   * @param value property value for matching
   * @return QueryBuilder instance
   */
  public QueryBuilder notEqual(String property, int value) {
    return appendComparison(property, value, "<>");
  }

  private QueryBuilder appendComparison(String property, Object value, String comparator) {
    queryBuilder.append(" ").append(property).append(comparator).append(value);
    return this;
  }

  /**
   * LOWER operator
   * @param property name of property
   * @return LOWER query string
   */
  public String lower(String property) {
    return "LOWER("+property+")";
  }

  /**
   * UPPER operator
   * @param property name of property
   * @return UPPER query string
   */
  public String upper(String property) {
    return "UPPER("+property+")";
  }

  /**
   * IS NULL operator
   * @param property name of property
   * @return QueryBuilder instance
   */
  public QueryBuilder isNull(String property) {
    return appendComparison(property, "NULL", " IS ");
  }

  /**
   * IS NOT NULL operator
   * @param property name of property
   * @return QueryBuilder instance
   */
  public QueryBuilder isNotNull(String property) {
    return appendComparison(property, "NULL", " IS NOT ");
  }

  /**
   * ORDER BY operator
   * @param property name of property
   * @param orderType DESC or ASC
   * @return QueryBuilder instance
   */
  public QueryBuilder orderBy(String property, String orderType) {
    revalidateQueryBuilder();
    queryBuilder.append(" ORDER BY ").append(property).append(" ").append(orderType);
    return this;
  }

  /**
   * Get the number of results of current query string
   * @return the number of results
   * @throws RepositoryException
   */
  public long count() throws RepositoryException {
    assert queryBuilder != null;

    QueryResult result = query();
    return result.getNodes().getSize();
  }

  /**
   * Find node of current query string.
   * @return the result node
   * @throws Exception
   */
  public Node findNode() throws Exception {
    List<Node> result = exec();
    if (result.size() == 1) {
      return result.get(0);
    } else if (result.size() > 1) {
      throw new Exception("Query result has more than one node.");
    } else {
      return null;
    }
  }

  /**
   * Get list of result nodes of current query string.
   * @return the query results
   * @throws Exception
   */
  public List<Node> exec() throws RepositoryException {
    assert queryBuilder != null;

    List<Node> nodes = new ArrayList<Node>();
    QueryResult result = query();
    NodeIterator nodeIterator = result.getNodes();

    while (nodeIterator.hasNext()){
      nodes.add(nodeIterator.nextNode());
    }

    return nodes;
  }

  /**
   * Get list of property values of current query string.
   * @return the query results
   * @throws Exception
   */
  public List<Value> execProperty() throws Exception {
    if (propertyNames == null || propertyNames.length == 0) {
      throw new Exception("Property name is not specified");
    }

    QueryResult result = query();
    RowIterator rowIterator = result.getRows();

    List<Value> values = new ArrayList<Value>();
    while (rowIterator.hasNext()){
      Row row = rowIterator.nextRow();
      values.add(row.getValue(propertyNames[0]));
    }

    return values;
  }

  /**
   * Get list of properties values of current query string.
   * @return the query results
   * @throws Exception
   */
  public List<Map<String,Value>> execProperties() throws Exception {
    if (propertyNames == null || propertyNames.length ==0) {
      throw new Exception("Property names are not specified");
    }

    QueryResult result = query();
    RowIterator rowIterator = result.getRows();

    List<Map<String, Value>> values = new ArrayList<Map<String, Value>>();
    while (rowIterator.hasNext()){
      Row row = rowIterator.nextRow();
      Map<String, Value> dic = new Hashtable<String, Value>();
      for (int i = 0; i < propertyNames.length; i++) {
        String name = propertyNames[i];
        dic.put(name,row.getValue(name));
      }
      values.add(dic);
    }

    return values;
  }

  /**
   * Get current SQL string
   * @return SQL string
   */
  public String getSQL() {
    assert queryBuilder != null;
    revalidateQueryBuilder();
    return queryBuilder.toString();
  }

  /**
   * Set offset for paging
   * @param offset
   */
  public void setOffset(long offset) {
    this.offset = offset;
  }

  /**
   * Set size for paging
   * @param limit
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }

  private QueryResult query() throws RepositoryException {
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    QueryImpl query = (QueryImpl) queryManager.createQuery(getSQL(), Query.SQL);

    //set paging
    if(offset != -1 & limit != -1){
      query.setOffset(offset);
      query.setLimit(limit);
    }

    QueryResult result = query.execute();
    return result;
  }
  
  private void revalidateQueryBuilder() {
    String query = queryBuilder.toString();
    if(query.endsWith(WHERE)){
      query = query.replace(WHERE,"");
    }
    queryBuilder = new StringBuilder(query);
  }
}
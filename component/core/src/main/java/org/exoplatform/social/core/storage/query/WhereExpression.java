/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.exoplatform.social.core.storage.query;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class WhereExpression {

  private final StringBuilder builder;

  private int openGroup = 0;

  public WhereExpression() {
    builder = new StringBuilder();
  }

  public <T> WhereExpression equals(PropertyLiteralExpression<T> property, T value) {
    checkParam(property, value);

    builder.append(String.format("%s = '%s' ", property.getName(), value));
    return this;
  }

  public <T> WhereExpression like(PropertyLiteralExpression<T> property, T value) {
    checkParam(property, value);

    builder.append(String.format("%s LIKE '%s' ", property.getName(), value));
    return this;
  }

  public <T> WhereExpression like(CallExpression<T> call, T value) {
    checkParam(call.getProperty(), value);

    builder.append(String.format("%s(%s) LIKE '%s' ", call.getFunction(), call.getProperty().getName(), value));
    return this;
  }

  public <T> WhereExpression contains(PropertyLiteralExpression<T> property, T value) {

    checkParam(property, value);

    builder.append(String.format("CONTAINS (%s, '%s') ", property.getName(), value));
    return this;
  }

  public <T> WhereExpression orderBy(PropertyLiteralExpression<T> property, Order order) {
    builder.append(String.format("ORDER BY %s %s", property.getName(), order.toString()));
    return this;
  }

  public WhereExpression startGroup() {
    builder.append("(");
    ++openGroup;
    return this;
  }

  public WhereExpression endGroup() {
    builder.append(") ");
    --openGroup;
    return this;
  }

  public WhereExpression endAllGroup() {
    while (openGroup > 0) {
      endGroup();
    }
    return this;
  }

  public WhereExpression not() {
    builder.append("NOT ");
    return this;
  }

  public WhereExpression or() {
    builder.append("OR ");
    return this;
  }

  public WhereExpression and() {
    builder.append("AND ");
    return this;
  }

  public String toString() {
    return builder.toString();
  }

  public <T> CallExpression callFunction(QueryFunction function, PropertyLiteralExpression<T> property) {
    return new CallExpression(function, property);
  }

  private <T> void checkParam(PropertyLiteralExpression<T> property, T value) {
    if (!property.getType().equals(value.getClass())) {
      throw new IllegalArgumentException();
    }
  }
  
}

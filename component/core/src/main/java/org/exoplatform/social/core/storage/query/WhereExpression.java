/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.storage.query;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class WhereExpression {

  private static final String QUOTED = "'%s'";
  private static final String DIRECT = "%s";

  private final StringBuilder builder;

  private int openGroup = 0;

  public WhereExpression() {
    builder = new StringBuilder();
  }

  public <T> WhereExpression equals(PropertyLiteralExpression<T> property, T value) {
    checkParam(property, value);

    builder.append(String.format("%s = %s ", property.getName(), espace(property, value)));
    return this;
  }

  public <T> WhereExpression lesser(PropertyLiteralExpression<T> property, T value) {
    checkParam(property, value);

    builder.append(String.format("%s < %s ", property.getName(), value));
    return this;
  }

  public <T> WhereExpression lessEq(PropertyLiteralExpression<T> property, T value) {
    checkParam(property, value);

    builder.append(String.format("%s <= %s ", property.getName(), value));
    return this;
  }

  public <T> WhereExpression greater(PropertyLiteralExpression<T> property, T value) {
    checkParam(property, value);

    builder.append(String.format("%s > %s ", property.getName(), espace(property, value)));
    return this;
  }

  public <T> WhereExpression greaterEq(PropertyLiteralExpression<T> property, T value) {
    checkParam(property, value);

    builder.append(String.format("%s >= '%s' ", property.getName(), value));
    return this;
  }

  public <T> WhereExpression like(PropertyLiteralExpression<T> property, T value) {
    checkParam(property, value);

    builder.append(String.format("%s LIKE %s ", property.getName(), espace(property, value)));
    return this;
  }

  public <T> WhereExpression like(CallExpression<T> call, T value) {
    checkParam(call.getProperty(), value);

    builder.append(String.format("%s(%s) LIKE %s ", call.getFunction(), call.getProperty().getName(), espace(call.getProperty(),
                                 value)));
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

  private <T> String espace(PropertyLiteralExpression<T> property, T value) {
    String format = (property.getType().equals(Long.class) ? DIRECT : QUOTED);

    if (value instanceof String) {
      String strValue = ((String) value).replaceAll("'", "''");
      return String.format(format, strValue);
    }

    return String.format(format, value);
  }

  private <T> void checkParam(PropertyLiteralExpression<T> property, T value) {
    if (!property.getType().equals(value.getClass())) {
      throw new IllegalArgumentException();
    }
  }
  
}

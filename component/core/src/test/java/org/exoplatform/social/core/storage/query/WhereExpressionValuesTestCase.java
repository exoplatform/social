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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class WhereExpressionValuesTestCase extends TestCase {

  public void testStringEqual() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression.equals(FakeEntity.strProperty, "foo");
    assertEquals("strProperty = 'foo' ", expression.toString());
    
  }

  public void testIntegerEqual() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression.equals(FakeEntity.intProperty, 42);
    assertEquals("intProperty = '42' ", expression.toString());

  }

  public void testStringLike() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression.like(FakeEntity.strProperty, "foo");
    assertEquals("strProperty LIKE 'foo' ", expression.toString());

  }

  public void testIntegerLike() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression.like(FakeEntity.intProperty, 42);
    assertEquals("intProperty LIKE '42' ", expression.toString());

  }

  public void testStringContains() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression.contains(FakeEntity.strProperty, "foo");
    assertEquals("CONTAINS (strProperty, 'foo') ", expression.toString());

  }

  public void testIntegerContains() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression.contains(FakeEntity.intProperty, 42);
    assertEquals("CONTAINS (intProperty, '42') ", expression.toString());

  }

  public void testOr() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression
        .equals(FakeEntity.intProperty, 42)
        .or()
        .like(FakeEntity.strProperty, "foo");
    assertEquals("intProperty = '42' OR strProperty LIKE 'foo' ", expression.toString());

  }

  public void testAnd() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression
        .equals(FakeEntity.intProperty, 42)
        .and()
        .like(FakeEntity.strProperty, "foo");
    assertEquals("intProperty = '42' AND strProperty LIKE 'foo' ", expression.toString());

  }

  public void testNotEquals() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression
        .not()
        .equals(FakeEntity.strProperty, "foo");
    assertEquals("NOT strProperty = 'foo' ", expression.toString());

  }

  public void testNotLIKE() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression
        .not()
        .like(FakeEntity.strProperty, "foo");
    assertEquals("NOT strProperty LIKE 'foo' ", expression.toString());

  }

  public void testNotContains() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression
        .not()
        .contains(FakeEntity.strProperty, "foo");
    assertEquals("NOT CONTAINS (strProperty, 'foo') ", expression.toString());

  }

  public void testGroup() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression
        .equals(FakeEntity.strProperty, "foo")
        .and()
        .startGroup()
          .contains(FakeEntity.strProperty, "bar")
          .or()
          .like(FakeEntity.intProperty, 42)
        .endGroup();

    assertEquals(
        "strProperty = 'foo' AND (CONTAINS (strProperty, 'bar') OR intProperty LIKE '42' ) ",
        expression.toString()
    );

  }

  public void testEndAllGroup() throws Exception {

    WhereExpression expression = new WhereExpression();
    expression
        .startGroup()
          .equals(FakeEntity.strProperty, "foo")
          .and()
          .startGroup()
            .contains(FakeEntity.strProperty, "bar")
            .or()
            .startGroup()
              .like(FakeEntity.intProperty, 42)
            .endGroup()
            .and()
            .equals(FakeEntity.intProperty, 43)
        .endAllGroup();

    assertEquals(
        "(strProperty = 'foo' AND (CONTAINS (strProperty, 'bar') OR (intProperty LIKE '42' ) AND intProperty = '43' ) ) ",
        expression.toString()
    );

  }

  public void testLowerFunction() throws Exception {

    WhereExpression expression = new WhereExpression();
    CallExpression<String> callExpression = expression.callFunction(QueryFunction.LOWER, FakeEntity.strProperty);
    expression.like(callExpression, "foo");
    assertEquals("LOWER(strProperty) LIKE 'foo' ", expression.toString());
    
  }

  public void testUpperFunction() throws Exception {

    WhereExpression expression = new WhereExpression();
    CallExpression<String> callExpression = expression.callFunction(QueryFunction.UPPER, FakeEntity.strProperty);
    expression.like(callExpression, "foo");
    assertEquals("UPPER(strProperty) LIKE 'foo' ", expression.toString());

  }
}

class FakeEntity {

  public static final PropertyLiteralExpression<String> strProperty = new PropertyLiteralExpression<String>(String.class, "strProperty");
  public static final PropertyLiteralExpression<Integer> intProperty = new PropertyLiteralExpression<Integer>(Integer.class, "intProperty");

}

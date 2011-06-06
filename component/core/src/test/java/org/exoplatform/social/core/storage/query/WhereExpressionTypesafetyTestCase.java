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

import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class WhereExpressionTypesafetyTestCase extends TestCase {

  public void testEqualsTypeSafety() throws Exception {
    WhereExpression expression = new WhereExpression();

    expression.equals(new PropertyLiteralExpression(String.class, "name"), "foo");

    try {
      expression.equals(new PropertyLiteralExpression(String.class, "name"), 42);
      fail();
    }
    catch (IllegalArgumentException e) {
      // ok;
    }
  }

  public void testLikeTypeSafety() throws Exception {
    WhereExpression expression = new WhereExpression();

    expression.like(new PropertyLiteralExpression(String.class, "name"), "foo");

    try {
      expression.like(new PropertyLiteralExpression(String.class, "name"), 42);
      fail();
    }
    catch (IllegalArgumentException e) {
      // ok;
    }
  }

  public void testContainsTypeSafety() throws Exception {
    WhereExpression expression = new WhereExpression();

    expression.like(new PropertyLiteralExpression(String.class, "name"), "foo");

    try {
      expression.like(new PropertyLiteralExpression(String.class, "name"), 42);
      fail();
    }
    catch (IllegalArgumentException e) {
      // ok;
    }
  }

  public void testFunctionTypeSafety() throws Exception {
    WhereExpression expression = new WhereExpression();

    PropertyLiteralExpression literalExpression = new PropertyLiteralExpression(String.class, "name");

    CallExpression lowerExpression = expression.callFunction(QueryFunction.LOWER, literalExpression);
    CallExpression upperExpression = expression.callFunction(QueryFunction.UPPER, literalExpression);

    expression.like(lowerExpression, "foo");
    expression.like(upperExpression, "foo");

    try {
      expression.like(lowerExpression, 42);
      fail();
    }
    catch (IllegalArgumentException e) {
      // ok;
    }

    try {
      expression.like(upperExpression, 42);
      fail();
    }
    catch (IllegalArgumentException e) {
      // ok;
    }
  }
}

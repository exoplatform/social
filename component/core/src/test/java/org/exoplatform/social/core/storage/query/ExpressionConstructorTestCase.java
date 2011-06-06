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

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ExpressionConstructorTestCase extends TestCase {

  public void testLiteralExpression() throws Exception {

    new PropertyLiteralExpression(String.class, "");

    try {
      new PropertyLiteralExpression(String.class, null);
    }
    catch (NullPointerException e) {
      // ok;
    }

    try {
      new PropertyLiteralExpression(null, "");
    }
    catch (NullPointerException e) {
      // ok;
    }
    
    try {
      new PropertyLiteralExpression(null, null);
    }
    catch (NullPointerException e) {
      // ok;
    }

  }

  public void testCallExpression() throws Exception {

    PropertyLiteralExpression property = new PropertyLiteralExpression(String.class, "");
    new CallExpression(QueryFunction.LOWER, property);

    try {
      new CallExpression(QueryFunction.LOWER, null);
    }
    catch (NullPointerException e) {
      // ok;
    }

    try {
      new CallExpression(null, property);
    }
    catch (NullPointerException e) {
      // ok;
    }

    try {
      new CallExpression(null, null);
    }
    catch (NullPointerException e) {
      // ok;
    }

  }

}

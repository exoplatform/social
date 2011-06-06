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
public class CallExpression <T> {

  private final QueryFunction function;
  private final PropertyLiteralExpression<T> property;

  public CallExpression(final QueryFunction function, final PropertyLiteralExpression<T> property) {

    if (function == null) {
      throw new NullPointerException();
    }
    
    if (property == null) {
      throw new NullPointerException();
    }

    this.function = function;
    this.property = property;
  }

  public QueryFunction getFunction() {
    return function;
  }

  public PropertyLiteralExpression<T> getProperty() {
    return property;
  }

}

/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.common.jcr.filter;

import org.exoplatform.social.common.jcr.filter.FilterLiteral.DIRECTION;
import org.exoplatform.social.common.jcr.filter.FilterLiteral.FilterOption;
import org.exoplatform.social.common.jcr.filter.FilterLiteral.OrderByOption;

import junit.framework.TestCase;

public class FilterLiteralTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testAppendFiler() throws Exception {
    assertNotNull(FooFilter.FOO_APPEND_FILTER);
    
    //FOO
    {
      FilterOption<FieldLiteral<?>> fooFieldFilter = FooFilter.FOO_APPEND_FILTER.get(FooFilter.FOO_FILTER_OPTION);
      assertNull(fooFieldFilter.getValue());
      assertEquals("foo", fooFieldFilter.getLiteral().getName());

      // set value
      fooFieldFilter.value("10");
      fooFieldFilter = FooFilter.FOO_APPEND_FILTER.get(FooFilter.FOO_FILTER_OPTION);
      assertEquals("10", fooFieldFilter.getValue());
    }
    
    //BAR
    {
      FilterOption<FieldLiteral<?>> barFieldFilter = FooFilter.FOO_APPEND_FILTER.get(FooFilter.BAR_FILTER_OPTION);
      assertNull(barFieldFilter.getValue());
      assertEquals("bar", barFieldFilter.getLiteral().getName());

      // set value
      barFieldFilter.value("15");
      barFieldFilter = FooFilter.FOO_APPEND_FILTER.get(FooFilter.BAR_FILTER_OPTION);
      assertEquals("15", barFieldFilter.getValue());
    }
  }
  
  public void testWithFiler() throws Exception {
    assertNotNull(FooFilter.BAR_ORDERBY_FILTER);
    
    //FOO
    {
      FilterOption<FieldLiteral<?>> fooFieldFilter = FooFilter.BAR_WITH_FILTER.get(FooFilter.FOO_FILTER_OPTION);
      assertEquals("foo", fooFieldFilter.getLiteral().getName());
      assertEquals("foo", fooFieldFilter.getValue());
    }
    
    //BAR
    {
      FilterOption<FieldLiteral<?>> barFieldFilter = FooFilter.BAR_WITH_FILTER.get(FooFilter.BAR_FILTER_OPTION);
      assertEquals("bar", barFieldFilter.getLiteral().getName());
      assertEquals("bar", barFieldFilter.getValue());
    }
  }
  
  public void testWithOrderBy() throws Exception {
    assertNotNull(FooFilter.BAR_ORDERBY_FILTER);
    
    //FOO
    {
      OrderByOption<FieldLiteral<?>> fooOrder = FooFilter.BAR_ORDERBY_FILTER.get(FooFilter.FOO_ORDER_BY);
      assertEquals("foo", fooOrder.getLiteral().getName());
      assertEquals(DIRECTION.DESC, fooOrder.getDirection());
    }
    
    //BAR
    {
      OrderByOption<FieldLiteral<?>> barOrder = FooFilter.BAR_ORDERBY_FILTER.get(FooFilter.BAR_ORDER_BY);
      assertEquals("bar", barOrder.getLiteral().getName());
      assertEquals(DIRECTION.ASC, barOrder.getDirection());
    }
  }

  
}

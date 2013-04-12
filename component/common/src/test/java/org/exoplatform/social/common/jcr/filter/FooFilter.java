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

import org.exoplatform.social.common.jcr.filter.FilterLiteral.FilterOption;
import org.exoplatform.social.common.jcr.filter.FilterLiteral.OrderByOption;

public class FooFilter {

  public static FilterOption<FieldLiteral<?>> FOO_FILTER_OPTION = new FilterOption<FieldLiteral<?>>(FooEntity.foo);
  public static FilterOption<FieldLiteral<?>> BAR_FILTER_OPTION = new FilterOption<FieldLiteral<?>>(FooEntity.bar);
  public static OrderByOption<FieldLiteral<?>> FOO_ORDER_BY = new OrderByOption<FieldLiteral<?>>(FooEntity.foo);
  public static OrderByOption<FieldLiteral<?>> BAR_ORDER_BY = new OrderByOption<FieldLiteral<?>>(FooEntity.bar);
  
  public static AbstractFilterLiteral<FieldLiteral<?>> FOO_APPEND_FILTER = new AbstractFilterLiteral<FieldLiteral<?>>() {
    
    @Override
    protected void start() {
      try {
        this.append(FOO_FILTER_OPTION.clone())
        .append(BAR_FILTER_OPTION.clone());
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      
    }

    @Override
    public void destroy() {
      
    }
  };
  
  public static AbstractFilterLiteral<FieldLiteral<?>> BAR_WITH_FILTER = new AbstractFilterLiteral<FieldLiteral<?>>() {
    
    @Override
    protected void start() {
      try {
        this.with(FOO_FILTER_OPTION.clone()).value("foo")
            .with(BAR_FILTER_OPTION.clone()).value("bar");
      } catch (Exception ex) {
        
      }
      
    }
    
    @Override
    public void destroy() {
      
    }
  };
  
  public static AbstractFilterLiteral<FieldLiteral<?>> BAR_ORDERBY_FILTER = new AbstractFilterLiteral<FieldLiteral<?>>() {
    
    @Override
    protected void start() {
      try {
        this.with(FOO_FILTER_OPTION.clone()).value("foo")
            .with(FOO_ORDER_BY.clone()).direction(DIRECTION.DESC)
            .with(BAR_ORDER_BY.clone()).direction(DIRECTION.ASC);
      } catch (Exception ex) {
        
      }
      
    }
    
    @Override
    public void destroy() {
      
    }
  };
}

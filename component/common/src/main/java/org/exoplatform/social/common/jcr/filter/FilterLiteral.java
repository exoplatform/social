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

import java.util.Iterator;


public interface FilterLiteral<P> {

  public enum DIRECTION {
    ASC,
    DESC
  }
  
  /**
   * Append the filter option.
   * @param param
   * @return
   */
  FilterLiteral<P> append(FilterOption<P> filter);
  
  /**
   * plug a filter in FilterLiteral
   * @param filter
   * @return
   */
  FilterOption<P> with(FilterOption<P> filter);
  
  /**
   * Append the orderBy option.
   * @param param
   * @return
   */
  FilterLiteral<P> append(OrderByOption<P> orderBy);
  
  /**
   * plug a filter in FilterLiteral
   * @param filter
   * @return
   */
  OrderByOption<P> with(OrderByOption<P> orderBy);
  
  /**
   * Removes the query parameter.
   * @param param
   * @return
   */
  FilterLiteral<P> remove(FilterOption<P> filter);
  
  /**
   * Removes the query parameter.
   * @param param
   * @return
   */
  FilterLiteral<P> remove(OrderByOption<P> orderBy);
  
  /**
   * Clear all of filter optional
   */
  void clear();
  /**
   * Gets FilterOption which was existing.
   * @param param
   * @return
   */
  FilterOption<P> get(FilterOption<P> filter);
  
  /**
   * Gets OrderByOption which was existing.
   * @param param
   * @return
   */
  OrderByOption<P> get(OrderByOption<P> param);
  
  /**
   * Gets OrderByOption iterator
   * @return
   */
  Iterator<OrderByOption<P>> getOrders();
  
  
  public static class FilterOption<P> implements Cloneable {
    private final P p;
    private Object value;
    private FilterLiteral<P> target;
    
    public FilterOption(P p, Object value) {
      this.p = p;
      this.value = value;
    }
    
    public FilterOption(P p) {
      this.p = p;
      this.value = null;
    }

    public P getLiteral() {
      return p;
    }
    
    public void setTarget(FilterLiteral<P> target) {
      this.target = target;
    }

    public Object getValue() {
      return value;
    }
    
    public FilterLiteral<P> value(Object value) {
      this.value = value;
      return target;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      
      FilterOption<?> other = null;
      if (obj instanceof FilterOption) {
        other = (FilterOption<?>) obj;
      } else {
        return false;
      }
      
      return this.p.equals(other.p);
    }
    
    
    public FilterOption<P> clone() throws CloneNotSupportedException {
      Object obj = super.clone();
      return (obj instanceof  FilterOption<?>) ? (FilterOption<P>) obj : null;
    }
  }
  /**
   * Defines the OrderBy Optional
   * @author thanh_vucong
   *
   * 
   */
  public static class OrderByOption<P> implements Cloneable {
    private final P p;
    private DIRECTION direction;
    private FilterLiteral<P> target;
    
    public OrderByOption(P p, DIRECTION direction) {
      this.p = p;
      this.direction = direction;
    }
    
    public OrderByOption(P p) {
      this.p = p;
      this.direction = DIRECTION.ASC;
    }

    public P getLiteral() {
      return p;
    }
    
    public void setTarget(FilterLiteral<P> target) {
      this.target = target;
    }

    public DIRECTION getDirection() {
      return this.direction;
    }
    
    public FilterLiteral<P> direction(DIRECTION direction) {
      this.direction = direction;
      return target;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      
      OrderByOption<?> other = null;
      if (obj instanceof OrderByOption) {
        other = (OrderByOption<?>) obj;
      } else {
        return false;
      }
      
      return this.p.equals(other.p);
    }
    
    
    public OrderByOption<P> clone() throws CloneNotSupportedException {
      Object obj = super.clone();
      return (obj instanceof  OrderByOption<?>) ? (OrderByOption<P>) obj : null;
    }
    
  }
}

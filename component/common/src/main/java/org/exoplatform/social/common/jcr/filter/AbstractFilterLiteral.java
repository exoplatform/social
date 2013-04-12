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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class AbstractFilterLiteral<P> implements FilterLiteral<P> {

  //TODO Improves ThreadLocal here
  private List<FilterOption<P>> filterOptions = new ArrayList<FilterOption<P>>(0);
  //
  private List<OrderByOption<P>> orderByOptions = new ArrayList<OrderByOption<P>>(0);
  
  protected abstract void start();
  
  public abstract void destroy();
  
  public AbstractFilterLiteral() {
    start();
  }

  @Override
  public FilterLiteral<P> append(OrderByOption<P> orderBy) {
    
    if (orderByOptions.contains(orderBy) == false) {
      List<OrderByOption<P>> results = new ArrayList<OrderByOption<P>>(orderByOptions.size() + 1);
      results.addAll(orderByOptions);
      // setTarget
      orderBy.setTarget(this);
      // Add the Filter Option to the new position.
      results.add(orderBy);
      orderByOptions = results;
    }

    return this;
  }
  
  @Override
  public OrderByOption<P> with(OrderByOption<P> orderBy) {
    this.append(orderBy);
    return this.get(orderBy);
  }

  @Override
  public FilterLiteral<P> remove(OrderByOption<P> orderBy) {
    orderByOptions.remove(orderBy);
    return this;
  }

  @Override
  public void clear() {
    filterOptions = new ArrayList<FilterOption<P>>(0);
    orderByOptions = new ArrayList<OrderByOption<P>>(0);
  }


  @Override
  public OrderByOption<P> get(OrderByOption<P> orderBy) {
    return orderByOptions.indexOf(orderBy) > -1 ? orderByOptions.get(orderByOptions.indexOf(orderBy)) : null;
  }
  
  @Override
  public Iterator<OrderByOption<P>> getOrders() {
    return orderByOptions.iterator();
  }

  @Override
  public FilterLiteral<P> append(FilterOption<P> filter) {
    if (filterOptions.contains(filter) == false) {
      List<FilterOption<P>> results = new ArrayList<FilterOption<P>>(filterOptions.size() + 1);
      results.addAll(filterOptions);
      
      // setTarget
      filter.setTarget(this);
      // Add the Filter Option to the new position.
      results.add(filter);
      filterOptions = results;
    }

    return this;
  }

  @Override
  public FilterLiteral<P> remove(FilterOption<P> filter) {
    filterOptions.remove(filter);
    return this;
  }

  @Override
  public FilterOption<P> get(FilterOption<P> filter) {
    return filterOptions.indexOf(filter) > -1 ? filterOptions.get(filterOptions.indexOf(filter)) : null;
  }
  
  @Override
  public FilterOption<P> with(FilterOption<P> filter) {
    this.append(filter);
    return this.get(filter);
  }
}

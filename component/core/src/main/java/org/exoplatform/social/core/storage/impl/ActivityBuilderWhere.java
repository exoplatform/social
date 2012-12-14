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
package org.exoplatform.social.core.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.chromattic.api.query.Ordering;
import org.chromattic.api.query.QueryBuilder;
import org.exoplatform.social.common.jcr.filter.FilterLiteral.DIRECTION;
import org.exoplatform.social.common.jcr.filter.FilterLiteral.OrderByOption;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.ActivityStorage.TimestampType;
import org.exoplatform.social.core.storage.query.BuilderWhereExpression;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;
import org.exoplatform.social.core.storage.query.WhereExpression;
import org.jboss.util.Strings;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Nov 21, 2012  
 */
public abstract class ActivityBuilderWhere implements BuilderWhereExpression<ActivityFilter, QueryBuilder<ActivityEntity>> {

  final WhereExpression where = new WhereExpression();
  Identity mentioner;
  List<Identity> identities;
  
  public String build(ActivityFilter filter) {
    init();
    String result = make(filter);
    destroy(filter);
    return result;
  }
   
  @Override
  public void orderBy(QueryBuilder<ActivityEntity> orderByBuilder, ActivityFilter filter) {
    Iterator<OrderByOption<PropertyLiteralExpression<?>>> it = filter.getOrders();
    
    OrderByOption<PropertyLiteralExpression<?>> orderBy = null;
    //
    while (it.hasNext()) {
      orderBy = it.next();
      orderByBuilder.orderBy(orderBy.getLiteral().getName(),
                             orderBy.getDirection() == DIRECTION.ASC ? Ordering.ASC : Ordering.DESC);
    }
  }
  
  public String make(ActivityFilter filter) {
    return where.toString();
  }
  
  private void init() {
    where.getStringBuilder();
  }
  
  private void destroy(ActivityFilter filter) {
    where.destroy();
    filter.destroy();
    mentioner = null;
    identities = null;
  }
  
  public ActivityBuilderWhere mentioner(Identity mentioner) {
    this.mentioner = mentioner;
    return this;
  }
  
  /**
   * @param identities contains StreamOwner of Activity
   * @return
   */
  public ActivityBuilderWhere owners(List<Identity> identities) {
    if (this.identities == null) {
      this.identities = new ArrayList<Identity>();
    }
    this.identities.addAll(identities);
    return this;
  }
  
  /**
   * 
   * @param identity
   * @return
   */
  public ActivityBuilderWhere owners(Identity ... identity) {
    return owners(Arrays.asList(identity));
  }
  
  public List<Identity> getOwners() {
    return this.identities == null ? new ArrayList<Identity>() : this.identities;
  }
  
  public static ActivityBuilderWhere ACTIVITY_FEED_BUILDER = new ActivityBuilderWhere() {

    @Override
    public String make(ActivityFilter filter) {
      
      //has relationship
      if (identities != null && identities.size() > 0) {
        boolean first = true;
        where.startGroup();
        for (Identity currentIdentity : identities) {

          if (first) {
            first = false;
          }
          else {
            where.or();
          }

          where.equals(ActivityEntity.identity, currentIdentity.getId());

        }
        where.endGroup();
        
      }
      where.and().equals(ActivityEntity.isComment, Boolean.FALSE);

      Object objFilter = filter.get(ActivityFilter.ACTIVITY_POINT_FIELD).getValue();
      //
      if (objFilter != null) {
        TimestampType type = null;
        if (objFilter instanceof TimestampType) {
          type = (TimestampType) objFilter;
          if (type != null) {
            switch (type) {
            case NEWER:
              where.and().greater(ActivityEntity.lastUpdated, type.get());
              break;
            case OLDER:
              where.and().lesser(ActivityEntity.lastUpdated, type.get());
              break;
            }
          }
        } else {
          return Strings.EMPTY;
        }
      }
      
      return where.toString();
    }
  };
  
  public static ActivityBuilderWhere ACTIVITY_BUILDER = new ActivityBuilderWhere() {

    @Override
    public String make(ActivityFilter filter) {
      
      //has relationship
      if (identities != null && identities.size() > 0) {
        boolean first = true;
        where.startGroup();
        for (Identity currentIdentity : identities) {

          if (first) {
            first = false;
          }
          else {
            where.or();
          }

          where.equals(ActivityEntity.identity, currentIdentity.getId());

        }
        where.endGroup();
        
      }
      where.and().equals(ActivityEntity.isComment, Boolean.FALSE);

      Object objFilter = filter.get(ActivityFilter.ACTIVITY_POINT_FIELD).getValue();
      //
      if (objFilter != null) {
        TimestampType type = null;
        if (objFilter instanceof TimestampType) {
          type = (TimestampType) objFilter;
          if (type != null) {
            switch (type) {
            case NEWER:
              where.and().greater(ActivityEntity.postedTime, type.get());
              break;
            case OLDER:
              where.and().lesser(ActivityEntity.postedTime, type.get());
              break;
            }
          }
        } else {
          return Strings.EMPTY;
        }
      }
      
      return where.toString();
    }
  };
}


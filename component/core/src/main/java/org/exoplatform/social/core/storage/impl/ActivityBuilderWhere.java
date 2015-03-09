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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.chromattic.api.query.Ordering;
import org.chromattic.api.query.QueryBuilder;
import org.exoplatform.social.common.jcr.filter.FilterLiteral.DIRECTION;
import org.exoplatform.social.common.jcr.filter.FilterLiteral.FilterOption;
import org.exoplatform.social.common.jcr.filter.FilterLiteral.OrderByOption;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.entity.HidableEntity;
import org.exoplatform.social.core.chromattic.filter.JCRFilterLiteral;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.api.ActivityStorage.TimestampType;
import org.exoplatform.social.core.storage.query.BuilderWhereExpression;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;
import org.exoplatform.social.core.storage.query.WhereExpression;

public abstract class ActivityBuilderWhere implements BuilderWhereExpression<JCRFilterLiteral, QueryBuilder<ActivityEntity>> {

  final WhereExpression where = new WhereExpression();
  
  /** */
  Identity poster;
  /** */
  Identity mentioner;
  
  /** */
  Identity liker;
  
  /** */
  Identity commenter;
  
  /** */
  ThreadLocal<List<Identity>> identitiesLocal = new ThreadLocal<List<Identity>>();
  
  /** */
  ThreadLocal<List<Identity>> postersLocal = new ThreadLocal<List<Identity>>();
  
  private Object lock = new Object();
  
    
  /** */
  String[] activityIds = new String[0];
  
  public String build(JCRFilterLiteral filter) {
    String result = "";
    
    //
    synchronized (lock) {
      init();
      result = make(filter);
      destroy(filter);
    }
    return result;
    
  }
   
  @Override
  public void orderBy(QueryBuilder<ActivityEntity> orderByBuilder, JCRFilterLiteral filter) {
    Iterator<OrderByOption<PropertyLiteralExpression<?>>> it = filter.getOrders();
    
    OrderByOption<PropertyLiteralExpression<?>> orderBy = null;
    //
    while (it.hasNext()) {
      orderBy = it.next();
      orderByBuilder.orderBy(orderBy.getLiteral().getName(),
                             orderBy.getDirection() == DIRECTION.ASC ? Ordering.ASC : Ordering.DESC);
    }
  }
  
  public String make(JCRFilterLiteral filter) {
    return where.toString();
  }
  
  private void init() {
    where.getStringBuilder();
  }
  
  private void destroy(JCRFilterLiteral filter) {
    where.destroy();
    filter.destroy();
    activityIds = new String[0];
    poster = null;
    mentioner = null;
    liker = null;
    commenter = null;
    identitiesLocal.remove();
    postersLocal.remove();
    //identities = new ArrayList<Identity>();
  }
  
  public ActivityBuilderWhere poster(Identity poster) {
    this.poster = poster;
    return this;
  }
  
  public ActivityBuilderWhere posters(List<Identity> posters) {
    List<Identity> posterList = postersLocal.get();
    
    //
    if (posterList == null) {
      posterList = new ArrayList<Identity>();
    }
    
    //
    posterList.addAll(posters);
    
    //
    postersLocal.set(posterList);
    return this;
  }
  
  public ActivityBuilderWhere mentioner(Identity mentioner) {
    this.mentioner = mentioner;
    return this;
  }
  
  public ActivityBuilderWhere liker(Identity liker) {
    this.liker = liker;
    return this;
  }
  
  public ActivityBuilderWhere commenter(Identity commenter) {
    this.commenter = commenter;
    return this;
  }
  /**
   * @param identities contains StreamOwner of Activity
   * @return
   */
  public ActivityBuilderWhere owners(List<Identity> identities) {
    
   List<Identity> identityList = identitiesLocal.get();
    
    //
    if (identityList == null) {
      identityList = new ArrayList<Identity>();
    }
    
    //
    identityList.addAll(identities);
    
    //
    identitiesLocal.set(identityList);
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
    return this.identitiesLocal.get() == null ? new ArrayList<Identity>() 
                                   : new CopyOnWriteArrayList<Identity>(this.identitiesLocal.get());
  }
  
  public List<Identity> getPosters() {
    return this.postersLocal.get() == null ? new ArrayList<Identity>() 
                                : new CopyOnWriteArrayList<Identity>(this.postersLocal.get());
  }
  
  public ActivityBuilderWhere excludedActivities(String...activityIds) {
    this.activityIds = activityIds;
    return this;
  }
  
  public String[] excludedActivityIds() {
    return this.activityIds;
  }
  
  public static ActivityBuilderWhere space() {

    return new ActivityBuilderWhere() {

      @Override
      public String make(JCRFilterLiteral filter) {
        List<Identity> identities = getOwners();

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

          if (mentioner != null) {
            where.or();
            where.contains(ActivityEntity.mentioners, mentioner.getId());
          }

          where.endGroup();

        }
        where.and().equals(ActivityEntity.isComment, Boolean.FALSE);

        //
        where.and();
        //
        where.startGroup();
        {
          where.equals(HidableEntity.isHidden, Boolean.FALSE);
          where.or().isNull(HidableEntity.isHidden);
        }
        where.endGroup();

        Object objFilter = filter.get(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).getValue();
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
          }
        }
        return where.toString();
      }
    };
  }
  
  public static ActivityBuilderWhere updated() {

    return new ActivityBuilderWhere() {

      @Override
      public String make(JCRFilterLiteral filter) {
        List<Identity> identities = getOwners();

        boolean hasIndentitiesCondition = identities != null && identities.size() > 0 ? identities.size() > 0 : false;

        boolean first = true;
        //has relationship
        where.startGroup();
        if ( hasIndentitiesCondition ) {

          for (Identity currentIdentity : identities) {

            if (first) {
              first = false;
            }
            else {
              where.or();
            }

            where.equals(ActivityEntity.identity, currentIdentity.getId());

          }
        }

        //
        if (mentioner != null) {

          if (first) {
            first = false;
          }
          else {
            where.or();
          }

          where.contains(ActivityEntity.mentioners, mentioner.getId());

        }

        //take care the case relationship add comment to owner
        //it also need to calculate in counter
        if (mentioner != null) {
          List<Identity> posters = getPosters();
          for (Identity currentIdentity : posters) {
            if (first) {
              first = false;
            }
            else {
              where.or();
            }
            where.startGroup();
            where.equals(ActivityEntity.identity, mentioner.getId());
            where.and().equals(ActivityEntity.poster, currentIdentity.getId());
            where.and().equals(ActivityEntity.isComment, true);
            where.endGroup();

          }
        }
        
        where.endGroup();

        Object objFilter = filter.get(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).getValue();
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
          }
        }

        //
        String[] excludedActivityIds = this.activityIds;
        for(String id : excludedActivityIds) {
          where.and().not().equals(JCRProperties.id, id);
        }
        
        //
        if (first) {
          where.equals(ActivityEntity.isComment, false);
        } else {
          where.and().equals(ActivityEntity.isComment, false);
        }

        return where.toString();
      }
    };
  }
  
  public static ActivityBuilderWhere viewedRange() {

    return new ActivityBuilderWhere() {

      @Override
      public String make(JCRFilterLiteral filter) {
        List<Identity> identities = getOwners();

        boolean hasIndentitiesCondition = identities != null && identities.size() > 0 ? identities.size() > 0 : false;

        //has relationship
        if ( hasIndentitiesCondition ) {
          boolean first = true;
          where.startGroup();

          for (Identity currentIdentity : identities) {

            if (currentIdentity == null) continue;

            if (first) {
              first = false;
            }
            else {
              where.or();
            }

            where.equals(ActivityEntity.identity, currentIdentity.getId());

          }

          if (mentioner != null) {
            where.or();
            where.contains(ActivityEntity.mentioners, mentioner.getId());
          }

          where.endGroup();

        } else {
          if (mentioner != null) {
            where.contains(ActivityEntity.mentioners, mentioner.getId());
          }
        }
        Object fromFilter = null;
        FilterOption<PropertyLiteralExpression<?>> frFilter = filter.get(ActivityFilter.ACTIVITY_FROM_UPDATED_POINT_FIELD);
        if (frFilter != null) {
          fromFilter = frFilter.getValue();
        }

        //
        if (fromFilter != null) {
          TimestampType type = null;
          if (fromFilter instanceof TimestampType) {
            type = (TimestampType) fromFilter;
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
          }
        }

        Object toFilter = null;
        FilterOption<PropertyLiteralExpression<?>> tFilter = filter.get(ActivityFilter.ACTIVITY_TO_UPDATED_POINT_FIELD);
        if (tFilter != null) {
          toFilter = tFilter.getValue();
        }
        //
        if (toFilter != null) {
          TimestampType type = null;
          if (toFilter instanceof TimestampType) {
            type = (TimestampType) toFilter;
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
          }
        }
        return where.toString();
      }
    };
  }
  
  public static ActivityBuilderWhere simple() {

    return new ActivityBuilderWhere() {

      @Override
      public String make(JCRFilterLiteral filter) {
        List<Identity> identities = getOwners();

        boolean first = true;

        //has relationship
        if (identities != null && identities.size() > 0) {
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
        }

        if (poster != null) {
          if (first) {
            where.startGroup();
            first = false;
          } else {
            where.or();
          }
          where.equals(ActivityEntity.poster, poster.getId());
        }

        if (mentioner != null) {
          if (first) {
            where.startGroup();
            first = false;
          } else {
            where.or();
          }
          where.contains(ActivityEntity.mentioners, mentioner.getId());
        }

        if (commenter != null) {
          if (first) {
            where.startGroup();
            first = false;
          } else {
            where.or();
          }
          where.contains(ActivityEntity.commenters, commenter.getId());
        }

        if (liker != null) {
          if (first) {
            where.startGroup();
            first = false;
          } else {
            where.or();
          }
          where.contains(ActivityEntity.likes, liker.getId());
        }

        if (!first) {
          where.endGroup();
        }
        
       where.and();

        where.equals(ActivityEntity.isComment, Boolean.FALSE);

        //
        where.and();
        //
        where.startGroup();
        {
          where.equals(HidableEntity.isHidden, Boolean.FALSE);
          where.or().isNull(HidableEntity.isHidden);
        }
        where.endGroup();

        Object objFilter = filter.get(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).getValue();
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
          }
        }
        return where.toString();
      }
    };
  }
  
  public static ActivityBuilderWhere userSpaces() {

    return new ActivityBuilderWhere() {

      @Override
      public String make(JCRFilterLiteral filter) {
        List<Identity> identities = getOwners();

        boolean first = true;
        //has relationship
        if (identities != null && identities.size() > 0) {
          
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

          if (poster != null) {
            where.or();
            where.equals(ActivityEntity.poster, poster.getId());
          }

          if (mentioner != null) {
            where.or();
            where.contains(ActivityEntity.mentioners, mentioner.getId());
          }

          if (commenter != null) {
            where.or();
            where.contains(ActivityEntity.commenters, commenter.getId());
          }

          if (liker != null) {
            where.or();
            where.contains(ActivityEntity.likes, liker.getId());
          }

          where.endGroup();

        }
        if (first == false) {
          where.and();
        }
        
        where.equals(ActivityEntity.isComment, Boolean.FALSE);

        //
        where.and();
        //
        where.startGroup();
        {
          where.equals(HidableEntity.isHidden, Boolean.FALSE);
          where.or().isNull(HidableEntity.isHidden);
        }
        where.endGroup();

        Object objFilter = filter.get(ActivityFilter.ACTIVITY_UPDATED_POINT_FIELD).getValue();
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
          }
        }
        return where.toString();
      }
    };
  }
  
  public static ActivityBuilderWhere owner() {

    return new ActivityBuilderWhere() {

      @Override
      public String make(JCRFilterLiteral filter) {
        List<Identity> identities = getOwners();

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

          if (first == false) {
            where.and();
          }

          where.equals(ActivityEntity.isComment, Boolean.FALSE);

          //
          where.and();
          //
          where.startGroup();
          {
            where.equals(HidableEntity.isHidden, Boolean.FALSE);
            where.or().isNull(HidableEntity.isHidden);
          }
          where.endGroup();

          //
          if (mentioner != null) {
            if (first) {
              first = false;
            }
            else {
              where.or();
            }

            where.contains(ActivityEntity.mentioners, mentioner.getId());
          }

          //
          if (poster != null) {
            where.or();
            where.startGroup();
            where.equals(ActivityEntity.poster, poster.getId());
            where.and().equals(ActivityEntity.isComment, true);
            where.endGroup();
          }
          where.endGroup();

        }

        return where.toString();
      }
    };
  }
  
  public static ActivityBuilderWhere viewOwner() {

    return new ActivityBuilderWhere() {

      @Override
      public String make(JCRFilterLiteral filter) {
        List<Identity> posterIdentities = getPosters();

        //has relationship
        if (posterIdentities != null && posterIdentities.size() > 0) {
          boolean first = true;
          where.startGroup();
          for (Identity identity : posterIdentities) {

            if (first) {
              first = false;
            }
            else {
              where.or();
            }
            where.equals(ActivityEntity.poster, identity.getId());

          }
          where.endGroup();
          
          where.and();
        
          where.equals(ActivityEntity.isComment, Boolean.FALSE);
  
          //
          where.and();
          //
          where.startGroup();
          {
            where.equals(HidableEntity.isHidden, Boolean.FALSE);
            where.or().isNull(HidableEntity.isHidden);
          }
          where.endGroup();
        }

        return where.toString();
      }
    };
  }

}


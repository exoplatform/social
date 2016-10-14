/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.storage.dao.jpa.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity_;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity_;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity_;
import org.exoplatform.social.core.jpa.storage.entity.StreamItemEntity;
import org.exoplatform.social.core.jpa.storage.entity.StreamItemEntity_;
import org.exoplatform.social.core.jpa.storage.entity.StreamType;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 1, 2015  
 */
public final class AStreamQueryBuilder {
  private Identity owner;
  private long offset;
  private long limit;
  //newer or older
  private long sinceTime = 0;
  private boolean isNewer = false;
  //memberOfSpaceIds
  private Collection<String> memberOfSpaceIds;
  private Identity myIdentity;
  private Identity viewer;
  //order by
  private boolean descOrder = true;
  String[] activityTypes;
  private List<Long> connections;

  public static AStreamQueryBuilder builder() {
    return new AStreamQueryBuilder();
  }

  public AStreamQueryBuilder owner(Identity owner) {
    this.owner = owner;
    return this;
  }
  
  public AStreamQueryBuilder viewer(Identity viewer) {
    this.viewer = viewer;
    return this;
  }
  
  public AStreamQueryBuilder myIdentity(Identity myIdentity) {
    this.myIdentity = myIdentity;
    return this;
  }

  public AStreamQueryBuilder offset(long offset) {
    this.offset = offset;
    return this;
  }

  public AStreamQueryBuilder limit(long limit) {
    this.limit = limit;
    return this;
  }

  public AStreamQueryBuilder activityTypes(String... activityTypes) {
    this.activityTypes = activityTypes;
    return this;
  }

  public AStreamQueryBuilder newer(long sinceTime) {
    this.isNewer = true;
    this.sinceTime = sinceTime;
    return this;
  }

  public AStreamQueryBuilder older(long sinceTime) {
    this.isNewer = false;
    this.sinceTime = sinceTime;
    return this;
  }

  public AStreamQueryBuilder memberOfSpaceIds(Collection<String> spaceIds) {
    this.memberOfSpaceIds = spaceIds;
    return this;
  }

  public AStreamQueryBuilder ascOrder() {
    this.descOrder = false;
    return this;
  }

  public AStreamQueryBuilder descOrder() {
    this.descOrder = true;
    return this;
  }

  
  public TypedQuery<ActivityEntity> build() {
    
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ActivityEntity> criteria = cb.createQuery(ActivityEntity.class);
    Root<ActivityEntity> activity = criteria.from(ActivityEntity.class);
    Join<ActivityEntity, StreamItemEntity> streamItem = activity.join(ActivityEntity_.streamItems);

    CriteriaQuery<ActivityEntity> select;
    select = criteria.select(activity).distinct(true);
    select.where(getPredicateForStream(activity, streamItem, cb, criteria));
    if (this.descOrder) {
      select.orderBy(cb.desc(activity.get(ActivityEntity_.updatedDate)));
    } else {
      select.orderBy(cb.asc(activity.get(ActivityEntity_.updatedDate)));
    }

    TypedQuery<ActivityEntity> typedQuery = em.createQuery(select);
    if (this.limit > 0) {
      typedQuery.setFirstResult((int) offset);
      typedQuery.setMaxResults((int) limit);
    }

    return typedQuery;
  }
  
  public TypedQuery<Tuple> buildId() {
    
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Tuple> criteria = cb.createTupleQuery();
    Root<StreamItemEntity> streamItem = criteria.from(StreamItemEntity.class);

    criteria.multiselect(streamItem.get(StreamItemEntity_.activityId).alias(StreamItemEntity_.activityId.getName()), streamItem.get(StreamItemEntity_.updatedDate)).distinct(true);
    List<Predicate> predicates = getPredicateForIdsStream(streamItem, cb, criteria);
    criteria.where(cb.or(predicates.toArray(new Predicate[predicates.size()])));
    if (this.descOrder) {
      criteria.orderBy(cb.desc(streamItem.get(StreamItemEntity_.updatedDate)));
    } else {
      criteria.orderBy(cb.asc(streamItem.get(StreamItemEntity_.updatedDate)));
    }

    TypedQuery<Tuple> typedQuery = em.createQuery(criteria);
    if (this.limit > 0) {
      typedQuery.setFirstResult((int) offset);
      typedQuery.setMaxResults((int) limit);
    }

    return typedQuery;
  }

  /**
   * Build count statement for FEED stream to get the number of the activity base on given conditions
   *
   * @return instance the TypedQuery
   */
  public TypedQuery<Long> buildCount() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
    Root<ActivityEntity> activity = criteria.from(ActivityEntity.class);
    Join<ActivityEntity, StreamItemEntity> streamItem = activity.join(ActivityEntity_.streamItems);

    CriteriaQuery<Long> select = criteria.select(cb.countDistinct(activity.get(ActivityEntity_.id)));
    select.where(getPredicateForStream(activity, streamItem, cb, criteria));

    return em.createQuery(select);
  }
  
  private Predicate getPredicateForStream(Root<ActivityEntity> activity, Join<ActivityEntity, StreamItemEntity> stream, CriteriaBuilder cb, CriteriaQuery criteria) {

    Predicate predicate = null;
    //owner
    if (this.owner != null) {
      predicate = cb.equal(stream.get(StreamItemEntity_.ownerId), owner.getId());
                  
      //view user's stream
      if (this.viewer != null && !this.viewer.getId().equals(this.owner.getId())) {
        predicate = cb.and(predicate, cb.equal(activity.get(ActivityEntity_.providerId), OrganizationIdentityProvider.NAME));
      }
    }
    
    // space members
    if (this.memberOfSpaceIds != null && memberOfSpaceIds.size() > 0) {
      List<Long> ids = new ArrayList<>();
      for (String id : memberOfSpaceIds) {
        ids.add(Long.parseLong(id));
      }
      if (predicate != null) {
        predicate = cb.or(predicate, addInClause(cb, stream.get(StreamItemEntity_.ownerId), ids));
      } else {
        predicate = addInClause(cb, stream.get(StreamItemEntity_.ownerId), ids);
      }
    }
    
    if (this.myIdentity != null) {
      long identityId = Long.valueOf(this.myIdentity.getId());

      Path ownerId = stream.get(StreamItemEntity_.ownerId);
      Path streamType = stream.get(StreamItemEntity_.streamType);

      Subquery sub;
      Root<ConnectionEntity> conn;
      Path sender, receiver, status;

      Predicate[] ps = new Predicate[2];

      //
      sub = criteria.subquery(Long.class);
      conn = sub.from(ConnectionEntity.class);
      receiver = conn.get(ConnectionEntity_.receiver);
      sender = conn.get(ConnectionEntity_.sender);
      status = conn.get(ConnectionEntity_.status);

      sub.select(conn.get(ConnectionEntity_.id));
      sub.where(cb.equal(receiver, ownerId), cb.equal(sender, identityId), cb.equal(status, Relationship.Type.CONFIRMED), cb.equal(streamType, StreamType.POSTER));

      ps[0] = cb.exists(sub);

      //
      sub = criteria.subquery(Long.class);
      conn = sub.from(ConnectionEntity.class);
      receiver = conn.get(ConnectionEntity_.receiver);
      sender = conn.get(ConnectionEntity_.sender);
      status = conn.get(ConnectionEntity_.status);

      sub.select(conn.get(ConnectionEntity_.id));
      sub.where(cb.equal(sender, ownerId), cb.equal(receiver, identityId), cb.equal(status, Relationship.Type.CONFIRMED), cb.equal(streamType, StreamType.POSTER));

      ps[1] = cb.exists(sub);

      if (predicate != null) {
        predicate = cb.or(predicate, ps[0], ps[1]);
      } else {
        predicate = cb.or(ps);
      }
    }
    //newer or older
    if (this.sinceTime > 0) {
      if (isNewer) {
        if (predicate != null) {
          predicate = cb.and(predicate, cb.greaterThan(activity.get(ActivityEntity_.updatedDate), this.sinceTime));
        } else {
          predicate = cb.greaterThan(activity.get(ActivityEntity_.updatedDate), this.sinceTime);
        }

      } else {
        if (predicate != null) {
          predicate = cb.and(predicate, cb.lessThan(activity.get(ActivityEntity_.updatedDate), this.sinceTime));
        } else {
          predicate = cb.lessThan(activity.get(ActivityEntity_.updatedDate), this.sinceTime);
        }
      }
    }

    //filter hidden = FALSE
    if (predicate != null) {
      predicate = cb.and(predicate, cb.equal(activity.<Boolean>get(ActivityEntity_.hidden), Boolean.FALSE));
    } else {
      predicate = cb.equal(activity.<Boolean>get(ActivityEntity_.hidden), Boolean.FALSE);
    }
    return predicate;
  }
  
  private List<Predicate> getPredicateForIdsStream(Root<StreamItemEntity> stream,
                                             CriteriaBuilder cb, CriteriaQuery criteria) {

    List<Predicate> predicates = new ArrayList<Predicate>();
    Predicate predicate = null;
    // owner
    if (this.owner != null) {
      // view user's stream
      if (this.viewer != null && !this.viewer.getId().equals(this.owner.getId())) {
        predicate = cb.equal(stream.get(StreamItemEntity_.ownerId), owner.getId());
        predicate = cb.and(predicate,
                           cb.equal(stream.get(StreamItemEntity_.streamType), StreamType.POSTER));
      } else if (this.memberOfSpaceIds != null && memberOfSpaceIds.size() > 0) {
        memberOfSpaceIds = new ArrayList<String>(memberOfSpaceIds);
        memberOfSpaceIds.add(this.owner.getId());
      } else {
        predicate = cb.equal(stream.get(StreamItemEntity_.ownerId), owner.getId());
      }
    }
    
    
    // space members
    if (this.memberOfSpaceIds != null && memberOfSpaceIds.size() > 0) {
      List<Long> ids = new ArrayList<>();
      for (String id : memberOfSpaceIds) {
        ids.add(Long.parseLong(id));
      }
      predicates.add(addInClause(cb, stream.get(StreamItemEntity_.ownerId), ids));
    }

    if (this.myIdentity != null && connections != null && !connections.isEmpty()) {
      Predicate streamTypePredicate = cb.equal(stream.get(StreamItemEntity_.streamType), StreamType.POSTER);
      Predicate existPredicates = addInClause(cb, stream.get(StreamItemEntity_.ownerId), connections);
      Predicate subqueryPredicate = cb.and(streamTypePredicate, existPredicates);
      predicates.add(subqueryPredicate);
    }
    // newer or older
    if (this.sinceTime > 0) {
      if (isNewer) {
        if (predicate != null) {
          predicate = cb.and(predicate, cb.greaterThan(stream.get(StreamItemEntity_.updatedDate), this.sinceTime));
        } else {
          predicate = cb.greaterThan(stream.get(StreamItemEntity_.updatedDate), this.sinceTime);
        }

      } else {
        if (predicate != null) {
          predicate = cb.and(predicate,
                             cb.lessThan(stream.get(StreamItemEntity_.updatedDate), this.sinceTime));
        } else {
          predicate = cb.lessThan(stream.get(StreamItemEntity_.updatedDate), this.sinceTime);
        }
      }
    }

    
    //
    if (predicate != null) {
      predicates.add(predicate);
    }
    
    return predicates;
  }
  

  private <T> Predicate addInClause(CriteriaBuilder cb,
                                    Path<T> pathColumn,
                                    Collection<T> values) {

    In<T> in = cb.in(pathColumn);
    for (T value : values) {
      in.value(value);
    }
    return in;

  }

  public TypedQuery<ActivityEntity> buildGetActivitiesByPoster() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ActivityEntity> criteria = cb.createQuery(ActivityEntity.class);
    Root<ActivityEntity> activity = criteria.from(ActivityEntity.class);
    Predicate predicate = cb.equal(activity.get(ActivityEntity_.posterId), owner.getId());
    if (this.activityTypes != null && this.activityTypes.length > 0) {
      List<String> types = new ArrayList<String>(Arrays.asList(this.activityTypes));
      predicate = cb.and(predicate, addInClause(cb, activity.get(ActivityEntity_.type), types));
    }
    //
    CriteriaQuery<ActivityEntity> select = criteria.select(activity).distinct(true);
    select.where(predicate);
    select.orderBy(cb.desc(activity.get(ActivityEntity_.updatedDate)));

    TypedQuery<ActivityEntity> typedQuery = em.createQuery(select);
    if (this.limit > 0) {
      typedQuery.setFirstResult((int) offset);
      typedQuery.setMaxResults((int) limit);
    }

    return typedQuery;
  }

  public TypedQuery<Long> buildActivitiesByPosterCount() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
    Root<ActivityEntity> activity = criteria.from(ActivityEntity.class);
    Predicate predicate = cb.equal(activity.get(ActivityEntity_.posterId), owner.getId());
    if (this.activityTypes != null && this.activityTypes.length > 0) {
      List<String> types = new ArrayList<String>(Arrays.asList(this.activityTypes));
      predicate = cb.and(predicate, addInClause(cb, activity.get(ActivityEntity_.type), types));
    }
    //
    CriteriaQuery<Long> select = criteria.select(cb.countDistinct(activity));
    select.where(predicate);

    return em.createQuery(select);
  }

  public AStreamQueryBuilder connections(List<Long> connections) {
    this.connections = connections;
    return this;
  }
}
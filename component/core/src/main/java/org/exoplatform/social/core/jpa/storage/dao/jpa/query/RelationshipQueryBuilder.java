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
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity_;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity_;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 4, 2015  
 */
public final class RelationshipQueryBuilder {

  private Identity owner;
  private Identity sender;
  private Identity receiver;
  private long offset;
  private long limit;
  private Relationship.Type status = null;
  private static Collection<Relationship.Type> types = Arrays.asList(Relationship.Type.INCOMING, Relationship.Type.OUTGOING);
  private ProfileFilter profileFilter;
  
  
  public static RelationshipQueryBuilder builder() {
    return new RelationshipQueryBuilder();
  }
  
  public RelationshipQueryBuilder owner(Identity owner) {
    this.owner = owner;
    return this;
  }
  
  public RelationshipQueryBuilder sender(Identity sender) {
    this.sender = sender;
    return this;
  }
  
  public RelationshipQueryBuilder receiver(Identity receiver) {
    this.receiver = receiver;
    return this;
  }
  
  public RelationshipQueryBuilder status(Relationship.Type status) {
    this.status = status;
    return this;
  }
  
  public RelationshipQueryBuilder offset(long offset) {
    this.offset = offset;
    return this;
  }
  
  public RelationshipQueryBuilder limit(long limit) {
    this.limit = limit;
    return this;
  }
  
  /**
   * Builds the Typed Query
   * @return the JPA typed query
   */
  public TypedQuery<ConnectionEntity> buildSingleRelationship() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ConnectionEntity> criteria = cb.createQuery(ConnectionEntity.class);
    Root<ConnectionEntity> connection = criteria.from(ConnectionEntity.class);
    
    Predicate predicate = null;
    if (this.sender != null && this.receiver != null) {
      predicate = cb.equal(connection.get(ConnectionEntity_.sender).get(IdentityEntity_.id), Long.valueOf(sender.getId())) ;
      predicate = cb.and(predicate, cb.equal(connection.get(ConnectionEntity_.receiver).get(IdentityEntity_.id), Long.valueOf(receiver.getId())));
    }
    
    CriteriaQuery<ConnectionEntity> select = criteria.select(connection).distinct(true);
    select.where(predicate);
    TypedQuery<ConnectionEntity> typedQuery = em.createQuery(select);
    
    return typedQuery;
  }
  
  /**
   * Builds the Typed Query
   * @return JPA query object
   */
  public TypedQuery<ConnectionEntity> build() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ConnectionEntity> criteria = cb.createQuery(ConnectionEntity.class);
    Root<ConnectionEntity> connection = criteria.from(ConnectionEntity.class);

    List<Predicate> predicates = new ArrayList<>();
    Path sender = connection.get(ConnectionEntity_.sender).get(IdentityEntity_.id);
    Path receiver = connection.get(ConnectionEntity_.receiver).get(IdentityEntity_.id);

    //owner
    if (this.owner != null) {
      Predicate predicate = null;
      if (this.status == Type.OUTGOING) {
        predicate = cb.equal(sender, Long.valueOf(owner.getId()));
      } else if (this.status == Type.INCOMING) {
        predicate = cb.equal(receiver, Long.valueOf(owner.getId()));
      } else {
        predicate = cb.or(cb.equal(sender, Long.valueOf(owner.getId())),
                cb.equal(receiver, Long.valueOf(owner.getId())));
      }
      predicates.add(predicate);
    }
    //status
    if (this.status != null) {
      if (status == Type.OUTGOING || status == Type.INCOMING) {
        predicates.add(cb.equal(connection.get(ConnectionEntity_.status), Type.PENDING));
      } else {
        predicates.add(cb.equal(connection.get(ConnectionEntity_.status), this.status));
      }
    }

    if (this.sender != null) {
      predicates.add(cb.equal(sender, Long.valueOf(this.sender.getId())));
    }
    if (this.receiver != null) {
      predicates.add(cb.equal(receiver, Long.valueOf(this.receiver.getId())));
    }
    
    CriteriaQuery<ConnectionEntity> select = criteria.select(connection).distinct(true);
    select.where(predicates.toArray(new Predicate[predicates.size()]));

    TypedQuery<ConnectionEntity> typedQuery = em.createQuery(select);
    if (this.limit > 0) {
      typedQuery.setFirstResult((int) offset);
      typedQuery.setMaxResults((int) limit);
    }
    
    return typedQuery;
  }
  
  /**
   * Builds the Typed Query
   * @return JPA query object
   */
  public TypedQuery<Long> buildCount() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
    Root<ConnectionEntity> connection = criteria.from(ConnectionEntity.class);
    
    Predicate predicate = null;
    //owner
    if (this.owner != null) {
      if (this.status == Type.OUTGOING) {
        predicate = cb.equal(connection.get(ConnectionEntity_.sender).get(IdentityEntity_.id), Long.valueOf(owner.getId()));
      } else if (this.status == Type.INCOMING) {
        predicate = cb.equal(connection.get(ConnectionEntity_.receiver).get(IdentityEntity_.id), Long.valueOf(owner.getId()));
      } else {
        predicate = cb.or(cb.equal(connection.get(ConnectionEntity_.sender).get(IdentityEntity_.id), Long.valueOf(owner.getId())),
                cb.equal(connection.get(ConnectionEntity_.receiver).get(IdentityEntity_.id), Long.valueOf(owner.getId())));
      }
    }
    //status
    if (this.status != null) {
      if (status == Type.OUTGOING || status == Type.INCOMING) {
        predicate = cb.and(predicate, cb.equal(connection.get(ConnectionEntity_.status), Type.PENDING));
      } else {
        predicate = cb.and(predicate, cb.equal(connection.get(ConnectionEntity_.status), this.status));
      }
    }
    
    CriteriaQuery<Long> select = criteria.select(cb.countDistinct(connection));
    select.where(predicate);

    return em.createQuery(select);
  }

  /**
   *
   * @return JPA query object
   */
  public TypedQuery<ConnectionEntity> buildLastConnections() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ConnectionEntity> criteria = cb.createQuery(ConnectionEntity.class);
    Root<ConnectionEntity> connection = criteria.from(ConnectionEntity.class);
    
    Predicate predicate = null;
    //owner
    if (this.owner != null) {
      if (this.status == Type.OUTGOING) {
        predicate = cb.equal(connection.get(ConnectionEntity_.sender).get(IdentityEntity_.id), Long.valueOf(owner.getId()));
      } else if (this.status == Type.INCOMING) {
        predicate = cb.equal(connection.get(ConnectionEntity_.receiver).get(IdentityEntity_.id), Long.valueOf(owner.getId()));
      } else {
        predicate = cb.or(cb.equal(connection.get(ConnectionEntity_.sender).get(IdentityEntity_.id), Long.valueOf(owner.getId())),
                cb.equal(connection.get(ConnectionEntity_.receiver).get(IdentityEntity_.id), Long.valueOf(owner.getId())));
      }
    }
    //status
    if (this.status != null) {
      if (status == Type.OUTGOING || status == Type.INCOMING) {
        predicate = cb.and(predicate, cb.equal(connection.get(ConnectionEntity_.status), Type.PENDING));
      } else {
        predicate = cb.and(predicate, cb.equal(connection.get(ConnectionEntity_.status), this.status));
      }
    }
    
    CriteriaQuery<ConnectionEntity> select = criteria.select(connection).distinct(true);
    select.where(predicate);
    select.orderBy(cb.desc(connection.<Date> get(ConnectionEntity_.updatedDate)));

    TypedQuery<ConnectionEntity> typedQuery = em.createQuery(select);
    if (this.limit > 0) {
      typedQuery.setFirstResult((int) offset);
      typedQuery.setMaxResults((int) limit);
    }
    
    return typedQuery;
  }

  /**
   *
   * @return JPA query object
   */
  public TypedQuery<ConnectionEntity> buildFilter() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<ConnectionEntity> criteria = cb.createQuery(ConnectionEntity.class);
    Root<ConnectionEntity> connection = criteria.from(ConnectionEntity.class);
    //
    CriteriaQuery<ConnectionEntity> select = criteria.select(connection);
    select.where(buildPredicateFilter(cb, connection));
    //
    TypedQuery<ConnectionEntity> typedQuery = em.createQuery(select);
    if (this.limit > 0) {
      typedQuery.setFirstResult((int) offset);
      typedQuery.setMaxResults((int) limit);
    }
    //
    return typedQuery;
  }

  /**
   *
   * @return JPA query object
   */
  public TypedQuery<Long> buildFilterCount() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
    Root<ConnectionEntity> relationship = criteria.from(ConnectionEntity.class);
//    Join<Connection, Profile> receiver = relationship.join(ConnectionEntity_.receiver);
    CriteriaQuery<Long> select = criteria.select(cb.countDistinct(relationship));
    //
    select.where(buildPredicateFilter(cb, relationship));
    //
    return em.createQuery(select);
  }
  
  private Predicate buildPredicateFilter(CriteriaBuilder cb, Root<ConnectionEntity> connection) {
    Predicate predicate = null;
    // owner
    if (this.owner != null) {
      predicate = cb.equal(connection.get(ConnectionEntity_.sender).get(IdentityEntity_.id), Long.valueOf(owner.getId()));
    }
    // status
    if (this.status != null) {
      predicate = cb.and(predicate, cb.equal(connection.get(ConnectionEntity_.status), this.status));
    }

    Predicate pFilter = null;
    if (profileFilter != null) {
    //Exclude identities
      List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();
      if (excludedIdentityList != null && excludedIdentityList.size() > 0) {
        In<Long> in = cb.in(connection.get(ConnectionEntity_.receiver).get(IdentityEntity_.id));
        for (Identity id : excludedIdentityList) {
          in.value(Long.valueOf(id.getId()));
        }
        predicate = cb.and(predicate, in.not());  
      }
    }
    //
    return appendPredicate(cb, predicate, pFilter);
  }
  
  
  public RelationshipQueryBuilder filter(ProfileFilter profileFilter) {
    this.profileFilter = profileFilter;
    return this;
  }
  
  private Predicate appendPredicate(CriteriaBuilder cb, Predicate pSource, Predicate input) {
    if (pSource != null) {
      if (input != null) {
        return cb.and(pSource, input);
      }
      return pSource;
    } else {
      return input;
    }
  }
  
  private <T> Predicate addInClause(CriteriaBuilder cb, Path<Type> path, Collection<Type> types) {
    In<Type> in = cb.in(path);
    for (Type value : types) {
      in.value(value);
    }
    return in;
  }
}

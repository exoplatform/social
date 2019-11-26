/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;

import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.social.core.jpa.search.XSpaceFilter;
import org.exoplatform.social.core.jpa.storage.entity.AppEntity_;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity_;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity.Status;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity_;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.SortBy;

public final class SpaceQueryBuilder {
  private long offset;
  private long limit;
  private XSpaceFilter spaceFilter;
  
  public static SpaceQueryBuilder builder() {
    return new SpaceQueryBuilder();
  }  
  
  public SpaceQueryBuilder offset(long offset) {
    this.offset = offset;
    return this;
  }
  
  public SpaceQueryBuilder limit(long limit) {
    this.limit = limit;
    return this;
  }
  
  public SpaceQueryBuilder filter(XSpaceFilter spaceFilter) {
    this.spaceFilter = spaceFilter;
    return this;
  }

  public TypedQuery<SpaceEntity> build() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<SpaceEntity> criteria = cb.createQuery(SpaceEntity.class);
    Root<SpaceEntity> spaceEntity = criteria.from(SpaceEntity.class);
    //
    CriteriaQuery<SpaceEntity> select = criteria.select(spaceEntity);
    if (!spaceFilter.isLastAccess() && !spaceFilter.isVisited()) {
      select.distinct(true);
    }

    Predicate predicateFilter = buildPredicateFilter(spaceEntity, criteria, cb, spaceEntity);
    if (predicateFilter.getExpressions().size() > 0) {
      select.where(predicateFilter);
    }
    select.orderBy(buildOrder(spaceEntity, cb));
    
    //
    TypedQuery<SpaceEntity> typedQuery = em.createQuery(select);
    if (this.limit > 0) {
      typedQuery.setFirstResult((int) offset);
      typedQuery.setMaxResults((int) limit);
    }
    //
    return typedQuery;
  }

  public TypedQuery<Long> buildCount() {
    EntityManager em = EntityManagerHolder.get();
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
    Root<SpaceEntity> spaceEntity = criteria.from(SpaceEntity.class);
    CriteriaQuery<Long> select = criteria.select(cb.countDistinct(spaceEntity));
    //
    Predicate predicateFilter = buildPredicateFilter(spaceEntity, criteria, cb, spaceEntity);
    if (predicateFilter.getExpressions().size() > 0) {
      select.where(predicateFilter);
    }
    //
    return em.createQuery(select);
  }
  
  private Predicate buildPredicateFilter(Root<SpaceEntity> root, CriteriaQuery<?> criteria, CriteriaBuilder cb, Root<SpaceEntity> connection) {
    List<Predicate> predicates = new LinkedList<>();
    
    //status
    if (!spaceFilter.getStatus().isEmpty()) {
      Path<SpaceMemberEntity> join = (Path<SpaceMemberEntity>) getMembersJoin(root, JoinType.INNER);

      List<Predicate> pStatusList = new LinkedList<>();      
      for(Status status : spaceFilter.getStatus()) {
        pStatusList.add(cb.equal(join.get(SpaceMemberEntity_.status), status));
      }      
      
      Predicate tmp = cb.and(cb.or(pStatusList.toArray(new Predicate[pStatusList.size()])),
                             cb.equal(join.get(SpaceMemberEntity_.userId), spaceFilter.getRemoteId()));
      boolean includePrivate = spaceFilter.isIncludePrivate();
      if (includePrivate) {
        predicates.add(cb.or(cb.equal(root.get(SpaceEntity_.visibility), SpaceEntity.VISIBILITY.PRIVATE), tmp));
      } else {
        predicates.add(tmp);
      }      
    }

    if (spaceFilter.isPublic()) {
      Subquery<Long> sub = criteria.subquery(Long.class);
      Root<SpaceEntity> spaceSub = sub.from(SpaceEntity.class);

      Path<SpaceMemberEntity> join = spaceSub.join(SpaceEntity_.members);
      sub.select(spaceSub.get(SpaceEntity_.id));
      sub.where(cb.equal(join.get(SpaceMemberEntity_.userId), spaceFilter.getRemoteId()));
      
      predicates.add(cb.not(cb.in(root.get(SpaceEntity_.id)).value(sub)));
    }

    //appid
    String app = spaceFilter.getAppId();    
    if (app != null && !(app = app.trim()).isEmpty()) {
      Subquery<Long> sub = criteria.subquery(Long.class);
      Root<SpaceEntity> spaceSub = sub.from(SpaceEntity.class);      
      sub.select(spaceSub.get(SpaceEntity_.id));
      
      Path<String> appPath = spaceSub.join(SpaceEntity_.app).get(AppEntity_.appId);
      
      List<Predicate> appCond = new LinkedList<>();
      for (String appId : app.split(",")) {
        appCond.add(cb.like(cb.lower(appPath), buildSearchCondition(appId, true)));
      }
      sub.where(cb.or(appCond.toArray(new Predicate[appCond.size()])));
      predicates.add(cb.in(root.get(SpaceEntity_.id)).value(sub));
    }

    //searchCondition
    String search = spaceFilter.getSpaceNameSearchCondition();
    if (search != null && !(search = search.trim()).isEmpty()) {
      String searchCondition = buildSearchCondition(search, true);
      Predicate prettyName = cb.like(cb.lower(root.get(SpaceEntity_.prettyName)), searchCondition);
      Predicate displayName = cb.like(cb.lower(root.get(SpaceEntity_.displayName)), searchCondition);
      Predicate description = cb.like(cb.lower(root.get(SpaceEntity_.description)), searchCondition);
      predicates.add(cb.or(prettyName, displayName, description));
    }    
    
    //first character
    char first = spaceFilter.getFirstCharacterOfSpaceName();
    if (!Character.isDigit(first) && first != '\u0000') {
      predicates.add(cb.like(cb.lower(root.get(SpaceEntity_.prettyName)), buildSearchCondition(String.valueOf(first), false)));
    }
    
    //not hidden
    boolean notHidden = spaceFilter.isNotHidden();
    if (notHidden) {
      predicates.add(cb.notEqual(root.get(SpaceEntity_.visibility), SpaceEntity.VISIBILITY.HIDDEN));
    }
    
    //
    return cb.and(predicates.toArray(new Predicate[predicates.size()]));
  }

  @SuppressWarnings("unchecked")
  private Join<SpaceEntity, SpaceMemberEntity> getMembersJoin(Root<SpaceEntity> root, JoinType joinType) {
    if(root.getJoins() == null || root.getJoins().isEmpty()) {
      SetJoin<SpaceEntity, SpaceMemberEntity> join = root.join(SpaceEntity_.members, joinType);
      join.alias("mem");
      return join;
    } else {
      return (Join<SpaceEntity, SpaceMemberEntity>) root.getJoins().iterator().next();
    }
  }

  private String buildSearchCondition(String app, boolean searchBack) {
    StringBuilder builder = new StringBuilder(app);
    removeAll(builder, "*");
    removeAll(builder, "%");
    removeAll(builder, "  ");
    
    if (searchBack) {
      builder.insert(0, "%");
    }
    builder.append("%");
    
    return builder.toString().toLowerCase();
  }

  private void removeAll(StringBuilder builder, String string) {
    int i;
    while ((i = builder.indexOf(string)) >= 0) {
      builder.deleteCharAt(i);
    }
  }

  private Order[] buildOrder(Root<SpaceEntity> spaceEntity, CriteriaBuilder cb) {
    List<Order> orders = new LinkedList<>();
    
    if (spaceFilter.isLastAccess()) {
      Path<SpaceMemberEntity> join = (Path<SpaceMemberEntity>) getMembersJoin(spaceEntity, JoinType.LEFT);
      orders.add(cb.desc(join.get(SpaceMemberEntity_.lastAccess)));
      orders.add(cb.asc(spaceEntity.get(SpaceEntity_.id)));
    } else if (spaceFilter.isVisited()) {
      Path<SpaceMemberEntity> join = (Path<SpaceMemberEntity>) getMembersJoin(spaceEntity, JoinType.LEFT);
      orders.add(cb.desc(join.get(SpaceMemberEntity_.visited)));
      orders.add(cb.asc(spaceEntity.get(SpaceEntity_.prettyName)));
    } else {
      Sorting sorting = spaceFilter.getSorting();
      if (sorting.orderBy.equals(Sorting.OrderBy.DESC)) {
        orders.add(cb.desc(getShortField(spaceEntity, sorting.sortBy)));
      } else {
        orders.add(cb.asc(getShortField(spaceEntity, sorting.sortBy)));
      }
    }

    return orders.toArray(new Order[orders.size()]);
  }

  private Expression<?> getShortField(Root<SpaceEntity> spaceEntity, SortBy sortBy) {
    if (sortBy.equals(SortBy.DATE)) {      
      return spaceEntity.get(SpaceEntity_.createdDate);
    } else {
      return spaceEntity.get(SpaceEntity_.prettyName);
    }
  }
}

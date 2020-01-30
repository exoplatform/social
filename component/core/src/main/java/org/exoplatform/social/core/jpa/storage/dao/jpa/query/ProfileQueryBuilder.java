/*
 * Copyright (C) 2015 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage.dao.jpa.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import org.exoplatform.social.core.jpa.search.ExtendProfileFilter;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity_;
import org.exoplatform.social.core.jpa.storage.entity.ProfileExperienceEntity;
import org.exoplatform.social.core.jpa.storage.entity.ProfileExperienceEntity_;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class ProfileQueryBuilder {

  ExtendProfileFilter filter;

  private ProfileQueryBuilder() {

  }

  public static ProfileQueryBuilder builder() {
    return new ProfileQueryBuilder();
  }

  public ProfileQueryBuilder withFilter(ExtendProfileFilter filter) {
    this.filter = filter;
    return this;
  }

  /**
   *
   * @param em the EntityManager
   * @return the JPA TypedQuery
   */
  public TypedQuery[] build(EntityManager em) {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery query = cb.createQuery(IdentityEntity.class);

    Root<IdentityEntity> identity = query.from(IdentityEntity.class);

    List<Predicate> predicates = new ArrayList<>();

    if (filter != null) {
      if (filter.isForceLoadProfile()) {
        //TODO: profile is now always EAGER load
//        Fetch<IdentityEntity,ProfileEntity> fetch = identity.fetch(IdentityEntity_.profile, JoinType.INNER);
      }

      if (filter.isExcludeDeleted()) {
        predicates.add(cb.isFalse(identity.get(IdentityEntity_.deleted)));
      }

      if (filter.isExcludeDisabled()) {
        predicates.add(cb.isTrue(identity.get(IdentityEntity_.enabled)));
      }

      if (filter.getIdentityIds() != null && filter.getIdentityIds().size() > 0) {
        predicates.add(identity.get(IdentityEntity_.id).in(filter.getIdentityIds()));
      }

      if (filter.getRemoteIds() != null && filter.getRemoteIds().size() > 0) {
        predicates.add(identity.get(IdentityEntity_.remoteId).in(filter.getRemoteIds()));
      }

      if (filter.getProviderId() != null && !filter.getProviderId().isEmpty()) {
        predicates.add(cb.equal(identity.get(IdentityEntity_.providerId), filter.getProviderId()));
      }

      SetJoin<IdentityEntity, ProfileExperienceEntity> experience = null;

      List<Identity> excludes = filter.getExcludedIdentityList();
      if (excludes != null && excludes.size() > 0) {
        List<Long> ids = new ArrayList<>(excludes.size());
        for (Identity id : excludes) {
          ids.add(Long.parseLong(id.getId()));
        }
        predicates.add(cb.not(identity.get(IdentityEntity_.id).in(ids)));
      }

      String all = filter.getAll();
      if (all == null || all.trim().isEmpty()) {
        String name = filter.getName();
        if (name != null && !name.isEmpty()) {
          name = processLikeString(name);
          MapJoin<IdentityEntity, String, String> properties = identity.join(IdentityEntity_.properties, JoinType.LEFT);
          predicates.add(cb.and(cb.like(cb.lower(properties.value()), name), properties.key().in(Arrays.asList(Profile.FIRST_NAME, Profile.LAST_NAME, Profile.FULL_NAME))));
        }

        String val = filter.getPosition();
        if (val != null && !val.isEmpty()) {
          val = processLikeString(val);
          Predicate[] p = new Predicate[2];
          MapJoin<IdentityEntity, String, String> properties = identity.join(IdentityEntity_.properties, JoinType.LEFT);
          p[1] = cb.and(cb.like(cb.lower(properties.value()), val), cb.equal(properties.key(), Profile.POSITION));
          if(experience == null) {
            experience = identity.join(IdentityEntity_.experiences, JoinType.LEFT);
          }
          p[0] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.position)), val);

          predicates.add(cb.or(p));
        }

        val = filter.getSkills();
        if (val != null && !val.isEmpty()) {
          val = processLikeString(val);
          if(experience == null) {
            experience = identity.join(IdentityEntity_.experiences, JoinType.LEFT);
          }
          predicates.add(cb.like(cb.lower(experience.get(ProfileExperienceEntity_.skills)), val));
        }

        val = filter.getCompany();
        if (val != null && !val.isEmpty()) {
          val = processLikeString(val);
          if(experience == null) {
            experience = identity.join(IdentityEntity_.experiences, JoinType.LEFT);
          }
          predicates.add(cb.like(cb.lower(experience.get(ProfileExperienceEntity_.company)), val));
        }
      } else {

        String name = filter.getName();
        all = processLikeString(all).toLowerCase();
        Predicate[] p = new Predicate[5];
        MapJoin<IdentityEntity, String, String> properties = identity.join(IdentityEntity_.properties, JoinType.LEFT);
        p[0] = cb.and(cb.like(cb.lower(properties.value()), name), properties.key().in(Arrays.asList(Profile.FIRST_NAME, Profile.LAST_NAME, Profile.FULL_NAME)));

        if(experience == null) {
          experience = identity.join(IdentityEntity_.experiences, JoinType.LEFT);
        }
        p[1] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.position)), all);
        p[2] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.skills)), all);
        p[3] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.company)), all);
        p[4] = cb.like(cb.lower(experience.get(ProfileExperienceEntity_.description)), all);

        predicates.add(cb.or(p));
      }

      char c = filter.getFirstCharacterOfName();
      if (c != '\u0000') {
        String val = Character.toLowerCase(c) + "%";
        MapJoin<IdentityEntity, String, String> properties = identity.join(IdentityEntity_.properties, JoinType.LEFT);
        predicates.add(cb.and(cb.equal(properties.key(), Profile.LAST_NAME), cb.like(cb.lower(properties.value()), val)));
      }
    }

    Predicate[] pds = predicates.toArray(new Predicate[predicates.size()]);

    query.select(cb.countDistinct(identity)).where(pds);
    TypedQuery<Long> count = em.createQuery(query);

    query.select(identity).distinct(true).where(pds);
    TypedQuery<IdentityEntity> select = em.createQuery(query);


    return new TypedQuery[]{select, count};
  }

  private String processLikeString(String s) {
    return "%" + s.toLowerCase() + "%";
  }
}

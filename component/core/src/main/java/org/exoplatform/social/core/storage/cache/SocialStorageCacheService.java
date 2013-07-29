/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.cache;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.social.core.storage.cache.model.data.ActivityData;
import org.exoplatform.social.core.storage.cache.model.data.IdentityData;
import org.exoplatform.social.core.storage.cache.model.data.IntegerData;
import org.exoplatform.social.core.storage.cache.model.data.ListActivitiesData;
import org.exoplatform.social.core.storage.cache.model.data.ListIdentitiesData;
import org.exoplatform.social.core.storage.cache.model.data.ListSpacesData;
import org.exoplatform.social.core.storage.cache.model.data.ProfileData;
import org.exoplatform.social.core.storage.cache.model.data.RelationshipData;
import org.exoplatform.social.core.storage.cache.model.data.SpaceData;
import org.exoplatform.social.core.storage.cache.model.data.SuggestionsData;
import org.exoplatform.social.core.storage.cache.model.key.ActivityCountKey;
import org.exoplatform.social.core.storage.cache.model.key.ActivityKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityCompositeKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.IdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.ListActivitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.ListIdentitiesKey;
import org.exoplatform.social.core.storage.cache.model.key.ListRelationshipsKey;
import org.exoplatform.social.core.storage.cache.model.key.ListSpacesKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipCountKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipIdentityKey;
import org.exoplatform.social.core.storage.cache.model.key.RelationshipKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceFilterKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceKey;
import org.exoplatform.social.core.storage.cache.model.key.SpaceRefKey;
import org.exoplatform.social.core.storage.cache.model.key.SuggestionKey;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SocialStorageCacheService {

  // IdentityStorage
  private final ExoCache<IdentityKey, IdentityData> identityCache;
  private final ExoCache<IdentityCompositeKey, IdentityKey> identityIndexCache;
  private final ExoCache<IdentityKey, ProfileData> profileCache;
  private final ExoCache<IdentityFilterKey, IntegerData> countIdentitiesCache;
  private final ExoCache<ListIdentitiesKey, ListIdentitiesData> identitiesCache;

  // RelationshipStorage
  private final ExoCache<RelationshipKey, RelationshipData> relationshipCache;
  private final ExoCache<RelationshipIdentityKey, RelationshipKey> relationshipCacheByIdentity;
  private final ExoCache<RelationshipCountKey, IntegerData> relationshipsCount;
  private final ExoCache<ListRelationshipsKey, ListIdentitiesData> relationshipsCache;
  
  // Suggestion
  private final ExoCache<SuggestionKey, SuggestionsData> suggestionCache;

  // ActivityStorage
  private final ExoCache<ActivityKey, ActivityData> activityCache;
  private final ExoCache<ActivityCountKey, IntegerData> activitiesCountCache;
  private final ExoCache<ListActivitiesKey, ListActivitiesData> activitiesCache;

  // SpaceStorage
  private final ExoCache<SpaceKey, SpaceData> spaceCache;
  private final ExoCache<SpaceRefKey, SpaceKey> spaceRefCache;
  private final ExoCache<SpaceFilterKey, IntegerData> spacesCountCache;
  private final ExoCache<ListSpacesKey, ListSpacesData> spacesCache;

  public SocialStorageCacheService(CacheService cacheService) {
    
    this.identityCache = CacheType.IDENTITY.getFromService(cacheService);
    this.identityIndexCache = CacheType.IDENTITY_INDEX.getFromService(cacheService);
    this.profileCache = CacheType.PROFILE.getFromService(cacheService);
    this.countIdentitiesCache = CacheType.IDENTITIES_COUNT.getFromService(cacheService);
    this.identitiesCache = CacheType.IDENTITIES.getFromService(cacheService);

    this.relationshipCache = CacheType.RELATIONSHIP.getFromService(cacheService);
    this.relationshipCacheByIdentity = CacheType.RELATIONSHIP_FROM_IDENTITY.getFromService(cacheService);
    this.relationshipsCount = CacheType.RELATIONSHIPS_COUNT.getFromService(cacheService);
    this.relationshipsCache = CacheType.RELATIONSHIPS.getFromService(cacheService);
    
    this.suggestionCache = CacheType.SUGGESTIONS.getFromService(cacheService);

    this.activityCache = CacheType.ACTIVITY.getFromService(cacheService);
    this.activitiesCountCache = CacheType.ACTIVITIES_COUNT.getFromService(cacheService);
    this.activitiesCache = CacheType.ACTIVITIES.getFromService(cacheService);

    this.spaceCache = CacheType.SPACE.getFromService(cacheService);
    this.spaceRefCache = CacheType.SPACE_REF.getFromService(cacheService);
    this.spacesCountCache = CacheType.SPACES_COUNT.getFromService(cacheService);
    this.spacesCache = CacheType.SPACES.getFromService(cacheService);

  }

  public ExoCache<IdentityKey, IdentityData> getIdentityCache() {
    return identityCache;
  }

  public ExoCache<IdentityCompositeKey, IdentityKey> getIdentityIndexCache() {
    return identityIndexCache;
  }

  public ExoCache<IdentityKey, ProfileData> getProfileCache() {
    return profileCache;
  }

  public ExoCache<IdentityFilterKey, IntegerData> getCountIdentitiesCache() {
    return countIdentitiesCache;
  }

  public ExoCache<ListIdentitiesKey, ListIdentitiesData> getIdentitiesCache() {
    return identitiesCache;
  }

  public ExoCache<RelationshipKey, RelationshipData> getRelationshipCache() {
    return relationshipCache;
  }

  public ExoCache<SuggestionKey, SuggestionsData> getSuggestionCache() {
    return suggestionCache;
  }
  
  public ExoCache<RelationshipIdentityKey, RelationshipKey> getRelationshipCacheByIdentity() {
    return relationshipCacheByIdentity;
  }

  public ExoCache<RelationshipCountKey, IntegerData> getRelationshipsCount() {
    return relationshipsCount;
  }

  public ExoCache<ListRelationshipsKey, ListIdentitiesData> getRelationshipsCache() {
    return relationshipsCache;
  }

  public ExoCache<ActivityKey, ActivityData> getActivityCache() {
    return activityCache;
  }

  public ExoCache<ActivityCountKey, IntegerData> getActivitiesCountCache() {
    return activitiesCountCache;
  }

  public ExoCache<ListActivitiesKey, ListActivitiesData> getActivitiesCache() {
    return activitiesCache;
  }

  public ExoCache<SpaceKey, SpaceData> getSpaceCache() {
    return spaceCache;
  }

  public ExoCache<SpaceRefKey, SpaceKey> getSpaceRefCache() {
    return spaceRefCache;
  }

  public ExoCache<SpaceFilterKey, IntegerData> getSpacesCountCache() {
    return spacesCountCache;
  }

  public ExoCache<ListSpacesKey, ListSpacesData> getSpacesCache() {
    return spacesCache;
  }
}

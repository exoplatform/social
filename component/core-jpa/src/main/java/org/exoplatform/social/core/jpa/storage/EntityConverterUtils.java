package org.exoplatform.social.core.jpa.storage;

import java.util.*;
import java.util.Map.Entry;

import org.exoplatform.commons.file.model.FileInfo;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.storage.entity.ProfileExperienceEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.IdentityWithRelationship;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.service.LinkProvider;

public class EntityConverterUtils {
  
  private static final Log LOG = ExoLogger.getLogger(EntityConverterUtils.class);
  
  public static Identity convertToIdentity(IdentityEntity entity) {
    return convertToIdentity(entity, true);
  }

  public static Identity convertToIdentity(IdentityEntity entity, boolean mapDeleted) {
    if (entity.isDeleted() && !mapDeleted) {
      return null;
    }

    Identity identity = new Identity(entity.getStringId());
    mapToIdentity(entity, identity);
    return identity;
  }

  public static void mapToIdentity(IdentityEntity entity, Identity identity) {
    identity.setProviderId(entity.getProviderId());
    identity.setRemoteId(entity.getRemoteId());
    identity.setProfile(convertToProfile(entity, identity));
    identity.setEnable(entity.isEnabled());
    identity.setDeleted(entity.isDeleted());
  }

  public static Profile convertToProfile(IdentityEntity entity, Identity identity) {
    Profile p = new Profile(identity);
    p.setId(String.valueOf(identity.getId()));
    mapToProfile(entity, p);
    if (OrganizationIdentityProvider.NAME.equals(identity.getProviderId()) && p.getProperty(Profile.USERNAME) == null) {
      p.getProperties().put(Profile.USERNAME, identity.getRemoteId());
    }
    return p;
  }

  public static void mapToProfile(IdentityEntity entity, Profile p) {
    Map<String, String> properties = entity.getProperties();
    
    Map<String, Object> props = p.getProperties();
    String providerId = entity.getProviderId();
    Identity identity = p.getIdentity();
    if (!OrganizationIdentityProvider.NAME.equals(providerId) && !SpaceIdentityProvider.NAME.equals(providerId)) {
      p.setUrl(properties.get(Profile.URL));
      p.setAvatarUrl(LinkProvider.buildAvatarURL(identity.getProviderId(), identity.getRemoteId()));
    } else {
      String remoteId = entity.getRemoteId();
      if (OrganizationIdentityProvider.NAME.equals(providerId)) {
        p.setUrl(LinkProvider.getUserProfileUri(remoteId));

      } else if (SpaceIdentityProvider.NAME.equals(providerId)) {
          p.setUrl(LinkProvider.getSpaceUri(remoteId));
      }
      if (entity.getAvatarFileId() != null && entity.getAvatarFileId() > 0) {
        p.setAvatarUrl(LinkProvider.buildAvatarURL(identity.getProviderId(), identity.getRemoteId()));
        Long lastUpdated = getAvatarLastUpdated(entity.getAvatarFileId());
        if (lastUpdated != null) {
          p.setAvatarLastUpdated(lastUpdated);
        }
      }
    }
    StringBuilder skills = new StringBuilder();
    StringBuilder positions = new StringBuilder();
    Set<ProfileExperienceEntity> experiences = entity.getExperiences();
    if (experiences != null && experiences.size() > 0) {
      List<Map<String, Object>> xpData = new ArrayList<>();
      for (ProfileExperienceEntity exp : experiences){
        Map<String, Object> xpMap = new HashMap<String, Object>();
        if (exp.getSkills() != null && !exp.getSkills().isEmpty()) {
          skills.append(exp.getSkills()).append(",");
        }
        if (exp.getPosition() != null && !exp.getPosition().isEmpty()) {
          positions.append(exp.getPosition()).append(",");
        }
        xpMap.put(Profile.EXPERIENCES_SKILLS, exp.getSkills());
        xpMap.put(Profile.EXPERIENCES_POSITION, exp.getPosition());
        xpMap.put(Profile.EXPERIENCES_START_DATE, exp.getStartDate());
        xpMap.put(Profile.EXPERIENCES_END_DATE, exp.getEndDate());
        xpMap.put(Profile.EXPERIENCES_COMPANY, exp.getCompany());
        xpMap.put(Profile.EXPERIENCES_DESCRIPTION, exp.getDescription());
        xpMap.put(Profile.EXPERIENCES_IS_CURRENT, exp.isCurrent());
        xpData.add(xpMap);
      }
      props.put(Profile.EXPERIENCES, xpData);
    }
    if (skills.length() > 0) {
      skills.deleteCharAt(skills.length() - 1);
      props.put(Profile.EXPERIENCES_SKILLS, skills.toString());
    }
    if (positions.length() > 0) {
      positions.deleteCharAt(positions.length() - 1);
      props.put(Profile.POSITION, positions.toString());
    }
    
    if (properties != null && properties.size() > 0) {
      for (Entry<String, String> entry : properties.entrySet()) {
        String key = entry.getKey();
        String value = entry.getValue();

        if (Profile.CONTACT_IMS.equals(key) || Profile.CONTACT_PHONES.equals(key) || Profile.CONTACT_URLS.equals(key)) {
          List<Map<String, String>> list = new ArrayList<>();
          try {
            JSONArray arr = new JSONArray(value);
            for (int i = 0 ; i < arr.length(); i++) {
              Map<String, String> map = new HashMap<>();
              JSONObject json = arr.getJSONObject(i);
              Iterator<String> keys = json.keys();
              while (keys.hasNext()) {
                String k = keys.next();
                map.put(k, json.optString(k));
              }
              list.add(map);
            }
          } catch (JSONException ex) {
            // Ignore this exception
          }

          props.put(key, list);

        } else if (!Profile.URL.equals(key)) {
          props.put(key, value);
        }
      }
    }

    p.setCreatedTime(entity.getCreatedDate().getTime());
    p.setLastLoaded(System.currentTimeMillis());
  }

  public static void mapToEntity(Identity identity, IdentityEntity entity) {
    entity.setProviderId(identity.getProviderId());
    entity.setRemoteId(identity.getRemoteId());
    entity.setEnabled(identity.isEnable());
    entity.setDeleted(identity.isDeleted());
  }

  public static long parseId(String id) {
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException ex) {
      return 0;
    }
  }

  public static List<IdentityWithRelationship> convertToIdentitiesWithRelationship(ListAccess<Entry<IdentityEntity, ConnectionEntity>> list, int offset, int limit) {
    try {
      if (list == null) {
        return Collections.emptyList();
      }

      Entry<IdentityEntity, ConnectionEntity>[] entities = list.load(offset, limit);
      
      List<IdentityWithRelationship> result = new ArrayList<>(limit);
      for (Entry<IdentityEntity, ConnectionEntity> tuple : entities) {
        IdentityEntity identityEntity = (IdentityEntity) tuple.getKey();
        ConnectionEntity connectionEntity = (ConnectionEntity) tuple.getValue();

        IdentityWithRelationship identityWithRelationship = new IdentityWithRelationship(identityEntity.getStringId());
        mapToIdentity(identityEntity, identityWithRelationship);

        Relationship relationship = convertRelationshipItemToRelationship(connectionEntity);
        identityWithRelationship.setRelationship(relationship);

        result.add(identityWithRelationship);
      }
      return result;
    } catch (Exception ex) {
      return Collections.emptyList();
    }
  }

  public static List<Identity> convertToIdentities(ListAccess<IdentityEntity> list, long offset, long limit) {
    try {
      return convertToIdentities(list.load((int)offset, (int)limit));
    } catch (Exception ex) {
      return Collections.emptyList();
    }
  }

  public static List<Identity> convertToIdentities(IdentityEntity[] entities) {
    if (entities == null || entities.length == 0) {
      return Collections.emptyList();
    }

    List<Identity> result = new ArrayList<>(entities.length);
    for (IdentityEntity entity : entities) {
      Identity idt = convertToIdentity(entity);
      if (idt != null) {
        result.add(idt);
      }
    }
    return result;
  }

  public static Relationship convertRelationshipItemToRelationship(ConnectionEntity item) {
    if (item == null) return null;
    //
    Relationship relationship = new Relationship(Long.toString(item.getId()));
    relationship.setId(String.valueOf(item.getId()));
    relationship.setSender(convertToIdentity(item.getSender()));
    relationship.setReceiver(convertToIdentity(item.getReceiver()));
    relationship.setStatus(item.getStatus());
    return relationship;
  }
  
  private static Long getAvatarLastUpdated(Long avatarFileId) {
    FileService fileService = ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(FileService.class);
    if (fileService != null) {
      FileInfo fileInfo = fileService.getFileInfo(avatarFileId);
      if (fileInfo != null && fileInfo.getUpdatedDate() != null) {
        return fileInfo.getUpdatedDate().getTime();
      }
      return null;
    } else {
      LOG.warn("File service is null");
      return null;
    }
  }
  
}

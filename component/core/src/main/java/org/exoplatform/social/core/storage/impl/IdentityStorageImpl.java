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

package org.exoplatform.social.core.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;

import org.chromattic.api.query.Ordering;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.chromattic.ext.ntdef.NTFile;
import org.chromattic.ext.ntdef.Resource;
import org.chromattic.core.query.QueryImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.chromattic.entity.ActivityProfileEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.chromattic.entity.ProfileXpEntity;
import org.exoplatform.social.core.chromattic.entity.ProviderEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipListEntity;
import org.exoplatform.social.core.chromattic.entity.SpaceRef;
import org.exoplatform.social.core.identity.IdentityResult;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.AttachedActivityType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeAlreadyExistsException;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.WhereExpression;

/**
 * IdentityStorage implementation.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IdentityStorageImpl extends AbstractStorage implements IdentityStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(IdentityStorageImpl.class);

  private IdentityStorage identityStorage;
  private RelationshipStorage relationshipStorage;
  private SpaceStorage spaceStorage;
  private OrganizationService organizationService;
  

  static enum PropNs {

    VOID("void"),
    IM("im"),
    PHONE("phone"),
    URL("url"),
    INDEX("index");

    private String prefix;
    private static final String SEPARATOR = "-";

    private PropNs(final String prefix) {
      this.prefix = prefix;
    }

    public String nameOf(String prop) {
      return String.format("%s%s%s", this.prefix, SEPARATOR, prop);
    }

    public static PropNs nsOf(String fullName) {
      int index = fullName.indexOf(SEPARATOR);
      String prefix = (index >= 0 ? fullName.substring(0, index) : fullName);
      return valueOf(prefix.toUpperCase());
    }

    public static String cleanPrefix(String name) {
      int index = name.indexOf(SEPARATOR) + 1;
      return (index >= 0 ? name.substring(index) : name);
    }

  }

  private Map<String, List<String>> createEntityParamMap(Object value) {

    Map<String, List<String>> params = new HashMap<String, List<String>>();
    List<Map<String, String>> map = (List<Map<String, String>>) value;

    for (Map<String, String> data : map) {
      
      List<String> got = params.get(data.get("key"));
      if (got == null) {
        got = new ArrayList<String>();
      }

      got.add(data.get("value"));
      params.put(data.get("key"), got);

    }

    return params;
  }

  private void fillProfileParam(ProfileEntity profileEntity, List<Map<String, String>> dataList, String name) {
    for (String currentValue : profileEntity.getProperty(name)) {
      Map<String, String> map = new HashMap<String, String>();
      map.put("key", PropNs.cleanPrefix(name));
      map.put("value", currentValue);
      dataList.add(map);
    }
  }

  private void putParam(ProfileEntity profileEntity, Object value, PropNs propNs) {
    Map<String, List<String>> params = createEntityParamMap(value);
    for (String paramKey : params.keySet()) {
      profileEntity.setProperty(propNs.nameOf(paramKey), params.get(paramKey));
    }
  }

  private void clearPropertyForPrefix(ProfileEntity profileEntity, String prefix) {
    for (String key : profileEntity.getProperties().keySet()) {
      if (key.startsWith(prefix)) {
        profileEntity.setProperty(key, null);
      }
    }
  }

  private void putParam(ProfileEntity profileEntity, Object value, String key) {
    Map<String, List<String>> params = createEntityParamMap(value);
    Iterator<List<String>> valuesItr = params.values().iterator();
    List<String> values = null;
    if (valuesItr.hasNext()) {
      values = (List<String>) valuesItr.next();
    }
    profileEntity.setProperty(key, values);
  }

  private IdentityStorage getStorage() {
    return (identityStorage != null ? identityStorage : this);
  }
  
  private RelationshipStorage getRelationshipStorage() {
    if (relationshipStorage == null) {
      relationshipStorage = (RelationshipStorage) PortalContainer.getInstance().
                                                                  getComponentInstanceOfType(RelationshipStorage.class);
    }

    return relationshipStorage;
  }
  
  private OrganizationService getOrganizationService() {
    if (organizationService == null) {
      organizationService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
    }

    return organizationService;
  }

  private SpaceStorage getSpaceStorage() {
    if (spaceStorage == null) {
      spaceStorage = (SpaceStorage) PortalContainer.getInstance().getComponentInstanceOfType(SpaceStorage.class);
    }

    return spaceStorage;
  }

  private QueryResult<ProfileEntity> getSpaceMemberIdentitiesByProfileFilterQueryBuilder(Space space,
      final ProfileFilter profileFilter, Type type,  long offset, long limit, boolean count)
      throws IdentityStorageException {

    if (offset < 0) {
      offset = 0;
    }

    String inputName = profileFilter.getName().replace(StorageUtils.ASTERISK_STR, StorageUtils.PERCENT_STR);
    StorageUtils.processUsernameSearchPattern(inputName.trim());
    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();

    QueryBuilder<ProfileEntity> builder = getSession().createQueryBuilder(ProfileEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    whereExpression.startGroup();
    whereExpression
        .like(JCRProperties.path, getProviderRoot().getProviders().get(
                                OrganizationIdentityProvider.NAME).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR)
        .and()
        .not().equals(ProfileEntity.deleted, "true");;

    StorageUtils.applyExcludes(whereExpression, excludedIdentityList);
    StorageUtils.applyFilter(whereExpression, profileFilter);

    List<Identity> relations = new ArrayList<Identity>();

    try {
      Space gotSpace = getSpaceStorage().getSpaceById(space.getId());

      String[] members = null;
      switch (type) {
        case MEMBER:
          members = gotSpace.getMembers();
          break;
        case MANAGER:
          members = gotSpace.getManagers();
          break;
      }

      for (int i = 0; i <  members.length; i++){
        relations.add(findIdentity(OrganizationIdentityProvider.NAME, members[i]));
      }

    } catch (IdentityStorageException e){
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_FIND_IDENTITY);
    }
    whereExpression.endGroup();
    whereExpression.and();
    StorageUtils.applyWhereFromIdentity(whereExpression, relations);

    builder.where(whereExpression.toString());
    applyOrder(builder, profileFilter);

    if(count){
     return builder.where(whereExpression.toString()).get().objects();
    } else {
     return builder.where(whereExpression.toString()).get().objects(offset, limit);
    }

  }

  private void applyOrder(QueryBuilder builder, ProfileFilter profileFilter) {

    //
    Sorting sorting;
    if (profileFilter == null) {
      sorting = new Sorting(Sorting.SortBy.TITLE, Sorting.OrderBy.ASC);
    } else {
      sorting = profileFilter.getSorting();
    }

    //
    Ordering ordering = Ordering.valueOf(sorting.orderBy.toString());
    switch (sorting.sortBy) {
      case DATE:
        builder.orderBy(ProfileEntity.createdTime.getName(), ordering);
        break;
      case RELEVANCY:
        builder.orderBy(JCRProperties.JCR_RELEVANCY.getName(), ordering);
      case TITLE:
        builder.orderBy(ProfileEntity.fullName.getName(), ordering);
        break;
    }
  }

  /*
   * Internal
   */

  protected IdentityEntity _createIdentity(final Identity identity) throws NodeAlreadyExistsException {

    // Get provider
    ProviderEntity providerEntity = getProviderRoot().getProvider(identity.getProviderId());

    // Create Identity
    if (providerEntity.getIdentities().containsKey(identity.getRemoteId())) {
      throw new NodeAlreadyExistsException("Identity " + identity.getRemoteId() + " already exists");
    }

    IdentityEntity identityEntity = providerEntity.createIdentity();
    providerEntity.getIdentities().put(identity.getRemoteId(), identityEntity);
    identityEntity.setProviderId(identity.getProviderId());
    identityEntity.setRemoteId(identity.getRemoteId());
    identityEntity.setDeleted(identity.isDeleted());
    identity.setId(identityEntity.getId());

    //
    getSession().save();

    //
    LOG.debug(String.format(
        "Identity %s:%s (%s) created",
        identity.getProviderId(),
        identity.getRemoteId(),
        identity.getId()
    ));

    //
    return identityEntity;
  }

  protected void _saveIdentity(final Identity identity) throws NodeAlreadyExistsException, NodeNotFoundException {

    IdentityEntity identityEntity;

    identityEntity = _findById(IdentityEntity.class, identity.getId());

    // change name
    if (!identityEntity.getName().equals(identity.getRemoteId())) {
      identityEntity.setName(identity.getRemoteId());
    }

    if (!identityEntity.getProviderId().equals(identity.getProviderId())) {

      // Get provider
      ProviderEntity providerEntity = getProviderRoot().getProvider(identity.getProviderId());

      // Move identity
      providerEntity.getIdentities().put(identity.getRemoteId(), identityEntity);
    }

    //
    identityEntity.setProviderId(identity.getProviderId());
    identityEntity.setRemoteId(identity.getRemoteId());
    identityEntity.setDeleted(identity.isDeleted());
    identity.setId(identityEntity.getId());

    //
    getSession().save();


    //
    LOG.debug(String.format(
        "Identity %s:%s (%s) saved",
        identity.getProviderId(),
        identity.getRemoteId(),
        identity.getId()
    ));
  }

  protected void _deleteIdentity(final Identity identity) throws NodeNotFoundException {

    //
    if (identity == null || identity.getId() == null) {
      throw new IllegalArgumentException();
    }

    //
    IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());
    identity.setProviderId(identityEntity.getProviderId());
    identity.setRemoteId(identityEntity.getRemoteId());

    //
    getSession().remove(identityEntity);

    //
    getSession().save();

    //
    LOG.debug(String.format(
        "Identity %s:%s (%s) deleted",
        identity.getProviderId(),
        identity.getRemoteId(),
        identity.getId()
    ));
  }

  protected void _hardDeleteIdentity(final Identity identity) throws NodeNotFoundException {

    //
    if (identity == null || identity.getId() == null) {
      throw new IllegalArgumentException();
    }

    IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());

    // Check if last manager
    Collection<SpaceRef> refs = identityEntity.getManagerSpaces().getRefs().values();
    for (SpaceRef ref : refs) {
      if (ref.getSpaceRef() != null && ref.getSpaceRef().getManagerMembersId().length == 1) {
        throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_DELETE_IDENTITY,
                                           "Unable to remove the last manager of space " + ref.getSpaceRef().getName());
      }
    }

    // Remove relationships
    _removeRelationshipList(identityEntity.getSender());
    _removeRelationshipList(identityEntity.getReceiver());
    _removeRelationshipList(identityEntity.getRelationship());
    _removeRelationshipList(identityEntity.getIgnore());
    _removeRelationshipList(identityEntity.getIgnored());

    // Remove space membership
    _removeSpaceMembership(SpaceStorageImpl.RefType.MANAGER, identityEntity);
    _removeSpaceMembership(SpaceStorageImpl.RefType.MEMBER, identityEntity);
    _removeSpaceMembership(SpaceStorageImpl.RefType.PENDING, identityEntity);
    _removeSpaceMembership(SpaceStorageImpl.RefType.INVITED, identityEntity);

    // Remove identity
    identity.setProviderId(identityEntity.getProviderId());
    identity.setRemoteId(identityEntity.getRemoteId());
    //
    identityEntity.setDeleted(Boolean.TRUE);
    Profile profile = loadProfile(new Profile(new Identity(identityEntity.getId())));
    profile.setProperty(Profile.DELETED, "true");
    saveProfile(profile);

    //
    getSession().save();

    //
    LOG.debug(String.format(
        "Identity %s:%s (%s) deleted",
        identity.getProviderId(),
        identity.getRemoteId(),
        identity.getId()
    ));
    
  }

  protected void _removeRelationshipList(RelationshipListEntity listEntity) {

    for (RelationshipEntity relationshipEntity : listEntity.getRelationships().values()) {
      getRelationshipStorage().removeRelationship(new Relationship(relationshipEntity.getId()));
    }

  }

  protected void _removeSpaceMembership(SpaceStorageImpl.RefType refType, IdentityEntity identity) {

    for(SpaceRef ref : refType.refsOf(identity).getRefs().values()) {
      Space space = getSpaceStorage().getSpaceById(ref.getSpaceRef().getId());
      String[] ids = refType.idsOf(space);
      List<String> idList = new ArrayList<String>(Arrays.asList(ids));
      idList.remove(identity.getRemoteId());
      refType.setIds(space, idList.toArray(new String[]{}));
      getSpaceStorage().saveSpace(space, false);
    }

  }

  protected Profile _createProfile(final Profile profile) throws NodeNotFoundException {

    //
    Identity identity = profile.getIdentity();
    if (identity.getId() == null) {
      throw new IllegalArgumentException();
    }

    //
    IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());
    ProfileEntity profileEntity = identityEntity.createProfile();
    
    //
    identityEntity.setProfile(profileEntity);
    profile.setId(profileEntity.getId());
    
    //
    ActivityProfileEntity activityPEntity = profileEntity.createActivityProfile();
    profileEntity.setActivityProfile(activityPEntity);
    
    profile.setCreatedTime(System.currentTimeMillis());

    //
    getSession().save();

    //
    LOG.debug(String.format(
        "Profile '%s' for %s:%s (%s) created",
        profile.getId(),
        identity.getProviderId(),
        identity.getRemoteId(),
        identity.getId()
    ));

    _saveProfile(profile);

    return profile;

  }

  protected Profile _loadProfile(final Profile profile) throws NodeNotFoundException {

    //
    if (profile.getIdentity().getId() == null) {
      throw new IllegalArgumentException();
    }

    //
    String identityId = profile.getIdentity().getId();
    IdentityEntity identityEntity = _findById(IdentityEntity.class, identityId);
    ProfileEntity profileEntity = identityEntity.getProfile();
    if (profileEntity == null) {
      throw new NodeNotFoundException("The identity " + identityId + " has no profile");
    }
    profile.setId(profileEntity.getId());
    populateProfile(profile, profileEntity);

    //
    LOG.debug(String.format(
        "Profile '%s' for %s:%s (%s) loaded",
        profile.getId(),
        identityEntity.getProviderId(),
        identityEntity.getRemoteId(),
        identityEntity.getId()
    ));

    return profile;
  }

  protected void _saveProfile(final Profile profile) throws NodeNotFoundException {

    if (profile.getIdentity().getId() == null || profile.getId() == null) {
      throw new NullPointerException();
    }

    //
    ProfileEntity profileEntity = _findById(ProfileEntity.class, profile.getId());
    String providerId = profile.getIdentity().getProviderId();

    Map<String, List<String>> phonesData = new HashMap<String, List<String>>();

    //
    for (String key : profile.getProperties().keySet()) {
      if (isJcrProperty(key)) {
        Object value = profile.getProperty(key);
        if (Profile.CONTACT_IMS.equals(key)) {
          clearPropertyForPrefix(profileEntity, PropNs.IM.prefix);
          putParam(profileEntity, value, PropNs.IM);
        }
        else if (Profile.CONTACT_PHONES.equals(key)) {
          clearPropertyForPrefix(profileEntity, PropNs.PHONE.prefix);
          putParam(profileEntity, value, PropNs.PHONE);
        }
        else if (Profile.CONTACT_URLS.equals(key)) {
          clearPropertyForPrefix(profileEntity, PropNs.URL.prefix);
          putParam(profileEntity, value, PropNs.URL.toString().toLowerCase());
        }
        else if (Profile.EXPERIENCES.equals(key)) {
          // TODO : update instead of remove/add
          for (ProfileXpEntity xpEntity : profileEntity.getXps().values()) {
            _removeById(ProfileXpEntity.class, xpEntity.getId());
          }

          // create
          List<String> skills = new ArrayList<String>();
          List<String> positions = new ArrayList<String>();
          List<String> organizations = new ArrayList<String>();
          List<String> jobsDescription = new ArrayList<String>();
          for (Map<String, String> currentXp : (List<Map<String, String>>) value) {

            ProfileXpEntity xpEntity = profileEntity.createXp();
            profileEntity.getXps().put(String.valueOf(System.currentTimeMillis()), xpEntity);
            xpEntity.setSkills(currentXp.get(Profile.EXPERIENCES_SKILLS));
            xpEntity.setPosition(currentXp.get(Profile.EXPERIENCES_POSITION));
            xpEntity.setStartDate(currentXp.get(Profile.EXPERIENCES_START_DATE));
            xpEntity.setEndDate(currentXp.get(Profile.EXPERIENCES_END_DATE));
            xpEntity.setCompany(currentXp.get(Profile.EXPERIENCES_COMPANY));
            xpEntity.setDescription(currentXp.get(Profile.EXPERIENCES_DESCRIPTION));

            //
            if (xpEntity.getSkills() != null) {
              skills.add(xpEntity.getSkills());
            }
            //
            if (xpEntity.getPosition() != null) {
              positions.add(xpEntity.getPosition());
            }
            //
            if (xpEntity.getCompany() != null) {
              organizations.add(xpEntity.getCompany());
            }
            //
            if (xpEntity.getDescription() != null) {
              jobsDescription.add(xpEntity.getDescription());
            }

          }
          profileEntity.setProperty(PropNs.INDEX.nameOf(Profile.EXPERIENCES_SKILLS), skills);
          profileEntity.setProperty(PropNs.INDEX.nameOf(Profile.EXPERIENCES_POSITION), positions);
          profileEntity.setProperty(PropNs.INDEX.nameOf(Profile.EXPERIENCES_COMPANY), organizations);
          profileEntity.setProperty(PropNs.INDEX.nameOf(Profile.EXPERIENCES_DESCRIPTION), jobsDescription);

        }
        else if (Profile.AVATAR.equals(key)) {
          AvatarAttachment attachement = (AvatarAttachment) value;
          NTFile avatar = profileEntity.getAvatar();
          if (avatar == null) {
            avatar = profileEntity.createAvatar();
            profileEntity.setAvatar(avatar);
          }
          avatar.setContentResource(new Resource(attachement.getMimeType(), null, attachement.getImageBytes()));
        }
        else {
          //need to check here to avoid to set property with name: "void-skills"
          if (Profile.EXPERIENCES_SKILLS.equals(key) == false) {
            if (value != null) {
              List<String> lvalue = new ArrayList<String>();
              lvalue.add((String) value);
              profileEntity.setProperty(PropNs.VOID.nameOf(key), lvalue);
            } else {
              profileEntity.setProperty(PropNs.VOID.nameOf(key), null);
            }
          }
        }
      }
    }
    
    // TODO : find better
    profileEntity.setParentId(profile.getIdentity().getId());
    profileEntity.setCreatedTime(profile.getCreatedTime());

    // External profile
    if (!OrganizationIdentityProvider.NAME.equals(providerId) && !SpaceIdentityProvider.NAME.equals(providerId)) {
      profileEntity.setExternalUrl(profile.getUrl());
      profileEntity.setExternalAvatarUrl(profile.getAvatarUrl());
    }
    
    getSession().save();

    //
    LOG.debug(String.format(
        "Profile '%s' for %s:%s (%s) saved",
        profile.getId(),
        profileEntity.getIdentity().getProviderId(),
        profileEntity.getIdentity().getRemoteId(),
        profileEntity.getIdentity().getId()
    ));
  }

  protected Identity _findIdentity(final String providerId, final String remoteId) throws NodeNotFoundException {

    IdentityEntity identityEntity = _findIdentityEntity(providerId, remoteId);

    Identity identity = new Identity(providerId, remoteId);
    identity.setDeleted(identityEntity.isDeleted());
    identity.setId(identityEntity.getId());

    try {
      _loadProfile(identity.getProfile());
    } catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e);
    }

    //
    LOG.debug(String.format(
        "Identity  %s:%s (%s) found",
        identity.getProviderId(),
        identity.getRemoteId(),
        identity.getId()
    ));

    return identity;
  }

  protected IdentityEntity _findIdentityEntity(final String providerId, final String remoteId)
      throws NodeNotFoundException {

    ProviderEntity providerEntity = getProviderRoot().getProviders().get(providerId);

    if (providerEntity == null) {
      throw new NodeNotFoundException("The node " + providerId + "/" + remoteId + " doesn't be found");
    }

    IdentityEntity identityEntity = providerEntity.getIdentities().get(remoteId);

    if (identityEntity == null) {
      throw new NodeNotFoundException("The node " + providerId + "/" + remoteId + " doesn't be found");
    }

    return identityEntity;

  }

  /*
   * Private
   */

  private Identity createIdentityFromEntity(final IdentityEntity identityEntity) {

    //
    return getStorage().findIdentityById(identityEntity.getId());
    
  }

  private void populateProfile(final Profile profile, final ProfileEntity profileEntity) {

    IdentityEntity identity = profileEntity.getIdentity();

    String providerId = identity.getProviderId();
    String remoteId = identity.getRemoteId();

    profile.setId(profileEntity.getId());
    profile.setCreatedTime(profileEntity.getCreatedTime());

    List<Map<String, String>> phones = new ArrayList<Map<String,String>>();
    List<Map<String, String>> ims = new ArrayList<Map<String,String>>();
    List<Map<String, String>> urls = new ArrayList<Map<String,String>>();

    //
    for (String name : profileEntity.getProperties().keySet()) {
      if (isJcrProperty(name)) {
        switch(PropNs.nsOf(name)) {
          case VOID:
          case INDEX:
            profile.setProperty(PropNs.cleanPrefix(name), profileEntity.getProperty(name).get(0));
            break;
          case PHONE:
            fillProfileParam(profileEntity, phones, name);
            break;
          case IM:
            fillProfileParam(profileEntity, ims, name);
            break;
          case URL:
            fillProfileParam(profileEntity, urls, name);
            break;
        }
      }
    }


    if (OrganizationIdentityProvider.NAME.equals(providerId) || SpaceIdentityProvider.NAME.equals(providerId)) {

      //
      if (OrganizationIdentityProvider.NAME.equals(providerId)) {
        profile.setUrl(LinkProvider.getUserProfileUri(remoteId));
      } else if (SpaceIdentityProvider.NAME.equals(providerId)) {
        spaceStorage = getSpaceStorage();
        if (spaceStorage.getSpaceByPrettyName(remoteId) != null) {
          profile.setUrl(LinkProvider.getSpaceUri(remoteId));
        }
      }
      
      //
      NTFile avatar = profileEntity.getAvatar();
      if (avatar != null) {
        try {
          String avatarPath = getSession().getPath(avatar);
          long lastModified = avatar.getLastModified().getTime();
          // workaround: as dot character (.) breaks generated url (Ref: SOC-2283)
          String avatarUrl = StorageUtils.encodeUrl(avatarPath) + "/?upd=" + lastModified;
          profile.setAvatarUrl(LinkProvider.escapeJCRSpecialCharacters(avatarUrl));
        } catch (Exception e) {
          LOG.warn("Failed to build file url from fileResource: " + e.getMessage());
        }
      }

    }
    // External profile
    else {
      profile.setUrl(profileEntity.getExternalUrl());
      profile.setAvatarUrl(profileEntity.getExternalAvatarUrl());
    }

    //
    if (phones.size() > 0) {
      profile.setProperty(Profile.CONTACT_PHONES, phones);
    }
    if (ims.size() > 0) {
      profile.setProperty(Profile.CONTACT_IMS, ims);
    }
    if (urls.size() > 0) {
      profile.setProperty(Profile.CONTACT_URLS, urls);
    }

    //
    List<Map<String, Object>> xpData = new ArrayList<Map<String, Object>>();
    for (ProfileXpEntity xpEntity : profileEntity.getXps().values()){
      Map<String, Object> xpMap = new HashMap<String, Object>();
      xpMap.put(Profile.EXPERIENCES_SKILLS, xpEntity.getSkills());
      xpMap.put(Profile.EXPERIENCES_POSITION, xpEntity.getPosition());
      xpMap.put(Profile.EXPERIENCES_START_DATE, xpEntity.getStartDate());
      xpMap.put(Profile.EXPERIENCES_END_DATE, xpEntity.getEndDate());
      xpMap.put(Profile.EXPERIENCES_COMPANY, xpEntity.getCompany());
      xpMap.put(Profile.EXPERIENCES_DESCRIPTION, xpEntity.getDescription());
      xpMap.put(Profile.EXPERIENCES_IS_CURRENT, xpEntity.isCurrent());
      xpData.add(xpMap);
    }

    profile.setProperty(Profile.EXPERIENCES, xpData);
  }

  /*
   * Public
   */

  /**
   * {@inheritDoc}
   */
  public void saveIdentity(final Identity identity) throws IdentityStorageException {

    try {
      try {
        _findById(IdentityEntity.class, identity.getId());
        _saveIdentity(identity);
      }
      catch (NodeNotFoundException e) {
        _createIdentity(identity);
        _saveIdentity(identity);
      }
    }
    catch (NodeAlreadyExistsException e1) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_SAVE_IDENTITY, e1.getMessage(), e1);
    }
    catch (NodeNotFoundException e1) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_SAVE_IDENTITY, e1.getMessage(), e1);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Identity updateIdentity(final Identity identity) throws IdentityStorageException {

    //
    saveIdentity(identity);

    //
    return findIdentityById(identity.getId());

  }
  
  /**
   * {@inheritDoc}
   */
  public void updateIdentityMembership(final String remoteId) throws IdentityStorageException {
    //only clear Identity Caching when user updated Group, what raised by Organization Service
  }

  public void hardDeleteIdentity(final Identity identity) throws IdentityStorageException {
    try {
      _hardDeleteIdentity(identity);
    }
    catch (NodeNotFoundException e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_DELETE_IDENTITY, e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Identity findIdentityById(final String nodeId) throws IdentityStorageException {

    try {

      //
      IdentityEntity identityEntity = _findById(IdentityEntity.class, nodeId);
      Identity identity = new Identity(nodeId);
      identity.setDeleted(identityEntity.isDeleted());
      identity.setRemoteId(identityEntity.getRemoteId());
      identity.setProviderId(identityEntity.getProviderId());

      //
      return identity;
    }
    catch (NodeNotFoundException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteIdentity(final Identity identity) throws IdentityStorageException {
    try {
      _deleteIdentity(identity);
    }
    catch (NodeNotFoundException e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_DELETE_IDENTITY, e.getMessage(), e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Profile loadProfile(Profile profile) throws IdentityStorageException {
    try {
      profile = _loadProfile(profile);
    }
    catch (NodeNotFoundException e) {
      try {
        profile = _createProfile(profile);
      }
      catch (NodeNotFoundException e1) {
        throw new IdentityStorageException(
            IdentityStorageException.Type.FAIL_TO_FIND_IDENTITY_BY_NODE_ID,
            e1.getMessage(), e1);
      }
    }

    profile.clearHasChanged();

    return profile;
  }

  /**
   * {@inheritDoc}
   */
  public Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException {
    try {
      return _findIdentity(providerId, remoteId);
    }
    catch (NodeNotFoundException e) {
      return null;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveProfile(final Profile profile) throws IdentityStorageException {

    try {
      if (profile.getId() == null) {
        _createProfile(profile);
      }
      else {
        _saveProfile(profile);
      }
    }
    catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e); // should never be thrown
    }
    profile.clearHasChanged();
  }

  /**
   * {@inheritDoc}
   */
  public void updateProfile(final Profile profile) throws IdentityStorageException {
    saveProfile(profile);
  }

  /**
   * {@inheritDoc}
   */
  public int getIdentitiesCount (final String providerId) throws IdentityStorageException {

    // TODO : use jcr property to improve the perfs
    ProviderEntity providerEntity = getProviderRoot().getProviders().get(providerId);
    int nb = providerEntity.getIdentities().size();

    //
    return nb;
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(
      final String providerId, final ProfileFilter profileFilter, long offset, long limit,
      boolean forceLoadOrReloadProfile)
      throws IdentityStorageException {

    if (offset < 0) {
      offset = 0;
    }

    String inputName = profileFilter.getName().replace(StorageUtils.ASTERISK_STR, StorageUtils.PERCENT_STR);
    StorageUtils.processUsernameSearchPattern(inputName.trim());
    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();
    List<Identity> listIdentity = new ArrayList<Identity>();

    QueryBuilder<ProfileEntity> builder = getSession().createQueryBuilder(ProfileEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    whereExpression
        .like(JCRProperties.path, getProviderRoot().getProviders().get(
                                                    providerId).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR)
        .and()
        .not().equals(ProfileEntity.deleted, "true");

    StorageUtils.applyExcludes(whereExpression, excludedIdentityList);
    StorageUtils.applyFilter(whereExpression, profileFilter);

    builder.where(whereExpression.toString());
    applyOrder(builder, profileFilter);


    QueryImpl<ProfileEntity> queryImpl = (QueryImpl<ProfileEntity>) builder.get();
    ((org.exoplatform.services.jcr.impl.core.query.QueryImpl) queryImpl.getNativeQuery()).setCaseInsensitiveOrder(true);
    
    QueryResult<ProfileEntity> results = queryImpl.objects(offset, limit);
    while (results.hasNext()) {

      ProfileEntity profileEntity = results.next();

      Identity identity = createIdentityFromEntity(profileEntity.getIdentity());
      Profile profile = getStorage().loadProfile(new Profile(identity));
      identity.setProfile(profile);
      listIdentity.add(identity);

    }

    return listIdentity;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesForMentions(
      final String providerId, final ProfileFilter profileFilter, long offset, long limit,
      boolean forceLoadOrReloadProfile)
      throws IdentityStorageException {

    if (offset < 0) {
      offset = 0;
    }

    String inputName = profileFilter.getName().replace(StorageUtils.ASTERISK_STR, StorageUtils.PERCENT_STR);
    StorageUtils.processUsernameSearchPattern(inputName);
    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();

    QueryBuilder<ProfileEntity> builder = getSession().createQueryBuilder(ProfileEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    whereExpression
        .like(JCRProperties.path, getProviderRoot().getProviders().get(
                                                    providerId).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR)
        .and()
        .not().equals(ProfileEntity.deleted, "true");

    StorageUtils.applyExcludes(whereExpression, excludedIdentityList);
    StorageUtils.applyFilter(whereExpression, profileFilter);

    builder.where(whereExpression.toString());
    applyOrder(builder, profileFilter);

    QueryImpl<ProfileEntity> queryImpl = (QueryImpl<ProfileEntity>) builder.get();
    ((org.exoplatform.services.jcr.impl.core.query.QueryImpl) queryImpl.getNativeQuery()).setCaseInsensitiveOrder(true);
    
    QueryResult<ProfileEntity> results = queryImpl.objects();

    //QueryResult<ProfileEntity> results = builder.get().objects();
    
    //
    IdentityResult identityResult = new IdentityResult(offset, limit, results.size());
    
    String remoteId = null;
    //
    while (results.hasNext()) {

      ProfileEntity profileEntity = results.next();

      remoteId = profileEntity.getIdentity().getRemoteId();
      
      if (StorageUtils.isUserActivated(remoteId) == false) continue;
      
      Identity identity = createIdentityFromEntity(profileEntity.getIdentity());
      
      Profile profile = getStorage().loadProfile(new Profile(identity));
      identity.setProfile(profile);
      
      identityResult.add(identity);
      
      //
      if (identityResult.addMore() == false) {
        break;
      }
      

    }

    return identityResult.result();
  }
  
  /**
   * {@inheritDoc}
   */
  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();

    QueryBuilder<ProfileEntity> builder = getSession().createQueryBuilder(ProfileEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    whereExpression
        .like(JCRProperties.path, getProviderRoot().getProviders().get(
                                                    providerId).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR)
        .and()
        .not().equals(ProfileEntity.deleted, "true");

    StorageUtils.applyExcludes(whereExpression, excludedIdentityList);
    StorageUtils.applyFilter(whereExpression, profileFilter);

    builder.where(whereExpression.toString());

    QueryResult<ProfileEntity> results = builder.get().objects();
    
    //
    IdentityResult identityResult = new IdentityResult(results.size());
    
    String remoteId = null;
    //
    while (results.hasNext()) {

      ProfileEntity profileEntity = results.next();

      remoteId = profileEntity.getIdentity().getRemoteId();
      
      if (StorageUtils.isUserActivated(remoteId) == false) continue;
      
      Identity identity = createIdentityFromEntity(profileEntity.getIdentity());
      
      identityResult.add(identity);
    }

    return identityResult.result().size();

  }

  
  
  /**
   * {@inheritDoc}
   */
  public int getIdentitiesByFirstCharacterOfNameCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException {

    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();

    QueryBuilder<ProfileEntity> builder = getSession().createQueryBuilder(ProfileEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    whereExpression
        .like(JCRProperties.path, getProviderRoot().getProviders().get(
                                                    providerId).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR)
        .and().not().equals(ProfileEntity.deleted, "true");

    StorageUtils.applyExcludes(whereExpression, excludedIdentityList);
    StorageUtils.applyFilter(whereExpression, profileFilter);

    builder.where(whereExpression.toString());
    
    QueryResult<ProfileEntity> results = builder.get().objects();
    
    //
    IdentityResult identityResult = new IdentityResult(results.size());
    
    String remoteId = null;
    //
    while (results.hasNext()) {

      ProfileEntity profileEntity = results.next();

      remoteId = profileEntity.getIdentity().getRemoteId();
      
      if (StorageUtils.isUserActivated(remoteId) == false) continue;
      
      Identity identity = createIdentityFromEntity(profileEntity.getIdentity());
      
      identityResult.add(identity);
    }

    return identityResult.result().size();
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByFirstCharacterOfName(final String providerId, final ProfileFilter profileFilter,
      long offset, long limit, boolean forceLoadOrReloadProfile) throws IdentityStorageException {

    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();

    //
    QueryBuilder<ProfileEntity> builder = getSession().createQueryBuilder(ProfileEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    whereExpression
        .like(JCRProperties.path, getProviderRoot().getProviders().get(
                                                    providerId).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR)
        .and()
        .not().equals(ProfileEntity.deleted, "true");

    StorageUtils.applyExcludes(whereExpression, excludedIdentityList);
    StorageUtils.applyFilter(whereExpression, profileFilter);

    builder.where(whereExpression.toString());
    applyOrder(builder, profileFilter);

    QueryImpl<ProfileEntity> queryImpl = (QueryImpl<ProfileEntity>) builder.get();
    ((org.exoplatform.services.jcr.impl.core.query.QueryImpl) queryImpl.getNativeQuery()).setCaseInsensitiveOrder(true);
    
    QueryResult<ProfileEntity> results = queryImpl.objects();
    
    //QueryResult<ProfileEntity> results = builder.get().objects();
    
    //
    IdentityResult identityResult = new IdentityResult(offset, limit, results.size());
    
    String remoteId = null;
    //
    while (results.hasNext()) {

      ProfileEntity profileEntity = results.next();
      
      remoteId = profileEntity.getIdentity().getRemoteId();
      
      if (StorageUtils.isUserActivated(remoteId) == false) continue;
      
      Identity identity = createIdentityFromEntity(profileEntity.getIdentity());
      
      Profile profile = getStorage().loadProfile(new Profile(identity));
      identity.setProfile(profile);
      
      identityResult.add(identity);
      
      //
      if (identityResult.addMore() == false) {
        break;
      }
      

    }

    return identityResult.result();

  }
  
  

  /**
   * {@inheritDoc}
   */
  public String getType(final String nodetype, final String property) {

    // TODO : move to appropriate classe

    Session jcrSession = getSession().getJCRSession();
    try {

      NodeTypeManager ntManager = jcrSession.getWorkspace().getNodeTypeManager();
      NodeType nt = ntManager.getNodeType(nodetype);
      PropertyDefinition[] pDefs = nt.getDeclaredPropertyDefinitions();

      for (PropertyDefinition pDef : pDefs) {
        if (pDef.getName().equals(property)) {
          return PropertyType.nameFromValue(pDef.getRequiredType());
        }
      }

    }
    catch (RepositoryException e) {
      return null;
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException {
    getStorage().updateProfile(profile);
  }

  /**
   * {@inheritDoc}
   */
  public void setStorage(IdentityStorage storage) {
    this.identityStorage = storage;
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getSpaceMemberIdentitiesByProfileFilter(
      Space space,
      ProfileFilter profileFilter,
      Type type,
      long offset, long limit) throws IdentityStorageException {


    List<Identity> listIdentity = new ArrayList<Identity>();
    QueryResult<ProfileEntity> results = getSpaceMemberIdentitiesByProfileFilterQueryBuilder(space, profileFilter, type, offset,
                                                                                             limit, false);

    while (results.hasNext()) {
      ProfileEntity profileEntity = results.next();
      Identity identity = createIdentityFromEntity(profileEntity.getIdentity());
      Profile profile = getStorage().loadProfile(new Profile(identity));
      identity.setProfile(profile);
      listIdentity.add(identity);
    }

    return listIdentity;
  }

  /**
   * {@inheritDoc}
   */
  public int getSpaceMemberIdentitiesByProfileFilterCount(
      Space space,
      ProfileFilter profileFilter,
      Type type,
      long offset, long limit) throws IdentityStorageException {
    
    return getSpaceMemberIdentitiesByProfileFilterQueryBuilder(space, profileFilter, type, offset, limit, true).size();

  }

  /**
   * {@inheritDoc}
   */
  public void updateProfileActivityId(Identity identity, String activityId, AttachedActivityType type) {
    try {
      ProfileEntity profileEntity = _findById(ProfileEntity.class, identity.getProfile().getId());
      ActivityProfileEntity activityPEntity = profileEntity.getActivityProfile();
      type.setActivityId(activityPEntity, activityId);
      profileEntity.setActivityProfile(activityPEntity);
      getSession().save();
    } catch (NodeNotFoundException e) {
      LOG.debug(e.getMessage(), e);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public String getProfileActivityId(Profile profile, AttachedActivityType type) {
    try {
      ProfileEntity profileEntity = _findById(ProfileEntity.class, profile.getId());
      ActivityProfileEntity activityPEntity = profileEntity.getActivityProfile();
      return type.getActivityId(activityPEntity);
    } catch (Exception e) {
      return null;
    }
  }
}

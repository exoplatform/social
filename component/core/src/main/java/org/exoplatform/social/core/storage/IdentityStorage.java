/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.storage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;

import org.apache.commons.lang.Validate;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.impl.core.value.BooleanValue;
import org.exoplatform.services.jcr.impl.core.value.DoubleValue;
import org.exoplatform.services.jcr.impl.core.value.LongValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.NodeProperties;
import org.exoplatform.social.common.jcr.NodeTypes;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.service.ProfileConfig;

/** The Class JCRStorage for identity and profile. */
public class IdentityStorage {
  private static final String PROFILE_AVATAR = "exo:avatar";
  private static final String ASTERISK_STR = "*";
  private static final String PERCENT_STR = "%";
  private static final char   ASTERISK_CHAR = '*';
  private static final String SPACE_STR = " ";
  private static final String EMPTY_STR = "";
  
  /** The config. */
  private ProfileConfig config = null;
  //new change
  /** The data location. */
  private final SocialDataLocation dataLocation;

  /** The session manager. */
  private final JCRSessionManager sessionManager;

  /**
   * The identity cache with key as identityId or uuid of that identity node.
   */
  private ExoCache<String, Identity> identityCache;
  
  /**
   * Instantiates a new jCR storage.
   *
   * @param dataLocation the data location
   * @param cacheService the cache service
   */
  public IdentityStorage(final SocialDataLocation dataLocation, CacheService cacheService) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
    this.identityCache = cacheService.getCacheInstance("exo.social.IdentityStorageIdentityCache");
  }

  /**
   * Saves identity.
   *
   * @param identity the identity
   * @throws IdentityStorageException 
   */
  public final void saveIdentity(final Identity identity) throws IdentityStorageException {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node identityNode;
      Node identityHomeNode = getIdentityServiceHome(session);

      if (identity.getId() == null) {
        identityNode = identityHomeNode.addNode(NodeTypes.EXO_IDENTITY, NodeTypes.EXO_IDENTITY);
        identityNode.addMixin(NodeTypes.MIX_REFERENCEABLE);
      } else {
        identityNode = session.getNodeByUUID(identity.getId());
      }
      identityNode.setProperty(NodeProperties.IDENTITY_REMOTEID, identity.getRemoteId());
      identityNode.setProperty(NodeProperties.IDENTITY_PROVIDERID, identity.getProviderId());
      identityNode.setProperty(NodeProperties.IDENTITY_IS_DELETED, identity.isDeleted());
      
      if (identity.getId() == null) {
        identityHomeNode.save();
        identity.setId(identityNode.getUUID());
      } else {
        identityNode.save();
      }
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_SAVE_IDENTITY, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Updates existing identity's properties.
   *
   * @param identity the identity to be updated.
   * @return the updated identity.
   * @throws IdentityStorageException
   * @since  1.2.0-GA
   */
  public Identity updateIdentity(Identity identity) throws IdentityStorageException {
    Session session = sessionManager.getOrOpenSession();
    String nodeUUID = identity.getId();
    try {
      Node identityNode = session.getNodeByUUID(nodeUUID);
      identityNode.setProperty(NodeProperties.IDENTITY_REMOTEID, identity.getRemoteId());
      identityNode.setProperty(NodeProperties.IDENTITY_PROVIDERID, identity.getProviderId());
      identityNode.setProperty(NodeProperties.IDENTITY_IS_DELETED, identity.isDeleted());      
      identityNode.save();
      if (identityCache.get(nodeUUID) != null) {
        identityCache.remove(nodeUUID);
      }
      identityCache.put(nodeUUID, identity);
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_UPDATE_IDENTITY, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
    return identity;
  }
  
  /**
   * Deletes an identity from JCR
   *
   * @param identity
   * @throws IdentityStorageException
   */
  public final void deleteIdentity(final Identity identity) throws IdentityStorageException {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node identityNode = session.getNodeByUUID(identity.getId());
      Profile profile = identity.getProfile();
      if ((profile != null) && (profile.getId() != null)) {
        deleteProfile(profile);
      }
      identityNode.remove();
      session.save();
      if (identityCache.get(identity.getId()) != null) {
        identityCache.remove(identity.getId());
      }
      //LOG.info("Identity: [" + identity.toString() + "] deleted.");
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_DELETE_IDENTITY, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets the identity by his id.
   *
   * @param nodeId the id of identity
   * @return the identity
   * @throws IdentityStorageException
   */
  public final Identity findIdentityById(final String nodeId) throws IdentityStorageException {
    Session session = sessionManager.getOrOpenSession();
    Identity identity = null;
    Node identityNode = null;

    identity = identityCache.get(nodeId);
    
    if (identity != null) {
      return identity;
    }
    
    try {
      identityNode = session.getNodeByUUID(nodeId);
      if (identityNode != null) {
        identity = getIdentity(identityNode);
      }
    } catch (ItemNotFoundException itemNotFoundExp) {
      return null;
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_FIND_IDENTITY_BY_NODE_ID, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
    return identity;
  }

  /**
   * Gets the identity by remote id.
   *
   * @param providerId the identity provider
   * @param remoteId   the id
   * @return the identity by remote id
   * @throws IdentityStorageException
   */
  public final Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException {
    Session session = sessionManager.getOrOpenSession();
    Node identityHomeNode = getIdentityServiceHome(session);
    Identity identity = null;
    try {
      List<Node> nodes = new QueryBuilder(session)
              .select(NodeTypes.EXO_IDENTITY)
              .like(NodeProperties.JCR_PATH, identityHomeNode.getPath() + "/" + PERCENT_STR)
              .and()
              .equal(NodeProperties.IDENTITY_PROVIDERID, providerId)
              .and()
              .equal(NodeProperties.IDENTITY_REMOTEID, remoteId)
              .exec();

      if (nodes.size() == 1) {
        Node identityNode = nodes.get(0);
        identity = getIdentity(identityNode);
      }
    } catch (ItemNotFoundException itemNotFoundExp) {
      return null;
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_FIND_IDENTITY, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }

    return identity;
  }

  /**
   * Counts the number of identities that match the first character of name.
   * 
   * @param providerId
   * @param profileFilter Profile filter object.
   * @return Number of identities that start with the first character of name.
   * @throws IdentityStorageException
   * @since 1.2.0-GA
   */
  public int getIdentitiesByFirstCharacterOfNameCount(String providerId, ProfileFilter profileFilter) throws IdentityStorageException {
    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();
    try {
      Session session = sessionManager.getOrOpenSession();
      Node profileHomeNode = getOrCreatExoProfileHomeNode(session, providerId);

      QueryBuilder queryBuilder = new QueryBuilder(session);
      queryBuilder.select(NodeTypes.EXO_PROFILE)
        .like(NodeProperties.JCR_PATH, profileHomeNode.getPath() + "/" + PERCENT_STR);
      
      if (excludedIdentityList != null & excludedIdentityList.size() > 0) {
        for (Identity identity : excludedIdentityList) {
          queryBuilder.and().not().equal(NodeProperties.PROFILE_IDENTITY, identity.getId());
        }
      }
      
      queryBuilder.and().like(queryBuilder.lower(Profile.FIRST_NAME), Character.toString(profileFilter.getFirstCharacterOfName()).toLowerCase() + PERCENT_STR);
      
      return (int)queryBuilder.count();
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_IDENTITY_BY_FIRSTCHAR_COUNT, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets the identities that match the first character of name.
   * 
   * @param providerId Id of provider.
   * @param profileFilter Profile filter object.
   * @param offset   Start index of list to be get.
   * @param limit    End index of list to be get.
   * @param forceLoadOrReloadProfile Load profile or not.
   * @return Identities that have name start with the first character.
   * @throws IdentityStorageException
   * @since 1.2.0-GA
   */
  public List<Identity> getIdentitiesByFirstCharacterOfName(String providerId, ProfileFilter profileFilter, int offset, int limit, boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();
    List<Identity> listIdentity = new ArrayList<Identity>();
    List<Node> nodes = null;

    try {
      Session session = sessionManager.getOrOpenSession();
      Node profileHomeNode = getOrCreatExoProfileHomeNode(session, providerId);

      QueryBuilder queryBuilder = new QueryBuilder(session);
      queryBuilder.select(NodeTypes.EXO_PROFILE, offset, limit)
        .like(NodeProperties.JCR_PATH, profileHomeNode.getPath() + "/" + PERCENT_STR);
      if (excludedIdentityList != null & excludedIdentityList.size() > 0) {
        for (Identity identity : excludedIdentityList) {
          queryBuilder.and().not().equal(NodeProperties.PROFILE_IDENTITY, identity.getId());
        }
      }
      queryBuilder.and().like(queryBuilder.lower(Profile.FIRST_NAME), Character.toString(profileFilter.getFirstCharacterOfName()).toLowerCase() + PERCENT_STR)
        .orderBy(Profile.FIRST_NAME, QueryBuilder.ASC);
      
      nodes = queryBuilder.exec();
      
      for (Node profileNode : nodes) {
        Node identityNode = profileNode.getProperty(NodeProperties.PROFILE_IDENTITY).getNode();
        Identity identity = getIdentity(identityNode);
        if (forceLoadOrReloadProfile) {
          loadProfile(identity.getProfile());
        }
        listIdentity.add(identity);
      }
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_IDENTITY_BY_FIRSTCHAR, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }

    return listIdentity;
  }
  
  /**
   * Counts the number of identity by profile filter.
   * 
   * @param providerId Id of Provider.
   * @param profileFilter Information of profile are used in filtering.
   * @return Number of identities that are filtered by profile.
   * @throws IdentityStorageException
   * @since 1.2.0-GA
   */
  public int getIdentitiesByProfileFilterCount(String providerId, ProfileFilter profileFilter) throws IdentityStorageException {
    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();
    String inputName = profileFilter.getName().replace(ASTERISK_STR, PERCENT_STR);
    processUsernameSearchPattern(inputName.trim());
    String position = addPositionSearchPattern(profileFilter.getPosition().trim()).replace(ASTERISK_STR, PERCENT_STR);
    String gender = profileFilter.getGender().trim();
    inputName = inputName.isEmpty() ? ASTERISK_STR : inputName;
    String nameForSearch = inputName.replace(ASTERISK_STR, SPACE_STR);
    try {
      Session session = sessionManager.getOrOpenSession();
      Node profileHomeNode = getOrCreatExoProfileHomeNode(session, providerId);

      QueryBuilder queryBuilder = new QueryBuilder(session);
      queryBuilder
              .select(NodeTypes.EXO_PROFILE)
              .like(NodeProperties.JCR_PATH, profileHomeNode.getPath() + "/" + PERCENT_STR);

      if (excludedIdentityList != null & excludedIdentityList.size() > 0) {
        for (Identity identity : excludedIdentityList) {
          queryBuilder.and().not().equal(NodeProperties.PROFILE_IDENTITY, identity.getId());
        }
      }

      if (nameForSearch.trim().length() != 0) {
        queryBuilder.and().like(queryBuilder.lower(Profile.FULL_NAME), PERCENT_STR + nameForSearch.toLowerCase() + PERCENT_STR);
      }
      if (position.length() != 0) {
        queryBuilder.and().like(queryBuilder.lower(Profile.POSITION), PERCENT_STR + position.toLowerCase() + PERCENT_STR);
      }
      if (gender.length() != 0) {
        queryBuilder.and().equal(Profile.GENDER, gender);
      }
      
      return (int) queryBuilder.count();
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_IDENTITY_BY_PROFILE_FILTER_COUNT, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets the identities by profile filter.
   *
   * @param identityProvider Id of provider.
   * @param profileFilter    Information of profile that used in filtering.
   * @param offset           Start index of list to be get.
   * @param limit            End index of list to be get.
   * @param forceLoadOrReloadProfile Load profile or not.
   * @return the identities by profile filter.
   * @throws IdentityStorageException
   * @since 1.2.0-GA
   */
  public final List<Identity> getIdentitiesByProfileFilter(final String identityProvider, final ProfileFilter profileFilter,
                                                           long offset, long limit, boolean forceLoadOrReloadProfile) throws IdentityStorageException {
    String inputName = profileFilter.getName().replace(ASTERISK_STR, PERCENT_STR);
    processUsernameSearchPattern(inputName.trim());
    List<Identity> excludedIdentityList = profileFilter.getExcludedIdentityList();
    String position = addPositionSearchPattern(profileFilter.getPosition().trim()).replace(ASTERISK_STR, PERCENT_STR);
    String gender = profileFilter.getGender().trim();
    inputName = inputName.isEmpty() ? ASTERISK_STR : inputName;
    String nameForSearch = inputName.replace(ASTERISK_STR, SPACE_STR);
    List<Identity> listIdentity = new ArrayList<Identity>();
    List<Node> nodes = null;
    try {
      Session session = sessionManager.getOrOpenSession();
      Node profileHomeNode = getOrCreatExoProfileHomeNode(session, identityProvider);

      QueryBuilder queryBuilder = new QueryBuilder(session);
      queryBuilder
              .select(NodeTypes.EXO_PROFILE, offset, limit)
              .like(NodeProperties.JCR_PATH, profileHomeNode.getPath() + "/" + PERCENT_STR);

      if (excludedIdentityList != null & excludedIdentityList.size() > 0) {
        for (Identity identity : excludedIdentityList) {
          queryBuilder.and().not().equal(NodeProperties.PROFILE_IDENTITY, identity.getId());
        }
      }

      if (nameForSearch.trim().length() != 0) {
        queryBuilder.and().like(queryBuilder.lower(Profile.FULL_NAME), PERCENT_STR + nameForSearch.toLowerCase() + PERCENT_STR);
      }
      
      if (position.length() != 0) {
        queryBuilder.and().like(queryBuilder.lower(Profile.POSITION), PERCENT_STR + position.toLowerCase() + PERCENT_STR);
      }
      if (gender.length() != 0) {
        queryBuilder.and().equal(Profile.GENDER, gender);
      }

      queryBuilder.orderBy(Profile.FULL_NAME, QueryBuilder.ASC);
      
      nodes = queryBuilder.exec();
  
      for (Node profileNode : nodes) {
        Node identityNode = profileNode.getProperty(NodeProperties.PROFILE_IDENTITY).getNode();
        Identity identity = getIdentity(identityNode);
        if (forceLoadOrReloadProfile) {
          loadProfile(identity.getProfile());
        }
        listIdentity.add(identity);
      }
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_IDENTITY_BY_PROFILE_FILTER, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }

    return listIdentity;
  }
  
  /**
   * Gets the identities by profile filter.
   *
   * @param identityProvider the identity provider
   * @param profileFilter    the profile filter
   * @param offset           the result offset
   * @param limit            the result limit
   * @return the identities by profile filter
   * @throws IdentityStorageException
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, ProfileFilter, int, int, boolean)} instead.
   *             Will be removed by 1.3.x
   */
  public final List<Identity> getIdentitiesByProfileFilter(final String identityProvider, final ProfileFilter profileFilter,
                                                           long offset, long limit) throws IdentityStorageException {
    return getIdentitiesByProfileFilter(identityProvider, profileFilter, (int)offset, (int)limit, false);
  }

  /**
   * Gets the identities filter by alpha bet.
   *
   * @param identityProvider the identity provider
   * @param profileFilter    the profile filter
   * @param offset
   * @param limit
   * @return the identities filter by alpha bet
   * @throws IdentityStorageException
   * @deprecated Use {@link #getIdentitiesByFirstCharacterOfName(String, ProfileFilter, int, int, boolean)} instead.
   *             Will be removed by 1.3.x
   */
  public final List<Identity> getIdentitiesFilterByAlphaBet(final String identityProvider, final ProfileFilter profileFilter,
                                                            long offset, long limit) throws IdentityStorageException {
    return getIdentitiesByFirstCharacterOfName(identityProvider, profileFilter, (int)offset, (int)limit, false);
  }

  /**
   * Save profile.
   * 
   * @param profile the profile
   * @throws IdentityStorageException
   */
  public final void saveProfile(final Profile profile) throws IdentityStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      
      if (profile.getIdentity().getId() == null) {
        throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_SAVE_PROFILE, "the identity has to be saved before saving the profile");
      }

      Identity identity = profile.getIdentity();
      Node profileHomeNode = getOrCreatExoProfileHomeNode(session, identity.getProviderId());
      Node profileNode;
      synchronized (profile) {
        if (profile.getId() == null) {
          profileNode = profileHomeNode.addNode(NodeTypes.EXO_PROFILE, NodeTypes.EXO_PROFILE);
          profileNode.addMixin(NodeTypes.MIX_REFERENCEABLE);

          Node identityNode = session.getNodeByUUID(profile.getIdentity().getId());
          profileNode.setProperty(NodeProperties.PROFILE_IDENTITY, identityNode);
          profileNode.setProperty(NodeProperties.JCR_LAST_MODIFIED, Calendar.getInstance());
          profileHomeNode.save();
        } else {
          profileNode = session.getNodeByUUID(profile.getId());
        }
        saveProfile(profile, profileNode, session);

        if (profile.getId() == null) {
          // create a new profile...
          profileHomeNode.save();
          profile.setId(profileNode.getUUID());
        } else {
          profileNode.save();
        }
      }
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_SAVE_PROFILE, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Add or modify properties of profile and persist to JCR. Profile parameter is a lightweight that 
   * contains only the property that you want to add or modify. NOTE: The method will
   * not delete the properties on old profile when the param profile have not those keys.
   * 
   * @param profile
   * @throws IdentityStorageException
   */
  public final void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException {
    Validate.notNull(profile.getId(), "profile.getId() must be not null.");
    try {
      Session session = sessionManager.getOrOpenSession();
   
      Node profileNode = session.getNodeByUUID(profile.getId());
      addOrModifyProfileProperties(profile, profileNode, session);

      profileNode.save();
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_ADD_OR_MODIFY_PROPERTIES, e.getMessage());      
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets total number of identities in storage depend on providerId.
   * @throws IdentityStorageException 
   */
  public int getIdentitiesCount (String providerId) throws IdentityStorageException {
    Session session = sessionManager.getOrOpenSession();
    int count = 0;
    Node identityHomeNode = getIdentityServiceHome(session);
    try {
      count = (int) new QueryBuilder(session).select(NodeTypes.EXO_IDENTITY)
      .like(NodeProperties.JCR_PATH, identityHomeNode.getPath()+"/" + PERCENT_STR)
      .and()
      .equal(NodeProperties.IDENTITY_PROVIDERID, providerId).count();
    } catch (Exception e){
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_IDENTITIES_COUNT, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Load profile.
   *
   * @param profile the profile
   * @throws IdentityStorageException
   */
  public final void loadProfile(final Profile profile) throws IdentityStorageException {
    if (profile.getIdentity().getId() == null) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_LOAD_PROFILE, "Failed to load profile. The identity must to be saved before loading profile");
    }

    Node identityNode;
    String workspaceName;

    Session session = sessionManager.getOrOpenSession();
    try {
      identityNode = session.getNodeByUUID(profile.getIdentity().getId());
      workspaceName = session.getWorkspace().getName();
      PropertyIterator references = identityNode.getReferences();
      if (references.getSize() == 0) {
        //there is no profile node referencing to this identity node -> create new profile node
        //Lazily initializing a new Profile...
        saveProfile(profile);
      } else {
        //profile node for this identity was created then load profile from that node
        while (references.hasNext()) {
          Property nodeReferencedProperty = (Property) references.next();
          if (nodeReferencedProperty.getParent().isNodeType(NodeTypes.EXO_PROFILE)) {
            Node profileNode = nodeReferencedProperty.getParent();
            profile.setId(profileNode.getUUID());
            loadProfile(profile, profileNode, workspaceName);
          }
        }
      }
    } catch (ItemNotFoundException e) {
      return;
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_LOAD_PROFILE, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets the type.
   *
   * @param nodetype the nodetype
   * @param property the property
   * @return the type
   * @throws IdentityStorageException
   */
  public final String getType(final String nodetype, final String property) {
    try {
      Session session = sessionManager.openSession();

      NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
      NodeType nt = ntManager.getNodeType(nodetype);
      PropertyDefinition[] pDefs = nt.getDeclaredPropertyDefinitions();

      for (PropertyDefinition pDef : pDefs) {
        if (pDef.getName().equals(property)) {
          return PropertyType.nameFromValue(pDef.getRequiredType());
        }
      }
    } catch (Exception e) {
      return null;
    } finally {
      sessionManager.closeSession();
    }
    return null;
  }
  

  /**
   * Deletes a profile
   *
   * @param profile
   * @throws IdentityStorageException
   * @since 1.1.1
   */
  protected final void deleteProfile(final Profile profile) throws IdentityStorageException {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node profileNode = session.getNodeByUUID(profile.getId());
      profileNode.remove();
      session.save();
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_DELETE_PROFILE, e.getMessage());
    } finally {
      sessionManager.closeSession();
    }

  }
  
  /**
   * Save profile.
   * 
   * @param profile the profile
   * @param profileNode the node
   * @param session the session
   * @throws IdentityStorageException
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected final void saveProfile(final Profile profile, final Node profileNode, final Session session) throws Exception,
                                                                                IOException {
    synchronized (profile) {

      long lastLoaded = profile.getLastLoaded();
      long lastPersisted = 0;
      if (profileNode.hasProperty(NodeProperties.JCR_LAST_MODIFIED)) {
        lastPersisted = profileNode.getProperty(NodeProperties.JCR_LAST_MODIFIED).getLong();
      }

      if (!profile.hasChanged() && lastPersisted > 0 && lastPersisted <= lastLoaded) {
        return;
      }
      profile.clearHasChanged();
      Calendar date = Calendar.getInstance();
      profileNode.setProperty(NodeProperties.JCR_LAST_MODIFIED, date);
      profile.setLastLoaded(date.getTimeInMillis());

      Profile oldProfile = new Profile(null);
      loadProfile(oldProfile, profileNode, session.getWorkspace().getName());

      // We remove all the property that was deleted
      for (String key : oldProfile.getProperties().keySet()) {
        if(!profile.contains(key))
        {
          if (profileNode.hasProperty(key)) {
            profileNode.getProperty(key).remove();
          } else if (profileNode.hasNode(key)) {
            profileNode.getNode(key).remove();
          }
        }
      }

      addOrModifyProfileProperties(profile, profileNode, session);
    }
  }
  
  /**
   * Add or modify properties of profile and persist to JCR. Profile parameter is a lightweight that 
   * contains only the property that you want to add or modify. NOTE: The method will
   * not delete the properties on old profile when the param profile have not those keys.
   *
   * @param profile
   * @param profileNode
   * @param session
   * @throws IdentityStorageException
   */
  protected final void addOrModifyProfileProperties(final Profile profile, final Node profileNode,
                                                    final Session session) throws IdentityStorageException {
    Map<String, Object> props = profile.getProperties();

    Iterator<Map.Entry<String, Object>> it = props.entrySet().iterator();
    try {
      while (it.hasNext()) {
        Map.Entry<String, Object> entry = it.next();
        String key = entry.getKey();
        //we skip all the property that are jcr related
        if (key.contains(":")) {
          continue;
        }
        setProperty(profileNode, session, key, entry.getValue());
      }
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_ADD_OR_MODIFY_PROPERTIES, e.getMessage());
    }
  }
  
  /**
   * Checks if is forced multi value.
   *
   * @param key the key
   * @return true, if is forced multi value
   */
  protected final boolean isForcedMultiValue(final String key) {
    return getConfig().isForcedMultiValue(key);
  }

  /**
   * Gets the node type name.
   *
   * @param nodeName the node name
   * @return the node type name
   */
  protected final String getNodeTypeName(final String nodeName) {
    return getConfig().getNodeType(nodeName);
  }

  /**
   * Loads profile.
   *
   * @param profile       the p
   * @param profileNode   the n
   * @param workspaceName the workspace name
   * @throws RepositoryException the repository exception
   * @throws IdentityStorageException
   */
  @SuppressWarnings("unchecked")
  protected final void loadProfile(final Profile profile, final Node profileNode,
                                   final String workspaceName) throws RepositoryException, IdentityStorageException {
    synchronized (profile) {
      long lastLoaded = profile.getLastLoaded();
      long lastPersisted = 0;
      if (profileNode.hasProperty(NodeProperties.JCR_LAST_MODIFIED)) {
        lastPersisted = profileNode.getProperty(NodeProperties.JCR_LAST_MODIFIED).getLong();
      } else {
        // Lazy add the property
        profileNode.setProperty(NodeProperties.JCR_LAST_MODIFIED, Calendar.getInstance());
        profileNode.save();
      }
      if (lastPersisted > 0 && lastPersisted <= lastLoaded) {
        return;
      }
      // Get the previous value of the flag has changed
      boolean hasChanged = profile.hasChanged();
      Calendar date = Calendar.getInstance();
      profile.setLastLoaded(date.getTimeInMillis());

      // Load profile properties from node properties
      PropertyIterator props = profileNode.getProperties();
      copyPropertiesToMap(props, profile.getProperties());

      // Load profile properties from node child nodes
      NodeIterator it = profileNode.getNodes();
      // TODO: Make better store for better load
      // Remove profile properties first (because have some properties like urls have 2 nodes defined)
      while (it.hasNext()) {
        Node node = it.nextNode();
        String nodeName = node.getName();
        while(profile.contains(nodeName)) {
          profile.removeProperty(nodeName);
        }
      }
      // Then load new properties again
      it = profileNode.getNodes();
      while(it.hasNext()) {
        Node node = it.nextNode();
        String nodeName = node.getName();
        if (nodeName.equals(PROFILE_AVATAR) || nodeName.startsWith(PROFILE_AVATAR + ImageUtils.KEY_SEPARATOR)) {
          if (node.isNodeType(NodeTypes.NT_FILE)) {
            AvatarAttachment file = new AvatarAttachment();
            file.setId(node.getPath());
            file.setMimeType(node.getNode(NodeProperties.JCR_CONTENT).getProperty(NodeProperties.JCR_MIME_TYPE).getString());
            try {
              file.setInputStream(node.getNode(NodeProperties.JCR_CONTENT).getProperty(NodeProperties.JCR_DATA).getValue().getStream());
            } catch (Exception e) {
              throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_LOAD_AVATAR, e.getMessage());
            }
            file.setLastModified(node.getNode(NodeProperties.JCR_CONTENT).getProperty(NodeProperties.JCR_LAST_MODIFIED).getLong());
            file.setFileName(nodeName);
            file.setWorkspace(workspaceName);
            profile.setProperty(nodeName, file);
          }
        } else {
          List<Map<String, Object>> mapPropertyList = (List<Map<String, Object>>) profile.getProperty(nodeName);
          if(mapPropertyList == null) {
            mapPropertyList = new ArrayList<Map<String, Object>>();
          }
          mapPropertyList.add(copyPropertiesToMap(node.getProperties(), null));
          profile.setProperty(nodeName, mapPropertyList);
        }
      }
      if (!hasChanged) {
        // The profile has not been modified before loading it so we
        // can safely clear the hasChanged flag
        profile.clearHasChanged();
      }
    }
  }

  /**
   * Gets the identity service home which is cached and lazy-loaded.
   *
   * @param session the session
   * @return the identity service home
   * @throws IdentityStorageException
   */
  private Node getIdentityServiceHome(final Session session) throws IdentityStorageException {
    Node identityServiceHome =null;

    String path = dataLocation.getSocialIdentityHome();
    try {
      identityServiceHome = session.getRootNode().getNode(path);
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_IDENTITY_SERVICE_HOME, e.getMessage());
    }
    
    return identityServiceHome;
  }

  /**
   * Gets the profile config.
   *
   * @return the config
   */
  private ProfileConfig getConfig() {
    if (config == null) {
      PortalContainer container = PortalContainer.getInstance();
      config = (ProfileConfig) container.getComponentInstanceOfType(ProfileConfig.class);
    }
    return config;
  }

  /**
   * Gets the profile service home which is cached and lazy-loaded.
   *
   * @param session the session
   * @return the profile service home
   * @throws IdentityStorageException
   */
  private Node getProfileServiceHome(final Session session) throws IdentityStorageException {
    Node profileServiceHome = null;
    try {
      String path = dataLocation.getSocialProfileHome();
      profileServiceHome = session.getRootNode().getNode(path);
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_PROFILE_SERVICE_HOME, e.getMessage());
    }
    return profileServiceHome;
  }

  /**
   * |
   * |--|Social_Profile
   * |            |
   * |            |_ providerId
   * |                   |
   * |                   |_exo:profile
   * |
   * 
   * @param session
   * @param providerId
   * @return Home node of profile.
   * @throws IdentityStorageException
   */
  private Node getOrCreatExoProfileHomeNode(Session session, String providerId) throws IdentityStorageException {
    
    try {
      // Gets or creates the node Path: Social_Profile.
      Node profileHomeNode = getProfileServiceHome(session);
      Node typeProfileHome;
      // Path: Social_Profile/providerId.
      if (profileHomeNode.hasNode(providerId)){
        return profileHomeNode.getNode(providerId);
      } else {
        typeProfileHome = profileHomeNode.addNode(providerId, NodeTypes.NT_UNSTRUCTURED);
        profileHomeNode.save();
        return typeProfileHome;
      }
      
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_GET_OR_CREAT_PROFILE_HOME_NODE, e.getMessage());
    } 
  }
  
  private String addPositionSearchPattern(final String position) {
    if (position.length() != 0) {
      if (position.indexOf("*") == -1) {
        return "*" + position + "*";
      }
      return position;
    }
    return "";
  }

  private String processUsernameSearchPattern(String userName) {
    if (userName.length() > 0) {
      userName = ((EMPTY_STR.equals(userName)) || (userName.length() == 0)) ? ASTERISK_STR : userName;
      userName = (userName.charAt(0) != ASTERISK_CHAR) ? ASTERISK_STR + userName : userName;
      userName = (userName.charAt(userName.length() - 1) != ASTERISK_CHAR) ? userName += ASTERISK_STR : userName;
      userName = (userName.indexOf(ASTERISK_STR) >= 0) ? userName.replace(ASTERISK_STR, "." + ASTERISK_STR) : userName; 
      userName = (userName.indexOf(PERCENT_STR) >= 0) ? userName.replace(PERCENT_STR, "." + ASTERISK_STR) : userName;
      Pattern.compile(userName);
    }
    return userName;
  }
  
  /**
   * Copies properties to map.
   *
   * @param props the props
   * @param map   the map
   * @return the map
   * @throws RepositoryException the repository exception
   */
  private Map<String, Object> copyPropertiesToMap(final PropertyIterator props,
                                                  Map<String, Object> map) throws RepositoryException {
    if (map == null) {
      map = new HashMap<String, Object>();
    }
    while (props.hasNext()) {
      Property prop = (Property) props.next();

      // we skip all the property that are jcr related
      String name = prop.getName();
      if (name.contains(":")) {
        continue;
      }

      try {
        Value value = prop.getValue();
        if (value instanceof StringValue) {
          map.put(name, value.getString());
        } else if (value instanceof LongValue) {
          map.put(name, value.getLong());
        } else if (value instanceof DoubleValue) {
          map.put(name, value.getDouble());
        } else if (value instanceof BooleanValue) {
          map.put(name, value.getBoolean());
        }
      } catch (ValueFormatException e) {
        Value[] values = prop.getValues();
        List<String> res = new ArrayList<String>();

        for (Value value : values) {
          res.add(value.getString());
        }
        map.put(name, res.toArray(new String[res.size()]));
      }
    }
    return map;
  }

  /**
   * Sets property for profile node from profile properties by name and value
   * 
   * @param profileNode
   * @param session
   * @param name
   * @param value
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void setProperty(final Node profileNode, final Session session,
                           final String name, Object value) throws Exception {
    if (isForcedMultiValue(name)) {
      // if it's a String, we convert it to string array to be able to store it
      if (value instanceof String) {
        value = new String[] { (String) value };
      }
      setProperty(name, (String[]) value, profileNode);
    } else if (value instanceof String) {
      profileNode.setProperty(name, (String) value);
    } else if (value instanceof Double) {
      profileNode.setProperty(name, (Double) value);
    } else if (value instanceof Boolean) {
      profileNode.setProperty(name, (Boolean) value);
    } else if (value instanceof Long) {
      profileNode.setProperty(name, (Long) value);
    } else if (value instanceof String[]) {
      final String[] strings = (String[]) value;
      if (strings.length == 1) {
        profileNode.setProperty(name, strings[0]);
      } else {
        setProperty(name, strings, profileNode);
      }
    } else if (value instanceof List<?>) {
      setProperty(name, (List<Map<String, Object>>) value, profileNode, session);
    } else if (value instanceof AvatarAttachment) {
      // fix id6 load
      saveAvatarAttachment(profileNode, session, name, (AvatarAttachment) value);
    }
  }

  /**
   * Saves avatar attachment, new JCR file node for avatar
   * 
   * @param profileNode
   * @param session
   * @param name
   * @param profileAtt
   * @throws Exception
   */
  private void saveAvatarAttachment(final Node profileNode,
                                    final Session session,
                                    final String name,
                                    final AvatarAttachment profileAtt) throws Exception {
    ExtendedNode extNode = (ExtendedNode) profileNode;
    if (extNode.canAddMixin(NodeTypes.EXO_PRIVILEGEABLE)) {
      extNode.addMixin(NodeTypes.EXO_PRIVILEGEABLE);
    }

    String[] arrayPers = { PermissionType.READ, PermissionType.ADD_NODE,
        PermissionType.SET_PROPERTY, PermissionType.REMOVE };

    extNode.setPermission(SystemIdentity.ANY, arrayPers);

    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries();
    for (AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers);
    }
    if (profileAtt.getFileName() != null) {
      Node nodeFile;
      try {
        nodeFile = profileNode.getNode(name);
      } catch (PathNotFoundException ex) {
        nodeFile = profileNode.addNode(name, NodeTypes.NT_FILE);
      }

      Node nodeContent;
      try {
        nodeContent = nodeFile.getNode(NodeProperties.JCR_CONTENT);
      } catch (PathNotFoundException ex) {
        nodeContent = nodeFile.addNode(NodeProperties.JCR_CONTENT, "nt:resource");
      }

      long lastModified = profileAtt.getLastModified();
      long lastSaveTime = 0;
      if (nodeContent.hasProperty(NodeProperties.JCR_LAST_MODIFIED)) {
        lastSaveTime = nodeContent.getProperty(NodeProperties.JCR_LAST_MODIFIED).getLong();
      }
      if ((lastModified != 0) && (lastModified != lastSaveTime)) {
        nodeContent.setProperty(NodeProperties.JCR_MIME_TYPE, profileAtt.getMimeType());
        nodeContent.setProperty(NodeProperties.JCR_DATA, profileAtt.getInputStream(session));
        nodeContent.setProperty(NodeProperties.JCR_LAST_MODIFIED, profileAtt.getLastModified());
      }
    } else {
      if (profileNode.hasNode(name)) {
        profileNode.getNode(name).remove();
        profileNode.save();
      }
    }
  }

  /**
   * Sets the property.
   * Sets the List<Map<String,Object>> property.
   *
   * @param name  the name
   * @param props the props
   * @param n     the node
   * @param session     the session
   * @throws Exception                    the exception
   * @throws ConstraintViolationException the constraint violation exception
   * @throws VersionException             the version exception
   */
  private void setProperty(final String name, final List<Map<String,Object>> props,
                           final Node n, final Session session) throws IdentityStorageException {
    String ntName = getNodeTypeName(name);
    try {
      if (ntName == null) {
        throw new Exception("no nodeType is defined for " + name);
      }
  
      // remove the existing nodes
      NodeIterator nIt = n.getNodes(name);
      while (nIt.hasNext()) {
        Node currNode = nIt.nextNode();
        currNode.remove();
      }
      n.save();
  
      Iterator<Map<String, Object>> it = props.iterator();
      while (it.hasNext()) {
        Map<String, Object> prop = it.next();
        Node propNode = n.addNode(name, ntName);
  
        Iterator<Map.Entry<String, Object>> iterator = prop.entrySet().iterator();
        while (iterator.hasNext()) {
          Map.Entry<String, Object> entry = iterator.next();
          String key = entry.getKey();
          Object propValue = entry.getValue();
  
          if (propValue instanceof String) {
            propNode.setProperty(key, (String) propValue);
          } else if (propValue instanceof Double) {
            propNode.setProperty(key, (Double) propValue);
          } else if (propValue instanceof Boolean) {
            propNode.setProperty(key, (Boolean) propValue);
          } else if (propValue instanceof Long) {
            propNode.setProperty(key, (Long) propValue);
          } else {
            
          }
        }
      }
    } catch (Exception e) {
      throw new IdentityStorageException(IdentityStorageException.Type.FAIL_TO_SET_PROPERTIES, e.getMessage());
    }
  }

  /**
   * Sets the property.
   *
   * @param name      the name
   * @param propValue the prop value
   * @param n         the node
   * @throws IOException                  Signals that an I/O exception has
   *                                      occurred.
   * @throws RepositoryException          the repository exception
   * @throws ConstraintViolationException the constraint violation exception
   * @throws VersionException             the version exception
   */
  private void setProperty(final String name, final String[] propValue,
                           final Node n) throws IOException, RepositoryException {
    ArrayList<Value> values = new ArrayList<Value>();
    for (String value : propValue) {
      if (value != null && value.length() > 0) {
        values.add(new StringValue(value));
      }
    }
    n.setProperty(name, values.toArray(new Value[values.size()]));
  }
  
  /**
   * Gets the identity.
   *
   * @param identityNode the identity node
   * @return the identity
   * @throws Exception the exception
   */
  private final Identity getIdentity(final Node identityNode) throws Exception {
    String nodeUUID = identityNode.getUUID();
    Identity identity = identityCache.get(nodeUUID);
    
    if (identity != null) {
      return identity;
    }
    
    identity = new Identity(nodeUUID);
    identity.setProviderId(identityNode.getProperty(NodeProperties.IDENTITY_PROVIDERID).getString());
    identity.setRemoteId(identityNode.getProperty(NodeProperties.IDENTITY_REMOTEID).getString());
    identity.setDeleted(identityNode.getProperty(NodeProperties.IDENTITY_IS_DELETED).getBoolean());

    Profile profile = new Profile(identity);
    loadProfile(profile);
    identity.setProfile(profile);

    identityCache.put(nodeUUID, identity);
    return identity;
  }
}
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
  private static final Log LOG = ExoLogger.getExoLogger(IdentityStorage.class);

  /** The Constant IDENTITY_NODETYPE. */
  public static final String IDENTITY_NODETYPE = "exo:identity".intern();

  /** The Constant PROFILE_NODETYPE. */
  public static final String PROFILE_NODETYPE = "exo:profile".intern();

  /** The Constant IDENTITY_REMOTEID. */
  public static final String IDENTITY_REMOTEID = "exo:remoteId".intern();

  /** The Constant IDENTITY_PROVIDERID. */
  public static final String IDENTITY_PROVIDERID = "exo:providerId".intern();

  /** The Constant PROFILE_IDENTITY. */
  public static final String PROFILE_IDENTITY = "exo:identity".intern();

  /** The Constant PROFILE_AVATAR. */
  public static final String PROFILE_AVATAR = "avatar".intern();

  /** The Constant JCR_UUID. */
  public static final String JCR_UUID = "jcr:uuid".intern();

  public static final String REFERENCEABLE_NODE = "mix:referenceable";

  /** The config. */
  private ProfileConfig config = null;
  //new change
  /** The data location. */
  private final SocialDataLocation dataLocation;

  /** The session manager. */
  private final JCRSessionManager sessionManager;

  private Node identityServiceHome;

  private Node profileServiceHome;

  private IdentityManager identityManager;
  
  /**
   * Instantiates a new jCR storage.
   *
   * @param dataLocation the data location
   */
  public IdentityStorage(final SocialDataLocation dataLocation) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
  }

  /**
   * Gets the identity service home which is cached and lazy-loaded.
   *
   * @param session the session
   * @return the identity service home
   * @throws Exception the exception
   */
  private Node getIdentityServiceHome(final Session session) {
    if (identityServiceHome == null) {
      String path = dataLocation.getSocialIdentityHome();
      try {
        identityServiceHome = session.getRootNode().getNode(path);
      } catch (PathNotFoundException e) {
        LOG.warn(e.getMessage(), e);
      } catch (RepositoryException e) {
        LOG.warn(e.getMessage(), e);
      }
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
   * @throws Exception the exception
   */
  private Node getProfileServiceHome(final Session session) throws Exception {
    if (profileServiceHome == null) {
      String path = dataLocation.getSocialProfileHome();
      profileServiceHome = session.getRootNode().getNode(path);
    }
    return profileServiceHome;
  }

  /**
   * Save identity.
   *
   * @param identity the identity
   */
  public final void saveIdentity(final Identity identity) {
    Identity checkingIdentity = null;
    checkingIdentity = findIdentity(identity.getProviderId(), identity.getRemoteId());

    Session session = sessionManager.getOrOpenSession();
    try {
      Node identityNode;
      Node identityHomeNode = getIdentityServiceHome(session);

      if (identity.getId() == null) {
        if (checkingIdentity == null) {
          identityNode = identityHomeNode.addNode(IDENTITY_NODETYPE, IDENTITY_NODETYPE);
          identityNode.addMixin(REFERENCEABLE_NODE);
        } else {
          identityNode = session.getNodeByUUID(checkingIdentity.getId());
        }
      } else {
        identityNode = session.getNodeByUUID(identity.getId());
      }
      identityNode.setProperty(IDENTITY_REMOTEID, identity.getRemoteId());
      identityNode.setProperty(IDENTITY_PROVIDERID, identity.getProviderId());

      if (identity.getId() == null) {
        identityHomeNode.save();
        identity.setId(identityNode.getUUID());
      } else {
        identityNode.save();
      }
    } catch (Exception e) {
      LOG.error("failed to save identity " + identity, e);
    } finally {
      sessionManager.closeSession();
    }
  }


  /**
   * Deletes an identity from JCR
   *
   * @param identity
   */
  public final void deleteIdentity(final Identity identity) {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node identityNode = session.getNodeByUUID(identity.getId());
      if (identity.getProfile().getId() != null) {
        deleteProfile(identity.getProfile());
      }
      identityNode.remove();
      session.save();
      //LOG.info("Identity: [" + identity.toString() + "] deleted.");
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Deletes a profile
   *
   * @param profile
   * @since 1.1.1
   */
  public final void deleteProfile(final Profile profile) {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node profileNode = session.getNodeByUUID(profile.getId());
      profileNode.remove();
      session.save();
    } catch (ItemNotFoundException e) {
      LOG.warn(e.getMessage(), e);
    } catch (RepositoryException e) {
      LOG.warn(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }

  }

  /**
   * Gets the identity by his id.
   *
   * @param nodeId the id of identity
   * @return the identity
   */
  public final Identity findIdentityById(final String nodeId) {
    Session session = sessionManager.getOrOpenSession();
    Identity identity = null;
    Node identityNode = null;
    try {
      identityNode = session.getNodeByUUID(nodeId);
      if (identityNode != null) {
        identity = getIdentity(identityNode);
      }
    } catch (ItemNotFoundException e) {
      LOG.warn("can not find identity with nodeId: " + nodeId);
    } catch (RepositoryException e) {
      LOG.warn("failed from repository", e);
    } catch (Exception e) {
      LOG.warn("failed getIdentity by identityNode:" + identityNode, e);
    } finally {
      sessionManager.closeSession();
    }
    return identity;
  }

  /**
   * Gets the all identity.
   *
   * @return the all identities
   */
  public final List<Identity> getAllIdentities() {
    List<Identity> identities = new ArrayList<Identity>();
    try {
      Session session = sessionManager.openSession();
      Node identityHomeNode = getIdentityServiceHome(session);
      NodeIterator iter = identityHomeNode.getNodes();
      Identity identity;
      while (iter.hasNext()) {
        Node identityNode = iter.nextNode();
        identity = getIdentity(identityNode);
        identities.add(identity);
      }
      return identities;
    } catch (Exception e) {
      LOG.error("Error while loading identities", e);
      return null;
    } finally {
      sessionManager.closeSession();
    }
  }


  /**
   * Gets the identity by remote id.
   *
   * @param providerId the identity provider
   * @param remoteId   the id
   * @return the identity by remote id
   */
  public final Identity findIdentity(final String providerId, final String remoteId) {
    Session session = sessionManager.getOrOpenSession();
    Node identityHomeNode = getIdentityServiceHome(session);
    Identity identity = null;
    try {
      List<Node> nodes = new QueryBuilder(session)
              .select(IDENTITY_NODETYPE)
              .like("jcr:path", identityHomeNode.getPath() + "/%")
              .and()
              .equal(IDENTITY_PROVIDERID, providerId)
              .and()
              .equal(IDENTITY_REMOTEID, remoteId)
              .exec();

      if (nodes.size() == 1) {
        Node identityNode = nodes.get(0);
        identity = getIdentity(identityNode);
      }
    } catch (Exception e) {
      LOG.warn("failed to load identity by remote id : " + providerId + ":" + remoteId, e);
    } finally {
      sessionManager.closeSession();
    }

    return identity;
  }

  /**
   * Gets the identity.
   *
   * @param identityNode the identity node
   * @return the identity
   * @throws Exception the exception
   */
  public final Identity getIdentity(final Node identityNode) throws Exception {
    Identity identity = new Identity(identityNode.getUUID());

    identity.setProviderId(identityNode.getProperty(IDENTITY_PROVIDERID).getString());
    identity.setRemoteId(identityNode.getProperty(IDENTITY_REMOTEID).getString());

    Profile profile = new Profile(identity);
    loadProfile(profile);
    identity.setProfile(profile);

    return identity;
  }


  /**
   * Gets the identities by profile filter.
   *
   * @param identityProvider the identity provider
   * @param profileFilter    the profile filter
   * @param offset           the result offset
   * @param limit            the result limit
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  public final List<Identity> getIdentitiesByProfileFilter(final String identityProvider, final ProfileFilter profileFilter, long offset, long limit) throws Exception {
    String inputName = profileFilter.getName();
    String userName = processUsernameSearchPattern(inputName.trim());
    String position = addPositionSearchPattern(profileFilter.getPosition().trim());
    String gender = profileFilter.getGender().trim();
    inputName = ((inputName == "") || (inputName.length() == 0)) ? "*" : inputName;
    String nameForSearch = inputName.replace("*", " ");
    String [] nameParts = nameForSearch.trim().split(" ");
    List<Identity> listIdentity = new ArrayList<Identity>();
    List<Node> nodes = null;

    try {
      Session session = sessionManager.getOrOpenSession();
      Node profileHomeNode = getProfileServiceHome(session);

      QueryBuilder queryBuilder = new QueryBuilder(session)
              .select(PROFILE_NODETYPE, offset, limit)
              .like("jcr:path", profileHomeNode.getPath() + "[%]/" + PROFILE_NODETYPE + "[%]");

      for (String namePart : nameParts) {
        if (namePart != "") {
          queryBuilder.or().like(queryBuilder.lower(Profile.FIRST_NAME), "%" + namePart.toLowerCase() + "%");
          queryBuilder.or().like(queryBuilder.lower(Profile.LAST_NAME),  "%" + namePart.toLowerCase() + "%");
        }
      }

      if (position.length() != 0) {
        queryBuilder.and().contains(Profile.POSITION, position);
      }
      if (gender.length() != 0) {
        queryBuilder.and().equal(Profile.GENDER, gender);
      }

      queryBuilder.orderBy(Profile.FIRST_NAME, QueryBuilder.ASC);
      
      nodes = queryBuilder.exec();
  
      for (Node profileNode : nodes) {
        Node identityNode = profileNode.getProperty(PROFILE_IDENTITY).getNode();
        Identity identity = getIdentityManager().getIdentity(identityNode.getUUID(), false);
        if (!identity.getProviderId().equals(identityProvider)) {
          continue;
        }
        if (userName.length() != 0) {
          String fullUserName = identity.getProfile().getFullName();
          String fullNameLC = fullUserName.toLowerCase();
          String userNameLC = userName.toLowerCase();
          if ((userNameLC.length() != 0) && fullNameLC.matches(userNameLC)) {
            listIdentity.add(identity);
          }
        } else {
          listIdentity.add(identity);
        }
      }
    } catch (Exception e) {
      LOG.warn("error while filtering identities: " + e.getMessage());
      return (new ArrayList<Identity>());
    } finally {
      sessionManager.closeSession();
    }

    return listIdentity;
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
      userName = (("".equals(userName)) || (userName.length() == 0)) ? "*" : userName;
      userName = (userName.charAt(0) != '*') ? "*" + userName : userName;
      userName = (userName.charAt(userName.length() - 1) != '*') ? userName += "*" : userName;
      userName = (userName.indexOf("*") >= 0) ? userName.replace("*", ".*") : userName;
      userName = (userName.indexOf("%") >= 0) ? userName.replace("%", ".*") : userName;
      Pattern.compile(userName);
    }
    return userName;
  }

  /**
   * Gets the identities filter by alpha bet.
   *
   * @param identityProvider the identity provider
   * @param profileFilter    the profile filter
   * @param offset
   * @param limit
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   */
  public final List<Identity> getIdentitiesFilterByAlphaBet(final String identityProvider, final ProfileFilter profileFilter, long offset, long limit) throws Exception {
    List<Identity> listIdentity = new ArrayList<Identity>();
    List<Node> nodes = null;

    try {
      Session session = sessionManager.getOrOpenSession();
      Node profileHomeNode = getProfileServiceHome(session);

      QueryBuilder queryBuilder = new QueryBuilder(session);
      queryBuilder
              .select(PROFILE_NODETYPE, offset, limit)
              .like("jcr:path", profileHomeNode.getPath() + "/%");

      String userName = profileFilter.getName();
      
      if (userName.length() != 0) {
        userName += "*";
        queryBuilder.and().contains(Profile.FIRST_NAME, userName);
      }

      queryBuilder.orderBy(Profile.FIRST_NAME, QueryBuilder.ASC);
      
      nodes = queryBuilder.exec();
      
      for (Node profileNode : nodes) {
        Node identityNode = profileNode.getProperty(PROFILE_IDENTITY).getNode();
        Identity identity = getIdentityManager().getIdentity(identityNode.getUUID(), false);
        if (!identity.getProviderId().equals(identityProvider)) {
          continue;
        }
        listIdentity.add(identity);
      }
    } catch (Exception e) {
      LOG.warn("Failed to filter identities by alphabet" + e.getMessage());
      return null;
    } finally {
      sessionManager.closeSession();
    }

    return listIdentity;
  }

  /**
   * Save profile.
   * 
   * @param profile the profile
   * @throws Exception the exception
   */
  public final void saveProfile(final Profile profile) {
    try {
      Session session = sessionManager.getOrOpenSession();
      Node profileHomeNode = getProfileServiceHome(session);
      if (profile.getIdentity().getId() == null) {
        LOG.warn("the identity has to be saved before saving the profile");
        return;
      }

      Node profileNode;
      synchronized (profile) {
        if (profile.getId() == null) {
          profileNode = profileHomeNode.addNode(PROFILE_NODETYPE, PROFILE_NODETYPE);
          profileNode.addMixin(REFERENCEABLE_NODE);

          Node identityNode = session.getNodeByUUID(profile.getIdentity().getId());
          profileNode.setProperty(PROFILE_IDENTITY, identityNode);
          profileNode.setProperty("jcr:lastModified", Calendar.getInstance());
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
      LOG.error("Failed to save profile " + profile, e);
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
   * @throws Exception the exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected final void saveProfile(final Profile profile, final Node profileNode, final Session session) throws Exception,
                                                                                IOException {
    synchronized (profile) {

      long lastLoaded = profile.getLastLoaded();
      long lastPersisted = 0;
      if (profileNode.hasProperty("jcr:lastModified")) {
        lastPersisted = profileNode.getProperty("jcr:lastModified").getLong();
      }

      if (!profile.hasChanged() && lastPersisted > 0 && lastPersisted <= lastLoaded) {
        return;
      }
      profile.clearHasChanged();
      Calendar date = Calendar.getInstance();
      profileNode.setProperty("jcr:lastModified", date);
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
   * @throws Exception
   */
  public final void addOrModifyProfileProperties(final Profile profile) throws Exception {
    Validate.notNull(profile.getId(), "profile.getId() must be not null.");
    try {
      Session session = sessionManager.getOrOpenSession();

      Node profileNode = session.getNodeByUUID(profile.getId());
      addOrModifyProfileProperties(profile, profileNode, session);

      profileNode.save();
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
   * @param profileNode
   * @param session
   * @throws Exception
   */
  protected final void addOrModifyProfileProperties(final Profile profile, final Node profileNode, final Session session) throws Exception {
    Map<String, Object> props = profile.getProperties();

    Iterator<Map.Entry<String, Object>> it = props.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Object> entry = it.next();
      String key = entry.getKey();
      //we skip all the property that are jcr related
      if (key.contains(":")) {
        continue;
      }
      setProperty(profileNode, session, key, entry.getValue());
    }
  }

  /**
   * Gets total number of identities in storage depend on providerId. 
   */
  public int getIdentitiesCount (String providerId) {
    Session session = sessionManager.getOrOpenSession();
    int count = 0;
    Node identityHomeNode = getIdentityServiceHome(session);
    try {
      count = (int) new QueryBuilder(session).select(IDENTITY_NODETYPE)
      .like("jcr:path", identityHomeNode.getPath()+"/%")
      .and()
      .equal(IDENTITY_PROVIDERID, providerId).count();
    } catch (Exception e){
      LOG.warn(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * The method set property for profile node from profile properties by name and value
   * 
   * @param profileNode
   * @param session
   * @param name
   * @param value
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void setProperty(final Node profileNode, final Session session, final String name, Object value) throws Exception {
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
   * Save avatar attachment, new JCR file node for avatar
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
    if (extNode.canAddMixin("exo:privilegeable")) {
      extNode.addMixin("exo:privilegeable");
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
        nodeFile = profileNode.addNode(name, "nt:file");
      }

      Node nodeContent;
      try {
        nodeContent = nodeFile.getNode("jcr:content");
      } catch (PathNotFoundException ex) {
        nodeContent = nodeFile.addNode("jcr:content", "nt:resource");
      }

      long lastModified = profileAtt.getLastModified();
      long lastSaveTime = 0;
      if (nodeContent.hasProperty("jcr:lastModified")) {
        lastSaveTime = nodeContent.getProperty("jcr:lastModified").getLong();
      }
      if ((lastModified != 0) && (lastModified != lastSaveTime)) {
        nodeContent.setProperty("jcr:mimeType", profileAtt.getMimeType());
        nodeContent.setProperty("jcr:data", profileAtt.getInputStream(session));
        nodeContent.setProperty("jcr:lastModified", profileAtt.getLastModified());
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
  private void setProperty(final String name, final List<Map<String,Object>> props, final Node n, final Session session) throws Exception {
    String ntName = getNodeTypeName(name);
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
          LOG.warn("Type of property does not support!" + propValue);
        }
      }
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
  private void setProperty(final String name, final String[] propValue, final Node n) throws IOException, RepositoryException, ConstraintViolationException, VersionException {
    ArrayList<Value> values = new ArrayList<Value>();
    for (String value : propValue) {
      if (value != null && value.length() > 0) {
        values.add(new StringValue(value));
      }
    }
    n.setProperty(name, values.toArray(new Value[values.size()]));
  }

  /**
   * Load profile.
   *
   * @param profile the profile
   * @throws Exception the exception
   */
  public final void loadProfile(final Profile profile) {
    if (profile.getIdentity().getId() == null) {
      LOG.warn("Failed to load profile. The identity has to be saved before loading the profile");
      return;
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
          if (nodeReferencedProperty.getParent().isNodeType(PROFILE_NODETYPE)) {
            Node profileNode = nodeReferencedProperty.getParent();
            profile.setId(profileNode.getUUID());
            loadProfile(profile, profileNode, workspaceName);
          }
        }
      }
    } catch (ItemNotFoundException e) {
      LOG.warn(e.getMessage(), e);
    } catch (RepositoryException e) {
      LOG.warn(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
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
   * Load profile.
   *
   * @param profile       the p
   * @param profileNode   the n
   * @param workspaceName the workspace name
   * @throws RepositoryException the repository exception
   */
  @SuppressWarnings("unchecked")
  protected final void loadProfile(final Profile profile, final Node profileNode, final String workspaceName) throws RepositoryException {
    synchronized (profile) {
      long lastLoaded = profile.getLastLoaded();
      long lastPersisted = 0;
      if (profileNode.hasProperty("jcr:lastModified")) {
        lastPersisted = profileNode.getProperty("jcr:lastModified").getLong();
      } else {
        // Lazy add the property
        profileNode.setProperty("jcr:lastModified", Calendar.getInstance());
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
          if (node.isNodeType("nt:file")) {
            AvatarAttachment file = new AvatarAttachment();
            file.setId(node.getPath());
            file.setMimeType(node.getNode("jcr:content").getProperty("jcr:mimeType").getString());
            try {
              file.setInputStream(node.getNode("jcr:content").getProperty("jcr:data").getValue().getStream());
            } catch (Exception e) {
              LOG.warn("Failed to load data for avatar of " + profile + ": " + e.getMessage());
            }
            file.setLastModified(node.getNode("jcr:content").getProperty("jcr:lastModified").getLong());
            file.setFileName(nodeName);
            file.setWorkspace(workspaceName);
            profile.setProperty(nodeName, file);
          }
        } else {
          List<Map<String, Object>> l = (List<Map<String, Object>>) profile.getProperty(nodeName);
          if(l == null) {
            l = new ArrayList<Map<String, Object>>();
          }
          l.add(copyPropertiesToMap(node.getProperties(), null));
          profile.setProperty(nodeName, l);
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
   * Copy properties to map.
   *
   * @param props the props
   * @param map   the map
   * @return the map
   * @throws RepositoryException the repository exception
   */
  private Map<String, Object> copyPropertiesToMap(final PropertyIterator props, Map<String, Object> map) throws RepositoryException {
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
   * Gets identity manager instance.
   * 
   * @return identity manager instance.
   */
  private IdentityManager getIdentityManager() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (identityManager == null) {
      identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    
    return identityManager;
  }
  
  /**
   * Gets the type.
   *
   * @param nodetype the nodetype
   * @param property the property
   * @return the type
   * @throws Exception the exception
   */
  public final String getType(final String nodetype, final String property) throws Exception {
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
      LOG.error("Could not find type of property " + property + " for nodetype " + nodetype);
      return null;
    } finally {
      sessionManager.closeSession();
    }
    return null;
  }
}

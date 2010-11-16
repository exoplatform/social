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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.service.ProfileConfig;

/** The Class JCRStorage for identity and profile. */
public class IdentityStorage {
  private static final Log LOG = ExoLogger.getExoLogger(IdentityStorage.class);

  /** The Constant IDENTITY_NODETYPE. */
  final public static String IDENTITY_NODETYPE = "exo:identity".intern();

  /** The Constant PROFILE_NODETYPE. */
  final public static String PROFILE_NODETYPE = "exo:profile".intern();

  /** The Constant IDENTITY_REMOTEID. */
  final public static String IDENTITY_REMOTEID = "exo:remoteId".intern();

  /** The Constant IDENTITY_PROVIDERID. */
  final public static String IDENTITY_PROVIDERID = "exo:providerId".intern();

  /** The Constant PROFILE_IDENTITY. */
  final public static String PROFILE_IDENTITY = "exo:identity".intern();

  /** The Constant PROFILE_AVATAR. */
  final public static String PROFILE_AVATAR = "avatar".intern();

  /** The Constant JCR_UUID. */
  final public static String JCR_UUID = "jcr:uuid".intern();

  public static final String REFERENCEABLE_NODE = "mix:referenceable";

  /** The config. */
  private ProfileConfig config = null;
  //new change
  /** The data location. */
  private SocialDataLocation dataLocation;

  /** The session manager. */
  private JCRSessionManager sessionManager;







  Comparator<Identity> identityComparator = new Comparator<Identity>() {
    public int compare(Identity o1, Identity o2) {
      String name1 = o1.getProfile().getProperties().get("firstName").toString();
      String name2 = o2.getProfile().getProperties().get("firstName").toString();

      return name1.compareToIgnoreCase(name2);
    }
  };

  private Node identityServiceHome;

  private Node profileServiceHome;

  /**
   * Instantiates a new jCR storage.
   *
   * @param dataLocation the data location
   */
  public IdentityStorage(SocialDataLocation dataLocation) {
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
  private Node getIdentityServiceHome(Session session) {
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
  private Node getProfileServiceHome(Session session) throws Exception {
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
  public void saveIdentity(Identity identity) {
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
  public void deleteIdentity(Identity identity) {
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
  public void deleteProfile(Profile profile) {
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
  public Identity findIdentityById(String nodeId) {
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
  public List<Identity> getAllIdentities() {
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
  public Identity findIdentity(String providerId, String remoteId) {
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
  public Identity getIdentity(Node identityNode) throws Exception {
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
  public List<Identity> getIdentitiesByProfileFilter(String identityProvider, ProfileFilter profileFilter, long offset, long limit) throws Exception {
    Session session = sessionManager.getOrOpenSession();
    Node profileHomeNode = getProfileServiceHome(session);

    List<Identity> listIdentity = new ArrayList<Identity>();
    List<Node> nodes = null;
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session)
              .select(PROFILE_NODETYPE, offset, limit)
              .like("jcr:path", profileHomeNode.getPath() + "[%]/" + PROFILE_NODETYPE + "[%]");

      String position = addPositionSearchPattern(profileFilter.getPosition().trim());
      String gender = profileFilter.getGender().trim();

      if (position.length() != 0) {
        queryBuilder.and().contains("position", position);
      }
      if (gender.length() != 0) {
        queryBuilder.and().equal("gender", gender);
      }

      nodes = queryBuilder.exec();
    } catch (Exception e) {
      LOG.warn("error while filtering identities: " + e.getMessage());
      return (new ArrayList<Identity>());
    } finally {
      sessionManager.closeSession();
    }

    String userName = processUsernameSearchPattern(profileFilter.getName().trim());

    for (Node profileNode : nodes) {
      Node identityNode = profileNode.getProperty(PROFILE_IDENTITY).getNode();
      Identity identity = getIdentity(identityNode);
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

    Collections.sort(listIdentity, identityComparator);

    return listIdentity;
  }

  private String addPositionSearchPattern(String position) {
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
      userName = ((userName == "") || (userName.length() == 0)) ? "*" : userName;
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
  public List<Identity> getIdentitiesFilterByAlphaBet(String identityProvider, ProfileFilter profileFilter, long offset, long limit) throws Exception {
    List<Identity> listIdentity = new ArrayList<Identity>();
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

      final List<Node> nodes = queryBuilder.exec();
      for (Node profileNode : nodes) {
        Node identityNode = profileNode.getProperty(PROFILE_IDENTITY).getNode();
        Identity identity = getIdentity(identityNode);
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
  public void saveProfile(Profile profile) {
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
        } else {
          profileNode = session.getNodeByUUID(profile.getId());
        }
        saveProfile(profile, profileNode, session);

        if (profile.getId() == null) {
          //create a new profile...
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
   * @param profile     the profile
   * @param profileNode the node
   * @param session     the session
   * @throws Exception   the exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void saveProfile(Profile profile, Node profileNode, Session session) throws Exception, IOException {
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

      Map<String, Object> props = profile.getProperties();

//     Iterator<String> it = props.keySet().iterator();

      Profile oldProfile = new Profile(null);
      loadProfile(oldProfile, profileNode, session.getWorkspace().getName());
      Map<String, Object> oldProps = oldProfile.getProperties();

      // We remove all the property that was deleted
      Set<String> removedProps = oldProps.keySet();
      removedProps.removeAll(props.keySet());
      Iterator<String> it = removedProps.iterator();
      while (it.hasNext()) {
        String name = it.next();

        // We skip all the property that are jcr related
        if (name.contains(":")) {
          continue;
        }
        if (!props.containsKey(name)) {
          if (profileNode.hasProperty(name)) {
            profileNode.getProperty(name).remove();
          } else if (profileNode.hasNode(name)) {
            profileNode.getNode(name).remove();
          }
        }
      }

      it = props.keySet().iterator();
      while (it.hasNext()) {
        String name = it.next();

        //we skip all the property that are jcr related
        if (name.contains(":")) {
          continue;
        }

        Object propValue = props.get(name);

        if (isForcedMultiValue(name)) {
          //if it's a String, we convert it to string array to be able to store it
          if (propValue instanceof String) {
            propValue = new String[]{(String) propValue};
          }
          setProperty(name, (String[]) propValue, profileNode);
        } else if (propValue instanceof String) {
          profileNode.setProperty(name, (String) propValue);
        } else if (propValue instanceof Double) {
          profileNode.setProperty(name, (Double) propValue);
        } else if (propValue instanceof Boolean) {
          profileNode.setProperty(name, (Boolean) propValue);
        } else if (propValue instanceof Long) {
          profileNode.setProperty(name, (Long) propValue);
        } else if (propValue instanceof String[]) {
          final String[] strings = (String[]) propValue;
          if (strings.length == 1) {
            profileNode.setProperty(name, strings[0]);
          } else {
            setProperty(name, strings, profileNode);
          }
        } else if (propValue instanceof List) {
          setProperty(name, (List<Map<String, Object>>) propValue, profileNode);
        } else if (propValue instanceof AvatarAttachment) {
          //fix id6 load
          ExtendedNode extNode = (ExtendedNode) profileNode;
          if (extNode.canAddMixin("exo:privilegeable")) {
            extNode.addMixin("exo:privilegeable");
          }

          String[] arrayPers = {
                  PermissionType.READ,
                  PermissionType.ADD_NODE,
                  PermissionType.SET_PROPERTY,
                  PermissionType.REMOVE
          };

          extNode.setPermission(SystemIdentity.ANY, arrayPers);

          List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries();
          for (AccessControlEntry accessControlEntry : permsList) {
            extNode.setPermission(accessControlEntry.getIdentity(), arrayPers);
          }
          AvatarAttachment profileAtt = (AvatarAttachment) propValue;
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
              session.save();
            }
          }
        }
      }
    }
  }

  /**
   * Sets the property.
   *
   * @param name  the name
   * @param props the props
   * @param n     the node
   * @throws Exception                    the exception
   * @throws ConstraintViolationException the constraint violation exception
   * @throws VersionException             the version exception
   */
  private void setProperty(String name, List<Map<String, Object>> props, Node n) throws Exception, VersionException {
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

    Iterator<Map<String, Object>> it = props.iterator();
    while (it.hasNext()) {
      Map<String, Object> prop = it.next();
      Node propNode = n.addNode(name, ntName);

      Iterator<String> itKey = prop.keySet().iterator();
      while (itKey.hasNext()) {
        String key = itKey.next();
        Object propValue = prop.get(key);

        if (propValue instanceof String) {
          propNode.setProperty(key, (String) propValue);
        } else if (propValue instanceof Double) {
          propNode.setProperty(key, (Double) propValue);
        } else if (propValue instanceof Boolean) {
          propNode.setProperty(key, (Boolean) propValue);
        } else if (propValue instanceof Long) {
          propNode.setProperty(key, (Long) propValue);
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
  private void setProperty(String name, String[] propValue, Node n) throws IOException, RepositoryException, ConstraintViolationException, VersionException {
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
  public void loadProfile(Profile profile) {
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
  protected boolean isForcedMultiValue(String key) {
    return getConfig().isForcedMultiValue(key);
  }

  /**
   * Gets the node type name.
   *
   * @param nodeName the node name
   * @return the node type name
   */
  protected String getNodeTypeName(String nodeName) {
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
  protected void loadProfile(Profile profile, Node profileNode, String workspaceName) throws RepositoryException {
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

      PropertyIterator props = profileNode.getProperties();
      copyPropertiesToMap(props, profile.getProperties());

      NodeIterator it = profileNode.getNodes();
      while (it.hasNext()) {
        Node node = it.nextNode();
        if (node.getName().equals(PROFILE_AVATAR) || node.getName().startsWith(PROFILE_AVATAR + "_")) {
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
            file.setFileName(node.getName());
            file.setWorkspace(workspaceName);
            profile.setProperty(node.getName(), file);
          }
        } else {
          List l = (List) profile.getProperty(node.getName());
          if (l == null) {
            profile.setProperty(node.getName(), new ArrayList());
            l = (List) profile.getProperty(node.getName());
            l.add(copyPropertiesToMap(node.getProperties(), new HashMap()));
          }
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
  private Map copyPropertiesToMap(PropertyIterator props, Map map) throws RepositoryException {
    while (props.hasNext()) {
      Property prop = (Property) props.next();

      //we skip all the property that are jcr related
      if (prop.getName().contains(":")) {
        continue;
      }

      try {
        Value v = prop.getValue();
        if (v instanceof StringValue) {
          map.put(prop.getName(), v.getString());
        } else if (v instanceof LongValue) {
          map.put(prop.getName(), v.getLong());
        } else if (v instanceof DoubleValue) {
          map.put(prop.getName(), v.getDouble());
        } else if (v instanceof BooleanValue) {
          map.put(prop.getName(), v.getBoolean());
        }
      }
      catch (ValueFormatException e) {
        Value[] values = prop.getValues();
        List<String> res = new ArrayList<String>();

        for (Value v : values) {
          res.add(v.getString());
        }
        map.put(prop.getName(), res.toArray(new String[res.size()]));
      }
    }
    return map;
  }

  /**
   * Gets the type.
   *
   * @param nodetype the nodetype
   * @param property the property
   * @return the type
   * @throws Exception the exception
   */
  public String getType(String nodetype, String property) throws Exception {
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

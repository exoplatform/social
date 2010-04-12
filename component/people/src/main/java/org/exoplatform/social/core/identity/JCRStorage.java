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
package org.exoplatform.social.core.identity;


import java.io.IOException;
import java.util.ArrayList;
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
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.space.JCRSessionManager;
import org.exoplatform.social.space.impl.SocialDataLocation;


// TODO: Auto-generated Javadoc
/**
 * The Class JCRStorage for identity and profile.
 */
public class JCRStorage {


  /** The Constant IDENTITY_NODETYPE. */
  final private static String IDENTITY_NODETYPE = "exo:identity".intern();
  
  /** The Constant PROFILE_NODETYPE. */
  final private static String PROFILE_NODETYPE = "exo:profile".intern();

  /** The Constant IDENTITY_REMOTEID. */
  final private static String IDENTITY_REMOTEID = "exo:remoteId".intern();
  
  /** The Constant IDENTITY_PROVIDERID. */
  final private static String IDENTITY_PROVIDERID = "exo:providerId".intern();

  /** The Constant PROFILE_IDENTITY. */
  final private static String PROFILE_IDENTITY = "exo:identity".intern();
  
  /** The Constant PROFILE_AVATAR. */
  final private static String PROFILE_AVATAR = "avatar".intern();
  
  /** The Constant JCR_UUID. */
  final private static String JCR_UUID = "jcr:uuid".intern();
  
  /** The config. */
  private ProfileConfig config = null;
  //new change
  /** The data location. */
  private SocialDataLocation dataLocation;
  
  /** The session manager. */
  private JCRSessionManager sessionManager;
  
  private static final Log LOG = ExoLogger.getExoLogger(JCRStorage.class);

  /**
   * Instantiates a new jCR storage.
   * 
   * @param dataLocation the data location
   */
  public JCRStorage(SocialDataLocation dataLocation) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
  }


  /**
   * Gets the identity service home.
   * 
   * @param session the session
   * @return the identity service home
   * @throws Exception the exception
   */
  private Node getIdentityServiceHome(Session session) throws Exception {
    String path = dataLocation.getSocialIdentityHome();
    return session.getRootNode().getNode(path);
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
   * Gets the profile service home.
   * 
   * @param session the session
   * @return the profile service home
   * @throws Exception the exception
   */
  private Node getProfileServiceHome(Session session) throws Exception {
    String path = dataLocation.getSocialProfileHome();
    return session.getRootNode().getNode(path);
  }

  /**
   * Save identity.
   * 
   * @param identity the identity
   */
  public void saveIdentity(Identity identity) {
    Session session =  sessionManager.openSession();
    try {
      Node identityNode;
      Node identityHomeNode = getIdentityServiceHome(session);

      if (identity.getId() == null) {
        identityNode = identityHomeNode.addNode(IDENTITY_NODETYPE, IDENTITY_NODETYPE);
        identityNode.addMixin("mix:referenceable");
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
   * Gets the identity by his id.
   * 
   * @param identityId the id of identity
   * @return the identity
   * @throws Exception the exception
   */
  public Identity findIdentityById(String identityId) throws Exception {
    Session session = sessionManager.openSession();
    Node identityNode = null;
    try {
      identityNode = session.getNodeByUUID(identityId);
      return getIdentity(identityNode);
    } catch (ItemNotFoundException e) {
      LOG.warn("failed to load identity " + identityId);
      return null;
    } finally {
      sessionManager.closeSession();
    }
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
   * @param remoteId the id
   * @return the identity by remote id
   * @throws Exception the exception
   */
  public Identity findIdentity(String providerId, String remoteId) throws Exception {
    Session session = sessionManager.openSession();
    Identity identity = null;
    try {
      Node identityHomeNode = getIdentityServiceHome(session);
  
      StringBuffer queryString = new StringBuffer("/").append(identityHomeNode.getPath())
          .append("/").append(IDENTITY_NODETYPE).append("[(@")
          .append(IDENTITY_PROVIDERID).append("='").append(providerId).append("' and @")
          .append(IDENTITY_REMOTEID).append("='").append(remoteId.replaceAll("'", "''")).append("')]");
      
      QueryManager queryManager = session.getWorkspace().getQueryManager();
      Query query = queryManager.createQuery(queryString.toString(), Query.XPATH);
      QueryResult queryResult = query.execute();
      NodeIterator nodeIterator = queryResult.getNodes();
  
      if (nodeIterator.getSize() == 1) {
        Node identityNode = (Node) nodeIterator.next();
        identity = new Identity(identityNode.getUUID());
        identity.setProviderId(identityNode.getProperty(IDENTITY_PROVIDERID).getString());
        identity.setRemoteId(identityNode.getProperty(IDENTITY_REMOTEID).getString());
      } else {
        LOG.debug("No node found for identity  " + providerId + ":" + remoteId);
      }
    } catch (Exception e) {
      LOG.error("failed to load identity by remote id : "+ providerId +":" + remoteId, e);
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
    Identity identity = null;
    identity = new Identity(identityNode.getUUID());
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
   * @param profileFilter the profile filter
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesByProfileFilter(String identityProvider, ProfileFiler profileFilter) throws Exception {
    Session session = sessionManager.openSession();
    Node profileHomeNode = getProfileServiceHome(session);
    List<Identity> listIdentity = new ArrayList<Identity>();
    NodeIterator nodeIterator = null;
    String userName = null;
    try {
      QueryManager queryManager = session.getWorkspace().getQueryManager() ;
      StringBuffer queryString = new StringBuffer("/").append(profileHomeNode.getPath())
          .append("/").append(PROFILE_NODETYPE);
      userName = profileFilter.getName().trim();
      String position = profileFilter.getPosition().trim();
      String gender = profileFilter.getGender().trim();
      
      if (userName.length() > 0) {
        userName = ((userName == "") || (userName.length() == 0)) ? "*" : userName;
        userName = (userName.charAt(0) != '*') ? "*" + userName : userName;
        userName = (userName.charAt(userName.length()-1) != '*') ? userName += "*" : userName;
        userName = (userName.indexOf("*") >= 0) ? userName.replace("*", ".*") : userName;
        userName = (userName.indexOf("%") >= 0) ? userName.replace("%", ".*") : userName;
        Pattern.compile(userName);
      }
      
      if ((position.length() != 0) || (gender.length() != 0)) {
        queryString.append("[");
      }
  
      if (position.length() != 0) {
        if(position.indexOf("*")<0){
          if(position.charAt(0) != '*') position = "*" + position;
          if(position.charAt(position.length()-1) != '*') position += "*";
        }
        queryString.append("jcr:contains(@position, ").append("'").append(position).append("')");
      }
  
      if (gender.length() != 0) {
        if (position.length() != 0) {
          queryString.append(" and (@gender ").append("= '").append(gender).append("')");
        } else {
          queryString.append(" @gender ").append("= '").append(gender).append("'");
        }
      }
      
      if ((position.length() != 0) || (gender.length() != 0)) {
        queryString.append("]");
      }
      
      Query query1 = queryManager.createQuery(queryString.toString(), Query.XPATH);
      QueryResult queryResult = query1.execute();
      nodeIterator = queryResult.getNodes();
    } catch (Exception e) {
      LOG.warn("error while filtering identities: " + e.getMessage());
      return (new ArrayList<Identity>());
    } finally { 
      sessionManager.closeSession();
    }
    
    Node profileNode = null;
    Node identityNode = null;
    Identity identity = null;
    String fullUserName = null;
    String fullNameLC = null;
    String userNameLC = null;
    
    while (nodeIterator.hasNext()) {
      profileNode = (Node) nodeIterator.next();
      identityNode = profileNode.getProperty(PROFILE_IDENTITY).getNode();
      identity = getIdentity(identityNode);
      if(!identity.getProviderId().equals(identityProvider)) continue;
      if (userName.length() != 0) {
        fullUserName = identity.getProfile().getFullName();
        fullNameLC = fullUserName.toLowerCase();
        userNameLC = userName.toLowerCase();
        if ((userNameLC.length() != 0) && fullNameLC.matches(userNameLC)) {
          listIdentity.add(identity);
        }
      } else {
        listIdentity.add(identity);
      }
    }
    
    return listIdentity;
  }

  /**
   * Gets the identities filter by alpha bet.
   * 
   * @param identityProvider the identity provider
   * @param profileFilter the profile filter
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(String identityProvider, ProfileFiler profileFilter) throws Exception {
    Session session = sessionManager.openSession();
    Node profileHomeNode = getProfileServiceHome(session);
    List<Identity> listIdentity = new ArrayList<Identity>();
    NodeIterator nodeIterator = null;
    try {
      QueryManager queryManager = session.getWorkspace().getQueryManager() ;
      StringBuffer queryString = new StringBuffer("/").append(profileHomeNode.getPath())
          .append("/").append(PROFILE_NODETYPE);
      String userName = profileFilter.getName();
  
      if (userName.length() != 0) {
        queryString.append("[");
      }
      
      if (userName.length() != 0) {
        userName += "*" ;
          
        queryString.append("(jcr:contains(@firstName, ").append("'").append(userName).append("'))");
      }
      
      if (userName.length() != 0) {
        queryString.append("]");
      }
      
      Query query1 = queryManager.createQuery(queryString.toString(), Query.XPATH);
      QueryResult queryResult = query1.execute();
      nodeIterator = queryResult.getNodes();
    } catch (Exception e) {
      LOG.warn("Failed to filter identities by alphabet" + e.getMessage());
      return null;
    } finally {
      sessionManager.closeSession();
    }
    
    while (nodeIterator.hasNext()) {
      Node profileNode = (Node) nodeIterator.next();
      Node identityNode = profileNode.getProperty(PROFILE_IDENTITY).getNode();
      Identity identity = getIdentity(identityNode);
      if(!identity.getProviderId().equals(identityProvider)) continue;
      listIdentity.add(identity);
    }
    
    return listIdentity;
  }
  
  /**
   * Save profile.
   * 
   * @param p the profile
   * @throws Exception the exception
   */
  public void saveProfile(Profile p) throws Exception {
    try {
      Session session = sessionManager.openSession();
      Node profileHomeNode = getProfileServiceHome(session);
      if (p.getIdentity().getId() == null) {
        throw new Exception("the identity has to be saved before saving the profile");
      }
  
      Node profileNode;
      if(p.getId() == null) {
        profileNode = profileHomeNode.addNode(PROFILE_NODETYPE, PROFILE_NODETYPE);
        profileNode.addMixin("mix:referenceable");
  
        Node identityNode = session.getNodeByUUID(p.getIdentity().getId());
  
        profileNode.setProperty(PROFILE_IDENTITY, identityNode);
      }
      else {
        profileNode = session.getNodeByUUID(p.getId());
      }
  
      saveProfile(p, profileNode, session);
  
  
      if (p.getId() == null) {
        LOG.debug("About to create a new profile...");
        profileHomeNode.save();
        p.setId(profileNode.getUUID());
      } else {
        profileNode.save();
      }
    } catch (Exception e) {
      LOG.error("Failed to save profile " + p, e);
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Save profile.
   * 
   * @param p the profile
   * @param n the node
   * @param session the session
   * @throws Exception the exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  protected void saveProfile(Profile p, Node n, Session session) throws Exception, IOException {
    Map<String,Object> props = p.getProperties();

    Iterator<String> it = props.keySet().iterator();

    //first we remove the nodes that have to be removed

    it = props.keySet().iterator();
    while(it.hasNext()) {
      String name = (String) it.next();

      //we skip all the property that are jcr related
      if (name.contains(":"))
        continue;

      Object propValue = props.get(name);

      //n.getProperty(name).remove();

      if(isForcedMultiValue(name)) {
        //if it's a String, we convert it to string array to be able to store it
        if(propValue instanceof String) {
          String[] arr = new String[1];
          arr[0] = (String) propValue;
          propValue = arr;
        }
        setProperty(name, (String[]) propValue, n);
      }
      else if (propValue instanceof String) {
        n.setProperty(name, (String) propValue);
        //System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if (propValue instanceof Double) {
        n.setProperty(name, (Double) propValue);
        //System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if (propValue instanceof Boolean) {
        n.setProperty(name, (Boolean) propValue);
        //System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if (propValue instanceof Long) {
        n.setProperty(name, (Long) propValue);
        //System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if ((propValue instanceof String[] && ((String[])propValue).length == 1)) {
        n.setProperty(name, ((String[])propValue)[0]);
        //System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if (propValue instanceof String[]) {
        setProperty(name, (String[]) propValue, n);
      }
      else if (propValue instanceof List) {
        setProperty(name, (List<Map<String,Object>>) propValue, n);
      }
      else if (propValue instanceof ProfileAttachment) {
        //fix id6 load
        ExtendedNode extNode = (ExtendedNode)n ;
        if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
        String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
        extNode.setPermission(SystemIdentity.ANY, arrayPers) ;
        List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;   
        for(AccessControlEntry accessControlEntry : permsList) {
          extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
        }
        ProfileAttachment profileAtt = (ProfileAttachment) propValue;
        if(profileAtt.getFileName() != null) {
          Node nodeFile = null ;
          try {
            nodeFile = n.getNode(name) ;
          } catch (PathNotFoundException ex) {
            nodeFile = n.addNode(name, "nt:file");
          }
          Node nodeContent = null ;
          try {
            nodeContent = nodeFile.getNode("jcr:content") ;
          } catch (PathNotFoundException ex) {
            nodeContent = nodeFile.addNode("jcr:content", "nt:resource") ;
          }
          long lastModified = profileAtt.getLastModified();
          long lastSaveTime = 0;
          if (nodeContent.hasProperty("jcr:lastModified"))
            lastSaveTime = nodeContent.getProperty("jcr:lastModified").getLong();
          if ((lastModified != 0) && (lastModified != lastSaveTime)) {
            nodeContent.setProperty("jcr:mimeType", profileAtt.getMimeType()) ;
            nodeContent.setProperty("jcr:data", profileAtt.getInputStream(session));
            nodeContent.setProperty("jcr:lastModified", profileAtt.getLastModified());
          }
        }
        else {
          if(n.hasNode(name)) {
            n.getNode(name).remove() ;
            // Add 29DEC. need review
            session.save();
          }
        }
      }
    }
  }

  /**
   * Sets the property.
   * 
   * @param name the name
   * @param props the props
   * @param n the node
   * @throws Exception the exception
   * @throws ConstraintViolationException the constraint violation exception
   * @throws VersionException the version exception
   */
  private void setProperty(String name, List<Map<String,Object>> props, Node n) throws Exception, ConstraintViolationException, VersionException {
    //System.out.println("setting the List prop " + name + " = " + props);

    String ntName = getNodeTypeName(name);
    if (ntName == null) {
      throw new Exception("no nodeType is defined for " + name);
    }

    // remove the existing nodes
    NodeIterator nIt = n.getNodes(name);
    while(nIt.hasNext()) {
      Node currNode = nIt.nextNode();
      currNode.remove();
    }

    Iterator<Map<String, Object>> it = props.iterator();
    while(it.hasNext()) {
      Map<String,Object> prop = it.next();
      Node propNode = n.addNode(name, ntName);

      Iterator<String> itKey = prop.keySet().iterator();
      while (itKey.hasNext()) {
        String key = (String) itKey.next();
        Object propValue = prop.get(key);

        if (propValue instanceof String) {
          propNode.setProperty(key, (String) propValue);
          //System.out.println("setting the prop String " + key + " = " + propValue);
        }
        else if (propValue instanceof Double) {
          propNode.setProperty(key, (Double) propValue);
          //System.out.println("setting the prop Double" + key + " = " + propValue);
        }
        else if (propValue instanceof Boolean) {
          propNode.setProperty(key, (Boolean) propValue);
          //System.out.println("setting the prop Boolean" + key + " = " + propValue);
        }
        else if (propValue instanceof Long) {
          propNode.setProperty(key, (Long) propValue);
//          System.out.println("setting the prop Long" + key + " = " + propValue);
        }
      }

    }
  }

  /**
   * Sets the property.
   * 
   * @param name the name
   * @param propValue the prop value
   * @param n the node
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws RepositoryException the repository exception
   * @throws ConstraintViolationException the constraint violation exception
   * @throws VersionException the version exception
   */
  private void setProperty(String name, String[] propValue, Node n) throws IOException, RepositoryException, ConstraintViolationException, VersionException {
    ArrayList<Value> values = new ArrayList<Value>();
    for (String value : propValue) {
      if(value != null && value.length() > 0) {
        values.add(new StringValue(value));
//        System.out.println("setting the multi prop " + name + " = " + value);
      }
    }
    n.setProperty(name, values.toArray(new Value[values.size()]));
  }

  /**
   * Load profile.
   * 
   * @param p the profile
   * @throws Exception the exception
   */
  public void loadProfile(Profile p) throws Exception {
    if (p.getIdentity().getId() == null) {
      throw new Exception("the identity has to be saved before loading the profile");
    }
    try {
      Session session = sessionManager.getOrOpenSession();
      
      Node idNode = session.getNodeByUUID(p.getIdentity().getId());
      PropertyIterator it = idNode.getReferences();
  
      while (it.hasNext()) {
        Property prop = (Property) it.next();
        if (prop.getParent().isNodeType(PROFILE_NODETYPE)) {
          Node n = prop.getParent();
          p.setId(n.getUUID());
          loadProfile(p, n, session);
          return;
        }
      }
      if(it.getSize() < 1) {
        LOG.debug("Lazily initializing a new Profile...");
        Node profileHomeNode = getProfileServiceHome(session);
          Node profileNode;
          if(p.getId() == null) {
            profileNode = profileHomeNode.addNode(PROFILE_NODETYPE, PROFILE_NODETYPE);
            profileNode.addMixin("mix:referenceable");
      
            Node identityNode = session.getNodeByUUID(p.getIdentity().getId());
      
            profileNode.setProperty(PROFILE_IDENTITY, identityNode);
          }
          else {
            profileNode = session.getNodeByUUID(p.getId());
          }
      
          saveProfile(p, profileNode, session);
          
          if (p.getId() == null) {
            profileHomeNode.save();
            p.setId(profileNode.getUUID());
          } else {
            profileNode.save();
          }
      }
    } catch (Exception e) {
      LOG.error("Failed to load Profile " + p, e);
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
   * @param p the p
   * @param n the n
   * @param session the session
   * @throws RepositoryException the repository exception
   */
  protected void loadProfile(Profile p, Node n, Session session) throws RepositoryException {
    //System.out.println("Loading the profile");
    PropertyIterator props = n.getProperties();

    copyPropertiesToMap(props, p.getProperties());
    //System.out.println("finished to load the props");

    NodeIterator it = n.getNodes();
    while(it.hasNext()) {
      Node node = it.nextNode();
      //System.out.println("Loading the node:" + node.getName());
      if(node.getName().equals(PROFILE_AVATAR)) {
        if (node.isNodeType("nt:file")) {
          ProfileAttachment file = new ProfileAttachment();
          file.setId(node.getPath());
          file.setMimeType(node.getNode("jcr:content").getProperty("jcr:mimeType").getString());
          try {
            file.setInputStream(node.getNode("jcr:content").getProperty("jcr:data").getValue().getStream());
          } catch (Exception e) {
            LOG.warn("Failed to load data for avatar of " + p + ": " + e.getMessage());
          }
          file.setLastModified(node.getNode("jcr:content").getProperty("jcr:lastModified").getLong());
          file.setFileName(node.getName());
          file.setWorkspace(session.getWorkspace().getName());
          p.setProperty(node.getName(), file);
        }
      } else {
        List l = (List) p.getProperty(node.getName());
        if(l == null) {
          p.setProperty(node.getName(), new ArrayList());
          l = (List) p.getProperty(node.getName());
        }
        l.add(copyPropertiesToMap(node.getProperties(), new HashMap()));
      }
    }
  }

  /**
   * Copy properties to map.
   * 
   * @param props the props
   * @param map the map
   * @return the map
   * @throws RepositoryException the repository exception
   */
  private Map copyPropertiesToMap(PropertyIterator props, Map map) throws RepositoryException {
    while(props.hasNext()) {
      Property prop = (Property) props.next();

      //we skip all the property that are jcr related
      if (prop.getName().contains(":"))
        continue;

      try {
        Value v = prop.getValue();
        if (v instanceof StringValue)
          map.put(prop.getName(), v.getString());
        else if (v instanceof LongValue)
          map.put(prop.getName(), v.getLong());
        else if (v instanceof DoubleValue)
          map.put(prop.getName(), v.getDouble());
        else if (v instanceof BooleanValue)
          map.put(prop.getName(), v.getBoolean());
        //System.out.println("loading " + prop.getName() + " = " + v.getString());
      }
      catch (ValueFormatException e) {
        //System.out.println("trying multivalue");
        Value[] values = prop.getValues();
        List<String> res = new ArrayList<String>();
          
        for(Value v : values) {
          res.add(v.getString());
          //System.out.println("loading multi" + prop.getName() + " = " + v.getString());
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
  
      for(PropertyDefinition pDef : pDefs) {
        if(pDef.getName().equals(property)) {
          return PropertyType.nameFromValue(pDef.getRequiredType());
        }
      }
    } catch (Exception e) {
      LOG.error("Could not find type of property " + property +" for nodetype " + nodetype);
      return null;
    } finally {
      sessionManager.closeSession();
    }
    return null;
  }
}

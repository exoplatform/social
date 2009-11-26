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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.impl.core.value.BooleanValue;
import org.exoplatform.services.jcr.impl.core.value.DoubleValue;
import org.exoplatform.services.jcr.impl.core.value.LongValue;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.space.JCRSessionManager;
import org.exoplatform.social.space.impl.SocialDataLocation;


public class JCRStorage {


  final private static String IDENTITY_NODETYPE = "exo:identity".intern();
  final private static String PROFILE_NODETYPE = "exo:profile".intern();

  final private static String IDENTITY_REMOTEID = "exo:remoteId".intern();
  final private static String IDENTITY_PROVIDERID = "exo:providerId".intern();

  final private static String PROFILE_IDENTITY = "exo:identity".intern();
  final private static String PROFILE_AVATAR = "avatar".intern();
  
  private ProfileConfig config = null;
  //new change
  private SocialDataLocation dataLocation;
  private JCRSessionManager sessionManager;
  


  public JCRStorage(SocialDataLocation dataLocation) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
  }


  private Node getIdentityServiceHome(SessionProvider sProvider) throws Exception {
    String path = dataLocation.getSocialIdentityHome();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }

  private ProfileConfig getConfig() {
    if (config == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      config = (ProfileConfig) container.getComponentInstanceOfType(ProfileConfig.class);
    }
    return config;
  }

  private Node getProfileServiceHome(SessionProvider sProvider) throws Exception {
    String path = dataLocation.getSocialProfileHome();
    return sessionManager.getSession(sProvider).getRootNode().getNode(path);
  }

  public void saveIdentity(Identity identity) {
    Session session =  sessionManager.openSession();
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      Node identityNode;
      Node identityHomeNode = getIdentityServiceHome(sProvider);

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
      // TODO: handle exception
    } finally {
      sProvider.close();
      sessionManager.closeSession(true);
    }
  }

  public Identity getIdentity(String id) throws Exception {
    Session session = sessionManager.openSession();
    Node identityNode;
    try {
      identityNode = session.getNodeByUUID(id);
    }
    catch (ItemNotFoundException e) {
      return null;
    } finally {
      sessionManager.closeSession();
    }
    Identity identity = new Identity(identityNode.getUUID());
    identity.setProviderId(identityNode.getProperty(IDENTITY_PROVIDERID).getString());
    identity.setRemoteId(identityNode.getProperty(IDENTITY_REMOTEID).getString());

    return identity;
  }

  public Identity getIdentityByRemoteId(String identityProvider, String id) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Session session = sessionManager.openSession();
    Node identityHomeNode = getIdentityServiceHome(sProvider);

    StringBuffer queryString = new StringBuffer("/").append(identityHomeNode.getPath())
        .append("/").append(IDENTITY_NODETYPE).append("[(@")
        .append(IDENTITY_PROVIDERID).append("='").append(identityProvider).append("' and @")
        .append(IDENTITY_REMOTEID).append("='").append(id.replaceAll("'", "''")).append("')]");
    
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    Query query = queryManager.createQuery(queryString.toString(), Query.XPATH);
    QueryResult queryResult = query.execute();
    NodeIterator nodeIterator = queryResult.getNodes();

    Identity identity = null;
    if (nodeIterator.getSize() == 1) {

      Node identityNode = (Node) nodeIterator.next();
      identity = new Identity(identityNode.getUUID());
      identity.setProviderId(identityNode.getProperty(IDENTITY_PROVIDERID).getString());
      identity.setRemoteId(identityNode.getProperty(IDENTITY_REMOTEID).getString());
    }
    sessionManager.closeSession();
    sProvider.close();
    return identity;
  }
  
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
  
  public List<Identity> getIdentitiesByProfileFilter(String identityProvider, ProfileFiler profileFilter) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node profileHomeNode = getProfileServiceHome(sProvider);
    Session session = sessionManager.openSession();
    
    QueryManager queryManager = session.getWorkspace().getQueryManager() ;
    StringBuffer queryString = new StringBuffer("/").append(profileHomeNode.getPath())
        .append("/").append(PROFILE_NODETYPE);
    String userName = profileFilter.getName();
    String position = profileFilter.getPosition();
    String gender = profileFilter.getGender();

    if ((userName.length() != 0) || (position.length() != 0) || (gender.length() != 0)) {
      queryString.append("[");
    }
    
    if (userName.length() != 0) {
      if(userName.indexOf("*")<0){
        if(userName.charAt(0)!='*') userName = "*"+userName ;
        if(userName.charAt(userName.length()-1)!='*') userName += "*" ;
      }
      queryString.append("(jcr:contains(@firstName, ").append("'").append(userName).append("')");
      queryString.append(" or jcr:contains(@lastName, ").append("'").append(userName).append("'))");
    }
    
    if (position.length() != 0) {
      if(position.indexOf("*")<0){
        if(position.charAt(0)!='*') position = "*"+position ;
        if(position.charAt(position.length()-1)!='*') position += "*" ;
      }
      if (userName.length() != 0) {
        queryString.append(" and jcr:contains(@position, ").append("'").append(position).append("')");
      } else {
        queryString.append("jcr:contains(@position, ").append("'").append(position).append("')");
      }
    }

    if (gender.length() != 0) {
      if ((userName.length() != 0) || (position.length() != 0)) {
        queryString.append(" and (@gender ").append("= '").append(gender).append("')");
      } else {
        queryString.append(" @gender ").append("= '").append(gender).append("'");
      }
    }
    
    if ((userName.length() != 0) || (position.length() != 0) || (gender.length() != 0)) {
      queryString.append("]");
    }
    
    Query query1 = queryManager.createQuery(queryString.toString(), Query.XPATH);
    QueryResult queryResult = query1.execute();
    NodeIterator nodeIterator = queryResult.getNodes();
    List<Identity> listIdentity = new ArrayList<Identity>();
    while (nodeIterator.hasNext()) {
      Node profileNode = (Node) nodeIterator.next();
      Node identityNode = profileNode.getProperty(PROFILE_IDENTITY).getNode();
      listIdentity.add(getIdentity(identityNode));
    }
    sProvider.close();
    sessionManager.closeSession();
    return listIdentity;
  }

  public List<Identity> getIdentitiesFilterByAlphaBet(String identityProvider, ProfileFiler profileFilter) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node profileHomeNode = getProfileServiceHome(sProvider);
    Session session = sessionManager.openSession();
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
    NodeIterator nodeIterator = queryResult.getNodes();
    List<Identity> listIdentity = new ArrayList<Identity>();
    while (nodeIterator.hasNext()) {
      Node profileNode = (Node) nodeIterator.next();
      Node identityNode = profileNode.getProperty(PROFILE_IDENTITY).getNode();
      listIdentity.add(getIdentity(identityNode));
    }
    sessionManager.closeSession();
    sProvider.close();
    return listIdentity;
  }
  
  public void saveProfile(Profile p) throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node profileHomeNode = getProfileServiceHome(sProvider);
    Session session = sessionManager.openSession();

    if (p.getIdentity().getId() == null) {
      sessionManager.closeSession();
      sProvider.close();
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

    saveProfile(p, profileNode);


    if (p.getId() == null) {
      //System.out.println("saving all the profileHomeNode");
      profileHomeNode.save();
      p.setId(profileNode.getUUID());
    } else {
      profileNode.save();
      //System.out.println("saving the profileNode");
      //getSession().save();
    }
    sProvider.close();
    sessionManager.closeSession(true);
  }

  protected void saveProfile(Profile p, Node n) throws Exception, IOException {
    Map props = p.getProperties();

    Iterator it = props.keySet().iterator();

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
        setProperty(name, (List<Map>) propValue, n);
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
          nodeContent.setProperty("jcr:mimeType", profileAtt.getMimeType()) ;
          nodeContent.setProperty("jcr:data", profileAtt.getInputStream());
          nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());
        }
        else {
          if(n.hasNode(name)) n.getNode(name).remove() ;
        }
      }
    }
  }

  private void setProperty(String name, List<Map> props, Node n) throws Exception, ConstraintViolationException, VersionException {
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

    Iterator<Map> it = props.iterator();
    while(it.hasNext()) {
      Map prop = it.next();
      Node propNode = n.addNode(name, ntName);

      Iterator itKey = prop.keySet().iterator();
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

  public void loadProfile(Profile p) throws Exception {
    if (p.getIdentity().getId() == null) {
      throw new Exception("the identity has to be saved before loading the profile");
    }

    Session session = sessionManager.openSession();
    
    Node idNode = session.getNodeByUUID(p.getIdentity().getId());
    PropertyIterator it = idNode.getReferences();

    while (it.hasNext()) {
      Property prop = (Property) it.next();
//      System.out.println("is the profile NT? " + prop.getParent().getPrimaryNodeType().getName()
//          + " " + prop.getParent().getUUID());
      if (prop.getParent().isNodeType(PROFILE_NODETYPE)) {
        //System.out.println("found the profile");
        Node n = prop.getParent();
        p.setId(n.getUUID());
        loadProfile(p, n);
        return;
      }
    }
    sessionManager.closeSession();
    //System.out.println("did not find the profile");
  }

  protected boolean isForcedMultiValue(String key) {
    return getConfig().isForcedMultiValue(key);
  }

  protected String getNodeTypeName(String nodeName) {
    return getConfig().getNodeType(nodeName);
  }

  protected void loadProfile(Profile p, Node n) throws RepositoryException {
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
          file.setFileName(node.getName());
          file.setWorkspace(node.getSession().getWorkspace().getName());
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
      //System.out.println("finish Loading the node:" + node.getName());
    }
    //System.out.println("nodetype: " + n.getPrimaryNodeType().getName());
  }

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

  public String getType(String nodetype, String property) throws Exception {
    //System.out.println("getType(" + nodetype + ", " + property + ")");
    Session session = sessionManager.openSession();
    
    NodeTypeManager ntManager = session.getWorkspace().getNodeTypeManager();
    NodeType nt = ntManager.getNodeType(nodetype);
    PropertyDefinition[] pDefs = nt.getDeclaredPropertyDefinitions();

    for(PropertyDefinition pDef : pDefs) {
      if(pDef.getName().equals(property)) {
        sessionManager.closeSession();
        //System.out.println("getType(" + nodetype + ", " + property + ") ==" + pDef.getRequiredType());
        return PropertyType.nameFromValue(pDef.getRequiredType());
      }
    }
    sessionManager.closeSession();
    return null;
  }
}

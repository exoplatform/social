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

import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.impl.core.value.StringValue;
import org.exoplatform.services.jcr.impl.core.value.LongValue;
import org.exoplatform.services.jcr.impl.core.value.DoubleValue;
import org.exoplatform.services.jcr.impl.core.value.BooleanValue;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

import javax.jcr.*;
import javax.jcr.version.VersionException;
import javax.jcr.nodetype.*;
import javax.jcr.lock.LockException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.QueryManager;
import javax.jcr.query.Query;
import java.util.*;
import java.io.IOException;


public class JCRStorage {

  private NodeHierarchyCreator nodeHierarchyCreator;

  final private static String IDENTITY_NODETYPE = "exo:identity".intern();
  final private static String IDENTITY_APP = "Social_Identity".intern();
  final private static String PROFILE_APP = "Social_Profile".intern();
  final private static String PROFILE_NODETYPE = "exo:profile".intern();
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern();

  final private static String IDENTITY_REMOTEID = "exo:remoteId".intern();
  final private static String IDENTITY_PROVIDERID = "exo:providerId".intern();

  final private static String PROFILE_IDENTITY = "exo:identity".intern();




  private ProfileConfig config = null;



  public JCRStorage(NodeHierarchyCreator nodeHierarchyCreator) {
    this.nodeHierarchyCreator = nodeHierarchyCreator;
  }


  private Node getIdentityServiceHome() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();

    Node appsNode = nodeHierarchyCreator.getPublicApplicationNode(sProvider);

    try {
      return appsNode.getNode(IDENTITY_APP);
    } catch (PathNotFoundException ex) {
      Node appNode = appsNode.addNode(IDENTITY_APP, NT_UNSTRUCTURED);
      appsNode.save();
      return appNode;
    }
  }

  private ProfileConfig getConfig() {
    if (config == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      config = (ProfileConfig) container.getComponentInstanceOfType(ProfileConfig.class);
    }
    return config;
  }

  private Node getProfileServiceHome() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();

    Node appsNode = nodeHierarchyCreator.getPublicApplicationNode(sProvider);

    try {
      return appsNode.getNode(PROFILE_APP);
    } catch (PathNotFoundException ex) {
      Node appNode = appsNode.addNode(PROFILE_APP, NT_UNSTRUCTURED);
      appsNode.save();
      return appNode;
    }
  }

  public void saveIdentity(Identity identity) throws Exception {
    Node identityNode;
    Node identityHomeNode = getIdentityServiceHome();

    if (identity.getId() == null) {
      identityNode = identityHomeNode.addNode(IDENTITY_NODETYPE, IDENTITY_NODETYPE);
      identityNode.addMixin("mix:referenceable");
    } else {
      identityNode = identityHomeNode.getSession().getNodeByUUID(identity.getId());
    }
    identityNode.setProperty(IDENTITY_REMOTEID, identity.getRemoteId());
    identityNode.setProperty(IDENTITY_PROVIDERID, identity.getProviderId());

    if (identity.getId() == null) {
      identityHomeNode.save();
      identity.setId(identityNode.getUUID());
    } else {
      identityNode.save();
    }
  }

  public Identity getIdentity(String id) throws Exception {
    Node identityHomeNode = getIdentityServiceHome();

    Node identityNode;
    try {
      identityNode = identityHomeNode.getSession().getNodeByUUID(id);
    }
    catch (ItemNotFoundException e) {
      return null;
    }
    Identity identity = new Identity(identityNode.getUUID());
    identity.setProviderId(identityNode.getProperty(IDENTITY_PROVIDERID).getString());
    identity.setRemoteId(identityNode.getProperty(IDENTITY_REMOTEID).getString());

    return identity;
  }

  public Identity getIdentityByRemoteId(String identityProvider, String id) throws Exception {
    Node identityHomeNode = getIdentityServiceHome();

    StringBuffer queryString = new StringBuffer("/").append(identityHomeNode.getPath())
        .append("/").append(IDENTITY_NODETYPE).append("[(@")
        .append(IDENTITY_PROVIDERID).append("='").append(identityProvider).append("' and @")
        .append(IDENTITY_REMOTEID).append("='").append(id.replaceAll("'", "''")).append("')]");
    QueryManager queryManager = identityHomeNode.getSession().getWorkspace().getQueryManager();
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

    return identity;
  }

  public void saveProfile(Profile p) throws Exception {
    Node profileHomeNode = getProfileServiceHome();

    if (p.getIdentity().getId() == null) {
      throw new Exception("the identity has to be saved before saving the profile");
    }

    Node profileNode;
    if(p.getId() == null) {
      profileNode = profileHomeNode.addNode(PROFILE_NODETYPE, PROFILE_NODETYPE);
      profileNode.addMixin("mix:referenceable");

      Node identityNode = profileHomeNode.getSession().getNodeByUUID(p.getIdentity().getId());

      profileNode.setProperty(PROFILE_IDENTITY, identityNode);
    }
    else {
      profileNode = profileHomeNode.getSession().getNodeByUUID(p.getId());
    }

    saveProfile(p, profileNode);


    if (p.getId() == null) {
      System.out.println("saving all the profileHomeNode");
      profileHomeNode.save();
      p.setId(profileNode.getUUID());
    } else {
      System.out.println("saving the profileNode");
      profileNode.save();
    }
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
        System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if (propValue instanceof Double) {
        n.setProperty(name, (Double) propValue);
        System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if (propValue instanceof Boolean) {
        n.setProperty(name, (Boolean) propValue);
        System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if (propValue instanceof Long) {
        n.setProperty(name, (Long) propValue);
        System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if ((propValue instanceof String[] && ((String[])propValue).length == 1)) {
        n.setProperty(name, ((String[])propValue)[0]);
        System.out.println("setting the prop " + name + " = " + propValue);
      }
      else if (propValue instanceof String[]) {
        setProperty(name, (String[]) propValue, n);
      }
      else if (propValue instanceof List) {
        setProperty(name, (List<Map>) propValue, n);
      }
      
    }
  }

  private void setProperty(String name, List<Map> props, Node n) throws Exception, ConstraintViolationException, VersionException {
    System.out.println("setting the List prop " + name + " = " + props);

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
          System.out.println("setting the prop String " + key + " = " + propValue);
        }
        else if (propValue instanceof Double) {
          propNode.setProperty(key, (Double) propValue);
          System.out.println("setting the prop Double" + key + " = " + propValue);
        }
        else if (propValue instanceof Boolean) {
          propNode.setProperty(key, (Boolean) propValue);
          System.out.println("setting the prop Boolean" + key + " = " + propValue);
        }
        else if (propValue instanceof Long) {
          propNode.setProperty(key, (Long) propValue);
          System.out.println("setting the prop Long" + key + " = " + propValue);
        }
      }

    }
  }

  private void setProperty(String name, String[] propValue, Node n) throws IOException, RepositoryException, ConstraintViolationException, VersionException {
    ArrayList<Value> values = new ArrayList<Value>();
    for (String value : propValue) {
      if(value != null && value.length() > 0) {
        values.add(new StringValue(value));
        System.out.println("setting the multi prop " + name + " = " + value);
      }
    }
    n.setProperty(name, values.toArray(new Value[values.size()]));
  }

  public void loadProfile(Profile p) throws Exception {
    Node profileHomeNode = getProfileServiceHome();

    if (p.getIdentity().getId() == null) {
      throw new Exception("the identity has to be saved before loading the profile");
    }

    Node idNode = profileHomeNode.getSession().getNodeByUUID(p.getIdentity().getId());
    PropertyIterator it = idNode.getReferences();

    while (it.hasNext()) {
      Property prop = (Property) it.next();
      System.out.println("is the profile NT? " + prop.getParent().getPrimaryNodeType().getName()
          + " " + prop.getParent().getUUID());
      if (prop.getParent().isNodeType(PROFILE_NODETYPE)) {
        System.out.println("found the profile");
        Node n = prop.getParent();
        p.setId(n.getUUID());
        loadProfile(p, n);
        return;
      }
    }
    System.out.println("did not find the profile");
  }

  protected boolean isForcedMultiValue(String key) {
    return getConfig().isForcedMultiValue(key);
  }

  protected String getNodeTypeName(String nodeName) {
    return getConfig().getNodeType(nodeName);
  }

  protected void loadProfile(Profile p, Node n) throws RepositoryException {
    System.out.println("Loading the profile");
    PropertyIterator props = n.getProperties();

    copyPropertiesToMap(props, p.getProperties());
    System.out.println("finished to load the props");

    NodeIterator it = n.getNodes();
    while(it.hasNext()) {
      Node node = it.nextNode();
      System.out.println("Loading the node:" + node.getName());
      List l = (List) p.getProperty(node.getName());
      if(l == null) {
        p.setProperty(node.getName(), new ArrayList());
        l = (List) p.getProperty(node.getName());
      }
      l.add(copyPropertiesToMap(node.getProperties(), new HashMap()));
      System.out.println("finish Loading the node:" + node.getName());
    }
    System.out.println("nodetype: " + n.getPrimaryNodeType().getName());
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
        System.out.println("loading " + prop.getName() + " = " + v.getString());
      }
      catch (ValueFormatException e) {
        System.out.println("trying multivalue");
        Value[] values = prop.getValues();
        List<String> res = new ArrayList<String>();

        for(Value v : values) {
          res.add(v.getString());
          System.out.println("loading multi" + prop.getName() + " = " + v.getString());
        }
        map.put(prop.getName(), res.toArray(new String[res.size()]));
      }
    }
    return map;
  }

  public String getType(String nodetype, String property) throws Exception {
    System.out.println("getType(" + nodetype + ", " + property + ")");

    ExoContainer container = ExoContainerContext.getCurrentContainer();
    SessionProviderService sProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class);

    NodeTypeManager ntManager = getProfileServiceHome().getSession().getWorkspace().getNodeTypeManager();
    NodeType nt = ntManager.getNodeType(nodetype);
    PropertyDefinition[] pDefs = nt.getDeclaredPropertyDefinitions();

    for(PropertyDefinition pDef : pDefs) {
      if(pDef.getName().equals(property)) {
        System.out.println("getType(" + nodetype + ", " + property + ") ==" + pDef.getRequiredType());
        return PropertyType.nameFromValue(pDef.getRequiredType());
      }
    }
    return null;
  }
}

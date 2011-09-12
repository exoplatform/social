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

package org.exoplatform.social.extras.migration.loading;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.staxnav.Naming;
import org.staxnav.StaxNavigator;
import org.staxnav.StaxNavigatorFactory;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.PropertyDefinition;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class DataLoader {

  private final StaxNavigator<QName> navigator;

  private OrganizationService organizationService;

  private Session session;

  private final String DATA_PACKAGE = "org.exoplatform.social.extras.migration";
  private final String EXO_NS = "http://www.exoplatform.com/jcr/exo/1.0";
  private final String LOADER_NS = "loader";

  private final String TYPE_NODE = "type";
  private final String PROVIDER_ID_NODE = "providerId";
  private final String REMOTE_ID_NODE = "remoteId";
  private final String GROUP_ID_NODE = "groupId";

  private static final Log LOG = ExoLogger.getLogger(DataLoader.class);

  public DataLoader(final String name, final Session session) {

    this.session = session;
    PortalContainer container = PortalContainer.getInstance();
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);
    String fullName = DATA_PACKAGE.replace('.', '/') + '/' + name;

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fullName);

    if (is == null) {
      throw new NullPointerException(name + " not found");
    }

    try {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader stream = factory.createXMLStreamReader(is);
      navigator = StaxNavigatorFactory.create(new Naming.Qualified(), stream);
    }
    catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }

  }

  public void load() throws Exception {

    Node rootNode = session.getRootNode();

    writeNode(rootNode, navigator);

  }

  private void writeNode(Node node, StaxNavigator<QName> nav) throws Exception {

    QName current = nav.child();

    while (current != null) {

      if ("loader".equals(current.getPrefix())) {
        current = nav.sibling();
        continue;
      }

      String name = current.getLocalPart();
      if (!"".equals(current.getPrefix())) {
        name = current.getPrefix() + ":" + name;
      }

      String type = nav.getAttribute(new QName(LOADER_NS, TYPE_NODE));
      String reuse = nav.getAttribute(new QName(LOADER_NS, "reuse"));
      String overridename = nav.getAttribute(new QName(LOADER_NS, "overridename"));

      if (overridename != null) {
        name = resolvePropertyValue(overridename);
      }

      Node created;

      if (!"true".equals(reuse)) {

        if (type == null) {
          created = node.addNode(name);
        }
        else {
          created = node.addNode(name, type);
        }
        LOG.trace("Create node : " + created.getPath());
      }
      else {
        created = node.getNode(name);
      }

      //
      handleUser(type, nav);
      handleGroup(type, nav);
      if ("exo:space".equals(type)) {
        handleMembership(node, nav.fork());
      }

      //
      writeNode(created, nav.fork());
      handleMixins(created, nav.fork());
      writeProperty(created, nav.fork());

      current = nav.sibling();

    }
  }

  private void writeProperty(Node node, StaxNavigator<QName> nav) throws RepositoryException {

    Map<QName, String> attributes = nav.getQualifiedAttributes();
    if (attributes.isEmpty()) {
      return;
    }

    for (QName key : attributes.keySet()) {


      if ("loader".equals(key.getPrefix())) {
        continue;
      }

      String name = key.getLocalPart();
      if (!"".equals(key.getPrefix())) {
        name = key.getPrefix() + ":" + name;
      }

      String unresolvedValue = attributes.get(key);

      Property p;
      if (isMultiple(node, name)) {
        p = node.setProperty(name, resolvePropertyValue(unresolvedValue.split(",")));
      }
      else {
        String propertyValue = resolvePropertyValue(unresolvedValue);
        if (propertyValue.equals(attributes.get(key))) {
          p = node.setProperty(name, propertyValue);
        }
        else {
          p = node.setProperty(name, session.getNodeByUUID(propertyValue));
        }
        LOG.trace("Create property : " + p.getPath() + " = " + propertyValue);
      }
    }

  }

  private void handleMixins(Node node, StaxNavigator<QName> nav) throws RepositoryException {

    boolean found = nav.child(new QName("loader", "mixin"));

    while (found) {

      node.addMixin(nav.getContent());

      found = nav.sibling(new QName("loader", "mixin"));

    }

  }

  private void handleMembership(Node node, StaxNavigator<QName> nav) throws Exception {

    String groupId = nav.getAttribute(new QName(EXO_NS, "groupId"));
    Group group = organizationService.getGroupHandler().findGroupById(groupId);

    boolean found = nav.child(new QName("loader", "membership"));

    while (found) {

      String members = nav.getAttribute("members");
      String managers = nav.getAttribute("managers");

      if (members != null) {
        for (String member : members.split(",")) {
          writeMembership("member", member, group);
        }
      }

      if (managers != null) {
        for (String manager :  managers.split(",")) {
          writeMembership("manager", manager, group);
        }
      }

      found = nav.sibling(new QName("loader", "membership"));

    }

  }

  private void handleUser(String type, StaxNavigator<QName> nav) throws Exception {
    if ("exo:identity".equals(type)) {
      if (nav.getAttribute(new QName(EXO_NS, PROVIDER_ID_NODE)).equals("organization")) {
        String username = nav.getAttribute(new QName(EXO_NS, REMOTE_ID_NODE));
        User u = organizationService.getUserHandler().createUserInstance(username);
        organizationService.getUserHandler().createUser(u, true);
        LOG.trace("Create user : " + u.getUserName());
      }
    }
  }

  private void handleGroup(String type, StaxNavigator<QName> nav) throws Exception {
    if ("exo:space".equals(type)) {
      Group g = organizationService.getGroupHandler().createGroupInstance();
      String groupId = nav.getAttribute(new QName(EXO_NS, GROUP_ID_NODE));
      String title = nav.getAttribute(new QName(EXO_NS, "name"));
      g.setGroupName(groupId.substring(groupId.lastIndexOf("/") + 1));
      g.setLabel(title);
      Group spaces = organizationService.getGroupHandler().findGroupById("/spaces");
      organizationService.getGroupHandler().addChild(spaces, g, true);
      LOG.trace("Create group : " + g.getId());
    }
  }

  private String resolvePropertyValue(String value) throws RepositoryException {

    // get UUID from path
    if (value.startsWith("#")) {
      Item item = session.getItem(value.substring(1));
      if (item.isNode()) {
        return ((Node) item).getUUID();
      }
    }

    // return value
    return value;

  }

  private String[] resolvePropertyValue(String[] values) throws RepositoryException {

    String[] resolvedValues = new String[values.length];

    for(int i = 0; i < values.length; ++i) {
      resolvedValues[i] = resolvePropertyValue(values[i]);
    }

    return resolvedValues;

  }

  private void writeMembership(String typeName, String memberName, Group group) throws Exception {
    MembershipType type = organizationService.getMembershipTypeHandler().findMembershipType(typeName);
    User user = organizationService.getUserHandler().findUserByName(memberName);
    organizationService.getMembershipHandler().linkMembership(user, group, type, true);
    LOG.trace("Create membership : " + user.getUserName() + " is " + type + " of " + group.getId());
  }

  private boolean isMultiple(Node node, String propertyName) throws RepositoryException {
    for (PropertyDefinition propertyDefinition : node.getPrimaryNodeType().getPropertyDefinitions()) {
      if (propertyDefinition.getName().equals(propertyName)) {
        return propertyDefinition.isMultiple();
      }
    }
    return false;
  }
  
}

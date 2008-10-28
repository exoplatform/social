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
package org.exoplatform.social.space.impl;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 29, 2008          
 */
public class SpaceServiceImpl implements SpaceService {

  final static private String SOCIAL_SPACE = "Social_Space".intern();
  final static private String SPACE = "Space".intern();
  final static private String NT_UNSTRUCTURED = "nt:unstructured".intern();
  final static private String SPACE_NODETYPE = "exo:space".intern();
  final static private String SPACE_ID = "exo:id".intern();
  final static private String SPACE_NAME = "exo:name".intern();
  final static private String SPACE_GROUPID = "exo:groupId".intern();
  final static private String SPACE_APP = "exo:app".intern();
  final static private String SPACE_PARENT = "exo:parent".intern();
  final static private String SPACE_DESCRIPTION = "exo:description".intern();
  final static private String SPACE_TAG = "exo:tag".intern();
  final static private String SPACE_PENDING_USER = "exo:pendingUser".intern();
  final static private String SPACE_INVITED_USER = "exo:invitedUser".intern();
  final static private String SPACE_TYPE = "exo:type".intern();
  
  private NodeHierarchyCreator nodeHierarchyCreator_ ;  
  
  public SpaceServiceImpl(NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    nodeHierarchyCreator_ = nodeHierarchyCreator;
  }

  private Node getSocialSpaceHome() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    Node appsNode = nodeHierarchyCreator_.getPublicApplicationNode(sProvider);

    try {
        return appsNode.getNode(SOCIAL_SPACE);
    } catch (PathNotFoundException ex) {
        Node appNode = appsNode.addNode(SOCIAL_SPACE, NT_UNSTRUCTURED);
        appsNode.save();
        return appNode;
    }
  }
  
  private Node getSpaceHome() throws Exception {
    Node appsNode = getSocialSpaceHome();
    
    try {
        return appsNode.getNode(SPACE);
    } catch (PathNotFoundException ex) {
        Node appNode = appsNode.addNode(SPACE, NT_UNSTRUCTURED);
        appsNode.save();
        return appNode;
    }
  }
  
  public List<Space> getAllSpaces() throws Exception {
    Node spaceHomeNode = getSpaceHome();
    List<Space> communities = new ArrayList<Space>();
    NodeIterator iter = spaceHomeNode.getNodes();
    Space space;
    while (iter.hasNext()) {
      Node spaceNode = iter.nextNode();
      space = getSpace(spaceNode);
      communities.add(space);
    }
    return communities;
  }

  public Space getSpace(String id) throws Exception {
    Node spaceHomeNode = getSpaceHome();
    try {
      return getSpace(spaceHomeNode.getNode(id));
    } catch (PathNotFoundException ex) {
      return null;
    }
  }

  public void saveSpace(Space space, boolean isNew) throws Exception {
    Node spaceHomeNode = getSpaceHome();
    saveSpace(spaceHomeNode, space, isNew);
    spaceHomeNode.getSession().save();
  }
  
  private void saveSpace(Node spaceHomeNode, Space space, boolean isNew) throws Exception {
    Node spaceNode;
    if(isNew) {
      spaceNode = spaceHomeNode.addNode(space.getId(),SPACE_NODETYPE);
      spaceNode.setProperty(SPACE_ID, space.getId());
    } else {
      spaceNode = spaceHomeNode.getNode(space.getId());
    }
    spaceNode.setProperty(SPACE_NAME, space.getName());
    spaceNode.setProperty(SPACE_GROUPID, space.getGroupId());
    spaceNode.setProperty(SPACE_APP, space.getApp());
    spaceNode.setProperty(SPACE_PARENT, space.getParent());
    spaceNode.setProperty(SPACE_DESCRIPTION, space.getDescription());
    spaceNode.setProperty(SPACE_TAG, space.getTag());
    spaceNode.setProperty(SPACE_PENDING_USER, space.getPendingUser());
    spaceNode.setProperty(SPACE_INVITED_USER, space.getInvitedUser());
    spaceNode.setProperty(SPACE_TYPE, space.getType());
  }
  
  private Space getSpace(Node spaceNode) throws Exception{
    Space space = new Space();
    if(spaceNode.hasProperty(SPACE_ID)) space.setId(spaceNode.getProperty(SPACE_ID).getString());
    if(spaceNode.hasProperty(SPACE_NAME)) space.setName(spaceNode.getProperty(SPACE_NAME).getString());
    if(spaceNode.hasProperty(SPACE_GROUPID)) space.setGroupId(spaceNode.getProperty(SPACE_GROUPID).getString());
    if(spaceNode.hasProperty(SPACE_APP)) space.setApp(spaceNode.getProperty(SPACE_APP).getString());
    if(spaceNode.hasProperty(SPACE_PARENT)) space.setParent(spaceNode.getProperty(SPACE_PARENT).getString());
    if(spaceNode.hasProperty(SPACE_DESCRIPTION)) space.setDescription(spaceNode.getProperty(SPACE_DESCRIPTION).getString());
    if(spaceNode.hasProperty(SPACE_TAG)) space.setTag(spaceNode.getProperty(SPACE_TAG).getString());
    if(spaceNode.hasProperty(SPACE_PENDING_USER)) space.setPendingUser(spaceNode.getProperty(SPACE_PENDING_USER).getString());
    if(spaceNode.hasProperty(SPACE_INVITED_USER)) space.setInvitedUser(spaceNode.getProperty(SPACE_INVITED_USER).getString());
    if(spaceNode.hasProperty(SPACE_TYPE)) space.setType(spaceNode.getProperty(SPACE_TYPE).getString());
    return space;
  }
}
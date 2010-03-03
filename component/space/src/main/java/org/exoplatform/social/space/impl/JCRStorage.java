/**
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
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.social.space.JCRSessionManager;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceAttachment;


public class JCRStorage {
  final static private String SPACE_NODETYPE = "exo:space".intern();
  final static private String SPACE_NAME = "exo:name".intern();
  final static private String SPACE_GROUPID = "exo:groupId".intern();
  final static private String SPACE_APP = "exo:app".intern();
  final static private String SPACE_PARENT = "exo:parent".intern();
  final static private String SPACE_DESCRIPTION = "exo:description".intern();
  final static private String SPACE_TAG = "exo:tag".intern();
  final static private String SPACE_PENDING_USER = "exo:pendingUsers".intern();
  final static private String SPACE_INVITED_USER = "exo:invitedUsers".intern();
  final static private String SPACE_TYPE = "exo:type".intern();
  final static private String SPACE_URL = "exo:url".intern();
  final static private String SPACE_VISIBILITY = "exo:visibility".intern();
  final static private String SPACE_REGISTRATION = "exo:registration".intern();
  final static private String SPACE_PRIORITY = "exo:priority".intern();

  //new change
  private SocialDataLocation dataLocation;
  private JCRSessionManager sessionManager;
  private String repository;
  private String workspace;

  public JCRStorage(SocialDataLocation dataLocation) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
    this.repository = dataLocation.getRepository();
    this.workspace = dataLocation.getWorkspace();
  }

  private Node getSpaceHome(Session session) throws Exception {
    String path = dataLocation.getSocialSpaceHome();
    return session.getRootNode().getNode(path);
  }

  public String getRepository() throws Exception {
     return repository;
  }
  
  public String getWorkspace() throws Exception {
     return workspace;
  }
  
  /**
   * Gets the all spaces.
   * 
   * @return the all spaces
   */
  public List<Space> getAllSpaces() {
    List<Space> spaces = new ArrayList<Space>();
    try {
      Session session = sessionManager.openSession();
      Node spaceHomeNode = getSpaceHome(session);
      NodeIterator iter = spaceHomeNode.getNodes();
      Space space;
      while (iter.hasNext()) {
        Node spaceNode = iter.nextNode();
        space = getSpace(spaceNode, session);
        spaces.add(space);
      }
      return spaces;
    } catch (Exception e) {
//      System.out.println("\n\n\n\n\n\n ===>>>>> ====getAllSpaces err. return null \n");
      e.printStackTrace();
      return null;
    } finally {
      sessionManager.closeSession();
    }
  }

  public Space getSpaceById(String id) {
    try {
      Session session = sessionManager.openSession();
      
      return getSpace(session.getNodeByUUID(id), session);
    } catch (Exception e) {
      return null;
    } finally {
      sessionManager.closeSession();
    }
  }
  
  public Space getSpaceByUrl(String url) {
    try {
      Session session = sessionManager.openSession();
      
      Node spaceHomeNode = getSpaceHome(session);
      NodeIterator iter = spaceHomeNode.getNodes();
      Space space;
      while (iter.hasNext()) {
        Node spaceNode = iter.nextNode();
        space = getSpace(spaceNode, session);
        if(space.getUrl().equals(url)) return space;
      }
    }catch (Exception e) {
      return null;
    } finally {
      sessionManager.closeSession();
    }
    return null;
  }
  
  public void deleteSpace(String id) {
    Session session = sessionManager.openSession();
    try {
      Node spaceNode = session.getNodeByUUID(id);
      if(spaceNode != null) {
        spaceNode.remove();
        session.save();
      }
    } catch (Exception e) {
      // TODO: handle exception
    } finally {
      sessionManager.closeSession();
    }
  }

  public void saveSpace(Space space, boolean isNew) {
    try {
      Session session = sessionManager.openSession();
      
      Node spaceHomeNode = getSpaceHome(session);
      saveSpace(spaceHomeNode, space, isNew, session);
    } catch (Exception e) {
      // TODO: handle exception
    } finally {
      sessionManager.closeSession();}
  }

  private void saveSpace(Node spaceHomeNode, Space space, boolean isNew, Session session) {
    Node spaceNode;
    try {
      if(isNew) {
        spaceNode = spaceHomeNode.addNode(SPACE_NODETYPE,SPACE_NODETYPE);
        spaceNode.addMixin("mix:referenceable");
      } else {
        spaceNode = session.getNodeByUUID(space.getId());
      }
      if(space.getId() == null) space.setId(spaceNode.getUUID());
      spaceNode.setProperty(SPACE_NAME, space.getName());
      spaceNode.setProperty(SPACE_GROUPID, space.getGroupId());
      spaceNode.setProperty(SPACE_APP, space.getApp());
      spaceNode.setProperty(SPACE_PARENT, space.getParent());
      spaceNode.setProperty(SPACE_DESCRIPTION, space.getDescription());
      spaceNode.setProperty(SPACE_TAG, space.getTag());
      spaceNode.setProperty(SPACE_PENDING_USER, space.getPendingUsers());
      spaceNode.setProperty(SPACE_INVITED_USER, space.getInvitedUsers());
      spaceNode.setProperty(SPACE_TYPE, space.getType());
      spaceNode.setProperty(SPACE_URL, space.getUrl());
      spaceNode.setProperty(SPACE_VISIBILITY, space.getVisibility());
      spaceNode.setProperty(SPACE_REGISTRATION, space.getRegistration());
      spaceNode.setProperty(SPACE_PRIORITY, space.getPriority());
      //  save image to contact
      SpaceAttachment attachment = space.getSpaceAttachment();
      if (attachment != null) {
      // fix load image on IE6 UI
        ExtendedNode extNode = (ExtendedNode)spaceNode;
        if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
        String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
        extNode.setPermission(SystemIdentity.ANY, arrayPers) ;
        List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;   
        for(AccessControlEntry accessControlEntry : permsList) {
          extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
        } 
        
        if (attachment.getFileName() != null) {
          Node nodeFile = null ;
          try {
            nodeFile = spaceNode.getNode("image") ;
          } catch (PathNotFoundException ex) {
            nodeFile = spaceNode.addNode("image", "nt:file");
          }
          Node nodeContent = null ;
          try {
            nodeContent = nodeFile.getNode("jcr:content") ;
          } catch (PathNotFoundException ex) {
            nodeContent = nodeFile.addNode("jcr:content", "nt:resource") ;
          }
          long lastModified = attachment.getLastModified();
          long lastSaveTime = 0;
          if (nodeContent.hasProperty("jcr:lastModified")) 
            lastSaveTime = nodeContent.getProperty("jcr:lastModified").getLong();
          if ((lastModified != 0) && (lastModified != lastSaveTime)) {
            nodeContent.setProperty("jcr:mimeType", attachment.getMimeType()) ;
            nodeContent.setProperty("jcr:data", attachment.getInputStream(session));
            nodeContent.setProperty("jcr:lastModified", attachment.getLastModified());
          }
        }
      } else {
        if(spaceNode.hasNode("image")) {
          spaceNode.getNode("image").remove() ;
          // add 12DEC
          session.save();
        }
      }
      //TODO: dang.tung need review
      if(isNew) spaceHomeNode.save();
      else spaceNode.save();
    } catch (Exception e) {
      // TODO: handle exception
    } finally {
    }
  }

  private Space getSpace(Node spaceNode, Session session) throws Exception{
    Space space = new Space();
    space.setId(spaceNode.getUUID());
    if(spaceNode.hasProperty(SPACE_NAME)) space.setName(spaceNode.getProperty(SPACE_NAME).getString());
    if(spaceNode.hasProperty(SPACE_GROUPID)) space.setGroupId(spaceNode.getProperty(SPACE_GROUPID).getString());
    if(spaceNode.hasProperty(SPACE_APP)) space.setApp(spaceNode.getProperty(SPACE_APP).getString());
    if(spaceNode.hasProperty(SPACE_PARENT)) space.setParent(spaceNode.getProperty(SPACE_PARENT).getString());
    if(spaceNode.hasProperty(SPACE_DESCRIPTION)) space.setDescription(spaceNode.getProperty(SPACE_DESCRIPTION).getString());
    if(spaceNode.hasProperty(SPACE_TAG)) space.setTag(spaceNode.getProperty(SPACE_TAG).getString());
    if(spaceNode.hasProperty(SPACE_PENDING_USER)) space.setPendingUsers(ValuesToStrings(spaceNode.getProperty(SPACE_PENDING_USER).getValues()));
    if(spaceNode.hasProperty(SPACE_INVITED_USER)) space.setInvitedUsers(ValuesToStrings(spaceNode.getProperty(SPACE_INVITED_USER).getValues()));
    if(spaceNode.hasProperty(SPACE_TYPE)) space.setType(spaceNode.getProperty(SPACE_TYPE).getString());
    if(spaceNode.hasProperty(SPACE_URL)) space.setUrl(spaceNode.getProperty(SPACE_URL).getString());
    if(spaceNode.hasProperty(SPACE_VISIBILITY)) space.setVisibility(spaceNode.getProperty(SPACE_VISIBILITY).getString());
    if(spaceNode.hasProperty(SPACE_REGISTRATION)) space.setRegistration(spaceNode.getProperty(SPACE_REGISTRATION).getString());
    if(spaceNode.hasProperty(SPACE_PRIORITY)) space.setPriority(spaceNode.getProperty(SPACE_PRIORITY).getString());
    if(spaceNode.hasNode("image")){
      Node image = spaceNode.getNode("image");
      if (image.isNodeType("nt:file")) {
        SpaceAttachment file = new SpaceAttachment() ;
        file.setId(image.getPath()) ;
        file.setMimeType(image.getNode("jcr:content").getProperty("jcr:mimeType").getString()) ;
        try {
          file.setInputStream(image.getNode("jcr:content").getProperty("jcr:data").getValue().getStream());
        } catch (Exception ex) {
          // TODO: handle exception
//          System.out.println("\n\n\n\n====>>>>>> getSpace err at getValue().getStream\n");
          ex.getStackTrace();
        }
        file.setFileName(image.getName()) ;
        file.setLastModified(image.getNode("jcr:content").getProperty("jcr:lastModified").getLong());
        file.setWorkspace(session.getWorkspace().getName()) ;
        space.setSpaceAttachment(file) ;
      }
    }
    return space;
  }
  
  private String [] ValuesToStrings(Value[] Val) throws Exception {
    if(Val.length == 1) return new String[]{Val[0].getString()};
    String[] Str = new String[Val.length];
    for(int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }
}

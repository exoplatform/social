package org.exoplatform.social.application.impl;

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;

public class SocialGroupEventListenerImpl extends GroupEventListener {

  private SpaceService spaceService;

  public SocialGroupEventListenerImpl() {
    // TODO Auto-generated constructor stub
  }

  /**
   * This method is called before the group is persisted to the database.
   * 
   * @param group The group to be saved
   * @param isNew if the group is a new record in the database or not
   * @throws Exception The developer can decide to throw an exception or not. If
   *           the listener throw an exception, the organization service should
   *           not save/update the group to the database
   */
  public void preSave(Group group, boolean isNew) throws Exception {
  }

  /**
   * This method is called after the group has been saved but not commited yet
   * 
   * @param group The group has been saved.
   * @param isNew if the group is a new record in the database or not
   * @throws Exception The developer can decide to throw the exception or not.
   *           If the method throw an exception. The organization service should
   *           role back the data to the state before the method
   *           GroupHandler.addChild(..) or GroupHandler.saveGroup(..) is
   *           called.
   */
  public void postSave(Group group, boolean isNew) throws Exception {
  }

  /**
   * This method is called before a group should be deleted
   * 
   * @param group the group to be delete
   * @throws Exception The developer can decide to throw the exception or not.
   *           If the method throw an exception. The organization service should
   *           not remove the group record from the database.
   */
  public void preDelete(Group group) throws Exception {
    SpaceService spaceSrv = getSpaceService();
    List<Space> allSpaces = spaceSrv.getAllSpaces();
    String groupId = group.getId();
    String groupIdOfSpace = null;
    for (Space space : allSpaces) {
      groupIdOfSpace = space.getGroupId();
      if (groupId.equals(groupIdOfSpace)) {
        spaceSrv.deleteSpace(space);
      }
    }

    UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
    UIWorkingWorkspace uiWorkSpace = uiPortalApp.getChild(UIWorkingWorkspace.class);
    uiWorkSpace.updatePortletsByName("SpacesToolbarPortlet");
    uiWorkSpace.updatePortletsByName("SocialUserToolBarGroupPortlet");
  }

  /**
   * This method should be called after the group has been removed from the
   * database but not commited yet.
   * 
   * @param group The group has been removed.
   * @throws Exception The developer can decide to throw the exception or not.
   *           If the method throw the exception, the organization service
   *           should role back the database to the state before the method
   *           GroupHandler.removeGroup(..) is called.
   */
  public void postDelete(Group group) throws Exception {
  }

  private SpaceService getSpaceService() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (spaceService == null)
      spaceService = (SpaceService) container.getComponentInstance(SpaceService.class);

    return spaceService;
  }
}

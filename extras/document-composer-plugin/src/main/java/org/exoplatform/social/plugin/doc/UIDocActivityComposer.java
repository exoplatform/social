/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

package org.exoplatform.social.plugin.doc;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ecm.webui.selector.UISelectable;
import org.exoplatform.ecm.webui.tree.selectone.UIOneNodePathSelector;
import org.exoplatform.ecm.webui.utils.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.composer.UIActivityComposer;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * The templateParamsProcessor to process an activity. Replace template
 * key by template value in activity's title.
 * @author    Zun
 * @since     Apr 19, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/plugin/doc/UIDocActivityComposer.gtmpl",
  events = {
    @EventConfig(listeners = UIActivityComposer.CloseActionListener.class),
    @EventConfig(listeners = UIActivityComposer.SubmitContentActionListener.class),
    @EventConfig(listeners = UIActivityComposer.ActivateActionListener.class),
    @EventConfig(listeners = UIDocActivityComposer.SelectDocumentActionListener.class)
  }
)
public class UIDocActivityComposer extends UIActivityComposer implements UISelectable {
  private static final Log LOG = ExoLogger.getLogger(UIDocActivityComposer.class);
  public static final String REPOSITORY = "repository";
  public static final String WORKSPACE = "collaboration";
  private final String POPUP_COMPOSER = "UIPopupComposer";
  private final String docActivityTitle = "Shared document <a href=\"${"+ UIDocActivity.DOCLINK +"}\">${" +UIDocActivity.DOCNAME +"}</a>";

  private String documentRefLink;
  private String rootpath;
  private String documentPath;
  private String documentName;
  private boolean isDocumentReady;

  /**
   * constructor
   */
  public UIDocActivityComposer() {
    resetValues();
  }

  private void resetValues() {
    documentRefLink = "";
    isDocumentReady = false;
    setReadyForPostingActivity(false);
  }

  public boolean isDocumentReady() {
    return isDocumentReady;
  }

  public String getDocumentName() {
    return documentName;
  }

  @Override
  protected void onActivate(Event<UIActivityComposer> event) {
    isDocumentReady = false;
    rootpath = "/Users/"+ event.getRequestContext().getRemoteUser();
    final UIDocActivityComposer docActivityComposer = (UIDocActivityComposer) event.getSource();
    showDocumentPopup(docActivityComposer);
  }

  private void showDocumentPopup(UIDocActivityComposer docActivityComposer) {
    UIComposer uiComposer = docActivityComposer.getAncestorOfType(UIComposer.class);
    UIContainer optionContainer = uiComposer.getOptionContainer();

    UIPopupWindow uiPopup = optionContainer.getChild(UIPopupWindow.class);
    if(uiPopup == null){
      try {
        uiPopup = optionContainer.addChild(UIPopupWindow.class, null, POPUP_COMPOSER);
      } catch (Exception e) {
        LOG.error(e);
      }
    }

    final UIComponent child = uiPopup.getUIComponent();
    if(child != null && child instanceof UIOneNodePathSelector){
      try {
        UIOneNodePathSelector uiOneNodePathSelector = (UIOneNodePathSelector) child;
        uiOneNodePathSelector.init(SessionProviderFactory.createSessionProvider());
        uiPopup.setShow(true);
        uiPopup.setResizable(true);
      } catch (Exception e) {
        LOG.error(e);
      }
    }
    else {
      try {
        uiPopup.setWindowSize(600, 600);

        UIOneNodePathSelector uiOneNodePathSelector = uiPopup.createUIComponent(UIOneNodePathSelector.class, null, "UIOneNodePathSelector");
        uiOneNodePathSelector.setIsDisable(WORKSPACE, true);
        uiOneNodePathSelector.setIsShowSystem(false);
        uiOneNodePathSelector.setAcceptedNodeTypesInPathPanel(new String[] {Utils.NT_FILE});
        uiOneNodePathSelector.setRootNodeLocation(REPOSITORY, WORKSPACE, rootpath);
        uiOneNodePathSelector.init(SessionProviderFactory.createSessionProvider());
        uiPopup.setUIComponent(uiOneNodePathSelector);
        uiOneNodePathSelector.setSourceComponent(this, null);
        uiPopup.setShow(true);
        uiPopup.setResizable(true);
      } catch (Exception e) {
        LOG.error(e);
      }
    }
  }

  @Override
  protected void onClose(Event<UIActivityComposer> event) {
    resetValues();
  }

  @Override
  protected void onSubmit(Event<UIActivityComposer> event) {
  }

  @Override
  public void onPostActivity(PostContext postContext, UIComponent source, WebuiRequestContext requestContext, String postedMessage) throws Exception {
    if(!isDocumentReady){
      requestContext.getUIApplication().addMessage(new ApplicationMessage("You have to choose document first!!!", null, ApplicationMessage.INFO));
    } else {
      Map<String, String> activityParams = new HashMap<String, String>();
      activityParams.put(UIDocActivity.DOCNAME, documentName);
      activityParams.put(UIDocActivity.DOCLINK, documentRefLink);
      activityParams.put(UIDocActivity.DOCPATH, documentPath);
      activityParams.put(UIDocActivity.REPOSITORY, REPOSITORY);
      activityParams.put(UIDocActivity.WORKSPACE, WORKSPACE);
      activityParams.put(UIDocActivity.MESSAGE, postedMessage);

      UIApplication uiApplication = requestContext.getUIApplication();
      if (activityParams.size() == 0) {
        uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message",
                                                      null,
                                                      ApplicationMessage.WARNING));
      } else if(postContext == UIComposer.PostContext.SPACE){
        postActivityToSpace(source, requestContext, activityParams);
      } else if (postContext == UIComposer.PostContext.USER){
        postActivityToUser(source, requestContext, activityParams);
      }
    }
    resetValues();
  }

  private void postActivityToUser(UIComponent source, WebuiRequestContext requestContext, Map<String, String> activityParams) throws Exception {
    UIUserActivitiesDisplay uiUserActivitiesDisplay = (UIUserActivitiesDisplay) getActivityDisplay();

    final UIComposer uiComposer = (UIComposer) source;
    ActivityManager activityManager = uiComposer.getApplicationComponent(ActivityManager.class);
    IdentityManager identityManager = uiComposer.getApplicationComponent(IdentityManager.class);

    String ownerName = uiUserActivitiesDisplay.getOwnerName();
    Identity ownerIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName);

    String remoteUser = requestContext.getRemoteUser();
    ExoSocialActivity activity = saveActivity(activityParams, activityManager, identityManager, ownerIdentity, remoteUser);

    if (uiUserActivitiesDisplay.getSelectedDisplayMode() == UIUserActivitiesDisplay.DisplayMode.MY_STATUS) {
      UIActivitiesContainer activitiesContainer = uiUserActivitiesDisplay.getActivitiesLoader().getActivitiesContainer();
      if (activitiesContainer.getChildren().size() == 1) {
        uiUserActivitiesDisplay.setSelectedDisplayMode(UIUserActivitiesDisplay.DisplayMode.MY_STATUS);
      } else {
        activitiesContainer.addActivity(activity);
        requestContext.addUIComponentToUpdateByAjax(activitiesContainer);
        requestContext.addUIComponentToUpdateByAjax(uiComposer);
      }
    } else{
      uiUserActivitiesDisplay.setSelectedDisplayMode(UIUserActivitiesDisplay.DisplayMode.MY_STATUS);
    }
  }

  private void postActivityToSpace(UIComponent source, WebuiRequestContext requestContext, Map<String, String> activityParams) throws Exception {
    final UIComposer uiComposer = (UIComposer) source;
    ActivityManager activityManager = uiComposer.getApplicationComponent(ActivityManager.class);
    IdentityManager identityManager = uiComposer.getApplicationComponent(IdentityManager.class);

    SpaceService spaceSrv = uiComposer.getApplicationComponent(SpaceService.class);
    Space space = spaceSrv.getSpaceByUrl(SpaceUtils.getSpaceUrl());

    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,space.getName(),false);
    String remoteUser = requestContext.getRemoteUser();
    ExoSocialActivity activity = saveActivity(activityParams, activityManager, identityManager, spaceIdentity, remoteUser);

    UISpaceActivitiesDisplay uiDisplaySpaceActivities = (UISpaceActivitiesDisplay) getActivityDisplay();
    UIActivitiesContainer activitiesContainer = uiDisplaySpaceActivities.getActivitiesLoader().getActivitiesContainer();
    activitiesContainer.addActivity(activity);
    requestContext.addUIComponentToUpdateByAjax(activitiesContainer);
    requestContext.addUIComponentToUpdateByAjax(uiComposer);
  }

  public void doSelect(String selectField, Object value) throws Exception {
    String rawPath = value.toString();
    rawPath = rawPath.substring(rawPath.indexOf(":/") + 2);
    documentRefLink = buildDocumentLink(rawPath);
    documentName = rawPath.substring(rawPath.lastIndexOf("/") + 1);
    documentPath = buildDocumentPath(rawPath);
    isDocumentReady = true;

    documentRefLink = documentRefLink.replace("//", "/");
    documentPath = documentPath.replace("//", "/");
    setReadyForPostingActivity(true);
  }

  private ExoSocialActivity saveActivity(Map<String, String> activityParams, ActivityManager activityManager, IdentityManager identityManager, Identity ownerIdentity, String remoteUser) throws ActivityStorageException {
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteUser);
    ExoSocialActivity activity = new ExoSocialActivityImpl(userIdentity.getId(),
                                     UIDocActivity.ACTIVITY_TYPE,
                                     docActivityTitle,
                                     null);
    activity.setTemplateParams(activityParams);
    activityManager.saveActivity(ownerIdentity, activity);
    return activity;
  }
  
  private String buildDocumentLink(String rawPath) {
    String portalContainerName = PortalContainer.getCurrentPortalContainerName();
    String restContextName = PortalContainer.getCurrentRestContextName();
    String restService = "jcr";
    return new StringBuilder().append("/").append(portalContainerName)
                                            .append("/").append(restContextName)
                                            .append("/").append(restService)
                                            .append("/").append(REPOSITORY)
                                            .append("/").append(WORKSPACE)
                                            .append(rootpath)            
                                            .append("/").append(rawPath).toString();
  }

  public String buildDocumentPath(String rawPath){
    return new StringBuilder().append(rootpath).append("/").append(rawPath).toString();
  }

  public static class SelectDocumentActionListener  extends EventListener<UIDocActivityComposer> {
    @Override
    public void execute(Event<UIDocActivityComposer> event) throws Exception {
      final UIDocActivityComposer docActivityComposer = event.getSource();
      docActivityComposer.rootpath = new StringBuilder().append("/Users/").append(event.getRequestContext().getRemoteUser()).append("/").toString();
      docActivityComposer.showDocumentPopup(docActivityComposer);
    }
  }
}
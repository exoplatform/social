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
package org.exoplatform.social.plugin.link;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.service.rest.LinkShare;
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
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * UIComposerLinkExtension.java
 * <p>
 * an ui component to attach link, gets link information and displays; changes link title,
 * description content inline.
 * </p>
 *
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since     Apr 19, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/plugin/link/UILinkActivityComposer.gtmpl",
  events = {
    @EventConfig(listeners = UILinkActivityComposer.AttachActionListener.class),
    @EventConfig(listeners = UILinkActivityComposer.ChangeLinkContentActionListener.class),
    @EventConfig(listeners = UIActivityComposer.CloseActionListener.class),
    @EventConfig(listeners = UIActivityComposer.SubmitContentActionListener.class),
    @EventConfig(listeners = UIActivityComposer.ActivateActionListener.class)
  }
)
public class UILinkActivityComposer extends UIActivityComposer {
  public static final String LINK_PARAM = "link";
  public static final String IMAGE_PARAM = "image";
  public static final String TITLE_PARAM = "title";
  public static final String DESCRIPTION_PARAM = "description";
  public static final String COMMENT_PARAM = "comment";

  //private static final String MSG_ERROR_ATTACH_LINK = "UIComposerLinkExtension.msg.error.Attach_Link";
  private static final String HTTP = "http://";
  private static final String HTTPS = "https://";
  private LinkShare linkShare_;
  private boolean linkInfoDisplayed_ = false;
  private Map<String, String> templateParams;
  
  /**
   * constructor
   */
  public UILinkActivityComposer() {
    setReadyForPostingActivity(false);
    addChild(new UIFormStringInput("InputLink", "InputLink", null));
  }

  public void setLinkInfoDisplayed(boolean displayed) {
    linkInfoDisplayed_ = displayed;
  }

  public boolean isLinkInfoDisplayed() {
    return linkInfoDisplayed_;
  }

  public void setTemplateParams(Map<String, String> tempParams) {
    templateParams = tempParams;
  }

  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  public void clearLinkShare() {
    linkShare_ = null;
  }

  public LinkShare getLinkShare() {
    return linkShare_;
  }

  /**
   * sets link url to gets content
   * @param url
   * @throws Exception
   */
  private void setLink(String url) throws Exception {
    if (!(url.contains(HTTP) || url.contains(HTTPS))) {
      url = HTTP + url;
    }
    linkShare_ = LinkShare.getInstance(url);
    templateParams = new HashMap<String, String>();
    templateParams.put(LINK_PARAM, linkShare_.getLink());
    String image = "";
    List<String> images = linkShare_.getImages();
    if (images != null && images.size() > 0) {
      image = images.get(0);
    }
    templateParams.put(IMAGE_PARAM, image);
    templateParams.put(TITLE_PARAM, linkShare_.getTitle());
    templateParams.put(DESCRIPTION_PARAM, linkShare_.getDescription());
    setLinkInfoDisplayed(true);
  }

  private boolean isImageLink(String link){
    Pattern pattern = Pattern.compile("(?-i)(\\.jpg|\\.gif|\\.jpeg|\\.bmp|\\.png|\\.tif)$");
    return pattern.matcher(link).find();
  }

  static public class AttachActionListener extends EventListener<UILinkActivityComposer> {

    @Override
    public void execute(Event<UILinkActivityComposer> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      UILinkActivityComposer uiComposerLinkExtension = event.getSource();
      String url = requestContext.getRequestParameter(OBJECTID);
      try {
        uiComposerLinkExtension.setLink(url.trim());
      } catch (Exception e) {
        uiComposerLinkExtension.setReadyForPostingActivity(false);
        // Comment this below line code for temporary fixing issue SOC-1091. Check later.
//        uiApplication.addMessage(new ApplicationMessage(MSG_ERROR_ATTACH_LINK, null, ApplicationMessage.WARNING));
        return;
      }
      requestContext.addUIComponentToUpdateByAjax(uiComposerLinkExtension);
      event.getSource().setReadyForPostingActivity(true);
    }
  }

  static public class ChangeLinkContentActionListener extends EventListener<UILinkActivityComposer> {
    @Override
    public void execute(Event<UILinkActivityComposer> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      UILinkActivityComposer uiComposerLinkExtension = event.getSource();
      Map<String, String> tempParams = new HashMap<String, String>();
      tempParams.put(LINK_PARAM, requestContext.getRequestParameter(LINK_PARAM));
      tempParams.put(IMAGE_PARAM, requestContext.getRequestParameter(IMAGE_PARAM));
      tempParams.put(TITLE_PARAM, requestContext.getRequestParameter(TITLE_PARAM));
      tempParams.put(DESCRIPTION_PARAM, requestContext.getRequestParameter(DESCRIPTION_PARAM));
      uiComposerLinkExtension.setTemplateParams(tempParams);
      requestContext.addUIComponentToUpdateByAjax(uiComposerLinkExtension);
      UIComponent uiParent = uiComposerLinkExtension.getParent();
      if (uiParent != null) {
        uiParent.broadcast(event, event.getExecutionPhase());
      }
    }
  }

  @Override
  protected void onActivate(Event<UIActivityComposer> arg0) {
  }

  @Override
  protected void onClose(Event<UIActivityComposer> arg0) {
    setReadyForPostingActivity(false);
  }

  @Override
  protected void onSubmit(Event<UIActivityComposer> arg0) {
  }

  @Override
  public void onPostActivity(PostContext postContext, UIComponent source, WebuiRequestContext requestContext, String postedMessage) throws Exception {
    final UIComposer uiComposer = (UIComposer) source;
    ActivityManager activityManager = uiComposer.getApplicationComponent(ActivityManager.class);
    IdentityManager identityManager = uiComposer.getApplicationComponent(IdentityManager.class);
    String remoteUser = requestContext.getRemoteUser();
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteUser);

    UIApplication uiApplication = requestContext.getUIApplication();
    Map<String, String> templateParams = getTemplateParams();
    templateParams.put(COMMENT_PARAM, postedMessage);
    setTemplateParams(templateParams);

    if (templateParams.size() == 0) {
      uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message",
                                                    null,
                                                    ApplicationMessage.WARNING));
      return;
    }
    
    String title = "Shared a link: <a href=\"${" + LINK_PARAM + "}\">${" + TITLE_PARAM + "} </a>"; 
    ExoSocialActivity activity = new ExoSocialActivityImpl(userIdentity.getId(),
                                                           UILinkActivity.ACTIVITY_TYPE,
                                                           title,
                                                           null);
    activity.setTemplateParams(templateParams);
    
    if (postContext == UIComposer.PostContext.SPACE) {
      UISpaceActivitiesDisplay uiDisplaySpaceActivities = (UISpaceActivitiesDisplay) getActivityDisplay();
      Space space = uiDisplaySpaceActivities.getSpace();

      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                   space.getName(),
                                                                   false);
      
      activityManager.saveActivity(spaceIdentity, activity);

      UIActivitiesContainer activitiesContainer = uiDisplaySpaceActivities.getActivitiesLoader().getActivitiesContainer();
      activitiesContainer.addActivity(activity);
      requestContext.addUIComponentToUpdateByAjax(activitiesContainer);
      requestContext.addUIComponentToUpdateByAjax(uiComposer);
    } else if (postContext == PostContext.USER) {
      UIUserActivitiesDisplay uiUserActivitiesDisplay = (UIUserActivitiesDisplay) getActivityDisplay();
      String ownerName = uiUserActivitiesDisplay.getOwnerName();
      Identity ownerIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                   ownerName);
      
      activityManager.saveActivity(ownerIdentity, activity);
      
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
  }
}

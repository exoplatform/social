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
package org.exoplatform.social.webui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.composer.UIComposer;
import org.exoplatform.social.webui.composer.UIComposerLinkExtension;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * UIActivity.java
 * <p>
 * Displays activity
 * </p>
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since     Apr 12, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath://groovy/social/webui/UIActivity.gtmpl",
  events = {
    @EventConfig(listeners = UIActivity.ToggleDisplayLikesActionListener.class),
    @EventConfig(listeners = UIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = UIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = UIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = UIActivity.PostCommentActionListener.class)
  }
)
public class UIActivity extends UIForm {
  static private final Log LOG = ExoLogger.getLogger(UIActivity.class);
  static public int LATEST_COMMENTS_SIZE = 2;
  private int commentMinCharactersAllowed_ = 0;
  private int commentMaxCharactersAllowed_ = 0;
  private JSONObject titleData_;
  static public enum Status {
    LATEST("latest"),
    ALL("all"),
    NONE("none");
    public String getStatus() {
      return status_;
    }
    private Status(String status) {
      status_ = status;
    }
    private String status_;
  }

  private Activity activity_;
  private List<Activity> comments_;
  private String[] identityLikes_;
  private ActivityManager activityManager_;
  private IdentityManager identityManager_;
  private boolean commentFormDisplayed_ = false;
  private boolean likesDisplayed_ = false;
  private Status commentListStatus_ = Status.LATEST;
  private boolean allCommentsHidden_ = false;
  private boolean commentFormFocused_ = false;
  /**
   * Constructor
   * @throws Exception
   */
  public UIActivity() throws Exception {
    setSubmitAction("return false;");
  }

  public UIActivity setActivity(Activity activity) {
    activity_ = activity;
    if (activity_ == null) {
      LOG.warn("activity_ is null!");
    }
    init();
    return this;
  }

  public Activity getActivity() {
    return activity_;
  }

  public void setCommentMinCharactersAllowed(int num) {
    commentMinCharactersAllowed_ = num;
  }

  public int getCommentMinCharactersAllowed() {
    return commentMinCharactersAllowed_;
  }

  public void setCommentMaxCharactersAllowed(int num) {
    commentMaxCharactersAllowed_ = num;
  }

  public int getCommentMaxCharactersAllowed() {
    return commentMaxCharactersAllowed_;
  }

  public void setCommentFormDisplayed(boolean commentFormDisplayed) {
    commentFormDisplayed_ = commentFormDisplayed;
  }

  public boolean isCommentFormDisplayed() {
    return commentFormDisplayed_;
  }

  public void setLikesDisplayed(boolean likesDisplayed) {
    likesDisplayed_ = likesDisplayed;
  }

  public boolean isLikesDisplayed() {
    return likesDisplayed_;
  }

  public void setAllCommentsHidden(boolean allCommentsHidden) {
    allCommentsHidden_ = allCommentsHidden;
  }

  public boolean isAllCommentsHidden() {
    return allCommentsHidden_;
  }

  public void setCommentFormFocused(boolean commentFormFocused) {
    commentFormFocused_ = commentFormFocused;
  }

  public boolean isCommentFormFocused() {
    return commentFormFocused_;
  }

  public void setCommentListStatus(Status status) {
    commentListStatus_ = status;
    if (status == Status.ALL) {
      commentFormDisplayed_ = true;
    }
  }

  public Status getCommentListStatus() {
    return commentListStatus_;
  }

  public boolean commentListToggleable() {
    return comments_.size() > LATEST_COMMENTS_SIZE;
  }

  /**
   * Gets all the comments or latest comments or empty list comments
   * Gets latest comments for displaying at the first time
   * if available, returns max LATEST_COMMENTS_SIZE latest comments.
   * @return
   */
  public List<Activity> getComments() {
    if (commentListStatus_ == Status.ALL) {
      return comments_;
    } else if (commentListStatus_ == Status.NONE) {
      return new ArrayList<Activity>();
    } else {
      int commentsSize = comments_.size();
      if (commentsSize > LATEST_COMMENTS_SIZE) {
        return comments_.subList(commentsSize - LATEST_COMMENTS_SIZE, commentsSize);
      }
    }
    return comments_;
  }

  public List<Activity> getAllComments() {
    return comments_;
  }

  public String[] getIdentityLikes() {
    return identityLikes_;
  }

  /**
   * removes currently viewing userId if he liked this activity
   * @return
   * @throws Exception
   */
  public String[] getDisplayedIdentityLikes() throws Exception {
    identityManager_ = getIdentityManager();
    Identity userIdentity = identityManager_.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getRemoteUser());
    if (isLiked()) {
      return (String[]) ArrayUtils.removeElement(identityLikes_, userIdentity.getId());
    }
    return identityLikes_;
  }

  public void setIdenityLikes(String[] identityLikes) {
    identityLikes_ = identityLikes;
  }

  /**
   * Gets user's full name by its userIdentityId
   * @param userIdentityId
   * @return
   * @throws Exception
   */
  public String getUserFullName(String userIdentityId) throws Exception {
    identityManager_ = getIdentityManager();
    Identity userIdentity = identityManager_.getIdentity(userIdentityId, true);
    if (userIdentity == null) {
      return null;
    }
    Profile userProfile = userIdentity.getProfile();
    return userProfile.getFullName();
  }

  /**
   * Gets user profile uri
   * @param userIdentityId
   * @return
   * @throws Exception
   */
  public String getUserProfileUri(String userIdentityId) {

    try {
      identityManager_ = getIdentityManager();
      Identity userIdentity = identityManager_.getIdentity(userIdentityId, true);
      if (userIdentity == null) {
        return "#";
      }

      String url = userIdentity.getProfile().getUrl();
      if (url != null) {
        return url;
      } else {
        return "#";
      }
    } catch (Exception e) {
      return "#";
    }
  }

  /**
   * Gets user's avatar image source by userIdentityId
   * @param userIdentityId
   * @return
   * @throws Exception
   */
  public String getUserAvatarImageSource(String userIdentityId) throws Exception {
    Identity userIdentity = identityManager_.getIdentity(userIdentityId, true);
    if (userIdentity == null) {
      return null;
    }
    Profile userProfile = userIdentity.getProfile();
    return userProfile.getAvatarImageSource();
  }

  /**
   * Gets space's avatar.
   *
   * @return
   * @throws SpaceException
   */
  public String getSpaceImageSource () throws SpaceException {
    Space space = getSpace();
    return space.getImageSource();
  }


  public boolean isSpaceActivity(String id) {
    try {
      identityManager_ = getIdentityManager();
      Identity identity = identityManager_.getIdentity(id, false);
      String remoteId = identity.getRemoteId();
      boolean result = (identityManager_.getIdentity(SpaceIdentityProvider.NAME, remoteId, false) != null);
      return result;
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isUserActivity(String id) throws Exception {
    try {
      identityManager_ = getIdentityManager();
      Identity identity = identityManager_.getIdentity(id, false);
      String remoteId = identity.getRemoteId();
      boolean result = (identityManager_.getIdentity(OrganizationIdentityProvider.NAME,
                                                     remoteId,
                                                     false) != null);
      return result;
    } catch (Exception e) {
      return false;
    }
  }

  public Space getSpace() throws SpaceException {
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    return spaceService.getSpaceByUrl(SpaceUtils.getSpaceUrl());
  }

  public String event(String name, String callback, boolean updateForm) throws Exception {
    if (updateForm) {
      return super.url(name);
    }
    StringBuilder b = new StringBuilder();
    b.append("javascript:eXo.social.webui.UIForm.submitForm('").append(getFormId()).append("','");
    b.append(name).append("',");
    b.append(callback).append(",");
    b.append("true").append(")");
    return b.toString();
  }


  public boolean hasAttachedExtension() {
    try {
      Validate.notNull(activity_.getTitle(), "activity_.getTitle() must not be null.");
      titleData_ = new JSONObject(activity_.getTitle());
      return true;
    } catch (org.json.JSONException je) {
      return false;
    }
  }

  public String getAttachedExtensionType() throws JSONException {
    if (titleData_ != null) {
      return titleData_.getString(UIComposer.EXTENSION_KEY);
    }
    return null;
  }

  public String getLinkTitle() throws JSONException {
    if (titleData_ != null) {
      return ((JSONObject)titleData_.get(UIComposer.DATA_KEY)).getString(UIComposerLinkExtension.TITLE_PARAM);
    }
    return "";
  }

  public String getLinkImage() throws JSONException {
    if (titleData_ != null) {
      return ((JSONObject)titleData_.get(UIComposer.DATA_KEY)).getString(UIComposerLinkExtension.IMAGE_PARAM);
    }
    return "";
  }

  public String getLinkSource() throws JSONException {
    if (titleData_ != null) {
      return ((JSONObject)titleData_.get(UIComposer.DATA_KEY)).getString(UIComposerLinkExtension.LINK_PARAM);
    }
    return "";
  }

  public String getLinkComment() throws JSONException {
    if (titleData_ != null) {
      return titleData_.getString(UIComposer.COMMENT_KEY);
    }
    return "";
  }

  public String getLinkDescription() throws JSONException {
    if (titleData_ != null) {
      return ((JSONObject)titleData_.get(UIComposer.DATA_KEY)).getString(UIComposerLinkExtension.DESCRIPTION_PARAM);
    }
    return "";
  }

  /**
   * Gets prettyTime by timestamp
   * @param timestamp
   * @return
   */
  public String toPrettyTime(long postedTime) {
    //TODO use app resource
    long time = (new Date().getTime() - postedTime) / 1000;
    long value = 0;
    if (time < 60) {
      return "less than a minute ago";
    } else {
      if (time < 120) {
        return "about a minute ago";
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          return "about " + value + " minutes ago";
        } else {
          if (time < 7200) {
            return "about an hour ago";
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              return "about " + value + " hours ago";
            } else {
              if (time < 172800) {
                return "about a day ago";
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  return "about " + value + " days ago";
                } else {
                  if (time < 5184000) {
                    return "about a month ago";
                  } else {
                    value = Math.round(time / 2592000);
                    return "about " + value + " months ago";
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private String getFormId() {
     WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
     if (context instanceof PortletRequestContext) {
        return ((PortletRequestContext)context).getWindowId() + "#" + getId();
     }
     return getId();
  }

  /**
   * Initialize activity's comments; activity's identityId likes list
   */
  private void init() {
    addChild(new UIFormTextAreaInput("CommentTextarea" + activity_.getId(), "CommentTextarea", null));
    if (activity_ != null) {
      activityManager_ = getActivityManager();
      comments_ = activityManager_.getComments(activity_);
      identityLikes_ = activity_.getLikeIdentityIds();
    }
  }

  private void saveComment(String remoteUser, String message) throws Exception {
    activityManager_ = getActivityManager();
    identityManager_ = getIdentityManager();
    Identity userIdentity = identityManager_.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteUser);
    Activity comment = new Activity(userIdentity.getId(), SpaceService.SPACES_APP_ID, message, null);
    activityManager_.saveComment(getActivity(), comment);
    comments_ = activityManager_.getComments(getActivity());
    setCommentListStatus(Status.ALL);
  }

  private void setLike(boolean isLiked, String remoteUser) throws Exception {
    activityManager_ = getActivityManager();
    identityManager_ = getIdentityManager();
    Identity userIdentity = identityManager_.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteUser);
    if (isLiked) {
      activityManager_.saveLike(activity_, userIdentity);
    } else {
      activityManager_.removeLike(activity_, userIdentity);
    }
    activity_ = activityManager_.getActivity(activity_.getId());
    setIdenityLikes(activity_.getLikeIdentityIds());
  }

  /**
   * Checks if this activity is liked by the remote user
   * @return
   * @throws Exception
   */
  public boolean isLiked() throws Exception {
    identityManager_ = getIdentityManager();
    return ArrayUtils.contains(identityLikes_, identityManager_.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getRemoteUser()).getId());
  }

  /**
   * refresh, regets all like, comments of this activity
   */
  private void refresh() {
    activityManager_ = getActivityManager();
    activity_ = activityManager_.getActivity(activity_.getId());
    if (activity_ == null) { //not found -> should render nothing
      LOG.info("activity_ is null, not found. It can be deleted!");
      return;
    }
    comments_ = activityManager_.getComments(activity_);
    identityLikes_ = activity_.getLikeIdentityIds();
  }


  private String getRemoteUser() {
    PortalRequestContext requestContext = Util.getPortalRequestContext();
    return requestContext.getRemoteUser();
  }

  /**
   * Gets activityManager
   * @return
   */
  private ActivityManager getActivityManager() {
    return getApplicationComponent(ActivityManager.class);
  }

  /**
   * Gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager() {
    return getApplicationComponent(IdentityManager.class);
  }


  static public class ToggleDisplayLikesActionListener extends EventListener<UIActivity> {

    @Override
    public void execute(Event<UIActivity> event) throws Exception {
      UIActivity uiActivity = event.getSource();
      uiActivity.refresh();
      if (uiActivity.isLikesDisplayed()) {
        uiActivity.setLikesDisplayed(false);
      } else {
        uiActivity.setLikesDisplayed(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);
    }

  }

  static public class LikeActivityActionListener extends EventListener<UIActivity> {

    @Override
    public void execute(Event<UIActivity> event) throws Exception {
      UIActivity uiActivity = event.getSource();
      uiActivity.refresh();
      WebuiRequestContext requestContext = event.getRequestContext();
      String isLikedStr = requestContext.getRequestParameter(OBJECTID);
      boolean isLiked = Boolean.parseBoolean(isLikedStr);
      uiActivity.setLike(isLiked, requestContext.getRemoteUser());
      requestContext.addUIComponentToUpdateByAjax(uiActivity);
    }

  }

  static public class SetCommentListStatusActionListener extends EventListener<UIActivity> {

    @Override
    public void execute(Event<UIActivity> event) throws Exception {
      UIActivity uiActivity = event.getSource();
      uiActivity.refresh();
      String status = event.getRequestContext().getRequestParameter(OBJECTID);
      Status commentListStatus = null;
      if (status.equals(Status.LATEST.getStatus())) {
        commentListStatus = Status.LATEST;
      } else if (status.equals(Status.ALL.getStatus())) {
        commentListStatus = Status.ALL;
      } else if (status.equals(Status.NONE.getStatus())) {
        commentListStatus = Status.NONE;
      }
      if (commentListStatus != null) {
        uiActivity.setCommentListStatus(commentListStatus);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);
    }
  }

  static public class ToggleDisplayCommentFormActionListener extends EventListener<UIActivity> {

    @Override
    public void execute(Event<UIActivity> event) throws Exception {
      UIActivity uiActivity = event.getSource();
      if (uiActivity.isCommentFormDisplayed()) {
        uiActivity.setCommentFormDisplayed(false);
      } else {
        uiActivity.setCommentFormDisplayed(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);
    }
  }


  static public class PostCommentActionListener extends EventListener<UIActivity> {

    @Override
    public void execute(Event<UIActivity> event) throws Exception {
      UIActivity uiActivity = event.getSource();
      uiActivity.refresh();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIFormTextAreaInput uiFormComment = uiActivity.getChild(UIFormTextAreaInput.class);
      String message = uiFormComment.getValue();
      uiFormComment.reset();
      uiActivity.saveComment(requestContext.getRemoteUser(), message);
      uiActivity.setCommentFormFocused(true);
      requestContext.addUIComponentToUpdateByAjax(uiActivity);

      uiActivity.getParent().broadcast(event, event.getExecutionPhase());
    }
  }
}

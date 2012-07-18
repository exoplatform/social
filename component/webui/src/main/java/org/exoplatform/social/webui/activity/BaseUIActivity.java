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
package org.exoplatform.social.webui.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay.DisplayMode;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Base UI Activity for other custom activity ui to extend for displaying.
 *
 * @author Zun
 * @author <a href="hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jul 23, 2010
 */
public class BaseUIActivity extends UIForm {
  private static final Log LOG = ExoLogger.getLogger(BaseUIActivity.class);

  private static int LATEST_COMMENTS_SIZE = 2;
  private int commentMinCharactersAllowed = 0;
  private int commentMaxCharactersAllowed = 100;

  private static final int DEFAULT_LIMIT = 20;
  
  public static enum CommentStatus {
    LATEST("latest"),    ALL("all"),    NONE("none");
    public String getStatus() {
      return commentStatus;
    }
    private CommentStatus(String status) {
      commentStatus = status;
    }
    private String commentStatus;
  }

  private ExoSocialActivity activity;
  private Identity ownerIdentity;
  private List<ExoSocialActivity> comments;
  private String[] identityLikes;
  private boolean commentFormDisplayed = false;
  private boolean likesDisplayed = false;
  private CommentStatus commentListStatus = CommentStatus.LATEST;
  private boolean allCommentsHidden = false;
  private boolean commentFormFocused = false;

  private static final String HTML_ATTRIBUTE_TITLE = "title";
  
  /**
   * Constructor
   * 
   * @throws Exception
   */
  public BaseUIActivity(){
    //tricktip for gatein bug
    setSubmitAction("return false;");

    comments = new ArrayList<ExoSocialActivity>();
  }

  public void setActivity(ExoSocialActivity activity) {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    this.activity = activity;
    setOwnerIdentity(Utils.getIdentityManager().getIdentity(activity.getUserId(), true));
    UIFormTextAreaInput commentTextArea = new UIFormTextAreaInput("CommentTextarea" + activity.getId(), "CommentTextarea", null);
    commentTextArea.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("BaseUIActivity.label.write_a_comment"));
    addChild(commentTextArea);
    try {
      refresh();
    } catch (ActivityStorageException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  public ExoSocialActivity getActivity() {
    return activity;
  }

  public void setCommentMinCharactersAllowed(int num) {
    commentMinCharactersAllowed = num;
  }

  public int getCommentMinCharactersAllowed() {
    return commentMinCharactersAllowed;
  }

  public void setCommentMaxCharactersAllowed(int num) {
    commentMaxCharactersAllowed = num;
  }

  public int getCommentMaxCharactersAllowed() {
    return commentMaxCharactersAllowed;
  }

  public void setCommentFormDisplayed(boolean commentFormDisplayed) {
    this.commentFormDisplayed = commentFormDisplayed;
  }

  public boolean isCommentFormDisplayed() {
    return commentFormDisplayed;
  }

  public void setLikesDisplayed(boolean likesDisplayed) {
    this.likesDisplayed = likesDisplayed;
  }

  public boolean isLikesDisplayed() {
    return likesDisplayed;
  }

  public void setAllCommentsHidden(boolean allCommentsHidden) {
    this.allCommentsHidden = allCommentsHidden;
  }

  public boolean isAllCommentsHidden() {
    return allCommentsHidden;
  }

  public void setCommentFormFocused(boolean commentFormFocused) {
    this.commentFormFocused = commentFormFocused;
  }

  public boolean isCommentFormFocused() {
    return commentFormFocused;
  }

  public void setCommentListStatus(CommentStatus status) {
    commentListStatus = status;
    if (status == CommentStatus.ALL) {
      commentFormDisplayed = true;
    }
  }

  public CommentStatus getCommentListStatus() {
    return commentListStatus;
  }

  public boolean commentListToggleable() {
    return comments.size() > LATEST_COMMENTS_SIZE;
  }


  /**
   * Gets all the comments or latest comments or empty list comments
   * Gets latest comments for displaying at the first time
   * if available, returns max LATEST_COMMENTS_SIZE latest comments.
   * @return
   */
  public List<ExoSocialActivity> getComments() {
    if (commentListStatus == CommentStatus.ALL) {
      return comments;
    } else if (commentListStatus == CommentStatus.NONE) {
      return new ArrayList<ExoSocialActivity>();
    } else {
      int commentsSize = comments.size();
      if (commentsSize > LATEST_COMMENTS_SIZE) {
        return comments.subList(commentsSize - LATEST_COMMENTS_SIZE, commentsSize);
      }
    }
    return comments;
  }

  public List<ExoSocialActivity> getAllComments() {
    return comments;
  }

  public String[] getIdentityLikes() {
    return identityLikes;
  }

  /**
   * Removes currently viewing userId if he liked this activity.
   * @return
   * @throws Exception
   */
  public String[] getDisplayedIdentityLikes() throws Exception {
    if (isLiked()) {
      return (String[]) ArrayUtils.removeElement(identityLikes, Utils.getViewerIdentity().getId());
    }
    return identityLikes;
  }

  public void setIdenityLikes(String[] identityLikes) {
    this.identityLikes = identityLikes;
  }

  public String event(String actionName, String callback, boolean updateForm) throws Exception {
    if (updateForm) {
      return super.url(actionName);
    }
    StringBuilder b = new StringBuilder();
    b.append("javascript:eXo.webui.UIForm.submitForm('").append(getFormId()).append("','");
    b.append(actionName).append("',");
    b.append("true").append(",");
    b.append(callback).append(")");
    return b.toString();
  }

  /**
   * Gets prettyTime by timestamp.
   * 
   * @param resourceBundle
   * @param postedTime
   * @return String
   */
  public String getPostedTimeString(WebuiBindingContext resourceBundle, long postedTime) throws Exception {
    long time = (new Date().getTime() - postedTime) / 1000;
    long value;
    if (time < 60) {
      return resourceBundle.appRes("UIActivity.label.Less_Than_A_Minute");
    } else {
      if (time < 120) {
        return resourceBundle.appRes("UIActivity.label.About_A_Minute");
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          return resourceBundle.appRes("UIActivity.label.About_?_Minutes").
                  replaceFirst("\\{0\\}", String.valueOf(value));
        } else {
          if (time < 7200) {
            return resourceBundle.appRes("UIActivity.label.About_An_Hour");
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              return resourceBundle.appRes("UIActivity.label.About_?_Hours").
                      replaceFirst("\\{0\\}", String.valueOf(value));
            } else {
              if (time < 172800) {
                return resourceBundle.appRes("UIActivity.label.About_A_Day");
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  return resourceBundle.appRes("UIActivity.label.About_?_Days").
                          replaceFirst("\\{0\\}", String.valueOf(value));
                } else {
                  if (time < 5184000) {
                    return resourceBundle.appRes("UIActivity.label.About_A_Month");
                  } else {
                    value = Math.round(time / 2592000);
                    return resourceBundle.appRes("UIActivity.label.About_?_Months").
                            replaceFirst("\\{0\\}", String.valueOf(value));
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * Gets prettyTime by timestamp of activities in space.
   * 
   * @param resourceBundle
   * @param postedTime
   * @return String
   */
  public String getPostedTimeInSpaceString(WebuiBindingContext resourceBundle, long postedTime) throws Exception {
    long time = (new Date().getTime() - postedTime) / 1000;
    long value;
    if (time < 60) {
      return resourceBundle.appRes("UIActivity.label.Less_Than_A_Minute_In_Space");
    } else {
      if (time < 120) {
        return resourceBundle.appRes("UIActivity.label.About_A_Minute_In_Space");
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          return resourceBundle.appRes("UIActivity.label.About_?_Minutes_In_Space").
                  replaceFirst("\\{0\\}", String.valueOf(value));
        } else {
          if (time < 7200) {
            return resourceBundle.appRes("UIActivity.label.About_An_Hour_In_Space");
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              return resourceBundle.appRes("UIActivity.label.About_?_Hours_In_Space").
                      replaceFirst("\\{0\\}", String.valueOf(value));
            } else {
              if (time < 172800) {
                return resourceBundle.appRes("UIActivity.label.About_A_Day_In_Space");
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  return resourceBundle.appRes("UIActivity.label.About_?_Days_In_Space").
                          replaceFirst("\\{0\\}", String.valueOf(value));
                } else {
                  if (time < 5184000) {
                    return resourceBundle.appRes("UIActivity.label.About_A_Month_In_Space");
                  } else {
                    value = Math.round(time / 2592000);
                    return resourceBundle.appRes("UIActivity.label.About_?_Months_In_Space").
                            replaceFirst("\\{0\\}", String.valueOf(value));
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

  protected void saveComment(String remoteUser, String message) throws Exception {
    ExoSocialActivity comment = new ExoSocialActivityImpl(Utils.getViewerIdentity().getId(),
            SpaceService.SPACES_APP_ID, message, null);
    Utils.getActivityManager().saveComment(getActivity(), comment);
    RealtimeListAccess<ExoSocialActivity> activityCommentsListAccess = Utils.getActivityManager().getCommentsWithListAccess(getActivity());
    comments = activityCommentsListAccess.loadAsList(0, DEFAULT_LIMIT);
    setCommentListStatus(CommentStatus.ALL);
  }

  protected void setLike(boolean isLiked, String remoteUser) throws Exception {
    Identity viewerIdentity = Utils.getViewerIdentity();
    activity.setBody(null);
    activity.setTitle(null);
    if (isLiked) {
      Utils.getActivityManager().saveLike(activity, viewerIdentity);
    } else {
      Utils.getActivityManager().deleteLike(activity, viewerIdentity);
    }
    activity = Utils.getActivityManager().getActivity(activity.getId());
    setIdenityLikes(activity.getLikeIdentityIds());
    activity = getI18N(activity);
  }

  /**
   * Checks if this activity is liked by the remote user.
   * 
   * @return
   * @throws Exception
   */
  public boolean isLiked() throws Exception {
    return ArrayUtils.contains(identityLikes, Utils.getViewerIdentity().getId());
  }

  /**
   * Refresh, regets all likes, comments of this activity.
   */
  protected void refresh() throws ActivityStorageException {
    activity = Utils.getActivityManager().getActivity(activity.getId());
    activity = getI18N(activity);
    if (activity == null) { //not found -> should render nothing
      LOG.info("activity is null, not found. It can be deleted!");
      return;
    }
    RealtimeListAccess<ExoSocialActivity> activityCommentsListAccess = Utils.getActivityManager().getCommentsWithListAccess(activity);
    comments = activityCommentsListAccess.loadAsList(0, DEFAULT_LIMIT);
    identityLikes = activity.getLikeIdentityIds();
  }

  public boolean isUserActivity() {
    boolean isUserActivity = false;
    if (getOwnerIdentity() != null) {
      isUserActivity = getOwnerIdentity().getProviderId().equals(OrganizationIdentityProvider.NAME);
    }
    return isUserActivity;
  }
  
  public boolean isSpaceActivity() {
    boolean isSpaceActivity = false;
    if (getOwnerIdentity() != null) {
      isSpaceActivity = getOwnerIdentity().getProviderId().equals(SpaceIdentityProvider.NAME);
    }
    return isSpaceActivity;
  }  

  public boolean isActivityDeletable() throws SpaceException {
    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();
    if (postContext == PostContext.SPACE) {
      Space space = uiActivitiesContainer.getSpace();
      SpaceService spaceService = getApplicationComponent(SpaceService.class);

      if (Utils.getViewerIdentity().equals(getOwnerIdentity())) {
        return true;
      }

      return spaceService.isLeader(space, Utils.getOwnerRemoteId());
    } else if (postContext == PostContext.USER) {
      UIUserActivitiesDisplay uiUserActivitiesDisplay = getAncestorOfType(UIUserActivitiesDisplay.class);
      if (Utils.getViewerIdentity().equals(getOwnerIdentity())) {
        return true;
      }
      if (uiUserActivitiesDisplay != null && uiUserActivitiesDisplay.isActivityStreamOwner()) {
        if (uiUserActivitiesDisplay.getSelectedDisplayMode() == DisplayMode.MY_STATUS) {
          return true;
        } else if (uiUserActivitiesDisplay.getSelectedDisplayMode() == DisplayMode.SPACE_UPDATES) {
          //currently displays only
          return false;
        } else {
          //connections
          return false;
        }
      }
    }
    return false;
  }

  public boolean isActivityCommentAndLikable() throws Exception {
    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();
    if (postContext == PostContext.USER) {
      UIUserActivitiesDisplay uiUserActivitiesDisplay = getAncestorOfType(UIUserActivitiesDisplay.class);
      if (uiUserActivitiesDisplay != null && !uiUserActivitiesDisplay.isActivityStreamOwner()) {
        String ownerName = uiUserActivitiesDisplay.getOwnerName();
        Identity ownerIdentity = Utils.getIdentityManager().
                getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName, false);
        Relationship relationship = Utils.getRelationshipManager().get(ownerIdentity, Utils.getViewerIdentity());
        if (relationship == null) {
          return false;
        } else if (!(relationship.getStatus() == Type.CONFIRMED)) {
          return false;
        }
      }
    }
    return true;
  }

  public boolean isCommentDeletable(String activityUserId) throws SpaceException {
    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();
    try {
      if (Utils.getViewerIdentity().getId().equals(activityUserId)) {
        return true;
      }
      if (postContext == PostContext.SPACE) {
        Space space = uiActivitiesContainer.getSpace();
        SpaceService spaceService = getApplicationComponent(SpaceService.class);
        return spaceService.isLeader(space, Utils.getOwnerRemoteId());
      } else if (postContext == PostContext.USER) {
        UIUserActivitiesDisplay uiUserActivitiesDisplay = getAncestorOfType(UIUserActivitiesDisplay.class);
        if (uiUserActivitiesDisplay != null && uiUserActivitiesDisplay.isActivityStreamOwner()) {
          if (uiUserActivitiesDisplay.getSelectedDisplayMode() == DisplayMode.MY_STATUS) {
            return true;
          } else if (uiUserActivitiesDisplay.getSelectedDisplayMode() == DisplayMode.SPACE_UPDATES) {
            return false;
          } else {
            //connections
            return false;
          }
        }
      }
    } catch (Exception e) {
      LOG.warn("can't not get remoteUserIdentity: remoteUser = " + Utils.getViewerRemoteId());
    }
    return false;
  }

  public void setOwnerIdentity(Identity ownerIdentity) {
    this.ownerIdentity = ownerIdentity;
  }

  public Identity getOwnerIdentity() {
    return ownerIdentity;
  }

  /**
   * Checks stream owner is space or user.
   * 
   * @return
   * @since 1.2.2
   */
  protected boolean isSpaceStreamOwner() {
    Identity identityStreamOwner = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, 
                                                                                  this.getActivity().getStreamOwner(), false);
    return (identityStreamOwner != null);
  }
  
  /**
   * Checks this activity is child of UISpaceActivitiesDisplay or not.
   * 
   * @return
   * @since 1.2.2
   */
  protected boolean isUISpaceActivitiesDisplay() {
    return (getParent().getParent().getParent() instanceof UISpaceActivitiesDisplay);
  }
  
  public static class ToggleDisplayLikesActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      uiActivity.refresh();
      if (uiActivity.isLikesDisplayed()) {
        uiActivity.setLikesDisplayed(false);
      } else {
        uiActivity.setLikesDisplayed(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);
    }
  }

  public static class LikeActivityActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      uiActivity.refresh();
      WebuiRequestContext requestContext = event.getRequestContext();
      String isLikedStr = requestContext.getRequestParameter(OBJECTID);
      boolean isLiked = Boolean.parseBoolean(isLikedStr);
      uiActivity.setLike(isLiked, requestContext.getRemoteUser());
      requestContext.addUIComponentToUpdateByAjax(uiActivity);
    }
  }

  public static class SetCommentListStatusActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      uiActivity.refresh();
      String status = event.getRequestContext().getRequestParameter(OBJECTID);
      CommentStatus commentListStatus = null;
      if (status.equals(CommentStatus.LATEST.getStatus())) {
        commentListStatus = CommentStatus.LATEST;
      } else if (status.equals(CommentStatus.ALL.getStatus())) {
        commentListStatus = CommentStatus.ALL;
      } else if (status.equals(CommentStatus.NONE.getStatus())) {
        commentListStatus = CommentStatus.NONE;
      }
      if (commentListStatus != null) {
        uiActivity.setCommentListStatus(commentListStatus);
      }
      uiActivity.setCommentFormDisplayed(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);
    }
  }

  public static class ToggleDisplayCommentFormActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      if (uiActivity.isCommentFormDisplayed()) {
        uiActivity.setCommentFormDisplayed(false);
      } else {
        uiActivity.setCommentFormDisplayed(true);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);
    }
  }

  public static class PostCommentActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
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

  public static class DeleteActivityActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      Utils.getActivityManager().deleteActivity(uiActivity.getActivity().getId());
      UIActivitiesContainer activitiesContainer = uiActivity.getParent();
      activitiesContainer.removeChildById(uiActivity.getId());
      activitiesContainer.removeActivity(uiActivity.getActivity());

      event.getRequestContext().addUIComponentToUpdateByAjax(activitiesContainer);
    }
  }


  public static class DeleteCommentActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      Utils.getActivityManager().deleteComment(uiActivity.getActivity().getId(),
                                               requestContext.getRequestParameter(OBJECTID));
      uiActivity.refresh();
      requestContext.addUIComponentToUpdateByAjax(uiActivity);
    }
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    try {
      super.processRender(context);
    }
    catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private ExoSocialActivity getI18N(ExoSocialActivity activity) {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    I18NActivityProcessor i18NActivityProcessor = getApplicationComponent(I18NActivityProcessor.class);
    if (activity.getTitleId() != null) {
      Locale userLocale = requestContext.getLocale();
      activity = i18NActivityProcessor.process(activity, userLocale);
    }
    return activity;
  }
}

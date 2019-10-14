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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.DateUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.SpaceActivityPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.social.webui.space.UISpaceActivitiesDisplay;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormTextAreaInput;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

/**
 * Base UI Activity for other custom activity ui to extend for displaying.
 *
 * @author Zun
 * @author <a href="hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jul 23, 2010
 */
public class BaseUIActivity extends UIForm {
  protected static final int LIKES_NUM_DEFAULT           = 0;
  private static final Log   LOG                         = ExoLogger.getLogger(BaseUIActivity.class);
  private static final int   DEFAULT_LIMIT               = 10;
  private static final String                   HTML_AT_SYMBOL_PATTERN = "@";
  private static final String                   HTML_AT_SYMBOL_ESCAPED_PATTERN = "&#64;";
  private static final String                   HTML_ATTRIBUTE_TITLE = "title";

  public static final String TEMPLATE_PARAM_COMMENT = "comment";
  public static final String COMPOSER_TEXT_AREA_EDIT_INPUT = "composerEditInput";
  private static int         LATEST_COMMENTS_SIZE        = 2;
  private int                commentMinCharactersAllowed = 0;
  private int                commentMaxCharactersAllowed = 100;
  private int                commentSize                 = 0;
  private int                                   loadCapacity;

  private int                                   currentLoadIndex     = 0;

  private RealtimeListAccess<ExoSocialActivity> activityCommentsListAccess;

  private ExoSocialActivity                     activity;

  private Identity                              ownerIdentity;

  private String[]                              identityLikes;

  private boolean                               commentFormDisplayed = false;

  private boolean                               allLoaded            = false;

  private CommentStatus                         commentListStatus    = CommentStatus.LATEST;

  private boolean                               allCommentsHidden    = false;

  private boolean                               commentFormFocused   = false;

  private String                                updatedCommentId;

  private ActivityManager activityManager;

  /**
   * Constructor
   */
  public BaseUIActivity() {
    // tricktip for gatein bug
    setSubmitAction("return false;");
  }

  public RealtimeListAccess<ExoSocialActivity> getActivityCommentsListAccess() {
    return activityCommentsListAccess;
  }

  public void setActivityCommentsListAccess(RealtimeListAccess<ExoSocialActivity> activityCommentsListAccess) {
    this.activityCommentsListAccess = activityCommentsListAccess;
    commentSize = activityCommentsListAccess.getSize();
  }

  public String getSpaceGroupId() {
    Space space = SpaceUtils.getSpaceByContext();
    return space == null ? "" : space.getGroupId();
  }

  public String getSpaceURL() {
    String spaceURL = SpaceUtils.getSpaceUrlByContext();
    return spaceURL;
  }
  
  public int getCurrentLoadIndex() {
    return currentLoadIndex;
  }

  public int getLoadCapacity() {
    return loadCapacity;
  }

  public void setLoadCapacity(int loadCapacity) {
    this.loadCapacity = loadCapacity;
  }

  public ExoSocialActivity getActivity() {
    return activity;
  }

  public void setActivity(ExoSocialActivity activity) {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    this.activity = activity;
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getUserId(), true);
    setOwnerIdentity(identity);

    UIFormTextAreaInput commentTextArea = new UIFormTextAreaInput("CommentTextarea" + activity.getId(), "CommentTextarea", null);
    commentTextArea.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("BaseUIActivity.label.Add_your_comment"));
    addChild(commentTextArea);
    //add textbox for inputting message
    UIFormTextAreaInput messageInput = new UIFormTextAreaInput(COMPOSER_TEXT_AREA_EDIT_INPUT + activity.getId(), COMPOSER_TEXT_AREA_EDIT_INPUT, null);
    addChild(messageInput);

    try {
      refresh();
    } catch (ActivityStorageException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  public int getCommentMinCharactersAllowed() {
    return commentMinCharactersAllowed;
  }

  public void setCommentMinCharactersAllowed(int num) {
    commentMinCharactersAllowed = num;
  }

  public int getCommentMaxCharactersAllowed() {
    return commentMaxCharactersAllowed;
  }

  public void setCommentMaxCharactersAllowed(int num) {
    commentMaxCharactersAllowed = num;
  }

  public boolean isCommentFormDisplayed() {
    return commentFormDisplayed;
  }

  public void setCommentFormDisplayed(boolean commentFormDisplayed) {
    this.commentFormDisplayed = commentFormDisplayed;
  }

  public boolean isAllLoaded() {
    return allLoaded;
  }

  public void setAllLoaded(boolean allLoaded) {
    this.allLoaded = allLoaded;
  }

  public boolean isAllCommentsHidden() {
    return allCommentsHidden;
  }

  public void setAllCommentsHidden(boolean allCommentsHidden) {
    this.allCommentsHidden = allCommentsHidden;
  }

  public boolean isCommentFormFocused() {
    return commentFormFocused;
  }

  public void setCommentFormFocused(boolean commentFormFocused) {
    this.commentFormFocused = commentFormFocused;
  }

  public CommentStatus getCommentListStatus() {
    return commentListStatus == null ? CommentStatus.NONE : commentListStatus;
  }

  public void setCommentListStatus(CommentStatus status) {
    commentListStatus = status;
    if (status == CommentStatus.ALL) {
      commentFormDisplayed = true;
    }
  }

  public boolean commentListToggleable() {
    return commentSize > LATEST_COMMENTS_SIZE;
  }

  /**
   * Gets all the comments or latest comments or empty list comments Gets latest
   * comments for displaying at the first time if available, returns max
   * LATEST_COMMENTS_SIZE latest comments.
   *
   * @return
   */
  public List<ExoSocialActivity> getComments() {
    if (commentSize == 0)
      return new ArrayList<ExoSocialActivity>();
    //
    List<ExoSocialActivity> comments = new ArrayList<ExoSocialActivity>();
    if (commentListStatus == CommentStatus.ALL) {
      if (currentLoadIndex == 0) {
        currentLoadIndex = commentSize - DEFAULT_LIMIT - LATEST_COMMENTS_SIZE;
        loadCapacity = DEFAULT_LIMIT + LATEST_COMMENTS_SIZE;
      } else {
        currentLoadIndex -= DEFAULT_LIMIT;
      }
      if (currentLoadIndex < 0)
        currentLoadIndex = 0;
      comments = activityCommentsListAccess.loadAsList(currentLoadIndex, loadCapacity);
      if (currentLoadIndex > 0) {
        loadCapacity += currentLoadIndex;
      }
    } else if (commentListStatus == CommentStatus.NONE) {
      return comments != null ? comments : new ArrayList<ExoSocialActivity>();
    } else {
      if (commentSize > LATEST_COMMENTS_SIZE) {
        comments = activityCommentsListAccess.loadAsList(commentSize - LATEST_COMMENTS_SIZE, LATEST_COMMENTS_SIZE);
      } else {
        comments = activityCommentsListAccess.loadAsList(0, commentSize >= DEFAULT_LIMIT ? DEFAULT_LIMIT : commentSize);
      }
    }
    return getI18N(comments);
  }

  /**
   * Count the list of parent comments of activity
   * 
   * @param comments
   * @return
   */
  public int getParentCommentsSize(List<ExoSocialActivity> comments) {
    int count = 0;
    for (ExoSocialActivity exoSocialActivity : comments) {
      if (exoSocialActivity.getParentCommentId() == null) {
        count++;
      }
    }
    return count;
  }

  /**
   * Count the list of sub comment of comment designed by id
   * 
   * @param comments
   * @param commentActivityId
   * @return
   */
  public int getSubCommentsSize(List<ExoSocialActivity> comments, String commentActivityId) {
    if (StringUtils.isBlank(commentActivityId)) {
      return 0;
    }
    int count = 0;
    for (ExoSocialActivity exoSocialActivity : comments) {
      if (StringUtils.equals(exoSocialActivity.getParentCommentId(), commentActivityId)) {
        count++;
      }
    }
    return count;
  }

  public boolean isSubCommentOfComment(List<ExoSocialActivity> comments, String commentId, String subCommentId) {
    if (StringUtils.isBlank(commentId) || StringUtils.isBlank(subCommentId)) {
      return false;
    }
    for (ExoSocialActivity exoSocialActivity : comments) {
      if (StringUtils.equals(exoSocialActivity.getId(), subCommentId)
          && StringUtils.equals(exoSocialActivity.getParentCommentId(), commentId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Don't use this method what you want to get the comments's size. You could
   * use the new method for this stuff: getAllCommentSize() method
   *
   * @return
   */
  @Deprecated
  public List<ExoSocialActivity> getAllComments() {
    return activityCommentsListAccess.loadAsList(0, activityCommentsListAccess.getSize());
  }

  /**
   * Gets number of comments of the specified activity
   *
   * @return
   */
  public int getAllCommentSize() {
    return activityCommentsListAccess.getSize();
  }

  public String[] getIdentityLikes() {
    return identityLikes;
  }

  /**
   * Gets likes to display
   *
   * @return
   * @throws Exception
   */
  public String[] getDisplayedIdentityLikes() throws Exception {
    ArrayUtils.reverse(identityLikes);
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

  public String getAbsolutePostedTime(Long postedTime) {
    Locale currentLocale = Util.getPortalRequestContext().getLocale();
    DateTimeFormatter df = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT).withLocale(currentLocale).withZone(ZoneId.systemDefault());
    return df.format(Instant.ofEpochMilli(postedTime));
  }

  /**
   * @deprecated use {@link BaseUIActivity#getRelativeTimeLabel} instead. This is kept for backward compatibility
   * @param webuiBindingContext WebUI binding context
   * @param postedTime posted time in milliseconds
   * @return label of relative posted time
   */
  @Deprecated
  public String getPostedTimeString(WebuiBindingContext webuiBindingContext, long postedTime) throws Exception {
    return getRelativeTimeLabel(webuiBindingContext, postedTime);
  }

  public String getRelativeTimeLabel(WebuiBindingContext webuiBindingContext, long postedTime) {
    Locale locale = webuiBindingContext.getRequestContext().getLocale();
    return DateUtils.getRelativeTimeLabel(locale, postedTime);
  }

  private String getFormId() {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    if (context instanceof PortletRequestContext) {
      return ((PortletRequestContext) context).getWindowId() + "#" + getId();
    }
    return getId();
  }

  protected ExoSocialActivity saveComment(String remoteUser, String message, String commentId) throws Exception {
    ExoSocialActivity comment = new ExoSocialActivityImpl(Utils.getViewerIdentity().getId(),
                                                          SpaceActivityPublisher.SPACE_APP_ID,
                                                          message,
                                                          null);
    if (StringUtils.isNotBlank(commentId) && commentId.startsWith("comment")) {
      comment.setParentCommentId(commentId);
    }
    Utils.getActivityManager().saveComment(getActivity(), comment);
    activityCommentsListAccess = Utils.getActivityManager().getCommentsWithListAccess(getActivity(), true);
    commentSize = activityCommentsListAccess.getSize();
    currentLoadIndex = 0;
    this.updatedCommentId = commentId;
    setCommentListStatus(CommentStatus.ALL);
    return comment;
  }

  /**
   *
   * @param message edited message
   */
  protected void editActivity(String message){
    getActivity().setTitle(message);
    getActivity().setUpdated(new Date().getTime());
    Utils.getActivityManager().updateActivity(getActivity());
  }

  /**
   *
   * @param commentActivity edited comment's activity
   * @param message chnaged message
   * @return
   */
  protected ExoSocialActivity editCommentMessage(ExoSocialActivity commentActivity, String message){
    commentActivity.setTitle(message);
    commentActivity.setUpdated(new Date().getTime());
    Utils.getActivityManager().saveComment(getActivity(), commentActivity);
    activityCommentsListAccess = Utils.getActivityManager().getCommentsWithListAccess(getActivity(), true);
    commentSize = activityCommentsListAccess.getSize();
    currentLoadIndex = 0;
    this.updatedCommentId = commentActivity.getId();
    return commentActivity;

  }

  public void setLike(boolean isLiked) throws Exception {
    Identity viewerIdentity = Utils.getViewerIdentity();
    activity = Utils.getActivityManager().getActivity(activity.getId());
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

  public void setLikeComment(boolean isLiked, String commentId){
    Identity viewerIdentity = Utils.getViewerIdentity();
    ExoSocialActivity commentActivity = Utils.getActivityManager().getActivity(commentId);
    if (isLiked) {
      Utils.getActivityManager().saveLike(commentActivity, viewerIdentity);
    } else {
      Utils.getActivityManager().deleteLike(commentActivity, viewerIdentity);
    }
    this.updatedCommentId = commentActivity.getParentCommentId();
  }

  public String getAndSetUpdatedCommentId(String newValue) {
    String updatedCommentId = this.updatedCommentId;
    this.updatedCommentId = newValue;
    return updatedCommentId;
  }

  protected String getActivityPermalink(String activityId) {
    return LinkProvider.getSingleActivityUrl(activityId);
  }

  protected String getCommentPermalink(String activityId, String commentId) {
    return LinkProvider.getSingleActivityUrl(activityId) + "&commentId=" + commentId;
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
    // activity = Utils.getActivityManager().getActivity(activity.getId());
    activity = getI18N(activity);
    if (activity == null) { // not found -> should render nothing
      LOG.info("activity is null, not found. It can be deleted!");
      return;
    }
    activityCommentsListAccess = Utils.getActivityManager().getCommentsWithListAccess(activity, true);
    commentSize = activityCommentsListAccess.getSize();
    identityLikes = activity.getLikeIdentityIds();

    // init single activity : focus to comment's box or expand all comments
    initSingleActivity();
  }

  private void initSingleActivity() {
    PostContext postContext = (PostContext) WebuiRequestContext.getCurrentInstance()
                                                               .getAttribute(UIActivitiesLoader.ACTIVITY_POST_CONTEXT_KEY);
    if (postContext == null) {
      postContext = getAncestorOfType(UIPortletApplication.class).findFirstComponentOfType(UIActivitiesLoader.class)
                                                                 .getPostContext();
    }
    if (postContext == PostContext.SINGLE) {
      if (!Utils.isExpandLikers() && !Utils.isFocusCommentBox()) {
        // expand all comments
        setCommentListStatus(CommentStatus.ALL);
      } else {
        setCommentListStatus(CommentStatus.LATEST);
      }
    }
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

    if (Utils.getViewerIdentity().equals(getOwnerIdentity())) {
      return true;
    }

    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    Space space = null;

    if (postContext == PostContext.SPACE) {
      space = uiActivitiesContainer.getSpace();
    } else if(org.exoplatform.social.core.activity.model.ActivityStream.Type.SPACE.equals(this.getActivity().getActivityStream().getType())) {
      Identity identityStreamOwner = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                                    this.getActivity().getStreamOwner(),
                                                                                    false);
      if (identityStreamOwner != null) {
        space = spaceService.getSpaceByPrettyName(identityStreamOwner.getRemoteId());
      }
    }

    if (space != null) {
      return spaceService.isManager(space, Utils.getOwnerRemoteId());
    }

    return false;
  }

  public boolean isActivityEditable(ExoSocialActivity activity) {
    return getActivityManager().isActivityEditable(activity, ConversationState.getCurrent().getIdentity());
  }

  private ActivityManager getActivityManager() {
    if (activityManager == null) {
      activityManager = this.getApplicationComponent(ActivityManager.class);
    }
    return activityManager;
  }

  public boolean isActivityCommentAndLikable() throws Exception {
    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();

    //
    if (postContext == PostContext.USER) {
      // base on SOC-3117
      // UIUserActivitiesDisplay uiUserActivitiesDisplay =
      // getAncestorOfType(UIUserActivitiesDisplay.class);
      // if (uiUserActivitiesDisplay != null &&
      // !uiUserActivitiesDisplay.isActivityStreamOwner()) {
      // String ownerName = uiUserActivitiesDisplay.getOwnerName();
      // Identity ownerIdentity = Utils.getIdentityManager().
      // getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName,
      // false);
      // Relationship relationship =
      // Utils.getRelationshipManager().get(ownerIdentity,
      // Utils.getViewerIdentity());
      // if (relationship == null) {
      // return false;
      // } else if (!(relationship.getStatus() == Type.CONFIRMED)) {
      // return false;
      // }
      // }
      return true;
    }
    return true;
  }

  public boolean isActivityCommentable() throws Exception {
    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();

    //
    if (getActivity().isLocked()) {
      return false;
    }

    //
    if (postContext == PostContext.USER) {
      UIUserActivitiesDisplay uiUserActivitiesDisplay = getAncestorOfType(UIUserActivitiesDisplay.class);
      if (uiUserActivitiesDisplay != null && !uiUserActivitiesDisplay.isActivityStreamOwner()) {
        String ownerName = uiUserActivitiesDisplay.getOwnerName();
        Identity ownerIdentity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                ownerName,
                false);
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
      Space space = null;
      SpaceService spaceService = getApplicationComponent(SpaceService.class);

      if (postContext == PostContext.SPACE) {
        space = uiActivitiesContainer.getSpace();
        spaceService = getApplicationComponent(SpaceService.class);
      } else if(org.exoplatform.social.core.activity.model.ActivityStream.Type.SPACE.equals(this.getActivity().getActivityStream().getType())) {
        Identity identityStreamOwner = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                                      this.getActivity().getStreamOwner(),
                                                                                      false);
        if (identityStreamOwner != null) {
          space = spaceService.getSpaceByPrettyName(identityStreamOwner.getRemoteId());
        }
      }

      if (space != null) {
        return spaceService.isManager(space, Utils.getOwnerRemoteId());
      }
    } catch (Exception e) {
      LOG.warn("can't not get remoteUserIdentity: remoteUser = " + Utils.getViewerRemoteId());
    }
    return false;
  }

  public Identity getOwnerIdentity() {
    return ownerIdentity;
  }

  public void setOwnerIdentity(Identity ownerIdentity) {
    this.ownerIdentity = ownerIdentity;
  }

  public Identity getSpaceCreatorIdentity() {

    // If an activity of space then set creator information
    if (SpaceIdentityProvider.NAME.equals(ownerIdentity.getProviderId())) {
      String spaceCreator = activity.getTemplateParams().get(Space.CREATOR);

      if (spaceCreator != null) {
        return Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, spaceCreator, false);
      }

      SpaceService spaceService = getApplicationComponent(SpaceService.class);
      Space space = spaceService.getSpaceByPrettyName(ownerIdentity.getRemoteId());

      if (space == null) {
        return ownerIdentity;
      } else {
        String[] managers = space.getManagers();
        return Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, managers[0], false);
      }
    }

    return null;
  }

  /**
   * Checks stream owner is space or user.
   *
   * @return
   * @since 1.2.2
   */
  protected boolean isSpaceStreamOwner() {
    return this.getActivity().getActivityStream().getType().name().equalsIgnoreCase(SpaceIdentityProvider.NAME);
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

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    try {
      Utils.initUserProfilePopup(getId());
      String focusActivityId = Utils.getActivityID();
      String focusCommentID = Utils.getCommentID();
      if(StringUtils.isNotBlank(focusCommentID)) {
        ExoSocialActivity focusedActivity = Utils.getActivityManager().getActivity(focusCommentID);
        if (focusedActivity != null && focusedActivity.getParentCommentId() != null) {
          getAndSetUpdatedCommentId(focusedActivity.getParentCommentId());
        } else {
          getAndSetUpdatedCommentId(focusCommentID);
        }
      }
      super.processRender(context);
      if (getActivity().getId().equals(focusActivityId)) {
        context.getJavascriptManager()
               .require("SHARED/social-ui-activity", "activity")
               .addScripts("setTimeout(function() { " + "activity.hightlightComment('" + focusActivityId + "');"
                   + ((Utils.isFocusCommentBox()) ? "activity.replyByURL('" + focusActivityId + "');" : "")
                   + ((Utils.isFocusCommentReplyBox()) ? "activity.replyByURL('" + focusCommentID + "');" : "")
                   + ((StringUtils.isNotBlank(focusCommentID)) ? "activity.focusToComment('" + focusCommentID + "');" : "")
                   + ((Utils.isExpandLikers()) ? "activity.loadLikersByURL();" : "") + "}, 100);");
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * @deprecated : Replace by {@link #isNoLongerExisting(String)}
   */
  protected boolean isNoLongerExisting(String activityId, Event<BaseUIActivity> event) {
    return isNoLongerExisting(activityId);
  }

  protected boolean isNoLongerExisting(String activityId) {
    ExoSocialActivity existingActivity = Utils.getActivityManager().getActivity(activityId);
    if (existingActivity == null) {
      getAncestorOfType(UIPortletApplication.class).addMessage(new ApplicationMessage("BaseUIActivity.msg.info.Activity_No_Longer_Exist",
              null,
              ApplicationMessage.INFO));
      return true;
    }
    return false;
  }

  public boolean isDeletedSpace(String streamOwner) {
    // only check when the activity belongs to the space stream owner
    if (this.activity.getActivityStream().getType().toString().equals(SpaceIdentityProvider.NAME)) {
      return CommonsUtils.getService(SpaceService.class).getSpaceByPrettyName(streamOwner) == null;
    } else {
      return false;
    }
  }

  /**
   * @return the identity of the current user who is commenting on the activity
   */
  public Identity getCommenterIdentity() {
    return Utils.getViewerIdentity();
  }

  /**
   * Allow child can be override this method to process I18N
   *
   * @param activity
   * @return
   */
  protected ExoSocialActivity getI18N(ExoSocialActivity activity) {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    I18NActivityProcessor i18NActivityProcessor = getApplicationComponent(I18NActivityProcessor.class);
    if (activity.getTitleId() != null) {
      Locale userLocale = requestContext.getLocale();
      activity = i18NActivityProcessor.process(activity, userLocale);
    }
    return activity;
  }

  private List<ExoSocialActivity> getI18N(List<ExoSocialActivity> comments) {
    List<ExoSocialActivity> cmts = new ArrayList<ExoSocialActivity>();
    for (ExoSocialActivity cmt : comments) {
      cmts.add(getI18N(cmt));
    }
    return cmts;
  }

  protected void focusToComment(String commentId) {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    JavascriptManager jm = pContext.getJavascriptManager();
    jm.require("SHARED/social-ui-activity", "UIActivity").addScripts("UIActivity.focusToComment('" + commentId + "', null, 2000);");
  }

  public static enum CommentStatus {
    LATEST("latest"), ALL("all"), NONE("none");
    private String commentStatus;

    private CommentStatus(String status) {
      commentStatus = status;
    }

    public String getStatus() {
      return commentStatus;
    }
  }

  public static class LoadLikesActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId)) {
        return;
      }
      // uiActivity.refresh();
      uiActivity.setAllLoaded(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);

      JavascriptManager jm = event.getRequestContext().getJavascriptManager();
      jm.require("SHARED/social-ui-activity", "activity").addScripts("activity.loadLikes('#ContextBox" + activityId + "');");

      Utils.initUserProfilePopup(uiActivity.getId());
    }
  }

  public static class LikeActivityActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId)) {
        return;
      }
      // uiActivity.refresh();
      WebuiRequestContext requestContext = event.getRequestContext();
      String isLikedStr = requestContext.getRequestParameter(OBJECTID);
      String commentId = requestContext.getRequestParameter("commentId");
      uiActivity.setLike(Boolean.parseBoolean(isLikedStr));
      //
      JavascriptManager jm = requestContext.getJavascriptManager();
      jm.require("SHARED/social-ui-activity", "activity").addScripts("activity.displayLike('#ContextBox" + activityId + "');");

      if(StringUtils.isNotBlank(commentId)) {
        uiActivity.getAndSetUpdatedCommentId(commentId);
      }
      requestContext.addUIComponentToUpdateByAjax(uiActivity);

      Utils.initUserProfilePopup(uiActivity.getId());
    }
  }

  public static class SetCommentListStatusActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId)) {
        return;
      }
      // uiActivity.refresh();
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
      uiActivity.setCommentFormDisplayed(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);

      //
      String inputContainerId = "InputContainer" + activityId;
      StringBuffer script = new StringBuffer("(function($) {");
      script.append("var inputContainer = $('#").append(inputContainerId).append("');");
      script.append("inputContainer.addClass('inputContainerShow hidden-phone').show();");
      script.append("})(jq);");

      JavascriptManager jm = event.getRequestContext().getJavascriptManager();

      Utils.initUserProfilePopup(uiActivity.getId());

      jm.require("SHARED/social-ui-activity", "activity").require("SHARED/jquery", "jq").
      addScripts("activity.initCKEditor('" + activityId + "', activity.spaceURL, activity.commentPlaceholder, activity.spaceGroupId);").
      addScripts(script.toString());
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

      Utils.initUserProfilePopup(uiActivity.getId());
    }
  }

  public static class PostCommentActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId)) {
        return;
      }
      String commentId = event.getRequestContext().getRequestParameter(OBJECTID);
      // uiActivity.refresh();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIFormTextAreaInput uiFormComment = uiActivity.getChild(UIFormTextAreaInput.class);
      String message = uiFormComment.getValue();

      if (message == null || message.equals("")) {
        UIApplication uiApplication = requestContext.getUIApplication();
        uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message", null, ApplicationMessage.WARNING));
        return;
      }

      uiFormComment.reset();
      //--- Processing outcome here aims to avoid escaping '@' symbol while preventing any undesirable side effects due to CSS sanitization.
      //--- The goal is to avoid escape '@' occurrences in microblog application, this enables to keep mention feature working as expected in the specification
      ExoSocialActivity newComment = uiActivity.saveComment(requestContext.getRemoteUser(), message.replaceAll(HTML_AT_SYMBOL_ESCAPED_PATTERN, HTML_AT_SYMBOL_PATTERN), commentId);
      uiActivity.setCommentFormFocused(true);
      requestContext.addUIComponentToUpdateByAjax(uiActivity);

      Utils.initUserProfilePopup(uiActivity.getId());
      uiActivity.focusToComment(newComment.getId());
      uiActivity.getParent().broadcast(event, event.getExecutionPhase());
    }
  }

  public static class DeleteActivityActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId)) {
        return;
      }
      Utils.getActivityManager().deleteActivity(activityId);

      UIActivitiesContainer activitiesContainer = uiActivity.getAncestorOfType(UIActivitiesContainer.class);
      activitiesContainer.removeChildById(uiActivity.getParent().getId());
      activitiesContainer.removeActivity(uiActivity.getActivity());

      WebuiRequestContext context = event.getRequestContext();
      context.getJavascriptManager()
             .require("SHARED/social-ui-activity", "activity")
             .addScripts("activity.responsiveMobile('"
                 + activitiesContainer.getAncestorOfType(UIPortletApplication.class).getId() + "');");
      //
      boolean isEmptyListActivity = (activitiesContainer.getActivityIdList().size() == 0)
          && (activitiesContainer.getActivityList().size() == 0);
      if (isEmptyListActivity) {
        context.addUIComponentToUpdateByAjax(activitiesContainer.getParent().getParent());
      } else {
        AbstractActivitiesDisplay uiActivitiesDisplay = activitiesContainer.getAncestorOfType(AbstractActivitiesDisplay.class);
        uiActivitiesDisplay.init();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiActivitiesDisplay);
      }
      Utils.clearUserProfilePopup();
    }
  }

  public static class DeleteCommentActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      String commentId = requestContext.getRequestParameter(OBJECTID);
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId) || uiActivity.isNoLongerExisting(commentId)) {
        return;
      }
      Utils.getActivityManager().deleteComment(activityId, commentId);
      // uiActivity.refresh();
      requestContext.addUIComponentToUpdateByAjax(uiActivity);

      Utils.initUserProfilePopup(uiActivity.getId());
    }
  }

  public static class LikeCommentActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      //ObjectID contains the action to do and the comment id
      String[] likeStatus = requestContext.getRequestParameter(OBJECTID).split("_");
      BaseUIActivity uiActivity = event.getSource();
      uiActivity.setLikeComment(Boolean.parseBoolean(likeStatus[0]), likeStatus[1]);
      String commentId = requestContext.getRequestParameter("commentId");
      if(StringUtils.isNotBlank(commentId)) {
        uiActivity.getAndSetUpdatedCommentId(commentId);
      }
      requestContext.addUIComponentToUpdateByAjax(uiActivity);
      Utils.initUserProfilePopup(uiActivity.getId());

    }
  }

  public static class EditActivityActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      BaseUIActivity uiActivity = event.getSource();
      String message = ((UIFormTextAreaInput)uiActivity.getChildById(COMPOSER_TEXT_AREA_EDIT_INPUT + uiActivity.getActivity().getId())).
              getValue().replaceAll(HTML_AT_SYMBOL_ESCAPED_PATTERN, HTML_AT_SYMBOL_PATTERN);

      uiActivity.editActivity(message);
      requestContext.addUIComponentToUpdateByAjax(uiActivity);
      Utils.initUserProfilePopup(uiActivity.getId());
      uiActivity.getParent().broadcast(event, event.getExecutionPhase());

    }
  }

  public static class EditCommentActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      String commentId = requestContext.getRequestParameter(OBJECTID);
      String message = requestContext.getRequestParameter("composerEditComment" + commentId);

      BaseUIActivity uiActivity = event.getSource();
      ExoSocialActivity originalActivity = Utils.getActivityManager().getActivity(commentId);

      uiActivity.editCommentMessage(originalActivity,message);
      if(StringUtils.isNotBlank(commentId)) {
        uiActivity.getAndSetUpdatedCommentId(commentId);
      }

      requestContext.addUIComponentToUpdateByAjax(uiActivity);
      Utils.initUserProfilePopup(uiActivity.getId());
      uiActivity.getParent().broadcast(event, event.getExecutionPhase());
    }
  }
}

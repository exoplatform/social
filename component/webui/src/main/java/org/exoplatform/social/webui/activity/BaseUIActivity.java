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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.SpaceActivityPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.space.SpaceException;
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

  private static final int DEFAULT_LIMIT = 10;
  
  protected static final int LIKES_NUM_DEFAULT = 0;
  
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

  private int loadCapacity;
  private int currentLoadIndex = 0;
  private RealtimeListAccess<ExoSocialActivity> activityCommentsListAccess;
  private ExoSocialActivity activity;
  private List<ExoSocialActivity> comments;
  private Identity ownerIdentity;
  private String[] identityLikes;
  private boolean commentFormDisplayed = false;
  private boolean allLoaded = false;
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

  public RealtimeListAccess<ExoSocialActivity> getActivityCommentsListAccess() {
    return activityCommentsListAccess;
  }

  public void setActivityCommentsListAccess(RealtimeListAccess<ExoSocialActivity> activityCommentsListAccess) {
    this.activityCommentsListAccess = activityCommentsListAccess;
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

  public void setActivity(ExoSocialActivity activity) {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    this.activity = activity;
    Identity identity = Utils.getIdentityManager().getIdentity(activity.getUserId(), true);
    setOwnerIdentity(identity);
    
    UIFormTextAreaInput commentTextArea = new UIFormTextAreaInput("CommentTextarea" + activity.getId(), "CommentTextarea", null);
    commentTextArea.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("BaseUIActivity.label.Add_your_comment"));
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

  public boolean isAllLoaded() {
    return allLoaded;
  }

  public void setAllLoaded(boolean allLoaded) {
    this.allLoaded = allLoaded;
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
    int commentsSize = activityCommentsListAccess.getSize();
    if (commentsSize == 0)
      return new ArrayList<ExoSocialActivity>();
    //
    List<ExoSocialActivity> comments = new ArrayList<ExoSocialActivity>();
    if (commentListStatus == CommentStatus.ALL) {
      if (currentLoadIndex == 0) {
        currentLoadIndex = commentsSize - DEFAULT_LIMIT - LATEST_COMMENTS_SIZE;
        loadCapacity = DEFAULT_LIMIT + LATEST_COMMENTS_SIZE;
      } else { 
        currentLoadIndex -= DEFAULT_LIMIT;
      }
      if (currentLoadIndex < 0) currentLoadIndex = 0;
      comments = activityCommentsListAccess.loadAsList(currentLoadIndex, loadCapacity);
      if (currentLoadIndex > 0) { 
        loadCapacity += currentLoadIndex;
      }
    } else if (commentListStatus == CommentStatus.NONE) {
      return comments != null ? comments : new ArrayList<ExoSocialActivity>();
    } else {
      if (commentsSize > LATEST_COMMENTS_SIZE) {
        comments = activityCommentsListAccess.loadAsList(commentsSize-LATEST_COMMENTS_SIZE, LATEST_COMMENTS_SIZE);
      } else {
        comments = activityCommentsListAccess.loadAsList(0, commentsSize >= DEFAULT_LIMIT ? DEFAULT_LIMIT : commentsSize);
      }
    }
    return getI18N(comments);
  }

  /**
   * Don't use this method what you want to get the comments's size.
   * You could use the new method for this stuff: getAllCommentSize() method
   * 
   * @return
   */
  @Deprecated
  public List<ExoSocialActivity> getAllComments() {
    return activityCommentsListAccess.loadAsList(0, activityCommentsListAccess.getSize());
  }

  /**
   * Gets number of comments of the specified activity
   * @return
   */
  public int getAllCommentSize() {
    return activityCommentsListAccess.getSize();
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
                                                          SpaceActivityPublisher.SPACE_APP_ID, message, null);
    Utils.getActivityManager().saveComment(getActivity(), comment);
    activityCommentsListAccess = Utils.getActivityManager().getCommentsWithListAccess(getActivity());
    comments = activityCommentsListAccess.loadAsList(0, DEFAULT_LIMIT);
    currentLoadIndex = 0;
    setCommentListStatus(CommentStatus.ALL);
  }

  public void setLike(boolean isLiked) throws Exception {
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
    activityCommentsListAccess = Utils.getActivityManager().getCommentsWithListAccess(activity);
    comments = activityCommentsListAccess.loadAsList(0, DEFAULT_LIMIT);
    comments = getI18N(comments);
    identityLikes = activity.getLikeIdentityIds();
    
    //init single activity : focus to comment's box or expand all comments
    initSingleActivity();
  }
  
  private void initSingleActivity() {
    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();
    if (postContext == PostContext.SINGLE) {
      if (! Utils.isExpandLikers() && ! Utils.isFocusCommentBox()) {
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
    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    
    if (Utils.getViewerIdentity().equals(getOwnerIdentity())) {
      return true;
    }
    
    Space space = null;
        
    if (postContext == PostContext.SPACE) {
      space = uiActivitiesContainer.getSpace();
    } else {
      Identity identityStreamOwner = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, 
                                                                                    this.getActivity().getStreamOwner(), false);
      if ( identityStreamOwner != null ) {
        space = spaceService.getSpaceByPrettyName(identityStreamOwner.getRemoteId());        
      }
    }
    
    if ( space != null ){ 
      return spaceService.isManager(space, Utils.getOwnerRemoteId());
    }
    
    return false;
  }

  public boolean isActivityCommentAndLikable() throws Exception {
    UIActivitiesContainer uiActivitiesContainer = getAncestorOfType(UIActivitiesContainer.class);
    PostContext postContext = uiActivitiesContainer.getPostContext();
    
    //
    if (postContext == PostContext.USER) {
      //base on SOC-3117
//      UIUserActivitiesDisplay uiUserActivitiesDisplay = getAncestorOfType(UIUserActivitiesDisplay.class);
//      if (uiUserActivitiesDisplay != null && !uiUserActivitiesDisplay.isActivityStreamOwner()) {
//        String ownerName = uiUserActivitiesDisplay.getOwnerName();
//        Identity ownerIdentity = Utils.getIdentityManager().
//                getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName, false);
//        Relationship relationship = Utils.getRelationshipManager().get(ownerIdentity, Utils.getViewerIdentity());
//        if (relationship == null) {
//          return false;
//        } else if (!(relationship.getStatus() == Type.CONFIRMED)) {
//          return false;
//        }
//      }
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
      Space space = null;
      SpaceService spaceService = getApplicationComponent(SpaceService.class);
      
      if (postContext == PostContext.SPACE) {
        space = uiActivitiesContainer.getSpace();
        spaceService = getApplicationComponent(SpaceService.class);
      } else {
        Identity identityStreamOwner = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, 
            this.getActivity().getStreamOwner(), false);
        if ( identityStreamOwner != null ) {
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

  public void setOwnerIdentity(Identity ownerIdentity) {
    this.ownerIdentity = ownerIdentity;
  }

  public Identity getOwnerIdentity() {
    return ownerIdentity;
  }

  public Identity getSpaceCreatorIdentity() {
    
    // If an activity of space then set creator information
    if ( SpaceIdentityProvider.NAME.equals(ownerIdentity.getProviderId()) ) {
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
        return Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME,managers[0], false);
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
  
  public static class LoadLikesActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId, event)) {
        return;
      }
      //uiActivity.refresh();
      uiActivity.setAllLoaded(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiActivity);
      
      JavascriptManager jm = event.getRequestContext().getJavascriptManager();
      jm.require("SHARED/social-ui-activity", "activity").addScripts("activity.loadLikes('#ContextBox" + activityId + "');");
      
      Utils.initUserProfilePopup(uiActivity.getId());
      Utils.resizeHomePage();
    }
  }

  public static class LikeActivityActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId, event)) {
        return;
      }
      //uiActivity.refresh();
      WebuiRequestContext requestContext = event.getRequestContext();
      String isLikedStr = requestContext.getRequestParameter(OBJECTID);
      uiActivity.setLike(Boolean.parseBoolean(isLikedStr));
      //
      JavascriptManager jm = requestContext.getJavascriptManager();
      jm.require("SHARED/social-ui-activity", "activity").addScripts("activity.displayLike('#ContextBox" + activityId + "');");      
      
      requestContext.addUIComponentToUpdateByAjax(uiActivity);
      
      Utils.initUserProfilePopup(uiActivity.getId());
      Utils.resizeHomePage();
    }
  }

  public static class SetCommentListStatusActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId, event)) {
        return;
      }
      //uiActivity.refresh();
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
      script.append("inputContainer.addClass('inputContainerShow').show();");
      script.append("})(jq);");
      
      JavascriptManager jm = event.getRequestContext().getJavascriptManager();
      
      Utils.initUserProfilePopup(uiActivity.getId());
      Utils.resizeHomePage();
      
      jm.require("SHARED/jquery", "jq").addScripts(script.toString());
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
      Utils.resizeHomePage();
    }
  }

  public static class PostCommentActionListener extends EventListener<BaseUIActivity> {
    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId, event)) {
        return;
      }
      //uiActivity.refresh();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIFormTextAreaInput uiFormComment = uiActivity.getChild(UIFormTextAreaInput.class);
      String message = uiFormComment.getValue();
      
      if (message == null || message.equals("")) {
        UIApplication uiApplication = requestContext.getUIApplication();
        uiApplication.addMessage(new ApplicationMessage("UIComposer.msg.error.Empty_Message",
                                                      null,
                                                      ApplicationMessage.WARNING));
        return;
      }
      
      uiFormComment.reset();
      uiActivity.saveComment(requestContext.getRemoteUser(), message);
      uiActivity.setCommentFormFocused(true);
      requestContext.addUIComponentToUpdateByAjax(uiActivity);
      
      Utils.initUserProfilePopup(uiActivity.getId());
      Utils.resizeHomePage();
      
      uiActivity.getParent().broadcast(event, event.getExecutionPhase());
    }
  }

  public static class DeleteActivityActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      if (uiActivity.isNoLongerExisting(activityId, event)) {
        return;
      }
      Utils.getActivityManager().deleteActivity(activityId);
      UIActivitiesContainer activitiesContainer = uiActivity.getParent();
      activitiesContainer.removeChildById(uiActivity.getId());
      activitiesContainer.removeActivity(uiActivity.getActivity());
      if (activitiesContainer.getActivityList().size() == 0) {
        event.getRequestContext().addUIComponentToUpdateByAjax(activitiesContainer.getParent().getParent());
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(activitiesContainer.getParent());
      }
      
      Utils.clearUserProfilePopup();
      Utils.resizeHomePage();
    }

  }

  public static class DeleteCommentActionListener extends EventListener<BaseUIActivity> {

    @Override
    public void execute(Event<BaseUIActivity> event) throws Exception {
      BaseUIActivity uiActivity = event.getSource();
      String activityId = uiActivity.getActivity().getId();
      String commentId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (uiActivity.isNoLongerExisting(activityId, event) || 
          uiActivity.isNoLongerExisting(commentId, event)) {
        return;
      }
      WebuiRequestContext requestContext = event.getRequestContext();
      Utils.getActivityManager().deleteComment(uiActivity.getActivity().getId(),
                                               requestContext.getRequestParameter(OBJECTID));
      //uiActivity.refresh();
      requestContext.addUIComponentToUpdateByAjax(uiActivity);
      
      Utils.initUserProfilePopup(uiActivity.getId());
      Utils.resizeHomePage();
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

  protected boolean isNoLongerExisting(String activityId, Event<BaseUIActivity> event) {
    ExoSocialActivity existingActivity = Utils.getActivityManager().getActivity(activityId);
    if (existingActivity == null) {
      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      uiApplication.addMessage(new ApplicationMessage("BaseUIActivity.msg.info.Activity_No_Longer_Exist",
                                                    null,
                                                    ApplicationMessage.INFO));
      return true;
    }
    return false;
  }
  
  public boolean isDeletedSpace(String streamOwner) {
    //only check when the activity belongs to the space stream owner
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
}

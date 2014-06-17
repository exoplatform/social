package org.exoplatform.social.notification;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;

public class LinkProviderUtils {
  
public static final String RESOURCE_URL = "social/notifications";
  
  public static final String INVITE_TO_CONNECT = RESOURCE_URL + "/inviteToConnect";
  
  public static final String CONFIRM_INVITATION_TO_CONNECT = RESOURCE_URL + "/confirmInvitationToConnect";
  
  public static final String IGNORE_INVITATION_TO_CONNECT = RESOURCE_URL + "/ignoreInvitationToConnect";
  
  public static final String ACCEPT_INVITATION_JOIN_SPACE = RESOURCE_URL + "/acceptInvitationToJoinSpace";
  
  public static final String IGNORE_INVITATION_JOIN_SPACE = RESOURCE_URL + "/ignoreInvitationToJoinSpace";
  
  public static final String VALIDATE_REQUEST_JOIN_SPACE = RESOURCE_URL + "/validateRequestToJoinSpace";
  
  public static final String REFUSE_SPACE_REQUEST_ACTION = RESOURCE_URL + "/refuseRequestToJoinSpace";
  
  public static final String REPLY_ACTIVITY = RESOURCE_URL + "/replyActivity";
  
  public static final String VIEW_FULL_DISCUSSION = RESOURCE_URL + "/viewFullDiscussion";
  
  public static final String REDIRECT_URL = RESOURCE_URL + "/redirectUrl";

  /**
   * Gets the url to the user's profile page of the receiver
   * 
   * @param senderId remoteId of the sender
   * @param receiverId remoteId of the receiver
   * @return
   */
  public static String getInviteToConnectUrl(String receiverId, String senderId) {
    return getRestUrl(INVITE_TO_CONNECT, receiverId, senderId);
  }
  
  /**
   * Gets the url to the user's profile page of the sender
   * @param senderId remoteId of the sender
   * @param receiverId remoteId of the receiver
   * @return
   */
  public static String getConfirmInvitationToConnectUrl(String senderId, String receiverId) {
    return getRestUrl(CONFIRM_INVITATION_TO_CONNECT, senderId, receiverId);
  }
  
  /**
   * Gets the url to the connection's tab of the current user
   * 
   * @param senderId remoteId of the sender
   * @param receiverId remoteId of the receiver
   * @return
   */
  public static String getIgnoreInvitationToConnectUrl(String senderId, String receiverId) {
    return getRestUrl(IGNORE_INVITATION_TO_CONNECT, senderId, receiverId);
  }
  
  /**
   * Gets the url to the space's home page
   * 
   * @param spaceId
   * @param userId
   * @return
   */
  public static String getAcceptInvitationToJoinSpaceUrl(String spaceId, String userId) {
    return getRestUrl(ACCEPT_INVITATION_JOIN_SPACE, spaceId, userId);
  }
  
  /**
   * Gets the url to the space's home page
   * 
   * @param spaceId
   * @param userId remoteId of the user
   * @return
   */
  public static String getIgnoreInvitationToJoinSpaceUrl(String spaceId, String userId) {
    return getRestUrl(IGNORE_INVITATION_JOIN_SPACE, spaceId, userId);
  }
  
  /**
   * Gets the url to the space's members
   * @param spaceId
   * @param userId remoteId of the user
   * @return
   */
  public static String getValidateRequestToJoinSpaceUrl(String spaceId, String userId) {
    return getRestUrl(VALIDATE_REQUEST_JOIN_SPACE, spaceId, userId);
  }
  
  /**
   * Gets the url to the space's members
   * @param spaceId
   * @param userId remoteId of the user
   * @return
   */
  public static String getRefuseRequestToJoinSpaceUrl(String spaceId, String userId) {
    return getRestUrl(REFUSE_SPACE_REQUEST_ACTION, spaceId, userId);
  }
  
  /**
   * Gets the associated page of type
   * @param type type of the page : user or space or activity
   * @param objectId can be a space's id or user's id or activity's id
   * @return
   */
  public static String getRedirectUrl(String type, String objectId) {
    return getRestUrl(REDIRECT_URL, type, objectId);
  }

  /**
   * Gets full rest url
   * 
   * @param type
   * @param objectId1
   * @param objectId2
   * @return
   */
  public static String getRestUrl(String type, String objectId1, String objectId2) {
    String baseUrl = getBaseRestUrl();
    return new StringBuffer(baseUrl).append("/").append(type).append("/").append(objectId1)
                                    .append("/").append(objectId2).toString();
  }
  
  /** 
   * Get base url of rest service
   * 
   * @return base rest url like : http://localhost:8080/rest
   */
  public static String getBaseRestUrl() {
    return new StringBuffer(CommonsUtils.getCurrentDomain()).append("/").append(CommonsUtils.getRestContextName()).toString();
  }
  
  /**
   * Gets the user's avatar url. In case this url is null, we take the default url
   * 
   * @param profile user profile
   * @return
   */
  public static String getUserAvatarUrl(Profile profile) {
    return CommonsUtils.getCurrentDomain() + ((profile != null && profile.getAvatarUrl() != null) ? profile.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
  }
  
  /**
   * Gets the space's avatar url. In case this url is null, we take the default url
   * 
   * @param space
   * @return
   */
  public static String getSpaceAvatarUrl(Space space) {
    return CommonsUtils.getCurrentDomain() + ((space != null && space.getAvatarUrl() != null) ? space.getAvatarUrl() : LinkProvider.SPACE_DEFAULT_AVATAR_URL);
  }
}

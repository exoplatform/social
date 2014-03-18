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
package org.exoplatform.social.core.activity.model;

/**
 * ExoSocialActivity interface extends {@link org.apache.shindig.social.opensocial.model.Activity}}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Nov 9, 2010
 * @since 1.2.0-GA
 */
public interface ExoSocialActivity extends org.apache.shindig.social.opensocial.model.Activity {
  /**
   * The  Constant label comment.
   */
  String IS_COMMENT = "IS_COMMENT";

  /**
   * Gets activity stream associated with this activity.
   *
   * @return ActivityStream
   */
  ActivityStream getActivityStream();

  /**
   * Sets activity stream associated with this activity.
   *
   * @param as the activity stream
   */
  void setActivityStream(ActivityStream as);

  /**
   * To indicate if this a comment to an activity.
   *
   * @return true if this is a comment or false
   */
  boolean isComment();

  /**
   * To indicate if this a comment to an activity.
   *
   * @param isCommentOrNot to know if this is a comment
   */
  void isComment(boolean isCommentOrNot);

  /**
   * The type of activity, this can be used for displaying activity with
   * different ui components.
   *
   * @return the type of activity
   */
  String getType();

  /**
   * The type of activity, this can be used for displaying activity with
   * different ui components.
   *
   * @param activityType the type of activity
   */
  void setType(String activityType);

  /**
   * Gets reply to identity id.
   *
   * @return the corresponding identity id
   */
  String[] getReplyToId();

  /**
   * Sets reply to identity id.
   *
   * @param replyToId the identity id
   */
  void setReplyToId(String[] replyToId);

  /**
   * To know if this is a hidden activity.
   *
   * @return true or false
   */
  boolean isHidden();

  /**
   * Sets to indicate if this is a hidden activity or not.
   *
   * @param isHiddenOrNot true or false
   */
  void isHidden(boolean isHiddenOrNot);
  
  /**
   * To know if this is a locked activity.
   *
   * @return true or false
   */
  boolean isLocked();

  /**
   * Sets to indicate if this is a locked activity or not.
   *
   * @param isLockedOrNot true or false
   */
  void isLocked(boolean isLockedOrNot);

  /**
   * Gets array of identityIds who like this activity.
   *
   * @return array of identityIds
   */
  String[] getLikeIdentityIds();
  
  /**
   * Gets number of Liker who like this activity.
   *
   * @return number of liker
   */
  int getNumberOfLikes();

  /**
   * Sets array of identityIds who like this activity.
   *
   * @param identityIds array of identity Ids
   */
  void setLikeIdentityIds(String[] identityIds);

  /**
   * Gets the stream owner, must be the remoteId of an identity.
   *
   * @return the stream owner
   */
  String getStreamOwner();

  /**
   * Sets the stream owner, must be the remoteId of an identity.
   *
   * @param so the stream owner
   */
  void setStreamOwner(String so);

  /**
   * The uuid of the node for this stream.
   *
   * @return stream id
   */
  String getStreamId();

  /**
   * The uuid of the node for this stream.
   *
   * @param streamId the stream id
   */
  void setStreamId(String streamId);

  /**
   * Gets the name; could be the name of the doc, the name of jira task or the
   * page title.
   *
   * @return the name
   */
  String getName();

  /**
   * Sets the name; could be the name of the doc, the name of jira task or the
   * page title.
   *
   * @param name the name
   */
  void setName(String name);

  /**
   * This string value provides a human readable description or summary of the
   * activity object.
   *
   * @return the activity summary
   */
  String getSummary();

  /**
   * Sets activity summary.
   *
   * @param summary the activity summary
   */
  void setSummary(String summary);

  /**
   * Gets the permanent link.
   * <p/>
   * Could be the link to the doc, the jira task, or the link
   *
   * @return a permalink string, possibly null
   */
  String getPermaLink();

  /**
   * Sets the permanent link. Could be the link to the doc, the jira task, or
   * the link
   *
   * @param permaLink the permalink link
   */
  void setPermanLink(String permaLink);
  
  /**
   * Gets array of identityIds who like this activity.
   *
   * @return array of identityIds
   */
  String[] getMentionedIds();

  /**
   * Sets array of identityIds who like this activity.
   *
   * @param identityIds array of identity Ids
   */
  void setMentionedIds(String[] identityIds);
  
  /**
   * Gets array of identityIds who comment this activity.
   *
   * @return array of identityIds
   */
  String[] getCommentedIds();

  /**
   * Sets array of identityIds who comment this activity.
   *
   * @param identityIds array of identity Ids
   */
  void setCommentedIds(String[] identityIds);

  /**
     * Set the last update datetime
     *
     * @param updated last update datetime
     */
  public void setUpdated(Long updated);
  
  /**
   * Gets id of identity who is poster.
   * 
   * @return Id of poster.
   */
  String getPosterId();

  /**
   * Sets poster id.
   * 
   * @param posterId
   */
  void setPosterId(String posterId);
  
  /**
   * Get id of parent activity.
   * 
   * @return Id of parent activity
   */
  String getParentId();
  
  /**
   * Set parent activity id
   * 
   * @param parentId
   */
  void setParentId(String parentId);
}

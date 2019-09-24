/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.storage.cache.model.data;

import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;

import java.util.*;

/**
 * Immutable activity data.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityData implements CacheData<ExoSocialActivity> {
  private static final long serialVersionUID = -7626120214861121711L;

  public final static ActivityData NULL = new ActivityData(new ExoSocialActivityImpl());
  
  private final String id;
  private final String title;
  private final String body;
  private final String[] likes;
  private final boolean isComment;
  private final boolean isHidden;
  private final boolean isLocked;
  private final Long postedTime;
  private final Long lastUpdated;
  private final String[] replyIds;
  private final String userId;
  private final String appId;
  private final String titleId;
  private final String bodyId;
  private final String type;
  private final Map templateParams;
  private final String externalId;
  private final String url;
  private final String streamId;
  private final String streamOwner;
  private final String streamFaviconUrl;
  private final String streamSourceUrl;
  private final String streamTitle;
  private final String streamUrl;
  private final String[] mentioners;
  private final String[] commenters;
  private final ActivityStream.Type streamType;
  private final String posterId;
  private final String parentId;
  private final String parentCommentId;

  public ActivityData(final ExoSocialActivity activity) {

    this.id = activity.getId();
    this.title = activity.getTitle();
    this.body = activity.getBody();
    this.likes = activity.getLikeIdentityIds();
    this.isComment = activity.isComment();
    this.isHidden = activity.isHidden();
    this.isLocked = activity.isLocked();
    this.postedTime = activity.getPostedTime();
    this.lastUpdated = activity.getUpdated().getTime();
    this.replyIds = activity.getReplyToId();
    this.userId = activity.getUserId();
    this.appId = activity.getAppId();
    this.titleId = activity.getTitleId();
    this.bodyId = activity.getBodyId();
    this.type = activity.getType();
    this.externalId = activity.getExternalId();
    this.url = activity.getUrl();
    this.streamId = activity.getStreamId();
    this.streamOwner = activity.getStreamOwner();
    this.streamFaviconUrl = activity.getStreamFaviconUrl();
    this.streamSourceUrl = activity.getStreamSourceUrl();
    this.streamTitle = activity.getStreamTitle();
    this.streamUrl = activity.getStreamUrl();
    this.mentioners = activity.getMentionedIds();
    this.commenters = activity.getCommentedIds();
    this.streamType = activity.getActivityStream().getType();
    this.posterId = activity.getPosterId();
    this.parentId = activity.getParentId();
    this.parentCommentId = activity.getParentCommentId();

    if (activity.getTemplateParams() != null) {
      this.templateParams = Collections.unmodifiableMap(activity.getTemplateParams());
    }
    else {
      this.templateParams = Collections.emptyMap();
    }

  }

  public ExoSocialActivity build() {

    //
    if (this == NULL) {
      return null;
    }
    
    ExoSocialActivity activity = new ExoSocialActivityImpl();

    activity.setId(id);
    activity.setTitle(title);
    activity.setBody(body);
    if (likes != null) { activity.setLikeIdentityIds(likes); }
    activity.setReplyToId(replyIds);
    activity.isComment(isComment);
    activity.isHidden(isHidden);
    activity.isLocked(isLocked);
    activity.setPostedTime(postedTime);
    activity.setUpdated(lastUpdated);
    activity.setUserId(userId);
    activity.setAppId(appId);
    activity.setTitleId(titleId);
    activity.setBodyId(bodyId);
    activity.setType(type);
    activity.setTemplateParams(new LinkedHashMap<String, String>(templateParams));
    activity.setExternalId(externalId);
    activity.setUrl(url);
    if (mentioners != null) { activity.setMentionedIds(mentioners); }
    if (commenters != null) { activity.setCommentedIds(commenters); }
    activity.setPosterId(posterId);
    activity.setParentId(parentId);
    activity.setParentCommentId(parentCommentId);

    ActivityStream activityStream = activity.getActivityStream();
    activityStream.setId(streamId);
    activityStream.setPrettyId(streamOwner);
    activityStream.setFaviconUrl(streamFaviconUrl);
    activityStream.setPermaLink(streamSourceUrl);
    activityStream.setTitle(streamTitle);
    activityStream.setType(streamType);

    activity.setActivityStream(activityStream);

    return activity;

  }

  public String getId() {
    return id;
  }

  public String getUserId() {
    return userId;
  }

  public String getStreamOwner() {
    return streamOwner;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ActivityData that = (ActivityData) o;
    return isComment == that.isComment &&
            isHidden == that.isHidden &&
            isLocked == that.isLocked &&
            Objects.equals(id, that.id) &&
            Objects.equals(title, that.title) &&
            Objects.equals(body, that.body) &&
            Arrays.equals(likes, that.likes) &&
            Objects.equals(postedTime, that.postedTime) &&
            Objects.equals(lastUpdated, that.lastUpdated) &&
            Arrays.equals(replyIds, that.replyIds) &&
            Objects.equals(userId, that.userId) &&
            Objects.equals(appId, that.appId) &&
            Objects.equals(titleId, that.titleId) &&
            Objects.equals(bodyId, that.bodyId) &&
            Objects.equals(type, that.type) &&
            Objects.equals(templateParams, that.templateParams) &&
            Objects.equals(externalId, that.externalId) &&
            Objects.equals(url, that.url) &&
            Objects.equals(streamId, that.streamId) &&
            Objects.equals(streamOwner, that.streamOwner) &&
            Objects.equals(streamFaviconUrl, that.streamFaviconUrl) &&
            Objects.equals(streamSourceUrl, that.streamSourceUrl) &&
            Objects.equals(streamTitle, that.streamTitle) &&
            Objects.equals(streamUrl, that.streamUrl) &&
            Arrays.equals(mentioners, that.mentioners) &&
            Arrays.equals(commenters, that.commenters) &&
            streamType == that.streamType &&
            Objects.equals(posterId, that.posterId) &&
            Objects.equals(parentId, that.parentId) &&
            Objects.equals(parentCommentId, that.parentCommentId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, title, body, likes, isComment, isHidden, isLocked, postedTime, lastUpdated, replyIds,
            userId, appId, titleId, bodyId, type, templateParams, externalId, url, streamId, streamOwner, streamFaviconUrl,
            streamSourceUrl, streamTitle, streamUrl, mentioners, commenters, streamType, posterId, parentId, parentCommentId);
  }
}

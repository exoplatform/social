/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.opensocial.service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Lists;
import org.apache.shindig.auth.AnonymousSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.core.model.ActivityImpl;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * The Class ExoActivityService.
 */
public class ExoActivityService extends ExoService implements ActivityService {

  /** The Constant OPENSOCIAL_PREFIX. */
  public final static String OPENSOCIAL_PREFIX        = "opensocial:";

  /** The Constant OPENSOCIAL_PREFIX_LENGTH. */
  public final static int    OPENSOCIAL_PREFIX_LENGTH = OPENSOCIAL_PREFIX.length();

  /*
   * (non-Javadoc)
   * @see
   * org.apache.shindig.social.opensocial.spi.ActivityService#getActivities(
   * java.util.Set, org.apache.shindig.social.opensocial.spi.GroupId,
   * java.lang.String, java.util.Set,
   * org.apache.shindig.social.opensocial.spi.CollectionOptions,
   * org.apache.shindig.auth.SecurityToken)
   */
  public Future<RestfulCollection<Activity>> getActivities(Set<UserId> userIds,
                                                           GroupId groupId,
                                                           String appId,
                                                           Set<String> fields,
                                                           CollectionOptions options,
                                                           SecurityToken token) throws ProtocolException {
    List<Activity> result = Lists.newArrayList();

    PortalContainer pc = getPortalContainer(token);
    ActivityManager am = (ActivityManager) pc.getComponentInstanceOfType(ActivityManager.class);

    try {
      Set<Identity> idSet = getIdSet(userIds, groupId, token);
      for (Identity id : idSet) {
        // TODO filter by appID
        RealtimeListAccess<ExoSocialActivity> activityListAccess = am.getActivitiesWithListAccess(id);
        result.addAll(convertToOSActivities(activityListAccess.loadAsList(options.getFirst(), options.getMax()), fields));
      }

      return ImmediateFuture.newInstance(new RestfulCollection<Activity>(result, 0, result.size()));
    } catch (Exception je) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, je.getMessage(), je);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.apache.shindig.social.opensocial.spi.ActivityService#getActivities(
   * org.apache.shindig.social.opensocial.spi.UserId,
   * org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String,
   * java.util.Set, org.apache.shindig.social.opensocial.spi.CollectionOptions,
   * java.util.Set, org.apache.shindig.auth.SecurityToken)
   */
  public Future<RestfulCollection<Activity>> getActivities(UserId userId,
                                                           GroupId groupId,
                                                           String appId,
                                                           Set<String> fields,
                                                           CollectionOptions options,
                                                           Set<String> activityIds,
                                                           SecurityToken token) throws ProtocolException {
    List<Activity> result = Lists.newArrayList();
    try {
      if (token instanceof AnonymousSecurityToken) {
        throw new Exception(Integer.toString(HttpServletResponse.SC_FORBIDDEN));
      }

      PortalContainer pc = getPortalContainer(token);
      ActivityManager am = (ActivityManager) pc.getComponentInstanceOfType(ActivityManager.class);

      String user = userId.getUserId(token);
      Identity id = getIdentity(user, token);

      List<ExoSocialActivity> exoActivities = am.getActivitiesWithListAccess(id)
                                                .loadAsList(options.getFirst(), options.getMax());

      // TODO : this is not efficient, this should be done by the JCR
      for (ExoSocialActivity exoActivity : exoActivities) {
        if (exoActivity.getType() != null && exoActivity.getType().startsWith(OPENSOCIAL_PREFIX)) {
          if (activityIds.contains(exoActivity.getType().substring(OPENSOCIAL_PREFIX_LENGTH)))
            ;
          result.add(convertToOSActivity(exoActivity, fields));
        }
      }

      return ImmediateFuture.newInstance(new RestfulCollection<Activity>(result, 0, result.size()));
    } catch (Exception je) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, je.getMessage(), je);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.apache.shindig.social.opensocial.spi.ActivityService#getActivity(org
   * .apache.shindig.social.opensocial.spi.UserId,
   * org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String,
   * java.util.Set, java.lang.String, org.apache.shindig.auth.SecurityToken)
   */
  public Future<Activity> getActivity(UserId userId,
                                      GroupId groupId,
                                      String appId,
                                      Set<String> fields,
                                      String activityId,
                                      SecurityToken token) throws ProtocolException {
    throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, null);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.apache.shindig.social.opensocial.spi.ActivityService#deleteActivities
   * (org.apache.shindig.social.opensocial.spi.UserId,
   * org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String,
   * java.util.Set, org.apache.shindig.auth.SecurityToken)
   */
  public Future<Void> deleteActivities(UserId userId,
                                       GroupId groupId,
                                       String appId,
                                       Set<String> activityIds,
                                       SecurityToken token) throws ProtocolException {
    throw new ProtocolException(HttpServletResponse.SC_NOT_IMPLEMENTED, null);
  }

  /*
   * (non-Javadoc)
   * @see
   * org.apache.shindig.social.opensocial.spi.ActivityService#createActivity
   * (org.apache.shindig.social.opensocial.spi.UserId,
   * org.apache.shindig.social.opensocial.spi.GroupId, java.lang.String,
   * java.util.Set, org.apache.shindig.social.opensocial.model.Activity,
   * org.apache.shindig.auth.SecurityToken)
   */
  public Future<Void> createActivity(UserId userId,
                                     GroupId groupId,
                                     String appId,
                                     Set<String> fields,
                                     Activity activity,
                                     SecurityToken token) throws ProtocolException {
    try {
      activity.setAppId(appId); //groupId = new GroupId(GroupId.Type.groupId, "space:qsdsqd")

      ExoSocialActivity exoActivity = convertFromOSActivity(activity, fields);


      if (token instanceof AnonymousSecurityToken) {
        throw new ProtocolException(HttpServletResponse.SC_UNAUTHORIZED, " a non anonymous security token is expected");
      }

      PortalContainer pc = getPortalContainer(token);
      ActivityManager am = (ActivityManager) pc.getComponentInstanceOfType(ActivityManager.class);
      IdentityManager identityManager = (IdentityManager) pc.getComponentInstanceOfType(IdentityManager.class);

      String user = userId.getUserId(token); // can be organization:name or organization:UUID
      if (user.contains(":")) {
        user = user.split(":")[1];
      }

      Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user, false);

      // identity for the stream to post on
      Identity targetStream = userIdentity;

      /// someone posting for a space ?
      if (groupId.getType() == GroupId.Type.groupId) {
        String group = groupId.getGroupId(); // can be space:name or space:UUID
        if (group.contains(":")) {
          group = group.split(":")[1];
        }
        targetStream = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, group, false);
        // TODO : check that member is allowed to post on group or throw SC_UNAUTHORIZED
      }

      // we need to know where to post
      if (targetStream == null) {
        throw new ProtocolException(HttpServletResponse.SC_FORBIDDEN, user + " is an unknown identity");
      }

      // Define activity user if not already set
      String activityUser = exoActivity.getUserId();
      if (activityUser == null) {
        exoActivity.setUserId(userIdentity.getId());

        // making sure it resolves to a valid identity
      } else {
        Identity activityUserIdentity = identityManager.getIdentity(activityUser);
        if (activityUserIdentity == null) {
          throw new ProtocolException(HttpServletResponse.SC_FORBIDDEN, activityUser + " is an unknown identity");
        }
      }

      am.saveActivityNoReturn(targetStream, exoActivity);

      return ImmediateFuture.newInstance(null);
    } catch (Exception e) {
      if (e instanceof ProtocolException) {
        throw (ProtocolException)e;
      }
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
  }

  /**
   * Convert from os activity.
   *
   * @param activity the activity
   * @param fields the fields
   * @return the org.exoplatform.social.core.activitystream.model. activity
   */
  private ExoSocialActivity convertFromOSActivity(Activity activity,
                                                                                          Set<String> fields) {

    ExoSocialActivity exoActivity = new ExoSocialActivityImpl();

    if (fields != null && !fields.isEmpty()) {

      for (String field : fields) {

        if (field.equals(Activity.Field.APP_ID.name())) {
          exoActivity.setAppId(activity.getAppId());
        }

        if (field.equals(Activity.Field.BODY.name())) {
          exoActivity.setBody(activity.getBody());
        }

        if (field.equals(Activity.Field.BODY_ID.name())) {
          exoActivity.setBodyId(activity.getBodyId());
        }

        if (field.equals(Activity.Field.EXTERNAL_ID.name())) {
          exoActivity.setExternalId(activity.getExternalId());
        }

        if (field.equals(Activity.Field.ID.name())) {
          exoActivity.setId(activity.getId());
        }

        if (field.equals(Activity.Field.MEDIA_ITEMS.name())) {
          exoActivity.setMediaItems(activity.getMediaItems());
        }

        if (field.equals(Activity.Field.POSTED_TIME.name())) {
          exoActivity.setPostedTime(activity.getPostedTime());
        }

        if (field.equals(Activity.Field.LAST_UPDATED.name())) {
          exoActivity.setUpdated(activity.getUpdated());
        }

        if (field.equals(Activity.Field.PRIORITY.name())) {
          exoActivity.setPriority(activity.getPriority());
        }

        if (field.equals(Activity.Field.STREAM_FAVICON_URL.name())) {
          exoActivity.setStreamFaviconUrl(activity.getStreamFaviconUrl());
        }

        if (field.equals(Activity.Field.STREAM_SOURCE_URL.name())) {
          exoActivity.setStreamSourceUrl(activity.getStreamSourceUrl());
        }

        if (field.equals(Activity.Field.STREAM_TITLE.name())) {
          exoActivity.setStreamTitle(activity.getStreamTitle());
        }

        if (field.equals(Activity.Field.STREAM_URL.name())) {
          exoActivity.setStreamUrl(activity.getStreamUrl());
        }

        if (field.equals(Activity.Field.TEMPLATE_PARAMS.name())) {
          exoActivity.setTemplateParams(activity.getTemplateParams());
        }

        if (field.equals(Activity.Field.TITLE.name())) {
          exoActivity.setTitle(activity.getTitle());
        }

        if (field.equals(Activity.Field.TITLE_ID)) {
          exoActivity.setTitleId(activity.getTitleId());
        }

        if (field.equals(Activity.Field.URL)) {
          exoActivity.setUrl(activity.getUrl());
        }
        if (field.equals(Activity.Field.USER_ID)) {
          exoActivity.setUserId(activity.getUserId());
        }

      }

    } else {

      exoActivity.setAppId(activity.getAppId());
      exoActivity.setBody(activity.getBody());
      exoActivity.setBodyId(activity.getBodyId());
      exoActivity.setExternalId(activity.getExternalId());
      exoActivity.setId(activity.getId());
      exoActivity.setMediaItems(activity.getMediaItems());
      if (activity.getPostedTime() != null)
        exoActivity.setPostedTime(activity.getPostedTime());
      exoActivity.setPriority(activity.getPriority());
      exoActivity.setStreamFaviconUrl(activity.getStreamFaviconUrl());
      exoActivity.setStreamSourceUrl(activity.getStreamSourceUrl());
      exoActivity.setStreamTitle(activity.getStreamTitle());
      exoActivity.setStreamUrl(activity.getStreamUrl());
      exoActivity.setTemplateParams(activity.getTemplateParams());
      exoActivity.setTitle(activity.getTitle());
      exoActivity.setTitleId(activity.getTitleId());
      if (activity.getUpdated() != null)
        exoActivity.setUpdated(activity.getUpdated());
      exoActivity.setUrl(activity.getUrl());
      exoActivity.setUserId(activity.getUserId());

    }
    return exoActivity;

  }

  /**
   * Convert to os activity.
   *
   * @param exoActivity the exo activity
   * @param fields the fields
   * @return the activity
   */
  private Activity convertToOSActivity(ExoSocialActivity exoActivity,
                                       Set<String> fields) {

    Activity activity = new ActivityImpl();

    if (fields != null && !fields.isEmpty()) {

      for (String field : fields) {

        if (field.equals(Activity.Field.APP_ID.name())) {
          activity.setAppId(exoActivity.getAppId());
        }

        if (field.equals(Activity.Field.BODY.name())) {
          activity.setBody(exoActivity.getBody());
        }

        if (field.equals(Activity.Field.BODY_ID.name())) {
          activity.setBodyId(exoActivity.getBodyId());
        }

        if (field.equals(Activity.Field.EXTERNAL_ID.name())) {
          activity.setExternalId(exoActivity.getExternalId());
        }

        if (field.equals(Activity.Field.ID.name())) {
          activity.setId(exoActivity.getId());
        }

        if (field.equals(Activity.Field.MEDIA_ITEMS.name())) {
          activity.setMediaItems(exoActivity.getMediaItems());
        }

        if (field.equals(Activity.Field.POSTED_TIME.name())) {
          activity.setPostedTime(exoActivity.getPostedTime());
        }

        if (field.equals(Activity.Field.LAST_UPDATED.name())) {
          activity.setUpdated(exoActivity.getUpdated());
        }

        if (field.equals(Activity.Field.PRIORITY.name())) {
          activity.setPriority(exoActivity.getPriority());
        }

        if (field.equals(Activity.Field.STREAM_FAVICON_URL.name())) {
          activity.setStreamFaviconUrl(exoActivity.getStreamFaviconUrl());
        }

        if (field.equals(Activity.Field.STREAM_SOURCE_URL.name())) {
          activity.setStreamSourceUrl(exoActivity.getStreamSourceUrl());
        }

        if (field.equals(Activity.Field.STREAM_TITLE.name())) {
          activity.setStreamTitle(exoActivity.getStreamTitle());
        }

        if (field.equals(Activity.Field.STREAM_URL.name())) {
          activity.setStreamUrl(exoActivity.getStreamUrl());
        }

        if (field.equals(Activity.Field.TEMPLATE_PARAMS.name())) {
          activity.setTemplateParams(exoActivity.getTemplateParams());
        }

        if (field.equals(Activity.Field.TITLE.name())) {
          activity.setTitle(exoActivity.getTitle());
        }

        if (field.equals(Activity.Field.TITLE_ID)) {
          activity.setTitleId(exoActivity.getTitleId());
        }

        if (field.equals(Activity.Field.URL)) {
          activity.setUrl(exoActivity.getUrl());
        }
        if (field.equals(Activity.Field.USER_ID)) {
          activity.setUserId(exoActivity.getUserId());
        }

      }

    } else {

      activity.setAppId(exoActivity.getAppId());
      activity.setBody(exoActivity.getBody());
      activity.setBodyId(exoActivity.getBodyId());
      activity.setExternalId(exoActivity.getExternalId());
      activity.setId(exoActivity.getId());
      activity.setMediaItems(exoActivity.getMediaItems());
      activity.setPostedTime(exoActivity.getPostedTime());
      activity.setPriority(exoActivity.getPriority());
      activity.setStreamFaviconUrl(exoActivity.getStreamFaviconUrl());
      activity.setStreamSourceUrl(exoActivity.getStreamSourceUrl());
      activity.setStreamTitle(exoActivity.getStreamTitle());
      activity.setStreamUrl(exoActivity.getStreamUrl());
      activity.setTemplateParams(exoActivity.getTemplateParams());
      activity.setTitle(exoActivity.getTitle());
      activity.setTitleId(exoActivity.getTitleId());
      activity.setUpdated(exoActivity.getUpdated());
      activity.setUrl(exoActivity.getUrl());
      activity.setUserId(exoActivity.getUserId());
    }
    return activity;

  }

  /**
   * Convert to os activities.
   *
   * @param activities the activities
   * @param fields the fields
   * @return the list
   */
  private List<Activity> convertToOSActivities(List<ExoSocialActivity> activities,
                                               Set<String> fields) {
    List<Activity> res = Lists.newArrayList();
    for (ExoSocialActivity activity : activities) {
      res.add(convertToOSActivity(activity, fields));
    }
    return res;
  }

}

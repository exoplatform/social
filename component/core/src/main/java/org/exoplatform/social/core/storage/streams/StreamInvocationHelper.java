/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.core.storage.streams;

import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.SocialServiceContext.ProcessType;
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.impl.StorageUtils;

public class StreamInvocationHelper {
  
  private static final Log LOG = ExoLogger.getLogger(StreamInvocationHelper.class);
  
  private static SocialServiceContext ctx = SocialServiceContextImpl.getInstance();

  /**
   * Invokes to records the activity to Stream
   * 
   * @param owner
   * @param activity
   * @param mentioners NULL is empty mentioner.
   * @return
   */
  public static ProcessContext save(Identity owner, ExoSocialActivity activity, String[] mentioners) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance(ProcessType.ASYNC);
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.NEW_ACTIVITY_PROCESS, ctx);
    processCtx.identity(owner).activity(activity).mentioners(mentioners);
    
    try {
      beforeAsync();
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.saveStream(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  private static boolean beforeAsync() {
    if (ctx.isAsync()) {
      return StorageUtils.persist();
    }
    
    return false;
  }
  
  public static ProcessContext update(ExoSocialActivity activity, long oldUpdated, String[] mentioners, String[] commenters) {
    //
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.UPDATE_ACTIVITY_PROCESS, ctx);
    processCtx.oldUpdate(oldUpdated).activity(activity).mentioners(mentioners).commenters(commenters);
    
    try {
      beforeAsync();
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.updateStream(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  //(parentActivity, mentioners, commenters);
  public static ProcessContext deleteComment(ExoSocialActivity activity, String[] mentioners, String[] commenters) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.DELETE_COMMENT_PROCESS, ctx);
    processCtx.activity(activity).mentioners(mentioners).commenters(commenters);
    
    try {
      beforeAsync();
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.deleteCommentStream(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  
  public static ProcessContext unLike(Identity removedLike, ExoSocialActivity activity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.UNLIKE_ACTIVITY_PROCESS, ctx);
    processCtx.identity(removedLike).activity(activity);
    
    try {
      beforeAsync();
      //don't use asynchronous because there is problem to get SessionProvider on Ecms side
      //org.exoplatform.services.cms.impl.Utils
      ctx.getServiceExecutor().execute(StreamProcessorFactory.unlikeActivity(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext like(Identity liker, ExoSocialActivity activity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LIKE_ACTIVITY_PROCESS, ctx);
    processCtx.identity(liker).activity(activity);
    
    try {
      beforeAsync();
      //don't use asynchronous because there is problem to get SessionProvider on Ecms side
      //org.exoplatform.services.cms.impl.Utils
      ctx.getServiceExecutor().execute(StreamProcessorFactory.likeActivity(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext deleteConnect(Identity sender, Identity receiver) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.DELETE_CONNECT_ACTIVITY_PROCESS, ctx);
    processCtx.sender(sender).receiver(receiver);
    
    try {
      beforeAsync();
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.deleteConnectStream(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext connect(Identity sender, Identity receiver) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance(ProcessType.ASYNC);
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.CONNECT_ACTIVITY_PROCESS, ctx);
    processCtx.sender(sender).receiver(receiver);
    
    try {
      beforeAsync();
      //
      ctx.getServiceExecutor().async(StreamProcessorFactory.connectStream(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext addSpaceMember(Identity owner, Identity spaceIdentity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance(ProcessType.ASYNC);
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.ADD_SPACE_MEMBER_ACTIVITY_PROCESS, ctx);
    processCtx.identity(owner).spaceIdentity(spaceIdentity);
    
    try {
      beforeAsync();
      //
      ctx.getServiceExecutor().async(StreamProcessorFactory.addSpaceMemberStream(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext removeSpaceMember(Identity owner, Identity spaceIdentity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.REMOVE_SPACE_MEMBER_ACTIVITY_PROCESS, ctx);
    processCtx.identity(owner).spaceIdentity(spaceIdentity);
    
    try {
      beforeAsync();
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.removeSpaceMemberStream(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createFeedActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance(ProcessType.ASYNC);
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      //
      ctx.getServiceExecutor().async(StreamProcessorFactory.createFeedActivityRef(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createConnectionsActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance(ProcessType.ASYNC);
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      //
      ctx.getServiceExecutor().async(StreamProcessorFactory.createConnectionsActivityRef(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createMySpacesActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance(ProcessType.ASYNC);
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      //
      ctx.getServiceExecutor().async(StreamProcessorFactory.createMySpacesActivityRef(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createMyActivitiesActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance(ProcessType.ASYNC);
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      //
      ctx.getServiceExecutor().async(StreamProcessorFactory.createMyActivitiesActivityRef(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createSpaceActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance(ProcessType.ASYNC);
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      //
      ctx.getServiceExecutor().async(StreamProcessorFactory.createSpaceActivityRef(), processCtx);
    } finally {
      LOG.info(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
}

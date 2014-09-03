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
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.impl.StorageUtils;

public class StreamInvocationHelper {
  
  private static final Log LOG = ExoLogger.getLogger(StreamInvocationHelper.class);
  
  private static SocialServiceContext ctx = SocialServiceContextImpl.getInstance();

  /**
   * Invokes to records the activity to Stream
   * 
   * @param owner
   * @param entity
   * @param mentioners NULL is empty mentioner.
   * @return
   */
  public static ProcessContext save(Identity owner, ActivityEntity entity, String[] mentioners) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.NEW_ACTIVITY_RELATIONS_PROCESS, ctx);
    processCtx.identity(owner).activityEntity(entity).mentioners(mentioners);
    
    try {
      if (ctx.isAsync()) {
        //
        ctx.getServiceExecutor().async(StreamProcessorFactory.saveStream(), processCtx);
      } else {
        ctx.getServiceExecutor().execute(StreamProcessorFactory.saveStream(), processCtx);
      }
      
    } finally {
      if (ctx.isTraced()) {
        LOG.debug(processCtx.getTraceLog());
      }
      
    }
    
    return processCtx;
  }
  
  /**
   * Invokes to records the activity to Stream
   * 
   * @param owner
   * @param activity
   * @param mentioners NULL is empty mentioner.
   * @return
   */
  public static ProcessContext savePoster(Identity owner, ActivityEntity entity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.NEW_ACTIVITY_PROCESS, ctx);
    processCtx.identity(owner).activityEntity(entity);
    
    try {
      //beforeAsync(); Why do we need to save here? Can make the problem with ADD_PROPERTY
      ctx.getServiceExecutor().execute(StreamProcessorFactory.savePoster(), processCtx);
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  private static boolean beforeAsync() {
    if (ctx.isAsync()) {
      return StorageUtils.persist();
    }
    return false;
  }
  
  public static ProcessContext update(ExoSocialActivity activity, long oldUpdated) {
    //
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.UPDATE_ACTIVITY_PROCESS, ctx);
    processCtx.activity(activity).oldLastUpdated(oldUpdated);
    
    try {
      if (ctx.isAsync()) {
        processCtx.getTraceElement().start();
        beforeAsync();
        ctx.getServiceExecutor().async(StreamProcessorFactory.updateStream(), processCtx);
        processCtx.getTraceElement().end();
      } else {
        ctx.getServiceExecutor().execute(StreamProcessorFactory.updateStream(), processCtx);
      }
      
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext updateHidable(Identity owner, ActivityEntity entity, ExoSocialActivity activity) {
    //
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.UPDATE_ACTIVITY_REF, ctx);
    processCtx.activity(activity).mentioners(entity.getMentioners()).identity(owner);
    
    try {
      if (ctx.isAsync()) {
        beforeAsync();
        ctx.getServiceExecutor().async(StreamProcessorFactory.updateHidable(), processCtx);
      } else {
        ctx.getServiceExecutor().execute(StreamProcessorFactory.updateHidable(), processCtx);
      }
      
    } finally {
      if (ctx.isTraced()) {
        LOG.debug(processCtx.getTraceLog());
      }
      
    }
    
    return processCtx;
  }
  
  public static ProcessContext updateCommenter(Identity commenter, ActivityEntity entity, String[] commenters, long oldUpdated) {
    //
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.UPDATE_ACTIVITY_COMMENTER_PROCESS, ctx);
    processCtx.identity(commenter).activityEntity(entity).commenters(commenters).oldLastUpdated(oldUpdated);
    
    try {
      //beforeAsync(); this point can make the problem with ADD_PROPERTY exception
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.updateCommenter(), processCtx);
    } finally {
      LOG.debug(processCtx.getTraceLog());
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
      //beforeAsync(); //this point can make the problem with ADD_PROPERTY exception
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.deleteCommentStream(), processCtx);
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  
  public static ProcessContext unLike(Identity removedLike, ExoSocialActivity activity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.UNLIKE_ACTIVITY_PROCESS, ctx);
    processCtx.identity(removedLike).activity(activity);
    
    try {
      //beforeAsync();
      ctx.getServiceExecutor().execute(StreamProcessorFactory.unlikeActivity(), processCtx);
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext like(Identity liker, ExoSocialActivity activity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LIKE_ACTIVITY_PROCESS, ctx);
    processCtx.identity(liker).activity(activity);
    
    try {
      //beforeAsync(); this point can make the problem with ADD_PROPERTY exception
      //don't use asynchronous because there is problem to get SessionProvider on Ecms side
      //org.exoplatform.services.cms.impl.Utils
      ctx.getServiceExecutor().execute(StreamProcessorFactory.likeActivity(), processCtx);
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext deleteConnect(Identity sender, Identity receiver) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.DELETE_CONNECT_ACTIVITY_PROCESS, ctx);
    processCtx.sender(sender).receiver(receiver);
    
    try {
      //beforeAsync(); //this point can make the problem with ADD_PROPERTY exception
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.deleteConnectStream(), processCtx);
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext connect(Identity sender, Identity receiver) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.CONNECT_ACTIVITY_PROCESS, ctx);
    processCtx.sender(sender).receiver(receiver);
    
    try {
      if(ctx.isAsync()) {
        beforeAsync();
        ctx.getServiceExecutor().async(StreamProcessorFactory.connectStream(), processCtx);
      } else {
        ctx.getServiceExecutor().execute(StreamProcessorFactory.connectStream(), processCtx);
      }
      
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext addSpaceMember(Identity owner, Identity spaceIdentity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.ADD_SPACE_MEMBER_ACTIVITY_PROCESS, ctx);
    processCtx.identity(owner).spaceIdentity(spaceIdentity);
    
    try {
      if(ctx.isAsync()) {
        beforeAsync();
        //
        ctx.getServiceExecutor().async(StreamProcessorFactory.addSpaceMemberStream(), processCtx);
      } else {
        ctx.getServiceExecutor().execute(StreamProcessorFactory.addSpaceMemberStream(), processCtx);
      }
      
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext removeSpaceMember(Identity owner, Identity spaceIdentity) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.REMOVE_SPACE_MEMBER_ACTIVITY_PROCESS, ctx);
    processCtx.identity(owner).spaceIdentity(spaceIdentity);
    
    try {
      //beforeAsync(); //this point can make the problem with ADD_PROPERTY exception
      //
      ctx.getServiceExecutor().execute(StreamProcessorFactory.removeSpaceMemberStream(), processCtx);
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createFeedActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      if(ctx.isAsync()) {
        beforeAsync();
        //
        ctx.getServiceExecutor().async(StreamProcessorFactory.createFeedActivityRef(), processCtx);
      } else {
        //
        ctx.getServiceExecutor().execute(StreamProcessorFactory.createFeedActivityRef(), processCtx);
      }
      
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createConnectionsActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      if(ctx.isAsync()) {
        //
        beforeAsync();     
        ctx.getServiceExecutor().async(StreamProcessorFactory.createConnectionsActivityRef(), processCtx);
      } else {
        //
        ctx.getServiceExecutor().execute(StreamProcessorFactory.createConnectionsActivityRef(), processCtx);
      }
      
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createMySpacesActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      if(ctx.isAsync()) {
        //
        beforeAsync();
        ctx.getServiceExecutor().async(StreamProcessorFactory.createMySpacesActivityRef(), processCtx);
      } else {
        //
        ctx.getServiceExecutor().execute(StreamProcessorFactory.createMySpacesActivityRef(), processCtx);
      }
      
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createMyActivitiesActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      if(ctx.isAsync()) {
        processCtx.getTraceElement().start();
        //
        beforeAsync();
        ctx.getServiceExecutor().async(StreamProcessorFactory.createMyActivitiesActivityRef(), processCtx);
        processCtx.getTraceElement().end();
      } else {
        //
        ctx.getServiceExecutor().execute(StreamProcessorFactory.createMyActivitiesActivityRef(), processCtx);
      }
      
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext createSpaceActivityRef(Identity owner, List<ExoSocialActivity> list) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LAZY_UPGRADE_STREAM_PROCESS, ctx);
    processCtx.identity(owner).activities(list);
    
    try {
      if(ctx.isAsync()) {
        //
        beforeAsync();
        ctx.getServiceExecutor().async(StreamProcessorFactory.createSpaceActivityRef(), processCtx);
      } else {
        //
        ctx.getServiceExecutor().execute(StreamProcessorFactory.createSpaceActivityRef(), processCtx);
      }
      
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext loadFeed(Identity owner) {
    //
    SocialServiceContext ctx = SocialServiceContextImpl.getInstance();
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.LOAD_ACTIVITIES_STREAM_PROCESS, ctx);
    processCtx.identity(owner);
    
    try {
      processCtx.getTraceElement().start();
      ctx.getServiceExecutor().async(StreamProcessorFactory.loadFeed(), processCtx);
      processCtx.getTraceElement().end();
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
  
  public static ProcessContext addMentioners(ExoSocialActivity activity, String[] mentioners) {
    //
    StreamProcessContext processCtx = StreamProcessContext.getIntance(StreamProcessContext.UPDATE_ACTIVITY_MENTIONER_PROCESS, ctx);
    processCtx.activity(activity).mentioners(mentioners);
    
    try {
      processCtx.getTraceElement().start();
      ctx.getServiceExecutor().async(StreamProcessorFactory.addMentioners(), processCtx);
      processCtx.getTraceElement().end();
    } finally {
      LOG.debug(processCtx.getTraceLog());
    }
    
    return processCtx;
  }
}

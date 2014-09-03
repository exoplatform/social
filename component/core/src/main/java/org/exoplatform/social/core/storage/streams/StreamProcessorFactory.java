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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.impl.SocialServiceContextImpl;
import org.exoplatform.social.common.service.utils.ObjectHelper;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;

public class StreamProcessorFactory {

  public StreamProcessorFactory() {
  }
  
  
  private static ActivityStreamStorage getStreamStorage() {
    return (ActivityStreamStorage) PortalContainer.getInstance().getComponentInstanceOfType(ActivityStreamStorage.class);
  }
  
  private static ActivityStorage getActivityStorage() {
    return (ActivityStorage) PortalContainer.getInstance().getComponentInstanceOfType(ActivityStorage.class);
  }
  /**
   * Build Save Stream processor
   * @return
   */
  public static SocialChromatticAsyncProcessor saveStream() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().save(processContext);
        return processContext;
      }

    };
  }
  
  /**
   * Build Save only these streams what own by poster.
   * 
   * @return
   */
  public static SocialChromatticAsyncProcessor savePoster() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().savePoster(processContext);
        return processContext;
      }

    };
  }
  
  /**
   * Build Update Stream processor
   * @return
   */
  public static SocialChromatticAsyncProcessor updateStream() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().update(processContext);
        return processContext;
      }

    };
  }
  
  /**
   * Build Update Activity References
   * @return
   */
  public static SocialChromatticAsyncProcessor updateHidable() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().updateHidable(processContext);
        return processContext;
      }

    };
  }
  
  /**
   * Build Update Commenter processor
   * @return
   */
  public static SocialChromatticAsyncProcessor updateCommenter() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().updateCommenter(processContext);
        return processContext;
      }

    };
  }
  
  /**
   * Build Delete comment Stream processor
   * @return
   */
  public static SocialChromatticAsyncProcessor deleteCommentStream() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().deleteComment(processContext);
        return processContext;
      }

    };
  }
  
  /**
   * Build Unlike processor
   * @return
   */
  public static SocialChromatticAsyncProcessor unlikeActivity() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext ctx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        getStreamStorage().unLike(ctx.getIdentity(), ctx.getActivity());
        return processContext;
      }

    };
  }
  
  /**
   * Build unlike processor
   * @return
   */
  public static SocialChromatticAsyncProcessor likeActivity() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext ctx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        getStreamStorage().like(ctx.getIdentity(), ctx.getActivity());
        return processContext;
      }

    };
  }
  
  /**
   * Build Delete Connection processor
   * @return
   */
  public static SocialChromatticAsyncProcessor deleteConnectStream() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext ctx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        getStreamStorage().deleteConnect(ctx.getSender(), ctx.getReceiver());
        return processContext;
      }

    };
  }
  
  /**
   * Build Delete Connection processor
   * @return
   */
  public static SocialChromatticAsyncProcessor connectStream() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext ctx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        getStreamStorage().connect(ctx.getSender(), ctx.getReceiver());
        return processContext;
      }

    };
  }
  
  /**
   * Build Add Space Member processor
   * @return
   */
  public static SocialChromatticAsyncProcessor addSpaceMemberStream() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().addSpaceMember(processContext);
        return processContext;
      }

    };
  }
  
  /**
   * Build Remove Space Member processor
   * @return
   */
  public static SocialChromatticAsyncProcessor removeSpaceMemberStream() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().removeSpaceMember(processContext);
        return processContext;
      }

    };
  }
  
  /**
   * Build Create ActivityRef for Feed Stream
   * @return
   */
  public static SocialChromatticAsyncProcessor createFeedActivityRef() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        getStreamStorage().createFeedActivityRef(streamCtx.getIdentity(), streamCtx.getActivities());
        return processContext;
      }

    };
  }
  
  /**
   * Build Create ActivityRef for Connections Stream
   * @return
   */
  public static SocialChromatticAsyncProcessor createConnectionsActivityRef() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        getStreamStorage().createConnectionsActivityRef(streamCtx.getIdentity(), streamCtx.getActivities());
        return processContext;
      }

    };
  }
  
  /**
   * Build Create ActivityRef for My Spaces Stream
   * @return
   */
  public static SocialChromatticAsyncProcessor createMySpacesActivityRef() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        getStreamStorage().createMySpacesActivityRef(streamCtx.getIdentity(), streamCtx.getActivities());
        return processContext;
      }
    };
  }
  
  /**
   * Build Create ActivityRef for My Activities Stream
   * @return
   */
  public static SocialChromatticAsyncProcessor createMyActivitiesActivityRef() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        
        getStreamStorage().createMyActivitiesActivityRef(streamCtx.getIdentity(), streamCtx.getActivities());
        return processContext;
      }

    };
  }
  
  /**
   * Build Create ActivityRef for Space Stream
   * @return
   */
  public static SocialChromatticAsyncProcessor createSpaceActivityRef() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        
        getStreamStorage().createSpaceActivityRef(streamCtx.getIdentity(), streamCtx.getActivities());
        return processContext;
      }

    };
  }
  
  /**
   * Build the process to load Feed activity asynchronous
   * @return
   */
  public static SocialChromatticAsyncProcessor loadFeed() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, processContext);
        
        getActivityStorage().getActivityFeed(streamCtx.getIdentity(), 0, 21);
        return processContext;
      }

    };
  }
  
  /**
   * Build Update Stream References in case mention
   * @return
   */
  public static SocialChromatticAsyncProcessor addMentioners() {
    return new SocialChromatticAsyncProcessor(SocialServiceContextImpl.getInstance()) {

      @Override
      protected ProcessContext execute(ProcessContext processContext) throws Exception {
        getStreamStorage().addMentioners(processContext);
        return processContext;
      }

    };
  }
}

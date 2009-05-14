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

//import org.apache.shindig.social.core.util.ContainerConf;
import org.apache.shindig.social.opensocial.service.DataRequestHandler;
import org.apache.shindig.social.opensocial.service.RequestItem;
import org.apache.shindig.social.opensocial.spi.SocialSpiException;
import org.apache.shindig.social.ResponseError;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;
import org.json.JSONException;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.opensocial.spi.ExoActivityService;
import org.exoplatform.social.opensocial.spi.ExoPeopleService;
import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.io.IOException;

public class ExoContainerHandler  extends DataRequestHandler {

  private final ExoActivityService activityService;
  private final ExoPeopleService peopleService;
//    private static ContainerConf containerConf;

  private static final String POST_PATH = "/samplecontainer/{type}/{doevil}";


  public ExoContainerHandler() {
    //super(containerConf);
	ExoContainer container = ExoContainerContext.getCurrentContainer();


    this.activityService = (ExoActivityService) container.getComponentInstanceOfType(ExoActivityService.class);
    this.peopleService = (ExoPeopleService) container.getComponentInstanceOfType(ExoPeopleService.class);
  }

  /**
   * We don't support any delete methods right now.
   */
  protected Future<?> handleDelete(RequestItem request) throws SocialSpiException {
    throw new SocialSpiException(ResponseError.NOT_IMPLEMENTED, null);
  }

  /**
   * We don't distinguish between put and post for these urls.
   */
  protected Future<?> handlePut(RequestItem request) throws SocialSpiException {
    return handlePost(request);
  }

  /**
   * Handles /samplecontainer/setstate and /samplecontainer/setevilness/{doevil}. TODO(doll): These
   * urls aren't very resty. Consider changing the samplecontainer.html calls post.
   */
  protected Future<?> handlePost(RequestItem request) throws SocialSpiException {
    request.applyUrlTemplate(POST_PATH);
    String type = request.getParameter("type");
    if (type.equals("setevilness")) {
      throw new SocialSpiException(ResponseError.NOT_IMPLEMENTED,
          "evil data has not been implemented yet");
    }

    return ImmediateFuture.newInstance(null);
  }

  /**
   * Handles /samplecontainer/dumpstate
   */
  protected Future<?> handleGet(RequestItem request) {
    throw new SocialSpiException(ResponseError.NOT_IMPLEMENTED, null);
  }

}
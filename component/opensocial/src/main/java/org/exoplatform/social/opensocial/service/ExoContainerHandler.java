package org.exoplatform.social.opensocial.service;

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
import org.exoplatform.social.opensocial.spi.ExoActivityService;
import org.exoplatform.social.opensocial.spi.ExoPeopleService;
import com.google.inject.Inject;

import java.util.concurrent.Future;
import java.io.IOException;

public class ExoContainerHandler  extends DataRequestHandler {

//  private final ExoActivityService activityService;
//  private final ExoPeopleService peopleService;


  private static final String POST_PATH = "/samplecontainer/{type}/{doevil}";


  public ExoContainerHandler() {
/*    ExoContainer container = ExoContainerContext.getCurrentContainer();


    this.activityService = container.getComponentInstanceOfType(ExoActivityService.class);
    this.peopleService = container.getComponentInstanceOfType(ExoPeopleService.class);*/
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
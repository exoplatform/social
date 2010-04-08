package org.exoplatform.social.opensocial;

import org.apache.shindig.auth.SecurityTokenDecoder;
import org.apache.shindig.auth.UrlParameterAuthenticationHandler;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class ExoUrlAuthenticationHandler extends UrlParameterAuthenticationHandler {

  @Inject
  public ExoUrlAuthenticationHandler(@Named("exo.auth.decoder") SecurityTokenDecoder securityTokenDecoder) {
    super(securityTokenDecoder);
  }

}

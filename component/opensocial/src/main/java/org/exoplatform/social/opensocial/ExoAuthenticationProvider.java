package org.exoplatform.social.opensocial;

import java.util.List;

import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.auth.UrlParameterAuthenticationHandler;
import org.apache.shindig.social.core.oauth.AuthenticationHandlerProvider;
import org.apache.shindig.social.core.oauth.OAuthAuthenticationHandler;

import com.google.inject.Inject;

public class ExoAuthenticationProvider extends AuthenticationHandlerProvider {


  @Inject
    public ExoAuthenticationProvider(UrlParameterAuthenticationHandler urlParam,
                                   OAuthAuthenticationHandler threeLeggedOAuth,
                                   AnonymousAuthenticationHandler anonymous) {
    super(urlParam, threeLeggedOAuth, anonymous);
    for (AuthenticationHandler handler : handlers) {
      
    }
  }




    public List<AuthenticationHandler> get() {
      return handlers;
    }
  

}

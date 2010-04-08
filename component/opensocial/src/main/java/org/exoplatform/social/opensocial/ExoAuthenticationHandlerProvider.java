package org.exoplatform.social.opensocial;

import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.social.core.oauth.AuthenticationHandlerProvider;
import org.apache.shindig.social.core.oauth.OAuthAuthenticationHandler;

import com.google.inject.Inject;

public class ExoAuthenticationHandlerProvider extends AuthenticationHandlerProvider {


    @Inject
    public ExoAuthenticationHandlerProvider(ExoUrlAuthenticationHandler urlParam,
                                   OAuthAuthenticationHandler threeLeggedOAuth,
                                   AnonymousAuthenticationHandler anonymous) {
    
    super(urlParam, threeLeggedOAuth, anonymous);

  }


 

}

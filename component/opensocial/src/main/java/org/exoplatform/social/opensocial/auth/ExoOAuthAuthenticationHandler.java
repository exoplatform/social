package org.exoplatform.social.opensocial.auth;

import java.io.File;

import javax.servlet.http.HttpServletRequest;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.util.TimeSource;
import org.apache.shindig.social.core.oauth.OAuthAuthenticationHandler;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;

import com.google.inject.Inject;
import com.google.inject.name.Named;


/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jul 7, 2010
 * Time: 5:34:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExoOAuthAuthenticationHandler extends OAuthAuthenticationHandler {

  private String portalContainerName;

  @Inject
  public ExoOAuthAuthenticationHandler(OAuthDataStore store, @Named("shindig.oauth.legacy-body-signing") boolean allowLegacyBodySigning) {
    super(store, allowLegacyBodySigning);
  }

  public String getName() {
    return super.getName();
  }

  public String getPortalContainerName() {
    if(portalContainerName == null){
      RestPortalContainerNameConfig containerNameConfigRest = (RestPortalContainerNameConfig) PortalContainer.getInstance().getComponentInstanceOfType(RestPortalContainerNameConfig.class);
      portalContainerName = containerNameConfigRest.getContainerName();
    }
    
    return portalContainerName;
  }

  public SecurityToken getSecurityTokenFromRequest(HttpServletRequest request) throws InvalidAuthenticationException {
    final SecurityToken securityToken = super.getSecurityTokenFromRequest(request);

    final BasicBlobCrypter crypter;
    final String portalContainer;
    final String domain;
    try {
      String keyFile = getKeyFilePath();
      crypter = new BasicBlobCrypter(new File(keyFile));
      crypter.timeSource = new TimeSource();

      portalContainer = getPortalContainerName(); 
      domain = securityToken.getDomain();
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

    final ExoBlobCrypterSecurityToken crypterSecurityToken = new ExoBlobCrypterSecurityToken(crypter, portalContainer, domain);
    crypterSecurityToken.setOwnerId(securityToken.getOwnerId());
    crypterSecurityToken.setAppUrl(securityToken.getAppUrl());
    crypterSecurityToken.setViewerId(securityToken.getViewerId());
    crypterSecurityToken.setPortalContainer(portalContainer);

    return crypterSecurityToken;
  }

  public String getWWWAuthenticateHeader(String realm) {
    return super.getWWWAuthenticateHeader(realm);
  }

     /**
    * Method returns a path to the file containing the encryption key
    */
  private String getKeyFilePath(){
    J2EEServerInfo info = new J2EEServerInfo();
    String confPath = info.getExoConfigurationDirectory();
    File keyFile = null;

    if (confPath != null) {
      File confDir = new File(confPath);
      if (confDir != null && confDir.exists() && confDir.isDirectory()) {
         keyFile = new File(confDir, "gadgets/key.txt");
      }
    }

    if (keyFile == null) {
      keyFile = new File("key.txt");
    }

    return keyFile.getAbsolutePath();
  }
}
package org.exoplatform.social.opensocial.oauth;

import org.apache.shindig.social.opensocial.oauth.OAuthLookupService;
import org.apache.shindig.social.core.oauth.OAuthSecurityToken;
import org.apache.shindig.auth.SecurityToken;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.URISyntaxException;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import net.oauth.*;

public class ExoContainerOAuthLookupService  implements OAuthLookupService {
  // If we were a real social network this would probably be a function
  private static Map<String, String> sampleContainerUrlToAppIdMap = Maps.immutableMap(
      "http://localhost:8080/gadgets/files/samplecontainer/examples/SocialHelloWorld.xml",
      "7810",
      "http://localhost:8080/gadgets/files/samplecontainer/examples/SocialActivitiesWorld.xml",
      "8355"
  );

  // If we were a real social network we would probably be keeping track of this in a db somewhere
  private static Map<String, ArrayList<String>> sampleContainerAppInstalls = Maps.immutableMap(
      "john.doe", Lists.newArrayList("7810", "8355")
  );

  // If we were a real social network we would establish shared secrets with each of our gadgets
  private static Map<String, String> sampleContainerSharedSecrets = Maps.immutableMap(
      "7810", "SocialHelloWorldSharedSecret",
      "8355", "SocialActivitiesWorldSharedSecret"
  );

  public boolean thirdPartyHasAccessToUser(OAuthMessage message, String appUrl, String userId) {
    String appId = getAppId(appUrl);
    return hasValidSignature(message, appUrl, appId)
        && userHasAppInstalled(userId, appId);
  }

  private boolean hasValidSignature(OAuthMessage message, String appUrl, String appId) {
    String sharedSecret = sampleContainerSharedSecrets.get(appId);
    if (sharedSecret == null) {
      return false;
    }

    OAuthServiceProvider provider = new OAuthServiceProvider(null, null, null);
    OAuthConsumer consumer = new OAuthConsumer(null, appUrl, sharedSecret, provider);
    OAuthAccessor accessor = new OAuthAccessor(consumer);

    SimpleOAuthValidator validator = new SimpleOAuthValidator();
    try {
      validator.validateMessage(message, accessor);
    } catch (OAuthException e) {
      return false;
    } catch (IOException e) {
      return false;
    } catch (URISyntaxException e) {
      return false;
    }

    return true;
  }

  private boolean userHasAppInstalled(String userId, String appId) {
    List<String> appInstalls = sampleContainerAppInstalls.get(userId);
    if (appInstalls != null) {
      for (String appInstall : appInstalls) {
        if (appInstall.equals(appId)) {
          return true;
        }
      }
    }

    return false;
  }

  public SecurityToken getSecurityToken(String appUrl, String userId) {
    return new OAuthSecurityToken(userId, appUrl, getAppId(appUrl), "samplecontainer");
  }

  private String getAppId(String appUrl) {
    return sampleContainerUrlToAppIdMap.get(appUrl);
  }

}
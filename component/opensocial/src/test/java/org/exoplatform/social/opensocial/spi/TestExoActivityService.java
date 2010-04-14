package org.exoplatform.social.opensocial.spi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.shindig.social.core.model.ActivityImpl;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.commons.testing.mock.SimpleMockOrganizationService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.opensocial.ExoBlobCrypterSecurityToken;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceIdentityProvider;
import org.exoplatform.social.space.SpaceService;
import org.testng.annotations.Test;

import static org.testng.Assert.*;


@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.opensocial.configuration.xml")})
public class TestExoActivityService extends AbstractJCRTestCase {

  


 
@Test
  public void testCreateActivityOnOwnStream() throws Exception {
    ExoActivityService activityService = new ExoActivityService();
    SimpleMockOrganizationService orgniaztionService = getComponent(OrganizationService.class);
    orgniaztionService.addMemberships("root", "member:/platform/users");
    IdentityManager identityManager = getComponent(IdentityManager.class);

    Identity root = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    String rootIdentityId = root.getId();
    
    UserId viewer = new UserId(UserId.Type.viewer, null); 
    GroupId self = new GroupId(GroupId.Type.self, null);
    String appId = "test";
    Set<String> fields = Collections.emptySet();
    Activity activity = new ActivityImpl();
    activity.setBody("root chez lui");
    activity.setTitle("Root Root");
    FakeToken token = new FakeToken();
    token.ownerId = rootIdentityId;
    token.viewerId = rootIdentityId;
    activityService.createActivity(viewer, self, appId, fields, activity, token);
    
    
    
    ActivityManager activityManager = getComponent(ActivityManager.class);
    List<org.exoplatform.social.core.activitystream.model.Activity> activities = activityManager.getActivities(root); 
    assertNotNull(activities);
    assertEquals(activities.size(), 1);
    org.exoplatform.social.core.activitystream.model.Activity act = activities.get(0);
    assertEquals(act.getBody(), activity.getBody() );
    assertEquals(act.getUserId(),  token.viewerId);
    
    
  }
  
@Test
  public void testCreateActivityOnSpaceStream() throws Exception {
    ExoActivityService activityService = new ExoActivityService();
    SimpleMockOrganizationService orgniaztionService = getComponent(OrganizationService.class);
    orgniaztionService.addMemberships("root", "member:/platform/users");
    IdentityManager identityManager = getComponent(IdentityManager.class);   
    Identity root = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    String rootIdentityId = root.getId();
    
    
    Space space = new Space();
    space.setName("space1");
    space.setGroupId("/spaces/space1");
    space.setDescription("desc");
    SpaceService spaceService = getComponent(SpaceService.class);
    spaceService.saveSpace(space, true);
   

    Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "space:space1");
    String space1ID = spaceIdentity.getId();
    
    UserId viewer = new UserId(UserId.Type.viewer, null); 
    GroupId space1Group = new GroupId(GroupId.Type.groupId, space1ID);
    String appId = "test";
    Set<String> fields = Collections.emptySet();
    Activity activity = new ActivityImpl();
    activity.setBody("root sur space1");
    activity.setTitle("Root Root");
    FakeToken token = new FakeToken();
    token.ownerId = rootIdentityId;
    token.viewerId = rootIdentityId;
    activityService.createActivity(viewer, space1Group, appId, fields, activity, token);
    
    
    ActivityManager activityManager = getComponent(ActivityManager.class);
    List<org.exoplatform.social.core.activitystream.model.Activity> activities = activityManager.getActivities(spaceIdentity); 
    assertNotNull(activities);
    assertEquals(activities.size(), 1);
    org.exoplatform.social.core.activitystream.model.Activity act = activities.get(0);
    assertEquals(act.getBody(), activity.getBody()  );
    assertEquals(act.getUserId(),  space1ID);
    
  }
  
  
  
  public class FakeToken extends ExoBlobCrypterSecurityToken {
    public String ownerId;
    public String viewerId;
    public FakeToken() {
      super(null, "default", "eXo");
    }

    public String getActiveUrl() {
      return "http://localhost:8080/social/social/rpc";
    }

    public String getAppId() {
      return getAppUrl();
    }

    public String getAppUrl() {
      return "http://localhost:8080/rest-socialdemo/jcr/repository/portal-system/production/app:gadgets/app:statusUpdate2/app:data/app:resources/activities2.xml";
    }

    public String getPortalContainer() {
      return "portal";
    }

    public String getHostName() {
    return "localhost";
    }


    public long getModuleId() {
      return 1566685858;
    }

    public String getOwnerId() {
      return ownerId;
    }

    public String getTrustedJson() {
      return "trusted";
    }

 
    public String getViewerId() {
      return viewerId;
    }

    public boolean isAnonymous() {
      return false;
    }

  }
  
}

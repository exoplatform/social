package org.exoplatform.social.benches;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.space.SpaceService;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

@SuppressWarnings("deprecation")
@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.benches.configuration.xml")})
public class TestDataInjector extends  AbstractJCRTestCase  {
  
  @Test
  public void injectPeople() {
    ActivityManager activityManager = getComponent(ActivityManager.class);
    
    OrganizationService organizationService = mock(OrganizationService.class);
    when(organizationService.getUserHandler()).thenReturn(new FakeUserHandler());
    ExoContainerContext.getCurrentContainer().unregisterComponent(OrganizationService.class);
    ExoContainerContext.getCurrentContainer().registerComponentInstance(OrganizationService.class, organizationService);    
    
    IdentityManager identityManager = getComponent(IdentityManager.class);
    RelationshipManager relationshipManager = getComponent(RelationshipManager.class);
    SpaceService spaceService = getComponent(SpaceService.class);
 
    
    DataInjector injector = new DataInjector(activityManager, identityManager, relationshipManager, spaceService, organizationService);
    Collection<Identity> identities = injector.generatePeople(10);    
    assertEquals(identities.size(), 10);
    for (Identity identity : identities) {
      assertNotNull(identity.getId());
    }
    
  }
  
  
  @Test
  public void initRandomUser() {
    
    OrganizationService organizationService = mock(OrganizationService.class);
    UserHandler handler = new FakeUserHandler();
    when(organizationService.getUserHandler()).thenReturn(handler);
    ExoContainerContext.getCurrentContainer().unregisterComponent(OrganizationService.class);
    ExoContainerContext.getCurrentContainer().registerComponentInstance(OrganizationService.class, organizationService);  
    
    DataInjector injector = new DataInjector(null, null, null, null, organizationService);
    User user = new SimpleUser("foo");
    injector.initRandomUser(user, "foo");
    assertNotNull(user.getFirstName());
    assertNotNull(user.getLastName());
  }
  
  @Test
  public void injectRelations() {
    ActivityManager activityManager = getComponent(ActivityManager.class);
    
    OrganizationService organizationService = mock(OrganizationService.class);
    when(organizationService.getUserHandler()).thenReturn(new FakeUserHandler());
    ExoContainerContext.getCurrentContainer().unregisterComponent(OrganizationService.class);
    ExoContainerContext.getCurrentContainer().registerComponentInstance(OrganizationService.class, organizationService);    
    
    IdentityManager identityManager = getComponent(IdentityManager.class);
    RelationshipManager relationshipManager = getComponent(RelationshipManager.class);
    SpaceService spaceService = getComponent(SpaceService.class);
 
    
    DataInjector injector = new DataInjector(activityManager, identityManager, relationshipManager, spaceService, organizationService);
    injector.generatePeople(10);   /// injecting relations requires some pple
    Collection<Relationship> relationships = injector.generateRelations(10);    
    assertEquals(relationships.size(), 10);
    for (Relationship relationship : relationships) {
      assertNotNull(relationship.getId());
    }   
  }
  
  
  
  @Test
  public void injectActivities() {
    ActivityManager activityManager = getComponent(ActivityManager.class);
    
    OrganizationService organizationService = mock(OrganizationService.class);
    when(organizationService.getUserHandler()).thenReturn(new FakeUserHandler());
    ExoContainerContext.getCurrentContainer().unregisterComponent(OrganizationService.class);
    ExoContainerContext.getCurrentContainer().registerComponentInstance(OrganizationService.class, organizationService);    
    
    IdentityManager identityManager = getComponent(IdentityManager.class);
    RelationshipManager relationshipManager = getComponent(RelationshipManager.class);
    SpaceService spaceService = getComponent(SpaceService.class);
 
    
    DataInjector injector = new DataInjector(activityManager, identityManager, relationshipManager, spaceService, organizationService);
    injector.generatePeople(10);   /// injecting activities requires some pple
    Collection<Activity> activities = injector.generateActivities(10);    
    assertEquals(activities.size(), 10);
    for (Activity activity : activities) {
      assertNotNull(activity.getId());
    }   
  }
  
  
  class FakeUserHandler implements UserHandler {
    Map<String,User> map;
    
    public FakeUserHandler() {
      map = new HashMap<String, User>();
    }
    
    public void addUserEventListener(UserEventListener listener) {
      // TODO Auto-generated method stub
      
    }

    public boolean authenticate(String username, String password) throws Exception {
      // TODO Auto-generated method stub
      return false;
    }

    public void createUser(User user, boolean broadcast) throws Exception {
      map.put(user.getUserName(), user);
    }

    public User createUserInstance() {
      // TODO Auto-generated method stub
      return null;
    }

    public User createUserInstance(String username) {
     return  new SimpleUser(username);
    }

    public ListAccess<User> findAllUsers() throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

    public User findUserByName(String userName) throws Exception {
 
      return map.get(userName);
    }

    public PageList<User> findUsers(Query query) throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

    public PageList<User> findUsersByGroup(String groupId) throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

    public ListAccess<User> findUsersByGroupId(String groupId) throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

    public ListAccess<User> findUsersByQuery(Query query) throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

    public PageList<User> getUserPageList(int pageSize) throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

    public User removeUser(String userName, boolean broadcast) throws Exception {
      // TODO Auto-generated method stub
      return null;
    }

    public void saveUser(User user, boolean broadcast) throws Exception {
      // TODO Auto-generated method stub
      
    }
    
    
  
    
  }
  public class SimpleUser implements User {
    String userName = null;
    String firstName;
    String lastName;
    String email;
    String password;

    public SimpleUser(String name) {
      this.userName = name;
    }

    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (!(obj instanceof User))
        return false;
      User other = (User) obj;
      return (userName.equals(other.getUserName()));
    }
    
    public int hashCode() {
      return super.hashCode();
    }
    
    public String toString() {
      return getFullName();
    }

    public Date getCreatedDate() {
      return new Date();
    }

   

    public String getFullName() {
      return firstName + " " + lastName;
    }

    public Date getLastLoginTime() {
      return new Date();
    }

 
    public String getOrganizationId() {
      return userName;
    }


    public void setCreatedDate(Date t) {
      // TODO Auto-generated method stub

    }


    public void setFullName(String s) {
      // TODO Auto-generated method stub

    }

    public void setLastLoginTime(Date t) {
      // TODO Auto-generated method stub

    }


    public void setOrganizationId(String organizationId) {
      // TODO Auto-generated method stub

    }

    public String getUserName() {
      return userName;
    }

    public void setUserName(String userName) {
      this.userName = userName;
    }

    public String getFirstName() {
      return firstName;
    }

    public void setFirstName(String firstName) {
      this.firstName = firstName;
    }

    public String getLastName() {
      return lastName;
    }

    public void setLastName(String lastName) {
      this.lastName = lastName;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }


  }
  
}

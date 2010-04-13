package org.exoplatform.social.benches;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.UserProfileHandler;


@SuppressWarnings("deprecation")
public class FakeOrganizationService implements OrganizationService {

  FakeUserHandler userHandler;
  public FakeOrganizationService() {
    userHandler = new FakeUserHandler();
  }
  
  public void addListenerPlugin(ComponentPlugin listener) throws Exception {
    // TODO Auto-generated method stub

  }

  public GroupHandler getGroupHandler() {
    // TODO Auto-generated method stub
    return null;
  }

  public MembershipHandler getMembershipHandler() {
    // TODO Auto-generated method stub
    return null;
  }

  public MembershipTypeHandler getMembershipTypeHandler() {
    // TODO Auto-generated method stub
    return null;
  }

  public UserHandler getUserHandler() {
    return userHandler;
  }

  public UserProfileHandler getUserProfileHandler() {
    // TODO Auto-generated method stub
    return null;
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

    public User saveUser(User user) throws Exception {
      createUser(user, true);
      return user;
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

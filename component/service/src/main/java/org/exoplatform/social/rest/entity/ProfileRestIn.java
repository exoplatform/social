package org.exoplatform.social.rest.entity;

public class ProfileRestIn {

  private String userName;

  private String firstName;

  private String lastName;

  private String password;

  private String email;

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

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
  
  public boolean isNotValid() {
    return isEmpty(this.userName) || isEmpty(this.email) 
         || isEmpty(this.firstName) || isEmpty(this.lastName); 
  }
  
  private boolean isEmpty(String input) {
    return input == null || input.length() == 0;
  }
}

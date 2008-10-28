package org.exoplatform.social.core.identity.model;


import java.util.Map;
import java.util.HashMap;


public class Profile {
  private Map<String, Object> properties = new HashMap<String, Object>();
  private Identity identity;
  private String id;

  public Profile(Identity id) {
    this.identity = id;
  }

  public Identity getIdentity() {
    return identity;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Object getProperty(String name) {
    return properties.get(name);
  }

  public void setProperty(String name, Object value) {
    properties.put(name, value);
  }

  public boolean contains(String name) {
    return properties.containsKey(name);  
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public void removeProperty(String name) {
    properties.remove(name);
  }

  /**
   * @deprecated 
   * @param name
   * @return
   */
  public Object getPropertyValue(String name) {
    return getProperty(name);
  }

  public String getFullName() {
    return getProperty("firstName") + " " + getProperty("lastName");
  }

}

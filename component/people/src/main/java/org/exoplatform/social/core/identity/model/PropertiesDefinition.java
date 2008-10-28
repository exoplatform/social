package org.exoplatform.social.core.identity.model;

import java.util.Map;
import java.util.HashMap;


public class PropertiesDefinition {
  private Map<String, String> propertiesDef = new HashMap<String, String>();

  // this will be replaced by an xml configuration file that will define all
  // the properties
  public PropertiesDefinition(){
    propertiesDef.put("firstName", "firstName");
    propertiesDef.put("lastName", "lastName");

    // multi value: List<String>
    propertiesDef.put("emails", "emails");
    
    propertiesDef.put("username", "username");

  }

  public String get(String name){
    return propertiesDef.get(name);
  }


}

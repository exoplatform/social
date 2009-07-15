package org.exoplatform.social.opensocial.model;

import org.apache.shindig.protocol.model.Exportablebean;
import org.exoplatform.social.opensocial.model.impl.SpaceImpl;

import com.google.inject.ImplementedBy;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Jan 08, 2009          
 */
@ImplementedBy(SpaceImpl.class)
@Exportablebean
public interface Space {

  /**
   * An enumeration of fields in the json Space object.
   */
  public static enum Field {
    /**
     * the json field for id of space
     */
    ID("id"),
    /**
     * the json field for displayName of space
     */
    DISPLAY_NAME("displayName");

    /**
     * the json key for this field.
     */
    private final String jsonString;

    /**
     * Construct the a field enum.
     * @param jsonString the json key for the field.
     */
    private Field(String jsonString) {
      this.jsonString = jsonString;
    }

    @Override
    public String toString() {
      return this.jsonString;
    }
  }
  
  public void setId(String id);
  
  public String getId();
  
  public void setDisplayName(String displayName);
  
  public String getDisplayName();

}

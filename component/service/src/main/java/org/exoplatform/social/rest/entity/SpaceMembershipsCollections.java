package org.exoplatform.social.rest.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpaceMembershipsCollections extends ResourceCollections{
  
  private List<Map<String, Object>> spaceMemberships = new ArrayList<Map<String, Object>>();
  
  public SpaceMembershipsCollections(int size, int offset, int limit) {
    super(size, offset, limit);
  }

  /**
   * @return the spaceMemberships
   */
  public List<Map<String, Object>> getSpaceMemberships() {
    return spaceMemberships;
  }

  /**
   * @param spaceMemberships the spaceMemberships to set
   */
  public void setSpaceMemberships(List<Map<String, Object>> spaceMemberships) {
    this.spaceMemberships = spaceMemberships;
  }

  @Override
  public Object getCollectionByFields(List<String> returnedProperties) {
    return extractInfo(returnedProperties, getSpaceMemberships());
  }
}

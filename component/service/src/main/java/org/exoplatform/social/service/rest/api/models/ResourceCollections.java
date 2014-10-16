package org.exoplatform.social.service.rest.api.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public abstract class ResourceCollections {

  private int size;
  private int offset;
  private int limit;
  
  public ResourceCollections(int size, int offset, int limit) {
    this.size = size;
    this.offset = offset;
    this.limit = limit;
  }
  
  public ResourceCollections() {
  }

  public abstract Object getCollectionByFields(List<String> returnedProperties);
  
  /**
   * @return the size
   */
  public int getSize() {
    return size;
  }
  /**
   * @param size the size to set
   */
  public void setSize(int size) {
    this.size = size;
  }
  /**
   * @return the offset
   */
  public int getOffset() {
    return offset;
  }
  /**
   * @param offset the offset to set
   */
  public void setOffset(int offset) {
    this.offset = offset;
  }
  /**
   * @return the limit
   */
  public int getLimit() {
    return limit;
  }
  /**
   * @param limit the limit to set
   */
  public void setLimit(int limit) {
    this.limit = limit;
  }
  
  public List<Map<String, Object>> extractInfo(List<String> returnedProperties, List<Map<String, Object>> elementInfos) {
    List<Map<String, Object>> returnedInfos = new ArrayList<Map<String,Object>>();
    for (Map<String, Object> elementInfo : elementInfos) {
      Map<String, Object> map = new LinkedHashMap<String, Object>();
      for (Map.Entry<String, Object> entry : elementInfo.entrySet()) {
        if (returnedProperties.contains(entry.getKey())) {
          map.put(entry.getKey(), entry.getValue());
        }
      }
      returnedInfos.add(map);
    }
    
    return returnedInfos;
  }
}

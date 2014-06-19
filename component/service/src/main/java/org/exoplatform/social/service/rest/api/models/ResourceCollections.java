package org.exoplatform.social.service.rest.api.models;


public class ResourceCollections {

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
}

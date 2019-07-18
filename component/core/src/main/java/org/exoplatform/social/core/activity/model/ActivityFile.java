package org.exoplatform.social.core.activity.model;

import java.io.InputStream;

public class ActivityFile {
  private String      uploadId;

  private String      storage;

  private String      name;

  private String      mimeType;

  private InputStream inputStream;

  private long        lastModified;

  public ActivityFile() {
  }

  /**
   * Constructor
   *
   * @param uploadId
   * @param storage
   */
  public ActivityFile(String uploadId, String storage) {
    this.uploadId = uploadId;
    this.storage = storage;
  }

  /**
   * Constructor
   *
   * @param uploadId
   * @param storage
   * @param name
   * @param mimeType
   * @param inputStream
   * @param lastModified
   * @throws Exception
   */
  public ActivityFile(String uploadId, String storage, String name, String mimeType, InputStream inputStream, long lastModified)
      throws Exception {
    this.uploadId = uploadId;
    this.storage = storage;
    this.name = name;
    this.mimeType = mimeType;
    this.inputStream=inputStream;
    this.lastModified = lastModified;
  }

  /**
   * Gets the file name.
   *
   * @return the file name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the file name.
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the mime type.
   *
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime type.
   *
   * @param s the new mime type
   */
  public void setMimeType(String s) {
    mimeType = s;
  }

  /**
   * Gets the last modified.
   *
   * @return the last modified
   */
  public long getLastModified() {
    return lastModified;
  }

  /**
   * Sets the last modified.
   *
   * @param lastModified the new last modified
   */
  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Gets the file uploadId.
   *
   * @return the uploadId
   */
  public String getUploadId() {
    return uploadId;
  }

  /**
   * Sets the uploadId
   *
   * @param uploadId
   */
  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  /**
   * Gets the file storage
   *
   * @return the file storage
   */
  public String getStorage() {
    return storage;
  }

  /**
   * Sets the file storage
   *
   * @param storage
   */
  public void setStorage(String storage) {
    this.storage = storage;
  }

  /**
   * Sets the file inputStream
   * 
   * @param inputStream
   */
  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * Gets the file inputStream
   * 
   * @return inputStream
   */
  public InputStream getInputStream() {
    return inputStream;
  }
}

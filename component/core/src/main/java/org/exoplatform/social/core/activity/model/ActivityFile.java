package org.exoplatform.social.core.activity.model;

public class ActivityFile {
    private String uploadId;
    private String storage;
    private String name;

    public ActivityFile(String uploadId, String storage, String name) {
        this.uploadId = uploadId;
        this.storage = storage;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }
}

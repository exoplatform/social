package org.exoplatform.social.core.jpa.storage;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ActivityFile;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.storage.ActivityFileStoragePlugin;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;

/**
 * This plugin will store activity attachment files using FileService
 */
public class RDBMSActivityFileStoragePlugin extends ActivityFileStoragePlugin {

  private static final String FILE_API_NAMESPACE = "social";

  private FileService         fileService;

  private UploadService       uploadService;

  public RDBMSActivityFileStoragePlugin(FileService fileService, UploadService uploadService, InitParams initParams) {
    super(initParams);
    this.fileService = fileService;
    this.uploadService = uploadService;
  }

  @Override
  public void storeAttachments(ExoSocialActivity activity, Identity streamOwner, ActivityFile... attachments) throws Exception {
    if (attachments == null || attachments.length == 0) {
      return;
    }
    for (ActivityFile activityFile : attachments) {
      UploadResource uploadedResource = uploadService.getUploadResource(activityFile.getUploadId());
      if (uploadedResource == null) {
        throw new IllegalStateException("Cannot attach uploaded file " + activityFile.getUploadId() + ", it may not exist");
      }

      FileItem fileItem = null;
      String fileDiskLocation = uploadedResource.getStoreLocation();
      try (InputStream inputStream = new FileInputStream(fileDiskLocation)) {
        fileItem = fileService.writeFile(new FileItem(null,
                                                      uploadedResource.getFileName(),
                                                      uploadedResource.getMimeType(),
                                                      FILE_API_NAMESPACE,
                                                      Double.doubleToLongBits(uploadedResource.getUploadedSize()),
                                                      new Date(),
                                                      activity.getPosterId(),
                                                      false,
                                                      inputStream));
      }

      if (fileItem != null) {
        if (activity.getTemplateParams() == null) {
          activity.setTemplateParams(new HashMap<>());
        }
        concatenateParam(activity.getTemplateParams(), ACTIVITY_FILE_STORAGE_PARAM_NAME, getDatasourceName());
        concatenateParam(activity.getTemplateParams(),
                         ACTIVITY_FILE_ID_PARAM_NAME,
                         String.valueOf(fileItem.getFileInfo().getId()));
      }
      uploadService.removeUploadResource(activityFile.getUploadId());
    }
  }

}

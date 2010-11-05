/*

 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.image;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.model.AvatarAttachment;

/**
 * @author tuan_nguyenxuan Oct 29, 2010
 */
public class ImageUtils {
  public static final String KEY_SEPARATOR           = "_";
  public static final String KEY_DIMENSION_SEPARATOR = "x";

  public static final String GIF_EXTENDSION          = "gif";
  private static final Log LOG = ExoLogger.getLogger(ImageUtils.class);

  /**
   * @param string Make string params not null
   */
  private static void makeNotNull(String... str)
  {
    for (String string : str) {
      if (string == null)
        string = "";
    }
  }

  /**
   * @param width
   * @param height
   * @return name of resized image name like from "avatar.jpg" to
   *         "RESIZED_avatar_100x100.jpg"
   */
  public static String buildFileName(String oldFileName, String subfix, String postfix) {
    makeNotNull(oldFileName,subfix,postfix);
    if(oldFileName.equals(""))
      return subfix + postfix;
    int dotIndex = oldFileName.lastIndexOf('.');
    if(dotIndex < 0)
      return subfix + oldFileName + postfix;
    String nameOnly = oldFileName.substring(0, dotIndex);
    String extendtionAndDot = oldFileName.substring(dotIndex);
    return subfix + nameOnly + postfix + extendtionAndDot;
  }

  /**
   * @param width
   * @param height
   * @return postfix for image name like avatar_100x100
   */

  public static String buildImagePostfix(int width, int height) {
    return KEY_SEPARATOR + (width < 0 ? 0 : width) + KEY_DIMENSION_SEPARATOR
        + (height < 0 ? 0 : height);
  }

  /**
   * @param imageStream
   * @param width
   * @param height
   * @param avatarId
   * @param avatarFileName
   * @param avatarMimeType
   * @param avatarWorkspace
   * @return new AvatarAtachment that contain parameter values and resized
   *         avatar
   */
  public static AvatarAttachment createResizedAvatarAttachment(InputStream imageStream,
                                                               int width,
                                                               int height,
                                                               String avatarId,
                                                               String avatarFileName,
                                                               String avatarMimeType,
                                                               String avatarWorkspace) {
    try {
      MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();

      BufferedImage image = null;
      int minSize = 0;
      String extension = mimeTypeResolver.getExtension(avatarMimeType);
      // TODO: Resize gif image. Now we skip gif because we can't resize it now
      if (extension.equalsIgnoreCase(GIF_EXTENDSION))
        return null;
      image = ImageIO.read(imageStream);
      if (height <= minSize & width <= minSize) {
        LOG.warn("Fail to resize image to avatar attachment with dimention <= 0x0");
        return null;
      }
      if (height <= minSize)
        height = image.getHeight() * width / image.getWidth();
      else if (width <= minSize)
        width = image.getWidth() * height / image.getHeight();

      // Create temp file to store resized image to put to avatar attachment
      File tmp = File.createTempFile("RESIZED", null);
      ImageIO.write(org.apache.shindig.gadgets.rewrite.image.ImageUtils.getScaledInstance(image,
                                                 width,
                                                 height,
                                                 RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
                                                 false,
                                                 BufferedImage.TYPE_INT_RGB),
                    extension,
                    tmp);

      // Create new avatar attachment
      AvatarAttachment newAvatarAttachment = new AvatarAttachment(avatarId,
                                                                  avatarFileName,
                                                                  avatarMimeType,
                                                                  new FileInputStream(tmp),
                                                                  avatarWorkspace,
                                                                  System.currentTimeMillis());

      // Delete temp file
      tmp.delete();
      return newAvatarAttachment;
    } catch (Exception e) {
      LOG.error("Fail to resize image to avatar attachment: " + e);
      return null;
    }
  }
}
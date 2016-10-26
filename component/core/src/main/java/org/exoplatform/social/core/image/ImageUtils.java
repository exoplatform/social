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

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.model.AvatarAttachment;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author tuan_nguyenxuan Oct 29, 2010
 */
public class ImageUtils {
  public static final String KEY_SEPARATOR           = "_";
  public static final String KEY_DIMENSION_SEPARATOR = "x";

  public static final String GIF_EXTENDSION          = "gif";
  private static final Log LOG = ExoLogger.getLogger(ImageUtils.class);
  public static BufferedImage image = null;

  /**
   * @param str Make string params not null
   */
  private static void makeNotNull(String... str)
  {
    for (String string : str) {
      if (string == null)
        string = "";
    }
  }

  /**
   * @param oldFileName
   * @param subfix
   * @param postfix
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
   * @param maxWidth
   * @param maxHeight
   * @param avatarId
   * @param avatarFileName
   * @param avatarMimeType
   * @param avatarWorkspace
   * @return new AvatarAtachment that contain parameter values and resized
   *         avatar
   */
  public static AvatarAttachment createResizedAvatarAttachment(InputStream imageStream,
                                                               int maxWidth,
                                                               int maxHeight,
                                                               String avatarId,
                                                               String avatarFileName,
                                                               String avatarMimeType,
                                                               String avatarWorkspace) {
    if (maxHeight <= 0 || maxWidth <= 0) {
      LOG.warn("Fail to resize image to avatar attachment with dimension <= 0");
      return null;
    }

    try {
      MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
      String extension = mimeTypeResolver.getExtension(avatarMimeType);
      // TODO: Resize gif image. Now we skip gif because we can't resize it now
      if (extension.equalsIgnoreCase(GIF_EXTENDSION)) {
        return null;
      }

      image = ImageIO.read(imageStream);

      int targetHeight = image.getHeight();
      int targetWidth = image.getWidth();

      double maxDimensionsRatio =  (double) maxHeight / (double) maxWidth;
      double imageRatio =  (double) image.getHeight() / (double) image.getWidth();

      if(imageRatio > maxDimensionsRatio && image.getHeight() > maxHeight) {
        targetHeight = maxHeight;
        targetWidth = (maxHeight * image.getWidth()) / image.getHeight();
      } else if(imageRatio < maxDimensionsRatio && image.getWidth() > maxWidth) {
        targetHeight = (maxWidth * image.getHeight()) / image.getWidth();
        targetWidth = maxWidth;
      }

      // Create temp file to store resized image to put to avatar attachment
      File tmp = File.createTempFile("RESIZED", null);
      image = resizeImage(image, targetWidth, targetHeight);

      ImageIO.write(image, extension, tmp);
      
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

  private static BufferedImage resizeImage(BufferedImage image, int width, int height) {
    final BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    final Graphics2D graphics2D = bufferedImage.createGraphics();
    graphics2D.setComposite(AlphaComposite.Src);
    //below three lines are for RenderingHints for better image quality
    graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    graphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    graphics2D.drawImage(image, 0, 0, width, height, null);
    graphics2D.dispose();
    return bufferedImage;
  }
}
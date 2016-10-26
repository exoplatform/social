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


import junit.framework.TestCase;
import org.exoplatform.social.core.model.AvatarAttachment;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUtilsTest extends TestCase {

  public void testBuildFileName() {
    String oldFileName = "avatar";
    String extendsion = ".jpg";
    String subfix = "RESIZED_";
    String postfix = "_100x100";
    String newFileName = ImageUtils.buildFileName(oldFileName + extendsion, subfix, postfix);
    assertEquals("Should be " + subfix + oldFileName + postfix + extendsion, subfix + oldFileName
        + postfix + extendsion, newFileName);
  }

  public void testBuildImagePostfix() {
    String postfix = ImageUtils.buildImagePostfix(100, -10);
    assertEquals("_100x0", postfix);
  }

  public void testCreateResizedAvatarAttachment() throws IOException {
    // image 144x40
    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    int width = 100;
    int height = 100;
    String avatarId = "null";
    String avatarMimeType = "image/jpeg";
    String avatarFileName = "eXo-Social.png";
    String avatarWorkspace = "null";
    AvatarAttachment avatar = ImageUtils.createResizedAvatarAttachment(inputStream, width, height, avatarId, avatarFileName, avatarMimeType, avatarWorkspace);
    assertNotNull(avatar);
    assertNotNull(avatar.getImageBytes());
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(avatar.getImageBytes()));
    assertEquals(100, image.getWidth());
    assertEquals(27, image.getHeight());
  }

}

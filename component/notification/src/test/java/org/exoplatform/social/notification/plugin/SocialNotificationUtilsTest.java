package org.exoplatform.social.notification.plugin;

import junit.framework.TestCase;
import org.exoplatform.commons.notification.NotificationUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SocialNotificationUtilsTest extends TestCase {

    public void testProcessImageTitle() {
        String chromeNotification = "<p>test text for notification with Image</p><p></p><p><img alt=\"\" class=\"pull-left\" data-plugin-name=\"selectImage\" referrerpolicy=\"no-referrer\" src=\"/portal/rest/images/repository/collaboration/054bd7057f0001012279c31b78a3bfdb\" /> </p>\n" +
                "<p> here is another image</p>\n" +
                "<p><img alt=\"\" class=\"pull-left\" data-plugin-name=\"selectImage\" referrerpolicy=\"no-referrer\" src=\"/portal/rest/images/repository/collaboration/054bd72f7f000101419e3a68603fdd72\" /></p>";
        String edgeNotification = "<p>A first image from Edge <img class=\"pull-left\" alt=\"\" src=\"/portal/rest/images/repository/collaboration/04ba00e37f0001015e7c15dc245675e8\" referrerpolicy=\"no-referrer\" data-plugin-name=\"selectImage\" /></p>\n" +
                "<p> here is another image</p>\n" +
                "<img class=\"pull-left\" alt=\"\" src=\"/portal/rest/images/repository/collaboration/04ba00e37f0001015e7c15dc245675e8\" referrerpolicy=\"no-referrer\" data-plugin-name=\"selectImage\" />";
        String placeHolder = "Image Inline";
        String chromeNotificationProcessed = "<p>test text for notification with Image</p><p></p><p><i> [Image Inline] </i> </p>\n" +
                "<p> here is another image</p>\n" +
                "<p><i> [Image Inline] </i></p>";
        String edgeNotificationProcessed = "<p>A first image from Edge <i> [Image Inline] </i></p>\n" +
                "<p> here is another image</p>\n" +
                "<i> [Image Inline] </i>";
        assertEquals(SocialNotificationUtils.processImageTitle(chromeNotification, placeHolder), chromeNotificationProcessed);
        assertEquals(SocialNotificationUtils.processImageTitle(edgeNotification, placeHolder), edgeNotificationProcessed);
    }
}
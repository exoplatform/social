package org.exoplatform.social.notification;

import junit.framework.TestCase;

public class UtilsTestCase extends TestCase {
  
  public void testProcessLinkInActivityTitle() throws Exception {
    String title = "<a href=\"www.yahoo.com\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\">Hotmail Site</a>";
    title = Utils.processLinkTitle(title);
    assertEquals("<a href=\"www.yahoo.com\" style=\"text-decoration: none;\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\" style=\"text-decoration: none;\">Hotmail Site</a>", title);
  }
}

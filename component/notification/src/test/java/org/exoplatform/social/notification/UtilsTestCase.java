package org.exoplatform.social.notification;

import org.exoplatform.commons.utils.CommonsUtils;

import junit.framework.TestCase;

public class UtilsTestCase extends TestCase {
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    System.setProperty(CommonsUtils.CONFIGURED_DOMAIN_URL_KEY, "http://exoplatform.com");
  }

  public void testProcessLinkInActivityTitle() throws Exception {
    String title = "<a href=\"www.yahoo.com\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\">Hotmail Site</a>";
    title = Utils.processLinkTitle(title);
    assertEquals("<a href=\"www.yahoo.com\" style=\"color: #2f5e92; text-decoration: none;\">Yahoo Site</a> is better than <a href=\"www.hotmail.com\" style=\"color: #2f5e92; text-decoration: none;\">Hotmail Site</a>", title);
    
    title = "Shared a document <a href=\"portal/rest/jcr/repository/collaboration/Users/D___/Do___/Do____/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\">New design. eXo in Smart Watch.jpg</a>";
    assertEquals("Shared a document <a href=\"http://exoplatform.com/portal/rest/jcr/repository/collaboration/Users/D___/Do___/Do____/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\" style=\"color: #2f5e92; text-decoration: none;\">New design. eXo in Smart Watch.jpg</a>", Utils.processLinkTitle(title));
    title = "Shared a document <a href=\"/portal/rest/jcr/repository/collaboration/Users/D___/Do___/Do____/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\">New design. eXo in Smart Watch.jpg</a>";
    assertEquals("Shared a document <a href=\"http://exoplatform.com/portal/rest/jcr/repository/collaboration/Users/D___/Do___/Do____/Do_Thanh_Tung/Public/New+design.+eXo+in+Smart+Watch.jpg\" style=\"color: #2f5e92; text-decoration: none;\">New design. eXo in Smart Watch.jpg</a>", Utils.processLinkTitle(title));
  }
}

/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.processor;

import java.util.LinkedHashMap;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link OSHtmlSanitizerProcessor}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  Jun 29, 2011
 */
public class OSHtmlSanitizerProcessorTest extends AbstractCoreTest {

  private OSHtmlSanitizerProcessor processor;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    processor = (OSHtmlSanitizerProcessor) PortalContainer.getInstance().
                                           getComponentInstanceOfType(OSHtmlSanitizerProcessor.class);

  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }


  public void testProcessActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String sample = "this is a <strong> tag to keep</strong>";
    activity.setTitle(sample);
    activity.setBody(sample);
    processor.processActivity(activity);

    assertEquals(sample, activity.getTitle());
    assertEquals(sample, activity.getBody());

    // tags with attributes
    sample = "text <a href='#'>bar</a> zed";

    activity.setTitle(sample);
    processor.processActivity(activity);

    assertEquals("text <a href=\"#\">bar</a> zed", activity.getTitle());

    // only with open tag
    sample = "<strong> only open!!!";
    activity.setTitle(sample);
    processor.processActivity(activity);
    assertEquals("<strong> only open!!!</strong>", activity.getTitle());

    // self closing tags
    sample = "<script href='#' />bar</a>";
    activity.setTitle(sample);
    processor.processActivity(activity);
    assertEquals("&lt;script href=&quot;#&quot; /&gt;bar&lt;/a&gt;", activity.getTitle());

    // forbidden tag
    sample = "<script>foo</script>";
    activity.setTitle(sample);
    processor.processActivity(activity);
    assertEquals("&lt;script&gt;foo&lt;/script&gt;", activity.getTitle());

    // embedded
    sample = "<span><strong>foo</strong>bar<script>zed</script></span>";
    activity.setTitle(sample);
    processor.processActivity(activity);
    assertEquals("<span><strong>foo</strong>bar&lt;script&gt;zed&lt;/script&gt;</span>", activity.getTitle());
  }
  
  public void testProcessActivityWithTemplateParam() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String sample = "this is a <strong> tag to keep</strong>";
    activity.setTitle(sample);
    activity.setBody(sample);
    String keysToProcess = "a|b|c";
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("a", "a\nb");
    templateParams.put("b", "exoplatform.com");
    templateParams.put("d", "exoplatform.com");
    templateParams.put(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS, keysToProcess);
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);
    
    templateParams = activity.getTemplateParams();
    assertEquals("a<br />b", templateParams.get("a"));
    assertEquals("<a href=\"http://exoplatform.com\" target=\"_blank\">exoplatform.com</a>", templateParams.get("b"));
    assertEquals("exoplatform.com", templateParams.get("d"));
  }

  public void testProcessOldActivityWithTemplateParam(){
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String sample = "this is a <strong> tag to keep</strong>";
    activity.setTitle(sample);
    activity.setBody(sample);
    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("a", "a\nb");
    templateParams.put("b", "exoplatform.com");
    templateParams.put("d", "exoplatform.org");
    activity.setTemplateParams(templateParams);
    processor.processActivity(activity);

    templateParams = activity.getTemplateParams();
    assertEquals("a<br />b", templateParams.get("a"));
    assertEquals("<a href=\"http://exoplatform.com\" target=\"_blank\">exoplatform.com</a>", templateParams.get("b"));
    assertEquals("<a href=\"http://exoplatform.org\" target=\"_blank\">exoplatform.org</a>", templateParams.get("d"));
  }
  
}

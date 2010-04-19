package org.exoplatform.social.core.activitystream;

import java.util.ArrayList;
import java.util.Arrays;

import org.exoplatform.commons.testing.AssertUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

import junit.framework.TestCase;

public class TestOSHtmlSanitizerProcess extends TestCase {

  

  
  public void testInitParams() {
    
    OSHtmlSanitizerProcessor processor = new OSHtmlSanitizerProcessor(null);
    String [] actual = processor.getAllowedTags();
    AssertUtils.assertContains(actual, OSHtmlSanitizerProcessor.OS_ALLOWED_TAGS);
    
    InitParams params = new InitParams();
    processor = new OSHtmlSanitizerProcessor(params);
    actual = processor.getAllowedTags();
    AssertUtils.assertContains(actual, OSHtmlSanitizerProcessor.OS_ALLOWED_TAGS);
    
    params = new InitParams();
    ValuesParam allowedTagsParams = new ValuesParam();
    allowedTagsParams.setName("allowedTags");
    allowedTagsParams.setValues(new ArrayList<String>(Arrays.asList("foo","bar", "zed")));
    params.addParameter(allowedTagsParams);
    processor = new OSHtmlSanitizerProcessor(params);
    actual = processor.getAllowedTags();
    AssertUtils.assertContains(actual, "foo","bar", "zed");
    
 
   
  }
  
  
  
  public void testEscapeHtml() {
    
    String [] allowed = new String []{"b", "i", "a", "span", "em", "strong", "p", "ol", "ul", "li", "br"};
    
    OSHtmlSanitizerProcessor processor = new OSHtmlSanitizerProcessor(null);
    processor.setAllowedTags(allowed);
    String sample = "this is a <b> tag to keep</b>";
    assertEquals(sample, processor.escapeHtml(sample));
    
    // tags with attributes
    sample = "text <a href='#' >bar</a> zed" ;
    assertEquals(sample, processor.escapeHtml(sample));
    
    // self closing tags
    sample = "<script href='#' />bar</a>";
    assertEquals("&lt;script href='#' /&gt;bar</a>", processor.escapeHtml(sample));
    
    // forbidden tag
    sample = "<script>foo</script>";
    assertEquals("&lt;script&gt;foo&lt;/script&gt;", processor.escapeHtml(sample));
    
    // embedded
    sample = "<span><strong>foo</strong>bar<script>zed</script></span>";
    assertEquals("<span><strong>foo</strong>bar&lt;script&gt;zed&lt;/script&gt;</span>", processor.escapeHtml(sample));
    

  }
  
}

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
package org.exoplatform.social.common.xmlprocessor.filters;

import org.exoplatform.social.common.xmlprocessor.Filter;

import junit.framework.TestCase;

/**
 * Unit Test for {@link URLConverterFilterPlugin}.
 */
public class URLConverterFilterPluginTest extends TestCase {
  
  /**
   * Unit test for {@link URLConverterFilterPlugin#doFilter(Object)} with default urlMaxLength
   */
  public void testURLConverterFilterPlugin() {
    Filter urlConverterFilter = new URLConverterFilterPlugin(0);

    assertEquals("<a href=\"http://google.com\" target=\"_blank\"" +
    		        ">http://google.com</a>", urlConverterFilter.doFilter("http://google.com"));
    
    assertEquals("<a href=\"http://google.com/\" target=\"_blank\"" +
                ">http://google.com/</a> " +
                "<a href=\"http://google.com/##testdup\" target=\"_blank\"" +
                ">http://google.com/##testdup</a>", urlConverterFilter.doFilter("http://google.com/ http://google.com/##testdup"));
    
    assertEquals("<a href=\"http://google.com/\" target=\"_blank\"" +
        ">http://google.com/</a> Test URL URL " +
        "<a href=\"http://google.com/##testdup\" target=\"_blank\"" +
        ">http://google.com/##testdup</a>", 
        urlConverterFilter.doFilter("http://google.com/ Test URL URL http://google.com/##testdup"));
    
    assertEquals("hello1 <a href=\"http://google.com/\" target=\"_blank\"" +
        ">http://google.com/</a> Test URL URL " +
        "<a href=\"http://google.com/##testdup\" target=\"_blank\"" +
        ">http://google.com/##testdup</a> hello2", 
        urlConverterFilter.doFilter("hello1 http://google.com/ Test URL URL http://google.com/##testdup hello2"));
    
    assertEquals("<a href=\"http://abc.com:80/abc.jsp?a=1&b=2\" target=\"_blank\">http://abc.com:80/abc.jsp?a=1&amp;b=2</a>",
        urlConverterFilter.doFilter("http://abc.com:80/abc.jsp?a=1&b=2"));
    
    assertEquals("Filter should not proccess the text inside <a>", "<a href=\"abc.com\">http://abc.com</a>", "<a href=\"abc.com\">http://abc.com</a>");
    assertEquals("Filter should not proccess the text inside <a>", "<a><div>http://abc.com</div> <div>http://def.com</div></a>","<a><div>http://abc.com</div> <div>http://def.com</div></a>");
    assertEquals("Filter should not proccess the text inside <a>", "<a><img src=\"x\" alt=\"x\" /> http://xyz.com </a>","<a><img src=\"x\" alt=\"x\" /> http://xyz.com </a>");
  }
  
  /**
   * Unit test for {@link URLConverterFilterPlugin#doFilter(Object)} with urlMaxLength = 15
   */
  public void testURLConverterFilterPluginWithMaxLength() {
    Filter urlConverterFilter = new URLConverterFilterPlugin(15);

    assertEquals("<a href=\"http://google.com\" target=\"_blank\"" +
                ">http://googl...</a>", urlConverterFilter.doFilter("http://google.com"));
    
    assertEquals("<a href=\"http://google.com/\" target=\"_blank\"" +
                ">http://googl...</a> " +
                "<a href=\"http://google.com/##testdup\" target=\"_blank\"" +
                ">http://googl...</a>", urlConverterFilter.doFilter("http://google.com/ http://google.com/##testdup"));
    
    assertEquals("<a href=\"http://google.com/\" target=\"_blank\"" +
        ">http://googl...</a> Test URL URL " +
        "<a href=\"http://google.com/##testdup\" target=\"_blank\"" +
        ">http://googl...</a>", 
        urlConverterFilter.doFilter("http://google.com/ Test URL URL http://google.com/##testdup"));
    
    assertEquals("hello1 <a href=\"http://google.com/\" target=\"_blank\"" +
        ">http://googl...</a> Test URL URL " +
        "<a href=\"http://google.com/##testdup\" target=\"_blank\"" +
        ">http://googl...</a> hello2", 
        urlConverterFilter.doFilter("hello1 http://google.com/ Test URL URL http://google.com/##testdup hello2"));
    
    assertEquals("<a href=\"http://abc.com:80/abc.jsp?a=1&b=2\" target=\"_blank\">http://abc.c...</a>",
        urlConverterFilter.doFilter("http://abc.com:80/abc.jsp?a=1&b=2"));
  }

}

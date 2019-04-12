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

    assertEquals("<a href=\"http://192.168.1.1/\" target=\"_blank\"" +
            ">http://192.168.1.1/</a>", urlConverterFilter.doFilter("http://192.168.1.1/"));

    assertEquals("192.168.1.1", urlConverterFilter.doFilter("192.168.1.1"));

    assertEquals("1.1.1.1", urlConverterFilter.doFilter("1.1.1.1"));

    assertEquals("1.1.1.1 " +
            "<a href=\"http://1.1.1.1\" target=\"_blank\"" +
            ">http://1.1.1.1</a>", urlConverterFilter.doFilter("1.1.1.1 http://1.1.1.1"));

    assertEquals("<a href=\"http://google.com\" target=\"_blank\"" +
                ">http://google.com</a>", urlConverterFilter.doFilter("http://google.com"));

    assertEquals("<a href=\"http://www.exoplatform.com\" target=\"_blank\"" +
                ">www.exoplatform.com</a>", urlConverterFilter.doFilter("www.exoplatform.com"));

    assertEquals("<a href=\"http://exoplatform.com\" target=\"_blank\"" +
            ">exoplatform.com</a>", urlConverterFilter.doFilter("exoplatform.com"));
    
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
    
    assertEquals( "<a href=\"http://cwks:9090\" target=\"_blank\">http://cwks:9090</a> " +
        "<a href=\"http://localhost:8080/\" target=\"_blank\">http://localhost:8080/</a> " +
        "<a href=\"http://phuonglm:gtn@localhost:8080\" target=\"_blank\">http://phuonglm:gtn@localhost:8080</a> " +
        "<a href=\"http://cwks\" target=\"_blank\">http://cwks</a> " +
        "<a href=\"HTTP://abc.com\" target=\"_blank\">HTTP://abc.com</a> " +
        "<a href=\"HTTP://mary:gtn@abc.com:8080\" target=\"_blank\">HTTP://mary:gtn@abc.com:8080</a> " +
        "<a href=\"HTTP://mary:gtn@abc.com:8080\" target=\"_blank\">HTTP://mary:gtn@abc.com:8080</a> " +
        "<a href=\"hTTP://mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks\" target=\"_blank\">" +
          "hTTP://mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks" +
        "</a> " +
        "<a href=\"HTTP://mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks#click1\" target=\"_blank\">" +
           "HTTP://mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks#click1" +
        "</a> " +
        "<a href=\"http://abc.com\" target=\"_blank\">abc.com</a> " +
        "<a href=\"http://mary:gtn@abc.com:8080\" target=\"_blank\">mary:gtn@abc.com:8080</a> " +
        "<a href=\"http://mary:gtn@abc.com:8080\" target=\"_blank\">mary:gtn@abc.com:8080</a> " +
        "<a href=\"http://mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks\" target=\"_blank\">" +
          "mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks" +
        "</a> " +
        "<a href=\"http://mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks#click1\" target=\"_blank\">" +
          "mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks#click1" +
        "</a> " +
        "mary*gtn@abc.com:8080/abc.php?blabla=/google@cwks#click1",
        urlConverterFilter.doFilter("http://cwks:9090 http://localhost:8080/ http://phuonglm:gtn@localhost:8080 " +
            "http://cwks HTTP://abc.com HTTP://mary:gtn@abc.com:8080 HTTP://mary:gtn@abc.com:8080 " +
            "hTTP://mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks " +
            "HTTP://mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks#click1 " +
            "abc.com mary:gtn@abc.com:8080 mary:gtn@abc.com:8080 mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks " +
            "mary:gtn@abc.com:8080/abc.php?blabla=/google@cwks#click1 " +
            "mary*gtn@abc.com:8080/abc.php?blabla=/google@cwks#click1")); 
    
    assertEquals("Do not convert invalid domain abc.def",
        "abc.def", urlConverterFilter.doFilter("abc.def"));
    assertEquals("Filter should not proccess the text inside <a>",
        "<a href=\"abc.com\">http://abc.com</a>", urlConverterFilter.doFilter("<a href=\"abc.com\">http://abc.com</a>"));
    assertEquals("Filter should not proccess the text inside <a>",
        "<a><div>http://abc.com</div> <div>http://def.com</div></a>",
        urlConverterFilter.doFilter("<a><div>http://abc.com</div> <div>http://def.com</div></a>"));
    assertEquals("Filter should not proccess the text inside <a>",
        "<a><img src=\"x\" alt=\"x\" /> http://xyz.com </a>",
        urlConverterFilter.doFilter("<a><img src=\"x\" alt=\"x\" /> http://xyz.com </a>"));
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

  public void testURLConverterFilterPluginWithMultiLine() {
    Filter urlConverterFilter = new URLConverterFilterPlugin(0);
    assertEquals("demo <a href=\"http://google.com\" target=\"_blank\"" +
            ">http://google.com</a> test <br />\t\n<a href=\"http://google.com\" target=\"_blank\">http://google.com</a>", urlConverterFilter.doFilter("demo http://google.com test <br />\t\nhttp://google.com"));

    assertEquals("demo <a href=\"http://google.com\" target=\"_blank\"" +
            ">http://google.com</a> test <br />\t\n <a href=\"http://google.com\" target=\"_blank\">http://google.com</a>", urlConverterFilter.doFilter("demo http://google.com test <br />\t\n http://google.com"));

    assertEquals("demo <a href=\"http://google.com\" target=\"_blank\"" +
            ">http://google.com</a> test\t\n<br />\t\n<a href=\"http://google.com\" target=\"_blank\">http://google.com</a>\n\t", urlConverterFilter.doFilter("demo http://google.com test\t\n<br />\t\nhttp://google.com\n\t"));
  }

  public void testShouldReturnNullWhenConvertingNullInput() {
    // Given
    URLConverterFilterPlugin urlConverterFilterPlugin = new URLConverterFilterPlugin(0);

    // When
    Object filteredInput = urlConverterFilterPlugin.doFilter(null);

    // Then
    assertNull(filteredInput);
  }
}

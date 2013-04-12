/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.common;

import org.exoplatform.social.common.jcr.filter.FilterLiteralTest;
import org.exoplatform.social.common.xmlprocessor.DOMParserTest;
import org.exoplatform.social.common.xmlprocessor.TokenizerTest;
import org.exoplatform.social.common.xmlprocessor.filters.DOMContentEscapeFilterPluginTest;
import org.exoplatform.social.common.xmlprocessor.filters.DOMLineBreakerFilterPluginTest;
import org.exoplatform.social.common.xmlprocessor.filters.DOMXMLTagFilterPluginTest;
import org.exoplatform.social.common.xmlprocessor.filters.LineBreakerFilterPluginTest;
import org.exoplatform.social.common.xmlprocessor.filters.URLConverterFilterPluginTest;
import org.exoplatform.social.common.xmlprocessor.filters.XMLBalancerFilterPluginTest;
import org.exoplatform.social.common.xmlprocessor.filters.XMLTagFilterPluginTest;
import org.exoplatform.social.common.xmlprocessor.model.AttributesTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
  DOMParserTest.class,
  TokenizerTest.class,
  AttributesTest.class,
  DOMContentEscapeFilterPluginTest.class,
  DOMLineBreakerFilterPluginTest.class,
  DOMXMLTagFilterPluginTest.class,
  LineBreakerFilterPluginTest.class,
  URLConverterFilterPluginTest.class,
  XMLBalancerFilterPluginTest.class,
  XMLTagFilterPluginTest.class,
  FilterLiteralTest.class
  })
public class NoContainerTestSuite {
  
  @BeforeClass
  public static void setUp() throws Exception {
    
  }

  @AfterClass
  public static void tearDown() {
  }


}

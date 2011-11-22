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
package org.exoplatform.social.common.xmlprocessor.model;

import java.util.Set;

import org.exoplatform.social.common.AbstractCommonTest;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy.AllowedTag;

/**
 * Unit Test for {@link XMLTagFilterPolicy}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 18, 2011
 */
public class XmlTagFilterPolicyTest extends AbstractCommonTest {

  private XMLTagFilterPolicy configXmlTagFilterPolicy;

  private XMLTagFilterPolicy plainXmlTagFilterPolicy;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    configXmlTagFilterPolicy = (XMLTagFilterPolicy) getContainer().getComponentInstanceOfType(XMLTagFilterPolicy.class);
    plainXmlTagFilterPolicy = new XMLTagFilterPolicy();
  }

  @Override
  public void tearDown() throws Exception {
    plainXmlTagFilterPolicy = null;
    super.tearDown();
  }

  public void testGetAllowedTagSet() {
    Set<AllowedTag> allowedTagSet = configXmlTagFilterPolicy.getAllowedTagSet();
    assertNotNull("allowedTagSet must not be null", allowedTagSet);
  }

}

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
package org.exoplatform.social.core.processor;

import java.util.HashMap;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.test.AbstractCoreTest;


public class TemplateParamsProcessorTest extends AbstractCoreTest {

  public void setUp() throws Exception {
    super.setUp();
  }
  
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testProcessSimpleTemplate(){
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String nameKey = "ZUN-NAME";
    String nameValue = "Zuanoc";
    
    String cityKey = "CITY";
    String cityValue = "Hanoi";

    String templateString = "This is ${" + nameKey + "}. City : ${" + cityKey + "}";
    String resultString = "This is "+nameValue+". City : "+ cityValue;
    activity.setTitle(templateString);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(nameKey, nameValue);
    params.put(cityKey, cityValue);

    activity.setTemplateParams(params);

    TemplateParamsProcessor processor = (TemplateParamsProcessor) PortalContainer.getComponent(TemplateParamsProcessor.class);
    processor.processActivity(activity);

    assertEquals(resultString, activity.getTitle());
  }

  public void testProcessComplicatedTemplate() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    String nameKey = "ZUN-NAME";
    String nameValue = "Zuanoc";

    String cityKey = "CITY";
    String cityValue = "Hanoi";

    String templateString = "This is ${" + nameKey + "}. City : ${" + cityKey + "}";
    templateString +=templateString;
    String resultString = "This is "+nameValue+". City : "+ cityValue;
    resultString +=resultString;

    activity.setTitle(templateString);
    HashMap<String, String> params = new HashMap<String, String>();
    params.put(nameKey, nameValue);
    params.put(cityKey, cityValue);

    activity.setTemplateParams(params);

    TemplateParamsProcessor processor = (TemplateParamsProcessor) PortalContainer.getComponent(TemplateParamsProcessor.class);
    processor.processActivity(activity);

    assertEquals(resultString, activity.getTitle());
  }  
}

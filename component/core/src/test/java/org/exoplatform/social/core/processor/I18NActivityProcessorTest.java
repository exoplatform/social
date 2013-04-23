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
package org.exoplatform.social.core.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.resources.LocaleConfigService;
import org.exoplatform.services.resources.Query;
import org.exoplatform.services.resources.ResourceBundleData;
import org.exoplatform.services.resources.impl.BaseResourceBundlePlugin;
import org.exoplatform.services.resources.impl.BaseResourceBundleService;
import org.exoplatform.services.resources.impl.LocaleConfigServiceImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;

/**
 * Unit Test for {@link org.exoplatform.social.core.processor.I18NActivityProcessor}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Feb 6, 2012
 */
public class I18NActivityProcessorTest extends TestCase {

  private I18NActivityProcessor i18NActivityProcessor;

  private ActivityResourceBundlePlugin activityResourceBundlePlugin;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    PropertyManager.setProperty(PropertyManager.DEVELOPING, "true");
    i18NActivityProcessor = new I18NActivityProcessor();
    FakeResourceBundleService fakeResourceBundleService = getResourceBundleService();
    i18NActivityProcessor.setResourceBundleService(fakeResourceBundleService);
  }


  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testNoRegisteredActivityResourceBundlePlugin() throws Exception {
    Map<String, String> activityKeyTypeMapping = new HashMap<String, String>();
    activityKeyTypeMapping.put("hello", "FakeResourceBundle.hello");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);

    final String title = "hello world from title.";
    ExoSocialActivity activity = createActivity("hello", title);
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity1 = i18NActivityProcessor.process(activity, enLocale);
    assertEquals(title, newActivity1.getTitle());
  }

  public void testAddActivityResourceBundlePlugin() throws Exception {
    Map<String, String> activityKeyTypeMapping = new HashMap<String, String>();
    activityKeyTypeMapping.put("hello", "FakeResourceBundle.hello");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);

    final String title = "hello world from title.";
    ExoSocialActivity activity = createActivity("hello", title);
    Locale enLocale = new Locale("en");

    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);
    ExoSocialActivity newActivity2 = i18NActivityProcessor.process(activity, enLocale);
    assertEquals("Hello from resource bundle (EN).", newActivity2.getTitle());

    Locale frLocale = new Locale("fr");
    ExoSocialActivity newActivity3 = i18NActivityProcessor.process(activity, frLocale);
    assertEquals("Hello from resource bundle (FR).", newActivity3.getTitle());
  }

  public void testRemoveActivityResourceBundlePlugin() throws Exception {
    Map<String, String> activityKeyTypeMapping = new HashMap<String, String>();
    activityKeyTypeMapping.put("hello", "FakeResourceBundle.hello");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);

    final String title = "hello world from title.";
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);
    ExoSocialActivity activity = createActivity("hello", title);
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity1 = i18NActivityProcessor.process(activity, enLocale);
    assertEquals("Hello from resource bundle (EN).", newActivity1.getTitle());

    i18NActivityProcessor.removeActivityResourceBundlePlugin(activityResourceBundlePlugin);

    ExoSocialActivity activity2 = createActivity("hello", title);
    ExoSocialActivity newActivity2 = i18NActivityProcessor.process(activity2, enLocale);
    assertEquals(title, newActivity2.getTitle());

  }

  public void testCompoundMessages() throws Exception {
    Map<String, String> activityKeyTypeMapping = new LinkedHashMap<String, String>();
    activityKeyTypeMapping.put("spaceships_detected", "FakeResourceBundle.time_day_number_detected_spaceships_on_planet");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);

    Map<String, String> templateParams = new LinkedHashMap<String, String>();
    templateParams.put("planet", "Mars");
    templateParams.put("number", "10");
    templateParams.put("time", "02:00 PM");
    templateParams.put("date", "Feb 6, 2012");

    final String title = "At 02:00 PM on Feb 6, 2012, we detected 10 spaceships on the planet Mars.";
    ExoSocialActivity activity = createActivity("spaceships_detected", title);
    activity.setTemplateParams(templateParams);
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity = i18NActivityProcessor.process(activity, enLocale);

    assertEquals("At 02:00 PM on Feb 6, 2012, we detected 10 spaceships on the planet Mars (EN).", newActivity.getTitle());

  }
  
  public void testCompoundMessagesWithMultiKeys() throws Exception {
    Map<String, String> activityKeyTypeMapping = new LinkedHashMap<String, String>();
    activityKeyTypeMapping.put("add_topic", "add_topic");
    activityKeyTypeMapping.put("update_topic_title", "FakeResourceBundle.update_topic_title");
    activityKeyTypeMapping.put("update_topic_content", "FakeResourceBundle.update_topic_content");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);

    final String title = "Title has been updated: edited topic title\nContent has been updated: edited topic content";
    ExoSocialActivity activity = createActivity(title);
    I18NActivityUtils.addResourceKey(activity, "update_topic_title", "edited topic title");
    I18NActivityUtils.addResourceKey(activity, "update_topic_content", "edited topic content");
    
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity = i18NActivityProcessor.processKeys(activity, enLocale);

    assertEquals("Title has been updated: edited topic title<br/>Content has been updated: edited topic content", newActivity.getTitle());

  }
  
  public void testCompoundMessagesNew() throws Exception {
    Map<String, String> activityKeyTypeMapping = new LinkedHashMap<String, String>();
    activityKeyTypeMapping.put("add_topic", "add_topic");
    activityKeyTypeMapping.put("update_topic_title", "FakeResourceBundle.update_topic_title");
    activityKeyTypeMapping.put("update_topic_content", "FakeResourceBundle.update_topic_content");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);

    final String title = "Title has been updated: edited topic title";
    ExoSocialActivity activity = createActivity(title);
    I18NActivityUtils.addResourceKey(activity, "update_topic_title", "edited topic title");
    
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity = i18NActivityProcessor.processKeys(activity, enLocale);

    assertEquals("Title has been updated: edited topic title", newActivity.getTitle());

  }
  
  public void testCompoundMessagesNewWithNull() throws Exception {
    Map<String, String> activityKeyTypeMapping = new LinkedHashMap<String, String>();
    activityKeyTypeMapping.put("add_topic", "add_topic");
    activityKeyTypeMapping.put("update_topic_title", "FakeResourceBundle.update_topic_title");
    activityKeyTypeMapping.put("update_topic_content", "FakeResourceBundle.update_topic_content");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);

    final String title = "Title has been updated: ";
    ExoSocialActivity activity = createActivity(title);
    I18NActivityUtils.addResourceKey(activity, "update_topic_title", null);
    
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity = i18NActivityProcessor.processKeys(activity, enLocale);

    assertEquals("Title has been updated: ", newActivity.getTitle());

  }
  
  public void testCompoundMessagesNewWithEmpty() throws Exception {
    Map<String, String> activityKeyTypeMapping = new LinkedHashMap<String, String>();
    activityKeyTypeMapping.put("add_topic", "add_topic");
    activityKeyTypeMapping.put("update_topic_title", "FakeResourceBundle.update_topic_title");
    activityKeyTypeMapping.put("update_topic_content", "FakeResourceBundle.update_topic_content");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);

    final String title = "Content has been updated: ";
    ExoSocialActivity activity = createActivity(title);
    I18NActivityUtils.addResourceKey(activity, "update_topic_content", "");
    
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity = i18NActivityProcessor.processKeys(activity, enLocale);

    assertEquals("Content has been updated: ", newActivity.getTitle());

  }
  
  public void testCompoundSingleQuote() throws Exception {
    Map<String, String> activityKeyTypeMapping = new LinkedHashMap<String, String>();
    activityKeyTypeMapping.put("add_topic", "add_topic");
    activityKeyTypeMapping.put("update_topic_title", "FakeResourceBundle.update_topic_title");
    activityKeyTypeMapping.put("update_topic_content", "FakeResourceBundle.update_topic_content");
    activityKeyTypeMapping.put("test_single_quote", "FakeResourceBundle.test_single_quote");
    activityKeyTypeMapping.put("test_three_quote", "FakeResourceBundle.test_three_quote");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);

    String title = "I'm connected mary";
    ExoSocialActivity activity = createActivity(title);
    I18NActivityUtils.addResourceKey(activity, "test_single_quote", "mary");
    
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity = i18NActivityProcessor.processKeys(activity, enLocale);

    assertEquals("I'm connected to mary", newActivity.getTitle());
    
    //
    title = "I'm connected 'mary'";
    activity = createActivity(title);
    I18NActivityUtils.addResourceKey(activity, "test_three_quote", "mary");
    
    //
    newActivity = i18NActivityProcessor.processKeys(activity, enLocale);
    assertEquals("I'm connected to 'mary'", newActivity.getTitle());

  }

  public void testNoRegisteredActivityKeyType() throws Exception {
    Map<String, String> activityKeyTypeMapping = new HashMap<String, String>();
    activityKeyTypeMapping.put("hello", "FakeResourceBundle.hello");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);

    final String title = "hello world from title.";
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);
    ExoSocialActivity activity = createActivity("hell", title);
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity = i18NActivityProcessor.process(activity, enLocale);
    assertEquals("hello world from title.", newActivity.getTitle());
  }

  public void testMissingMessageBundleKey() throws Exception {
    Map<String, String> activityKeyTypeMapping = new HashMap<String, String>();
    activityKeyTypeMapping.put("hello", "FakeResourceBundle.hello");
    activityKeyTypeMapping.put("hell", "FakeResourceBundle.hell");
    initActivityResourceBundlePlugin(activityKeyTypeMapping);

    final String title = "hello world from title.";
    i18NActivityProcessor.addActivityResourceBundlePlugin(activityResourceBundlePlugin);
    ExoSocialActivity activity = createActivity("hell", title);
    Locale enLocale = new Locale("en");

    ExoSocialActivity newActivity = i18NActivityProcessor.process(activity, enLocale);
    assertEquals("hello world from title.", newActivity.getTitle());
  }


  private FakeResourceBundleService getResourceBundleService() throws Exception {
    FakeResourceBundleService fakeResourceBundleService = new FakeResourceBundleService(createService());
    

    //register resource bundle plugin
    ValuesParam valuesParam = new ValuesParam();
    valuesParam.setName("classpath.resources");
    valuesParam.setDescription("register FakeResourceBundle message bundle");
    List<String> messageBundleKeys = new ArrayList<String>();
    messageBundleKeys.add("FakeResourceBundle");
    valuesParam.setValues((ArrayList) messageBundleKeys);
    InitParams initParams = new InitParams();
    initParams.addParam(valuesParam);
    BaseResourceBundlePlugin resourceBundlePlugin = new BaseResourceBundlePlugin(initParams);

    fakeResourceBundleService.addResourceBundle(resourceBundlePlugin);
    return fakeResourceBundleService;
  }

  private ExoSocialActivity createActivity(String titleId, String title) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setType("fake-type");
    activity.setTitleId(titleId);
    activity.setTitle(title);
    return activity;
  }
  
  private ExoSocialActivity createActivity(String title) {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setType("fake-type");
    activity.setTitle(title);
    return activity;
  }

  private void initActivityResourceBundlePlugin(Map<String, String> activityKeyTypeMapping) {
    InitParams initParams = new InitParams();
    initParams.addParam(getObjectParameter("FakeResourceBundle", activityKeyTypeMapping));
    activityResourceBundlePlugin = new ActivityResourceBundlePlugin(initParams);
    activityResourceBundlePlugin.setName("fake-type");
  }
  
  private LocaleConfigService createService() throws Exception {
    ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("locale.config.file");
    param.setValue("classpath:/conf/locales-config.xml");
    params.addParameter(param);

    //
    LocaleConfigService service = new LocaleConfigServiceImpl(params, cm);
    return service;
  }

  /**
   * The fake ResourceBundleService implementation for testing purpose.
   */
  private class FakeResourceBundleService extends BaseResourceBundleService {
    
    public FakeResourceBundleService(LocaleConfigService localeService) {
      this.localeService_ = localeService;
    }

    @Override
    protected ResourceBundle getResourceBundleFromDb(String id, ResourceBundle parent, Locale locale) throws Exception {
      return null;
    }

    @Override
    public ResourceBundleData getResourceBundleData(String id) {
      return null;
    }

    @Override
    public ResourceBundleData removeResourceBundleData(String id) {
      return null;
    }

    @Override
    public void saveResourceBundle(ResourceBundleData data) {

    }

    @Override
    public PageList<ResourceBundleData> findResourceDescriptions(Query q) {
      return null;
    }
  }

  private ObjectParameter getObjectParameter(String resourceBundleKeyFile, Map<String, String> activityKeyTypeMapping) {
    ObjectParameter objectParameter = new ObjectParameter();
    objectParameter.setName(resourceBundleKeyFile);
    ActivityResourceBundlePlugin pluginConfig = new ActivityResourceBundlePlugin();
    pluginConfig.setActivityKeyTypeMapping(activityKeyTypeMapping);
    objectParameter.setObject(pluginConfig);
    return objectParameter;
  }
}

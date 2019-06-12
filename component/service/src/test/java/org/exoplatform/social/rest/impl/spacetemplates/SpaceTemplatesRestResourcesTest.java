package org.exoplatform.social.rest.impl.spacetemplates;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.space.SpaceApplication;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.SpaceTemplateConfigPlugin;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
import org.exoplatform.social.service.test.AbstractResourceTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SpaceTemplatesRestResourcesTest extends AbstractResourceTest {
  private SpaceTemplateService spaceTemplateService;
  private ConfigurationManager configurationManager;

  private SpaceTemplatesRestResourcesV1 spaceTemplatesRestResourcesV1;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");

    spaceTemplateService = getContainer().getComponentInstanceOfType(SpaceTemplateService.class);
    configurationManager = getContainer().getComponentInstanceOfType(ConfigurationManager.class);

    spaceTemplatesRestResourcesV1 = new SpaceTemplatesRestResourcesV1(spaceTemplateService, configurationManager);
    registry(spaceTemplatesRestResourcesV1);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(spaceTemplatesRestResourcesV1.getClass());
  }

  public void testShouldReturnAllSpaceTemplatesDetails() throws Exception {
    // Given
    SpaceApplication homeApplication = new SpaceApplication();
    homeApplication.setAppTitle("fakeHome");
    homeApplication.setPortletApp("fakeHomeApp");
    homeApplication.setPortletName("fakeHomeName");

    List<SpaceApplication> applicationList = new ArrayList<>();
    for (int i=0; i<3; i++) {
      SpaceApplication app = new SpaceApplication();
      app.setAppTitle("fakeTitle" + i);
      app.setPortletApp("fakeApp" + i);
      app.setPortletName("fakeName" + i);
      applicationList.add(app);
    }
    SpaceTemplate spaceTemplate = new SpaceTemplate();
    spaceTemplate.setName("custom");
    spaceTemplate.setVisibility("private");
    spaceTemplate.setRegistration("open");
    spaceTemplate.setPermissions("*:/platform/administrators");
    spaceTemplate.setHomeApplication(homeApplication);
    spaceTemplate.setSpaceApplicationList(applicationList);
    InitParams params = new InitParams();
    ObjectParameter objParam = new ObjectParameter();
    objParam.setName("template");
    objParam.setObject(spaceTemplate);
    params.addParameter(objParam);
    SpaceTemplateConfigPlugin spaceTemplateConfigPlugin = new SpaceTemplateConfigPlugin(params);
    spaceTemplateService.registerSpaceTemplatePlugin(spaceTemplateConfigPlugin);

    startSessionAs("root");

    // When
    ContainerResponse response = service("GET", getURLResource("spaceTemplates/templates"), "", null, null, "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    List<SpaceTemplate> templates = (List<SpaceTemplate>) response.getEntity();
    assertNotNull(templates);
    assertEquals(2, templates.size());
    List<SpaceTemplate> classicTemplate = templates.stream().filter(t -> t.getName().equals("classic")).collect(Collectors.toList());
    assertNotNull(classicTemplate);
    assertEquals(1, classicTemplate.size());
    List<SpaceTemplate> customTemplate = templates.stream().filter(t -> t.getName().equals("custom")).collect(Collectors.toList());
    assertNotNull(customTemplate);
    assertEquals(1, customTemplate.size());
    List<SpaceApplication> customApps = customTemplate.get(0).getSpaceApplicationList();
    assertNotNull(customApps);
    assertEquals(3, customApps.size());
  }

  public void testShouldReturnSpaceTemplateBannerStream() throws Exception {
    // Given
    SpaceApplication homeApplication = new SpaceApplication();
    homeApplication.setAppTitle("fakeHome");
    homeApplication.setPortletApp("fakeHomeApp");
    homeApplication.setPortletName("fakeHomeName");
    SpaceTemplate spaceTemplate = new SpaceTemplate();
    spaceTemplate.setName("custom");
    spaceTemplate.setVisibility("private");
    spaceTemplate.setRegistration("open");
    spaceTemplate.setBannerPath("classpath:/conf/social-extension/social/space-template/custom/banner.png");
    spaceTemplate.setHomeApplication(homeApplication);
    InitParams params = new InitParams();
    ObjectParameter objParam = new ObjectParameter();
    objParam.setName("template");
    objParam.setObject(spaceTemplate);
    params.addParameter(objParam);
    SpaceTemplateConfigPlugin spaceTemplateConfigPlugin = new SpaceTemplateConfigPlugin(params);
    spaceTemplateService.registerSpaceTemplatePlugin(spaceTemplateConfigPlugin);

    startSessionAs("root");

    // When
    ContainerResponse response = service("GET", getURLResource("spaceTemplates/bannerStream?templateName=custom"), "", null, null, "root");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    assertEquals("image", response.getContentType().getType());
    assertEquals("png", response.getContentType().getSubtype());
  }
}

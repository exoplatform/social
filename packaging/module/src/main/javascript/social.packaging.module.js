eXo.require("eXo.projects.Module");
eXo.require("eXo.projects.Product");

function getModule(params) {

  var kernel = params.kernel;
  var core = params.core;
  var eXoPortletContainer = params.eXoPortletContainer;
  var ws = params.ws;
  var jcr = params.eXoJcr;

  var module = new Module();

  module.version = "${project.version}";
  module.relativeMavenRepo = "org/exoplatform/social";
  module.relativeSRCRepo = "social";
  module.name = "social";

  var shindigVersion = "${org.shindig.version}";
  var platformCommonsVersion = "${org.exoplatform.commons.version}";

  module.component = {} ;
  module.component.common =
    new Project("org.exoplatform.social", "social-component-common","jar", module.version);

  module.component.core =
  new Project("org.exoplatform.social", "social-component-core","jar", module.version).
    addDependency(new Project("org.exoplatform.commons", "commons-component-common", "jar", platformCommonsVersion)).
    addDependency(new Project("org.exoplatform.commons", "commons-api", "jar", platformCommonsVersion));

  module.component.service =
  new Project("org.exoplatform.social", "social-component-service","jar", module.version).
    addDependency(ws.frameworks.json).
    addDependency(new Project("org.exoplatform.commons", "commons-webui-component", "jar", platformCommonsVersion));


  module.component.opensocial =
  new Project("org.exoplatform.social", "social-component-opensocial","jar", module.version);

  module.component.webui =
  new Project("org.exoplatform.social", "social-component-webui","jar", module.version).
      addDependency(new Project("org.exoplatform.commons", "commons-webui-ext", "jar", platformCommonsVersion));

  module.webapp = {};

  module.webapp.opensocial = new Project("org.exoplatform.social", "social-webapp-opensocial", "war", module.version);
  /*
    addDependency(new Project("commons-betwixt", "commons-betwixt", "jar", "0.8")).
    addDependency(new Project("net.sf.json-lib", "json-lib", "jar", "2.2")).
    addDependency(new Project("org.gatein.shindig", "shindig-social-api", "jar", shindigVersion)).
    addDependency(new Project("org.apache.geronimo.specs", "geronimo-stax-api_1.0_spec", "jar", "1.0.1"));
  */
  module.webapp.opensocial.deployName = "social";

  module.webapp.portlet = new Project("org.exoplatform.social", "social-webapp-portlet", "war", module.version);
  module.webapp.portlet.deployName = "social-portlet";

  module.webapp.resources = new Project("org.exoplatform.social", "social-webapp-resources", "war", module.version);
  module.webapp.resources.deployName = "social-resources";
  
  module.webapp.juzu = {};
  module.webapp.juzu.portlet = new Project("org.exoplatform.social", "social-webapp-juzu-portlet", "war", module.version);
  module.webapp.juzu.portlet.deployName = "juzu-social";

  //module.webapp.socialExtensionPortal = new Project("org.exoplatform.social", "social-webapp-social-extension-portal", "war", module.version);
  //module.webapp.socialExtensionPortal.deployName = "social-extension-portal";

  module.extras = {};
  module.extras.feedmash = new Project("org.exoplatform.social", "social-extras-feedmash", "jar", module.version);
  module.extras.linkComposerPlugin = new Project("org.exoplatform.social", "social-extras-link-composer-plugin", "jar", module.version);

  module.extras.migration = new Project("org.exoplatform.social", "social-extras-migration", "jar", module.version);
  module.extras.updater = new Project("org.exoplatform.social", "social-extras-updater", "jar", module.version);

  module.extras.widgetRest = new Project("org.exoplatform.social", "social-extras-widget-rest", "jar", module.version);
  module.extras.widgetResources = new Project("org.exoplatform.social", "social-extras-widget-resources", "war", module.version);
  module.extras.widgetResources.deployName = "socialWidgetResources";

  module.extension = {};

  module.extension.war =
   new Project("org.exoplatform.social", "social-extension-war", "war", module.version).
   addDependency(new Project("org.exoplatform.social", "social-extension-config", "jar", module.version));
  module.extension.war.deployName = "social-extension";

  module.demo = {};
  // demo portal
  module.demo.portal =
    new Project("org.exoplatform.social", "social-demo-war", "war", module.version).
    addDependency(new Project("org.exoplatform.social", "social-demo-config", "jar", module.version));
  module.demo.portal.deployName = "socialdemo";

  // demo rest endpoint
  module.demo.rest =
    new Project("org.exoplatform.social", "social-demo-rest", "war", module.version);
  module.demo.rest.deployName = "rest-socialdemo";

   module.server = {}

   module.server.tomcat = {}
   module.server.tomcat.patch =
   new Project("org.exoplatform.social", "social-server-tomcat-patch", "jar", module.version);

   module.server.jbossear = {}
   module.server.jbossear.patch =
   new Project("org.exoplatform.social", "social-server-jboss-patch-ear", "jar", module.version);

  return module;

}

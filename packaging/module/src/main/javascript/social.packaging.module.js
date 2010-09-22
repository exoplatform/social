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
    new Project("org.exoplatform.social", "exo.social.component.common","jar", module.version);

  module.component.core =
  new Project("org.exoplatform.social", "exo.social.component.core","jar", module.version);

  module.component.service =
  new Project("org.exoplatform.social", "exo.social.component.service","jar", module.version).
    addDependency(ws.frameworks.json);

  module.component.opensocial =
  new Project("org.exoplatform.social", "exo.social.component.opensocial","jar", module.version);

  module.component.webui =
  new Project("org.exoplatform.social", "exo.social.component.webui","jar", module.version).
      addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.webui.ext", "jar", platformCommonsVersion));

  module.webapp = {};

  module.webapp.opensocial = new Project("org.exoplatform.social", "exo.social.webapp.opensocial", "war", module.version);
  /*
    addDependency(new Project("commons-betwixt", "commons-betwixt", "jar", "0.8")).
    addDependency(new Project("net.sf.json-lib", "json-lib", "jar", "2.2")).
    addDependency(new Project("org.gatein.shindig", "shindig-social-api", "jar", shindigVersion)).
    addDependency(new Project("org.apache.geronimo.specs", "geronimo-stax-api_1.0_spec", "jar", "1.0.1"));
  */
  module.webapp.opensocial.deployName = "social";

  module.webapp.portlet = new Project("org.exoplatform.social", "exo.social.webapp.portlet", "war", module.version);
  module.webapp.portlet.deployName = "social-portlet";

  module.webapp.resources = new Project("org.exoplatform.social", "exo.social.webapp.resources", "war", module.version);
  module.webapp.resources.deployName = "social-resources";

  module.webapp.socialExtensionPortal = new Project("org.exoplatform.social", "exo.social.webapp.social-extension-portal", "war", module.version);
  module.webapp.socialExtensionPortal.deployName = "social-extension-portal";

  module.extras = {};
  module.extras.feedmash = new Project("org.exoplatform.social", "exo.social.extras.feedmash", "jar", module.version);
  module.extras.linkComposerPlugin = new Project("org.exoplatform.social", "exo.social.extras.link-composer-plugin", "jar", module.version);

  module.extras.widgetRest = new Project("org.exoplatform.social", "exo.social.extras.widget.rest", "jar", module.version);
  module.extras.widgetResources = new Project("org.exoplatform.social", "exo.social.extras.widget.resources", "war", module.version);
  module.extras.widgetResources.deployName = "socialWidgetResources";

  module.extension = {};

  module.extension.war =
   new Project("org.exoplatform.social", "exo.social.extension.war", "war", module.version).
   addDependency(new Project("org.exoplatform.social", "exo.social.extension.config", "jar", module.version));
  module.extension.war.deployName = "social-extension";

  module.demo = {};
  // demo portal
  module.demo.portal =
    new Project("org.exoplatform.social", "exo.social.demo.war", "war", module.version).
    addDependency(new Project("org.exoplatform.social", "exo.social.demo.config", "jar", module.version));
  module.demo.portal.deployName = "socialdemo";

  // demo rest endpoint
  module.demo.rest =
    new Project("org.exoplatform.social", "exo.social.demo.rest", "war", module.version);
  module.demo.rest.deployName = "rest-socialdemo";

   module.server = {}

   module.server.tomcat = {}
   module.server.tomcat.patch =
   new Project("org.exoplatform.social", "exo.social.server.tomcat.patch", "jar", module.version);

   module.server.jbossear = {}
   module.server.jbossear.patch =
   new Project("org.exoplatform.social", "exo.social.server.jboss.patch-ear", "jar", module.version);

  return module;

}

eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {
  var product = new Product();

  product.name = "eXoPortal" ;
  product.portalwar = "portal.war" ;
  product.codeRepo = "portal" ;//module in modules/portal/module.js
  product.serverPluginVersion = "${org.exoplatform.portal.version}"; // CHANGED for Social to match portal version. It was ${project.version}

  var kernel = Module.GetModule("kernel") ;
  var core = Module.GetModule("core") ;
  var ws = Module.GetModule("ws", {kernel : kernel, core : core});
  var eXoJcr = Module.GetModule("jcr", {kernel : kernel, core : core, ws : ws}) ;
  var portal = Module.GetModule("portal", {kernel : kernel, ws:ws, core : core, eXoJcr : eXoJcr});
  var social = Module.GetModule("social", {kernel : kernel, ws:ws, core : core, eXoJcr : eXoJcr, portal:portal});

  product.addDependencies(portal.web.rest) ;
  product.addDependencies(portal.portlet.exoadmin) ;
  product.addDependencies(portal.portlet.web) ;
  product.addDependencies(portal.portlet.dashboard) ;
  product.addDependencies(portal.portlet.redirect);
  product.addDependencies(portal.eXoGadgetServer) ;
  product.addDependencies(portal.eXoGadgets) ;
  product.addDependencies(portal.webui.portal);
  product.addDependencies(portal.web.eXoResources);
  product.addDependencies(portal.web.portal);

  // starter for social
  portal.starter = new Project("org.exoplatform.portal", "exo.portal.starter.war", "war", portal.version);
  portal.starter.deployName = "zzzstarter";
  product.addDependencies(portal.starter);
  product.addDependencies(social.component.common);
  product.addDependencies(social.component.core);
  product.addDependencies(social.component.service);
  product.addDependencies(social.component.opensocial);
  product.addDependencies(social.component.webui);
  product.addDependencies(social.webapp.opensocial) ;
  product.addDependencies(social.webapp.portlet);
  product.addDependencies(social.webapp.juzu.portlet);
  product.addDependencies(social.webapp.resources);
  //product.addDependencies(social.webapp.socialExtensionPortal);
  product.addDependencies(social.extras.feedmash);
  product.addDependencies(social.extras.linkComposerPlugin);
  product.addDependencies(social.extras.widgetRest);
  product.addDependencies(social.extras.widgetResources);
  //product.addDependencies(social.extension.war) ;
  product.addDependencies(social.demo.portal);
  product.addDependencies(social.demo.rest);

  product.addServerPatch("tomcat", social.server.tomcat.patch);
  product.addServerPatch("jboss",  portal.server.jboss.patch);
  product.addServerPatch("jbossear",  social.server.jbossear.patch);

  //product.removeDependency(new Project("org.exoplatform.jcr", "exo.jcr.component.ftp", "jar", eXoJcr.version));

  //upgrade lib for opensocial
  //product.removeDependency(new Project("commons-beanutils", "commons-beanutils", "jar", "1.6"));
  //product.removeDependency(new Project("commons-collections", "commons-collections", "jar", "2.1"));
  //product.removeDependency(new Project("commons-collections", "commons-collections", "jar", "3.1"));
  //product.removeDependency(new Project("commons-digester", "commons-digester", "jar", "1.6"));
  //product.removeDependency(new Project("commons-httpclient", "commons-httpclient", "jar", "3.0"));
  //product.removeDependency(new Project("xstream", "xstream", "jar", "1.0.2"));
  //product.removeDependency(new Project("org.exoplatform.core", "exo.core.component.document", "jar", core.version));
  //product.removeDependency(new Project("org.exoplatform.core", "exo.core.component.xml-processing", "jar", core.version));


  //product.addDependencies(new Project("commons-beanutils", "commons-beanutils", "jar", "1.7.0"));
  //product.addDependencies(new Project("commons-beanutils", "commons-beanutils-core", "jar", "1.7.0"));
  //product.addDependencies(new Project("commons-digester", "commons-digester", "jar", "1.7"));
  //product.addDependencies(new Project("commons-httpclient", "commons-httpclient", "jar", "3.1"));

  //product.addDependencies(new Project("findbugs", "annotations", "jar", "1.0.0"));

  product.module = social ;
  product.dependencyModule = [kernel, core, ws, eXoJcr, portal];

  return product ;

  }

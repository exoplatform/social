eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getModule(params) {

  var kernel = params.kernel;
  var core = params.core;
  var eXoPortletContainer = params.eXoPortletContainer;
	var ws = params.ws;
  var jcr = params.eXoJcr;

  var module = new Module();

  module.version = "1.0-Beta02-SNAPSHOT" ;
  module.relativeMavenRepo =  "org/exoplatform/social" ;
  module.relativeSRCRepo =  "social/trunk" ;
  module.name = "social" ;  
	
  module.component = {} ;
  module.component.people = 
	new Project("org.exoplatform.social", "exo.social.component.people","jar", module.version);

  module.component.space = 
	new Project("org.exoplatform.social", "exo.social.component.space","jar", module.version);

  module.component.opensocial = 
	new Project("org.exoplatform.social", "exo.social.component.opensocial","jar", module.version);

  module.web = {} ;
  /*module.web.socialportal = 
    new Project("org.exoplatform.social", "exo.social.web.portal", "exo-portal", module.version).
    addDependency(jcr.frameworks.command).
    addDependency(jcr.frameworks.web);*/
	
  module.web.eXoResources = new Project("org.exoplatform.social", "exo.social.web.socialResources", "war", module.version);
  module.web.eXoResources.deployName = "eXoResourcesSocial" ;
	
  module.portlet = {}
  module.portlet.space = new Project("org.exoplatform.social", "exo.social.portlet.space", "exo-portlet", module.version);
  module.portlet.space.deployName = "space" ;

  module.portlet.profile = new Project("org.exoplatform.social", "exo.social.portlet.profile", "exo-portlet", module.version);
  module.portlet.profile.deployName = "profile" ;

  module.application = {}
  module.application.rest = new Project("org.exoplatform.social", "exo.social.application.rest","jar", module.version).
	addDependency(ws.frameworks.json);  	

  module.web.opensocial =new Project("org.exoplatform.social", "exo.social.web.opensocial", "war", module.version).
		addDependency(new Project("commons-betwixt", "commons-betwixt", "jar", "0.8")).
		addDependency(new Project("net.sf.json-lib", "json-lib", "jar", "2.2")).
		addDependency(new Project("org.apache.shindig", "shindig-social-api", "jar", "SNAPSHOT-r790473")).
		addDependency(new Project("org.apache.geronimo.specs", "geronimo-stax-api_1.0_spec", "jar", "1.0.1"));


  module.web.opensocial.deployName = "social" ;
		
	module.extension = {};
	module.extension.war =
   new Project("org.exoplatform.social", "exo.social.extension.war", "war", module.version).
   addDependency(new Project("org.exoplatform.social", "exo.social.extension.jar", "jar", module.version)).
   addDependency(new Project("org.exoplatform.social", "exo.social.extension.config", "jar", module.version));
	module.extension.war.deployName = "social-ext";

   module.demo = {};
   // demo portal
   module.demo.portal = 
	   new Project("org.exoplatform.social", "exo.social.demo.war", "war", module.version).
	   addDependency(new Project("org.exoplatform.social", "exo.social.demo.config", "jar", module.version)).
		addDependency(new Project("org.exoplatform.social", "exo.social.demo.jar", "jar", module.version));
	   module.demo.portal.deployName = "socialdemo";  
	   
   // demo rest endpoint	   
   module.demo.rest = 
       new Project("org.exoplatform.social", "exo.social.demo.rest-war", "war", module.version);
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

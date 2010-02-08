/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {
  var product = new Product();
  
  product.name = "eXoSocial" ;
  product.portalwar = "portal.war" ;
  product.codeRepo = "social" ;//module in modules/portal/module.js
  product.serverPluginVersion = "3.0.0-Beta05";

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
  product.addDependencies(portal.eXoGadgetServer) ;
  product.addDependencies(portal.eXoGadgets) ;
  product.addDependencies(portal.webui.portal);

  product.addDependencies(portal.web.eXoResources);

  product.addDependencies(portal.web.portal);

	// starter for social
	//portal.starter = new Project("org.exoplatform.portal", "exo.portal.starter.war", "war", portal.version);
	//portal.starter.deployName = "starter";
	//product.addDependencies(portal.starter);
	//product.addDependencies(social.web.socialportal) ;
  product.addDependencies(social.extension.war) ;
  product.addDependencies(social.web.eXoResources) ;
  product.addDependencies(social.component.people) ;
  product.addDependencies(social.component.space) ;
  product.addDependencies(social.portlet.space) ;
  product.addDependencies(social.portlet.profile);
  product.addDependencies(social.web.opensocial);
  product.addDependencies(social.component.opensocial);
  product.addDependencies(social.application.rest);
  product.addDependencies(social.webui.social);

  product.addDependencies(social.demo.portal);
  product.addDependencies(social.demo.rest);

  product.addServerPatch("tomcat", social.server.tomcat.patch);
  product.addServerPatch("jboss",  portal.server.jboss.patch);
  product.addServerPatch("jbossear",  social.server.jbossear.patch);

	product.removeDependency(new Project("org.exoplatform.jcr", "exo.jcr.component.ftp", "jar", eXoJcr.version));

  //upgrade lib for opensocial
  product.removeDependency(new Project("commons-beanutils", "commons-beanutils", "jar", "1.6"));
  product.removeDependency(new Project("commons-collections", "commons-collections", "jar", "2.1"));
  product.removeDependency(new Project("commons-collections", "commons-collections", "jar", "3.1"));
  product.removeDependency(new Project("commons-digester", "commons-digester", "jar", "1.6"));
  product.removeDependency(new Project("commons-httpclient", "commons-httpclient", "jar", "3.0"));
  product.removeDependency(new Project("xstream", "xstream", "jar", "1.0.2"));

  product.addDependencies(new Project("commons-beanutils", "commons-beanutils", "jar", "1.7.0"));
  product.addDependencies(new Project("commons-beanutils", "commons-beanutils-core", "jar", "1.7.0"));
  product.addDependencies(new Project("commons-digester", "commons-digester", "jar", "1.7"));
  product.addDependencies(new Project("commons-httpclient", "commons-httpclient", "jar", "3.1"));

  product.addDependencies(new Project("findbugs", "annotations", "jar", "1.0.0"));
 
	product.module = social ;
  product.dependencyModule = [kernel, core, ws, eXoJcr, portal];


  return product ;
}

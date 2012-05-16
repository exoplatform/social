SOC-2641: ConcurrentModificationException in UIUserActivityStreamPortlet

What is the problem to fix?
Running cloud-workspaces.com 1.0.0-beta7 found this exception in logs:
2012-02-21 13:55:51,300 [http-8080-24] ERROR currentTenant=exopartners org.exoplatform.webui.application.portlet.PortletApplicationController - Error while rendering >the porlet
org.exoplatform.groovyscript.TemplateRuntimeException: Groovy template exception at DataText[pos=Position[col=1,line=34],data=  uicomponent.refresh();] for template app:/groovy/social/portlet/UIUserActivityStreamPortlet.gtmpl
 at org.exoplatform.groovyscript.GroovyScript.buildRuntimeException(GroovyScript.java:178) [exo.portal.component.scripting-3.2.2-PLF.jar:3.2.2-PLF]
 at org.exoplatform.groovyscript.GroovyScript.render(GroovyScript.java:121) [exo.portal.component.scripting-3.2.2-PLF.jar:3.2.2-PLF]
 at org.exoplatform.groovyscript.GroovyTemplate.render(GroovyTemplate.java:118) [exo.portal.component.scripting-3.2.2-PLF.jar:3.2.2-PLF]
     ...

Problem analysis
	There problem when Portlet remove child component from child component list, while another thread modify same list of child component cause concurent modify exception. 

How is the problem fixed?
	Add lock synchronized object to lock the child component list while in modify operation.

Reproduction test
	Run server in debug mode, add breakpoint at modification list code and hold one thread at that point. 
	Open new tab in browser and point to that UI component. 
	Exception happened.

Changes in SNIFF/FUNC/REG tests
	No

Changes in Selenium scripts 
	No

Documentation (User/Admin/Dev/Ref) changes:
	No

Configuration changes:
	No

Will previous configuration continue to work?
	Yes

Can this bug fix have any side effects on current client projects?
	- Function or ClassName change: No 
	- Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?
	Only small impact at synchronized block and it rare to happend on this case.

PM Comment
	PM validated.

Support Comment
	Support validated.

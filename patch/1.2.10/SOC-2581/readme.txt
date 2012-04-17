SOC-2581: PortletStatisticLifecycle in GateIn is no longer necessary

What is the problem to fix?
PortletStatisticLifecycle listener in GateIn is no longer necessary and is removed soon, ApplicationMonitoringFilter filter could replace it. However, Social still uses PortletStatisticLifecycle listener in webui configuration, please check and correct it.
This task is quite urgent, because it's needed for port Platform to use EPP in next time.

Problem analysis
Follow the changes at portal side then social now not need to add life-cycle listener at each portlet definition. Portal will support service to add it to each definition as default.

How is the problem fixed?
Add ApplicationMonitoringFilter service by configuration at social side then remove all PortletStatisticLifecycle at portlet definition level.
Add configurations as below:
- configuration.properties
///////////////////////////////////////////////////////////////////
#Configuration for jboss server
gatein.conf.dir=${jboss.server.home.dir}/conf/gatein

# Global portlet.xml
+gatein.portlet.validation=true
+gatein.portlet.config=${gatein.conf.dir}/portlet.xml
///////////////////////////////////////////////////////////////////

- ../server/default/conf/gatein/portlet.xml
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
<portlet-app version="1.0" xmlns="http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd">

 <!-- This filter collects runtime request statistics -->
 <filter>
   <filter-name>org.exoplatform.portal.application.ApplicationMonitoringFilter</filter-name>
   <filter-class>org.exoplatform.portal.application.ApplicationMonitoringFilter</filter-class>
   <lifecycle>ACTION_PHASE</lifecycle>
   <lifecycle>RENDER_PHASE</lifecycle>
   <lifecycle>EVENT_PHASE</lifecycle>
   <lifecycle>RESOURCE_PHASE</lifecycle>
 </filter>

</portlet-app>
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

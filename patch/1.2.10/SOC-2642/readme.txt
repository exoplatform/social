SOC-2642: SpaceUtils - appStatus is not in correct form

What is the problem to fix?
Running cloud-workspaces.com 1.0.4 found such warnings in logs :
...
 2012-05-02 13:39:51,209 [http-8080-12] WARN currentTenant=meganetwork org.exoplatform.social.core.space.SpaceUtils - appStatus is not in correct form of [appId:appNodeName:isRemovableString:status] : Agenda:true:active
...

After such warning also appear following error:
...
 2012-05-02 13:39:51,249 [http-8080-12] ERROR currentTenant=meganetwork portal:UIPortalApplication - Error during the processAction phase
java.lang.NullPointerException: null
 at org.exoplatform.social.core.space.SpaceUtils.isRemovableApp(SpaceUtils.java:1237) [exo.social.component.core-1.2.8.jar:1.2.8]
 ...

Problem analysis
Problem in logic code since in list applications that contain the same part of name between applications. In that case, maybe that just is Agenda:true:active while the right format must be [appId:appNodeName:isRemovableString:status], after split the condition when check the length is not equal 4 is true that return null lead to hook into one process that fire out NPE.



How is the problem fixed?
Change the way in checking each application in list applications to be processed then ignore the invalid case, make changes in SpaceUtils#getAppStatusPattern(String, String) to check with appId exactly as code below:

private static String getAppStatusPattern(String installedApps, String appId) {
     // .....................................

     for (String app : apps) {
       if (app.contains(appId)) {
          String[] splited = app.split(":");
         if (splited.length != 4) {
            LOG.warn("appStatus is not in correct form of [appId:appNodeName:isRemovableString:status] : "
                    + app);
           return null;
          }

         return app;
        }
      }
   
   return null;
  }



Reproduction test
1. Create a new space
2. Go to Applications Setting and add Agenda Gadget (Collaboration Categories) 
- Get an Unknown Error
The log is the same as in description.

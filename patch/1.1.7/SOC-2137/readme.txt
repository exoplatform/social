Summary

    * Status: Can't create a space with Startable Service or Rest one
    * CCP Issue: CCP-1125, Product Jira Issue: SOC-2137.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
The code of space's creation doesn't work in a startable service or Rest one :
?
String spaceName = "mySpace";
            String creator = "root";
            PortalContainer container = PortalContainer.getInstance();
            SpaceService spaceService = (SpaceService) container.getComponentInstanceOfType(SpaceService.class);
            // We verify if there is no space already created
            try {
            Space space = spaceService.getSpaceByName(spaceName);
            if (space == null) {
              space = new Space();
              space.setName(spaceName);
              space.setRegistration(Space.OPEN);
              space.setDescription("space description");
              //DefaultSpaceApplicationHander is the default implementation of SpaceApplicationHandler.
                      //You can create your own by extending SpaceApplicationHandler.
                      //The default type is "classic" (DefaultSpaceApplicationHandler.NAME = clasic)
              space.setType(DefaultSpaceApplicationHandler.NAME);
              //We create the space
 
              space = spaceService.createSpace(space, creator);
              //We initialize the applications
 
                spaceService.initApp(space);
 }
            } catch (SpaceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e1) {
              // TODO Auto-generated catch block
              e1.printStackTrace();
                }
      }

We have this exception :
?
INFO: added activity processor class org.exoplatform.social.core.processor.TemplateParamsProcessor
java.lang.NullPointerException
        at org.exoplatform.portal.webui.util.Util.getPortalRequestContext(Util.java:53)
        at org.exoplatform.portal.webui.util.Util.getUIPortalApplication(Util.java:60)
        at org.exoplatform.social.core.space.SpaceUtils.isSpaceNameExisted(SpaceUtils.java:725)
        at org.exoplatform.social.core.space.SpaceUtils.createGroup(SpaceUtils.java:668)
        at org.exoplatform.social.core.space.impl.SpaceServiceImpl.createSpace(SpaceServiceImpl.java:335)
        at org.exoplatform.social.core.space.impl.SpaceServiceImpl.createSpace(SpaceServiceImpl.java:326)
        at org.exoplatform.macif.spaces.CreateSpaces.createspace(CreateSpaces.java:58)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:597)
        ...............................................................
Fix description

How is the problem fixed?

    * Problem comes from the using of UI Context in service so when making request with REST then NPE will be occurred.
      To solve this problem, we have to check in case request is REST then return at the point UI Context is called.


Patch files:SOC-2137.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* NO
Configuration changes

Configuration changes:
* NO

Will previous configuration continue to work?
* YES
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: NONE

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PM validated

Support Comment
* Support validated

QA Feedbacks
*


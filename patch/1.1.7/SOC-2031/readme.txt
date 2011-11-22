Summary

    * Status: Can't delete a space from a webservice
    * CCP Issue: CCP-1068, Product Jira Issue: SOC-2031.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
it seems there is still some part of the code that use getPortalRequestContext. This is not available in the context of a REST webservice.
?
WARNING: WebApplication exception occurs.
org.exoplatform.social.core.space.SpaceException: org.exoplatform.social.core.space.SpaceException: java.lang.NullPointerException
    at org.exoplatform.social.core.space.impl.SpaceServiceImpl.deleteSpace(SpaceServiceImpl.java:371)
    at org.exoplatform.api.webservices.v1.social.spaces.model.SpaceFeed.deleteEntry(SpaceFeed.java:140)
    at org.exoplatform.api.webservices.v1.helper.SimpleFeedWebService.delete(SimpleFeedWebService.java:116)
    at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 
        ..........................................................................................
 
    at java.lang.Thread.run(Thread.java:680)
Caused by: org.exoplatform.social.core.space.SpaceException: java.lang.NullPointerException
    at org.exoplatform.social.core.space.SpaceUtils.removeGroup(SpaceUtils.java:715)
    at org.exoplatform.social.core.space.impl.SpaceServiceImpl.deleteSpace(SpaceServiceImpl.java:363)
    ... 38 more
Caused by: java.lang.NullPointerException
    at org.exoplatform.portal.webui.util.Util.getPortalRequestContext(Util.java:53)
    at org.exoplatform.portal.webui.util.Util.getUIPortalApplication(Util.java:60)
    at org.exoplatform.social.core.space.impl.SocialGroupEventListenerImpl.preDelete(SocialGroupEventListenerImpl.java:85)
    at org.exoplatform.services.organization.idm.GroupDAOImpl.preDelete(GroupDAOImpl.java:513)
    at org.exoplatform.services.organization.idm.GroupDAOImpl.removeGroup(GroupDAOImpl.java:164)
    at org.exoplatform.social.core.space.SpaceUtils.removeGroup(SpaceUtils.java:713)
    ... 39 more
Fix description

How is the problem fixed?
The root cause is when deleting a Space then preDelete() method of GroupEventListener is invoked. With the existing service, UI Context is using for updating UI working space. So when request is REST then NPE is fired. To solve this problem we need remove these line of codes that relate to UI Context in service.

Patch files: SOC-2031.patch	

Tests to perform

Reproduction test
*cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PM validated

Support Comment

    * Support validated

QA Feedbacks
*


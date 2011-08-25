Summary

    * Status: Don't add external plugins configurations embedded in the jar
    * CCP Issue: N/A, Product Jira Issue: SOC-1961.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In Platform, When the social profile is not activated, an error is thrown when updating an user profile in the organization portlet.
Steps to reproduce :

   1. start PLF with the command line : ./start_eXo.sh collaboration,knowledge
   2. log in the acme site with root
   3. go to the organization management portlet
   4. edit an user
   5. go to the User Profile tab
   6. change the value of one the fields
   7. click on Save -> Error with the Exception

java.lang.RuntimeException: Cannot instantiate component key=org.exoplatform.social.core.manager.IdentityManager type=org.exoplatform.social.core.manager.IdentityManager found at jar:file:/home/thomas/exoplatform/bundles/PLF/eXoPlatform-3.0.4/bin/tomcat6-bundle/lib/exo.social.component.core-1.1.4.jar!/conf/portal/configuration.xml
    at org.exoplatform.container.jmx.MX4JComponentAdapter.getComponentInstance(MX4JComponentAdapter.java:131)
    at org.exoplatform.container.management.ManageableComponentAdapter.getComponentInstance(ManageableComponentAdapter.java:68)
    at org.exoplatform.container.ConcurrentPicoContainer.getInstance(ConcurrentPicoContainer.java:400)
    at org.exoplatform.container.ConcurrentPicoContainer.getComponentInstanceOfType(ConcurrentPicoContainer.java:389)
    at org.exoplatform.container.CachingContainer.getComponentInstanceOfType(CachingContainer.java:139)
    at org.exoplatform.social.core.listeners.SocialUserEventListenerImpl.postSave(SocialUserEventListenerImpl.java:48)
...

Fix description

Problem analysis
This is due to the fact that the jar:/conf/portal/configuration.xml is always deployed on PortalContainers, and we have no way to redefine external component plugins defined there. The configuration that causes this problem is:
?
<external-component-plugins>
   <target-component>org.exoplatform.services.organization.OrganizationService</target-component>
   <component-plugin>
     <name>new.user.event.listener</name>
     <set-method>addListenerPlugin</set-method>
     <type>org.exoplatform.social.core.listeners.SocialUserEventListenerImpl</type>
   </component-plugin>
  </external-component-plugins>

Please move this declaration in social.war configuration files instead of.

How is the problem fixed?
Move the declaration of org.exoplatform.social.core.listeners.SocialUserEventListenerImpl (external plugin) from social core component (jar) to social-demo and social-extension (war) for easier customization (enable, disable).

Patch file: SOC-1961.patch

Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * Cf. above

Will previous configuration continue to work?

    * Cf. above

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?

    * N/A.

Validation (PM/Support/QA)

PM Comment

    * Patch approved.

Support Comment

    * Patch validated.

QA Feedbacks
*

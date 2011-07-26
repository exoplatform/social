Summary

    * Status: Move configuration from .jar file to .war file
    * CCP Issue: CCP-1015, Product Jira Issue: SOC-1915.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

Forum topic: http://int.exoplatform.org/portal/public/intranet/forum/topic/topic04a38d962e8902a90044b1ad68df50fd

In Social code(exo.social.component.core-1.1.6-SNAPSHOT.jar), we have some listener configurations in file configuration.xml
?
<external-component-plugins>
    <target-component>org.exoplatform.social.core.manager.RelationshipManager</target-component>
    <component-plugin>
      <name>RelationshipPublisher</name>
      <set-method>addListenerPlugin</set-method>
      <type>org.exoplatform.social.core.application.RelationshipPublisher</type>
    </component-plugin>
  </external-component-plugins>

So that we can not override or disable the listeners.
Please move these listeners from .jar file to .war file (social-extension.war, in file social-extension/WEB-INF/conf/social-extension/social/component-plugins-configuration.xml). If we move them to .war file, we can disable OR overridden the listeners easily.

Fix description

How is the problem fixed?

    * Move all component plugins from social core component (jar) to social-demo, social-extension (war) for easier customization (enable, disable).

Patch files:SOC-1915.patch

Tests to perform

Reproduction test

    * Improvement, there is no need to reproduce.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * social component plugins are moved from core (jar) to web apps (war) which is transparent to other projects.

Will previous configuration continue to work?

    * Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?

    * No risk at all.

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch validated

Support Comment

    * Support review: Patch validated

QA Feedbacks
	

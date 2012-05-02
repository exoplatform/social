Summary

Status: People Application's first load is very slow when people directory is relatively big
CCP Issue: PLF:CCP-958, Product Jira Issue: PLF:SOC-1776.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?

SOC-1776
Problem: People Application's first load is very slow when people directory is relatively big
To reproduce:
1)Connect PLF to an LDAP sever with 700 users for example(or find another way to create this number of users)
2)Go to people portlet
==>The people page takes more than one minute to load
– Environment: Test done under PLF 3.0.4+Jboss EAP 5.0.1 with Xmx 1800 m

SOC-2207
Problem: After applying SOC-1776 on plf 3.0.6, I have a problem with link provider.
ActivityStream is on portal intranet, and link to users profile in activityStream are http://localhost:8080/portal/private/classic/profile/demo.
In social-configuration.xml, I have this

<component>
    <key>org.exoplatform.social.core.service.LinkProvider</key>
    <type>org.exoplatform.social.core.service.LinkProvider</type>
    <init-params>
        <value-param>
          <name>predefinedOwner</name>
          <description>this for generate profile link</description>
          <value>intranet</value>
        </value-param>
    </init-params>
  </component>
After debugging, we can see that this

public LinkProvider(InitParams params){
    if(params.getValueParam("predefinedOwner") != null) DEFAULT_PORTAL_OWNER = params.getValueParam("predefinedOwner").getValue();
  }
is never executed.

We always go through

public LinkProvider(InitParams params, IdentityManager identityManager) {
    this.identityManager = identityManager;
    init(params);
  }
 
  private void init(InitParams params) {
  }
and DEFAULT_PORTAL_OWNER is never set to parameters setetd in configuration.xml.
– To reproduce:

Go to intranet home
Share your status
See your status has been shared on screen
Click on username link on the activity stream
o Actual result: Link to the selected username: http://localhost:8080/portal/private/classic/profile/john --> This link is not exist
o Expected result: Link to the selected username: http://localhost:8080/portal/private/intranet/profile/john --> Displays the the john's profile.
Fix description
How is the problem fixed?

SOC-1776 The solution is trying to avoid calling getIdentities(String, Boolean) when we first access the page. With this patch we change the behaviour : By default when you arrive on the people page, apply a filter (e: g. filter on 'A' letter). This should reduce the number of results. It won't solve the fact that you may need pagination on 'A' results. But you have a good chance to avoid loading the full user directory.... And The problem will be the same with the "All" tab so "All" will be removed as a tradeoff.
SOC-2207 To get the default portal owner from configuration file and set to DEFAULT_PORTAL_OWNER we must add the processing code into init method to make sure it run through as below:
LinkProvider.java
public LinkProvider(InitParams params, IdentityManager identityManager) {
  this.identityManager = identityManager;
  init(params);
}
 
private void init(InitParams params) {
  if(params.getValueParam("predefinedOwner") != null)
    DEFAULT_PORTAL_OWNER = params.getValueParam("predefinedOwner").getValue();
}

There are currently no attachments on this page.
Tests to perform
Reproduction test
* Steps to reproduce:
1)Connect PLF to an LDAP sever with 700 users for example(or find another way to create this number of users)
2)Go to people portlet
==>The people page takes more than one minute to load

Test done under PLF 3.0.4+Jboss EAP 5.0.1 with Xmx 1800 m

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes
Documentation changes:
* Need to update user guide

Configuration changes
Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects? "All" is not displayed and "A" filter is selected by default.

Function or ClassName change: No
Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
* N/A

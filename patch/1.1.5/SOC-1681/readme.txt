Summary

    * Status: Social token generator rise problem for rest service in ks (PLF integrated) could not get user id
    * CCP Issue: CCP-809, Product Jira Issue: SOC-1681.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Can not get current user name in gadget application using rest service.
Fix description

How is the problem fixed?
To fix this bug, we made changed in Social Token Generator. As before Social used identityId to create token so if in stand-alone we can get user information in RestService but in PLF we can't. So to unify with all others, Social now using remoteId in creating token. To get current userId as in case of the sample gadget Latest Forum Post , we should make request in type of signed request .

Patch files:SOC-1681.patch

Tests to perform

Reproduction test
Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*No
Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change:None

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*


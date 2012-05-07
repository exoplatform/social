Summary

Status: Exception when accessing posts of suppressed user
CCP Issue: CCP-1041, Product Jira Issue: SOC-1994.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?
Exception when accessing posts of suppressed user

Steps to reproduce:

Login as root
Create new space (named "abc" for example)
Invite Demo to new space (Go to Space Setting > Member tab)
Logout root.
Login as Demo.
Accept root's invitation.
Add new comments to new space "abc".
Logout Demo, login as root.
Delete user Demo (Go to My Groups > Portal Administration > Manage Users and Groups)
Return to new space "abc".
Fix description
How is the problem fixed?
This bug occurs due to the Cache problem, when user removed from system old status and activities of that user still existing in cache. To solve this problem we need to clear the cache (activity cache) in case of user is deleted.

Tests to perform
Reproduction test
* cf. above

Tests performed at DevLevel
* cf. above

Tests performed at QA/Support Level
* cf. above

Documentation changes
Documentation changes:
* None

Configuration changes
Configuration changes:
* None

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: None
Is there a performance risk/cost?
* None

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment

Support review: Patch validated
QA Feedbacks
* N/A

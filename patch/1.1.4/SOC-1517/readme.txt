Summary

    * Status: Load activities from private spaces to connections tab
    * CCP Issue: CCP-806, Product Jira Issue: SOC-1517.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * the activities (forum activities) in private spaces are loaded and displayed in connections tab.

The scenario to reproduce this issue is the following:
1)Login as root
2)Go to My Spaces>Find Spaces
3)Add New Space(space1) , check validation(or close) in visibility tab then create
4)In space1 share some activities
5)send invitation to Mary to join root's Network
6)Logout and login as Mary

Excepted Result: Mary cannot see in her activity stream activities shared by root in space1 because it's a private space
Actual Result :in Mary's activity stream, you can see all activities shared by root especially those shared in space1 despite space1 is a private space.
This happens because Mary and root are connected.
Fix description

How is the problem fixed?

    * This is the bug from ActivityStorage implementation. When getting activities from connections, the activities must be in jcr path: "/exo:applications/Social_Activity/organization/%" only.

Patch information:
Patch files: SOC-1517.patch

Tests to perform

Reproduction test

    * Login by Demo, create a new space: "Test" with default options.
    * Demo access that "Test" space, share an activity "Demo posted on Test space" on that space.
    * Demo requests to connect with Mary.
    * Login by Mary, accept Demo's connection request.
    * Many open her activity page. On "connections" tab, Mary sees "Demo posted on Test space".

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * No.

Configuration changes

Configuration changes:

    * No.

Will previous configuration continue to work?

    * Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: No.

Is there a performance risk/cost?

    * A little bit performance could possibly gain. No risk or cost at all.

Validation (PM/Support/QA)

PM Comment
*PL review : patch approved.

Support Comment
*Support review : patch validated

QA Feedbacks
*


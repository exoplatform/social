Summary

    * Status: Anybody can Accept/Refuse a connection request between 2 other people
    * CCP Issue: CCP-756, Product Jira Issue: SOC-1454.
    * Complexity: low

The Proposal
Problem description

What is the problem to fix?
Invitations to connect are displayed in the activity stream. A link to accept/refuse the invitation is displayed. Anybody viewing the stream can click on it to accept/refuse the invitation.
Fix description

How is the problem fixed?
The accept and refuse links are displayed only if the current user is the receiver of the connection request.

Patch file: SOC-1454.patch

Tests to perform

Reproduction test

    * Login by John, request connect to Mary, Demo.
    * Login by Mary, accept connection request from John. Mary requests connect to Demo.
    * Login by Demo, accept connection request from John.
    * Login by John, see his activity stream on "connections" tab. John will see the connection request activity between Mary and Demo, and the "accept | refuse" action link.

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

    * No

Validation (PM/Support/QA)

PM Comment

    * PL review: patch approved.

Support Comment

    * Support review: patch validated

QA Feedbacks
*


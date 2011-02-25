Summary

    * Status: Invitation acceptance clears the connection list
    * CCP Issue: CCP-757, Product Jira Issue: SOC-1455.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  When accepting one connection, all the other connections disappear. The actual connection list is broken, only see the connection that has just been accepted.

Fix description

How is the problem fixed?

    * When a user accepts a connection request, the cache management of relationship cache should be properly updated.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: SOC-1455

Tests to perform

Reproduction test
    * Login by Root, send a connection request to Demo.
    * Login by John, send a connection request to Demo.
    * Login by Mary, send a connection request to Demo.
    * Login by Demo, accept connection requests from Root, John.
    * Restart server to clear the existing cache.
    * Login by Demo, go to "Connections" page, click on "Incoming" link and accept Mary's connection request.
    * Click on "Network", only Mary is displayed, Root and John disappear => KO.

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

    * Function or ClassName change

Is there a performance risk/cost?

    * There is cache management change, we need to perform performance test to measure.

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated

QA Feedbacks
*


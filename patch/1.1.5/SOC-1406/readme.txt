Summary

    * Status: If there are many admin per space, avatars of all admin are the same
    * CCP Issue: CCP-830, Product Jira Issue: SOC-1406.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  If there are many admin per space, avatars of all admin are the same

Problem analysis
* In loop for of users, when a user has an avatar which is not a default avatar, his/her avatar will be set. But after that, the avatar of the next leader will not be set by his own avatar. It is set however as the avatar of the previous one.

Fix description

How is the problem fixed?
*   Set right avatar.

Patch file: SOC-1406.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No changes.

Configuration changes

Configuration changes:
* No changes.

Will previous configuration continue to work?
* Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*

Validation (PM/Support/QA)

PM Comment
* Approved

Support Comment
* Validated

QA Feedbacks
*


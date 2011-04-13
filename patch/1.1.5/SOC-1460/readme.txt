Summary

    * Status: Show Unknown error when deleting default application on space
    * CCP Issue: CCP-841, Product Jira Issue: SOC-1460.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Show UNKNOWN ERROR when deleting default application on space. When deleting an application, the corresponding page node and page of that space application is not found for deleting.

Fix description

How is the problem fixed?

    * Find the right page node and page of the corresponding space application to delete.

Patch file: SOC-1460.patch

Tests to perform

Reproduction test
* Steps to reproduce:
    * Create a space AAA
    * Access to space AAA
    * Access to Space setting -> Application: select a default application (eg: member, calendar, forum, etc) to delete ==> show UNKNOWN ERROR

Tests performed at DevLevel
* No

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

Can this bug fix have any side effects on current client projects? No.

    * Function or ClassName change: No

Is there a performance risk/cost?

    * No.

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated

QA Feedbacks
*


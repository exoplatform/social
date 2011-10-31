Summary

    * Status: Search tooltip not localized
    * CCP Issue: N/A, Product Jira Issue: SOC-1847.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Steps to reproduce in social standalone:

Login
People/Directory in the Site Admin bar
Change the language to French
Hove the mouse over the glass icon of "Find contact". "Search" should be "Chercher".

Steps in PLF 3.0.x:

Login
Hove the mouse over the user name, click "Find People" menu.

Another word should be translated: Hove the mouse over invite people button, "Invite" should be translated to "Inviter".
Fix description

How is the problem fixed?
Avoid hard-coding in labels and replace all of them by values that get from resource bundle.


Patch files: SOC-1847.patch
Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*
Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*


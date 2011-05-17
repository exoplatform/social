Summary

    * Status: There is something wrong after renaming a space name
    * CCP Issue: N/A, Product Jira Issue: ECMS-1835.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Create a space named eXo2
    * Rename this space into eXo3
      -> There is something wrong

Fix description

How is the problem fixed?
As comments in this issue SOC-871, current solution is "do not allow changing its space name", so we disable the re-name space function- space's name now can not edit.

Patch files: SOC-871.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
No
Configuration changes

Configuration changes:
No

Will previous configuration continue to work?
YES
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change
          o apps/portlet-explorer/src/main/webapp/groovy/webui/component/explorer/control/UIActionBar.gtmpl

Is there a performance risk/cost?
N/A
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*


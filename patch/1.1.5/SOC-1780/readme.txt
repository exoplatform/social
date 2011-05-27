Summary

    * Status: Don't update new space after created in My space list
    * CCP Issue: CCP-837, Product Jira Issue: SOC-1780.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Login in root
    * Add new space -> OK
    * Move cursor on My Space -> Don't update new space on menu.See attach file

Fix description

How is the problem fixed?

    * Hard-coded here in UISpaceAddForm.java

?
UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChild(UIWorkingWorkspace.class);
uiWorkingWS.updatePortletsByName("SocialUserToolBarGroupPortlet");
uiWorkingWS.updatePortletsByName("SpacesToolbarPortlet"); // For social demo

Need to remove it, and update all working workspace.

Patch files: SOC-1780.patch

Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* Nothing changes.
Configuration changes

Configuration changes:
* Nothing changes.

Will previous configuration continue to work?
*
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*


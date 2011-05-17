Summary

    * Status: Contact form is "corrupted" when deleted an IM in the middle and the list and adding a new one
    * CCP Issue: N/A, Product Jira Issue: SOC-1554.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
1- Edit your profile
2- Add multiple IMs (4)
3- Delete the 2nd one
4- add a new IM
>> Form is "corrupted" as you can see in the attached screenshot.

It is working only when we delete the latest one
Fix description

How is the problem fixed?
Form corrupted in case one component (IM) that is removed is among others. To fix this bug, we need re-calculate and reset the id for all components after removing one in group.

Patch files:SOC-1554.patch

Tests to perform

Reproduction test
* Steps to reproduce:

    * Go to Newsletter page
    * Create new category and then delete it
    * Open form to create new subscription
    * Put values in all fields
    * Click Save --> show message "Error when perform get list subscriptions."

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*No
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
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*


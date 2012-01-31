Summary

Status: Error when sharing an activity within an empty activity stream
CCP Issue: CCP-xyz, Product Jira Issue: SOC-2350.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?
Error popup is appeared when sharing an activity within an empty activity stream.

Steps to reproduce:
1. Login
2. Create a new space
3. Go to the space
4. Delete all the messages in the activity stream (like user has just created space)
5. Make sure that the following text is shown under the activitystream "Not any updates posted yet." (if this text is not shown the problem won't occur !)
To reach that situation, delete all comments (in some cases you must add a comment and delete it) and press CTRL-F5
6. Try to add a comment
"The target blockId to update is not found : UIActivitiesContainer_9999999999" (where 999999 is variable)==>KO

Fix description
How is the problem fixed?
Error occurs in case the updated by ajax component cannot be found, maybe it has been removed when we remove the last activity. To avoid this bug, we should specify the updated component to make sure that component still existing in any case of removing activities.

Patch files: SOC-2350.patch

Tests to perform
Reproduction test
*

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

Function or ClassName change
No
Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
PM validated

Support Comment
Support validated

QA Feedbacks
*

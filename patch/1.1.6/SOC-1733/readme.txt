Summary

    * Status: Display error messages when click "More" button in My Activities->My status
    * CCP Issue: CCP-927, Product Jira Issue: SOC-1733.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
How to reproduce this bug:
1. Create a new users ('aaa' and 'bbb'), then login to make connection with each others and post activities or add comments.
2. Login by 'root', then delete 'aaa'
3. Stop and restart server
4. Login by 'bbb' and go to his activities page ---> Err
Fix description

Problem analysis
The root cause of this bug comes from a problem in Social when one user is deleted, but his activities, connections, relations (in space), etc still exist in our system. So when all data related to that deleted user are used to get user information, the bug occurs, not only when click "More" button as description of this issue.

How is the problem fixed?

    * We divide into 2 cases:
         1. How to process old data of our system that contains deleted users information
         2. How to process all related data with deleted users after this fix
    * With these 2 cases we decided:
         1. With existing data (deleted users' information included): Using "strike" tag to mark that the user is deleted.
            For example, in case when root connected with demo and demo is deleted so in root's activity stream, there is one activity like this:
            Root Root
            I am now connected with demo
         2. With this fix, from now, when one user is deleted then all related data (connections, activities, etc) will be deleted also.
            To do so we override postDelete(User) method of UserEventListener to listen delete event from organization service.

Patch file: SOC-1733.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No.
Configuration changes

Configuration changes:
* Cf. above.

Will previous configuration continue to work?
* No.
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?
* N/A
Validation (PM/Support/QA)

PM Comment
* Patch approved.

Support Comment
* Patch validated.

QA Feedbacks
*

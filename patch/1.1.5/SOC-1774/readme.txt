Summary

    * Status: Activities of spaces are not well sorted
    * CCP Issue: CCP-955, Product Jira Issue: SOC-1774.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
To reproduce the problem:

Under PLF 3.0.x connect with root and create two spaces(aaa,bbb).
Add more activities in two spaces.
Invite demo to join one space (aaa).
Add some activities with demo account.
Change the system time and restart the server.
Connect as demo and comment some activities (e.g: Jack Miller has joined aaa space).
Reconnect or refresh your browser => activities are not well sorted=>KO
Fix description

How is the problem fixed?
There are some problem in implementing sort method of Collection so then the returned list result is not in the right order of activities. To fix we use compareTo(Long) method of Comparator that more precise than performing minus two posted time value of 2 activities.

Patch files:SOC-1774.patch

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


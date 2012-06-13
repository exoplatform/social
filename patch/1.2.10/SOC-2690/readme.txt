Summary
	* Issue Title: Problem in migration member of space when space has only one member 
	* CCP Issue:  n/a 
	* Product Jira Issue: SOC-2690.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* Problem in migration member of space when space has only one member 

Fix description

Problem analysis
	* In fact that when space has only one member, who is space's manager also then the problem will occured. In truth the memberIds in this case is not created in new data then the member list is Null when we get data from JCR.

How is the problem fixed?
	* To ignore this case we get data from service instead of from JCR, then with that case data has been processed.

Tests to perform

Reproduction test
	* After migration from PLF 3.0.x to PLF 3.5.x, can not open tab "Members" of a space. There are several exception in console.
	* Problem only happens with space having only one member (this member is Manager of space). 

Tests performed at DevLevel
	* Migration test

Tests performed at Support Level
	* Migration test

Tests performed at QA
	* n/a

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* No

Changes in Selenium scripts 
	* No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* No

Configuration changes

Configuration changes:
	* n/a

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
	* Function or ClassName change: No 
    	* Data (template, node type) migration/upgrade: No 

Is there a performance risk/cost?
	* n/a

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*

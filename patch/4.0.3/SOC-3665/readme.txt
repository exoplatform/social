Summary
	Issue title UISpaceNavigationPortlet: Slowness in getLastAccessedSpace
	Product Jira Issue: SOC-3665 and PLF-5402 .
	Complexity: N/A
Proposal
Problem description
What is the problem to fix?
	- UISpaceNavigationPortlet: Slowness in getLastAccessedSpace

Fix description
Problem analysis
	- Retrieves 30 spaces but only show 10 spaces on left navigation.
How is the problem fixed?
	- Load number of space what need to display.
	- Add new API to support ListAccess

Tests to perform
Reproduction test
	- Slowness in Space home page load
Tests performed at DevLevel
	- N/A

Tests performed at Support Level
	- Functional test

Tests performed at QA
	- 

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
	- No

Changes in Selenium scripts 
	- No

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
	* No

Configuration changes
Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Any change in API (name, signature, annotation of a class/method)? 
	- Add new API on SpaceService: ListAccess<Space> getLastAccessedSpace(String remoteId, String appId);

Data (template, node type) upgrade: N/A
Is there a performance risk/cost?

N/A

SOC-2536: Issue with creating a page and space with same name

Problem description
* What is the problem to fix?
	User can create space which has the same name with page at root path. It should be not allowed because they have the same url.

Fix description
* Problem analysis
	Problem is cause by cache issue of portal navigation.

* How is the problem fixed?
	Work around to check if spaces are existing first, then check the remain things that relate to group navigation.

Patch file: https://github.com/exoplatform/social/pull/20

Tests to perform
Reproduction test
Case 1:
- Log in Intranet and create a new page at root path with name Test successfully
- Create a new space with name Test
Expected output: Show message to alert that user cannot create space which has the same name with page at root path because they have the same url

Case 2:
- Log in Intranet and create a new page under Intranet/Home path with name Test1 successfully
- Create a new space with name Test1, click Save
Expected output: No warning message "A page named 'Test1' already exists, please choose a different name for the space." 

Tests performed at DevLevel
*

Tests performed at Support Level
*

Tests performed at QA
*

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
...
Changes in Selenium scripts 
...
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:


Configuration changes
Configuration changes:
*

Will previous configuration continue to work?
*

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: 
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?
...

Validation (PM/Support/QA)
PM Comment
...
Support Comment
...
QA Feedbacks
...

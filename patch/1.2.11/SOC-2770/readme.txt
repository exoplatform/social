SOC-2770: [RestService] Doesn't work when get Space with XML format 

Problem description
* What is the problem to fix?
Get error when trying to get Space with XML format using Rest service.

Fix description
* Problem analysis
Default constructor of class SpaceRest is not defined.
* How is the problem fixed?
Add default constructor of class SpaceRest.

Patch file: https://github.com/exoplatform/social/pull/91

Tests to perform

Reproduction test
- Start PLF 3.5.x server. No need to login.
- Go to: http://localhost:8080/portal/rest/portal/social/spaces/mySpaces/show.xml
Expected result: see XML file, e.g:
<spaceList>
 <moreSpacesUrl>/default/spaces</moreSpacesUrl>
</spaceList>

Tests performed at DevLevel
...
Tests performed at Support Level
...
Tests performed at QA
...

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
	PM validated.
Support Comment
...
QA Feedbacks
...

SOC-2658: Cannot search people by position, skill and gender

Problem description
* What is the problem to fix?
	In Find People, advance search doesn't work.

Fix description
* Problem analysis
The problem is in UIProfileUserSearch#SearchActionListener. When user does not enter a name to find, the program does not set filter name to empty string but pass the default value 'name' to the filter name. So that the query builder builds a wrong query and returns an empty list, because there is no profile having name 'name'.
An other problem not impact to the final result but to the query performance is an incorrect string comparison in IdentityStorage#getIdentitiesByProfileFilter: using != instead of .equals

* How is the problem fixed?
- In UIProfileUserSearch.java, adding new codes to set filter name to empty string if user does not input name
- Use .equals instead of != in IdentityStorage#getIdentitiesByProfileFilter


Patch file: https://github.com/exoplatform/social/pull/37

Tests to perform
* Reproduction test
- Login as John
- Edit profile: current position is "manager", gender is "male" and skill is "management"
- Login as other user
- Go to Find people page
- Input position "manager" and press Enter ==> Estimated result: Show John
- Input skill "management" and press Enter ==> Estimated result: Show John
- Select gender "male" and click search button ==> Estimated result: Show John

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

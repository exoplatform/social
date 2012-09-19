SOC-2665: Activity stream is wrong in some special cases

Problem description
* What is the problem to fix?
Activity stream is wrong in some special cases.

Fix description
* Problem analysis
The XMLTagFilter for activity processor and XMLBalancer sometime not working so well together because it have some duplication in execute the HTML tag escape in two activity Processor.
W3C standard have define some tag not self-closeable and in previous behaviour we think all tag are self-closeable  (e.g <strong />) when browser render <strong />, it didn't think that this is self-close tag and make page display wrong.

* How is the problem fixed?
Remove duplicate execution.
Add some logic to catch up with W3C spec.

Patch file: https://github.com/exoplatform/social/pull/61

Tests to perform
* Reproduction test
Login and create new space
Case 1:
Input special characters
<>
Click share ==> New activity is displayed correctly-OK

Case 2:
Input other activity
<test>
Click Share ==> New activity is displayed wrong: <test>

Case 3:
Input other activity
<a>
Click Share ==> New activity is displayed with blank text

Case 4:
Input activity
<b> or <strong>
Click Share

> Activity stream of space is applied this tag, it becomes bigger
This issue also occurs on activity stream of user
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
	PM validated
Support Comment
...
QA Feedbacks
...

SOC-2566: Problem with @ in comments in exo social

Problem description
* What is the problem to fix?
	When writing a comment containing the character "@" it's interpreted i.e it's replaced by <strike></strike> (in plf-3.0.7)
	In plf-3.0.6 the words following the "@" character are deleted i.e the <strike></strike> tag is interpreted
	Because of this we can not add an e-mail as a comment.

Fix description
* Problem analysis
	The problem of <strike> happend because the activity and comment doesn't allow html <strike> tag. However the previous commit already remove <strike> through the username when identity was deleted. So this problem won't need to fix.
	The mention processor well recognize with some pattern like email. Need to update the MetionProcessor's regex pattern to pass this case.

* How is the problem fixed?
	Update the MetionProcessor's regex pattern to pass the case of '@' sign in an email or in a normal phrase.

Patch file: https://github.com/exoplatform/social/pull/29

Tests to perform
* Reproduction test
- In PLF-3.0.7
1. connect and go to intranet
2. write a comment in "What you are working on ?" field
3. comment this update and put in your comment the symbol "@"==> the words following the "@" symbol are surrounded by <strike></strike>
N.B: if you write something followed by the "@" symbol and comment ==> the "@" symbol is not interpreted 

- In PLF-3.0.6
1. make the steps of the last scenario
2. what we get: the words following the "@" symbol are deleted and when we write something followed by the "@" symbol the comment stays as it i.e the "@" symbol is not interpreted

Tests performed at DevLevel
*

Tests performed at Support Level
*

Tests performed at QA
*

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
	None
Changes in Selenium scripts 
	None
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
	None

Configuration changes
Configuration changes:
*	None

Will previous configuration continue to work?
*	Yes

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
	Support validated
QA Feedbacks
...

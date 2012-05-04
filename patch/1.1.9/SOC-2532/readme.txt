Summary
Display problem of special characters in space name after clicking the "Like" button in the activity stream 
CCP Issue:  CCP-1220
Product Jira Issue: SOC-2532.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?
Case 1: Arabic
- select arabic language
- add new space with arabic name
- access the home page of new space
- click Like button in the activity stream
Problem: The characters in the space name display not well ( look pictures after and before clicking on "like" button )
Case 2: French
- Select French
- Create a new space, named "espace testé à la plage"
- Access the home page of this space
- Click Like
Problem: the space name in the message "was created by" becomes

espace test&eacute; &agrave; la plage

- Click Unlike

espace test&amp;eacute; &amp;agrave; la plage

Fix description
Problem analysis
Cause:

After gets Activity from JCR, encode processor will do encode Activity's title and body.
2. When user likes existing activity, then updated it. Storage will push encoded Activity's title and body.
How is the problem fixed?

When update the activity, not re-save the title and the body of the activity which are re encoded.
Tests to perform
Reproduction test

cf. above
Tests performed at DevLevel

cf. above
Tests performed at Support Level

cf. above
Tests performed at QA
*

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

None
Changes in Selenium scripts 

None
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

No
Configuration changes
Configuration changes:

No
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: : None
Data (template, node type) migration/upgrade: 
Is there a performance risk/cost?

No
Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

N/A

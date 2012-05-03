Summary
Add FAQPortlet to a space then its link displays 'FA' that is abnormal - it normally was 'FAQ' 
CCP Issue:  CCP-xyz 
Product Jira Issue: SOC-1574.
Complexity: N/A
Proposal
 

Problem description
What is the problem to fix?
   The function ==getDisplayAppName== missing case for get display application name.
Steps to reproduce:

Login /portal/private/intranet
2. Create a space
3. Add application FAQ Portlet to the space
4. The problem is: the link of the portlet displays (in the left navigation) as 'FA'. It should have been 'FAQ'. 
Fix description
Problem analysis

 We have two cases for display application name:
   + The application name contain word " portlet".
   + The application name contain word "portlet".
  So, the function  *getDisplayAppName* will remove this word for display application name.
 After fix: The function  *getDisplayAppName* only run for fist case => error, it will remove last character of application name. (FAQ becomes  FA)
    int len = appDisplayName.length() - 1;
   if (appDisplayName.toLowerCase().endsWith("portlet")) return appDisplayName.substring(0, len - 7);
How is the problem fixed?

 Fix: Change logic function *getDisplayAppName*
    + Remove word "portlet" and if it contain " " last character, remove also this character by function trim() of String.
   if (appDisplayName.toLowerCase().endsWith(key)) {
     return appDisplayName.substring(0, appDisplayName.length() - key.length()).trim();
    }
Tests to perform
Reproduction test
*

Tests performed at DevLevel

No
Tests performed at Support Level

No
Tests performed at QA

No
Changes in Test Referential
Changes in SNIFF/FUNC/REG tests

Not change
Changes in Selenium scripts 

Not change
Documentation changes
Documentation (User/Admin/Dev/Ref) changes:

 Not change
Configuration changes
Configuration changes:

Not change
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Not change
Is there a performance risk/cost?

 No


Validation (PM/Support/QA)
PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

N/A

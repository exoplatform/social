Summary

    * Status: Unable to share links when clicking attach button
    * CCP Issue: CCP-849, Product Jira Issue: SOC-1581.
    * Fixes also: SOC-1585.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. Access home page of a Space
   2. Enter an http or https link in attach link form
          * particular http link, like http://www.google.com/language_tools?hl=en
          * any https link, for example https://mail.google.com/
   3. Click Attach
      ==>The form is reset and the link is not shared

Fix description

How is the problem fixed?
There are some problems have already solved:
    * Links start with https: we change the condition so that in both cases, http and https, the url starts with the right prefix 'http://' and 'https://'.
    * Add a Util to check the input links. 
    * All the links that are in the right form will be attached whether they are parsed or not (caused by some special reasons like: links are blocked, or are not existing longer, etc). Otherwise, a message will be showed to user to re-input the link.

Patch file: SOC-1581.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* UtilTest

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
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*

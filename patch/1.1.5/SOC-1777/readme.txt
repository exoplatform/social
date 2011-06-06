Summary

    * Status: XSS weakness in Profile
    * CCP Issue: N?A, Product Jira Issue: SOC-1777.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
It is possible to add inject code in Social for a basic user using the profile form.

Steps to reproduce :

    * Go on your Social profile page
    * Edit the Experiences section
    * in the Job Details field, entre the following value : <script>alert("XSS !");</script>
    * Save
      ---> no validation, and the alert is executed.

Malicious code can be injected, which can for example allow to collect data to connect with another account.
We must prevent an user to inject code, so we need validation on all fields.
Fix description

How is the problem fixed?
To prevent user from injecting malicious code, we using org.apache.commons.lang.StringEscapeUtils to escape html in Profile Section fields.

Patch files:SOC-1777.patch

Tests to perform

Reproduction test
*

Tests performed at DevLevel
Done

Tests performed at QA/Support Level
*\ No
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
*
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*


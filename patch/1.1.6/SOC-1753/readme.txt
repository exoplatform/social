Summary

    * Status: a Space Home page's URL and a regular page must not have the same URL
    * CCP Issue: CCP-938, Product Jira Issue: SOC-1753.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

How to reproduce on social:
1)Create a new page at the root of the tree "/space1".The URL of this page looks like "http://localhost:8080/socialdemo/private/classic/space1"
2)Create a new space called "space1".The home page of this space has the same URL as the page created in step 1 !
3)Attempt to access the new space home page by clicking spaces>space1
You are redirected to the page create in step 1 rather than the space home page ==>KO

We need to:

    * have better names for spaces for example be sure we have unique names and avoid conflicts (for example /spaces/space1 instead of space1)
    * check the validity of name at the creation of the space

Fix description

How is the problem fixed?

Instead of checking the existing of space's name only, Social side now performing check the existed URL in commons (not only with spaces but also others - such as pages or Dashboard tabs).

Patch files:SOC-1753.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
*

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
*
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*


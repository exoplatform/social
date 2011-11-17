Summary

    Status: Platform Source distribution
    CCP Issue: N/A, Product Jira Issue: PLF-1747.
    Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
   Platform source distribution. Create a Zip archive with all projects sources.

Fix description

How is the problem fixed?
   Add a maven plugin to generate the Zip automatically during the release.

Patch information:
Patch files: KER-176.patch

Tests to perform

Reproduction test
    none

Tests performed at DevLevel
    mvn clean install -Prelease to make sure the project still builds successfully

Tests performed at QA/Support Level
    none

Documentation changes

Documentation changes:
    none

Configuration changes

Configuration changes:
    none

Will previous configuration continue to work?
    yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
    none

Is there a performance risk/cost?
    none

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*


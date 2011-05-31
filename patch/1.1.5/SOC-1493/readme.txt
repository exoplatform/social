Summary

    * Status: UIDisplayProfileList: don't load profile when not needed
    * CCP Issue: N/A, Product Jira Issue: SOC-1493.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Loading profiles when getting identities is expensive, only load profiles when it is needed.

Fix description

How is the problem fixed?

    * Only load profiles when it is needed, use boolean value on the method: IdentityManager#getOrCreateIdentity(String, String, boolean) to indicate if profile is required to be loaded or reloaded.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:SOC-1493.patch

Tests to perform

Reproduction test

    * N/A

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * No changes.

Configuration changes

Configuration changes:

    * No.

Will previous configuration continue to work?

    * Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?

    * No performance risk.

Validation (PM/Support/QA)

PM Comment

    * PL review: patch validated

Support Comment

    * Support review: patch validated

QA Feedbacks
*


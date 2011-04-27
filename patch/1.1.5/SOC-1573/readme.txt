Summary

    * Status: Activities in Spaces activity stream are not well sorted
    * CCP Issue: CCP-850, Product Jira Issue: SOC-1573.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Activities Stream are not well sorted when making some more actions (like, comment, etc) on activities.
To reproduce:

    * Create a new Space.
    * Access into Home of Space to post activities on Space. Wait some minutes (to make the posted time in different values) and then post some more activities.
    * Take action 'like' on the oldest (or on the other activities that not at the top) activity of these Space's activities and refresh the Browser. -> Error.

Problem analysis
  This problem is not only in Space Activity Stream as description but in all other Activity Streams also. 
  We sorted the returned result in JCR Query by field named "exo:updatedTimestamp" instead of "exo:postedTime" when getting Activities.

Fix description
* Replace the field that is used to sort in query in ActivityStorage.java

Patch file: SOC-1573.patch

Tests to perform

Reproduction test

    * Create a new Space.
    * Access into Home of Space to post activities on Space. Wait some minutes (to make the posted time in different values) and then post some more activities.
    * Take action 'like' on the oldest (or on the other activities that not at the top) activity of these Space's activities and refresh the Browser -> Wrong order.

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No

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

    * Function or ClassName change: No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PM review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

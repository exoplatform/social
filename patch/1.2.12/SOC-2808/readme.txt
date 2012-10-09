SOC-2808: Rest Service GET {identityId}.{format} returns wrong activity

Problem description
* What is the problem to fix?
The Rest Service described on this page http://docs.exoplatform.com/PLF35/topic/org.exoplatform.doc.35/SOCref.DevelopersReferences.RestService_APIs_v1alpha3.ActivityStreamResources.html returns wrong data.

Fix description
* Problem analysis
Function getNumberOfNewerOnUserActivities doesn't return correct activity by ID.
* How is the problem fixed?
Use new function getActivitiesOfIdentityQuery to return correct activity by ID.

Tests to perform
* Reproduction test
Steps to reproduce in PLF 3.5.5 Tomcat: 
- Login as John 
- Create 5 activities in activity stream, for example, from "activity 1" to "activity 5".
- Use Firebug to get ID of the newest activity ("activity 5"), for example: ActivityContextBox20ebfc9e7f00010101c0f3461d1492eb ==> ID = 20ebfc9e7f00010101c0f3461d1492eb
- Open RESTClient (Firefox addons)
 Method: GET
 URL: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/20ebfc9e7f00010101c0f3461d1492eb.json
(URL formula: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity/[ID].json)
 Click SEND
- In Reponse Body tab returned, get Identity ID of this activity, for example, Identity ID = 1cb95fa87f0001010099f5b2598e4fcf
- In RESTClient:
Method: GET
URL: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity_stream/1cb95fa87f0001010099f5b2598e4fcf.json?max_id=20ebfc9e7f00010101c0f3461d1492eb
(URL formula: http://localhost:8080/rest/private/api/social/v1-alpha3/portal/activity_stream/[IDENTITY_ID].json?max_id=[ID])
- Click SEND
Problem: It returns some very old activity stream ==> NOK 
Expected result: It returns the 2nd activity in Activity Stream (i.e: "activity 4").

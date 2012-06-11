SOC-2630: Wrong behavior when receiving the invitation from a space after request sent

What is the problem to fix?
- Wrong behavior when receiving the invitation from a space after request sent

Problem analysis
- When inviting new user to a default space (need validation for request to join), we are missing the conditional that this user must be in the pending list first. Then space manager can add this user to a space.

How is the problem fixed?
- Add one more condition to check that invited user is in pending list.

Reproduction test
- Login as John on browser 1
- Go to My space page
- Add new space "test" with visibility is Visible and Registration mode is Validation
- John go to Space settings ==> Member tab
- Login as Mary on browser 2
- Go to My spaces page
- Click on All spaces tab
- On browser 2, Mary click on "Request to join" button
- On browser 1, John input mary into text box and click on Invite icon
Actual result: Mary becomes member of space "test"
Expected result: Mary should be shown in pending list



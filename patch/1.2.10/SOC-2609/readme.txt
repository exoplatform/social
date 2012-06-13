SOC-2609: Wrong default focus on My connections page

What is the problem to fix?
Default focus on My connections page is on "Everyone" list. It should be focus on "My connections" list.

Problem analysis
- The URL of My Connections page is refered to the All People page.

How is the problem fixed?
- Change the URL of My Connections page refers to the My Connections page valid URL in the following pages: People Directory, People Toolbar and User Profile.

Reproduction test
1. Login
2. Hover the mouse over the user name
3. Click on My Profile
4. Click on My Connections tab on the left pane
Result: Focus on Everyone list
Expected result: When click on My connections tab on the left pane, it should be focus on "My connections" list.

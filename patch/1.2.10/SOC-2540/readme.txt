SOC-2540: User viewer list gadget does not display latest connection

What is the problem to fix?
- The last added friend on the user's connections is not shown in the user's friends list on the Viewer Friend Gadget.

Problem analysis
- The upper limit index in ExoPeopleService#getPeople(Set<UserId>, GroupId, CollectionOptions, Set<String>, SecurityToken) is being set to wrong value (totalSize - 1):
toIndex = Math.min(toIndex, totalSize - 1) > 0 ? Math.min(toIndex, totalSize - 1) : 0;

How is the problem fixed?
- Set the upper limit index in ExoPeopleService#getPeople(Set<UserId>, GroupId, CollectionOptions, Set<String>, SecurityToken) to the appropriate value:
toIndex = totalSize < toIndex ? totalSize : toIndex;
toIndex = toIndex < fromIndex ? fromIndex : toIndex;

Reproduction test
Steps to check on Social standalone:
- Login as Mary
- Go to People > Connections
- Send invitations to root and demo.
- Login as demo and accept invitation from Mary.
- Logout demo.
- Login as root and accept invitation from Mary
- Go to Site Editor > Add new page
- Create new page "test" and add Application Registry to this page
- On Application Registry portlet, click Import Applications
- Logout root
- Login as Mary
- Go to Dashboard
- Add "Viewer Friends" Gadget
Expected result :
All Mary's connections should be shown(root and demo)

Tests performed at DevLevel
Steps to check on Social standalone:
- Login as Mary
- Go to People > Connections
- Send invitations to root and demo.
- Login as demo and accept invitation from Mary.
- Logout demo.
- Login as root and accept invitation from Mary
- Go to Site Editor > Add new page
- Create new page "test" and add Application Registry to this page
- On Application Registry portlet, click Import Applications
- Logout root
- Login as Mary
- Go to Dashboard
- Add "Viewer Friends" Gadget
 All Mary's connections should be shown(root and demo)

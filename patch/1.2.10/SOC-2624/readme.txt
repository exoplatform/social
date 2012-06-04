SOC-2624: Space navigation - Wrong level of added node by Add node button 

What is the problem to fix?
	Cannot add child node under current selected node.

Fix description
	Problem analysis
		As we click Add Node to add a new node into the selected navigation then any cases the root node will be parent node so added node will be placed under root node.

	How is the problem fixed?
		Each time we add a new node to a specific node, that selected node will be parent node. To do that we replace the old code that point to root node by a new code that specify and point to current selected node.

Tests to perform
	Reproduction test
		- Login
		- Go to My spaces page
		- Add new space "john1" for example
		- Click on Space setting and select Navigation
		- Click on a node "Discussion" for example to select this node
		- Click on Add node button at the bottom
		- Input name of new node and click save
			+ Result: New node has the same level with node "Discussion", see SpaceNavigation.png
			+ Expected result: New node is the sub-node of node "Discussion"
		This issue does not occurs on exo gtn 3.2.4

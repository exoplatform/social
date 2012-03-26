SOC-2328: [ACCESS] ACHECKER-106 All onmouseout event handlers have an associated onblur event handler.
Requirement
	All onmouseout event handlers have an associated onblur event handler.
Error
	script not keyboard accessible - onmouseout missing onblur.
Short Description
	Any element that contains an onmouseout attribute must also contain an onblur attribute.
How To Repair
	Add an onblur handler to your script that performs the same function as the onmouseout handler.
More info
	http://achecker.ca/checker/suggestion.php?id=106

SOC-2337: [ACCESS] ACHECKER-107 All onmouseover event handlers have an associated onfocus event handler.
Requirement
	All onmouseover event handlers have an associated onfocus event handler.
Error
	onmouseover event handler missing onfocus event handler.
Short Description
	Any element that contains an onmouseover attribute must also contain an onfocus attribute.
How To Repair
	Add an onfocus handler to your script that performs the same function as the onmouseover handler.
More info
	http://achecker.ca/checker/suggestion.php?id=107



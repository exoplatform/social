/*jslint browser:true, nomen:false, evil:true */
/*global alert: false, confirm: false, console: false, prompt: false, window: true */
(function () {
    //We set default configuration values
    var configuration = {
        //Do not include a trailing /
        serverURL: "http://localhost:8080",

        //start and end by a /
        spaceServicePath: "/rest/private/spaces/",

        // This is default values work with eXo Platform 3.0.x
        // This can be configured using space.setContainerName and space.setPortalName
        containerName: "portal",
        portalName: "intranet",

        linkElId: "exoSpacesLink",
        spaceInfoTmpl: '<div style="height:200px;overflow:hidden;width:220px;border:medium none;"><a href="javascript:void(0)" rel="close" style="float:right;">Close</a><br /><%= iframe %></div>'
    },
    templateRenderer;
    
    function getIframe(spaceName, description) {
        return '<iframe  scrolling="no" height="180" frameborder="0" width="220" src="' + configuration.serverURL + configuration.spaceServicePath + configuration.containerName + "/space_info?spaceName=" + encodeURIComponent(spaceName) + '&portalName=' + encodeURIComponent(configuration.portalName) + '&description=' + encodeURIComponent(description) + '"></iframe>'
    }
    
    function createSpaceBox(container, spaceName, description) {
        if (typeof container === "string") {
            container = document.getElementById(container);
        }
        container.innerHTML = getIframe(spaceName, description);
    }
    
    function getTemplateGenerator(template) {
        //from ejohn http://ejohn.org/blog/javascript-micro-templating/ 
        //MIT Licence
        return new Function("obj",
                "var p=[],print=function(){p.push.apply(p,arguments);};" +

                // Introduce the data as local variables using with(){}
                "with(obj){p.push('" +

                // Convert the template into pure JavaScript
                template
                  .replace(/[\r\t\n]/g, " ")
                  .split("<%").join("\t")
                  .replace(/((^|%>)[^\t]*)'/g, "$1\r")
                  .replace(/\t=(.*?)%>/g, "',$1,'")
                  .split("\t").join("');")
                  .split("%>").join("p.push('")
                  .split("\r").join("\\'") + "');}return p.join('');");
    }
    
    function hideInfo() {
        if (infoEl) {
            infoEl.style.display = "none";
        }
    }
    
    
    function bind(el, eventName, fn) {
        el = (typeof(el) === 'string') ? document.getElementById.get(el) : el;
        if (window.addEventListener) {  
            el.addEventListener(eventName, fn, false);
        }
        if (window.attachEvent) {
            el.attachEvent('on' + eventName, fn);
        }
    }
    
    function showInfo(event, link, spaceName, description) {
        var html = [],
            infoEl = document.getElementById("espInfoBox_" + encodeURIComponent(spaceName)),
            xCoordinate = event.clientX,
            yCoordinate = event.clientY,
            isIE = document.all?true:false;
    
        if (!infoEl) {
            
            if (!templateRenderer) {
                templateRenderer = getTemplateGenerator(configuration.spaceInfoTmpl);
            }
            
            infoEl = document.createElement('div');
            infoEl.className = "espInfoBox";
            infoEl.id = "espInfoBox_" + encodeURIComponent(spaceName);
            
            infoEl.style["z-index"] = "10001";
            
            

            if(isIE) {
                xCoordinate = xCoordinate + document.body.scrollLeft;
                yCoordinate = yCoordinate + document.body.scrollTop;
            }
            
            infoEl.style.top = yCoordinate + "px";
            infoEl.style.left = xCoordinate + "px";
            
            infoEl.innerHTML = templateRenderer({
                iframe: getIframe(spaceName, description)
            });
            document.body.appendChild(infoEl);
            
            els = infoEl.getElementsByTagName("*");
            for (i = 0, len = els.length; i < len; i += 1) {
                if (els[i].getAttribute("rel") === "close") {
                    bind(els[i], "click", function() {
                        infoEl.style.display = "none";
                    });
                }
            }
        }
        infoEl.style.display = "block";
    }
    
    
    function createPopup(link, spaceName, description) {
        var stylesheetLink, scriptEl;
        //Insert our stylesheet before our script
        if (!document.getElementById("exoSpacesStylesheet")) {
            stylesheetLink = document.createElement('link');
            stylesheetLink.type = 'text/css';
            stylesheetLink.rel = 'stylesheet';
            stylesheetLink.href = configuration.serverURL + '/socialWidgetResources/skin/css/social-extras-widget-resource.css';
            scriptEl = document.getElementsByTagName('script')[0]; 
            scriptEl.parentNode.insertBefore(stylesheetLink, scriptEl);
        }
        
        if (typeof link === "string") {
            link = document.getElementById(link);
        }
        
        
        if (link.tagName === "A") {
            link.href = configuration.serverURL + configuration.spaceServicePath + configuration.containerName + "/go_to_space?spaceName=" + encodeURIComponent(spaceName) + '&portalName=' + encodeURIComponent(configuration.portalName) + '&description=' + encodeURIComponent(description);
        }
        
        link.onclick = function(e) {
            
            if (!e) { 
                e = window.event;
            }
            
            showInfo(e, link, spaceName, description);
            
            e.cancelBubble = true;
            if (e.stopPropagation) {
                e.stopPropagation();
            }
            return false;
        };
    }
    
    function initialize() {
        var i,
            _esp = window["_esp"] || [],
            param;
        
        //If we have some configuration provided we set it
        if (_esp) {
            for (i = 0; i < _esp.length; i += 1) {
                param = _esp[i];
                if (param[0] === "createSpaceBox") {
                    createSpaceBox(param[1], param[2], param[3]);
                } else if (param[0] === "createPopup") {
                    createPopup(param[1], param[2], param[3]);
                } else {
                    configuration[param[0]] = param[1];
                }
                
            }
        }
    }

    //We expose the space object outside of this scope.
    var spaces = window.spaces = window.spaces || {};
    spaces.createSpaceBox = createSpaceBox;
    spaces.createPopup = createPopup;
    spaces.setServerURL = function(value) {
        configuration["serverURL"] = value;
    };
    spaces.setSpaceServicePath = function(value) {
        configuration["spaceServicePath"] = value;    
    };
    spaces.setPortalName = function(value) {
        configuration["portalName"] = value;    
    };
    spaces.setContainerName = function(value) {
        configuration["containerName"] = value;    
    };
    
    initialize();
}());

(function($){
function initSearchAdmin() {
  
    $.getJSON("/rest/search/registry", function(registry){
      var row_template =
        "<tr>" +
          "<td>%{displayName}</td>" +
          "<td>%{description}</td>" +
          "<td class='center'>" +
            "<input type='button' class='btn btn-mini contentType' id='%{id}' name='Enable' value='%{key}'>" +
          "</td>" +
        "</tr>";

      var connectors = registry[0];
      var searchTypes = registry[1];
      $.each(connectors, function(searchType, connector){
        $("#searchAdmin table").append(row_template.replace(/%{id}/g, connector.searchType).replace(/%{key}/g,eXo.ecm.WCMUtils.getBundle('SearchAdmin.action.Enable', eXo.env.portal.language))
                                                  .replace(/%{displayName}/g, eXo.ecm.WCMUtils.getBundle("SearchAdmin.type." + connector.displayName , eXo.env.portal.language))
                                                  .replace(/%{description}/g,  eXo.ecm.WCMUtils.getBundle("SearchAdmin.type." + connector.displayName + ".description", eXo.env.portal.language)));
      });

      $.each(searchTypes, function(i, type){
        $(".contentType#"+type).val(eXo.ecm.WCMUtils.getBundle("SearchAdmin.action.Disable", eXo.env.portal.language));
        $(".contentType#"+type).attr("name","Disable");

        //$(".ContentType#"+type).next().attr("disabled", false);
      });
    });

    $('body').on('click', '.contentType', function() {
      if("Enable"==$(this).attr("name")) {
        $(this).attr("name","Disable");
        $(this).val(eXo.ecm.WCMUtils.getBundle("SearchAdmin.action.Disable", eXo.env.portal.language));
        //$(this).next().attr("disabled", false);
      } else {
        $(this).attr("name","Enable");
        $(this).val(eXo.ecm.WCMUtils.getBundle("SearchAdmin.action.Enable", eXo.env.portal.language));
        //$(this).next().attr("disabled", true);
      }

      var enabledTypes = [];
      $.each($(".contentType"), function(){
        if("Disable"== this.name) enabledTypes.push(this.id);
      });

      $.ajax({
        url: '/rest/search/enabled-searchtypes/' + enabledTypes,
        method: 'POST',
        data: {
          searchTypes: enabledTypes.join(",")
        },
        complete: function (data) {
          if ("ok" == data.responseText) {
            console.log("Search setting has been saved succesfully.");
          } else {
            alert("Problem occurred when saving your setting: " + data.responseText);
          }
        }
      });
    });
	}


  initSearchAdmin();

})($);

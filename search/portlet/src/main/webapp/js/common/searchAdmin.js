(function($){
function initSearchAdmin() {

    const i18NData = {};
    const lang = typeof eXo !== 'undefined' ? eXo.env.portal.language : 'en';
    const url = `${eXo.env.portal.context}/${eXo.env.portal.rest}/i18n/bundle/locale.portlet.searchadministration.searchadministration-${lang}.json`;
    fetch(url, {
      method: 'get',
      credentials: 'include'
    })
      .then(resp => resp && resp.ok && resp.json())
      .then(data => data && Object.assign(i18NData, data))
      .then(() => {
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
            $("#searchAdmin table").append(row_template.replace(/%{id}/g, connector.searchType).replace(/%{key}/g,i18NData['SearchAdmin.action.Enable'])
                                                      .replace(/%{displayName}/g, i18NData["SearchAdmin.type." + connector.displayName ])
                                                      .replace(/%{description}/g,  i18NData["SearchAdmin.type." + connector.displayName + ".description"]));
          });

          $.each(searchTypes, function(i, type){
            $(".contentType#"+type).val(i18NData["SearchAdmin.action.Disable"]);
            $(".contentType#"+type).attr("name","Disable");

            //$(".ContentType#"+type).next().attr("disabled", false);
          });
        });
      });

    $('body').on('click', '.contentType', function() {
      if("Enable"==$(this).attr("name")) {
        $(this).attr("name","Disable");
        $(this).val(i18NData["SearchAdmin.action.Disable"]);
        //$(this).next().attr("disabled", false);
      } else {
        $(this).attr("name","Enable");
        $(this).val(i18NData["SearchAdmin.action.Enable"]);
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

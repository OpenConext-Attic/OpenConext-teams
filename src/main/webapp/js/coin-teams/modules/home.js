/*
 * Copyright 2011 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

COIN.MODULES.Home = function(sandbox) {

  var searchInputSelector = 'input[type=text][name=teamSearch]';
  var teamsTableSelector = "table.team-table";
  var teamsSearchResultSelector = "table.team-search-result";

  var module = {
    init: function() {
      // Handle search queries
      $("form#searchTeamsForm").submit(function(e) {
        e.preventDefault();
        library.showSearchThrobbler();
        $.post("findPublicTeams.json",
          $(searchInputSelector).parents('form:first').serialize(),
          function(data) {
            library.displaySearchResults(data);
            library.hideSearchThrobbler();
          }
        );
      });

      // by default hide the table for search result
      $(teamsSearchResultSelector).hide();
    },

    destroy: function() {
    }
  };

  // Private library (through closure)
  var library = {
    showSearchThrobbler: function() {
      $(teamsTableSelector).hide();
      $(teamsSearchResultSelector).hide();

      $("<img/>")
        .attr("src", "media/ajax-loader.gif")
        .attr("id", "searchThrobbler")
        .attr("style", "display:block; margin: auto; padding: 75px 0 100px 0")
      .insertAfter($(teamsTableSelector));
    },

    hideSearchThrobbler: function() {
      $("img#searchThrobbler").remove();
      $(teamsSearchResultSelector).show();
    },

    clearResults: function() {
      $(teamsSearchResultSelector + " tbody tr").remove();
      $(".pagination").remove();
    },

    displaySearchResults: function(results) {
      this.clearResults();
      $(results["teams"]).each(function(i) {
        $(teamsSearchResultSelector).append("<tr><td><a href=\"detailteam.shtml?view="+view+"&team=" + encodeURIComponent(this['id']) + "\">" + library.htmlEncode(this['name']) + "</a></td><td>" + library.htmlEncode((this['description'] || "")) + "</td></tr>");
        sandbox.fixTableLayout($(teamsSearchResultSelector));
      });
    },

    htmlEncode: function(value) {
      if (value) {
        return $('<div />').text(value).html();
      } else {
        return '';
      }
    }
  };

  return module;
};
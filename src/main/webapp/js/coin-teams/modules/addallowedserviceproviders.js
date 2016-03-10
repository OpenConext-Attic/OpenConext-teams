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

COIN.MODULES.AddAllowedServiceProviders = function(sandbox) {
  // Public interface
  var serviceProviders = [];
  var module = {
    init: function() {
      var url = $("#search-service-providers-container").data("url");
      $.get(url, function(data) {
        $.merge(serviceProviders, data.map(function(val) {
          val.displayNameEn = $.trim(val.displayNameEn);
          val.displayNameNl = $.trim(val.displayNameNl);
          return val;
        }));
      });

      $('#search-service-providers').typeahead(
        {
          hint: false,
          highlight: true,
          minLength: 1
        },
        {
          name: 'sps',
          displayKey: 'displayNameEn',
          source: library.substringMatcher(serviceProviders)
        });

      $('#search-service-providers').on("typeahead:selected", function(e, selected) {
        var element = $("#selected-service-providers .hidden-service-provider").clone();
        element.attr("class", "new-service-provider").find("span").text(selected.displayNameEn);
        element.find("input").val(selected.entityId);
        $("#selected-service-providers").append(element);

        $(this).typeahead('val', '');
      });

      $(document).on("click", "a.delete-service-provider", function(e) {
        console.log(e);
        $(this).parents("li").remove();
      });

      $(document).on("click", 'input[name=cancel-add-sp]', function(e) {
        e.preventDefault();
        var team = $('input[name=team]').val();
        window.location.href = '../../detailteam.shtml?team=' + encodeURIComponent(team);

      });


    },

    destroy: function() {

    }
  };

  // Private library (through closure)
  var library = {
    substringMatcher: function(serviceProviders) {
      return function findMatches(q, cb) {
        var matches, substrRegex;

        // an array that will be populated with substring matches
        matches = [];

        // regex used to determine if a string contains the substring `q`
        substrRegex = new RegExp(q, 'i');

        // iterate through the pool of strings and for any string that
        // contains the substring `q`, add it to the `matches` array
        $.each(serviceProviders, function(i, serviceProvider) {
          if (substrRegex.test(serviceProvider.displayNameEn)) {
            // the typeahead jQuery plugin expects suggestions to a
            // JavaScript object, refer to typeahead docs for more info
            matches.push(serviceProvider);
          }
        });

        cb(matches);
      };
    }
  };

  // Return the public interface
  return module;
};

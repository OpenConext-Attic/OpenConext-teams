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

COIN.MODULES.Jointeam = function(sandbox) {
  // Public interface
  var module = {
    init: function() {
      $(document).on("click", 'input[name=cancelJoinTeam],.close a', function(e) {
        e.preventDefault();
        var teamId = $('input[name=team]').val();
        if (teamId) {
          sandbox.redirectBrowserTo('detailteam.shtml?team=' + encodeURIComponent(teamId));
        } else {
          sandbox.redirectBrowserTo('home.shtml?teams=my');
        }
      });

      $(document).on("change", 'input[name=consent]', function() {
        library.toggleDisable($('input[name=joinTeam]'));
      });
    },

    destroy: function() {

    }
  };

  // Private library (through closure)
  var library = {
    toggleDisable: function(el) {
      if (el instanceof jQuery) {
        if (!el.attr('disabled')) {
          el.attr('disabled', true);
          el.removeClass('button').addClass('button-disabled');
        } else {
          el.removeAttr('disabled');
          el.removeClass('button-disabled').addClass('button');
        }
      }
    }
  };

  // Return the public interface
  return module;
};

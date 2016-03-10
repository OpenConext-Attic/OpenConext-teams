/*
 * Copyright 2012 SURFnet bv, The Netherlands
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

COIN.MODULES.Addteam = function (sandbox) {
  // Public interface
  var module = {
    init:function () {
      var admin2MessageContainer = $('#admin2messagecontainer');
      var admin2LanguageContainer = $('#admin2languagecontainer');

      if ($('#admin2').val() === '') {
        admin2MessageContainer.addClass('hide');
        admin2LanguageContainer.addClass('hide');
      }

      // Clicked [ Cancel ]
      $(document).on("click", 'input[name=cancelCreateTeam]', function (e) {
        e.preventDefault();
        sandbox.redirectBrowserTo('home.shtml?teams=my');
      });

      // Clicked [ Consent ]
      $(document).on("change", 'input[name=consent]', function (e) {
        library.toggleDisable($('input[name=createTeam]'));
      });

      $(document).on("focus", 'input[id=admin2]', function (e) {
        e.preventDefault();
        if (admin2MessageContainer.hasClass('hide')) {
          admin2MessageContainer.removeClass('hide');
        }
        if (admin2LanguageContainer.hasClass('hide')) {
          admin2LanguageContainer.removeClass('hide');
        }
      });

      $('#TeamName').focus();
    },

    destroy:function () {
    }
  };

  // Private library (through closure)
  var library = {
    toggleDisable:function (el) {
      if (el instanceof jQuery) {
        if (!el.attr('disabled')) {
          el.removeClass('button').addClass('button-disabled');
          el.attr('disabled', true);
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

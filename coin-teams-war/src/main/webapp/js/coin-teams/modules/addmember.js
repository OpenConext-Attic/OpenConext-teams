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

COIN.MODULES.Addmember = function (sandbox) {
  // Public interface
  var module = {
    init:function () {
      sandbox.addPlaceholderSupport();

      // Clicked [ Cancel ]
      $('input[name=cancelAddMember]').live('click', function (e) {
        e.preventDefault();
        var team = $('input[name=team]').val();
        var view = $('input[name=view]').val();
        sandbox.redirectBrowserTo('detailteam.shtml?team=' + escape(team) + '&view=' + view);
      });

      $('#csvFileTrigger,#filePath').live('click', function(e) {
        e.preventDefault();
        $('#csvFile').click();
      });
      $('#csvFile').addClass('transparent');
      $('#csvFile').live('change', function (e) {
        var fileName = $(this).val();
        $('#filePath').text(fileName);
      });
    },

    destroy:function () {

    }
  };

  // Private library (through closure)
  var library = {

  };

  // Return the public interface
  return module;
};
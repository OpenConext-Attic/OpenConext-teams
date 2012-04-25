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
      $('input[name=cancelAddMember],.close a').live('click', function (e) {
        e.preventDefault();
        var team = $('input[name=team]').val();
        var view = $('input[name=view]').val();
        sandbox.redirectBrowserTo('detailteam.shtml?team=' + escape(team) + '&view=' + view);
      });

      var fileUploadBox = $('#fileUploadBox');
      var fileUploader = fileUploadBox.find('input[type=file]');

      // native IE7 (not IE9 acting as IE7) needs the real file input element
      if (!library.isVeryOldMsie()) {
        fileUploader.addClass('transparent');
      }

      // MSIE clears the file upload if the click event was triggered automatically
      if (library.isMsie() === false) {
        library.clickBrowseButton(fileUploadBox, fileUploader);
      }

      // native IE7 (not IE9 acting as IE7) needs the real file input element
      if (!library.isVeryOldMsie()) {
        library.showFileNameToUpload(fileUploadBox, fileUploader);
      }
    },

    destroy:function () {

    }
  };

  // Private library (through closure)
  var library = {
    isVeryOldMsie:function () {
      return ($.browser.msie && $.browser.version.slice(0, 1) <= 7);
    },
    isMsie:function () {
      return ($.browser.msie === true);
    },
    clickBrowseButton : function (parentContainer, fileUploader) {
      parentContainer.find('label, i').live('click', function (e) {
        e.preventDefault();
        fileUploader.focus();
        fileUploader.click();
        fileUploader.blur();
      });
    },
    showFileNameToUpload : function (parentContainer, fileUploader) {
      fileUploader.live($.browser.msie ? 'blur' : 'change', function (e) {
        var fileNameTag = parentContainer.find('i');
        // some browsers add C:\fakepath\ before the file name to obfuscate the file system location
        var filePath = $(this).val().replace("C:\\fakepath\\", "");
        if (fileNameTag.length > 0) {
          fileNameTag.text(filePath);
        } else {
          fileNameTag = '<i>' + filePath + '</i>';
          parentContainer.append(fileNameTag).show();
        }
      });
    }
  };

  // Return the public interface
  return module;
};
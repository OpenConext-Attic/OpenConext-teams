COIN.MODULES.Acceptinvitation = function(sandbox) {
  // Public interface
  var module = {
    init: function() {
      // Clicked [ Cancel ]
      $('input[name=cancelJoinTeam]').live('click', function(e) {
        e.preventDefault();
        var view = $('input[name=view]').val();
        sandbox.redirectBrowserTo('home.shtml' + '&view=' + view);
      });

      $('input[name=consent]').live('click', function() {
        library.toggleDisable($('input[name=joinTeam]'));
      });
    },

    destroy: function() {

    }
  };

  // Private library (through closure)
  var library = {
    toggleDisable : function(el) {
      if (el instanceof jQuery) {
        if (!el.attr('disabled')) {
          el.attr('disabled', true);
          el.removeClass('button-primary').addClass('button-disabled');
        } else {
          el.removeAttr('disabled');
          el.removeClass('button-disabled').addClass('button-primary');
        }
      }
    }
  };

  // Return the public interface
  return module;
};
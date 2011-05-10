COIN.MODULES.Addteam = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
			// Clicked [ Cancel ]
			$('input[name=cancelCreateTeam]').live('click', function(e) {
				e.preventDefault();
        var view = $('input[name=view]').val();
				sandbox.redirectBrowserTo('home.shtml?teams=my&view=' + view);
			});
			
			// Clicked [ Consent ]
			$('input[name=consent]').live('click', function(e) {
			  library.toggleDisable($('input[name=createTeam]'));
			});
			
			$('#TeamName').focus();
		},
		
		destroy: function() {
			
		}
	};
	
	// Private library (through closure)
	var library = {
			toggleDisable : function(el) {
			  if (el instanceof jQuery) {
			    if (el.attr('disabled') == false) {
			      el.removeClass('button-primary').addClass('button-disabled');
			      el.attr('disabled', true);
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
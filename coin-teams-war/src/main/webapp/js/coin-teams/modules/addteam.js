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
			
			$('#TeamName').focus();
		},
		
		destroy: function() {
			
		}
	};
	
	// Private library (through closure)
	var library = {
			
	};

	// Return the public interface
	return module;
};
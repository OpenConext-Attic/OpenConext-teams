COIN.MODULES.Addteam = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
			// Clicked [ Cancel ]
			$('input[name=cancelCreateTeam]').live('click', function(e) {
				e.preventDefault();
				sandbox.redirectBrowserTo('home.shtml?teams=my');
			});
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
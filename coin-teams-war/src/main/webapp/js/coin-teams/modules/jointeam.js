COIN.MODULES.Jointeam = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
			// Clicked [ Cancel ]
			$('input[name=cancelJoinTeam]').live('click', function(e) {
				e.preventDefault();
				var teamId = $('input[name=team]').val();
        var view = $('input[name=view]').val();
				sandbox.redirectBrowserTo('detailteam.shtml?team=' + escape(teamId) + '&view=' + view);
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
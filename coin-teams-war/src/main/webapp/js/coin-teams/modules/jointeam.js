COIN.MODULES.Jointeam = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
			// Clicked [ Cancel ]
			$('form#JoinTeamForm > input[name=cancelJoinTeam]').live('click', function(e) {
				e.preventDefault();
				var teamId = $('input[name=teamId]').val();
				sandbox.redirectBrowserTo('detailteam.shtml?team=' + teamId);
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
COIN.MODULES.Editteam = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
			
			// Validate the form
			$('#EditTeamForm').validate();
			
			// Clicked [ Cancel ]
			$('input[name=cancelEditTeam]').live('click', function(e) {
				e.preventDefault();
				var teamId = $('input[name=teamId]').val();
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
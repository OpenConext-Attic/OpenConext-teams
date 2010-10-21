COIN.MODULES.Addmember = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
			// Clicked [ Cancel ]
			$('form#AddMemberForm > input[name=cancelAddMember]').live('click', function(e) {
				e.preventDefault();
				var team = $('input[name=team]').val();;
				sandbox.redirectBrowserTo('detailteam.shtml?team=' + team);
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
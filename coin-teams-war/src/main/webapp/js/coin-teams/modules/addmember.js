COIN.MODULES.Addmember = function(sandbox) {
	// Public interface
	var module = {
		init : function() {
			
			// Clicked [ Cancel ]
			$('input[name=cancelAddMember]').live('click',function(e) {
				e.preventDefault();
				var team = $('input[name=team]').val();
				var view = $('input[name=view]').val();
				sandbox.redirectBrowserTo('detailteam.shtml?team=' + escape(team) + '&view=' + view);
			});
		},

		destroy : function() {

		}
	};

	// Private library (through closure)
	var library = {

	};

	// Return the public interface
	return module;
};
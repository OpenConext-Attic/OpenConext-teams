COIN.MODULES.Home = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
		  		
			// Focus on the search box
			$('input[type=text][name=teamSearch]').live('focus', function() {
				if ($(this).val() == '<spring:message code="jsp.home.DefaultSearchTerm" />') {
					$(this).val('');
				};
			});
			
			// Blur on the search box
			$('input[type=text][name=teamSearch]').live('blur', function() {
				if ($(this).val() == '') {
					$(this).val('<spring:message code="jsp.home.DefaultSearchTerm" />');
				};
			});
			
      // Clicked on the [ Help ]
      $('#HelpPage').live('click', function(e) {
        e.preventDefault();
        window.open('help.shtml', 'Help','menubar=no,width=430,height=360,toolbar=no,screenX=400,screenY=300,resizable=yes,scrollbars=yes');
      });
			
//			// Click on the [ Submit ]
//			$('#SubmitTeamSearch').live('click', function() {
//				
//			});
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
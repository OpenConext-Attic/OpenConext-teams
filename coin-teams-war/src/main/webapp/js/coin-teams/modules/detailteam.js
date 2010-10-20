COIN.MODULES.Detailteam = function(sandbox) {
	// Public interface
	var module = {
		init: function() {
			
			// Leave Team Confirm (appears when a user clicks 
			// the "Leave" button of a team in the detailteam screen)
			$("#LeaveTeamDialog").dialog({
				autoOpen   : false,
				width      : 250,
				resizable  : false,
				modal      : true,
				dialogClass: "ui-popup",
				buttons: {
					'<spring:message code='jsp.dialog.leaveteam.Submit' />': library.leaveTeam,
					'<spring:message code='jsp.general.Cancel' />': library.cancelLeave
				},
				open: function() {
//					$buttonPane = $(this).next();
//		            $buttonPane.find('button:first').addClass('ui-priority-primary');
//		            $buttonPane.find('button:last').addClass('ui-priority-secondary');   
				},
				closeOnEscape: true
			});
			
			// Delete Team Confirm (appears when a user clicks 
			// the "Delete" button of a team in the detailteam screen)
			$("#DeleteTeamDialog").dialog({
				autoOpen   : false,
				width      : 250,
				resizable  : false,
				modal      : true,
				dialogClass: "ui-popup",
				buttons: {
					'<spring:message code='jsp.dialog.leaveteam.Submit' />': library.leaveTeam,
					'<spring:message code='jsp.general.Cancel' />': library.cancelLeave
				},
				open: function() {
//					$buttonPane = $(this).next();
//		            $buttonPane.find('button:first').addClass('ui-priority-primary');
//		            $buttonPane.find('button:last').addClass('ui-priority-secondary');   
				},
				closeOnEscape: true
			});
			
			// Clicked [ Leave ]
			$('a#LeaveTeam').live('click', function(e) {
				e.preventDefault();
				$('#LeaveTeamDialog').removeClass('hide').dialog('open');
			});
			
			// Clicked [ Delete ]
			$('a#DeleteTeam').live('click', function(e) {
				e.preventDefault();
				$('#DeleteTeamDialog').removeClass('hide').dialog('open');
			});		
		},
		
		destroy: function() {
			
		}
	};
	
	// Private library (through closure)
	var library = {
		leaveTeam: function() {
			
			sandbox.redirectBrowserTo($('a#LeaveTeam').attr('href'));
		},
		cancelLeave: function() {
			$('#LeaveTeamDialog').addClass('hide').dialog('close');
		},
		deleteTeam: function() {
			sandbox.redirectBrowserTo($('a#DeleteTeam').attr('href'));
		},
		cancelDelete: function() {
			$('#DeleteTeamDialog').addClass('hide').dialog('close');
		}
	};

	// Return the public interface
	return module;
};
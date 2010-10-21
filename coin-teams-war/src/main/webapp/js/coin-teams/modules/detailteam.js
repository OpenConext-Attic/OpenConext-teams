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
			
			// Clicked [ Permission ]
			$('input[type=checkbox]').live('click', function() {
				if($(this).attr('checked')) {
					library.addRole($(this));
				} else {
					library.removeRole($(this));
				}
			})
		},
		
		destroy: function() {
			
		}
	};
	
	// Private library (through closure)
	var library = {
		getMemberId: function(el) {
			if (el instanceof jQuery) {
				var idSplit = el.attr('id').split('_', '2');
				return idSplit[1];
			}
		},
		getRole: function(el) {
			if (el instanceof jQuery) {
				var idSplit = el.attr('id').split('_', '2');
				return idSplit[0];
			}
		},
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
		},
		addRole: function(el) {
			var teamId = $('input[name=teamId]').val();
			var memberId = library.getMemberId(el);
			var role = library.getRole(el);
			
			var data = {
					'team' : teamId,
					'member' : memberId,
					'role' : role
			};
			
			sandbox.post('doaddrole.shtml', data, function(data) {
				if (data === 'success') {
					$.notifyBar({ cls: "success", html: "<spring:message code='jsp.detailteam.AddRoleSuccess' />", delay: 1000 });
				} else {
					el.attr('checked') ? el.attr('checked', false) : el.attr('checked', true);
					$.notifyBar({ cls: "error", html: "<spring:message code='jsp.detailteam.AddRoleFailure' />" });
				}
			});
		},
		removeRole: function(el) {
			var teamId = $('input[name=teamId]').val();
			var memberId = library.getMemberId(el);
			var role = library.getRole(el);
			
			var data = {
					'team' : teamId,
					'member' : memberId,
					'role' : role
			};
			
			sandbox.post('doremoverole.shtml', data, function(data) {
				if (data === 'success') {
					$.notifyBar({ cls: "success", html: "<spring:message code='jsp.detailteam.RemoveRoleSuccess' />", delay: 1000 });
				} else {
					el.attr('checked') ? el.attr('checked', false) : el.attr('checked', true);
					$.notifyBar({ cls: "error", html: "<spring:message code='jsp.detailteam.RemoveRoleFailure' />" });
				}
			});
		}
	};

	// Return the public interface
	return module;
};
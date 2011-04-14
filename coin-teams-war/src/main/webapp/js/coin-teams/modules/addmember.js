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

			// Add jquery validator method
			jQuery.validator.addMethod("multiemail", function(value, element) {
				if (this.optional(element)) // return true on optional element
					return true;
				var emails = value.split(new RegExp("\\s*,\\s*", "gi"));
				valid = true;
				for ( var i in emails) {
					value = emails[i];
					valid = valid
							&& jQuery.validator.methods.email.call(this, value,
									element);
				}
				return valid;
			}, '<spring:message code="error.wrongFormattedEmailList" />');

			$('#AddMemberForm').validate();
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
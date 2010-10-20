/**
 * Note that this is NOT the /js/coin.js being loaded by the User Agent, that is handled
 * by /control/JSController.java
 * 
 * For a list of what is included in /js/coin.js see /jsp/js.jsp
 */

// set default easing
jQuery.easing.def = "easeOutQuad";

var COIN = { 
	MODULES: {
	}
};

// function is called when the DOM is loaded
$(function() {
	
	COIN.Core.register('home', COIN.MODULES.Home);
	
	if ($('#TeamsTableContainer').length > 0) {
		COIN.Core.register('teamoverview', COIN.MODULES.Teamoverview);
	}
	
	if ($('#AddTeamForm').length > 0) {
		COIN.Core.register('addteam', COIN.MODULES.Addteam);
	}
	
	COIN.Core.register('detailteam', COIN.MODULES.Detailteam);
	
	
//	if ($("#IntroPage").length > 0) {
//		COIN.Core.register('landingpage', COIN.MODULES.Landingpage);
//	}
		
	COIN.Core.startAll();
});
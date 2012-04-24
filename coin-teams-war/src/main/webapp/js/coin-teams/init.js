/*
 * Copyright 2012 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	
	if ($('#EditTeamForm').length > 0) {
		COIN.Core.register('editteam', COIN.MODULES.Editteam);
	}
	
	if ($('#JoinTeamForm').length > 0) {
		COIN.Core.register('jointeam', COIN.MODULES.Jointeam);
	}
	
	if ($('#invitationForm').length > 0) {
		COIN.Core.register('addmember', COIN.MODULES.Addmember);
	}

  if ($('#AcceptInvitationForm').length > 0) {
    COIN.Core.register('acceptinvitation', COIN.MODULES.Acceptinvitation);
  }
  
	COIN.Core.register('detailteam', COIN.MODULES.Detailteam);
	
	
//	if ($("#IntroPage").length > 0) {
//		COIN.Core.register('landingpage', COIN.MODULES.Landingpage);
//	}
		
	COIN.Core.startAll();
});
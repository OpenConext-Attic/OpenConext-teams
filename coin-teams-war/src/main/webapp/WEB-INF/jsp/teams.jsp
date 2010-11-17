<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/xml; charset=UTF-8" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<Module>
	<ModulePrefs title="SURFteams" height="330">
		<Require feature="opensocial-0.8" />
		<Require feature="dynamic-height" />
		<Require feature="setprefs" />
	</ModulePrefs>
	<UserPref name="groupContext" />
	<Content type="html">
        <![CDATA[
        <div id="SURFteamsContent"></div>
        <script type="text/javascript">
        
        function loadFrame() {
                var srcString ='<c:out value="${teamsURL}" />';
                var prefs = new gadgets.Prefs();
                var groupContext = escape(prefs.getString('groupContext'));
                        if(groupContext != "") {
                                srcString += 'detailteam.shtml?team=' + groupContext;
                        }
                document.getElementById('SURFteamsContent').innerHTML = '<iframe frameborder="0" scrolling="no" width="100%" height="320" src="'+srcString+'"></iframe>';
                gadgets.window.adjustHeight(); //so app resizes itself based on content
        }
        
        gadgets.util.registerOnLoadHandler(loadFrame);
 		]]>
	</Content>
</Module>
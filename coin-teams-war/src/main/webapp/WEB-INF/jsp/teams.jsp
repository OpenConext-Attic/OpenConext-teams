<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/xml; charset=UTF-8" %>
<Module>
  <ModulePrefs
    title="SURFteams"
    height="350">
  </ModulePrefs>
  <UserPref name="groupContext" />
  <Content type="html">
        <![CDATA[
        <div id="SURFteamsContent"></div>
        <script type="text/javascript">

        var srcString ='${teamsURL}/';
        var prefs = new gadgets.Prefs();
        var groupContext = escape(prefs.getString('groupContext'));
        if(groupContext != '') {
          srcString += 'detailteam.shtml?view=gadget&team=' + groupContext;
        } else {
          srcString += 'home.shtml?view=gadget';
        }
        document.getElementById('SURFteamsContent').innerHTML = '<iframe frameborder="0" scrolling="no" width="100%" height="325" src="'+srcString+'"></iframe>';

     ]]>
  </Content>
</Module>
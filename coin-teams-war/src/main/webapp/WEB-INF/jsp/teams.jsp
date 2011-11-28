<?xml version="1.0" encoding="UTF-8" ?>
<%@ page contentType="text/xml; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c"%>
<%--
  ~ Copyright 2011 SURFnet bv, The Netherlands
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<Module>
  <ModulePrefs title="SURFteams">
    <Require feature="dynamic-height"/>
  </ModulePrefs>
  <UserPref name="groupContext"/>
  <Content type="html">
    <![CDATA[
      <div id="SURFteamsContent"></div>
      <script type="text/javascript">

      var srcString ='${teamsURL}/';
      var prefs = new gadgets.Prefs();

      var groupContext = escape(prefs.getString('groupContext'));
      var groupNameContext = escape('${groupNameContext}');

      if(groupContext != '' && groupContext.indexOf(groupNameContext) != -1) {
        srcString += 'detailteam.shtml?view=gadget&team=' + groupContext.replace(groupNameContext, '');
      } else if (groupContext != '') {
        srcString += 'detailteam.shtml?view=gadget&team=' + groupContext;
      } else {
        srcString += 'home.shtml?view=gadget';
      }
      var rpcToken = gadgets.util.getUrlParameters()['rpctoken'];
      srcString += '&rpctoken=' + rpcToken;

      document.getElementById('SURFteamsContent').innerHTML = '<iframe id="teams-iframe" name="teams-iframe" frameborder="0" scrolling="auto" width="100%" height="375" src="'+srcString+'"></iframe>';
      gadgets.util.registerOnLoadHandler(function() {
        gadgets.rpc.setAuthToken('teams-iframe', rpcToken);
        gadgets.rpc.setRelayUrl('teams-iframe', '<c:out value="${shindigHost}" />/container/rpc_relay.html');
        gadgets.rpc.register("setheight", function(new_height) {
          document.getElementById('teams-iframe').height = new_height;
          gadgets.window.adjustHeight();
        });
      });
      </script>
     ]]>
  </Content>
</Module>
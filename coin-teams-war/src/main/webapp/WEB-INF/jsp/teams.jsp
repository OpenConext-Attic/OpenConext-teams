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
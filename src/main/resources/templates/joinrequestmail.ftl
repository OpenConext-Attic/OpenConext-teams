[#ftl]
[#setting url_escaping_charset="UTF-8"]
[#--<!doctype html>--]
[#--
  Copyright 2012 SURFnet bv, The Netherlands

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  --]

[#import "macros_htmlmail.ftl" as macros_htmlmail/]
[#--
Template variables:
String requesterName
String requesterEmail
Team team
String teamsURL
String message
 --]
[@macros_htmlmail.mailheader/]
        <p lang="en">
          ${requesterName?html} (${requesterEmail?html}) would like to join team <strong>${team.name?html}</strong>.
        </p>

        [#if message?has_content]
        <p>
          [#assign msg]${message?html}[/#assign]
          <strong>Personal message from ${requesterName?html}:</strong><br /> "${msg?replace("\n","<br />")}"
        </p>
        [/#if]

        [#if team.description?has_content]
        [#assign description]${team.description?html}[/#assign]
        <p>
          <strong>Team description:</strong><br /> "${description?replace("\n","<br />")}"
        </p>
        [/#if]

        [#assign detailTeamUrl]${teamsURL}/detailteam.shtml?view=app&team=${team.id?url}[/#assign]
        <table cellpadding="10" width="90%" align="center" style="margin-bottom:1em;margin-left:auto;margin-right:auto;margin-top:1em;">
          <tr>
            <td bgcolor="#D9EDF7" style="mso-line-height-rule:exactly;line-height:18px;font-size:13px;font-family:Arial, sans-serif;border-radius:4px 4px 4px 4px;color:#333333;border-style:solid;border-width:1px;border-color:#4FB3CF;"
                align="center" width="100%">
                <span lang="en"><a href="${detailTeamUrl}" style="color:#0088CC;">Login to process this request</a></span>
                <br/><span lang="nl"><a href="${detailTeamUrl}" style="color:#0088CC;">Inloggen om dit verzoek af te handelen</a></span>
            </td>
          </tr>
        </table>
[@macros_htmlmail.mailfooter/]
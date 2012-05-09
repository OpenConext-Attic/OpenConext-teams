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

[#--
Template variables:
String requesterName
String requesterEmail
Team team
String teamsURL
String message
 --]
[#-- In head: Generate more than 1109 spaces for iOS devices, but no more than 250 chars at a time --]
<html>
<head><title></title>
[#list 1..1110 as x]${' '}[#if x % 250 = 0]${'\n'}[/#if][/#list]
</head>
<body style="color:#333333;mso-line-height-rule:exactly;line-height:18px;font-size:13px;font-family:Arial, sans-serif;">
<table width="100%" border="0" cellspacing="0" cellpadding="0"
       style="color:#333333;mso-line-height-rule:exactly;line-height:18px;font-size:13px;font-family:Arial, sans-serif;">
  <tr>
    <td>
      <style type="text/css">
        .ReadMsgBody {
          width: 100%;
        }

        .ExternalClass {
          width: 100%;
        }

        body {
          color: #333333;
          line-height: 18px;
          font-size: 13px;
          font-family: Arial, sans-serif;
        }

        body, td {
          font-family: Arial, sans-serif;
          font-size: 13px;
          mso-line-height-rule: exactly;
          line-height: 18px;
          color: #333333;
        }

        h1 {
          font-size: 28px;
          font-weight: normal;
          mso-line-height-rule: exactly;
          line-height: 33px;
        }

        a, a:visited {
          color: #0088CC;
        }

        span.yshortcuts {
          color: #000;
          background-color: none;
          border: none;
        }

        span.yshortcuts:hover,
        span.yshortcuts:active,
        span.yshortcuts:focus {
          color: #000;
          background-color: none;
          border: none;
        }
      </style>
      <div
          style="max-width:960px;border-radius:4px 4px 4px 4px;margin-bottom:0 ;margin-left:auto;margin-right:auto;margin-top:0 ;padding-bottom:1%;padding-left:1%;padding-right:1%;padding-top:1%;border-style:solid;border-width:1px;border-color:#D8DADC;">
        <img src="https://static.surfconext.nl/media/surfconext.png" width="63" height="40"
            alt="SURFconext logo" align="right"/>

        <h1 style="mso-line-height-rule:exactly;line-height:33px;font-weight:normal;font-size:28px;margin-top:0;">
          SURFteams</h1>

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

        <p lang="en">
          Consult the
          <a href="https://wiki.surfnetlabs.nl/display/conextsupport/SURFteams" style="color:#0088CC;">manual for specific information on SURFteams</a>
          or contact SURFteams support at <a href="mailto:help@surfteams.nl" style="color:#0088CC;">help@surfteams.nl</a>.
        </p>

        <p lang="en">
          This service is powered by <a href="http://www.surfconext.nl" style="color:#0088CC;">SURFconext</a> - brought
          to you by SURFnet
        </p>
      </div>
    </td>
  </tr>
</table>
</body>
</html>
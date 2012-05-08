[#ftl]
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
Invitation invitation
Person inviter
Team team
String teamsURL
 --]

<html>
<head><title></title>
[#-- Generate more than 1109 spaces for iOS devices --]
[#list 1..1110 as x]${' '}[/#list]
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
        <img
            src="https://wayf.surfnet.nl/federate/surfnet/img/logo/surfnet.png" width="108" height="44"
            alt="SURFnet logo" align="right"/>

        <h1 style="mso-line-height-rule:exactly;line-height:33px;font-weight:normal;font-size:28px;margin-top:0;">
          SURFteams</h1>

        <p>
          You are invited by ${inviter.displayName?html} to join team <strong>${team.name?html}</strong>.
        </p>

        [#if invitation.latestInvitationMessage?has_content && invitation.latestInvitationMessage.message?has_content]
        <p>
          [#assign msg]${invitation.latestInvitationMessage.message?html}[/#assign]
          <strong>Personal message from ${inviter.displayName?html}:</strong> "${msg?replace("\n","<br />")}"
        </p>
        [/#if]

        [#if team.description?has_content]
        <p>
          <strong>Team description:</strong> "${team.description?html}"
        </p>
        [/#if]

        [#assign acceptUrl]${teamsURL}/acceptInvitation.shtml?id=${invitation.invitationHash}[/#assign]
        [#assign declineUrl]${teamsURL}/declineInvitation.shtml?id=${invitation.invitationHash}[/#assign]
        <table style="width:90%;margin-bottom:1em;margin-left:auto;margin-right:auto;margin-top:1em;">
          <tr>
            <td style="mso-line-height-rule:exactly;line-height:18px;font-size:13px;font-family:Arial, sans-serif;border-radius:4px 4px 4px 4px;color:#489406;background-color:#EDFFDE;padding-bottom:10px;padding-left:10px;padding-right:10px;padding-top:10px;border-style:solid;border-width:1px;border-color:#489406;"
                align="center" width="50%">
              <div style="margin-bottom:0 ;margin-left:auto;margin-right:auto;margin-top:0 ;">
                <span lang="en"><a href="${acceptUrl}" style="color:#0088CC;">Login to accept this invitation</a></span>
                <br/><span lang="nl"><a href="${acceptUrl}" style="color:#0088CC;">Inloggen om de uitnodiging te accepteren</a></span>
              </div>
            </td>
            <td style="color:#333333;mso-line-height-rule:exactly;line-height:18px;font-size:13px;font-family:Arial, sans-serif;padding-bottom:10px;padding-left:10px;padding-right:10px;padding-top:10px;"
                align="center" width="50%">
              <div style="margin-bottom:0 ;margin-left:auto;margin-right:auto;margin-top:0 ;">
                <span lang="en"><a href="${declineUrl}" style="color:#0088CC;">Decline this invitation</a></span>
                <br/><span lang="nl"><a href="${declineUrl}" style="color:#0088CC;">De uitnodiging afwijzen</a></span>
              </div>
            </td>
          </tr>
        </table>
        <p lang="en">
          This invitation automatically expires after 14 days.
        </p>

        <p>
          Consult the manual for specific information on SURFteams
          <a href="https://wiki.surfnetlabs.nl/display/conextsupport/SURFteams" style="color:#0088CC;">https://wiki.surfnetlabs.nl/display/conextsupport/SURFteams</a>
          or contact SURFteams support at <a href="mailto:help@surfteams.nl" style="color:#0088CC;">help@surfteams.nl</a>.
        </p>

        <p>
          This service is powered by <a href="http://www.surfconext.nl" style="color:#0088CC;">SURFconext</a> - brought
          to you by SURFnet
        </p>
      </div>
    </td>
  </tr>
</table>
</body>
</html>
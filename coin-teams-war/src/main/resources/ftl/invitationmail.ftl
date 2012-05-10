[#ftl]
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
Invitation invitation
Person inviter
Team team
String teamsURL
 --]
[@macros_htmlmail.mailheader/]
        <p lang="en">
          You are invited by ${inviter.displayName?html} to join team <strong>${team.name?html}</strong>.
        </p>

        [#if invitation.latestInvitationMessage?has_content && invitation.latestInvitationMessage.message?has_content]
        <p>
          [#assign msg]${invitation.latestInvitationMessage.message?html}[/#assign]
          <strong>Personal message from ${inviter.displayName?html}:</strong><br /> "${msg?replace("\n","<br />")}"
        </p>
        [/#if]

        [#if team.description?has_content]
        [#assign description]${team.description?html}[/#assign]
        <p>
          <strong>Team description:</strong><br /> "${description?replace("\n","<br />")}"
        </p>
        [/#if]

        [#assign acceptUrl]${teamsURL}/acceptInvitation.shtml?id=${invitation.invitationHash}[/#assign]
        [#assign declineUrl]${teamsURL}/declineInvitation.shtml?id=${invitation.invitationHash}[/#assign]
        <table cellpadding="10" width="90%" align="center" style="margin-bottom:1em;margin-left:auto;margin-right:auto;margin-top:1em;">
          <tr>
            <td bgcolor="#EDFFDE" style="mso-line-height-rule:exactly;line-height:18px;font-size:13px;font-family:Arial, sans-serif;border-radius:4px 4px 4px 4px;color:#489406;border-style:solid;border-width:1px;border-color:#489406;"
                align="center" width="50%">
                <span lang="en"><a href="${acceptUrl}" style="color:#0088CC;">Login to accept this invitation</a></span>
                <br/><span lang="nl"><a href="${acceptUrl}" style="color:#0088CC;">Inloggen om de uitnodiging te accepteren</a></span>
            </td>
            <td style="color:#333333;mso-line-height-rule:exactly;line-height:18px;font-size:13px;font-family:Arial, sans-serif;"
                align="center" width="50%">
                <span lang="en"><a href="${declineUrl}" style="color:#0088CC;">Decline this invitation</a></span>
                <br/><span lang="nl"><a href="${declineUrl}" style="color:#0088CC;">De uitnodiging afwijzen</a></span>
            </td>
          </tr>
        </table>
        <p lang="en">
          This invitation automatically expires after 14 days.
        </p>
[@macros_htmlmail.mailfooter/]
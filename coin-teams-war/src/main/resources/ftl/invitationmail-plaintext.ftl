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
[#import "macros_plaintextmail.ftl" as macros_plaintextmail/]
[#--
Template variables:
Invitation invitation
Person inviter
Team team
String teamsURL
 --]
[#assign acceptUrl]${teamsURL}/acceptInvitation.shtml?id=${invitation.invitationHash}[/#assign]
[#assign declineUrl]${teamsURL}/declineInvitation.shtml?id=${invitation.invitationHash}[/#assign]

[@macros_plaintextmail.mailheader/]

You have been invited by ${inviter.displayName?html} to join team *${team.name?html}*.

[#if invitation.latestInvitationMessage?has_content && invitation.latestInvitationMessage.message?has_content]
*Personal message from ${inviter.displayName?html}:*
"${invitation.latestInvitationMessage.message?html}"[/#if]

[#if team.description?has_content]
*Team description:*
"${team.description?html}"
[/#if]

Login to accept this invitation: ${acceptUrl}
Inloggen om de uitnodiging te accepteren: ${acceptUrl}

Decline this invitation: ${declineUrl}
De uitnodiging afwijzen: ${declineUrl}

This invitation automatically expires after 14 days.

[@macros_plaintextmail.mailfooter/]
[#ftl]
[#setting url_escaping_charset="UTF-8"]
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

*SURFteams*

${requesterName?html} (${requesterEmail?html}) would like to join team *${team.name?html}*.

[#if message?has_content]
*Personal message from ${requesterName?html}:*
"${message?html}"
[/#if]

[#if team.description?has_content]
*Team description:*
"${team.description?html}"
[/#if]

[#assign detailTeamUrl]${teamsURL}/detailteam.shtml?view=app&team=${team.id?url}[/#assign]
Login to process this request: ${detailTeamUrl}
Inloggen om dit verzoek af te handelen: ${detailTeamUrl}

Consult the manual for specific information on SURFteams: https://wiki.surfnetlabs.nl/display/conextsupport/SURFteams
or contact SURFteams support at help@surfteams.nl.

This service is powered by SURFconext http://www.surfconext.nl/ - brought to you by SURFnet

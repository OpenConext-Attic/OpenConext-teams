
<#macro message code>${messages.getMessage(code, [], locale)}</#macro>

<#macro messageArgs code, args>${messages.getMessage(code, args, locale)}</#macro>
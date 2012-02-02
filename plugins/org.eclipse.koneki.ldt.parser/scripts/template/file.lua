--------------------------------------------------------------------------------
--  Copyright (c) 2012 Sierra Wireless.
--  All rights reserved. This program and the accompanying materials
--  are made available under the terms of the Eclipse Public License v1.0
--  which accompanies this distribution, and is available at
--  http://www.eclipse.org/legal/epl-v10.html
-- 
--  Contributors:
--       Kevin KIN-FOO <kkinfoo@sierrawireless.com>
--           - initial API and implementation and initial documentation
--------------------------------------------------------------------------------
return[[#
<div id="content">
# --
# -- Module name
# --
# if _file.name then
   <h1>Module <code>$(_file.name)</code></h1>
# end
# --
# -- Descriptions
# --
# if _file.shortdescription then
   $( markdown(_file.shortdescription) )
# end
# if _file.description then
   <br/>$( markdown(_file.description) )
# end
# --
# -- Show quick description of current type
# --
# 
# -- show quick description for globals
# if not isempty(_file.globalvars) then
	<h2>Global(s)</h2>
	<table class="function_list">
#	for _, item in pairs(_file.globalvars) do
		<tr>
		<td class="name" nowrap="nowrap"><a href="#$(linkto(item))">$( prettyname(item) )</a></td>
		<td class="summary">$( markdown(item.shortdescription) )</td>
		</tr>
# 	end
	</table>
# end
# -- show quick description type exposed by module
# local currenttype = _file.returns[1] and _file.types[ _file.returns[1].types[1].typename ]
# if currenttype then
	<a id="$(anchor(currenttype))" />
	<h2>Type <code>$(currenttype.name)</code></h2>
	$( applytemplate(currenttype, 'index') )
# end
# --
# -- Show quick description of other types
# --
# if _file.types then
#	for name, type in pairs( _file.types ) do
#		if type ~= currenttype and type.tag == 'recordtypedef' then
			<a id="$(anchor(type))" />
			<h2>Type <code>$(name)</code></h2>
			$( applytemplate(type, 'index') )
#		end
#	end
# end
# --
# -- Long description of globals
# --
# if not isempty(_file.globalvars) then
	<h2>Global(s)</h2>
#	for name, item in pairs(_file.globalvars)do
		$( applytemplate(item) )
#	end
# end
# --
# -- Long description of current type
# --
# if currenttype then
	<h2>Type <code>$(currenttype.name)</code></h2>
	$( applytemplate(currenttype) )
# end
# --
# -- Long description of other types
# --
# if not isempty( _file.types ) then
#	for name, type in pairs( _file.types ) do
#		if type ~= currenttype  and type.tag == 'recordtypedef' then
			<h2>Type <code>$(name)</code></h2>
			$( applytemplate(type) )
#		end
#	end
# end
</div>
]]
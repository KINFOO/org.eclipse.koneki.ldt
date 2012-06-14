--------------------------------------------------------------------------------
--  Copyright (c) 2011-2012 Sierra Wireless.
--  All rights reserved. This program and the accompanying materials
--  are made available under the terms of the Eclipse Public License v1.0
--  which accompanies this distribution, and is available at
--  http://www.eclipse.org/legal/epl-v10.html
--
--  Contributors:
--       Kevin KIN-FOO <kkinfoo@sierrawireless.com>
--           - initial API and implementation and initial documentation
--------------------------------------------------------------------------------
local M = {}

local itemclass             = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.Item")
local returnclass           = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.Return")
local recordtypedefclass    = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.RecordTypeDef")
local functiontypedefclass  = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.FunctionTypeDef")
local parameterclass        = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.Parameter")
local externaltyperefclass  = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.ExternalTypeRef")
local internaltyperefclass  = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.InternalTypeRef")
local primitivetyperefclass = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.PrimitiveTypeRef")
local luafileapiclass       = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.LuaFileAPI")
local moduletyperefclass    = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.ModuleTypeRef")
local exprtyperefclass      = java.require("org.eclipse.koneki.ldt.core.internal.ast.models.api.ExprTypeRef")

local print = function (string) print(string) io.flush() end

local templateengine = require 'templateengine'

--
-- Update documentation templateengine environment
--
local templateengineenv = require 'template.utils'

-- Remove default implementation not supported from IDE
templateengineenv.anchortypes['externaltyperef'] = nil
templateengineenv.linktypes['externaltyperef'] = nil

--
-- So far, documentation embedded in the IDE does not support links very well.
-- To circumvent this, we will need to desactivate link generation while
-- generation documentation.

-- Cache of link generation
local originallinkto = templateengineenv.linkto

-- Restore link generators
local function enablelinks()
	templateengine.env.linkto = originallinkto
end

-- Disable link generators
local function disablelinks()
	templateengine.env.linkto = function()
		return nil, 'Link generation is disabled.'
	end
end

-- Links are disabled by default
disablelinks()

-- Handle only local item references
templateengineenv.linktypes['item'] = function(item)
	if item.parent and item.parent.tag == 'recordtypedef' then
		return string.format('#%s.%s', templateengineenv.anchor(item.parent), item.name)
	end
	return string.format('#%s', templateengineenv.anchor(item))
end

-- Perform actual environment update
for functionname, body in pairs( templateengineenv ) do
	templateengine.env[ functionname ] = body
end

-- create typeref
function M._typeref (_type)
	if not _type then return nil end
	if _type.tag == "externaltyperef" then
		return externaltyperefclass:new(_type.modulename, _type.typename)
	elseif _type.tag == "internaltyperef" then
		return internaltyperefclass:new(_type.typename)
	elseif _type.tag == "moduletyperef" then
		return moduletyperefclass:new(_type.modulename,_type.returnposition)
	elseif _type.tag == "exprtyperef" then
		return exprtyperefclass:new(_type.returnposition)
	elseif _type.tag == "primitivetyperef" then
		return primitivetyperefclass:new(_type.typename)
	end
end

-- create item
function M._item(_item)
	local jitem = itemclass:new()
	jitem:setDocumentation(templateengine.applytemplate(_item))
	jitem:setName(_item.name)
	jitem:setStart(_item.sourcerange.min)
	jitem:setEnd(_item.sourcerange.max)
	-- Define optional type
	if _item.type then
		jitem:setType(M._typeref(_item.type))
	end
	return jitem
end

-- create typedef
function M._typedef(_typedef)
	local jtypedef
	-- Dealing with records
	if _typedef.tag == "recordtypedef" then

		jtypedef = recordtypedefclass:new()
		jtypedef:setName(_typedef.name)
		jtypedef:setStart(_typedef.sourcerange.min)
		jtypedef:setEnd(_typedef.sourcerange.max)
		jtypedef:setDocumentation(templateengine.applytemplate(_typedef))

		-- Appending fields
		for _, _item in pairs(_typedef.fields) do
			local jitem =  M._item(_item)
			jtypedef:addField(_item.name, jitem)
		end

	elseif _typedef.tag == "functiontypedef" then
		-- Dealing with function
		jtypedef = functiontypedefclass:new()

		-- Appending parameters
		for _, _param in ipairs(_typedef.params) do
			jtypedef:addParameter( parameterclass:new(_param.name, M._typeref(_param.type), _param.description) )
		end

		-- Appending returned types
		for _, _return in ipairs(_typedef.returns) do
			local jreturn = returnclass:new()
			for _, _type in ipairs( _return.types ) do
				jreturn:addType(M._typeref(_type))
			end
			jtypedef:addReturn(jreturn)
		end
	end
	return jtypedef
end

-- create lua file api
function M._file(_file)

	-- Fill file object
	local jfile = luafileapiclass:new()

	-- Enable links just for module file objects 
	enablelinks()
	jfile:setDocumentation(templateengine.applytemplate(_file))
	disablelinks()

	-- Adding gloval variables
	for _, _item in pairs(_file.globalvars) do
		-- Fill Java item
		jfile:addGlobalVar(_item.name, M._item(_item))
	end

	-- Adding returned types
	for _, _return in ipairs(_file.returns) do
		local jreturn = returnclass:new()
		for _, _type in ipairs(_return.types) do
			jreturn:addType(M._typeref(_type))
		end
		jfile:addReturns(jreturn)
	end

	-- Adding types defined in files
	for _, _typedef in pairs(_file.types) do
		jfile:addType(_typedef.name, M._typedef(_typedef))
	end

	return jfile
end
return M

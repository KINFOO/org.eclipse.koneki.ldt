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
local J = {}
local externalapi = require 'externaljavaapi'

local blockclass =           java.require 'org.eclipse.koneki.ldt.parser.ast.Block'
local callclass  =           java.require 'org.eclipse.koneki.ldt.parser.ast.Call'
local identifierclass =      java.require 'org.eclipse.koneki.ldt.parser.ast.Identifier'
local indexclass =           java.require 'org.eclipse.koneki.ldt.parser.ast.Index'
local invokeclass =          java.require 'org.eclipse.koneki.ldt.parser.ast.Invoke'
local internalcontentclass = java.require 'org.eclipse.koneki.ldt.parser.ast.LuaInternalContent'
local localvarclass =        java.require 'org.eclipse.koneki.ldt.parser.ast.LocalVar'

--------------------------------------
-- create internal content java object
function J._internalcontent(_internalcontent)
   local jinternalcontent = internalcontentclass:new()
   
   -- Setting body
   local handledexpr ={} 
   local jblock = J._block(_internalcontent.content,handledexpr)
   jinternalcontent:setContent(jblock)
   
   -- Appending global variables
   for _, _item in ipairs(_internalcontent.unknownglobalvars) do
      local jitem = externalapi.createitem(_item)
      jinternalcontent:addUnknownglobalvar(jitem)
      
      -- add occurrences 
      for _,_occurrence in ipairs(_item.occurrences) do
         jidentifier = handledexpr[_occurrence]
         if jidentifier then 
            jitem:addOccurrence(jidentifier)
         end
      end
   end
   
   return jinternalcontent
end

--------------------------------------
-- create block java object
function J._block(_block,handledexpr)
	-- Setting source range
	local jblock = blockclass:new()
	jblock:setStart(_block.sourcerange.min)
	jblock:setEnd(_block.sourcerange.max)
	
	-- Append nodes to block
	for _, _expr in pairs(_block.content) do
		local jexpr = J._expression(_expr,handledexpr)
		jblock:addContent(jexpr)
	end
	
	for _, _localvar in pairs(_block.localvars) do
      -- Create Java item
      local jitem = externalapi.createitem(_localvar.item)
      if _localvar.item.type and _localvar.item.type.tag == "exprtyperef" then
         jitem:getType():setExpression(handledexpr[_localvar.item.type.expression]) 
      end
      
--table.print(_localvar.item.occurrences)
io.flush()
      
      -- add occurrence   
      for _,_occurrence in ipairs(_localvar.item.occurrences) do
         jidentifier = handledexpr[_occurrence]
         if jidentifier then 
            jitem:addOccurrence(jidentifier)
         end
      end
         
            
      -- Append Java local variable definition
      local jlocalvar  = localvarclass:new(jitem, _localvar.scope.min, _localvar.scope.max)
      jblock:addLocalVar(jlocalvar)
   end
   return jblock
end

--------------------------------------
-- create expression java object
function J._expression(_expr,handledexpr)
	local tag = _expr.tag
	if tag == "MIdentifier" then
		return J._identifier(_expr,handledexpr)
	elseif tag == "MIndex" then
		return J._index(_expr,handledexpr)
	elseif tag == "MCall" then
		return J._call(_expr,handledexpr)
	elseif tag == "MInvoke" then
		return J._invoke(_expr,handledexpr)
   elseif tag == "MBlock" then
      return J._block(_expr,handledexpr)
	end
	return nil
end

--------------------------------------
-- create identifier java object
function J._identifier(_identifier,handledexpr)
	local jidentifier = identifierclass:new()
	jidentifier:setStart(_identifier.sourcerange.min)
	jidentifier:setEnd  (_identifier.sourcerange.max)
	
	handledexpr[_identifier] =jidentifier 
	return jidentifier
end

--------------------------------------
-- create index java object
function J._index(_index,handledexpr)
	local jindex = indexclass:new()
	jindex:setStart(_index.sourcerange.min)
	jindex:setEnd  (_index.sourcerange.max)
	jindex:setLeft (J._expression(_index.left,handledexpr))
	jindex:setRight(_index.right)
	
	handledexpr[_index] =jindex 
	return jindex
end

--------------------------------------
-- create call java object
function J._call(_call,handledexpr)
   local jcall = callclass:new()
   jcall:setStart(_call.sourcerange.min)
   jcall:setEnd  (_call.sourcerange.max)
   jcall:setFunction(J._expression(_call.func,handledexpr))
   
   handledexpr[_call] =jcall 
   return jcall
end

--------------------------------------
-- create invoke java object
function J._invoke(_invoke,handledexpr)
  local jinvoke = invokeclass:new()
	jinvoke:setStart(_invoke.sourcerange.min)
	jinvoke:setEnd  (_invoke.sourcerange.max)
	jinvoke:setFunctionName(_invoke.functionname)
	jinvoke:setRecord(J._expression(_invoke.record,handledexpr))
	
	handledexpr[_invoke] =jinvoke
	return jinvoke
end
return J

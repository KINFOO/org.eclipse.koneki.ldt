#!/usr/bin/lua
-------------------------------------------------------------------------------
-- Copyright (c) 2012 Sierra Wireless and others.
-- All rights reserved. This program and the accompanying materials
-- are made available under the terms of the Eclipse Public License v1.0
-- which accompanies this distribution, and is available at
-- http://www.eclipse.org/legal/epl-v10.html
--
-- Contributors:
--     Sierra Wireless - initial API and implementation
-------------------------------------------------------------------------------

--
-- Generate serialized lua API models files next to given file.
--
require 'errnode'
local serializer = require 'serpent'
local apimodelbuilder = require 'models.apimodelbuilder'
local tablecompare  = require 'tablecompare'
if #arg < 1 then
	print 'No file to serialize.'
	return
end
for k = 1, #arg do

	-- Load source to serialize
	local filename = arg[k]
	local luafile = io.open(filename, 'r')
	local luasource = luafile:read('*a')
	luafile:close()

	-- Generate AST
	local ast, errormessage = getast( luasource )
	if not ast then
		print(string.format('Unable to generate AST for %s.\n%s', filename, errormessage))
	else
		--Generate API model
		local apimodel = apimodelbuilder.createmoduleapi(ast)

		-- Strip functions
		 apimodel = tablecompare.stripfunctions( apimodel )
 
		-- Serialize model
		local serializedcode = serializer.serialize( apimodel )

		-- Define file name		
		local serializedfilename = filename:gsub('([%w%-_/\]+)%.lua','%1.serialized')

		-- Save serialized model
		local serializefile = io.open(serializedfilename, 'w')
		serializefile:write( serializedcode )
		serializefile:close()

		-- This a success
		print( string.format('%s serialized to %s.', filename, serializedfilename) )
	end
end

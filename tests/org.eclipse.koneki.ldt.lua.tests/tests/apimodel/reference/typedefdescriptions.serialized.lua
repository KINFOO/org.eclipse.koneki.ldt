do
	local _ = {
		description = "",
		globalvars = { } --[[table: 0x929a0b0]],
		shortdescription = "",
		name = "mod",
		returns = {
			{
				types = {
					{
						typename = "mod",
						tag = "internaltyperef"
					} --[[table: 0x9299f78]]
				} --[[table: 0x929a040]],
				description = "",
				tag = "return"
			} --[[table: 0x9299fa0]]
		} --[[table: 0x929a0d8]],
		types = {
			subtype = {
				description = "Type detailed description.",
				fields = {} --[[table: 0x92a5908]],
				name = "subtype",
				shortdescription = " Type description.",
				parent = nil --[[ref]],
				sourcerange = {
					min = 163,
					max = 237
				} --[[table: 0x92a5930]],
				tag = "recordtypedef"
			} --[[table: 0x92a57d8]],
			mod = {
				fields = {} --[[table: 0x9288950]],
				name = "mod",
				parent = nil --[[ref]],
				sourcerange = {
					min = 140,
					max = 161
				} --[[table: 0x9288978]],
				tag = "recordtypedef"
			} --[[table: 0x9288840]]
		} --[[table: 0x929a088]],
		tag = "file"
	} --[[table: 0x9299f50]];
	_.types.subtype.parent = _;
	_.types.mod.parent = _;
	return _;
end

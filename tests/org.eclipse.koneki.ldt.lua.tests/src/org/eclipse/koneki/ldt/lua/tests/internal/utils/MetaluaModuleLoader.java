/*******************************************************************************
 * Copyright (c) 2012 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.lua.tests.internal.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.koneki.ldt.core.internal.Activator;
import org.eclipse.koneki.ldt.core.internal.ast.parser.ModelsBuilderLuaModule;
import org.eclipse.koneki.ldt.metalua.AbstractMetaLuaModule;

import com.naef.jnlua.LuaState;

/**
 * The aim here is to load Metalua and to compile some Metalua files at runtime.
 */
public class MetaluaModuleLoader extends AbstractMetaLuaModule {

	private final List<String> filesToCompileList;
	private final List<String> luaSourcePath;

	public MetaluaModuleLoader(final List<String> filesToCompile) {

		// Putting files to compile in lua path
		luaSourcePath = new ArrayList<String>(1);
		luaSourcePath.add(ModelsBuilderLuaModule.EXTERNAL_LIB_PATH);

		// Define files to compile at runtime
		filesToCompileList = new ArrayList<String>(filesToCompile);
	}

	@Override
	protected List<String> getMetaLuaSourcePaths() {
		return getLuaSourcePaths();
	}

	@Override
	protected List<String> getMetaLuaFileToCompile() {
		return filesToCompileList;
	}

	@Override
	protected String getPluginID() {
		// It is intentional to provide core plug-in ID as we want to load files from there
		return Activator.PLUGIN_ID;
	}

	@Override
	protected String getModuleName() {
		// Dummy loading
		return "templateengine"; //$NON-NLS-1$
	}

	@Override
	protected List<String> getLuaSourcePaths() {
		return luaSourcePath;
	}

	public LuaState getLuaState() {
		return loadLuaModule();
	}
}

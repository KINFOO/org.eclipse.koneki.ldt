/*******************************************************************************
 * Copyright (c) 2011 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.module;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.koneki.ldt.parser.Activator;

import com.naef.jnlua.LuaState;

/**
 * Abstract class to manipulate Lua module
 */
public abstract class AbstractLuaModule {

	private static final String LUA_PATTERN = "?.lua;"; //$NON-NLS-1$
	private static final String LUAC_PATTERN = "?.luac;"; //$NON-NLS-1$

	private Map<String, File> foldersCache = new HashMap<String, File>();

	/**
	 * load the module with the name return by moduleName and store it in global var module name
	 */
	protected LuaState loadLuaModule() {
		// get lua state
		LuaState luaState = createLuaState();

		// set lua path
		List<File> luaSourceFolders = getScriptFolders(getLuaSourcePaths());
		List<File> luacSourceFolders = getScriptFolders(getLuacSourcePaths());
		setLuaPath(luaState, luaSourceFolders, luacSourceFolders);

		// load module
		luaState.getGlobal("require"); //$NON-NLS-1$
		luaState.pushString(getModuleName());
		luaState.call(1, 1);
		luaState.setGlobal(getModuleName());

		return luaState;
	}

	protected void pushLuaModule(LuaState luaState) {
		luaState.getGlobal(getModuleName());
	}

	/**
	 * return all the script folders
	 */
	protected List<File> getScriptFolders(List<String> folderRelativePaths) {
		List<File> scriptFolders = new ArrayList<File>();
		if (folderRelativePaths != null) {
			for (String folderRelativePath : folderRelativePaths) {
				File scriptFolder = getScriptFolder(folderRelativePath);
				if (scriptFolder != null)
					scriptFolders.add(scriptFolder);
			}
		}
		return scriptFolders;
	}

	/**
	 * return the folder in the bundle (pluginID) at the relative path
	 */
	protected File getScriptFolder(String relativepath) {
		File folder = foldersCache.get(relativepath);
		if (folder == null) {
			try {
				// extract file from bundle and get url
				URL folderUrl = FileLocator.toFileURL(Platform.getBundle(getPluginID()).getEntry(relativepath));
				folder = new File(folderUrl.getFile());
				foldersCache.put(relativepath, folder);
			} catch (IOException e) {
				Activator.logError("Unable to get entry " + relativepath + " in the plugin " + getPluginID(), e); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return folder;
	}

	/**
	 * Add the given folders to the lua path
	 */
	public static void setLuaPath(LuaState luaState, List<File> luafolders, List<File> luacfolders) {
		// Change path
		final StringBuffer code = new StringBuffer("package.path=[["); //$NON-NLS-1$
		for (File folder : luafolders) {
			code.append(folder.getPath());
			code.append(File.separatorChar);
			code.append(LUA_PATTERN);
		}
		for (File folder : luacfolders) {
			code.append(folder.getPath());
			code.append(File.separatorChar);
			code.append(LUAC_PATTERN);
		}
		code.append("]]..package.path"); //$NON-NLS-1$
		luaState.load(code.toString(), "reloadingPath"); //$NON-NLS-1$
		luaState.call(0, 0);
	}

	protected abstract List<String> getLuaSourcePaths();

	protected abstract List<String> getLuacSourcePaths();

	protected abstract LuaState createLuaState();

	protected abstract String getPluginID();

	protected abstract String getModuleName();
}

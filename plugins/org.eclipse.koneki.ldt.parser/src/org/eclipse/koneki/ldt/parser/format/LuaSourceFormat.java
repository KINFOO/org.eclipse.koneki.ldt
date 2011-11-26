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
package org.eclipse.koneki.ldt.parser.format;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.koneki.ldt.metalua.MetaluaStateFactory;
import org.eclipse.koneki.ldt.parser.Activator;

import com.naef.jnlua.LuaRuntimeException;
import com.naef.jnlua.LuaState;

/**
 * All about Lua source code transformations.
 * 
 * This class uses <strong>Metalua</strong> to gather information about source code depth and thus enable to modify if adequately.
 * 
 * @author Kevin KIN-FOO <kkinfoo@sierrawireless.com>
 */
public final class LuaSourceFormat {
	public static final String FORMATTER_PATH = "/scripts/"; //$NON-NLS-1$
	public static final String FORMATTER_LIB_NAME = "format"; //$NON-NLS-1$
	public static final String INDENTATION_FUNTION = "indentCode"; //$NON-NLS-1$

	private LuaSourceFormat() {
	}

	/**
	 * Provide semantic depth of a given source code offset.
	 * 
	 * @param source
	 *            Lua source code to analyze
	 * @param offset
	 *            Source code position which depth is required
	 * @return Offset semantic depth
	 */
	public static int depth(final String source, final int offset) {
		// Load function
		final LuaState lua = loadState();
		lua.getField(-1, "indentLevel"); //$NON-NLS-1$

		// Pass arguments
		lua.pushString(source);
		lua.pushInteger(offset);

		// Call with parameters count and return values count
		try {
			lua.call(2, 1);
		} catch (final LuaRuntimeException e) {
			Activator.logWarning(Messages.LuaSourceFormatDepthError, e);
			return 0;
		}
		final int result = lua.toInteger(-1);
		lua.close();
		return result > 0 ? result - 1 : result;
	}

	/**
	 * Indents Lua source code
	 * 
	 * @param source
	 *            Lua code to indent
	 * @param delimiter
	 *            Line delimiter, <code>\n</code> for Linux and Unix
	 * @param tabulation
	 *            String used as tabulation, it could be one or several white space character like <code>' '</code> of <code>'\t'</code>
	 * @param originalIndentationLevel
	 *            Indicates original semantic depth, useful for selections
	 * @return Indented Lua source code
	 */
	public static String indent(final String source, final String delimiter, final String tabulation, final int originalIndentationLevel) {
		// Load function
		final LuaState lua = loadState();
		lua.getField(-1, INDENTATION_FUNTION);
		lua.pushString(source);
		lua.pushString(delimiter);
		lua.pushString(tabulation);
		lua.pushInteger(originalIndentationLevel);
		try {
			lua.call(4, 1);
		} catch (final LuaRuntimeException e) {
			Activator.logWarning(Messages.LuaSourceFormatIndentationError, e);
			return source;
		}
		final String formattedCode = lua.toString(-1);
		lua.close();
		return formattedCode;
	}

	/**
	 * Indent Lua source code mixing tabulation and spaces. It will indent with space and reach indentation size with spaces.
	 * 
	 * @param source
	 *            Lua Source code to indent
	 * @param delimiter
	 *            Line delimiter, <code>\n</code> for Linux and Unix
	 * @param tabSize
	 *            Count of spaces a tabulation mean
	 * @param indentationSizeCount
	 *            of spaces an indentation mean
	 * @param originalInentationLevel
	 *            Indicates original semantic depth, useful for selections
	 * @return indented Lua source code
	 * @see #indent(String, String, String, int)
	 */
	public static String indent(final String source, final String delimiter, final int tabSize, final int indentationSize,
			final int originalInentationLevel) {
		final LuaState lua = loadState();
		lua.getField(-1, INDENTATION_FUNTION);
		lua.pushString(source);
		lua.pushString(delimiter);
		lua.pushInteger(tabSize);
		lua.pushInteger(indentationSize);
		lua.pushInteger(originalInentationLevel);
		try {
			lua.call(5, 1);
		} catch (final LuaRuntimeException e) {
			Activator.logWarning(Messages.LuaSourceFormatIndentationError, e);
			return source;
		}
		final String formattedCode = lua.toString(-1);
		lua.close();
		return formattedCode;
	}

	private static LuaState loadState() {
		// Loading LuaState with Metalua capabilities
		LuaState lua = MetaluaStateFactory.newLuaState();
		String path = null;
		// Compute path to library
		try {
			final URL folderUrl = FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getEntry(FORMATTER_PATH));
			final File folder = new File(folderUrl.getFile());
			path = folder.getPath() + File.separatorChar;
		} catch (IOException e) {
			Activator.logError(Messages.LuaSourceFormatUnableToLoad + FORMATTER_PATH, e);
		}
		// Updating path to enable formatter library loading
		String code = "package.path = [[" + path + "?.lua;]] .. package.path"; //$NON-NLS-1$ //$NON-NLS-2$
		lua.load(code, "UpdatingFormatterPath"); //$NON-NLS-1$
		lua.call(0, 0);

		// Loading formatter library
		lua.getGlobal("require"); //$NON-NLS-1$
		lua.pushString(FORMATTER_LIB_NAME);
		lua.call(1, 1);
		return lua;
	}
}

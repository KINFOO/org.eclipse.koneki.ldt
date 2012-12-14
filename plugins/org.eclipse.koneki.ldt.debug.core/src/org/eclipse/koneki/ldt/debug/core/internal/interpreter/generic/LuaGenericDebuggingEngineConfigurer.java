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
package org.eclipse.koneki.ldt.debug.core.internal.interpreter.generic;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.dltk.launching.debug.DbgpConnectionConfig;
import org.eclipse.koneki.ldt.debug.core.internal.Activator;
import org.eclipse.koneki.ldt.debug.core.internal.LuaDebugConstants;

public class LuaGenericDebuggingEngineConfigurer extends LuaGenericInterpreterConfigurer {

	private InterpreterConfig initialConfig;

	@Override
	public InterpreterConfig alterConfig(final ILaunch launch, final InterpreterConfig config, final IInterpreterInstall interpreterinstall)
			throws CoreException {
		// In debug engine the config must not be alter, surely because it is used as key to retreive the DBGPConnectionConfig.
		initialConfig = config;
		InterpreterConfig interpreterConfig = (InterpreterConfig) config.clone();
		return super.alterConfig(launch, interpreterConfig, interpreterinstall);
	}

	protected List<IPath> getLuaPath(ILaunch launch, InterpreterConfig config) throws CoreException {
		List<IPath> luaPath = super.getLuaPath(launch, config);

		// add debugger path to lua path
		try {
			URL debuggerEntry = Activator.getDefault().getBundle().getEntry(LuaDebugConstants.DEBUGGER_PATH);
			File debuggerFolder = new File(FileLocator.toFileURL(debuggerEntry).getFile());
			luaPath.add(new Path(debuggerFolder.getPath()));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Unable to extract debugger files.")); //$NON-NLS-1$
		}

		return luaPath;
	}

	@Override
	protected void addCommands(List<String> commandList, ILaunch launch, InterpreterConfig config) throws CoreException {
		// add debugger command to existing ones
		super.addCommands(commandList, launch, config);
		commandList.add(createRunDebuggerCommand(launch, config));
	}

	protected String createRunDebuggerCommand(ILaunch launch, InterpreterConfig config) {

		// get the dbgp connection corresponding to this config.
		DbgpConnectionConfig dbgpConnectionConfig = DbgpConnectionConfig.load(initialConfig);
		String host = "127.0.0.1"; //$NON-NLS-1$
		int port = dbgpConnectionConfig.getPort();
		String sessionId = dbgpConnectionConfig.getSessionId();
		IPath workingDirectory = config.getWorkingDirectoryPath();
		String oS = Platform.getOS();
		String transportLayer = getTransportLayer();

		// create command
		StringBuilder command = new StringBuilder();
		command.append("require ('debugger')"); //$NON-NLS-1$
		command.append("("); //$NON-NLS-1$
		// HOST
		command.append("'").append(host).append("'"); //$NON-NLS-1$//$NON-NLS-2$
		command.append(","); //$NON-NLS-1$
		// PORT
		command.append(port);
		command.append(","); //$NON-NLS-1$
		// SESSION ID
		command.append("'").append(sessionId).append("'");//$NON-NLS-1$//$NON-NLS-2$
		command.append(","); //$NON-NLS-1$
		// TRANSPORT LAYER
		if (transportLayer == null)
			command.append("nil");//$NON-NLS-1$
		else
			command.append("'").append(transportLayer).append("'");//$NON-NLS-1$ //$NON-NLS-2$
		command.append(","); //$NON-NLS-1$
		// PLATFORM
		if (oS.equals(Platform.OS_WIN32))
			command.append("'win'");//$NON-NLS-1$
		else
			command.append("'unix'");//$NON-NLS-1$
		command.append(","); //$NON-NLS-1$
		// WORKING DIRECTORY
		if (workingDirectory.isEmpty())
			command.append("nil");//$NON-NLS-1$
		else
			command.append("'").append(workingDirectory.toOSString()).append("'");//$NON-NLS-1$//$NON-NLS-2$
		command.append(");"); //$NON-NLS-1$

		return command.toString();
	}

	protected String getTransportLayer() {
		return null;
	}
}

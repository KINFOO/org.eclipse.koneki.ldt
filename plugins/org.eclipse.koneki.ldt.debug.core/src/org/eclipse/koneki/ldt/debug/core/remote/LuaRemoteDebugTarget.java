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
package org.eclipse.koneki.ldt.debug.core.remote;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.dltk.dbgp.exceptions.DbgpException;
import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
import org.eclipse.dltk.debug.core.IDbgpService;
import org.eclipse.dltk.debug.core.model.IScriptBreakpointPathMapper;
import org.eclipse.dltk.debug.core.model.IScriptDebugThreadConfigurator;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget;
import org.eclipse.dltk.internal.debug.core.model.ScriptThread;
import org.eclipse.dltk.internal.debug.core.model.operations.DbgpDebugger;
import org.eclipse.dltk.internal.launching.LaunchConfigurationUtils;
import org.eclipse.koneki.ldt.debug.core.Activator;
import org.eclipse.koneki.ldt.debug.core.LuaAbsoluteFileURIBreakpointPathMapper;
import org.eclipse.koneki.ldt.debug.core.LuaDebugConstant;
import org.eclipse.koneki.ldt.debug.core.LuaDebugTarget;
import org.eclipse.koneki.ldt.debug.core.LuaModuleURIBreakpointPathMapper;

public abstract class LuaRemoteDebugTarget extends LuaDebugTarget {

	public LuaRemoteDebugTarget(String modelId, IDbgpService dbgpService, String sessionId, final ILaunch launch, IProcess process) {
		super(modelId, dbgpService, sessionId, launch, process);

		// initialize DBGP client
		String mappingType = LaunchConfigurationUtils.getString(launch.getLaunchConfiguration(), LuaDebugConstant.ATTR_LUA_SOURCE_MAPPING_TYPE,
				LuaDebugConstant.LOCAL_MAPPING_TYPE);
		if (mappingType.equals(LuaDebugConstant.MODULE_MAPPING_TYPE)) {
			setScriptDebugThreadConfigurator(new IScriptDebugThreadConfigurator() {

				@Override
				public void initializeBreakpoints(IScriptThread thread, IProgressMonitor monitor) {
					// do nothing
				}

				@Override
				public void configureThread(DbgpDebugger engine, ScriptThread scriptThread) {
					String urimode = "module"; //$NON-NLS-1$
					try {
						scriptThread.getDbgpSession().getCoreCommands().setFeature("uri", urimode); //$NON-NLS-1$
					} catch (DbgpException e) {
						Activator.logWarning("Unable to set feature uri to " + urimode, e); //$NON-NLS-1$
					}
				}
			});
		}
	}

	@Override
	public boolean isRemote() {
		return true;
	}

	/**
	 * For Remote Debug Target we show the idekey and the port used as the user must managed this at client side
	 * 
	 * @see org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget#toString()
	 */
	@Override
	public String toString() {
		return "Debugging engine (idekey = " + getSessionId() + ", port =" + DLTKDebugPlugin.getDefault().getDbgpService().getPort() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * @see org.eclipse.koneki.ldt.debug.core.LuaDebugTarget#createPathMapper()
	 */
	@Override
	protected IScriptBreakpointPathMapper createPathMapper() {
		String mappingType = LaunchConfigurationUtils.getString(getLaunch().getLaunchConfiguration(), LuaDebugConstant.ATTR_LUA_SOURCE_MAPPING_TYPE,
				LuaDebugConstant.LOCAL_MAPPING_TYPE);

		if (mappingType.equals(LuaDebugConstant.MODULE_MAPPING_TYPE)) {
			return new LuaModuleURIBreakpointPathMapper(getScriptProject());
		} else if (mappingType.equals(LuaDebugConstant.REPLACE_PATH_MAPPING_TYPE)) {

			return new LuaRemoteBreakpointPathMapper(getScriptProject(), folder());
		} else {
			return new LuaAbsoluteFileURIBreakpointPathMapper();
		}
	}

	/**
	 * This method is abstract as a <strong>workaround</strong>. {@link #createPathMapper()} is called in
	 * {@link ScriptDebugTarget#ScriptDebugTarget(String, IDbgpService, String, ILaunch, IProcess)} before end of current object contruction.
	 * 
	 * @return folder name on remote
	 */
	protected abstract String folder();
}

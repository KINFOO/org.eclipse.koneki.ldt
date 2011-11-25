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
package org.eclipse.koneki.ldt.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.URIUtil;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.IExternalSourceModule;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IParent;
import org.eclipse.dltk.core.IProjectFragment;
import org.eclipse.dltk.core.IScriptFolder;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
import org.eclipse.koneki.ldt.Activator;

/**
 * Utility class for Lua
 */
public final class LuaUtils {

	private LuaUtils() {
	}

	/**
	 * @return full name of a module with dot syntax <br>
	 * 
	 *         e.g. : socket.core
	 */
	public static String getModuleFullName(IModuleSource module) {
		IModelElement modelElement = module.getModelElement();
		if (modelElement instanceof ISourceModule) {
			return getModuleFullName((ISourceModule) modelElement);
		} else {
			return module.getFileName();
		}
	}

	/**
	 * @return full name of a module with dot syntax <br>
	 * 
	 *         e.g. : socket.core
	 */
	public static String getModuleFullName(ISourceModule module) {
		// get module name
		String moduleName = module.getElementName();
		if (moduleName.endsWith(".lua")) { //$NON-NLS-1$
			moduleName = moduleName.replaceFirst("\\.lua$", ""); //$NON-NLS-1$//$NON-NLS-2$
		}

		// get prefix
		String prefix = null;
		if (module.getParent() instanceof IScriptFolder) {
			prefix = getFolderFullName((IScriptFolder) module.getParent());
		}

		if (prefix != null)
			if ("init".equalsIgnoreCase(moduleName))//$NON-NLS-1$
				return prefix;
			else
				return prefix + "." + moduleName; //$NON-NLS-1$
		else
			return moduleName;
	}

	/**
	 * @return the source folder full name with module dot syntax
	 */
	private static String getFolderFullName(IScriptFolder folder) {
		if (!folder.isRootFolder()) {
			// get folder name
			String folderName = folder.getElementName().replace("/", "."); //$NON-NLS-1$//$NON-NLS-2$

			// get prefix
			IModelElement parent = folder.getParent();
			String prefix = null;
			if (parent instanceof IScriptFolder) {
				prefix = getFolderFullName((IScriptFolder) parent) + "."; //$NON-NLS-1$
			}

			if (prefix != null)
				return prefix + "." + folderName; //$NON-NLS-1$
			else
				return folderName;
		}
		return null;
	}

	/**
	 * @return the {@link IModuleSource} from full name with module dot syntax
	 */
	public static IModuleSource getModuleSource(String name, IScriptProject project) {
		if (project == null && name == null || name.isEmpty())
			return null;

		// search in all source path.
		IProjectFragment[] allProjectFragments;
		try {
			allProjectFragments = project.getAllProjectFragments();
			for (IProjectFragment projectFragment : allProjectFragments) {
				IModuleSource moduleSource = getModuleSource(name, projectFragment);
				if (moduleSource != null)
					return moduleSource;
			}
		} catch (ModelException e) {
			Activator.logError("unable to find module :" + name, e); //$NON-NLS-1$
			return null;
		}
		return null;
	}

	/**
	 * @return the {@link IModuleSource} from full name with module dot syntax
	 */
	private static IModuleSource getModuleSource(String name, IParent parent) throws ModelException {
		IModelElement[] children = parent.getChildren();
		for (IModelElement child : children) {
			if (child instanceof IModuleSource) {
				if (name.equals(getModuleFullName((IModuleSource) child))) {
					return (IModuleSource) child;
				}
			} else if (child instanceof IParent) {
				IModuleSource moduleSource = getModuleSource(name, (IParent) child);
				if (moduleSource != null)
					return moduleSource;
			}

		}
		return null;
	}

	/**
	 * @return the {@link ISourceModule} from full name with module dot syntax
	 */
	public static ISourceModule getSourceModule(String name, IScriptProject project) {
		IModuleSource moduleSource = getModuleSource(name, project);
		if (moduleSource instanceof ISourceModule) {
			return (ISourceModule) moduleSource;
		}
		return null;
	}

	/**
	 * @return the {@link IModuleSource} from Absolute local file URI
	 */
	public static IModuleSource getModuleSourceFromAbsoluteURI(URI absolutepath, IScriptProject project) {
		if (project == null || absolutepath == null)
			return null;

		ISourceModule sourceModule = getSourceModuleFromAbsoluteURI(absolutepath, project);
		if (sourceModule instanceof IModuleSource) {
			return (IModuleSource) sourceModule;
		}
		return null;
	}

	/**
	 * @return the {@link ISourceModule} from Absolute local file URI
	 */
	public static ISourceModule getSourceModuleFromAbsoluteURI(URI absolutepath, IScriptProject project) {
		if (project == null || absolutepath == null)
			return null;

		// search in all source path.
		IProjectFragment[] allProjectFragments;
		try {
			allProjectFragments = project.getAllProjectFragments();
			for (IProjectFragment projectFragment : allProjectFragments) {
				ISourceModule moduleSource = getSourceModuleFromAbsolutePath(absolutepath, projectFragment);
				if (moduleSource != null)
					return moduleSource;
			}
		} catch (ModelException e) {
			Activator.logError("unable to find module :" + absolutepath, e); //$NON-NLS-1$
			return null;
		}
		return null;
	}

	/**
	 * @return the {@link ISourceModule} from Absolute local file URI and a parent
	 */
	private static ISourceModule getSourceModuleFromAbsolutePath(URI absolutepath, IParent parent) throws ModelException {
		IModelElement[] children = parent.getChildren();
		for (IModelElement child : children) {
			if (child instanceof ISourceModule) {
				if (URIUtil.sameURI(absolutepath, getModuleAbsolutePath((ISourceModule) child))) {
					return (ISourceModule) child;
				}
			} else if (child instanceof IParent) {
				ISourceModule moduleSource = getSourceModuleFromAbsolutePath(absolutepath, (IParent) child);
				if (moduleSource != null)
					return moduleSource;
			}

		}
		return null;
	}

	/**
	 * @return Absolute local file URI of a module source
	 */
	public static URI getModuleAbsolutePath(ISourceModule module) {
		if (module instanceof IExternalSourceModule) {
			String path = EnvironmentPathUtils.getLocalPath(module.getPath()).toString();
			if (path.length() != 0 && path.charAt(0) != '/') {
				path = '/' + path;
			}
			try {
				return new URI("file", "", path, null); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (URISyntaxException e) {
				Activator.logWarning("Unable to get file uri for external module : " + module.getPath(), e); //$NON-NLS-1$
			}
		} else {
			if (module.getResource() != null)
				return module.getResource().getLocationURI();
		}
		return null;
	}
}

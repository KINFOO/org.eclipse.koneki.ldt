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
package org.eclipse.koneki.ldt.wizards.pages;

import org.eclipse.dltk.ui.wizards.ProjectWizardFirstPage;
import org.eclipse.koneki.ldt.core.buildpath.LuaExecutionEnvironment;
import org.eclipse.swt.widgets.Composite;

/**
 * A restriction of {@link ProjectWizardFirstPage}, remove useless UI elements:
 * <ul>
 * <li>interpreters</li>
 * <li>hosts</li>
 * </ul>
 */
public class LuaProjectSettingsPage extends ProjectWizardFirstPage
// implements Observer
{

	private LuaExecutionEnvironmentGroup luaExecutionEnvironmentGroup;

	public LuaProjectSettingsPage() {
		setTitle(Messages.LuaProjecSettingsPageLabel);
	}

	/**
	 * Interpreters are not supported yet. Related UI options and functionality should be disabled.
	 * 
	 * @see ProjectWizardFirstPage#supportInterpreter()
	 * @return false
	 */
	@Override
	protected boolean supportInterpreter() {
		return false;
	}

	/**
	 * Provide specific {@link LocationGroup} in order to avoid UI section dealing with Host, as no host is currently used.
	 * 
	 * @see ProjectWizardFirstPage#createLocationGroup()
	 */
	@Override
	protected LocationGroup createLocationGroup() {
		return new LuaLocationGroup();
	}

	protected void createCustomGroups(final Composite composite) {
		luaExecutionEnvironmentGroup = createExecutionEnvironmentGroup(composite);
	}

	protected LuaExecutionEnvironmentGroup createExecutionEnvironmentGroup(final Composite composite) {
		final LuaExecutionEnvironmentGroup eeGroup = new LuaExecutionEnvironmentGroup(composite);
		// Listen to Execution Environment list changes
		// eeGroup.addObserver(this);
		return eeGroup;
	}

	/**
	 * Just disable <i>Host</i> section defined in {@link LuaLocationGroup#fEnvironment}
	 * 
	 * @see LuaLocationGroup#fEnvironment
	 */
	public class LuaLocationGroup extends LocationGroup {
		@Override
		protected void createEnvironmentControls(final Composite group, final int numColumns) {
		}
	}

	public LuaExecutionEnvironment getExecutionEnvironment() {
		if (luaExecutionEnvironmentGroup != null)
			return luaExecutionEnvironmentGroup.getSelectedLuaExecutionEnvironment();
		return null;
	}

	// @Override
	// public boolean isPageComplete() {
	// return super.isPageComplete() && getExecutionEnvironment() != null;
	// }
	//
	// @Override
	// public void update(Observable o, Object arg) {
	// getContainer().updateButtons();
	// validateProject();
	// }
}
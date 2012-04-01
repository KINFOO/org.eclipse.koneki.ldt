/*******************************************************************************
 * Copyright (c) 2012 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - initial API and implementation
 *******************************************************************************/
package org.eclipse.koneki.ldt.editor.tests;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.koneki.ldt.editor.internal.tests.LuaWordFinderTest;

/**
 * The Class Suite, groups all {@link TestCase} for {@link LuaEditor}
 */
public class Suite extends TestSuite {

	/**
	 * Instantiates a new suite registering all {@link TestCase} of the plug-in.
	 */
	public Suite() {
		setName("Lua Editor"); //$NON-NLS-1$
		addTestSuite(LuaWordFinderTest.class);
	}
}

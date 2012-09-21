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
package org.eclipse.koneki.ldt.ui.wizards.pages;

import java.util.List;
import java.util.Observable;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.koneki.ldt.core.internal.buildpath.LuaExecutionEnvironment;
import org.eclipse.koneki.ldt.core.internal.buildpath.LuaExecutionEnvironmentConstants;
import org.eclipse.koneki.ldt.core.internal.buildpath.LuaExecutionEnvironmentManager;
import org.eclipse.koneki.ldt.ui.internal.buildpath.LuaExecutionEnvironmentContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class LuaExecutionEnvironmentGroup extends Observable {

	private final ComboViewer installedEEsComboViewer;
	private boolean isListAvailable = false;
	private ISelection selection;
	private final Button eeButton;
	private final Button noEEButton;
	private boolean hasToCreateMain = true;

	/**
	 * Will make {@link #installedEEsComboViewer} available only when {@link #eeButton} is checked
	 * 
	 * @see Button#getSelection()
	 */
	private final SelectionListener eeChoiceListener = new SelectionListener() {

		@Override
		public void widgetSelected(SelectionEvent e) {
			widgetDefaultSelected(e);
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			if (eeButton.getSelection()) {
				isListAvailable = true;

			} else if (noEEButton.getSelection()) {
				isListAvailable = false;
			}
			installedEEsComboViewer.getCombo().setEnabled(isListAvailable);
		}
	};
	private Button mainCheckBox;

	public LuaExecutionEnvironmentGroup(final Composite parent) {
		// Create group
		final Group group = new Group(parent, SWT.NONE);
		group.setText(Messages.LuaExecutionEnvironmentGroupTitle);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).applyTo(group);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(group);

		// Button for no Execution Environment at project creation
		noEEButton = new Button(group, SWT.RADIO);
		noEEButton.setText(Messages.LuaExecutionEnvironmentGroupNoEEForProjectCreation);
		noEEButton.addSelectionListener(eeChoiceListener);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(noEEButton);

		// Button for no Execution Environment at project creation
		eeButton = new Button(group, SWT.RADIO);
		eeButton.setText(Messages.LuaExecutionEnvironmentGroupSelectEE);
		eeButton.addSelectionListener(eeChoiceListener);

		// Execution Environment actual list
		installedEEsComboViewer = new ComboViewer(group, SWT.READ_ONLY | SWT.BORDER);
		installedEEsComboViewer.setContentProvider(new LuaExecutionEnvironmentContentProvider());
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.BEGINNING).grab(true, false).applyTo(installedEEsComboViewer.getControl());
		updateExecutionEnvironmentList();

		// Set link to define a new execution environment
		final Link link = new Link(group, SWT.NONE);
		link.setFont(group.getFont());
		link.setText("<a>" + Messages.LuaExecutionEnvironmentGroupManageExecutionEnvironment + "</a>"); //$NON-NLS-1$  //$NON-NLS-2$
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(link);

		// Should we create a main.lua
		mainCheckBox = new Button(group, SWT.CHECK);
		mainCheckBox.setText(Messages.LuaExecutionEnvironmentGroupMainLabel);
		mainCheckBox.setSelection(hasToCreateMain);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(mainCheckBox);
		mainCheckBox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				widgetDefaultSelected(e);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				hasToCreateMain = mainCheckBox.getSelection();
			}
		});

		// Refresh list after user went to Execution Environment preferences
		link.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				final String pageId = LuaExecutionEnvironmentConstants.PREFERENCE_PAGE_ID;
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), pageId, new String[] { pageId }, null).open();
				updateExecutionEnvironmentList();
			}
		});

	}

	/**
	 * @return {@link LuaExecutionEnvironmentConstants} when one is selected in enabled list
	 */
	public LuaExecutionEnvironment getSelectedLuaExecutionEnvironment() {

		// No Execution Environment will be provided when list is not available
		if (isListAvailable) {
			// Secure selection and Execution Environment list status retrieval
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					if (installedEEsComboViewer != null) {
						selection = installedEEsComboViewer.getSelection();
					} else {
						selection = null;
					}
				}
			});

			// Extract Execution Environment from selection
			if ((selection != null) && !selection.isEmpty() && (selection instanceof IStructuredSelection)) {
				return (LuaExecutionEnvironment) ((IStructuredSelection) selection).getFirstElement();
			}
		}
		return null;
	}

	public boolean hasToCreateMain() {
		return hasToCreateMain;
	}

	private void updateExecutionEnvironmentList() {
		if (installedEEsComboViewer != null && eeButton != null && noEEButton != null) {
			final List<LuaExecutionEnvironment> installedExecutionEnvironments = LuaExecutionEnvironmentManager.getAvailableExecutionEnvironments();
			installedEEsComboViewer.setInput(installedExecutionEnvironments);

			// Select first execution environment when available
			if (installedExecutionEnvironments.size() > 0) {
				installedEEsComboViewer.setSelection(new StructuredSelection(installedExecutionEnvironments.get(0)));
				eeButton.setEnabled(true);
				eeButton.setSelection(true);
				noEEButton.setSelection(false);

				eeChoiceListener.widgetDefaultSelected(null);
			} else {
				eeButton.setEnabled(false);
				noEEButton.setSelection(true);
			}

			// Ask for page reload
			setChanged();
			notifyObservers();
		}
	}
}

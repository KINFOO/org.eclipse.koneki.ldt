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
package org.eclipse.koneki.ldt.ui.preferences;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.koneki.ldt.core.buildpath.LuaExecutionEnvironment;
import org.eclipse.koneki.ldt.core.buildpath.LuaExecutionEnvironmentConstants;
import org.eclipse.koneki.ldt.core.buildpath.LuaExecutionEnvironmentManager;
import org.eclipse.koneki.ldt.core.buildpath.exceptions.LuaExecutionEnvironmentException;
import org.eclipse.koneki.ldt.ui.Activator;
import org.eclipse.koneki.ldt.ui.SWTUtil;
import org.eclipse.koneki.ldt.ui.buildpath.LuaExecutionEnvironmentContentProvider;
import org.eclipse.koneki.ldt.ui.buildpath.LuaExecutionEnvironmentLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class LuaExecutionEnvironmentPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private TreeViewer eeTreeViewer;
	private Button removeButton;

	public LuaExecutionEnvironmentPreferencePage() {
		setDescription(Messages.LuaExecutionEnvironmentPreferencePageTitle);
		noDefaultAndApplyButton();
	}

	@Override
	public void init(IWorkbench workbench) {

	}

	@Override
	protected Control createContents(Composite parent) {
		// ----------------
		// CREATE CONTROL
		// create container composite
		Composite containerComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(containerComposite);

		eeTreeViewer = new TreeViewer(containerComposite);
		eeTreeViewer.setContentProvider(new LuaExecutionEnvironmentContentProvider());
		eeTreeViewer.setLabelProvider(new LuaExecutionEnvironmentLabelProvider());
		eeTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (removeButton == null)
					return;
				removeButton.setEnabled(true);
			}
		});
		GridDataFactory.fillDefaults().grab(true, true).applyTo(eeTreeViewer.getControl());

		// create buttons
		Composite buttonsComposite = new Composite(containerComposite, SWT.NONE);
		GridDataFactory.fillDefaults().applyTo(buttonsComposite);
		RowLayoutFactory.fillDefaults().type(SWT.VERTICAL).fill(true).applyTo(buttonsComposite);
		// Add
		Button addButton = new Button(buttonsComposite, SWT.None);
		RowDataFactory.swtDefaults().hint(SWTUtil.getButtonWidthHint(addButton), -1).applyTo(addButton);
		addButton.setText(Messages.LuaExecutionEnvironmentPreferencePage_addbutton);
		// Remove
		removeButton = new Button(buttonsComposite, SWT.None);
		removeButton.setEnabled(false);
		RowDataFactory.swtDefaults().hint(SWTUtil.getButtonWidthHint(removeButton), -1).applyTo(removeButton);
		removeButton.setText(Messages.LuaExecutionEnvironmentPreferencePage_removeButton);

		// ----------------
		// ADD LISTENERS
		addButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				doAddButtonSelection(e);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		removeButton.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(final SelectionEvent e) {
				widgetDefaultSelected(e);
			}

			@Override
			public void widgetDefaultSelected(final SelectionEvent e) {
				doRemoveSelection(e);
			}
		});

		// ----------------
		// SET INPUT
		List<LuaExecutionEnvironment> installedExecutionEnvironments = LuaExecutionEnvironmentManager.getInstalledExecutionEnvironments();
		eeTreeViewer.setInput(installedExecutionEnvironments);

		return containerComposite;
	}

	private void doAddButtonSelection(SelectionEvent se) {
		/*
		 * Ask user for a file
		 */
		FileDialog filedialog = new FileDialog(Display.getDefault().getActiveShell());
		filedialog.setFilterExtensions(new String[] { LuaExecutionEnvironmentConstants.FILE_EXTENSION });
		final String selectedFilePath = filedialog.open();
		if (selectedFilePath == null) {
			return;
		}

		/*
		 * Deploy
		 */
		try {
			LuaExecutionEnvironmentManager.installLuaExecutionEnvironment(selectedFilePath).getEEIdentifier();

			// Refresh the treeviewer
			refreshExecutionEnvironmentList();
		} catch (FileNotFoundException e) {
			final Status status = new Status(Status.INFO, Activator.PLUGIN_ID, e.getMessage());
			ErrorDialog.openError(filedialog.getParent(), Messages.LuaExecutionEnvironmentPreferencePageIOProblemTitle,
					Messages.LuaExecutionEnvironmentPreferencePageProblemWithFile, status);
		} catch (LuaExecutionEnvironmentException e) {
			final Status status = new Status(Status.INFO, Activator.PLUGIN_ID, e.getMessage());
			ErrorDialog.openError(filedialog.getParent(), Messages.LuaExecutionEnvironmentPreferencePageUnableToInstallTitle,
					Messages.LuaExecutionEnvironmentPreferencePageInvalidFile, status);
		} catch (IOException e) {
			final Status status = new Status(Status.ERROR, Activator.PLUGIN_ID, e.getMessage());
			ErrorDialog.openError(filedialog.getParent(), Messages.LuaExecutionEnvironmentPreferencePageUnableToInstallTitle,
					Messages.LuaExecutionEnvironmentPreferencePageInstallationAborted, status);
		}
	}

	private void doRemoveSelection(final SelectionEvent event) {
		/*
		 * Extract selected Execution Environment
		 */
		if (eeTreeViewer == null)
			return;
		final ISelection selection = eeTreeViewer.getSelection();
		if (selection == null) {
			final IStatus status = new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.LuaExecutionEnvironmentPreferencePageNoCurrentSelection);
			ErrorDialog.openError(getShell(), Messages.LuaExecutionEnvironmentPreferencePageRemoveDialogTitle,
					Messages.LuaExecutionEnvironmentPreferencePageNoEESelectted, status);
			Activator.log(status);
			return;
		}
		LuaExecutionEnvironment ee = null;
		if (selection instanceof StructuredSelection) {
			final StructuredSelection sSelection = (StructuredSelection) selection;
			final Object currentSelection = sSelection.getFirstElement();
			if (currentSelection instanceof LuaExecutionEnvironment)
				ee = (LuaExecutionEnvironment) currentSelection;
		}
		if (ee == null)
			return;
		try {
			// Remove selected Execution Environment
			LuaExecutionEnvironmentManager.removeLuaExecutionEnvironment(ee);
			refreshExecutionEnvironmentList();
		} catch (final LuaExecutionEnvironmentException e) {
			final IStatus status = new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.LuaExecutionEnvironmentPreferencePageUnableToDelete, e);
			ErrorDialog.openError(getShell(), Messages.LuaExecutionEnvironmentPreferencePageRemoveDialogTitle,
					Messages.LuaExecutionEnvironmentPreferencePageUnableToDelete, status);
			Activator.log(status);
		}
	}

	private void refreshExecutionEnvironmentList() {
		if (eeTreeViewer == null)
			return;
		final List<LuaExecutionEnvironment> installedExecutionEnvironments = LuaExecutionEnvironmentManager.getInstalledExecutionEnvironments();
		eeTreeViewer.setInput(installedExecutionEnvironments);
	}
}

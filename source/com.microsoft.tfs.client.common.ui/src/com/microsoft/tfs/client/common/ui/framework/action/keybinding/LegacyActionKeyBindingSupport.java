// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the repository root.

package com.microsoft.tfs.client.common.ui.framework.action.keybinding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ActiveShellExpression;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.handlers.IHandlerActivation;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.expressions.ActivePartExpression;
import org.eclipse.ui.keys.IBindingService;

import com.microsoft.tfs.util.Check;

public class LegacyActionKeyBindingSupport implements ActionKeyBindingSupport {
    private final IBindingService bindingService;
    private final IHandlerService handlerService;
    private final Expression expression;
    private final List handlerActivations = new ArrayList();

    public LegacyActionKeyBindingSupport(final IWorkbench workbench, final IWorkbenchPart part) {
        this(workbench, part.getSite().getShell(), part.getSite());
    }

    public LegacyActionKeyBindingSupport(final IWorkbench workbench, final Shell shell) {
        this(workbench, shell, null);
    }

    private LegacyActionKeyBindingSupport(
        final IWorkbench workbench,
        final Shell shell,
        final IWorkbenchPartSite site) {
        Check.notNull(workbench, "workbench"); //$NON-NLS-1$
        Check.notNull(shell, "shell"); //$NON-NLS-1$

        // Get services from workbench
        bindingService = (IBindingService) workbench.getAdapter(IBindingService.class);
        handlerService = (IHandlerService) workbench.getAdapter(IHandlerService.class);

        if (bindingService == null || handlerService == null) {
            throw new IllegalArgumentException("workbench could not provide IBindingService or IHandlerService"); //$NON-NLS-1$
        }

        // Set up expression based on whether we have a site or just a shell
        if (site != null) {
            final IWorkbenchPart part = site.getPart();
            this.expression = new ActivePartExpression(part);
        } else {
            this.expression = new ActiveShellExpression(shell);
        }
    }

    @Override
    public void addAction(final IAction action) {
        Check.notNull(action, "action"); //$NON-NLS-1$

        final String commandId = action.getActionDefinitionId();

        if (commandId == null) {
            throw new IllegalArgumentException("action does not have an action definition ID set"); //$NON-NLS-1$
        }

        final TriggerSequence[] bindings = bindingService.getActiveBindingsFor(commandId);

        if (bindings.length > 0) {
            final IHandler handler = new ActionHandler(action);
            final IHandlerActivation activation =
                handlerService.activateHandler(commandId, handler, expression);
            handlerActivations.add(activation);
        }
    }

    @Override
    public void dispose() {
        for (final Iterator it = handlerActivations.iterator(); it.hasNext();) {
            final IHandlerActivation activation = (IHandlerActivation) it.next();
            handlerService.deactivateHandler(activation);
            activation.getHandler().dispose();
        }
        handlerActivations.clear();
    }
}

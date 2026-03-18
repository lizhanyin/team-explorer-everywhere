// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See License.txt in the repository root.

package com.microsoft.tfs.client.common.ui.framework.help;

import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class ContextSensitiveHelp {
    public synchronized static void setHelp(final Control control, final String contextId) {
        try {
            final IWorkbench workbench = PlatformUI.getWorkbench();

            final Object helpSystem = workbench.getClass().getMethod("getHelpSystem", (Class[]) null).invoke( //$NON-NLS-1$
                workbench,
                (Object[]) null);

            helpSystem.getClass().getMethod("setHelp", new Class[] //$NON-NLS-1$
            {
                Control.class,
                String.class
            }).invoke(helpSystem, new Object[] {
                control,
                contextId
            });
        } catch (final Throwable t) {
            // Help system not available, silently ignore
        }
    }
}

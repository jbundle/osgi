/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.finder;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

public class BundleStartupListener implements BundleListener
{
    BundleContext context = null;

    private Thread thread = null;
    
    Bundle bundle = null;
    
    public BundleStartupListener(Thread thread, BundleContext context, Bundle bundle)
    {
        super();
        this.thread = thread;
        this.context = context;
        this.bundle = bundle;
    }
    @Override
    public void bundleChanged(BundleEvent event) {
        if (event.getBundle() == bundle)
            if (event.getType() == BundleEvent.STARTED)
        {   // Class finder came up
            synchronized (thread)
            {
                thread.notify();
            }
        }
    }
}

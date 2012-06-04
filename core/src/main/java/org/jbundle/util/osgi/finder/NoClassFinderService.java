/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.finder;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;

/**
 * NoClassFinderImpl - If there are no persistent class finders, I have to
 * use this class finder which only looks in the currently installed bundles.
 *
 * @author don
 * 
 */
public class NoClassFinderService extends BaseClassFinderService
	implements BundleActivator
{

	@Override
	public Object deployThisResource(String packageName, String versionRange, boolean start) {
        Bundle bundle = this.findBundle(null, bundleContext, packageName, versionRange);
        if (start)
        	this.startBundle(bundle);
		return null;
	}	

}

/*
 * Copyright © 2011 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.finder;

import org.osgi.framework.BundleActivator;

/**
 * NoClassFinderImpl - If there are not persistent class finders, I have to
 * use this class finder which only looks in the currently installed bundles.
 *
 * @author don
 * 
 */
public class NoClassFinderService extends BaseClassFinderService
	implements BundleActivator
{

	@Override
	public Object deployThisResource(String packageName, String version, boolean start) {
		return null;
	}	

}

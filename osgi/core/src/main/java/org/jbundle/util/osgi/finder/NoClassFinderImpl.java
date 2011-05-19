package org.jbundle.util.osgi.finder;

import org.osgi.framework.BundleActivator;

/**
 * NoClassFinderImpl - If there are not persistent class finders, I have to
 * use this class finder which only looks in the currently installed bundles.
 *
 * @author don
 * 
 */
public class NoClassFinderImpl extends BaseClassFinder
	implements BundleActivator
{

	@Override
	public Object deployThisResource(String className, boolean start,
			boolean resourceType) {
		return null;
	}	

}

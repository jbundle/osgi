/*
 * Copyright © 2012 jbundle.org. All rights reserved.
 */
package org.jbundle.util.osgi.bundle;

import java.util.Dictionary;

import org.jbundle.util.osgi.finder.BaseClassFinderService;
import org.jbundle.util.osgi.finder.ClassFinderActivator;
import org.osgi.framework.BundleContext;

/**
 * This class find and start the bundle that belongs to this class.
 * Note: This is a thread since is is usually takes a while to run.
 * @author don <don@tourgeek.com>
 *
 */
public class BundleStarter extends Thread
{
	BundleContext bundleContext = null;
	String dependentServiceClassName = null;
	String interfaceClassName = null;
	String versionRange = null;
	Dictionary<String, Object> properties = null;
	
	public BundleStarter(BaseBundleActivator bundleService, BundleContext bundleContext, String interfaceClassName, String dependentServiceClassName, String versionRange, Dictionary<String, Object> properties)
	{
		this.bundleContext = bundleContext;
		this.dependentServiceClassName = dependentServiceClassName;
		this.interfaceClassName = interfaceClassName;
		this.versionRange = versionRange;
		this.properties = properties;
	}
	public void run()
	{
		BaseClassFinderService classFinder = (BaseClassFinderService)ClassFinderActivator.getClassFinder(bundleContext, -1);
		classFinder.startBaseBundle(bundleContext, interfaceClassName, dependentServiceClassName, versionRange, null, -1);
	}
}

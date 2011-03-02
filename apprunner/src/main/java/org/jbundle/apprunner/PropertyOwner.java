/**
 * Copyright (c) 2009 jbundle.org. All Rights Reserved.
 *		Don_Corley@msn.com
 */
package org.jbundle.util.apprunner;

import java.util.*;

/**
 * A base interface for a Model, View, or Control that has properties.
 */
public interface PropertyOwner
{
	/**
	 * Set this control up to implement these new properties.
	 */
	public void setProperties(Properties properties);
	/**
	 * Screen that is used to change the properties.
	 */
	public PropertyView getPropertyView(Properties properties);
}

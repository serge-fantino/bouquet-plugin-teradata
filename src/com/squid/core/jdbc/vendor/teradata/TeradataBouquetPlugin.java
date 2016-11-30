package com.squid.core.jdbc.vendor.teradata;

import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ServiceLoader;

import com.squid.core.database.plugins.BaseBouquetPlugin;

public class TeradataBouquetPlugin extends BaseBouquetPlugin {

	@Override
	public void loadDriver() {
		// get the current plugin jar 
		URL[] paths = new URL[1];
		paths[0] = this.getClass().getProtectionDomain().getCodeSource().getLocation();

		// load the driver within an isolated classLoader
		this.driverCL = new URLClassLoader(paths);
		ClassLoader rollback = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(driverCL);

		ServiceLoader<Driver> sl = ServiceLoader.load(java.sql.Driver.class, driverCL) ;
		Iterator<Driver> driversIter = sl.iterator() ;
		this.drivers = new ArrayList<Driver>();
	
		while (driversIter.hasNext()){
			drivers.add(driversIter.next());			
		}
		Thread.currentThread().setContextClassLoader(rollback);
}

}

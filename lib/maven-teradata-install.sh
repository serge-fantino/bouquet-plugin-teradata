#!/bin/sh
# install the Teradata driver in local POM
#
mvn install:install-file -DgroupId=com.teradata.jdbc -DartifactId=tdgssconfig -Dversion=15.10.00.22 -Dpackaging=jar -Dfile=tdgssconfig.jar -DgeneratePom=true 
mvn install:install-file -DgroupId=com.teradata.jdbc -DartifactId=terajdbc4 -Dversion=15.10.00.22 -Dpackaging=jar -Dfile=terajdbc4.jar -DgeneratePom=true 

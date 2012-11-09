#!/bin/bash

export J2EE_HOME=$(echo /localhost/packages/ds/glassfish-*)
nbhome=$(echo /localhost/packages/ds/netbeans-*)
export PATH=$J2EE_HOME/glassfish/bin/:$nbhome/bin:$PATH

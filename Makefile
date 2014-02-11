#!/bin/bash

# if java/javac are not in your ${PATH}, set this line to the directory
# they are located in, with a trailing '/', e.g. 'JAVAPATH=/usr/bin/'
JAVAPATH=

compile: clean
	# set up
	echo "PWD=${PWD}"
	mkdir classes

	# compile	 
	${JAVAPATH}javac -target 1.5 \
	  -cp lib/joeq.jar \
	  -sourcepath src -d classes `find . -name "*.java"`

	# jar it all up
	cd classes; jar cf parun.jar `find . -name "*.class"`
	mv classes/parun.jar lib/parun.jar

	# set up parun
	cat bin/parun-template | sed -e "s|PARUNPATH|${PWD}|g" \
		| sed -e "s|JAVAPATH|${JAVAPATH}|g" > bin/parun
	chmod a+x bin/parun

solvertests: compile cp cp2 lv lv2

cp:  
	rm -rf mysolver.out
	bin/parun flow.Flow submit.MySolver flow.ConstantProp test.Test > mysolver.out
	diff mysolver.out src/test/test.cp.out

cp2:  
	rm -rf mysolver.out
	bin/parun flow.Flow submit.MySolver flow.ConstantProp test.TestTwo > mysolver.out
	diff mysolver.out src/test/test2.cp.out

lv:  
	rm -rf mysolver.out
	bin/parun flow.Flow submit.MySolver flow.Liveness test.Test > mysolver.out
	diff mysolver.out src/test/test.lv.out
lv2:  
	rm -rf mysolver.out
	bin/parun flow.Flow submit.MySolver flow.Liveness test.TestTwo > mysolver.out
	diff mysolver.out src/test/test2.lv.out

clean:
	find . -name '*~' -delete
	find . -name '#*#' -delete
	rm -rf classes
	rm -rf bin/parun
	rm -rf lib/parun.jar
	rm -rf mysolver.out

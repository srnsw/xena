This is the JavaHelp subversion repository.

It contains two separate, but interrelated, source trees:

 JSearch/
	A default search engine conforming to the JavaHelp search API

 JavaHelp/
	The JavaHelp API and its reference implementation.


Sources reside in exactly one of these source trees.  For example, the
javax.javahelp.search package is listed in JavaHelp even though the
JSearch/ subtree requires it.

JSearch must be built first.  JavaHelp requires classes from JSearch
for packaging the final product, although not for compiling it.

Each tree refers, by default, to its sibling, although the Makefiles
have variables that can be used to overwrite this.

Everything must be built using JDK1.2fcs-K or later (note the -target 1.1
flag).

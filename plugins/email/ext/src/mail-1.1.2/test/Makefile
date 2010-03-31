# Test makefile.
empty:=
space:=$(empty) $(empty)

# Set by configure.
PROJECTROOT = .
JAVA = jamvm
JAVAC = javac
JAVAC_OPTS = 
JAR = jar
CLASSPATH = /gnu/share/classpath/glibj.zip

prefix = /gnu/share/java
extra = /gnu/share/java

OPTIONS = -Dmail.pop3.apop=false -Dmail.debug=true -Djavax.net.debug=true \
-Dmail.smtp.host=bluezoo.org -Dmail.smtp.auth=required \
-Djava.util.logging.config.file=logging.properties

all: test

test_sourcedir=$(PROJECTROOT)/src
test_sourcefiles=$(shell find $(test_sourcedir) -name "*.java" -print)
test_classpath=$(prefix)/gnumail.jar $(prefix)/gnumail-providers.jar \
$(prefix)/activation.jar $(prefix)/inetlib.jar \
$(extra)/junit.jar
#$(prefix)/jsse.jar $(prefix)/jessie.jar \
#$(prefix)/javax-security.jar $(prefix)/gnu-crypto.jar \
#$(prefix)/javax-crypto.jar
test_classesdest=$(PROJECTROOT)/classes
test_otherfiles=
test_debugclasses=$(test_sourcefiles:.java=.class)
test_classfiles=$(test_debugclasses:$(test_sourcedir)%=$(test_classesdest)%)

test: test-init $(test_classfiles) test-compilation $(test_otherfiles) $(test_manifest)
	$(JAVA) -cp $(call PATHMK,$(CLASSPATH) $(call PATHMK,$(test_classesdest) $(call PATHMK,$(test_classpath)))) $(OPTIONS) AllTests

.PHONY: test-init test-compilation

test-init:
	echo > filelist

test-compilation: $(test_classesdest)
	$(if $(shell cat filelist),$(JAVAC) $(JAVAC_OPTS) -d $(test_classesdest) -classpath $(call PATHMK,$(test_classesdest) $(call PATHMK,$(test_classpath))) @filelist)

$(test_classesdest):
	mkdir $@

$(test_classesdest)/%.class: $(test_sourcedir)/%.java
	@echo $? >> filelist

echo_test:
	@echo sourcedir = $(test_sourcedir)
	@echo sourcefiles = $(test_sourcefiles)
	@echo classpath = $(call PATHMK,$(test_classpath))
	@echo classesdest = $(test_classesdest)
	@echo classfiles = $(test_classfiles)

ifeq (${OS},Windows_NT)
PATHMK = $(subst $(space),;,$(1))
else
PATHMK = $(subst $(space),:,$(1))
endif
# Ensure everything generated is cleaned.
# We could probably do with automake for this.
clean:
	-rm -rf $(PROJECTROOT)/classes filelist

distclean: clean
	-rm -rf Makefile Makefile.in config.*

mrproper: distclean
	-rm -rf configure  

# End Makefile.

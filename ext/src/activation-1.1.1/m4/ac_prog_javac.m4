dnl Available from the GNU Autoconf Macro Archive at:
dnl http://www.gnu.org/software/ac-archive/htmldoc/ac_prog_javac.html
dnl
AC_DEFUN([AC_PROG_JAVAC],[
AC_REQUIRE([AC_EXEEXT])dnl
if test "x$JAVAPREFIX" = x; then
        test "x$JAVAC" = x && AC_CHECK_PROGS(JAVAC, "gcj$EXEEXT -C" jikes$EXEEXT ejc$EXEEXT guavac$EXEEXT javac$EXEEXT)
else
        test "x$JAVAC" = x && AC_CHECK_PROGS(JAVAC, "gcj$EXEEXT -C" jikes$EXEEXT ejc$EXEEXT guavac$EXEEXT javac$EXEEXT, $JAVAPREFIX)
fi
test "x$JAVAC" = x && AC_MSG_ERROR([no acceptable Java compiler found in \$PATH])
AC_PROG_JAVAC_WORKS
(echo "$JAVAC" | grep -e " -C$" >/dev/null 2>/dev/null) && AC_MSG_WARN([
The build seems to be using gcj for bytecode generation.  Some
versions of gcj are known to produce bad bytecode.  See here for a
list of bugs that may be relevant:

http://gcc.gnu.org/bugzilla/buglist.cgi?component=java&keywords=wrong-code&order=default

At least bug 19921 is known to affect gjdoc (in Feb 2005).

You may want to set the environment variable JAVAC to an alternate
compiler, such as jikes, to make sure that you end up with valid
bytecode.
]);
AC_PROVIDE([$0])dnl
])

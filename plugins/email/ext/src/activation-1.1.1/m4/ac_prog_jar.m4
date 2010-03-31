dnl Available from the GNU Autoconf Macro Archive at:
dnl http://www.gnu.org/software/ac-archive/htmldoc/ac_prog_jar.html
dnl
dnl Changes for gjdoc: 
dnl - check for "fastjar, jar", not only "jar";
dnl - output warning if detected jar is kaffe jar.
dnl
AC_DEFUN([AC_PROG_JAR],[
AC_REQUIRE([AC_EXEEXT])dnl
if test "x$JAVAPREFIX" = x; then
        test "x$JAR" = x && AC_CHECK_PROGS(JAR, fastjar$EXEEXT jar$EXEEXT)
else
        test "x$JAR" = x && AC_CHECK_PROGS(JAR, fastjar$EXEEXT jar$EXEEXT, $JAVAPREFIX)
fi

dnl Complain if not found
test "x$JAR" = "x" && AC_MSG_ERROR([no acceptable jar program found in \$PATH])

dnl Strip any parameters
sed_expr_param=['s/[ ][^"]*$//']
jarabs=`echo $JAR | sed -e "$sed_expr_param"`

dnl Convert ~/ to $HOME/
sed_expr_home="s|^~/|$HOME/|"
jarabs=`echo $jarabs | sed -e "$sed_expr_home"`

dnl If not already absolute filename, find on PATH
sed_expr_abspath=['s/^~?\/.*$//']
if (test `echo $jarabs | sed -e "$sed_expr_abspath"`); then
  dnl Stolen from libtool.m4:
  lt_save_ifs="$IFS"; IFS=$PATH_SEPARATOR
  for dir in $PATH; do
    IFS="$lt_save_ifs"
    if (test -f $dir/$jarabs || test -f $dir/$jarabs$ac_exeext); then
      jarabs="$dir/$jarabs"
      break
    fi
  done
fi

dnl Warn if it's the kaffe jar
grep 'kaffe\.tools\.jar\.Jar' "$jarabs" >/dev/null
test "$?" != "0" || \
  AC_MSG_WARN([
The build seems to be using the Jar tool that comes with Kaffe.  Note
that there are known issues in some versions of this tool.
Unfortunately it does not support any --version option, so I can't
detect whether your version works.

If you see error messages from the Jar tool, or the build hangs,
please set environment variable JAR to a working Jar tool.
])
AC_PROVIDE([$0])dnl
])

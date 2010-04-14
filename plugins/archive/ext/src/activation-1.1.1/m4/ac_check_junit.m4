dnl Available from the GNU Autoconf Macro Archive at:
dnl http://www.gnu.org/software/ac-archive/htmldoc/ac_check_junit.html
dnl
AC_DEFUN([AC_CHECK_JUNIT],[
AC_CACHE_VAL(ac_cv_prog_JUNIT,[
AC_CHECK_CLASS(junit.textui.TestRunner)
if test x"`eval 'echo $ac_cv_class_junit_textui_TestRunner'`" != xno ; then
  ac_cv_prog_JUNIT='$(CLASSPATH_ENV) $(JAVA) $(JAVAFLAGS) junit.textui.TestRunner'
fi])
AC_MSG_CHECKING([for junit])
if test x"`eval 'echo $ac_cv_prog_JUNIT'`" != x ; then
  JUNIT="$ac_cv_prog_JUNIT"
  JAVA_JUNIT='$(JAVA_JUNIT)'
  TESTS_JUNIT='$(TESTS_JUNIT)'
else
  JUNIT=
  JAVA_JUNIT=
  TESTS_JUNIT=
fi
AC_MSG_RESULT($JAVA_JUNIT)
AC_SUBST(JUNIT)
AC_SUBST(JAVA_JUNIT)
AC_SUBST(TESTS_JUNIT)])

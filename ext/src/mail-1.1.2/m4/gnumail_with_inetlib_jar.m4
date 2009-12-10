dnl @synopsis GNUMAIL_WITH_INETLIB_JAR
dnl
AC_DEFUN([GNUMAIL_WITH_INETLIB_JAR],[
AC_ARG_WITH([inetlib_jar],
        AC_HELP_STRING([--with-inetlib-jar=DIR],
                [path to GNU inetlib (inetlib.jar). If unspecified, /usr/share/java and /usr/local/share/java are searched, in that order.]),
        [if test "x${withval}" != x; then
                AC_MSG_CHECKING([for ${withval}/inetlib.jar])
                if test -r ${withval}/inetlib.jar ; then
                        AC_MSG_RESULT(yes)
                        INETLIB_JAR=${withval}/inetlib.jar
                        found_inetlib_jar=true
                else
                        AC_MSG_ERROR([can't find inetlib.jar in specified path])
                fi
        fi],
        [found_inetlib_jar=false])
if test "x${INETLIB_JAR}" = x ;then
        dnl AC_MSG_NOTICE([no value supplied, searching default locations])
        _GNUMAIL_FIND_INETLIB_JAR([/usr/share/java /usr/local/share/java ${prefix}/share/java])
        if test "x${INETLIB_JAR}" = x; then
                AC_MSG_ERROR([can't find inetlib.jar; use --with-inetlib-jar])
        fi
        found_inetlib_jar=true
fi
AC_SUBST(INETLIB_JAR)
])
dnl @synopsis _GNUMAIL_FIND_INETLIB_JAR
dnl
AC_DEFUN([_GNUMAIL_FIND_INETLIB_JAR],[
AC_MSG_CHECKING([for inetlib.jar])
for _F in $1 ;do
        if test -r ${_F}/inetlib.jar ; then
                INETLIB_JAR=${_F}/inetlib.jar
                break
        fi
done
if test x${INETLIB_JAR} = x ; then
        AC_MSG_RESULT([not found])
else
        AC_MSG_RESULT([${INETLIB_JAR}])
fi])

dnl @synopsis GNUMAIL_WITH_ACTIVATION_JAR
dnl
AC_DEFUN([GNUMAIL_WITH_ACTIVATION_JAR],[
AC_ARG_WITH([activation_jar],
        AC_HELP_STRING([--with-activation-jar=DIR],
                [path to JAF (activation.jar). If unspecified, /usr/share/java and /usr/local/share/java are searched, in that order.]),
        [if test "x${withval}" != x ; then
                AC_MSG_CHECKING([for ${withval}/activation.jar])
                if test -r ${withval}/activation.jar ; then
                        AC_MSG_RESULT(yes)
                        ACTIVATION_JAR=${withval}/activation.jar
                        found_activation_jar=true
                else
                        AC_MSG_ERROR([can't find activation.jar in specified path])
                fi
        fi],
        [found_activation_jar=false])
if test "x${ACTIVATION_JAR}" = x ; then
        dnl AC_MSG_NOTICE([no value supplied, searching default locations])
        _GNUMAIL_FIND_ACTIVATION_JAR([/usr/share/java /usr/local/share/java ${prefix}/share/java])
        if test "x${ACTIVATION_JAR}" = x; then
                AC_MSG_ERROR([can't find activation.jar; use --with-activation-jar])
        fi
fi
AC_SUBST(ACTIVATION_JAR)
])
dnl @synopsis _GNUMAIL_FIND_ACTIVATION_JAR
dnl
AC_DEFUN([_GNUMAIL_FIND_ACTIVATION_JAR],[
AC_MSG_CHECKING([for activation.jar])
for _F in $1 ;do
        if test -r ${_F}/activation.jar ; then
                ACTIVATION_JAR=${_F}/activation.jar
                break
        fi
done
if test x${ACTIVATION_JAR} = x ; then
        AC_MSG_RESULT([not found])
else
        AC_MSG_RESULT([${ACTIVATION_JAR}])
fi])

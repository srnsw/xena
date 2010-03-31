dnl @synopsis INETLIB_WITH_AUTH_CALLBACK
dnl
AC_DEFUN([INETLIB_WITH_AUTH_CALLBACK],[
AC_REQUIRE([AC_PROG_JAVAC])dnl
AC_REQUIRE([AC_PROG_JAVA])dnl
AC_MSG_CHECKING([for javax.security.auth.callback])
AUTH_CALLBACK_JAR=""
AUTH_CALLBACK_CLASSPATH=.
if test "x" != "x$CLASSPATH" ; then
        AUTH_CALLBACK_CLASSPATH="$AUTH_CALLBACK_CLASSPATH:$CLASSPATH"
fi
AC_ARG_WITH([auth_callback],
        AC_HELP_STRING([--with-auth-callback=FILE],
                [path to external javax.security.auth.callback library]),
        [
        if test -r "${withval}" ; then
                        AUTH_CALLBACK_JAR="${withval}"
                        AUTH_CALLBACK_CLASSPATH="$AUTH_CALLBACK_CLASSPATH:$AUTH_CALLBACK_JAR"
        fi
        ])
changequote(, )dnl
cat << \EOF > Test.java
/* [#]line __oline__ "configure" */
public class Test {
  public static void main(String[] args) {
    try { Class.forName("javax.security.auth.callback.Callback"); }
    catch (Throwable e) { System.exit(1); }
  }
}
EOF
changequote([, ])dnl
if AC_TRY_COMMAND($JAVAC $JAVACFLAGS Test.java) && test -s Test.class && ($JAVA $JAVAFLAGS -classpath "$AUTH_CALLBACK_CLASSPATH" Test) 2>/dev/null
then
        AC_MSG_RESULT(yes)
else
        AC_MSG_ERROR([can't find javax.security.auth.callback classes; use --with-auth-callback])
fi
rm Test.java Test.class
AC_SUBST(AUTH_CALLBACK_JAR)
])

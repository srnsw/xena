dnl @synopsis INETLIB_WITH_SASL
dnl
AC_DEFUN([INETLIB_WITH_SASL],[
AC_REQUIRE([AC_PROG_JAVAC])dnl
AC_REQUIRE([AC_PROG_JAVA])dnl
AC_MSG_CHECKING([for SASL])
SASL_JAR=""
SASL_CLASSPATH=.
if test "x" != "x$CLASSPATH" ; then
        SASL_CLASSPATH="$SASL_CLASSPATH:$CLASSPATH"
fi
AC_ARG_WITH([sasl],
        AC_HELP_STRING([--with-sasl=FILE], [path to external SASL library]),
        [
        if test -r "${withval}" ; then
                        SASL_JAR="${withval}"
                        SASL_CLASSPATH="$SASL_CLASSPATH:$SASL_JAR"
        fi
        ])
changequote(, )dnl
cat << \EOF > Test.java
/* [#]line __oline__ "configure" */
public class Test {
  public static void main(String[] args) {
    try { Class.forName("javax.security.sasl.SaslClient"); }
    catch (Throwable e) { System.exit(1); }
  }
}
EOF
changequote([, ])dnl
if AC_TRY_COMMAND($JAVAC $JAVACFLAGS Test.java) && test -s Test.class && ($JAVA $JAVAFLAGS -classpath "$SASL_CLASSPATH" Test) 2>/dev/null
then
        AC_MSG_RESULT(yes)
else
        AC_MSG_ERROR([can't find SASL classes; use --with-sasl])
fi
rm Test.java Test.class
AC_SUBST(SASL_JAR)
])

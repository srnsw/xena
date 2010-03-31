dnl @synopsis INETLIB_WITH_JSSE
dnl
AC_DEFUN([INETLIB_WITH_JSSE],[
AC_REQUIRE([AC_PROG_JAVAC])dnl
AC_REQUIRE([AC_PROG_JAVA])dnl
AC_MSG_CHECKING([for JSSE])
JSSE_JAR=""
JSSE_CLASSPATH=.
if test "x" != "x$CLASSPATH" ; then
        JSSE_CLASSPATH="$JSSE_CLASSPATH:$CLASSPATH"
fi
AC_ARG_WITH([jsse],
        AC_HELP_STRING([--with-jsse=FILE], [path to external JSSE library]),
        [
        if test -r "${withval}" ; then
                JSSE_JAR="${withval}"
                JSSE_CLASSPATH="$JSSE_CLASSPATH:$JSSE_JAR"
        fi
        ])
changequote(, )dnl
cat << \EOF > Test.java
/* [#]line __oline__ "configure" */
public class Test {
  public static void main(String[] args) {
    try { Class.forName("javax.net.ssl.SSLSocket"); }
    catch (Throwable e) { System.exit(1); }
  }
}
EOF
changequote([, ])dnl
if AC_TRY_COMMAND($JAVAC $JAVACFLAGS Test.java) && test -s Test.class && ($JAVA $JAVAFLAGS -classpath "$JSSE_CLASSPATH" Test; exit) 2>/dev/null
then
        AC_MSG_RESULT(yes)
else
        AC_MSG_ERROR([can't find JSSE classes; use --with-jsse])
fi
rm Test.java Test.class
AC_SUBST(JSSE_JAR)
])

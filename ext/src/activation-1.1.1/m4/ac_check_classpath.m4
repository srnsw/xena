dnl Available from the GNU Autoconf Macro Archive at:
dnl http://www.gnu.org/software/ac-archive/htmldoc/ac_check_classpath.html
dnl
AC_DEFUN([AC_CHECK_CLASSPATH],[
if test "x$CLASSPATH" = x; then
        echo "You have no CLASSPATH, I hope it is good"
else
        echo "You have CLASSPATH $CLASSPATH, hope it is correct"
fi
])

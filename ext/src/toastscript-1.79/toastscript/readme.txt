
- If Java is installed, run ToastScript by double-clicking
  the archive "toastscript.jar"

- The project file toastscript.jpx is for JBuilder4. If you
  experience problems running ToastScript from within the
  JBuilder environment, check your project properities for
  the correct location of "ps.jar".

- Make sure to set working directory to directory which
  contains gsfonts when started from NetBeans, otherwise
  fonts will not be found.

- Version 1.72 and later has some support for debugging
  PostScript files. Use $$break in your page description to
  enter the debugger. The debugger prompt will be displayed
  in System.out (not the ToastScript console window). Enter
  h<CR> into System.in for a list of commands.

- The source code is best viewed using a tab setting of
  4 spaces, both for java sources and ps (plain text) files.
  If you add code, please make sure to use 4 spaces per
  tab in order to keep the code well formatted and readable.

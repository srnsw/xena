dnl Process this file with autoconf to produce a configure script.

dnl 
dnl $Id$
dnl Copyright (C) 2003 The Free Software Foundation
dnl 
dnl This file is part of GNU classpath inetlib.
dnl 
dnl GNU classpath is free software; you can redistribute it and/or modify it
dnl under the terms of the GNU General Public License as published by the
dnl Free Software Foundation; either version 2, or (at your option) any
dnl later version.
dnl  
dnl GNU classpath is distributed in the hope that it will be useful, but
dnl WITHOUT ANY WARRANTY; without even the implied warranty of
dnl MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
dnl General Public License for more details.
dnl 
dnl You should have received a copy of the GNU General Public License
dnl along with GNU JavaMail; see the file COPYING.  If not, write to the
dnl Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
dnl 02111-1307 USA. 
dnl

AC_INIT(inetlib, 1.1)
AC_CONFIG_SRCDIR(source/gnu/inet/util/LineInputStream.java)
AC_CONFIG_AUX_DIR([config])
AM_INIT_AUTOMAKE

PACKAGE="inetlib"
VERSION="1.1"
AC_SUBST(PACKAGE)
AC_SUBST(VERSION)

AC_ARG_ENABLE(smtp, AC_HELP_STRING([--enable-smtp],
	[Enable the SMTP client (default=yes).]),
	[case "$enableval" in
		no) enable_smtp=no ;;
		*) enable_smtp=yes ;;
	esac], [enable_smtp=yes])
AM_CONDITIONAL(ENABLE_SMTP, test "x$enable_smtp" = "xyes")

AC_ARG_ENABLE(imap, AC_HELP_STRING([--enable-imap],
	[Enable the IMAP client (default=yes).]),
	[case "$enableval" in
		no) enable_imap=no ;;
		*) enable_imap=yes ;;
	esac], [enable_imap=yes])
AM_CONDITIONAL(ENABLE_IMAP, test "x$enable_imap" = "xyes")

AC_ARG_ENABLE(pop3, AC_HELP_STRING([--enable-pop3],
	[Enable the POP3 client (default=yes).]),
	[case "$enableval" in
		no) enable_pop3=no ;;
		*) enable_pop3=yes ;;
	esac], [enable_pop3=yes])
AM_CONDITIONAL(ENABLE_POP3, test "x$enable_pop3" = "xyes")

AC_ARG_ENABLE(nntp, AC_HELP_STRING([--enable-nntp],
	[Enable the NNTP client (default=yes).]),
	[case "$enableval" in
		no) enable_nntp=no ;;
		*) enable_nntp=yes ;;
	esac], [enable_nntp=yes])
AM_CONDITIONAL(ENABLE_NNTP, test "x$enable_nntp" = "xyes")

AC_ARG_ENABLE(ftp, AC_HELP_STRING([--enable-ftp],
	[Enable the FTP client (default=yes).]),
	[case "$enableval" in
		no) enable_ftp=no ;;
		*) enable_ftp=yes ;;
	esac], [enable_ftp=yes])
AM_CONDITIONAL(ENABLE_FTP, test "x$enable_ftp" = "xyes")

AC_ARG_ENABLE(gopher, AC_HELP_STRING([--enable-gopher],
	[Enable the Gopher client (default=yes).]),
	[case "$enableval" in
		no) enable_gopher=no ;;
		*) enable_gopher=yes ;;
	esac], [enable_gopher=yes])
AM_CONDITIONAL(ENABLE_GOPHER, test "x$enable_gopher" = "xyes")

AC_ARG_ENABLE(finger, AC_HELP_STRING([--enable-finger],
	[Enable the finger client (default=yes).]),
	[case "$enableval" in
		no) enable_finger=no ;;
		*) enable_finger=yes ;;
	esac], [enable_finger=yes])
AM_CONDITIONAL(ENABLE_FINGER, test "x$enable_finger" = "xyes")

AC_ARG_ENABLE(http, AC_HELP_STRING([--enable-http],
	[Enable the HTTP client (default=yes).]),
	[case "$enableval" in
		no) enable_http=no ;;
		*) enable_http=yes ;;
	esac], [enable_http=yes])
AM_CONDITIONAL(ENABLE_HTTP, test "x$enable_http" = "xyes")

AC_ARG_ENABLE(ldap, AC_HELP_STRING([--enable-ldap],
	[Enable the experimental LDAP client (default=yes).]),
	[case "$enableval" in
		no) enable_ldap=no ;;
		*) enable_ldap=yes ;;
	esac], [enable_ldap=yes])
AM_CONDITIONAL(ENABLE_LDAP, test "x$enable_ldap" = "xyes")

AC_ARG_ENABLE(comsat, AC_HELP_STRING([--enable-comsat],
	[Enable the experimental comsat client (default=yes).]),
	[case "$enableval" in
		no) enable_comsat=no ;;
		*) enable_comsat=yes ;;
	esac], [enable_comsat=yes])
AM_CONDITIONAL(ENABLE_COMSAT, test "x$enable_comsat" = "xyes")

AC_PROG_JAVAC
AC_PROG_JAR
AC_PROG_JAVADOC
AC_JAVA_OPTIONS
dnl AC_CHECK_CLASSPATH

INETLIB_WITH_JSSE
INETLIB_WITH_SASL
INETLIB_WITH_AUTH_CALLBACK

AC_CONFIG_FILES(Makefile)

AC_OUTPUT


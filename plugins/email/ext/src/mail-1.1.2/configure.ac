# Process this file with autoconf to produce a configure script.

# 
# $Id$
# Copyright (C) 2003 Nic Ferrier
# 
# This file is part of GNU JavaMail.
# 
# GNU JavaMail is free software; you can redistribute it and/or modify it
# under the terms of the GNU General Public License as published by the
# Free Software Foundation; either version 2, or (at your option) any
# later version.
#  
# GNU JavaMail is distributed in the hope that it will be useful, but
# WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with GNU JavaMail; see the file COPYING.  If not, write to the
# Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
# 02110-1301 USA. 
#

AC_INIT(gnumail, 1.1.1)
AC_CONFIG_SRCDIR(source/gnu/mail/providers/ReadOnlyMessage.java)
AM_INIT_AUTOMAKE

PACKAGE="mail"
VERSION="1.1.1"
AC_SUBST(PACKAGE)
AC_SUBST(VERSION)

AC_ARG_ENABLE(smtp, AC_HELP_STRING([--enable-smtp],
	[Enable the SMTP provider (default=yes).]),
	[case "$enableval" in
		no) enable_smtp=no ;;
		*) enable_smtp=yes ;;
	esac], [enable_smtp=yes])
AM_CONDITIONAL(ENABLE_SMTP, test "x$enable_smtp" = "xyes")

AC_ARG_ENABLE(imap, AC_HELP_STRING([--enable-imap],
	[Enable the IMAP provider (default=yes).]),
	[case "$enableval" in
		no) enable_imap=no ;;
		*) enable_imap=yes ;;
	esac], [enable_imap=yes])
AM_CONDITIONAL(ENABLE_IMAP, test "x$enable_imap" = "xyes")

AC_ARG_ENABLE(pop3, AC_HELP_STRING([--enable-pop3],
	[Enable the POP3 provider (default=yes).]),
	[case "$enableval" in
		no) enable_pop3=no ;;
		*) enable_pop3=yes ;;
	esac], [enable_pop3=yes])
AM_CONDITIONAL(ENABLE_POP3, test "x$enable_pop3" = "xyes")

AC_ARG_ENABLE(nntp, AC_HELP_STRING([--enable-nntp],
	[Enable the NNTP provider (default=yes).]),
	[case "$enableval" in
		no) enable_nntp=no ;;
		*) enable_nntp=yes ;;
	esac], [enable_nntp=yes])
AM_CONDITIONAL(ENABLE_NNTP, test "x$enable_nntp" = "xyes")

AC_ARG_ENABLE(mbox, AC_HELP_STRING([--enable-mbox],
	[Enable the mbox provider (default=yes).]),
	[case "$enableval" in
		no) enable_mbox=no ;;
		*) enable_mbox=yes ;;
	esac], [enable_mbox=yes])
AM_CONDITIONAL(ENABLE_MBOX, test "x$enable_mbox" = "xyes")

AC_ARG_ENABLE(maildir, AC_HELP_STRING([--enable-maildir],
	[Enable the Maildir provider (default=yes).]),
	[case "$enableval" in
		no) enable_maildir=no ;;
		*) enable_maildir=yes ;;
	esac], [enable_maildir=yes])
AM_CONDITIONAL(ENABLE_MAILDIR, test "x$enable_maildir" = "xyes")

AC_PROG_JAVAC
AC_PROG_JAVADOC
AC_PROG_JAR
AC_JAVA_OPTIONS
dnl AC_CHECK_CLASSPATH

GNUMAIL_WITH_ACTIVATION_JAR
GNUMAIL_WITH_INETLIB_JAR
GNUMAIL_WITH_JSSE

AC_CONFIG_FILES(Makefile)

AC_OUTPUT

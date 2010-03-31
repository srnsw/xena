## Process this file with automake to produce Makefile.in.

# Makefile.am for GNU Classpathx JavaMail.

JAVAC = @JAVAC@
JAVACFLAGS = @JAVACFLAGS@
JAVADOC = @JAVADOC@
JAVADOCFLAGS = @JAVADOCFLAGS@

src = @srcdir@/source
dst = classes
doc = @srcdir@/docs

gnumail_jar = gnumail.jar
providers_jar = gnumail-providers.jar

# The java source files that make up the GNU version of the javax.mail API.
gnumail_sources = \
$(src)/javax/mail/Address.java \
$(src)/javax/mail/AuthenticationFailedException.java \
$(src)/javax/mail/Authenticator.java \
$(src)/javax/mail/BodyPart.java \
$(src)/javax/mail/FetchProfile.java \
$(src)/javax/mail/Flags.java \
$(src)/javax/mail/Folder.java \
$(src)/javax/mail/FolderClosedException.java \
$(src)/javax/mail/FolderNotFoundException.java \
$(src)/javax/mail/Header.java \
$(src)/javax/mail/IllegalWriteException.java \
$(src)/javax/mail/Message.java \
$(src)/javax/mail/MessageAware.java \
$(src)/javax/mail/MessageContext.java \
$(src)/javax/mail/MessageRemovedException.java \
$(src)/javax/mail/MessagingException.java \
$(src)/javax/mail/MethodNotSupportedException.java \
$(src)/javax/mail/Multipart.java \
$(src)/javax/mail/MultipartDataSource.java \
$(src)/javax/mail/NoSuchProviderException.java \
$(src)/javax/mail/Part.java \
$(src)/javax/mail/PasswordAuthentication.java \
$(src)/javax/mail/Provider.java \
$(src)/javax/mail/Quota.java \
$(src)/javax/mail/QuotaAwareStore.java \
$(src)/javax/mail/ReadOnlyFolderException.java \
$(src)/javax/mail/SendFailedException.java \
$(src)/javax/mail/Service.java \
$(src)/javax/mail/Session.java \
$(src)/javax/mail/Store.java \
$(src)/javax/mail/StoreClosedException.java \
$(src)/javax/mail/Transport.java \
$(src)/javax/mail/UIDFolder.java \
$(src)/javax/mail/URLName.java \
$(src)/javax/mail/event/ConnectionAdapter.java \
$(src)/javax/mail/event/ConnectionEvent.java \
$(src)/javax/mail/event/ConnectionListener.java \
$(src)/javax/mail/event/FolderAdapter.java \
$(src)/javax/mail/event/FolderEvent.java \
$(src)/javax/mail/event/FolderListener.java \
$(src)/javax/mail/event/MailEvent.java \
$(src)/javax/mail/event/MessageChangedEvent.java \
$(src)/javax/mail/event/MessageChangedListener.java \
$(src)/javax/mail/event/MessageCountAdapter.java \
$(src)/javax/mail/event/MessageCountEvent.java \
$(src)/javax/mail/event/MessageCountListener.java \
$(src)/javax/mail/event/StoreEvent.java \
$(src)/javax/mail/event/StoreListener.java \
$(src)/javax/mail/event/TransportAdapter.java \
$(src)/javax/mail/event/TransportEvent.java \
$(src)/javax/mail/event/TransportListener.java \
$(src)/javax/mail/internet/AddressException.java \
$(src)/javax/mail/internet/ContentDisposition.java \
$(src)/javax/mail/internet/ContentType.java \
$(src)/javax/mail/internet/HeaderTokenizer.java \
$(src)/javax/mail/internet/InternetAddress.java \
$(src)/javax/mail/internet/InternetHeaders.java \
$(src)/javax/mail/internet/MailDateFormat.java \
$(src)/javax/mail/internet/MimeBodyPart.java \
$(src)/javax/mail/internet/MimeMessage.java \
$(src)/javax/mail/internet/MimeMultipart.java \
$(src)/javax/mail/internet/MimePart.java \
$(src)/javax/mail/internet/MimePartDataSource.java \
$(src)/javax/mail/internet/MimeUtility.java \
$(src)/javax/mail/internet/NewsAddress.java \
$(src)/javax/mail/internet/ParameterList.java \
$(src)/javax/mail/internet/ParseException.java \
$(src)/javax/mail/internet/SharedInputStream.java \
$(src)/javax/mail/search/AddressStringTerm.java \
$(src)/javax/mail/search/AddressTerm.java \
$(src)/javax/mail/search/AndTerm.java \
$(src)/javax/mail/search/BodyTerm.java \
$(src)/javax/mail/search/ComparisonTerm.java \
$(src)/javax/mail/search/DateTerm.java \
$(src)/javax/mail/search/FlagTerm.java \
$(src)/javax/mail/search/FromStringTerm.java \
$(src)/javax/mail/search/FromTerm.java \
$(src)/javax/mail/search/HeaderTerm.java \
$(src)/javax/mail/search/IntegerComparisonTerm.java \
$(src)/javax/mail/search/MessageIDTerm.java \
$(src)/javax/mail/search/MessageNumberTerm.java \
$(src)/javax/mail/search/NotTerm.java \
$(src)/javax/mail/search/OrTerm.java \
$(src)/javax/mail/search/ReceivedDateTerm.java \
$(src)/javax/mail/search/RecipientStringTerm.java \
$(src)/javax/mail/search/RecipientTerm.java \
$(src)/javax/mail/search/SearchException.java \
$(src)/javax/mail/search/SearchTerm.java \
$(src)/javax/mail/search/SentDateTerm.java \
$(src)/javax/mail/search/SizeTerm.java \
$(src)/javax/mail/search/StringTerm.java \
$(src)/javax/mail/search/SubjectTerm.java \
$(src)/javax/mail/util/ByteArrayDataSource.java \
$(src)/javax/mail/util/SharedByteArrayInputStream.java \
$(src)/javax/mail/util/SharedFileInputStream.java \
$(src)/gnu/mail/handler/Application.java \
$(src)/gnu/mail/handler/ApplicationOctetStream.java \
$(src)/gnu/mail/handler/Message.java \
$(src)/gnu/mail/handler/MessageRFC822.java \
$(src)/gnu/mail/handler/Multipart.java \
$(src)/gnu/mail/handler/MultipartAlternative.java \
$(src)/gnu/mail/handler/MultipartMixed.java \
$(src)/gnu/mail/handler/MultipartRelated.java \
$(src)/gnu/mail/handler/MultipartSigned.java \
$(src)/gnu/mail/handler/Text.java \
$(src)/gnu/mail/handler/TextHtml.java \
$(src)/gnu/mail/handler/TextPlain.java \
$(src)/gnu/mail/handler/TextXml.java

# Utility and helper classes for the JavaMail API.
util_sources = \
$(src)/gnu/mail/util/BOutputStream.java \
$(src)/gnu/mail/util/Base64InputStream.java \
$(src)/gnu/mail/util/Base64OutputStream.java \
$(src)/gnu/mail/util/MailboxURLConnection.java \
$(src)/gnu/mail/util/QInputStream.java \
$(src)/gnu/mail/util/QOutputStream.java \
$(src)/gnu/mail/util/QPInputStream.java \
$(src)/gnu/mail/util/QPOutputStream.java \
$(src)/gnu/mail/util/RFC2822OutputStream.java \
$(src)/gnu/mail/util/UUInputStream.java \
$(src)/gnu/mail/util/UUOutputStream.java

# Utility and helper classes for the providers.
provider_sources = \
$(src)/gnu/mail/treeutil/StatusEvent.java \
$(src)/gnu/mail/treeutil/StatusListener.java \
$(src)/gnu/mail/treeutil/StatusSource.java \
$(src)/gnu/mail/providers/ReadOnlyMessage.java

# The SMTP provider
if ENABLE_SMTP
smtp_sources = \
$(src)/gnu/mail/providers/smtp/SMTPTransport.java
else
smtp_sources =
endif

# The IMAP provider
if ENABLE_IMAP
imap_sources = \
$(src)/gnu/mail/providers/imap/ACL.java \
$(src)/gnu/mail/providers/imap/IMAPBodyPart.java \
$(src)/gnu/mail/providers/imap/IMAPFlags.java \
$(src)/gnu/mail/providers/imap/IMAPFolder.java \
$(src)/gnu/mail/providers/imap/IMAPMessage.java \
$(src)/gnu/mail/providers/imap/IMAPMultipart.java \
$(src)/gnu/mail/providers/imap/IMAPMultipartDataSource.java \
$(src)/gnu/mail/providers/imap/IMAPStore.java \
$(src)/gnu/mail/providers/imap/Rights.java
else
imap_sources =
endif

# POP3 protocol
if ENABLE_POP3
pop3_sources = \
$(src)/gnu/mail/providers/pop3/POP3Folder.java \
$(src)/gnu/mail/providers/pop3/POP3Message.java \
$(src)/gnu/mail/providers/pop3/POP3Store.java
else
pop3_sources =
endif

# News protocol
if ENABLE_NNTP
nntp_sources = \
$(src)/gnu/mail/providers/nntp/ListFolderListener.java \
$(src)/gnu/mail/providers/nntp/NNTPFolder.java \
$(src)/gnu/mail/providers/nntp/NNTPMessage.java \
$(src)/gnu/mail/providers/nntp/NNTPRootFolder.java \
$(src)/gnu/mail/providers/nntp/NNTPStore.java \
$(src)/gnu/mail/providers/nntp/NNTPTransport.java
else
nntp_sources =
endif

# Mbox provider.
if ENABLE_MBOX
mbox_sources = \
$(src)/gnu/mail/providers/mbox/MboxFolder.java \
$(src)/gnu/mail/providers/mbox/MboxMessage.java \
$(src)/gnu/mail/providers/mbox/MboxOutputStream.java \
$(src)/gnu/mail/providers/mbox/MboxStore.java
else
mbox_sources =
endif

# Maildir message store provider.
if ENABLE_MAILDIR
maildir_sources = \
$(src)/gnu/mail/providers/maildir/MaildirFolder.java \
$(src)/gnu/mail/providers/maildir/MaildirMessage.java \
$(src)/gnu/mail/providers/maildir/MaildirStore.java
else
maildir_sources =
endif

# Package names for Javadoc

if ENABLE_SMTP
smtp_package_name = gnu.mail.providers.smtp
else
smtp_package_name =
endif

if ENABLE_IMAP
imap_package_name = gnu.mail.providers.imap
else
imap_package_name =
endif

if ENABLE_POP3
pop3_package_name = gnu.mail.providers.pop3
else
pop3_package_name =
endif

if ENABLE_NNTP
nntp_package_name = gnu.mail.providers.nntp
else
nntp_package_name =
endif

if ENABLE_MBOX
mbox_package_name = gnu.mail.providers.mbox
else
mbox_package_name =
endif

if ENABLE_MAILDIR
maildir_package_name = gnu.mail.providers.maildir
else
maildir_package_name =
endif

package_names = \
javax.mail \
javax.mail.event \
javax.mail.internet \
javax.mail.search \
$(smtp_package_name) \
$(imap_package_name) \
$(pop3_package_name) \
$(nntp_package_name) \
$(mbox_package_name) \
$(maildir_package_name) \
gnu.mail.util

# The GNU JAF library.
activation_jar = @ACTIVATION_JAR@

# The GNU classpath inetlib library.
inetlib_jar = @INETLIB_JAR@

# The JSSE library.
jsse_jar = @JSSE_JAR@

# The META stuff
gnumail_meta = \
META-INF/COPYING \
META-INF/mime.types \
META-INF/mailcap \
META-INF/javamail.charset.map

providers_meta = \
META-INF/javamail.default.providers \
META-INF/javamail.default.address.map

manifest_mf = @srcdir@/MANIFEST.MF

# Classpaths
classpath = $(src):$(activation_jar):$(inetlib_jar):$(jsse_jar)

# Targets
BUILT_SOURCES = $(gnumail_jar) $(providers_jar)

#£EXTRA_DIST = jarcompiler $(API_JAVASRC) $(GNU_SRC) $(MAIL_META)

gnumail_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(gnumail_sources))
util_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(util_sources))
provider_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(provider_sources))
smtp_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(smtp_sources))
imap_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(imap_sources))
pop3_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(pop3_sources))
nntp_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(nntp_sources))
mbox_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(mbox_sources))
maildir_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(maildir_sources))

META-INF:
	mkdir -p META-INF

META-INF/COPYING: META-INF @srcdir@/COPYING
	cp @srcdir@/COPYING META-INF/COPYING

META-INF/mime.types: META-INF @srcdir@/mime.types
	cp @srcdir@/mime.types META-INF/mime.types

META-INF/mailcap: META-INF @srcdir@/mailcap
	cp @srcdir@/mailcap META-INF/mailcap

META-INF/javamail.charset.map: META-INF @srcdir@/javamail.charset.map
	cp @srcdir@/javamail.charset.map META-INF/javamail.charset.map

META-INF/javamail.default.providers: META-INF
	echo "# javamail.providers for GNU JavaMail" > javamail.providers
if ENABLE_SMTP
	grep "protocol=smtp" @srcdir@/javamail.providers.in >> javamail.providers
endif
if ENABLE_IMAP
	grep "protocol=imap" @srcdir@/javamail.providers.in >> javamail.providers
endif
if ENABLE_POP3
	grep "protocol=pop3" @srcdir@/javamail.providers.in >> javamail.providers
endif
if ENABLE_NNTP
	grep "protocol=nntp" @srcdir@/javamail.providers.in >> javamail.providers
endif
if ENABLE_MBOX
	grep "protocol=mbox" @srcdir@/javamail.providers.in >> javamail.providers
endif
if ENABLE_MAILDIR
	grep "protocol=maildir" @srcdir@/javamail.providers.in >> javamail.providers
endif
	cp javamail.providers META-INF/javamail.default.providers

META-INF/javamail.default.address.map: META-INF javamail.address.map
	cp javamail.address.map META-INF/javamail.default.address.map

javamail.address.map:
	rm -f javamail.address.map
if ENABLE_SMTP
	echo "rfc822=smtp" >> javamail.address.map
endif
if ENABLE_NNTP
	echo "news=nntp-post" >> javamail.address.map
endif

$(dst):
	test ! -d $(dst) && mkdir $(dst)

# Jars

$(gnumail_jar): $(dst) $(gnumail_meta) $(gnumail_classes) $(util_classes)
	$(JAR) cfm $(gnumail_jar) $(manifest_mf) \
		$(gnumail_meta) \
		-C $(dst) javax \
		-C $(dst) gnu/mail/handler \
		-C $(dst) gnu/mail/util 

$(providers_jar): $(dst) $(providers_meta) $(provider_classes) $(smtp_classes) \
	$(imap_classes) $(pop3_classes) $(nntp_classes) $(mbox_classes) \
	$(maildir_classes)
	$(JAR) cfm $(providers_jar) $(manifest_mf) \
		$(providers_meta) \
		-C $(dst) gnu/mail/providers \
		-C $(dst) gnu/mail/treeutil

# Compilation

#gnumail: util $(gnumail_classes)

$(gnumail_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

#util: $(util_classes)

$(util_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

#provider_util: $(gnumail_jar) $(provider_classes)

$(provider_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

#smtp: provider_util $(smtp_classes)

$(smtp_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

#imap: provider_util $(imap_classes)

$(imap_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

#pop3: provider_util $(pop3_classes)

$(pop3_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

#nntp: provider_util $(nntp_classes)

$(nntp_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

#mbox: provider_util $(mbox_classes)

$(mbox_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

#maildir: provider_util $(maildir_classes)

$(maildir_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(classpath) -d $(dst) $<

# Javadoc
javadoc:
	mkdir -p $(doc)
	$(JAVADOC) $(JAVADOCFLAGS) -d $(doc) -sourcepath $(src) \
          -windowtitle "GNU JavaMail API documentation" $(package_names)

# Remove jar files and directories
clean-local:
	rm -f javamail.address.map javamail.providers
	rm -rf $(gnumail_jar) $(providers_jar) META-INF $(dst) $(doc)

install-data-local:
	@srcdir@/mkinstalldirs $(DESTDIR)/$(datadir)/java
	$(INSTALL_DATA) $(gnumail_jar) $(DESTDIR)/$(datadir)/java
	$(INSTALL_DATA) $(providers_jar) $(DESTDIR)/$(datadir)/java

# End of Makefile.am

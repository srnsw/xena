## Process this file with automake to produce Makefile.in.

# Makefile.am for GNU classpath inetlib.

JAVAC = @JAVAC@
JAVACFLAGS = @JAVACFLAGS@
JAVADOC = @JAVADOC@
JAVADOCFLAGS = @JAVADOCFLAGS@

src = @srcdir@/source
dst = classes
doc = @srcdir@/docs

inetlib_jar = inetlib.jar

# Utility and helper classes for the clients.
util_sources = \
$(src)/gnu/inet/util/BASE64.java \
$(src)/gnu/inet/util/CRLFInputStream.java \
$(src)/gnu/inet/util/CRLFOutputStream.java \
$(src)/gnu/inet/util/EmptyX509TrustManager.java \
$(src)/gnu/inet/util/GetLocalHostAction.java \
$(src)/gnu/inet/util/GetSystemPropertyAction.java \
$(src)/gnu/inet/util/LineInputStream.java \
$(src)/gnu/inet/util/MessageInputStream.java \
$(src)/gnu/inet/util/MessageOutputStream.java \
$(src)/gnu/inet/util/SaslCallbackHandler.java \
$(src)/gnu/inet/util/SaslCramMD5.java \
$(src)/gnu/inet/util/SaslInputStream.java \
$(src)/gnu/inet/util/SaslLogin.java \
$(src)/gnu/inet/util/SaslOutputStream.java \
$(src)/gnu/inet/util/SaslPlain.java \
$(src)/gnu/inet/util/TraceLevel.java

# The SMTP client
smtp_sources = \
$(src)/gnu/inet/smtp/Parameter.java \
$(src)/gnu/inet/smtp/ParameterList.java \
$(src)/gnu/inet/smtp/SMTPConnection.java

# The IMAP client
imap_sources = \
$(src)/gnu/inet/imap/IMAPConnection.java \
$(src)/gnu/inet/imap/IMAPConstants.java \
$(src)/gnu/inet/imap/IMAPException.java \
$(src)/gnu/inet/imap/IMAPResponse.java \
$(src)/gnu/inet/imap/IMAPResponseTokenizer.java \
$(src)/gnu/inet/imap/ListEntry.java \
$(src)/gnu/inet/imap/Namespaces.java \
$(src)/gnu/inet/imap/MailboxStatus.java \
$(src)/gnu/inet/imap/MessageStatus.java \
$(src)/gnu/inet/imap/Pair.java \
$(src)/gnu/inet/imap/Quota.java \
$(src)/gnu/inet/imap/UTF7imap.java

# The POP3 client
pop3_sources = \
$(src)/gnu/inet/pop3/POP3Connection.java

# The NNTP client
nntp_sources = \
$(src)/gnu/inet/nntp/ActiveTime.java \
$(src)/gnu/inet/nntp/ActiveTimesIterator.java \
$(src)/gnu/inet/nntp/ArticleNumberIterator.java \
$(src)/gnu/inet/nntp/ArticleResponse.java \
$(src)/gnu/inet/nntp/ArticleStream.java \
$(src)/gnu/inet/nntp/FileNewsrc.java \
$(src)/gnu/inet/nntp/GroupIterator.java \
$(src)/gnu/inet/nntp/Group.java \
$(src)/gnu/inet/nntp/GroupResponse.java \
$(src)/gnu/inet/nntp/HeaderEntry.java \
$(src)/gnu/inet/nntp/HeaderIterator.java \
$(src)/gnu/inet/nntp/LineIterator.java \
$(src)/gnu/inet/nntp/Newsrc.java \
$(src)/gnu/inet/nntp/NNTPConnection.java \
$(src)/gnu/inet/nntp/NNTPConstants.java \
$(src)/gnu/inet/nntp/NNTPException.java \
$(src)/gnu/inet/nntp/OverviewIterator.java \
$(src)/gnu/inet/nntp/Overview.java \
$(src)/gnu/inet/nntp/PairIterator.java \
$(src)/gnu/inet/nntp/Pair.java \
$(src)/gnu/inet/nntp/PendingData.java \
$(src)/gnu/inet/nntp/PostStream.java \
$(src)/gnu/inet/nntp/Range.java \
$(src)/gnu/inet/nntp/StatusResponse.java

# The FTP client.
ftp_sources = \
$(src)/gnu/inet/ftp/ActiveModeDTP.java \
$(src)/gnu/inet/ftp/BlockInputStream.java \
$(src)/gnu/inet/ftp/BlockOutputStream.java \
$(src)/gnu/inet/ftp/CompressedInputStream.java \
$(src)/gnu/inet/ftp/CompressedOutputStream.java \
$(src)/gnu/inet/ftp/DTPInputStream.java \
$(src)/gnu/inet/ftp/DTP.java \
$(src)/gnu/inet/ftp/DTPOutputStream.java \
$(src)/gnu/inet/ftp/FTPConnection.java \
$(src)/gnu/inet/ftp/FTPException.java \
$(src)/gnu/inet/ftp/FTPResponse.java \
$(src)/gnu/inet/ftp/FTPURLConnection.java \
$(src)/gnu/inet/ftp/Handler.java \
$(src)/gnu/inet/ftp/PassiveModeDTP.java \
$(src)/gnu/inet/ftp/StreamInputStream.java \
$(src)/gnu/inet/ftp/StreamOutputStream.java

# The Gopher client.
gopher_sources = \
$(src)/gnu/inet/gopher/DirectoryEntry.java \
$(src)/gnu/inet/gopher/DirectoryListing.java \
$(src)/gnu/inet/gopher/GopherConnection.java \
$(src)/gnu/inet/gopher/GopherContentHandler.java \
$(src)/gnu/inet/gopher/GopherURLConnection.java \
$(src)/gnu/inet/gopher/Handler.java

# The finger client.
finger_sources = \
$(src)/gnu/inet/finger/FingerConnection.java \
$(src)/gnu/inet/finger/FingerURLConnection.java \
$(src)/gnu/inet/finger/Handler.java

# The HTTP client.
http_sources = \
$(src)/gnu/inet/http/Authenticator.java \
$(src)/gnu/inet/http/ByteArrayRequestBodyWriter.java \
$(src)/gnu/inet/http/ByteArrayResponseBodyReader.java \
$(src)/gnu/inet/http/ChunkedInputStream.java \
$(src)/gnu/inet/http/Cookie.java \
$(src)/gnu/inet/http/CookieManager.java \
$(src)/gnu/inet/http/Credentials.java \
$(src)/gnu/inet/http/Handler.java \
$(src)/gnu/inet/http/HTTPConnection.java \
$(src)/gnu/inet/http/HTTPDateFormat.java \
$(src)/gnu/inet/http/HTTPURLConnection.java \
$(src)/gnu/inet/http/Headers.java \
$(src)/gnu/inet/http/Request.java \
$(src)/gnu/inet/http/RequestBodyWriter.java \
$(src)/gnu/inet/http/Response.java \
$(src)/gnu/inet/http/ResponseBodyReader.java \
$(src)/gnu/inet/http/ResponseHeaderHandler.java \
$(src)/gnu/inet/http/SimpleCookieManager.java \
$(src)/gnu/inet/http/event/ConnectionEvent.java \
$(src)/gnu/inet/http/event/ConnectionListener.java \
$(src)/gnu/inet/http/event/RequestEvent.java \
$(src)/gnu/inet/http/event/RequestListener.java \
$(src)/gnu/inet/https/Handler.java

# The LDAP client.
ldap_sources = \
$(src)/gnu/inet/ldap/AttributeValues.java \
$(src)/gnu/inet/ldap/BERConstants.java \
$(src)/gnu/inet/ldap/BERDecoder.java \
$(src)/gnu/inet/ldap/BEREncoder.java \
$(src)/gnu/inet/ldap/BERException.java \
$(src)/gnu/inet/ldap/LDAPConnection.java \
$(src)/gnu/inet/ldap/LDAPResult.java \
$(src)/gnu/inet/ldap/Modification.java \
$(src)/gnu/inet/ldap/ResultHandler.java

# The comsat client.
comsat_sources = \
$(src)/gnu/inet/comsat/ComsatClient.java \
$(src)/gnu/inet/comsat/ComsatInfo.java

# Package names for gjdoc
package_names = \
gnu.inet.util \
gnu.inet.smtp \
gnu.inet.imap \
gnu.inet.pop3 \
gnu.inet.nntp \
gnu.inet.ftp \
gnu.inet.gopher \
gnu.inet.finger \
gnu.inet.http \
gnu.inet.http.event \
gnu.inet.ldap \
gnu.inet.comsat

# Targets
BUILT_SOURCES = $(inetlib_jar)

jsse = @JSSE_JAR@
sasl = @SASL_JAR@
auth_callback = @AUTH_CALLBACK_JAR@

INETLIB_CLASSPATH = $(dst):$(src):$(jsse):$(sasl):$(auth_callback)
META_INF = $(dst)/META-INF
META_INF_FILES = $(META_INF)/COPYING

util_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(util_sources))
smtp_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(smtp_sources))
imap_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(imap_sources))
pop3_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(pop3_sources))
nntp_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(nntp_sources))
ftp_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(ftp_sources))
gopher_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(gopher_sources))
finger_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(finger_sources))
http_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(http_sources))
ldap_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(ldap_sources))
comsat_classes = $(patsubst $(src)/%.java, $(dst)/%.class, $(comsat_sources))

# Targets...

# Jar

$(inetlib_jar): $(dst) $(META_INF_FILES) $(smtp_classes) $(imap_classes) $(pop3_classes) $(nntp_classes) $(ftp_classes) $(gopher_classes) $(finger_classes) $(http_classes) $(ldap_classes) $(comsat_classes)
	$(JAR) cf $(inetlib_jar) -C $(dst) META-INF -C $(dst) gnu

$(dst):
	mkdir -p $(dst)

$(META_INF):
	mkdir -p $(META_INF)

$(META_INF)/COPYING: $(META_INF) COPYING
	cp @srcdir@/COPYING $(META_INF)/

# Compilation

$(util_classes): $(dst)/%.class: $(src)/%.java
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<

$(smtp_classes): $(dst)/%.class: $(src)/%.java $(util_classes)
if ENABLE_SMTP
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif


$(imap_classes): $(dst)/%.class: $(src)/%.java $(util_classes)
if ENABLE_IMAP
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

$(pop3_classes): $(dst)/%.class: $(src)/%.java $(util_classes)
if ENABLE_POP3
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

$(nntp_classes): $(dst)/%.class: $(src)/%.java $(util_classes)
if ENABLE_NNTP
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

$(ftp_classes): $(dst)/%.class: $(src)/%.java $(util_classes)
if ENABLE_FTP
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

$(gopher_classes): $(dst)/%.class: $(src)/%.java $(util_classes)
if ENABLE_GOPHER
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

$(finger_classes): $(dst)/%.class: $(src)/%.java
if ENABLE_FINGER
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

$(http_classes): $(dst)/%.class: $(src)/%.java
if ENABLE_HTTP
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

$(ldap_classes): $(dst)/%.class: $(src)/%.java
if ENABLE_LDAP
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

$(comsat_classes): $(dst)/%.class: $(src)/%.java
if ENABLE_COMSAT
	$(JAVAC) $(JAVACFLAGS) -classpath $(INETLIB_CLASSPATH) -d $(dst) $<
endif

javadoc:
	mkdir -p $(doc)
	$(JAVADOC) -d $(doc) -windowtitle "GNU inetlib API documentation" -sourcepath $(src) $(package_names)

clean-local:
	rm -rf $(META_INF) $(doc) $(inetlib_jar)
	find $(dst) -name '*.class' -exec rm -f '{}' ';'

install-data-local:
	$(mkinstalldirs) $(prefix)/share/java
	$(INSTALL_DATA) $(inetlib_jar) $(prefix)/share/java

# End of Makefile.am

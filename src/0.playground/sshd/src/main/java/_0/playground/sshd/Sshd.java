package _0.playground.sshd;

import java.io.IOException;
import java.security.PublicKey;

import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0.playground.core._0;

public class Sshd implements PublickeyAuthenticator, PasswordAuthenticator, AutoCloseable {

	private static final Logger log = LoggerFactory.getLogger(Sshd.class);

	private SshServer server = null;

	public Sshd()
			throws IOException {
		this(0);
	}

	public Sshd(final int port)
			throws IOException {

		server = SshServer.setUpDefaultServer();
		server.setPort(port);
		server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
//		server.setUserAuthFactories(List.of(new UserAuthPasswordFactory(), new UserAuthPublicKeyFactory()));
		server.setPublickeyAuthenticator(this);
		server.setPasswordAuthenticator(this);
		server.setShellFactory(InteractiveProcessShellFactory.INSTANCE);
		server.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
		server.setCommandFactory(new ScpCommandFactory());
		// TODO: sftp

		server.start();
		log.trace("sshd: {}", port());

	}

	public int port() {
		return server.getPort();
	}

	@Override
	public boolean authenticate(final String username, final PublicKey key, final ServerSession session)
			throws AsyncAuthException {

		log.debug("client={}, username={}, type={}, format={}, pub={}", session.getClientAddress(), username, key.getAlgorithm(), key.getFormat()); // key.getEncoded();

		// TODO: 公開鍵認証

		return false;

	}

	@Override
	public boolean authenticate(final String username, final String password, final ServerSession session)
			throws PasswordChangeRequiredException, AsyncAuthException {

		log.debug("client={}, username={}, password={}", session.getClientAddress(), username, password);

		// TODO: 共通鍵認証

		return false;

	}

	@Override
	public void close() {
		_0.close(server);
	}

}

package _0.sshd;

import java.io.Closeable;
import java.io.IOException;

import org.apache.sshd.scp.server.ScpCommandFactory;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.forward.AcceptAllForwardingFilter;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.shell.InteractiveProcessShellFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0._0;

public final class Sshd implements Closeable {

	private static final Logger log = LoggerFactory.getLogger(Sshd.class);

	private SshServer server = null;

	public Sshd()
			throws IOException {
		this(0);
	}

	public Sshd(int port)
			throws IOException {

		server = SshServer.setUpDefaultServer();
		server.setPort(port);
		server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
//		server.setUserAuthFactories(List.of(new UserAuthPasswordFactory(), new UserAuthPublicKeyFactory()));
		server.setPublickeyAuthenticator((username, key, session) -> {
			log.debug("client={}, username={}, type={}, format={}, pub={}", session.getClientAddress(), username, key.getAlgorithm(), key.getFormat(), _0.hex(key.getEncoded()));
			return true; // TODO: 公開鍵認証
		});
		server.setPasswordAuthenticator((username, password, session) -> {
			log.debug("client={}, username={}, password={}", session.getClientAddress(), username, password);
			return null != password && !"".equals(password);
		});
		server.setShellFactory(InteractiveProcessShellFactory.INSTANCE);
		server.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
		server.setCommandFactory(new ScpCommandFactory());
		// TODO: sftp
		server.start();

		log.info("sshd. {}", server.getPort());

	}

	@Override
	public void close() {
		_0.close(server);
	}

}

package _0.playground.cli.sshd;

import java.io.IOException;
import java.security.PublicKey;
import java.util.HexFormat;

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

import _0.playground.core._0;

public class Main implements PublickeyAuthenticator, PasswordAuthenticator {

	public static void main(final String... args)
			throws Throwable {
		new Main();
	}

	public Main()
			throws IOException, InterruptedException {
		this(0);
	}

	public Main(final int port)
			throws IOException, InterruptedException {

		SshServer server = SshServer.setUpDefaultServer();
		server.setPort(port);
		server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		server.setPasswordAuthenticator(this);
		server.setPublickeyAuthenticator(this);
		server.setShellFactory(InteractiveProcessShellFactory.INSTANCE);
		server.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
		server.setCommandFactory(new ScpCommandFactory());
		server.start();

		System.out.println("---");
		System.out.println(getClass().getName() + ":");
		System.out.println("  port: " + server.getPort());
		System.out.println("  hash: " + HexFormat.of().toHexDigits(hashCode()));

		Runtime.getRuntime().addShutdownHook(new Thread(() -> _0.close(server)));
		Thread.sleep(Long.MAX_VALUE);

	}

	@Override
	public boolean authenticate(final String username, final PublicKey key, final ServerSession session)
			throws AsyncAuthException {

		System.out.println("---");
		System.out.println(getClass().getName() + ":");
		System.out.println("  client: "   + session.getClientAddress());
		System.out.println("  username: " + username);
		System.out.println("  type: "     + key.getAlgorithm());
		System.out.println("  format: "   + key.getFormat());
		System.out.println("  pub: "      + HexFormat.of().formatHex(key.getEncoded()));

		return _0.user.name.equals(username) && false;

	}

	@Override
	public boolean authenticate(final String username, final String password, final ServerSession session)
			throws PasswordChangeRequiredException, AsyncAuthException {

		System.out.println("---");
		System.out.println(getClass().getName() + ":");
		System.out.println("  client: "   + session.getClientAddress());
		System.out.println("  username: " + username);
		System.out.println("  password: " + password);

		return _0.user.name.equals(username) && HexFormat.of().toHexDigits(hashCode()).equals(password);

	}

}

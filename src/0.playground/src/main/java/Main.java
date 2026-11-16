import java.awt.Point;
import java.awt.Toolkit;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Path;
import java.security.PublicKey;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.transform.Rotate;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class Main extends Application {

	private static final Logger log = LoggerFactory.getLogger(Main.class);

	public  static final int cpu_core = Runtime.getRuntime().availableProcessors();

	private static final String  os_name  = System.getProperty("os.name");
	public  static final boolean is_win   = -1 < os_name.toLowerCase().indexOf("win");
	public  static final boolean is_mac   = -1 < os_name.toLowerCase().indexOf("mac");
	public  static final boolean is_linux = -1 < os_name.toLowerCase().indexOf("linux") || -1 < os_name.toLowerCase().indexOf("unix");

	public static final String name = System.getProperty("user.name");
	public static final Path   home = Path.of(System.getProperty("user.home"));

	public static final ThreadPoolExecutor worker = (ThreadPoolExecutor)Executors.newFixedThreadPool(cpu_core, (runnable) -> {
		Thread thread = Executors.defaultThreadFactory().newThread(runnable);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(true);
		return thread;
	});

	private static ServerSocketChannel sock  = null;
	private static SshServer           sshd  = null;
	private static Stage               stage = null;

	public static void main(final String... args)
			throws Throwable {

		Runtime.getRuntime().addShutdownHook(new Thread(() -> close()));

		sock  = sock(0);
		sshd  = sshd(0);
//		stage = stage();

		Thread.sleep(Long.MAX_VALUE);

	}

	private static ServerSocketChannel sock(final int port)
			throws IOException {

		Selector selector = Selector.open();

		sock = ServerSocketChannel.open();
		sock.configureBlocking(false);
		sock.setOption(StandardSocketOptions.SO_REUSEADDR, true);
		sock.bind(new InetSocketAddress(port));
		sock.register(selector, SelectionKey.OP_ACCEPT);

		log.info("sock.port: " + sock.socket().getLocalPort());

//		while (sock.isOpen()) {
//
//			int cnt = selector.select();
//
//			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
//			while (keys.hasNext()) {
//
//				SelectionKey key = keys.next();
//				keys.remove();
//
//			}
//
//		}

		return sock;

	}

	private static SshServer sshd(final int port)
			throws IOException {

		sshd = SshServer.setUpDefaultServer();
		sshd.setPort(port);
		sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
		sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

			@Override
			public boolean authenticate(final String username, final String password, final ServerSession session) {

				InetSocketAddress addr = (InetSocketAddress)session.getClientAddress();

				log.trace("sshd.auth: " + addr.getAddress() + ":" + addr.getPort() + " " + username + " " + password);

				return false;

			}

		});
		sshd.setPublickeyAuthenticator(new PublickeyAuthenticator() {

			@Override
			public boolean authenticate(final String username, final PublicKey pub, final ServerSession session) {

				InetSocketAddress addr = (InetSocketAddress)session.getClientAddress();

				log.trace("sshd.auth: " + addr.getAddress() + ":" + addr.getPort() + " " + username);

				return false;

			}

		});
//		sshd.setShellFactory(InteractiveProcessShellFactory.INSTANCE);
//		sshd.setForwardingFilter(AcceptAllForwardingFilter.INSTANCE);
//		sshd.setCommandFactory(new ScpCommandFactory());
		sshd.start();

		log.info("sshd.port: " + sshd.getPort());

		return sshd;

	}

	private static Stage stage()
			throws InterruptedException {

		new Thread(() -> Application.launch(Main.class)).start();

		while (null == stage) {
			Thread.sleep(100);
		}

		return stage;

	}

	@Override
	public void start(final Stage primary)
			throws Exception {

		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFarClip(Double.MAX_VALUE);

		Group axis = new Group();
		{

			double radius = 0.0005d;
			double height = 1.0d;

			Cylinder axis_x = new Cylinder(radius, height);
			Cylinder axis_y = new Cylinder(radius, height);
			Cylinder axis_z = new Cylinder(radius, height);

			axis_x.setMaterial(new PhongMaterial(Color.RED));
			axis_y.setMaterial(new PhongMaterial(Color.GREEN));
			axis_z.setMaterial(new PhongMaterial(Color.BLUE));

			axis_x.getTransforms().add(new Rotate(90.0d, Rotate.Z_AXIS));
			axis_y.getTransforms().add(new Rotate(90.0d, Rotate.Y_AXIS));
			axis_z.getTransforms().add(new Rotate(90.0d, Rotate.X_AXIS));

			axis.getChildren().addAll(axis_x, axis_y, axis_z);

		}

		Point.Double pos = new Point.Double();

		Rotate rotate_x = new Rotate(0.0d, Rotate.X_AXIS);
		Rotate rotate_y = new Rotate(0.0d, Rotate.Y_AXIS);
		Rotate rotate_z = new Rotate(0.0d, Rotate.Z_AXIS);

		Group rotate = new Group();
		rotate.getTransforms().addAll(rotate_x, rotate_y, rotate_z);
		rotate.getChildren().addAll(axis);

		SubScene scene3d = new SubScene(rotate, Screen.getPrimary().getBounds().getWidth(), Screen.getPrimary().getBounds().getHeight(), true, SceneAntialiasing.BALANCED);
		scene3d.setFill(Color.BLACK);
		scene3d.setCamera(camera);
		scene3d.addEventHandler(ScrollEvent.ANY, e -> {

			double delta = e.getDeltaY();
			delta  = Math.min(delta,  1.0d);
			delta  = Math.max(delta, -1.0d);
			delta *= 0.1d;

			double z = Math.min(camera.getTranslateZ() + delta, 0.0d);
			camera.setTranslateZ(z);

		});
		scene3d.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
			pos.x = e.getSceneX();
			pos.y = e.getSceneY();
		});
		scene3d.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {

			double now_x = e.getSceneX();
			double now_y = e.getSceneY();

			rotate_y.setAngle(rotate_y.getAngle() + (pos.getX() - now_x) * 0.1d);
			rotate_x.setAngle(rotate_x.getAngle() - (pos.getY() - now_y) * 0.1d);

			pos.x = now_x;
			pos.y = now_y;

		});

		StackPane stack = new StackPane();
		stack.getChildren().add(scene3d);

		primary.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (KeyCode.ESCAPE == e.getCode()) {
				close();
			}
		});
		primary.setFullScreen(true);
		primary.setScene(new Scene(stack));
		primary.show();

		stage = primary;

	}

	public static final void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	public static final boolean empty(final Object obj) {
		return switch (obj) {
			case null            -> true;
			case Boolean       e -> !e.booleanValue();
			case Number        e -> 0.0d == e.doubleValue();
			case CharSequence  e -> "".equals(e.toString());
			case Iterator<?>   e -> e.hasNext();
			case Collection<?> e -> e.isEmpty();
			case Map<?, ?>     e -> e.isEmpty();
			default              -> throw new UnsupportedOperationException(obj.getClass().getName());
		};
	}

	private static final void close() {

		close(sock);
		close(sshd);

		if (null != stage) {
			Platform.runLater(() -> stage.close());
		}

		worker.shutdown();

	}

	public static final void close(final Closeable closeable) {
		if (null != closeable) {
			try {
				closeable.close();
			} catch (IOException e) {
				log.warn("", e);
			}
		}
	}

	public static final void flush(final Flushable flushable) {
		if (null != flushable) {
			try {
				flushable.flush();
			} catch (IOException e) {
				log.warn("", e);
			}
		}
	}

}

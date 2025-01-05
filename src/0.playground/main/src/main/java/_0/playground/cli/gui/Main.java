package _0.playground.cli.gui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Main extends Application {

	private double scale = 1.0d;

	public static void main(final String... args)
			throws Throwable {
		Application.launch(Main.class);
	}

	@Override
	public void start(final Stage primary)
			throws Exception {

		primary.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (KeyCode.ESCAPE == e.getCode()) {
				primary.close();
			}
		});
		primary.setFullScreen(true);
		primary.setScene(scene());
		primary.show();

	}

	private Scene scene() {

		PerspectiveCamera camera = new PerspectiveCamera(true);
		camera.setFarClip(Double.MAX_VALUE);
		camera.setFieldOfView(30.0);

		Group root  = new Group();
		Scene scene = new Scene(root, -1, -1, true);
		scene.setFill(Color.BLACK);
		scene.setCamera(camera);

		return scene;

	}

}

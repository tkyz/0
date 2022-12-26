package _0.playground;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.parser.ParserException;

import _0._0;

@SuppressWarnings("serial")
public final class Config extends HashMap<String, Object> {

	private static final List<Path> default_files = new LinkedList<>() {

		{
			add(_0.userhome.resolve("0.yml"));
			add(Path.of(".").resolve("0.yml"));
			add(Path.of(".").resolve("playground.yml"));
		}

	};

	public Config()
			throws IOException {

		boolean load = false;
		for (Path default_file : default_files) {

			load |= load(default_file);

			if (load) {
				break;
			}

		}

		if (!load) {

			String msg = default_files.stream()
					.map(Path::toAbsolutePath)
					.map(Path::normalize)
					.map(Path::toString)
					.collect(Collectors.joining(", "));

			throw new FileNotFoundException(msg);

		}

	}

	@SuppressWarnings("unchecked")
	private boolean load(Path file)
			throws IOException {

		boolean load = false;

		if (Files.exists(file) && !Files.isDirectory(file)) {

			try (InputStream in = new FileInputStream(file.toFile())) {

				putAll(new Yaml().loadAs(in, Map.class));

				load = true;

			} catch (ParserException e) {
				throw new IOException(file.toAbsolutePath().normalize().toString(), e);
			}

		}

		return load;

	}

}

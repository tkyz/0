package _0.playground.cli.tag;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import _0.playground.core._0;

public class Main implements FileVisitor<Path> {

	private static final Path start = Path.of("./doc/ref/tag");

	private Map<Path, List<Path>> map = new HashMap<>();

	public static void main(final String... args)
			throws Throwable {
		Files.walkFileTree(start, new Main());
	}

	@Override
	public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs)
			throws IOException {

		Path start = dir.resolve("start.txt");
		if (Files.isRegularFile(start)) {
			Files.delete(start);
		}

		map.put(dir, new LinkedList<>());

		return FileVisitResult.CONTINUE;

	}

	@Override
	public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
			throws IOException {

		map.get(file.getParent()).add(file);

		return FileVisitResult.CONTINUE;

	}

	@Override
	public FileVisitResult visitFileFailed(final Path file, final IOException e)
			throws IOException {
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
			throws IOException {

		List<Path> list = map.remove(dir);
		if (!_0.empty(list)) {

			Collections.sort(list);

			Path start = dir.resolve("start.txt");
			try (PrintWriter pw = new PrintWriter(new FileOutputStream(start.toFile()))) {

				int i = 0;
				while (!_0.empty(list)) {

					Path file = list.remove(0);

					if (0 == i) {
						pw.print('|');
					}
					pw.print("{{" + file.getFileName() + "?256}}|");

					i = (i + 1) % 4;
					if (0 == i || _0.empty(list)) {
						pw.println();
					}

				}
				_0.flush(pw);

			}

			System.out.println(dir);

		}

		return FileVisitResult.CONTINUE;

	}

}

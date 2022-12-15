package _0;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public final class FileExtFilter implements Function<Path, Boolean>, Predicate<Path>, FileFilter, FilenameFilter {

	private Set<String> exts = null;

	public FileExtFilter() {

		Set<String> exts = new HashSet<>();

		// source
		if (true) {
			exts.add("c"); exts.add("cpp"); exts.add("cs"); exts.add("h");
			exts.add("rs");
			exts.add("go");
			exts.add("java");
			exts.add("php");
			exts.add("pl");
			exts.add("py");
			exts.add("js");
			exts.add("sh");
			exts.add("bat");
			exts.add("bas");
			exts.add("frm");
			exts.add("cob");
			exts.add("sql"); exts.add("ddl");
			exts.add("Makefile");
			exts.add("Dockerfile");
			exts.add("Vagrantfile");
			exts.add("htm"); exts.add("html");
			exts.add("css");
			exts.add("cnf"); exts.add("conf"); exts.add("config");
			exts.add("ini");
			exts.add("properties");
			exts.add("json");
			exts.add("yml"); exts.add("yaml");
			exts.add("xml");
		}

		// data
		if (true) {
			exts.add("tsv");
			exts.add("csv");
			exts.add("avro");
			exts.add("avsc");
			exts.add("mdb");
			exts.add("accdb");
		}

		// sec
		if (true) {
			exts.add("key");
			exts.add("pub");
			exts.add("crt");
			exts.add("csr");
			exts.add("pem");
			exts.add("id_rsa");
		}

		// archive
		if (true) {
			exts.add("jar");
			exts.add("war");
			exts.add("ear");
			exts.add("tar");
			exts.add("bz2");
			exts.add("gz");
			exts.add("rar");
			exts.add("zip");
			exts.add("7z");
			exts.add("lzh");
		}

		// media
		if (true) {
			exts.add("png");
			exts.add("gif");
			exts.add("jpg");
			exts.add("jpeg");
			exts.add("bmp");
			exts.add("mp3");
			exts.add("mp4");
			exts.add("flv");
			exts.add("avi");
			exts.add("wav");
		}

		if (true) {
			exts.add("txt");
			exts.add("xls");
			exts.add("xlsx");
			exts.add("doc");
			exts.add("docx");
			exts.add("ppt");
			exts.add("pptx");
			exts.add("pdf");
			exts.add("iso");
			exts.add("img");
		}

		this.exts = exts;

	}

	public FileExtFilter(final String... exts) {
		this(Arrays.asList(exts));
	}

	public FileExtFilter(final Collection<String> exts) {
		this.exts = new HashSet<>(exts);
	}

	@Override
	public Boolean apply(final Path path) {
		return Boolean.valueOf(accept(path));
	}

	@Override
	public boolean test(final Path path) {
		return accept(path.toFile());
	}

	public boolean accept(final Path path) {
		return accept(path.toFile());
	}

	@Override
	public boolean accept(final File file) {

		File   dir  = file.getParentFile();
		String name = file.getName();

		return accept(dir, name);

	}

	@Override
	public boolean accept(final File dir, final String name) {

		int start = Math.max(0, name.lastIndexOf(".") + 1);

		String ext = name.substring(start).toLowerCase();

		return exts.contains(ext);

	}

}

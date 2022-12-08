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

	public FileExtFilter(final String... exts) {
		this(Arrays.asList(exts));
	}

	public FileExtFilter(final Collection<String> exts) {
		this.exts = new HashSet<>(exts);
	}

	@Override
	public Boolean apply(final Path path) {
		return Boolean.valueOf(test(path));
	}

	@Override
	public boolean test(final Path path) {
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

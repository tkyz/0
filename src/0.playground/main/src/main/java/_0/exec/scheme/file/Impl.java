package _0.exec.scheme.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import _0.bigset.BigSet;
import _0.bigset.BigSet.Entry;
import _0.core._0;

public final class Impl {

	public final void run(final Entry entry)
			throws IOException, InterruptedException {

		Path file = entry.path(true);
		if (null == file) {
			throw new FileNotFoundException(entry.getKey());
		}

		Number last      = entry.val("meta/last");
		Number size      = entry.val("meta/size");
		String mime_type = entry.val("meta/mime_type");
		String hash      = entry.val("meta/sha256");

		Number last_ = Files.getLastModifiedTime(file).toMillis();
		Number size_ = Files.size(file);

		boolean changed = false;
		changed |= 0 != _0.compare(last, last_);
		changed |= 0 != _0.compare(size, size_);
		if (changed) {
			last      = last_;
			size      = size_;
			mime_type = null;
			hash      = null;
		}

		changed |= _0.empty(mime_type);
		if (changed) {
			mime_type = _0.fs.mime_type(file);
			hash      = null;
		}

		changed |= _0.empty(hash);
		if (changed && !file.equals(BigSet.store_file)) {
			try {
				hash = _0.format.hex(_0.hash.sha256(file));
			} catch (FileNotFoundException e) {
				// pass *.sock
			}
		}

		_0.set(entry.getValue(), "meta/last",      last);
		_0.set(entry.getValue(), "meta/size",      size);
		_0.set(entry.getValue(), "meta/mime_type", mime_type);
		_0.set(entry.getValue(), "meta/sha256",    hash);

	}

}

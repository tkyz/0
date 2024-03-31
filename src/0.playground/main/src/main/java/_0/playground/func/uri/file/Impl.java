package _0.playground.func.uri.file;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0.playground.Main;
import _0.playground.Main.Global;
import _0.playground.UserConfig;
import _0.playground.core._0;
import _0.playground.func.Func;

public class Impl extends Func<Path> {

	private static final Logger log = LoggerFactory.getLogger(Func.class);

	public Impl(final String key, final Map<String, Object> val) {
		super(key, val);
	}

	@Override
	public Path cast() {
		String key = key();
		return key.startsWith("file:///") ? Path.of(key.substring("file://".length())) : null;
	}

	@Override
	public Void call()
			throws GeneralSecurityException, IOException {

		String hash = file();

		blob(hash);

		return null;

	}

	private String file()
			throws GeneralSecurityException, IOException {

		String              key  = key();
		Map<String, Object> val  = val();
		Path                file = cast();

		if (!UserConfig.walk_target.test(file) || Files.isDirectory(file)) {
			Global.of().kvs.del(key);
			log("-", key, null, val);
			return null;
		}

		String hash = _0.get(val, "blob/hash");
		boolean upd = false;
		{

			BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);

			Number size = attrs.size();
			String date = Main.date(_0.max(attrs.creationTime().toMillis(), attrs.lastModifiedTime().toMillis()));

			upd |= 0 != _0.compare(size, _0.get(val, "meta/size"));
			upd |= 0 != _0.compare(date, _0.get(val, "meta/date"));
			upd |= null == hash;

			_0.set(val, "meta/size", size);
			_0.set(val, "meta/date", date);

		}

		if (upd) {

			MessageDigest md_sha256 = MessageDigest.getInstance("SHA-256");
			MessageDigest md_sha512 = MessageDigest.getInstance("SHA-512");

			String sha256 = null;
			String sha512 = null;
			try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()))) {

				byte[] buffer = new byte[1 << 16];
				int size = -1;

				while (-1 < (size = in.read(buffer))) {
					md_sha256.update(buffer, 0, size);
					md_sha512.update(buffer, 0, size);
				}

				sha256 = _0.hex(md_sha256.digest());
				sha512 = _0.hex(md_sha512.digest());

			}

			hash = sha256;
			_0.set(val, "blob/hash", hash);

		}

		if (upd) {
			Global.of().kvs.set(key, val);
			log("*", key, null, val);
		}

		return hash;

	}

	private void blob(final String hash) {

		String              key = "blob://" + hash;
		Map<String, Object> val = new HashMap<>();
		_0.set(val, "source:", List.of(key()));

		Global.of().kvs.set(key, val);

	}

	private void log(String kvssign, String key, String linksign, Map<String, Object> val) {

		String hash = _0.get(val, "blob/hash");

		hash     = StringUtils.rightPad(_0.nvl(hash,     ""), 64, ' ');
		kvssign  = StringUtils.rightPad(_0.nvl(kvssign,  ""),  1, ' ');
		linksign = StringUtils.rightPad(_0.nvl(linksign, ""),  2, ' ');

		log.debug("{} {} {} {}", kvssign, hash, linksign, key);

	}

}

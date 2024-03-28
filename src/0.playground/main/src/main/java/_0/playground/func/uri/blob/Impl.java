package _0.playground.func.uri.blob;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import _0.playground.Main.Global;
import _0.playground.func.Func;

public class Impl extends Func<Path> {

	// TODO: native: file --brief --mime-type *
	@SuppressWarnings("serial")
	private static final Map<String, String> mime_map = new HashMap<>() {
		{

			put("inode/x-empty",                                 null);
			put("application/javascript",                        "js");
			put("application/octet-stream",                      null);
			put("application/x-mswinurl",                        "url");
			put("application/x-apple-rsr",                       "rsrc");
			put("application/vnd.microsoft.portable-executable", "exe");
			put("text/plain",                                    "txt");
			put("text/xml",                                      "xml");
			put("text/html",                                     "html");
			put("text/x-asm",                                    "css");

			put("application/pdf",                               "pdf");
			put("image/gif",                                     "gif");
			put("image/png",                                     "png");
			put("image/jpeg",                                    "jpg");
			put("image/x-icns",                                  "icns");
			put("audio/mpeg",                                    "mp3");
			put("audio/mp4",                                     "mp4");
			put("audio/flac",                                    "flac");
			put("audio/x-wav",                                   "wav");
			put("application/x-shockwave-flash",                 "swf");
			put("video/mpeg",                                    "mpg");
			put("video/mp4",                                     "mp4");
			put("video/ogg",                                     "ogg");
			put("video/MP2T",                                    "ts");
			put("video/x-m4v",                                   "m4v");
			put("video/x-flv",                                   "flv");
			put("video/x-msvideo",                               "avi");
			put("video/x-ms-asf",                                "asf"); // wmv
			put("video/x-matroska",                              "mkv");

		}
	};

	public Impl(final String key, final Map<String, Object> val) {
		super(key, val);
	}

	@Override
	public Path cast() {
		String key = key();
		return key.startsWith("blob://") ? cast(key.replaceAll(".*/", "")) : null;
	}

	public static Path cast(final String hash) {
		return Global.of().blob_dir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4)).resolve(hash);
	}

	@Override
	public Void call()
			throws IOException {

		String              key  = key();
		Map<String, Object> val  = val();
		Path                file = cast();

/*
		// blob/ext
		upd |= null == _0.get(val, "blob/ext");
		if (upd) {

			// TODO: native call
			String ext = null;
			try {

				Process process = Runtime.getRuntime().exec(new String[] {"file", "--brief", "--mime-type", file.toString()});
				process.waitFor();

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					ext = reader.readLine();
				}

			} catch (IOException e) {
				log.warn("{}", e.getMessage());
			}

			if (mime_map.containsKey(ext)) {
				ext = mime_map.get(ext);
			} else {
				log.warn("{}", ext);
				ext = null;
			}

			_0.set(val, "blob/ext", ext);

		}

if (!"txt".equals(_0.get(val, "blob/ext")) && 0 != _0.compare(_0.get(val, "blob/ext"), _0.get(val, "meta/ext"))) {
	log("@", key, null, val);
}

		boolean is_reffs = file.startsWith(Global.instance.ref_dir);

		boolean blob_ext_target = false;
		blob_ext_target |= UserConfig.blob_ext_target.test(_0.get(val, "meta/ext"));
		blob_ext_target |= UserConfig.blob_ext_target.test(_0.get(val, "blob/ext"));

		// blob
		Path blob_file = Global.instance.blob_dir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4)).resolve(hash);
		if (blob_ext_target && !Files.exists(blob_file)) {

			if (!is_reffs) {

				Files.createDirectories(blob_file.getParent());
				Files.copy(file, blob_file, StandardCopyOption.COPY_ATTRIBUTES);
				log(".", key, "<<", val);

				Path copy_file = Global.instance.ref_dir.resolve(".copy").resolve(hash + "_" + file.getFileName().toString());
				Files.createDirectories(copy_file.getParent());
				Files.createLink(copy_file, blob_file);
				log(".", key, "->", val);

			} else {
				Files.createDirectories(blob_file.getParent());
				Files.createLink(blob_file, file);
				log(".", key, "<-", val);
			}

		}

		if (!blob_ext_target) {
			log(".", key, "#", val);
		}

		// ref配下のファイル置き換え
		if (is_reffs && Files.exists(blob_file)) {

			long ino1 = _0.ino(file);
			long ino2 = _0.ino(blob_file);

			// blob対象でinode違いはlinkにする
			if (blob_ext_target && ino1 != ino2) {

				long sz1 = Files.size(file);
				long sz2 = Files.size(blob_file);
				long ms1 = Files.getLastModifiedTime(file).toMillis();
				long ms2 = Files.getLastModifiedTime(blob_file).toMillis();

				if (sz1 != sz2) {
					log(".", key, "!", val);

				} else {
					Files.setLastModifiedTime(blob_file, FileTime.fromMillis(Math.min(ms1, ms2)));
					Files.delete(file);
					Files.createLink(file, blob_file);
					log(".", key, "->", val);
				}

			}

			// blob非対象でinode一致はlinkを切り離す
			if (!blob_ext_target && ino1 == ino2) {
				// TODO:
				log(".", key, "/", val);
			}

		}

if (false) {

		if (null != blob_file && Files.exists(blob_file)) {
			try {

				BufferedImage img = ImageIO.read(blob_file.toFile());
				if (null != img) {

					Number width  = img.getWidth();
					Number height = img.getHeight();

					// width x height
					if (256 <= width.longValue() && 256 <= height.longValue()) {

						Path out_dir  = Global.instance.ref_dir.resolve(".image/size/" + width + "x" + height);
						Path out_file = out_dir.resolve(hash);

						if (!Files.exists(out_file)) {
							Files.createDirectories(out_dir);
							Files.createLink(out_file, blob_file);
							log.debug("{} {} {} {}", StringUtils.rightPad(_0.nvl(hash, ""), 64, ' '), "->", ".", "file://" + Main.toString(out_file));
						}

					}

					// pixel
					if (256 <= width.longValue() && 256 <= height.longValue()) {

						Path out_dir  = Global.instance.ref_dir.resolve(".image/pixel/" + (width.longValue() * height.longValue()));
						Path out_file = out_dir.resolve(hash);

						if (!Files.exists(out_file)) {
							Files.createDirectories(out_dir);
							Files.createLink(out_file, blob_file);
							log.debug("{} {} {} {}", StringUtils.rightPad(_0.nvl(hash, ""), 64, ' '), "->", ".", "file://" + Main.toString(out_file));
						}

					}

					// radius
					if (256 <= width.longValue() && 256 <= height.longValue()) {

						double radius = Math.sqrt(Math.pow(width.longValue() >> 1, 2) + Math.pow(height.longValue() >> 1, 2));

						Path out_dir  = Global.instance.ref_dir.resolve(".image/radius/" + (long)radius);
						Path out_file = out_dir.resolve(hash);

						if (!Files.exists(out_file)) {
							Files.createDirectories(out_dir);
							Files.createLink(out_file, blob_file);
							log.debug("{} {} {} {}", StringUtils.rightPad(_0.nvl(hash, ""), 64, ' '), "->", ".", "file://" + Main.toString(out_file));
						}

					}

				}
			} catch (IIOException e) {
			}
		}

}
*/
		return null;

	}

}

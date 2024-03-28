package _0.playground.func.uri.https;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.SSLException;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import _0.playground.Main;
import _0.playground.Main.Global;
import _0.playground.UserConfig;
import _0.playground.core._0;
import _0.playground.func.Func;

public class Impl extends Func<URI> {

	private static final SimpleDateFormat format_last_modified = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);

	private static final Logger log = LoggerFactory.getLogger(Impl.class);

	public Impl(String key, Map<String, Object> val) {
		super(key, val);
	}

	@Override
	public URI cast() {

		URI target = null;

		String key = key();
		if (key.startsWith("http://") || key.startsWith("https://")) {

			try {

				String enc = key;
				enc = enc.replaceAll(" ",   "%20");
				enc = enc.replaceAll("\"",  "%22");
				enc = enc.replaceAll("\\(", "%28");
				enc = enc.replaceAll("\\)", "%29");
				enc = enc.replaceAll("\\+", "%2B");
				enc = enc.replaceAll("\\[", "%5B");
				enc = enc.replaceAll("\\]", "%5D");
				enc = enc.replaceAll("\\^", "%5E");
//				enc = enc.replaceAll("\\-", "%5F");
				enc = enc.replaceAll("\\|", "%7C");
				enc = enc.replaceAll("\\{", "%7B");
				enc = enc.replaceAll("\\}", "%7D");
				enc = enc.replaceAll("　",  "%E3%80%80");

				target = new URI(enc);

			} catch (URISyntaxException e) {
				log.warn("{}", e.getMessage());
			}

		}

		return target;

	}

	public Void _call_(final String key, final Map<String, Object> prev_val, final URI uri, final boolean cli)
			throws IOException, NoSuchAlgorithmException {

		String prev_hash = _0.get(prev_val, "data/hash");
		if (!cli && null != prev_hash) {
			return null;
		}

		if (!cli && !UserConfig.proc_uri_https_target.test(uri)) {
			log.debug("{} {} {} {}", StringUtils.rightPad("", 64, ' '), "  ", "#", key);
			return null;
		}

		RequestConfig config = RequestConfig.custom()
//				.setConnectTimeout(10 * 1000)
//				.setConnectionRequestTimeout(60 * 1000)
//				.setSocketTimeout(3 * 60 * 1000)
				.build();

		HttpUriRequestBase req = new HttpGet(key);
		req.addHeader("User-Agent",      "Mozilla/5.0");
		req.addHeader("Accept-Charaset", "UTF-8");
		req.addHeader("Accept-Language", "ja, en;");
		req.setConfig(config);

		Path    tmp     = null;
		String  hash    = null;
		String  date    = null;
		Number  size    = null;
		Number  code    = null;
		String  mime    = null;
		Charset charset = null;

		try (CloseableHttpClient client = HttpClientBuilder.create().build(); CloseableHttpResponse res = client.execute(req, Global.instance.main.http_context)) {

			// TODO: redirect
			code = res.getCode();

			HttpEntity entity = res.getEntity();

			ContentType content_type = ContentType.parse(entity.getContentType());
			if (null != content_type) {
				charset = content_type.getCharset();
				mime    = content_type.getMimeType().toLowerCase();
			}

			Date last_modified = date(res);

			tmp = Files.createTempFile(null, null);
			try (InputStream in = entity.getContent(); OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp.toFile()))) {

				MessageDigest md = MessageDigest.getInstance("SHA-256");

				byte[] buffer = new byte[1 << 16];
				int  read  = -1;
				long size_ = 0;

				while (-1 < (read = in.read(buffer))) {
					md.update(buffer, 0, read);
					out.write(buffer, 0, read);
					size_ += read;
				}

				_0.flush(out);

				size = size_;
				hash = _0.hex(md.digest());

			}
			if (null != last_modified) {
				date = Main.date(last_modified);
				Files.setLastModifiedTime(tmp, FileTime.fromMillis(last_modified.getTime()));
			}

		} catch (UnknownHostException e) {
			log.warn("{}", e.toString());

		} catch (java.net.SocketTimeoutException e) {
			log.warn("{}", e.toString());

		} catch (SocketException e) {
			log.warn("{}", e.toString());

		} catch (SSLException e) {
			log.warn("{}", e.toString());

		} catch (ClientProtocolException e) {
			log.warn("{}", e.toString());

		} catch (UnsupportedCharsetException e) {
			log.trace("{} {}", key, e.toString());
		}

//		Number width  = null;
//		Number height = null;
//		if (null != tmp && Files.exists(tmp)) {
//
//			// text
//			if (mime.startsWith("text/")) {
//				try {
//
//					Charset charset_ = _0.nvl(charset, _0.utf8);
//
//					Document html = Jsoup.parse(tmp.toFile(), charset_.name());
//
//					Consumer<String> println = cli ? e -> System.out.println(e) : e -> {};
//
//					Map<String, Object> val_ = new HashMap<>();
//					_0.set(val_, "parent", List.of(orgkey));
//
//					links(key, html, charset_).stream()
//							.sorted((o1, o2) -> _0.compare(o1, o2))
//							.peek(println)
//							.forEach(e -> kvs.set(e, val_));
//
//					debug("text", "<-", orgkey);
//
//				} catch (IOException e) {
//				}
//			}
//
//			// blob
//			if (mime.startsWith("image/") || mime.startsWith("video/") || mime.endsWith("octet-stream") || "application/pdf".equals(mime) || "jpg".equals(mime)) {
//
//				try {
//					BufferedImage img = ImageIO.read(tmp.toFile());
//					if (null != img) {
//						width  = img.getWidth();
//						height = img.getHeight();
//					}
//				} catch (IOException e) {
//				}
//
//				Path blob_dir  = blob_dir.resolve(hash.substring(0, 2)).resolve(hash.substring(2, 4));
//				Path blob_file = blob_dir.resolve(hash);
//
//				if (!Files.exists(blob_file)) {
//
//					Files.createDirectories(blob_dir);
//					Files.move(tmp, blob_file);
//
//					debug(hash, "<-", orgkey);
//
//if ((null == width && null == height) || (256 <= width.longValue() && 256 <= height.longValue())) {
//
//Path new_dir  = ref_dir.getParent().resolve("ref.new").resolve(width + "x" + height);
//Path new_file = new_dir.resolve(hash);
//
//Files.createDirectories(new_dir);
//Files.createLink(new_file, blob_file);
////proc &= Files.list(new_dir.getParent()).toList().size() < 32;
//
//}
//
//				}
//
//				Path uri_file = UserConfig.ref_uri_dir.resolve(key.getScheme()).resolve(_0.reverse(".", key.getHost())).resolve(key.getPath().replaceAll("^/", ""));
//				Path uri_dir  = uri_file.getParent();
//
//				if (!Files.exists(uri_file)) {
//
//					Files.createDirectories(uri_dir);
//					Files.createLink(uri_file, blob_file);
//
//					debug(hash, "->", "file://" + Main.toString(uri_file));
//
//				}
//
//			}
//
//			try {
//				Files.delete(tmp);
//			} catch (IOException e) {
//			}
//
//		}
//
//		// https?://.*
//		{
//
//			Map<String, Object> val_ = new HashMap<>();
//			_0.set(val_, "res/code",    code);
//			_0.set(val_, "res/mime",    mime);
//			_0.set(val_, "data/date",   date);
//			_0.set(val_, "data/size",   size);
//			_0.set(val_, "data/hash",   hash);
//
//			if (null != width && null != height) {
//				_0.set(val_, "data/width",  width);
//				_0.set(val_, "data/height", height);
//			}
//
//			_0.set(val_, "data/count",  JSONObject.NULL);
//			_0.set(val_, "data/wc",     JSONObject.NULL);
//			_0.set(val_, "wc",          JSONObject.NULL);
//			_0.set(val_, "attr",        JSONObject.NULL);
//			_0.set(val_, "attrs",       JSONObject.NULL);
//
//			kvs.set(orgkey, val_);
//
//		}

		return null;

	}

//		private static List<String> links(final URI base_url, final Document doc, final Charset charset) {
//
//			Set<String> links = new HashSet<>();
//			doc.select("a[href]"    ).stream().map(e -> e.attr("href"  )).forEach(links::add);
//			doc.select("link[href]" ).stream().map(e -> e.attr("href"  )).forEach(links::add);
//			doc.select("iframe[src]").stream().map(e -> e.attr("src"   )).forEach(links::add);
//			doc.select("img[src]"   ).stream().map(e -> e.attr("src"   )).forEach(links::add);
////			doc.select("img[srcset]").stream().map(e -> e.attr("srcset")).flatMap(e -> Arrays.asList(e.split(_0.regex_spaces)).stream()).forEach(links::add);
//			doc.select("video[src]" ).stream().map(e -> e.attr("src"   )).forEach(links::add);
//			doc.select("script[src]").stream().map(e -> e.attr("src"   )).forEach(links::add);
//
//			return links.parallelStream()
//					.map(_0::trim)
//					.filter(e -> !e.startsWith("data:image/"))
//					.map(e -> e.replaceAll("[\\r\\n\\t]+", ""))
//					// TODO: uri resolve
//					.map(e -> e.replaceAll(" ",   "%20"))
//					.map(e -> e.replaceAll("　",  "%E3%80%80"))
//					.map(e -> e.replaceAll("\\|", "%7C"))
//					.map(e -> e.replaceAll("\\{", "%7B"))
//					.map(e -> e.replaceAll("\\}", "%7D"))
//					.map(e -> e.replaceAll("\"",  "%22"))
//					.map(e -> {
//						String ret = null;
//						try {
//							ret = (e.matches("^https?://.*$") ? new URI(e) : base_url.resolve(e)).normalize().toString();
//						} catch (URISyntaxException ex) {
//							log.warn("links {}", ex.getMessage());
//							ret = e;
//						} catch (IllegalArgumentException ex) {
//							log.warn("links {}", ex.getMessage());
//							ret = e;
//						}
//						return ret;
//					})
//					.map(e -> {
//						String ret = null;
//						try {
//							ret = URLDecoder.decode(e, (Charset)_0.nvl(charset, _0.utf8));
//						} catch (IllegalArgumentException ex) {
//							log.warn("links {}", ex.getMessage());
//							ret = e;
//						}
//						return ret;
//					})
//					.map(normalize_key.andThen(UserConfig.normalize_key))
//					.filter(Objects::nonNull)
//					.distinct()
//					.toList();
//
//		}

	private static Date date(final HttpResponse res) {

		Date ret = null;

		Header header = res.getFirstHeader("last-modified");
		if (null != header) {

			String val = header.getValue();

			try {
				synchronized (format_last_modified) {
					ret = format_last_modified.parse(val);
				}
			} catch (ParseException e) {
				log.warn("{} {}", val, e.toString());
			}

		}

		return ret;

	}

}

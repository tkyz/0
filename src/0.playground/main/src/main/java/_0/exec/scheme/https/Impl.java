package _0.exec.scheme.https;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Timeout;

import _0.bigset.BigSet;
import _0.bigset.BigSet.Entry;
import _0.core._0;
import _0.playground.Main;

public final class Impl {

	public final void run(final Entry entry)
			throws Exception {

		long   now       = _0.sys.now();
		String key       = entry.getKey();
		URI    uri       = entry.uri();
		String namespace = entry.namespace();
		Number status    = entry.val("meta/status");
		Number last      = entry.val("meta/last");
		Number size      = entry.val("meta/size");
		String mime_type = entry.val("meta/mime_type");
		String sha256    = entry.val("meta/sha256");
		Path   obj_file  = entry.obj(true);

		boolean isreq = false;
		isreq |= null == status;
		isreq |= null == last;
		isreq |= null == size || -1 == ((Number)size).longValue();
		isreq |= null == mime_type;
		isreq |= null == sha256;
		isreq |= null == obj_file;
		if (!isreq) {
			return;
		}

		synchronized (Main.wait_map) {

			if (!Main.wait_map.containsKey(namespace)) {
				// pass
			} else if (now - Main.wait_map.get(namespace) < 60 * 1000) {
				return;
			}

			Main.wait_map.put(namespace, Long.MAX_VALUE);

		}

		Path tmp_file = null;
		try {

			Timeout sec10 = Timeout.ofSeconds(10);

			ConnectionConfig con_conf = ConnectionConfig.custom()
					.setConnectTimeout(sec10)
					.build();

			RequestConfig req_conf = RequestConfig.custom()
					.setConnectionRequestTimeout(sec10)
					.build();

			CookieStore cookie_store = new BasicCookieStore();

//			TrustStrategy trust_all = new TrustAllStrategy();
//			SSLContext ssl_context = SSLContexts.custom()
//					.loadTrustMaterial(trust_all)
//					.build();

			HttpClientBuilder builder = HttpClientBuilder.create()
//					.setSSLContext(ssl_context)
					.setDefaultRequestConfig(req_conf)
					.setDefaultCookieStore(cookie_store);

			HttpContext context = new BasicHttpContext();

			ClassicHttpRequest req = new HttpGet(uri);
			req.addHeader("User-Agent",      "Mozilla/5.0");
			req.addHeader("Accept-Charaset", "UTF-8");
			req.addHeader("Accept-Language", "ja, en;");

//			BasicHttpClientResponseHandler handler = new BasicHttpClientResponseHandler();

			HttpEntity entity = null;
			try (CloseableHttpClient client = builder.build(); CloseableHttpResponse res = (CloseableHttpResponse)client.executeOpen(null, req, context)) {

				status = res.getCode();
				entity = res.getEntity();

				if (null == entity) {
					throw new IOException(status + " " + key);
				}
//				if (0 != _0.compare(status, 200)) {
//					ProtocolVersion version          = res.getVersion();
//					Header[]        headers          = res.getHeaders();
//					String          content_encoding = entity.getContentEncoding();
//					long            content_length   = entity.getContentLength();
//					synchronized (System.out) {
//						System.out.println(uri);
//						System.out.println(status);
//						for (Header header : headers) {
//							System.out.println(header.getName() + ": " + header.getValue());
//						}
//					}
//				}

//				String content_type = entity.getContentType();

				tmp_file = Files.createTempFile(BigSet.tmp_dir, null, null);
				try (InputStream in = entity.getContent(); OutputStream out = new BufferedOutputStream(new FileOutputStream(tmp_file.toFile()))) {

					MessageDigest md = MessageDigest.getInstance("SHA-256");

					byte[] buffer = new byte[1 << 16];
					long   size_ = 0;

					int read = -1;
					while (-1 < (read = in.read(buffer))) {
						md.update(buffer, 0, read);
						out.write(buffer, 0, read);
						size_ += read;
					}

					_0.flush(out);

					size   = size_;
					sha256 = _0.format.hex(md.digest());

				}

				last = last(res);
				Files.setLastModifiedTime(tmp_file, FileTime.fromMillis(last.longValue()));

			} finally {
				if (null != entity) {
					EntityUtils.consume(entity);
				}
			}

			mime_type = _0.fs.mime_type(tmp_file);

			_0.set(entry.getValue(), "meta/status",    status);
			_0.set(entry.getValue(), "meta/last",      last);
			_0.set(entry.getValue(), "meta/size",      size);
			_0.set(entry.getValue(), "meta/mime_type", mime_type);
			_0.set(entry.getValue(), "meta/sha256",    sha256);

			entry.mkobj(tmp_file);

		} finally {
			synchronized (Main.wait_map) {
				if (Long.MAX_VALUE == Main.wait_map.get(namespace)) {
					Main.wait_map.put(namespace, now);
				}
			}
			if (null != tmp_file) {
				try {
					Files.deleteIfExists(tmp_file);
				} catch (IOException e) {
				}
			}
		}

	}

	private static long last(final HttpResponse res)
			throws ParseException {

		long last = -1;

		Header header = res.getFirstHeader("last-modified");
		if (null != header) {

			String val = header.getValue();

			List<String> patterns = new LinkedList<>();
			patterns.add("EEE, dd MMM yyyy HH:mm:ss zzz");
			patterns.add("EEE, dd MMM yyyy HH:mm:sszzz");
			patterns.add("EEEE, dd-MMM-yy HH:mm:ss zzz");
			patterns.add("zzz");
//			2024-07-05T08:56:22.230Z
//			Sat Dec 23 2023 06:19:09 GMT+0000 (Coordinated Universal Time)

			while (!_0.empty(patterns)) {

				try {

					SimpleDateFormat df = new SimpleDateFormat(patterns.remove(0), Locale.ENGLISH);

					last = df.parse(val).getTime();

					break;

				} catch (ParseException e) {
					if (_0.empty(patterns)) {
						throw e;
					}
				}

			}

		}

		return last;

	}

}

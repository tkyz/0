package _0.exec.mime_type.text.html;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.ibm.icu.text.CharsetDetector;

import _0.bigset.BigSet;
import _0.bigset.BigSet.Entry;
import _0.core._0;

public final class Impl {

	public final void run(final Entry entry)
			throws IOException {

		String key = entry.getKey();
		URI    uri = entry.uri();
		if (null == uri) {
			return;
		}

		Path obj_file = entry.obj(true);
		if (null == obj_file) {
			return;
		}

		byte[] bytes = Files.readAllBytes(obj_file);

		CharsetDetector detector = new CharsetDetector();
		detector.setText(bytes);

		Charset  charset = Charset.forName(detector.detect().getName());
		String   content = new String(bytes, charset);
		Document doc     = Jsoup.parse(content);

		String      title = doc.select("title").text().replaceAll(_0.regex.spaces, " ");
		Set<String> links = new HashSet<>();
		doc.select("a[href]"     ).stream().map(e -> e.attr("href"  )).forEach(links::add);
		doc.select("link[href]"  ).stream().map(e -> e.attr("href"  )).forEach(links::add);
		doc.select("iframe[src]" ).stream().map(e -> e.attr("src"   )).forEach(links::add);
		doc.select("img[src]"    ).stream().map(e -> e.attr("src"   )).forEach(links::add);
		doc.select("video[src]"  ).stream().map(e -> e.attr("src"   )).forEach(links::add);
		doc.select("script[src]" ).stream().map(e -> e.attr("src"   )).forEach(links::add);
		doc.select("form[action]").stream().map(e -> e.attr("action")).forEach(links::add);
		doc.select("img[srcset]" ).stream().map(e -> e.attr("srcset"))
				.map(    e -> e.replaceAll("[ ]+[0-9]+(\\.[0-9]+)?[wx],?", "\n"))
				.flatMap(e -> Stream.of(e.split(_0.regex.spaces)))
				.forEach(links::add);

		for (String link : links) {

			link = _0.trim(link);
			if (_0.empty(link)) {
				continue;
			}

			String scheme = _0.scheme(link, true);
			if (null != scheme) {
				// pass

			} else if (link.startsWith("://")) {
				link = "https" + link;

			} else if (link.startsWith("//")) {
				link = "https:" + link;

			} else if (link.startsWith("/")) {
				link = key.replaceAll("^(?<scheme>[^:]+):[/]+(?<host>[^@:/\\?&#]*).*", "${scheme}://${host}" + Matcher.quoteReplacement(link));

			} else {
				try {

					// TODO: esc
					String esc = link;
					esc = esc.replace(" ",  "%20");
					esc = esc.replace("　", "%E3%80%80");

					link = uri.resolve(esc).normalize().toString();

				} catch (IllegalArgumentException e) {
				}
			}

			scheme = _0.scheme(link, true);
			if ("https".equals(scheme)) {
				try {
					String prev = null;
					while (!link.equals(prev)) {
						prev = link;
						link = URLDecoder.decode(link, charset);
					}
				} catch (IllegalArgumentException e) {
				}
			}

			BigSet.of().add(new Entry(link));

		}

	}

}

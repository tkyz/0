package _0.exec.mime_type.application.pdf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import _0.bigset.BigSet.Entry;

public final class Impl {

	public final void run(final Entry entry) {

		Path obj = entry.obj(true);
		Path dir = entry.headdir();
		if (null == obj || null == dir) {
			return;
		}

		try (PDDocument doc = Loader.loadPDF(new RandomAccessReadBufferedFile(obj))) {

			int p = 0;
			for (PDPage page : doc.getPages()) {

				p++;

				PDResources res = page.getResources();
				for (COSName name : res.getXObjectNames()) {

					PDXObject o = res.getXObject(name);

					if (o instanceof PDImageXObject e) {

						Path head = dir.resolve(p + "_" + name.getName());
						if (!Files.exists(head)) {

							BufferedImage image = e.getImage();

							Files.createDirectories(head.getParent());
							ImageIO.write(image, "png", head.toFile());

						}

					} else if (o instanceof PDFormXObject) {
						// pass

					} else {
						entry.exceptions.add(new UnsupportedOperationException(o.getClass().getName()));
					}

				}

			}
		} catch (IOException e) {
			entry.exceptions.add(e);
		}

	}

}

package com.gattopiccolo.portfell.storage;

import com.gattopiccolo.portfell.config.UploadProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class HtmlStorageServiceTests {

	@TempDir
	Path uploadDirectory;

	@Test
	void storesHtmlFile() throws Exception {
		var service = new HtmlStorageService(new UploadProperties(uploadDirectory));
		var file = new MockMultipartFile("file", "page.html", "text/html", "<h1>Hello</h1>".getBytes());

		service.store(file);

		assertThat(Files.readString(uploadDirectory.resolve("page.html"))).isEqualTo("<h1>Hello</h1>");
	}

	@Test
	void rejectsNonHtmlFile() throws Exception {
		var service = new HtmlStorageService(new UploadProperties(uploadDirectory));
		var file = new MockMultipartFile("file", "notes.txt", "text/plain", "text".getBytes());

		assertThatIllegalArgumentException().isThrownBy(() -> service.store(file));
	}

	@Test
	void rejectsPathTraversal() throws Exception {
		var service = new HtmlStorageService(new UploadProperties(uploadDirectory));
		var file = new MockMultipartFile("file", "../outside.html", "text/html", "bad".getBytes());

		assertThatIllegalArgumentException().isThrownBy(() -> service.store(file));
		assertThat(uploadDirectory.getParent().resolve("outside.html")).doesNotExist();
	}

	@Test
	void deletesHtmlFile() throws Exception {
		var service = new HtmlStorageService(new UploadProperties(uploadDirectory));
		Files.writeString(uploadDirectory.resolve("page.html"), "<h1>Hello</h1>");

		service.delete("page.html");

		assertThat(uploadDirectory.resolve("page.html")).doesNotExist();
	}

	@Test
	void rejectsDeletingPathTraversal() throws Exception {
		var service = new HtmlStorageService(new UploadProperties(uploadDirectory));

		assertThatIllegalArgumentException().isThrownBy(() -> service.delete("../outside.html"));
	}
}

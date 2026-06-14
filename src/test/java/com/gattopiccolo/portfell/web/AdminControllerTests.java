package com.gattopiccolo.portfell.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.MOCK, properties = {
		"app.admin.username=test-admin",
		"app.admin.password=test-password"
})
@AutoConfigureMockMvc
class AdminControllerTests {

	@TempDir
	static Path uploadDirectory;

	@Autowired
	MockMvc mockMvc;

	@DynamicPropertySource
	static void properties(DynamicPropertyRegistry registry) {
		registry.add("app.upload.directory", uploadDirectory::toString);
	}

	@Test
	void redirectsAnonymousUserToLogin() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void authenticatedAdminCanUploadHtml() throws Exception {
		var file = new MockMultipartFile("file", "published.html", "text/html", "<p>Published</p>".getBytes());

		mockMvc.perform(multipart("/admin/upload")
						.file(file)
						.with(user("admin").roles("ADMIN"))
						.with(csrf()))
				.andExpect(status().is3xxRedirection());

		assertThat(Files.readString(uploadDirectory.resolve("published.html"))).isEqualTo("<p>Published</p>");
	}
}

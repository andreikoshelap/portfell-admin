package com.gattopiccolo.portfell.storage;

import com.gattopiccolo.portfell.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;

@Service
public class HtmlStorageService {

	private final Path uploadDirectory;

	public HtmlStorageService(UploadProperties properties) throws IOException {
		this.uploadDirectory = properties.directory().toAbsolutePath().normalize();
		Files.createDirectories(uploadDirectory);
	}

	public String store(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			throw new IllegalArgumentException("Выберите непустой HTML-файл.");
		}

		String fileName = file.getOriginalFilename();
		if (fileName == null || fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
			throw new IllegalArgumentException("Недопустимое имя файла.");
		}
		if (!fileName.toLowerCase(Locale.ROOT).endsWith(".html")) {
			throw new IllegalArgumentException("Разрешены только файлы с расширением .html.");
		}

		Path destination = uploadDirectory.resolve(fileName).normalize();
		if (!destination.getParent().equals(uploadDirectory)) {
			throw new IllegalArgumentException("Недопустимый путь файла.");
		}

		try (var inputStream = file.getInputStream()) {
			Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
		}
		return fileName;
	}

	public List<String> listFiles() throws IOException {
		try (var files = Files.list(uploadDirectory)) {
			return files
					.filter(Files::isRegularFile)
					.map(Path::getFileName)
					.map(Path::toString)
					.filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".html"))
					.sorted(String.CASE_INSENSITIVE_ORDER)
					.toList();
		}
	}

	public Path getFile(String fileName) {
		Path file = resolveHtmlFile(fileName);
		if (!Files.isRegularFile(file)) {
			throw new IllegalArgumentException("Файл «" + fileName + "» не найден.");
		}
		return file;
	}

	public void delete(String fileName) throws IOException {
		Path file = resolveHtmlFile(fileName);
		if (!Files.deleteIfExists(file)) {
			throw new IllegalArgumentException("Файл «" + fileName + "» не найден.");
		}
	}

	public Path getUploadDirectory() {
		return uploadDirectory;
	}

	private Path resolveHtmlFile(String fileName) {
		if (fileName == null || fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")
				|| !fileName.toLowerCase(Locale.ROOT).endsWith(".html")) {
			throw new IllegalArgumentException("Недопустимое имя файла.");
		}

		Path file = uploadDirectory.resolve(fileName).normalize();
		if (!file.getParent().equals(uploadDirectory)) {
			throw new IllegalArgumentException("Недопустимый путь файла.");
		}
		return file;
	}
}

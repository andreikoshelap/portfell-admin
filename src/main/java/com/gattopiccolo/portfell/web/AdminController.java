package com.gattopiccolo.portfell.web;

import com.gattopiccolo.portfell.storage.HtmlStorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
public class AdminController {

	private final HtmlStorageService storageService;

	public AdminController(HtmlStorageService storageService) {
		this.storageService = storageService;
	}

	@GetMapping("/")
	public String root() {
		return "redirect:/admin";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/admin")
	public String admin(Model model) throws IOException {
		model.addAttribute("files", storageService.listFiles());
		model.addAttribute("uploadDirectory", storageService.getUploadDirectory());
		return "admin";
	}

	@PostMapping("/admin/upload")
	public String upload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
		try {
			String fileName = storageService.store(file);
			redirectAttributes.addFlashAttribute("success", "Файл «" + fileName + "» загружен.");
		} catch (IllegalArgumentException | IOException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
		}
		return "redirect:/admin";
	}
}

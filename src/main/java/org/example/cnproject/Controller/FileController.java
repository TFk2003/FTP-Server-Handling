package org.example.cnproject.Controller;

import jakarta.servlet.http.HttpSession;
import org.example.cnproject.DTO.FileInfo;
import org.example.cnproject.Model.User;
import org.example.cnproject.Service.FTPService;
import org.example.cnproject.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FTPService ftpService;

    @Autowired
    private UserService userService;

    @GetMapping("/upload")
    public String showUploadPage(Model model, @AuthenticationPrincipal UserDetails  userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        String username = userDetails.getUsername();

        try {
            List<User> users = userService.findAllUsers();
            List<FileInfo> files = new ArrayList<>();
            for (User user : users) {
                files.addAll(ftpService.listUserFiles(user.getUsername()));
            }
            model.addAttribute("files", files);
        } catch (IOException e) {
            model.addAttribute("error", "Error connecting to FTP server. Please try again.");
            e.printStackTrace();
        }
        return "file-transfer";
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        String username = userDetails.getUsername();

        if (file.isEmpty()) {
            model.addAttribute("error", "Please select a file to upload");
            return "file-transfer";
        }
        try {
            String fileUrl = ftpService.uploadFile(file, username);
            model.addAttribute("message", "File uploaded successfully!");
            model.addAttribute("fileUrl", fileUrl);
            return "redirect:/files/upload";
        } catch (IOException e) {
            model.addAttribute("error", "Failed to upload file: " + e.getMessage());
            return "file-transfer";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping("/download/{username}/{filename:.+}")
    public ResponseEntity<InputStreamResource> downloadFile(@PathVariable String username,
                                                            @PathVariable String filename) throws IOException {
        InputStreamResource resource = new InputStreamResource(ftpService.downloadFile(username, filename));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}

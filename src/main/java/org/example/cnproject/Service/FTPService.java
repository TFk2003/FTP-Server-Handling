package org.example.cnproject.Service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.example.cnproject.DTO.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class FTPService {
    @Autowired
    private DefaultFtpSessionFactory ftpSessionFactory;
    @Value("${ftp.server.host}")
    private String host;

    @Value("${ftp.server.port}")
    private int port;

    @Value("${ftp.server.username}")
    private String username2;

    @Value("${ftp.server.password}")
    private String password;

    @Value("${ftp.server.base-directory}")
    private String baseDirectory;

    public String uploadFile(MultipartFile file, String username) throws IOException, InterruptedException {
        FTPClient ftpClient = new FTPClient();
        int retryCount = 0;
        final int MAX_RETRIES = 2;
        while (retryCount < MAX_RETRIES) {
        try {
            ftpClient.setConnectTimeout(30000);
            ftpClient.setDataTimeout(60000);
            ftpClient.setControlKeepAliveTimeout(300);
            ftpClient.setAutodetectUTF8(true);

            System.out.println("Connecting to FTP server...");
            System.out.println("Attempt #" + (retryCount + 1));
            ftpClient.connect(host, port);
            printFtpReplies(ftpClient);
            System.out.println("Attempting login with: " + username2);
            if (!ftpClient.login(username2, password)) {
                throw new IOException("Login failed. Server response: " + ftpClient.getReplyString());
            }
           // ftpClient.execPBSZ(0);
           // ftpClient.execPROT("P");
            System.out.println("Login successful. Reply: " + ftpClient.getReplyString());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setRemoteVerificationEnabled(false); // disables host check

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(1024 * 1024);
            System.out.println("Passive mode: " + ftpClient.getPassiveHost() + ":" + ftpClient.getPassivePort());
            String userDir = baseDirectory + "/" + username;
            System.out.println("Target directory: " + userDir);

            if (!userDir.startsWith("/htdocs")) {
                System.err.println("Unsafe directory path: " + userDir);
                if (retryCount < MAX_RETRIES - 1) {
                    retryCount++;
                    Thread.sleep(1000);
                    continue;  // retry once
                } else {
                    throw new IOException("Unsafe directory path after retry: " + userDir);
                }
            }
            if (!ftpClient.changeWorkingDirectory(userDir)) {
                System.out.println("Creating user directory: " + userDir);
                if (!ftpClient.makeDirectory(userDir)) {
                    throw new IOException("Couldn't create user directory" + ftpClient.getReplyString());
                }
                if (!ftpClient.changeWorkingDirectory(userDir)) {
                    throw new IOException("Failed to change to the new directory: " + userDir);
                }
            }
            System.out.println("Uploading: " + file.getOriginalFilename());
            try (InputStream inputStream = file.getInputStream()) {
                boolean success = ftpClient.storeFile(file.getOriginalFilename(), inputStream);
                System.out.println("File store success: " + success);
                System.out.println("Reply: " + ftpClient.getReplyString());

                if (!success) {
                    throw new IOException("FTP store command failed");
                }
            }

            return "/files/download/" + username + "/" + file.getOriginalFilename();
        } catch (IOException e) {
            System.err.println("Attempt #" + (retryCount + 1) + " failed: " + e.getMessage());
            if (retryCount == MAX_RETRIES - 1) {
                throw e;
            }
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.noop(); // Verify connection alive
                    ftpClient.logout();
                } catch (IOException e) {
                    System.err.println("Cleanup error: " + e.getMessage());
                }
            }
        }

            retryCount++;
            try {
                Thread.sleep(1000); // Wait before retry
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        throw new IOException("All upload attempts failed");
    }
    private void printFtpReplies(FTPClient ftp) {
        for (String reply : ftp.getReplyStrings()) {
            System.out.println("FTP: " + reply);
        }
    }
    public List<FileInfo> listUserFiles(String username) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(30000);
        ftpClient.setDataTimeout(60000);
        ftpClient.setControlKeepAliveTimeout(300);
        ftpClient.setAutodetectUTF8(true);

        ftpClient.connect(host, port);
        if (!ftpClient.login(this.username2, this.password)) {
            throw new IOException("Login failed: " + ftpClient.getReplyString());
        }
        //ftpClient.execPBSZ(0);
        //ftpClient.execPROT("P");

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);


        String userDirectory = baseDirectory + "/" + username;
        List<FileInfo> files = new ArrayList<>();

        if (ftpClient.changeWorkingDirectory(userDirectory)) {
            for (org.apache.commons.net.ftp.FTPFile ftpFile : ftpClient.listFiles()) {
                String name = ftpFile.getName();
                if (name.equals(".") || name.equals("..")) continue;
                FileInfo fileInfo = new FileInfo();
                fileInfo.setName(ftpFile.getName());
                fileInfo.setSize(ftpFile.getSize());
                fileInfo.setUploadDate(ftpFile.getTimestamp().getTime().toString());
                fileInfo.setUrl("/files/download/" + username + "/" + ftpFile.getName());
                files.add(fileInfo);
            }
        }

        ftpClient.logout();
        ftpClient.disconnect();
        return files;
    }

    public InputStream downloadFile(String username, String filename) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(host, port);
        ftpClient.login(this.username2, this.password);
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        String filePath = baseDirectory + "/" + username + "/" + filename;
        InputStream inputStream = ftpClient.retrieveFileStream(filePath);

        if (inputStream == null) {
            ftpClient.logout();
            ftpClient.disconnect();
            throw new IOException("File not found: " + filePath);
        }

        return new InputStream() {
            @Override
            public int read() throws IOException {
                return inputStream.read();
            }

            @Override
            public void close() throws IOException {
                inputStream.close();
                ftpClient.completePendingCommand();
                ftpClient.logout();
                ftpClient.disconnect();
            }
        };
    }
}

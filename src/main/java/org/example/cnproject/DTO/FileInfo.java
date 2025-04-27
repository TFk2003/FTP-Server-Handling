package org.example.cnproject.DTO;

import lombok.Data;

@Data
public class FileInfo {
    private String name;
    private String url;
    private long size;
    private String uploadDate;
}

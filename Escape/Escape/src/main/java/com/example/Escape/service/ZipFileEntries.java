package com.example.Escape.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipFileEntries implements Iterable<ZipFileEntries.ZipFileEntry> {

    private final ZipFile zipFile;

    public ZipFileEntries(ZipFile zipFile) {
        this.zipFile = zipFile;
    }

    public static ZipFileEntries fromInputStream(InputStream inputStream) throws IOException {
        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipFile zipFile = createTempZipFile(zipInputStream);
            return new ZipFileEntries(zipFile);
        }
    }

    private static ZipFile createTempZipFile(ZipInputStream zipInputStream) throws IOException {
        Path tempFile = Files.createTempFile("temp", ".zip");
        try (ZipOutputStream tempZipOutputStream = new ZipOutputStream(Files.newOutputStream(tempFile, StandardOpenOption.CREATE))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                tempZipOutputStream.putNextEntry(entry);
                while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                    tempZipOutputStream.write(buffer, 0, bytesRead);
                }
                tempZipOutputStream.closeEntry();
            }
        }
        return new ZipFile(tempFile.toFile());
    }

    @Override
    public Iterator<ZipFileEntry> iterator() {
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return entries.hasMoreElements();
            }

            @Override
            public ZipFileEntry next() {
                ZipEntry entry = entries.nextElement();
                return new ZipFileEntry(entry);
            }
        };
    }

    public ZipFile getZipFile() {
        return zipFile;
    }

    public class ZipFileEntry {
        private final ZipEntry zipEntry;

        private ZipFileEntry(ZipEntry zipEntry) {
            this.zipEntry = zipEntry;
        }

        public String getName() {
            return zipEntry.getName();
        }

        public boolean isDirectory() {
            return zipEntry.isDirectory();
        }

        public InputStream getInputStream() throws IOException {
            return zipFile.getInputStream(zipEntry);
        }
    }
}

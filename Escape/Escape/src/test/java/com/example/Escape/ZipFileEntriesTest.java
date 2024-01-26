package com.example.Escape;

import com.example.Escape.service.ZipFileEntries;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

public class ZipFileEntriesTest {

    @Test
    public void testFromInputStream() throws IOException {
        // Test verilerini içeren bir zip dosyası oluşturma
        Path tempZipFile = Files.createTempFile("test", ".zip");
        try (ZipOutputStream tempZipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZipFile, StandardOpenOption.CREATE))) {
            ZipEntry entry = new ZipEntry("file.txt");
            tempZipOutputStream.putNextEntry(entry);
            tempZipOutputStream.write("Content of file".getBytes());
            tempZipOutputStream.closeEntry();
        }

        // Zip dosyasının içeriği
        byte[] zipContent = Files.readAllBytes(tempZipFile);

        // ByteArrayInputStream kullanarak InputStream oluşturma
        ByteArrayInputStream inputStream = new ByteArrayInputStream(zipContent);

        // fromInputStream metodunu kullanarak ZipFileEntries oluşturma
        ZipFileEntries zipFileEntries = ZipFileEntries.fromInputStream(inputStream);

        // Iterator ve içerik kontrolü
        Iterator<ZipFileEntries.ZipFileEntry> iterator = zipFileEntries.iterator();
        assertTrue(iterator.hasNext());
        ZipFileEntries.ZipFileEntry zipFileEntry = iterator.next();
        assertEquals("file.txt", zipFileEntry.getName());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testGetInputStream() throws IOException {
        // Geçici zip dosyası
        Path tempZipFile = Files.createTempFile("test", ".zip");
        try (ZipOutputStream tempZipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZipFile, StandardOpenOption.CREATE))) {
            // Test verilerini zip dosyasına ekleme
            ZipEntry entry = new ZipEntry("file.txt");
            tempZipOutputStream.putNextEntry(entry);
            tempZipOutputStream.write("Content of file".getBytes());
            tempZipOutputStream.closeEntry();
        }

        try (ZipFile zipFile = new ZipFile(tempZipFile.toFile())) {
            ZipFileEntries zipFileEntries = new ZipFileEntries(zipFile);

            Iterator<ZipFileEntries.ZipFileEntry> iterator = zipFileEntries.iterator();
            assertTrue(iterator.hasNext());
            ZipFileEntries.ZipFileEntry zipFileEntry = iterator.next();

            // InputStream'i kontrol et
            try (InputStream inputStream = zipFileEntry.getInputStream()) {
                byte[] content = inputStream.readAllBytes();
                assertEquals("Content of file", new String(content));
            }
        }
    }

    @Test
    public void testIterator() throws IOException {
        // Geçici zip dosyası oluşturma ve test verilerini zip dosyasına ekleme
        Path tempZipFile = Files.createTempFile("test", ".zip");
        try (ZipOutputStream tempZipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZipFile, StandardOpenOption.CREATE))) {
            ZipEntry entry1 = new ZipEntry("file1.txt");
            tempZipOutputStream.putNextEntry(entry1);
            tempZipOutputStream.write("Content of file1".getBytes());
            tempZipOutputStream.closeEntry();

            ZipEntry entry2 = new ZipEntry("folder/file2.txt");
            tempZipOutputStream.putNextEntry(entry2);
            tempZipOutputStream.write("Content of file2".getBytes());
            tempZipOutputStream.closeEntry();
        }

        // ZipFileEntries sınıfını oluşturma
        try (ZipFile zipFile = new ZipFile(tempZipFile.toFile())) {
            ZipFileEntries zipFileEntries = new ZipFileEntries(zipFile);

            Iterator<ZipFileEntries.ZipFileEntry> iterator = zipFileEntries.iterator();
            // Expected ZipFileEntry kontrolleri
            assertTrue(iterator.hasNext());
            iterator.next(); // entry1
            assertTrue(iterator.hasNext());
            iterator.next(); // entry2
            assertFalse(iterator.hasNext());
        }
    }
    @Test
    public void testGetZipFile() throws IOException {

        Path tempZipFile = Files.createTempFile("test", ".zip");
        try (ZipOutputStream tempZipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZipFile, StandardOpenOption.CREATE))) {
            ZipEntry entry = new ZipEntry("file.txt");
            tempZipOutputStream.putNextEntry(entry);
            tempZipOutputStream.write("Content of file".getBytes());
            tempZipOutputStream.closeEntry();
        }

        try (ZipFile zipFile = new ZipFile(tempZipFile.toFile())) {
            ZipFileEntries zipFileEntries = new ZipFileEntries(zipFile);
            // getZipFile metodu ile ZipFile'ı kontrol etme
            ZipFile retrievedZipFile = zipFileEntries.getZipFile();
            assertEquals(tempZipFile.toString(), retrievedZipFile.getName());
        }
    }
}

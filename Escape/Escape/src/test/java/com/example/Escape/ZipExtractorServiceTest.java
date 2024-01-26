package com.example.Escape;

import com.example.Escape.service.ZipExtractorService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import static org.junit.jupiter.api.Assertions.*;

public class ZipExtractorServiceTest {

    @Test
    public void testExtractAndCompareFiles() throws IOException {
        // Test verilerini içeren örnek iki zip dosyası oluşturma

        Path studentZipPath = createTempZipFile("studentsolutions", "Q1/question1.java", "Content of student's answer");
        Path instructorZipPath = createTempZipFile("instructorsolutions", "Q1/question1.java", "Content of instructor's answer");

        MockMultipartFile studentZipFile = new MockMultipartFile("studentZip", Files.readAllBytes(studentZipPath));
        MockMultipartFile instructorZipFile = new MockMultipartFile("instructorZip", Files.readAllBytes(instructorZipPath));


        ZipExtractorService zipExtractorService = new ZipExtractorService();

        String result = zipExtractorService.extractAndCompareFiles(studentZipFile, instructorZipFile, 1);
        assertNotNull(result);
        assertTrue(result.contains("Total Score"));
    }

    @Test
    public void testGetInstructorFolderPath() {
        ZipExtractorService zipExtractorService = new ZipExtractorService();
        String instructorFolderPath = zipExtractorService.getInstructorFolderPath();
        assertNotNull(instructorFolderPath);
        assertTrue(instructorFolderPath.contains("instructorsolutions"));
    }

    private Path createTempZipFile(String directory, String filePath, String fileContent) throws IOException {
        Path tempZipFile = Files.createTempFile("test", ".zip");

        try (ZipOutputStream tempZipOutputStream = new ZipOutputStream(Files.newOutputStream(tempZipFile))) {

            ZipEntry entry = new ZipEntry(directory + "/" + filePath);
            tempZipOutputStream.putNextEntry(entry);
            tempZipOutputStream.write(fileContent.getBytes());
            tempZipOutputStream.closeEntry();
        }

        return tempZipFile;
    }
}

package com.example.Escape;

import com.example.Escape.controller.FileUploadController;
import com.example.Escape.service.ZipExtractorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileUploadControllerTest {

    @Mock
    private ZipExtractorService zipExtractorService;

    @InjectMocks
    private FileUploadController fileUploadController;

    @Mock
    private Model model;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleFileUpload() throws IOException {
        MockMultipartFile studentZipFile = createMockZipFile("student.zip");
        MockMultipartFile instructorZipFile = createMockZipFile("instructor.zip");
        String expectedResult = "Mock result from zipExtractorService";
        when(zipExtractorService.extractAndCompareFiles(studentZipFile, instructorZipFile, 5)).thenReturn(expectedResult);

        String result = fileUploadController.handleFileUpload(studentZipFile, instructorZipFile, 5, model);

        verify(model).addAttribute("result", expectedResult);
        verify(zipExtractorService).extractAndCompareFiles(studentZipFile, instructorZipFile, 5);

        assertEquals("result", result);
    }

    private MockMultipartFile createMockZipFile(String fileName) {

        return null;
    }
}

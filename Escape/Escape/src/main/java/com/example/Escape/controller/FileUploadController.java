package com.example.Escape.controller;

import com.example.Escape.service.ZipExtractorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Controller
public class FileUploadController {

    private final ZipExtractorService zipExtractorService;
    private static final String OUTPUT_PATH = "src/main/output";

    @Autowired
    public FileUploadController(ZipExtractorService zipExtractorService) {
        this.zipExtractorService = zipExtractorService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/uploadFiles")
    public String handleFileUpload(
            @RequestParam("studentZipFile") MultipartFile studentZipFile,
            @RequestParam("instructorZipFile") MultipartFile instructorZipFile,
            @RequestParam("questionCount") int questionCount,
            Model model) {
        try {
            String result = zipExtractorService.extractAndCompareFiles(studentZipFile, instructorZipFile, questionCount);

            model.addAttribute("result", result);


            List<String> studentFolders = zipExtractorService.getStudentFolders(studentZipFile);
            for (String studentFolder : studentFolders) {
                String studentPath = zipExtractorService.getStudentFolderPath(studentFolder);
                String instructorPath = zipExtractorService.getInstructorFolderPath();

                String report = createReport(studentFolder, studentPath, instructorPath, questionCount);
                saveReport(studentFolder, report);
            }

            return "result";
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred during file extraction and comparison.";
        }
    }

    private String createReport(String studentFolder, String studentPath, String instructorPath, int questionCount) throws IOException {
        StringBuilder resultBuilder = new StringBuilder();
        int totalScore = 0;

        resultBuilder.append("Student: ").append(studentFolder).append("\n");

        for (int questionNumber = 1; questionNumber <= questionCount; questionNumber++) {
            String studentAnswerPath = studentPath + "/Q" + questionNumber + "/question" + questionNumber + ".java";
            String instructorAnswerPath = instructorPath + "/Q" + questionNumber + "/question" + questionNumber + ".java";


            if (Files.exists(Path.of(studentAnswerPath)) && Files.exists(Path.of(instructorAnswerPath))) {
                String studentContent = Files.readString(Path.of(studentAnswerPath));
                String instructorContent = Files.readString(Path.of(instructorAnswerPath));

                boolean isCorrect = studentContent.equals(instructorContent);
                totalScore += isCorrect ? 10 : 0;

                resultBuilder.append("  Question ").append(questionNumber).append(": ").append(isCorrect ? "Passed" : "Failed").append("\n");
            } else {
                resultBuilder.append("  Question ").append(questionNumber).append(": Not Found\n");
            }
        }

        resultBuilder.append("Total Score: ").append(totalScore).append("\n\n");

        return resultBuilder.toString();
    }

    private void saveReport(String studentFolder, String report) throws IOException {
        Path reportPath = Path.of(OUTPUT_PATH, "reports", studentFolder + "_report.txt");
        Files.createDirectories(reportPath.getParent());

        try (PrintWriter writer = new PrintWriter(new FileWriter(reportPath.toString(), true))) {
            writer.println(report);
        }
    }
}

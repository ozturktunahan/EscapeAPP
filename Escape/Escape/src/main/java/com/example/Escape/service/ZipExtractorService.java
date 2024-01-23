package com.example.Escape.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

@Service
public class ZipExtractorService {

    private static final String OUTPUT_PATH = "src/main/output";

    public String extractAndCompareFiles(MultipartFile studentZipFile, MultipartFile instructorZipFile, int questionCount) {
        try {

            Path studentPath = extractZipFile(studentZipFile, "studentsolutions");
            Path instructorPath = extractZipFile(instructorZipFile, "instructorsolutions");

            StringBuilder resultBuilder = new StringBuilder();
            int totalScore = 0;

            List<String> studentFolders = getStudentFolders(studentPath);

            for (String studentFolder : studentFolders) {
                resultBuilder.append("Student: ").append(studentFolder).append("\n");

                for (int questionNumber = 1; questionNumber <= questionCount; questionNumber++) {
                    String studentAnswerPath = studentPath.resolve(studentFolder + "/Q" + questionNumber + "/question" + questionNumber + ".java").toString();
                    String instructorAnswerPath = instructorPath.resolve("/Q" + questionNumber + "/question" + questionNumber + ".java").toString();


                    if (Files.exists(Path.of(studentAnswerPath)) && Files.exists(Path.of(instructorAnswerPath))) {
                        System.out.println("Student Content:\n" + Files.readString(Path.of(studentAnswerPath)));
                        System.out.println("Instructor Content:\n" + Files.readString(Path.of(instructorAnswerPath)));

                        String studentContent = Files.readString(Path.of(studentAnswerPath));
                        String instructorContent = Files.readString(Path.of(instructorAnswerPath));

                        boolean isCorrect = studentContent.equals(instructorContent);
                        totalScore += isCorrect ? 10 : 0;

                        resultBuilder.append("  Question ").append(questionNumber).append(": ").append(isCorrect ? "Passed" : "Failed").append("\n");
                    } else {

                        resultBuilder.append("  Question ").append(questionNumber).append(": Not Found\n");
                    }
                }

                resultBuilder.append("\n");
            }

            resultBuilder.insert(0, "Total Score: " + totalScore + "\n\n");
            return resultBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occurred during file extraction and comparison.";
        }
    }


    public List<String> getStudentFolders(MultipartFile studentZipFile) throws IOException {
        Path studentPath = extractZipFile(studentZipFile, "studentsolutions");
        return getStudentFolders(studentPath);
    }

    public String getStudentFolderPath(String studentFolder) {
        return Paths.get(OUTPUT_PATH, "studentsolutions","studentsolutions", studentFolder).toString();
    }

    public String getInstructorFolderPath() {
        return Paths.get(OUTPUT_PATH, "instructorsolutions","instructorsolutions").toString();
    }

    private List<String> getStudentFolders(Path studentPath) throws IOException {
        List<String> studentFolders = new ArrayList<>();

        try (Stream<Path> studentFiles = Files.walk(studentPath, 2)) {
            studentFiles
                    .filter(path -> Files.isDirectory(path) && !path.equals(studentPath))
                    .forEach(path -> {
                        String folderName = path.getFileName().toString();
                        studentFolders.add(folderName);
                    });
        }

        return studentFolders;
    }

    private Path extractZipFile(MultipartFile zipFile, String destinationDirectory) throws IOException {
        try (var zipInputStream = zipFile.getInputStream()) {
            var zipFileEntries = ZipFileEntries.fromInputStream(zipInputStream);

            Path destPath = Path.of(OUTPUT_PATH, destinationDirectory);
            if (!Files.exists(destPath)) {
                Files.createDirectories(destPath);
            }

            zipFileEntries.forEach(entry -> {

                Path entryPath = destPath.resolve(entry.getName());
                if (entry.isDirectory()) {
                    try {
                        Files.createDirectories(entryPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try (var entryInputStream = entry.getInputStream()) {
                        Files.copy(entryInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            return destPath;
        }
    }
}

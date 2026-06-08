package com.codewithmd.blogger.bloggerappsapis.config;

import com.codewithmd.blogger.bloggerappsapis.payloads.BlogAI;
import com.codewithmd.blogger.bloggerappsapis.payloads.FileData;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.codewithmd.blogger.bloggerappsapis.payloads.BlogAI;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileParserUtil {
    /**
     * Accepts an array of MultipartFiles.
     * Each file can be:
     *   - A single .docx / .pdf / .txt (or any other type)
     *   - A .zip containing multiple files
     *
     * Returns a flat List<FileData> with fileName and base64-encoded fileData.
     */
    public static List<FileData> fetchMultipleFiles(MultipartFile[] files) throws Exception {
        List<FileData> result = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            byte[] bytes = file.getBytes();
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("One of the uploaded files has no name.");
            }

            if (fileName.endsWith(".zip")) {
                // Extract all files from zip and convert each to base64
                List<FileData> fromZip = extractFromZip(bytes);
                result.addAll(fromZip);
            } else {
                // Convert single file directly to base64
                String base64Data = Base64.getEncoder().encodeToString(bytes);
                result.add(new FileData(fileName, base64Data));
            }
        }

        return result;
    }

    // ─────────────────────────────────────────────
    // ZIP: extract each entry and convert to base64
    // ─────────────────────────────────────────────
    private static List<FileData> extractFromZip(byte[] zipBytes) throws Exception {
        List<FileData> result = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    zis.closeEntry();
                    continue;
                }

                String entryName = entry.getName();
                // Strip folder path inside zip e.g. "folder/file.docx" → "file.docx"
                String simpleFileName = entryName.contains("/")
                        ? entryName.substring(entryName.lastIndexOf("/") + 1)
                        : entryName;

                byte[] entryBytes = zis.readAllBytes();
                String base64Data = Base64.getEncoder().encodeToString(entryBytes);

                result.add(new FileData(simpleFileName, base64Data));

                zis.closeEntry();
            }
        }

        return result;
    }

    // ─────────────────────────────────────────────
// ✅ NEW: Public method to parse single file from bytes
// Called by SqsConsumerService
// ─────────────────────────────────────────────
    public static BlogAI parseSingleFileFromBytes(String fileName, byte[] bytes) throws Exception {
        String rawText;

        if (fileName.endsWith(".docx")) {
            rawText = extractFromDocx(bytes);
        } else if (fileName.endsWith(".txt")) {
            rawText = new String(bytes);
        } else if (fileName.endsWith(".doc")) {
            // ✅ Add .doc support using Apache POI HWPF
            return parseDoc(fileName, bytes);

        } else if (fileName.endsWith(".pdf")) {
            rawText = extractFromPdf(bytes);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported file type: '" + fileName + "'. Allowed: .docx, .pdf, .txt"
            );
        }

        return extractTitleAndContent(fileName, rawText);
    }

    private static BlogAI parseDoc(String fileName, byte[] bytes) throws Exception {
        try (HWPFDocument doc = new HWPFDocument(new ByteArrayInputStream(bytes))) {
            WordExtractor extractor = new WordExtractor(doc);
            String content = extractor.getText();
            String title = fileName.replace(".doc", "").replace("_", " ");

            BlogAI blog = new BlogAI();
            blog.setTitle(title);
            blog.setContent(content);
            return blog;
        }
    }

    // ─────────────────────────────────────────────
    // .docx extractor (Apache POI)
    // ─────────────────────────────────────────────
    private static String extractFromDocx(byte[] bytes) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            for (XWPFParagraph para : doc.getParagraphs()) {
                String text = para.getText().trim();
                if (!text.isBlank()) {
                    sb.append(text).append("\n");
                }
            }
        }
        return sb.toString();
    }

    // ─────────────────────────────────────────────
    // .pdf extractor (Apache PDFBox)
    // ─────────────────────────────────────────────
    // ✅ CORRECT for PDFBox 3.x
    private static String extractFromPdf(byte[] bytes) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument doc =
                     org.apache.pdfbox.Loader.loadPDF(bytes)) {
            org.apache.pdfbox.text.PDFTextStripper stripper =
                    new org.apache.pdfbox.text.PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    // ───────────────────────────────────ss──────────
    // Title + Content extractor & validator
    // ─────────────────────────────────────────────
    private static BlogAI extractTitleAndContent(String fileName, String rawText) {
        String title = null;
        String content = null;

        String[] lines = rawText.split("\\r?\\n");

        for (String line : lines) {
            // Remove **bold** markers that docx/pdf exports sometimes add
            String cleaned = line.replaceAll("\\*\\*", "").trim();

            if (cleaned.toLowerCase().startsWith("title:")) {
                title = cleaned.substring("title:".length()).trim();
            } else if (cleaned.toLowerCase().startsWith("content:")) {
                content = cleaned.substring("content:".length()).trim();
            }
        }

        // ── Validation ──────────────────────────────
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "File '" + fileName + "' is missing a valid 'Title:' field."
            );
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "File '" + fileName + "' is missing a valid 'Content:' field."
            );
        }

        BlogAI blog = new BlogAI();
        blog.setTitle(title);
        blog.setContent(content);
        return blog;
    }
}
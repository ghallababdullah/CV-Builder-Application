package components.CVBuilder.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class LatexToPdfConverter {
    public static void convertLatexToPdf(String texPath, String pdfOutputPath)
            throws IOException, InterruptedException {

        File texFile = new File(texPath);
        File outputDir = texFile.getParentFile();

        ProcessBuilder pb = new ProcessBuilder(
            "xelatex",
            "-interaction=nonstopmode",
            "-output-directory=" + outputDir.getAbsolutePath(),
            texFile.getName()
        );

        pb.directory(outputDir);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[XeLaTeX] " + line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("xelatex failed (exit code " + exitCode + ")");
        }

        File generatedPdf = new File(texPath.replace(".tex", ".pdf"));
        if (!generatedPdf.renameTo(new File(pdfOutputPath))) {
            throw new IOException("Failed to move PDF to destination");
        }
    }
}
package components.CVBuilder.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import components.CVBuilder.model.CV;
import components.CVBuilder.model.Education;
import components.CVBuilder.model.Experience;
import components.CVBuilder.model.Language;
import components.CVBuilder.model.Skill;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class LaTeXGenerator {

    public static void generateLatexFile(CV cv, String fileName) throws Exception {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
        cfg.setDefaultEncoding("UTF-8");

        // ✅ Load templates from resources (works inside JAR)
        cfg.setClassLoaderForTemplateLoading(
            Thread.currentThread().getContextClassLoader(),
            "components/CVBuilder/resources/templates"
        );

        // ✅ Fallback for IDE/dev mode
        File devTemplateDir = new File("src/main/resources/components/CVBuilder/resources/templates");
        if (devTemplateDir.exists()) {
            cfg.setDirectoryForTemplateLoading(devTemplateDir);
        }

        // Use a safe output directory
        File outputDir = new File(System.getProperty("user.dir"), "output_temp");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Build full path for LaTeX file
     // Ensure fileName is just a file name (not full path)
        String safeName = new File(fileName).getName();
        if (!safeName.endsWith(".tex")) {
            safeName += ".tex";
        }

        File outputFile = new File(outputDir, safeName);

        // Create CV data map
        Map<String, Object> cvData = new HashMap<>();
        cvData.put("title", escapeLaTeX(cv.getTitle()));
        cvData.put("fullName", escapeLaTeX(cv.getFullName()));
        cvData.put("email", escapeLaTeX(cv.getEmail()));
        cvData.put("phone", escapeLaTeX(cv.getPhone()));

        if (cv.getAddress() != null) cvData.put("address", escapeLaTeX(cv.getAddress()));
        if (cv.getSummary() != null) cvData.put("summary", escapeLaTeX(cv.getSummary()));

        cvData.put("education", prepareEducationList(cv.getEducation()));
        cvData.put("experience", prepareExperienceList(cv.getExperience()));
        cvData.put("skills", prepareSkillsList(cv.getSkills()));
        cvData.put("languages", prepareLanguagesList(cv.getLanguages()));

        Map<String, Object> data = new HashMap<>();
        data.put("cv", cvData);

        // Write LaTeX file
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8)) {
            Template template = cfg.getTemplate("cv_template.ftl");
            template.process(data, writer);
        }

        System.out.println("✅ LaTeX file generated at: " + outputFile.getAbsolutePath());
    }

    private static String formatPathForLatex(String path) {
        if (path == null) return "";
        return path.replace("\\", "/").replace(" ", "\\ ");
    }

    private static String escapeLaTeX(String input) {
        if (input == null) return "";
        return input.replace("&", "\\&")
                    .replace("%", "\\%")
                    .replace("$", "\\$")
                    .replace("#", "\\#")
                    .replace("_", "\\_")
                    .replace("{", "\\{")
                    .replace("}", "\\}");
    }

    private static List<Map<String, String>> prepareEducationList(List<Education> education) {
        if (education == null) return Collections.emptyList();

        List<Map<String, String>> eduList = new ArrayList<>();
        for (Education edu : education) {
            Map<String, String> eduMap = new HashMap<>();
            eduMap.put("institution", escapeLaTeX(edu.getInstitution()));
            eduMap.put("degree", escapeLaTeX(edu.getDegree()));
            eduMap.put("fieldOfStudy", escapeLaTeX(edu.getFieldOfStudy()));
            eduMap.put("startDate", edu.getStartDate() != null ?
                    edu.getStartDate().format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)) : "Present");
            eduMap.put("endDate", edu.getEndDate() != null ?
                    edu.getEndDate().format(DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH)): "Present");
            eduList.add(eduMap);
        }
        return eduList;
    }

    private static List<Map<String, String>> prepareExperienceList(List<Experience> experience) {
        if (experience == null) return Collections.emptyList();

        List<Map<String, String>> expList = new ArrayList<>();
        for (Experience exp : experience) {
            Map<String, String> expMap = new HashMap<>();
            expMap.put("company", escapeLaTeX(exp.getCompany()));
            expMap.put("position", escapeLaTeX(exp.getPosition()));
            expMap.put("location", escapeLaTeX(exp.getLocation()));
            expMap.put("startDate", formatDate(exp.getStartDate()));
            expMap.put("endDate", formatDate(exp.getEndDate()));
            expMap.put("description", escapeLaTeX(exp.getDescription()));
            expList.add(expMap);
        }
        return expList;
    }

    private static List<Map<String, String>> prepareSkillsList(List<Skill> skills) {
        if (skills == null) return Collections.emptyList();

        List<Map<String, String>> skillList = new ArrayList<>();
        for (Skill skill : skills) {
            Map<String, String> skillMap = new HashMap<>();
            skillMap.put("name", escapeLaTeX(skill.getName()));
            skillMap.put("level", String.valueOf(skill.getLevel()));
            skillList.add(skillMap);
        }
        return skillList;
    }

    private static List<Map<String, String>> prepareLanguagesList(List<Language> languages) {
        if (languages == null) return Collections.emptyList();

        List<Map<String, String>> langList = new ArrayList<>();
        for (Language lang : languages) {
            Map<String, String> langMap = new HashMap<>();
            langMap.put("name", escapeLaTeX(lang.getName()));
            langMap.put("proficiency", escapeLaTeX(lang.getProficiency()));
            langList.add(langMap);
        }
        return langList;
    }

    private static String formatDate(LocalDate date) {
        if (date == null) return "Present";
        return date.format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale.ENGLISH));
    }

    private static String escapeLaTeX1(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\textbackslash")
                    .replace("&", "\\&")
                    .replace("%", "\\%")
                    .replace("$", "\\$")
                    .replace("#", "\\#")
                    .replace("_", "\\_")
                    .replace("{", "\\{")
                    .replace("}", "\\}");
    }
}

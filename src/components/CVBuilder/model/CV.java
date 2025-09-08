
package components.CVBuilder.model;

import java.util.ArrayList;
import java.util.List;

public class CV {
    private int id;
    private int userId;
    private String title;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String summary;
    private List<Education> education;
    private List<Experience> experience;
    private List<Skill> skills;
    private List<Language> languages;

    public CV() {
        education = new ArrayList<>();
        experience = new ArrayList<>();
        skills = new ArrayList<>();
        languages = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Education> getEducation() {
        return education;
    }

    public void setEducation(List<Education> education) {
        this.education = education;
    }

    public List<Experience> getExperience() {
        return experience;
    }

    public void setExperience(List<Experience> experience) {
        this.experience = experience;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(List<Language> languages) {
        this.languages = languages;
    }

    public void addEducation(Education edu) {
        education.add(edu);
    }

    public void addExperience(Experience exp) {
        experience.add(exp);
    }

    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    public void addLanguage(Language language) {
        languages.add(language);
    }

}

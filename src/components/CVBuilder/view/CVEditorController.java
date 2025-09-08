
package components.CVBuilder.view;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import components.CVBuilder.MainApp;
import components.CVBuilder.model.CV;
import components.CVBuilder.model.Education;
import components.CVBuilder.model.Experience;
import components.CVBuilder.model.Language;
import components.CVBuilder.model.Skill;
import components.CVBuilder.model.User;
import components.CVBuilder.util.DatabaseHandler;
import components.CVBuilder.util.LaTeXGenerator;
import components.CVBuilder.util.LatexToPdfConverter;
import components.CVBuilder.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;

public class CVEditorController {
    @FXML
    private TextField cvTitleField;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField addressField;

    @FXML
    private TextArea summaryArea;

    @FXML
    private ListView<Education> educationListView;

    @FXML
    private ListView<Experience> experienceListView;

    @FXML
    private ListView<Skill> skillsListView;

    @FXML
    private ListView<Language> languagesListView;

    private MainApp mainApp;
    private CV currentCV;

    @FXML
    private void initialize() {
        // Setup cell factories for list views
        setupEducationListView();
        setupExperienceListView();
        setupSkillsListView();
        setupLanguagesListView();
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void loadUserCV(User user, int cvId) {
        if (cvId > 0) {
            // Load specific CV
            this.currentCV = DatabaseHandler.getInstance().getCVById(cvId);
        } else {
            // Create new CV
            this.currentCV = new CV();
            this.currentCV.setUserId(user.getId());
        }

        // Fill fields with CV data (or clear them for new CV)
        cvTitleField.setText(currentCV.getTitle() != null ? currentCV.getTitle() : "");
        fullNameField.setText(currentCV.getFullName() != null ? currentCV.getFullName() : "");
        emailField.setText(currentCV.getEmail() != null ? currentCV.getEmail() : "");
        phoneField.setText(currentCV.getPhone() != null ? currentCV.getPhone() : "");
        addressField.setText(currentCV.getAddress() != null ? currentCV.getAddress() : "");
        summaryArea.setText(currentCV.getSummary() != null ? currentCV.getSummary() : "");

        // Update list views
        updateListViews();
    }

    private void updateListViews() {
        // Clear and update education list
        educationListView.getItems().clear();
        educationListView.getItems().addAll(currentCV.getEducation());

        // Clear and update experience list
        experienceListView.getItems().clear();
        experienceListView.getItems().addAll(currentCV.getExperience());

        // Clear and update skills list
        skillsListView.getItems().clear();
        skillsListView.getItems().addAll(currentCV.getSkills());

        // Clear and update languages list
        languagesListView.getItems().clear();
        languagesListView.getItems().addAll(currentCV.getLanguages());
    }
    @FXML
    private void handlePreview() {
        // Your code here
    }

    @FXML
    private void handleSave() {
        try {
            // Update CV with form data
            currentCV.setTitle(cvTitleField.getText());
            currentCV.setFullName(fullNameField.getText());
            currentCV.setEmail(emailField.getText());
            currentCV.setPhone(phoneField.getText());
            currentCV.setAddress(addressField.getText());
            currentCV.setSummary(summaryArea.getText());

            // Validate before saving
            ValidationUtil.validateCV(currentCV);
            for (Education edu : currentCV.getEducation()) {
                ValidationUtil.validateEducation(edu);
            }
            for (Experience exp : currentCV.getExperience()) {
                ValidationUtil.validateExperience(exp);
            }

            // Save to database
            boolean success;
            if (currentCV.getId() > 0) {
                success = DatabaseHandler.getInstance().updateCV(currentCV);
            } else {
                int cvId = DatabaseHandler.getInstance().createCV(currentCV);
                success = (cvId > 0);
            }

            if (success) {
                showAlert(Alert.AlertType.INFORMATION,
                    mainApp.getBundle().getString("message.saved"));
            }
        } catch (IllegalArgumentException e) {
            handleValidationError(e);
        } catch (SQLException e) {
            handleDatabaseError(e);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR,
                mainApp.getBundle().getString("error.general") + ": " + e.getMessage());
        }
    }

    private void handleValidationError(IllegalArgumentException e) {
        String[] parts = e.getMessage().split(":");
        String errorKey = parts[0];
        String fieldName = parts.length > 1 ? parts[1] : "";

        String errorMessage = mainApp.getBundle().getString(errorKey);
        if (!fieldName.isEmpty()) {
            errorMessage = errorMessage.replace("{0}",
                mainApp.getBundle().getString("cv." + fieldName));
        }

        showAlert(Alert.AlertType.ERROR, errorMessage);
    }

    private void handleDatabaseError(SQLException e) {
        String errorKey;
        switch (e.getSQLState()) {
            case "23505":
                errorKey = "error.unique_violation";
                break;
            case "23502":
                errorKey = "error.null_violation";
                break;
            case "23514":
                errorKey = "error.check_violation";
                break;
            case "23503":
                errorKey = "error.foreign_key";
                break;
            default:
                errorKey = "error.database";
        }

        String errorMessage = mainApp.getBundle().getString(errorKey);
        if (e.getMessage() != null) {
            errorMessage += ": " + e.getMessage();
        }
        showAlert(Alert.AlertType.ERROR, errorMessage);
    }


    @FXML
    private void handleBack() {
        mainApp.showDashboard();
    }


    @FXML
    private void handlePrint() {
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("My_CV.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File outputFile = fileChooser.showSaveDialog(null);


        if (outputFile != null) {
            try {

            	 File workingDir = new File("output_temp");
                 workingDir.mkdirs();

                 String texPath = new File(workingDir, "CV.tex").getAbsolutePath();
                 String pdfPath = outputFile.getAbsolutePath();

                // 1. Generate LaTeX file in same directory

                LaTeXGenerator.generateLatexFile(currentCV, texPath);
                System.out.println("LaTeX file generated at: " + new File(texPath).getAbsolutePath());

                // 2. Convert to PDF
                LatexToPdfConverter.convertLatexToPdf(texPath, outputFile.getAbsolutePath());
                if (outputFile.exists()) {
                    System.out.println("PDF successfully created at: " + outputFile.getAbsolutePath());
                } else {
                    System.out.println("ERROR: PDF was not created.");
                }
                // 3. Clean up temporary .tex and auxiliary files
                new File(texPath).delete();
            } catch (Exception e) {
                e.printStackTrace();

                showAlert(Alert.AlertType.INFORMATION,
                         "PDF saved successfully at: " + outputFile.getAbsolutePath());
            }
        }
    }

    // Education methods
    private void setupEducationListView() {
        educationListView.setCellFactory(param -> new ListCell<Education>() {
            @Override
            protected void updateItem(Education item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String dates = (item.getStartDate() != null ? item.getStartDate() : "?")
                        + " - " +
                        (item.getEndDate() != null ? item.getEndDate() : "?");
                    setText(item.getInstitution() + " (" + dates + ")");
                }
            }
        });
    }



    @FXML
    private void handleAddEducation() {
        Dialog<Education> dialog = createEducationDialog(null);

        Optional<Education> result = dialog.showAndWait();
        result.ifPresent(education -> {
            currentCV.addEducation(education);
            updateListViews();
        });
    }

    @FXML
    private void handleEditEducation() {
        Education selected = educationListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
			return;
		}

        Dialog<Education> dialog = createEducationDialog(selected);

        Optional<Education> result = dialog.showAndWait();
        result.ifPresent(education -> {

            selected.setInstitution(education.getInstitution());
            selected.setDegree(education.getDegree());
            selected.setFieldOfStudy(education.getFieldOfStudy());
            selected.setStartDate(education.getStartDate());
            selected.setEndDate(education.getEndDate());
            selected.setDescription(education.getDescription());

            updateListViews();
        });
    }

    @FXML
    private void handleDeleteEducation() {
        Education selected = educationListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentCV.getEducation().remove(selected);
            updateListViews();
        }
    }

    private Dialog<Education> createEducationDialog(Education education) {
        Dialog<Education> dialog = new Dialog<>();
        dialog.setTitle(mainApp.getBundle().getString("cv.section.education"));

        // Set the button types
        ButtonType saveButtonType = new ButtonType(mainApp.getBundle().getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField institutionField = new TextField();
        institutionField.setPromptText(mainApp.getBundle().getString("cv.education.institution"));

        TextField degreeField = new TextField();
        degreeField.setPromptText(mainApp.getBundle().getString("cv.education.degree"));

        TextField fieldOfStudyField = new TextField();
        fieldOfStudyField.setPromptText(mainApp.getBundle().getString("cv.education.field"));


        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText(mainApp.getBundle().getString("cv.education.description"));

        // If editing, populate fields
        if (education != null) {
            institutionField.setText(education.getInstitution());
            degreeField.setText(education.getDegree());
            fieldOfStudyField.setText(education.getFieldOfStudy());
            startDatePicker.setValue((education.getStartDate()));
            endDatePicker.setValue((education.getEndDate()));
            descriptionArea.setText(education.getDescription());
        }

        grid.add(new Label(mainApp.getBundle().getString("cv.education.institution")), 0, 0);
        grid.add(institutionField, 1, 0);
        grid.add(new Label(mainApp.getBundle().getString("cv.education.degree")), 0, 1);
        grid.add(degreeField, 1, 1);
        grid.add(new Label(mainApp.getBundle().getString("cv.education.field")), 0, 2);
        grid.add(fieldOfStudyField, 1, 2);
        grid.add(new Label(mainApp.getBundle().getString("cv.education.start")), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label(mainApp.getBundle().getString("cv.education.end")), 0, 4);
        grid.add(endDatePicker, 1, 4);
        grid.add(new Label(mainApp.getBundle().getString("cv.education.description")), 0, 5);
        grid.add(descriptionArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to an education object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Education newEducation = education != null ? education : new Education();
                newEducation.setInstitution(institutionField.getText());
                newEducation.setDegree(degreeField.getText());
                newEducation.setFieldOfStudy(fieldOfStudyField.getText());
                newEducation.setStartDate(formatDate(startDatePicker.getValue()));
                newEducation.setEndDate(formatDate(endDatePicker.getValue()));
                newEducation.setDescription(descriptionArea.getText());
                return newEducation;
            }
            return null;
        });

        return dialog;
    }
 // Add these helper methods to convert between String and LocalDate
    private LocalDate formatDate(LocalDate date) {

        return date;
    }


    // Experience methods
    private void setupExperienceListView() {
        experienceListView.setCellFactory(new Callback<ListView<Experience>, ListCell<Experience>>() {
            @Override
            public ListCell<Experience> call(ListView<Experience> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Experience item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getCompany() + " - " + item.getPosition());
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleAddExperience() {
        Dialog<Experience> dialog = createExperienceDialog(null);

        Optional<Experience> result = dialog.showAndWait();
        result.ifPresent(experience -> {
            currentCV.addExperience(experience);
            updateListViews();
        });
    }

    @FXML
    private void handleEditExperience() {
        Experience selected = experienceListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
			return;
		}

        Dialog<Experience> dialog = createExperienceDialog(selected);

        Optional<Experience> result = dialog.showAndWait();
        result.ifPresent(experience -> {
            selected.setCompany(experience.getCompany());
            selected.setPosition(experience.getPosition());
            selected.setLocation(experience.getLocation());
            selected.setStartDate(experience.getStartDate());
            selected.setEndDate(experience.getEndDate());
            selected.setDescription(experience.getDescription());

            updateListViews();
        });
    }

    @FXML
    private void handleDeleteExperience() {
        Experience selected = experienceListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentCV.getExperience().remove(selected);
            updateListViews();
        }
    }

    private Dialog<Experience> createExperienceDialog(Experience experience) {
        Dialog<Experience> dialog = new Dialog<>();
        dialog.setTitle(mainApp.getBundle().getString("cv.section.experience"));

        // Set the button types
        ButtonType saveButtonType = new ButtonType(mainApp.getBundle().getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField companyField = new TextField();
        companyField.setPromptText(mainApp.getBundle().getString("cv.experience.company"));

        TextField positionField = new TextField();
        positionField.setPromptText(mainApp.getBundle().getString("cv.experience.position"));

        TextField locationField = new TextField();
        locationField.setPromptText(mainApp.getBundle().getString("cv.experience.location"));

        // Replace TextFields with DatePickers
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText(mainApp.getBundle().getString("cv.experience.description"));

        // If editing, populate fields
        if (experience != null) {
            companyField.setText(experience.getCompany());
            positionField.setText(experience.getPosition());
            locationField.setText(experience.getLocation());
            startDatePicker.setValue(experience.getStartDate());
            endDatePicker.setValue(experience.getEndDate());
            descriptionArea.setText(experience.getDescription());
        }

        grid.add(new Label(mainApp.getBundle().getString("cv.experience.company")), 0, 0);
        grid.add(companyField, 1, 0);
        grid.add(new Label(mainApp.getBundle().getString("cv.experience.position")), 0, 1);
        grid.add(positionField, 1, 1);
        grid.add(new Label(mainApp.getBundle().getString("cv.experience.location")), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label(mainApp.getBundle().getString("cv.experience.start")), 0, 3);
        grid.add(startDatePicker, 1, 3);
        grid.add(new Label(mainApp.getBundle().getString("cv.experience.end")), 0, 4);
        grid.add(endDatePicker, 1, 4);
        grid.add(new Label(mainApp.getBundle().getString("cv.experience.description")), 0, 5);
        grid.add(descriptionArea, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to an experience object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Experience newExperience = experience != null ? experience : new Experience();
                newExperience.setCompany(companyField.getText());
                newExperience.setPosition(positionField.getText());
                newExperience.setLocation(locationField.getText());
                newExperience.setStartDate(startDatePicker.getValue());
                newExperience.setEndDate(endDatePicker.getValue());
                newExperience.setDescription(descriptionArea.getText());
                return newExperience;
            }
            return null;
        });

        return dialog;
    }

    // Skills methods
    private void setupSkillsListView() {
        skillsListView.setCellFactory(new Callback<ListView<Skill>, ListCell<Skill>>() {
            @Override
            public ListCell<Skill> call(ListView<Skill> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Skill item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName() + " - " + item.getLevel() + "/5");
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleAddSkill() {
        Dialog<Skill> dialog = createSkillDialog(null);

        Optional<Skill> result = dialog.showAndWait();
        result.ifPresent(skill -> {
            currentCV.addSkill(skill);
            updateListViews();
        });
    }

    @FXML
    private void handleEditSkill() {
        Skill selected = skillsListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
			return;
		}

        Dialog<Skill> dialog = createSkillDialog(selected);

        Optional<Skill> result = dialog.showAndWait();
        result.ifPresent(skill -> {
            selected.setName(skill.getName());
            selected.setLevel(skill.getLevel());

            updateListViews();
        });
    }

    @FXML
    private void handleDeleteSkill() {
        Skill selected = skillsListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentCV.getSkills().remove(selected);
            updateListViews();
        }
    }

    private Dialog<Skill> createSkillDialog(Skill skill) {
        Dialog<Skill> dialog = new Dialog<>();
        dialog.setTitle(mainApp.getBundle().getString("cv.skills.name"));

        // Set the button types
        ButtonType saveButtonType = new ButtonType(mainApp.getBundle().getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText(mainApp.getBundle().getString("cv.skills.name"));

        Slider levelSlider = new Slider(1, 5, 3);
        levelSlider.setShowTickLabels(true);
        levelSlider.setShowTickMarks(true);
        levelSlider.setMajorTickUnit(1);
        levelSlider.setMinorTickCount(0);
        levelSlider.setSnapToTicks(true);

        Label levelValueLabel = new Label("3");
        levelSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            levelValueLabel.setText(String.valueOf(newVal.intValue()));
        });

        // If editing, populate fields
        if (skill != null) {
            nameField.setText(skill.getName());
            levelSlider.setValue(skill.getLevel());
            levelValueLabel.setText(String.valueOf(skill.getLevel()));
        }

        grid.add(new Label(mainApp.getBundle().getString("cv.skills.name")), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label(mainApp.getBundle().getString("cv.skills.level")), 0, 1);
        grid.add(levelSlider, 1, 1);
        grid.add(levelValueLabel, 2, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a skill object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Skill newSkill = new Skill();
                newSkill.setName(nameField.getText());
                newSkill.setLevel((int) levelSlider.getValue());
                return newSkill;
            }
            return null;
        });

        return dialog;
    }

    // Languages methods
    private void setupLanguagesListView() {
        languagesListView.setCellFactory(new Callback<ListView<Language>, ListCell<Language>>() {
            @Override
            public ListCell<Language> call(ListView<Language> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Language item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item.getName() + " - " + item.getProficiency());
                        }
                    }
                };
            }
        });
    }

    @FXML
    private void handleAddLanguage() {
        Dialog<Language> dialog = createLanguageDialog(null);

        Optional<Language> result = dialog.showAndWait();
        result.ifPresent(language -> {
            currentCV.addLanguage(language);
            updateListViews();
        });
    }

    @FXML
    private void handleEditLanguage() {
        Language selected = languagesListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
			return;
		}

        Dialog<Language> dialog = createLanguageDialog(selected);

        Optional<Language> result = dialog.showAndWait();
        result.ifPresent(language -> {
            selected.setName(language.getName());
            selected.setProficiency(language.getProficiency());

            updateListViews();
        });
    }

    @FXML
    private void handleDeleteLanguage() {
        Language selected = languagesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            currentCV.getLanguages().remove(selected);
            updateListViews();
        }
    }

    private Dialog<Language> createLanguageDialog(Language language) {
        Dialog<Language> dialog = new Dialog<>();
        dialog.setTitle(mainApp.getBundle().getString("cv.language.name"));

        // Set the button types
        ButtonType saveButtonType = new ButtonType(mainApp.getBundle().getString("button.save"), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Create the form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText(mainApp.getBundle().getString("cv.language.name"));

        ComboBox<String> proficiencyComboBox = new ComboBox<>();
        proficiencyComboBox.getItems().addAll(
            mainApp.getBundle().getString("cv.language.beginner"),
            mainApp.getBundle().getString("cv.language.intermediate"),
            mainApp.getBundle().getString("cv.language.advanced"),
            mainApp.getBundle().getString("cv.language.native")
        );
        proficiencyComboBox.setValue(mainApp.getBundle().getString("cv.language.intermediate"));

        // If editing, populate fields
        if (language != null) {
            nameField.setText(language.getName());
            proficiencyComboBox.setValue(language.getProficiency());
        }

        grid.add(new Label(mainApp.getBundle().getString("cv.language.name")), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label(mainApp.getBundle().getString("cv.language.proficiency")), 0, 1);
        grid.add(proficiencyComboBox, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Convert the result to a language object when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Language newLanguage = new Language();
                newLanguage.setName(nameField.getText());
                newLanguage.setProficiency(proficiencyComboBox.getValue());
                return newLanguage;
            }
            return null;
        });

        return dialog;
    }

    private void showAlert(Alert.AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
			return null;
		}
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
            return LocalDate.parse(dateString, formatter);
        } catch (Exception e) {
            return null;
        }
    }}


package components.CVBuilder.view;

import java.util.List;

import components.CVBuilder.MainApp;
import components.CVBuilder.model.CV;
import components.CVBuilder.model.User;
import components.CVBuilder.util.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DashboardController {
    @FXML
    private Label welcomeLabel;

    @FXML
    private VBox cvListContainer;

    @FXML
    private Label emptyLabel;

    private MainApp mainApp;
    private User currentUser;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    public void loadUserData(User user) {
        this.currentUser = user;

        // Set welcome message
        welcomeLabel.setText(mainApp.getBundle().getString("dashboard.welcome") + user.getUsername());

        // Load CVs from database
        loadCVs();
    }

    private void loadCVs() {
        // Clear existing items
        cvListContainer.getChildren().clear();

        // Get CVs for current user
        List<CV> cvList = DatabaseHandler.getInstance().getCVsByUserId(currentUser.getId());

        if (cvList.isEmpty()) {
            // Show empty message
            emptyLabel.setVisible(true);
        } else {
            // Hide empty message
            emptyLabel.setVisible(false);

            // Populate CV list
            for (CV cv : cvList) {
                HBox cvItem = createCVListItem(cv);
                cvListContainer.getChildren().add(cvItem);
            }
        }
    }

    private HBox createCVListItem(CV cv) {
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        hbox.setPadding(new Insets(5));
        hbox.getStyleClass().add("cv-item");

        // CV title label
        Label titleLabel = new Label(cv.getTitle());
        titleLabel.getStyleClass().add("cv-title");
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Edit button
        Button editButton = new Button(mainApp.getBundle().getString("button.edit"));
        editButton.setOnAction(event -> {
            mainApp.showCVEditor(cv.getId());
        });

        // Delete button
        Button deleteButton = new Button(mainApp.getBundle().getString("button.delete"));
        deleteButton.setOnAction(event -> {
            if (DatabaseHandler.getInstance().deleteCV(cv.getId())) {
                loadCVs(); // Refresh list
            }
        });

        // Add components to HBox
        hbox.getChildren().addAll(titleLabel, editButton, deleteButton);

        return hbox;
    }

    @FXML
    private void handleCreateCV() {
        mainApp.showCVEditor();
    }

    @FXML
    private void handleLogout() {
        mainApp.logout();
    }

    @FXML
    private void switchLanguage() {
        mainApp.switchLanguage();
    }
}

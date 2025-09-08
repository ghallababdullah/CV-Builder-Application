package components.CVBuilder.view;

import java.sql.SQLException;
import java.util.regex.Pattern;

import components.CVBuilder.MainApp;
import components.CVBuilder.model.User;
import components.CVBuilder.util.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegistrationController {
    // Email regex pattern for basic validation
    private static final String EMAIL_REGEX = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" +
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label errorLabel;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleRegister() throws SQLException {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Basic validation
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText(mainApp.getBundle().getString("registration.error.empty"));
            return;
        }

        if (!isValidEmail(email)) {
            errorLabel.setText(mainApp.getBundle().getString("registration.error.invalidEmail"));
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText(mainApp.getBundle().getString("registration.error.mismatch"));
            return;
        }

        // Create new user
        User user = new User(username, email, password);

        // Try to register user
        if (DatabaseHandler.getInstance().registerUser(user)) {
            // Registration successful, go to login
            mainApp.showLoginScreen();
        } else {
            errorLabel.setText(mainApp.getBundle().getString("registration.error.exists"));
        }
    }

    // Helper method to validate email format
    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    @FXML
    private void handleCancel() {
        mainApp.showLoginScreen();
    }

    @FXML
    private void handleLoginLink() {
        mainApp.showLoginScreen();
    }
}
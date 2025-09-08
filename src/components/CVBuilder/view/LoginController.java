
package components.CVBuilder.view;

import java.sql.SQLException;

import components.CVBuilder.MainApp;
import components.CVBuilder.model.User;
import components.CVBuilder.util.DatabaseHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() throws SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText(mainApp.getBundle().getString("login.error"));
            return;
        }

        User user = DatabaseHandler.getInstance().loginUser(username, password);

        if (user != null) {
            mainApp.setCurrentUser(user);
            mainApp.showDashboard();
        } else {
            errorLabel.setText(mainApp.getBundle().getString("login.error"));
        }
    }

    @FXML
    private void handleRegisterLink() {
        mainApp.showRegistrationScreen();
    }

    @FXML
    private void switchLanguage() {
        mainApp.switchLanguage();
    }
}

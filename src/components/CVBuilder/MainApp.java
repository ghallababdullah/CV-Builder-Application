package components.CVBuilder;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

import components.CVBuilder.model.User;
import components.CVBuilder.view.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private User currentUser;
    private ResourceBundle bundle;
    private Locale currentLocale;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("CV Builder");


        // Initialize database connection
     /*   DatabaseHandler.getInstance();*/

        // Set default locale (Russian)
        setLocale(new Locale("ru", "RU"));

        initRootLayout();
        showLoginScreen();
    }

    public void initRootLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
            loader.setResources(bundle);
            rootLayout = (BorderPane) loader.load();

            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Login.fxml"));
            loader.setResources(bundle);
            BorderPane loginPane = (BorderPane) loader.load();

            rootLayout.setCenter(loginPane);

            LoginController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Dashboard.fxml"));
            loader.setResources(bundle);
            BorderPane dashboardPane = (BorderPane) loader.load();

            rootLayout.setCenter(dashboardPane);

            components.CVBuilder.view.DashboardController controller = loader.getController();
            controller.setMainApp(this);
            controller.loadUserData(currentUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegistrationScreen() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/Registration.fxml"));
            loader.setResources(bundle);
            BorderPane registrationPane = (BorderPane) loader.load();

            rootLayout.setCenter(registrationPane);

            components.CVBuilder.view.RegistrationController controller = loader.getController();
            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showCVEditor() {
        showCVEditor(0); // Default to creating a new CV
    }

    public void showCVEditor(int cvId) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource("view/CVEditor.fxml"));
            loader.setResources(bundle);
            BorderPane editorPane = (BorderPane) loader.load();

            rootLayout.setCenter(editorPane);

            components.CVBuilder.view.CVEditorController controller = loader.getController();
            controller.setMainApp(this);
            controller.loadUserCV(currentUser, cvId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setLocale(Locale locale) {
        currentLocale = locale;
        bundle = ResourceBundle.getBundle("components.CVBuilder.resources.language", locale);

        // Refresh UI if already loaded
        if (rootLayout != null) {
            initRootLayout();
            if (currentUser != null) {
                showDashboard();
            } else {
                showLoginScreen();
            }
        }
    }

    public void switchLanguage() {
        if (currentLocale.getLanguage().equals("ru")) {
            setLocale(new Locale("en", "US"));
        } else {
            setLocale(new Locale("ru", "RU"));
        }
    }

    public void logout() {
        this.currentUser = null;
        showLoginScreen();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public ResourceBundle getBundle() {
        return bundle;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

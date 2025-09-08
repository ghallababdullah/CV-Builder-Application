package components.CVBuilder.util;

import org.postgresql.util.PSQLException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ErrorUtils {
    public static void showDatabaseError(Throwable e) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Database Error");

        if (e instanceof PSQLException) {
            PSQLException pe = (PSQLException) e;
            String message = pe.getServerErrorMessage().getMessage();
            String detail = pe.getServerErrorMessage().getDetail();

            alert.setHeaderText("Database constraint violation");
            alert.setContentText(getFriendlyErrorMessage(message) +
                               (detail != null ? "\n\nDetails: " + detail : ""));
        } else {
            alert.setHeaderText("Database operation failed");
            alert.setContentText(e.getMessage());
        }

        alert.showAndWait();
    }

    private static String getFriendlyErrorMessage(String technicalMessage) {
        if (technicalMessage.contains("null value in column")) {
            return "Required field is missing. Please fill all required fields.";
        } else if (technicalMessage.contains("duplicate key value")) {
            return "This value already exists. Please use a unique value.";
        } else if (technicalMessage.contains("violates foreign key constraint")) {
            return "Invalid reference. Please check related records exist.";
        } else if (technicalMessage.contains("violates check constraint")) {
            return "Invalid value. Please check your input meets the requirements.";
        }
        return technicalMessage;
    }
}
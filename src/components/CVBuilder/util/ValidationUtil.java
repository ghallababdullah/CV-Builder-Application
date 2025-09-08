

package components.CVBuilder.util;

import java.time.LocalDate;

import components.CVBuilder.model.CV;
import components.CVBuilder.model.Education;
import components.CVBuilder.model.Experience;
import components.CVBuilder.model.User;

public class ValidationUtil {
    public static void validateUser(User user) throws IllegalArgumentException {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new IllegalArgumentException("error.required_field:username");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("error.email_format");
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            throw new IllegalArgumentException("error.required_field:password");
        }
    }

    public static void validateCV(CV cv) throws IllegalArgumentException {
        if (cv.getTitle() == null || cv.getTitle().isEmpty()) {
            throw new IllegalArgumentException("error.required_field:title");
        }
        if (cv.getEmail() == null || !cv.getEmail().matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            throw new IllegalArgumentException("error.email_format");
        }
        if (cv.getPhone() == null || !cv.getPhone().matches("^\\+\\d{1,15}$")) {
            throw new IllegalArgumentException("error.phone_format");
        }
    }

    public static void validateEducation(Education education) throws IllegalArgumentException {
        if (education.getInstitution() == null || education.getInstitution().isEmpty()) {
            throw new IllegalArgumentException("error.required_field:institution");
        }
        validateDate(education.getStartDate());
        validateDate(education.getEndDate());
    }

    public static void validateExperience(Experience experience) throws IllegalArgumentException {
        if (experience.getCompany() == null || experience.getCompany().isEmpty()) {
            throw new IllegalArgumentException("error.required_field:company");
        }
        validateDate(experience.getStartDate());
        validateDate(experience.getEndDate());
    }

    private static void validateDate(LocalDate date) throws IllegalArgumentException {
        if (date == null) {
			return;
		}

        LocalDate minDate = LocalDate.of(1900, 1, 1);
        LocalDate maxDate = LocalDate.now();

        if (date.isBefore(minDate) || date.isAfter(maxDate)) {
            throw new IllegalArgumentException("error.date_range");
        }
    }
}
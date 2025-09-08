
package components.CVBuilder.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import components.CVBuilder.model.CV;
import components.CVBuilder.model.Education;
import components.CVBuilder.model.Experience;
import components.CVBuilder.model.Language;
import components.CVBuilder.model.Skill;
import components.CVBuilder.model.User;

public class DatabaseHandler {
    private static DatabaseHandler instance;
    private Connection connection;

    // PostgreSQL connection details
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASSWORD;

    static {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        if (url == null || user == null || password == null) {
            Properties props = new Properties();
            try (InputStream input = DatabaseHandler.class.getClassLoader()
                    .getResourceAsStream("database.properties")) {
                if (input == null) {
                    throw new RuntimeException("❌ database.properties not found in resources!");
                }
                props.load(input);
            } catch (IOException e) {
                throw new RuntimeException("❌ Failed to load database.properties: " + e.getMessage(), e);
            }

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");
        }

        DB_URL = url;
        DB_USER = user;
        DB_PASSWORD = password;
    }

    public static synchronized DatabaseHandler getInstance() {
        if (instance == null) {
            instance = new DatabaseHandler();
        }
        return instance;
    }

    private DatabaseHandler() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ Database connected successfully!");
        } catch (SQLException e) {
            throw new RuntimeException("❌ Database connection failed: " + e.getMessage(), e);
        }
    }

    private SQLException translateSQLException(SQLException e) {
        String sqlState = e.getSQLState();
        String message = e.getMessage();

        switch (sqlState) {
            case "23505": // Unique violation
                if (message.contains("users_username_key")) {
                    return new SQLException("Username already exists", sqlState);
                } else if (message.contains("users_email_key")) {
                    return new SQLException("Email already exists", sqlState);
                }
                break;
            case "23502": // Not null violation
                return new SQLException("Required field is missing", sqlState);
            case "23514": // Check violation
                if (message.contains("valid_")) {
                    return new SQLException("Phone must start with + and contain 1-15 digits", sqlState);
                } else if (message.contains("valid_email")) {
                    return new SQLException("Invalid email format", sqlState);
                } else if (message.contains("validdate")) {
                    return new SQLException("Dates must be between 1900-01-01 and current date", sqlState);
                }
                break;
            case "23503": // Foreign key violation
                if (message.contains("cvs_user_id_fkey")) {
                    return new SQLException("User does not exist", sqlState);
                } else if (message.contains("_cv_id_fkey")) {
                    return new SQLException("CV does not exist", sqlState);
                }
                break;
        }
        return e;
    }

    public Connection getConnection() {
        return connection;
    }

    // User operations
    public boolean registerUser(User user) throws SQLException {
        String insert = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            int result = statement.executeUpdate();

            return result > 0;
        } catch (SQLException e) {
        	throw translateSQLException(e);
        }
    }

    public User loginUser(String username, String password) throws SQLException {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                User user = new User();
                user.setId(resultSet.getInt("id"));
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setPassword("");
                return user;
            }
        } catch (SQLException e) {
        	throw translateSQLException(e);
        }

        return null;
    }

    // CV operations
    public int createCV(CV cv) throws SQLException {
        String insert = "INSERT INTO cvs (user_id, title, full_name, email, phone, address, summary) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, cv.getUserId());
            statement.setString(2, cv.getTitle());
            statement.setString(3, cv.getFullName());
            statement.setString(4, cv.getEmail());
            statement.setString(5, cv.getPhone());
            statement.setString(6, cv.getAddress());
            statement.setString(7, cv.getSummary());

            int result = statement.executeUpdate();

            if (result > 0) {
                ResultSet generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int cvId = generatedKeys.getInt(1);
                    cv.setId(cvId);

                    // Save related data
                    for (Education education : cv.getEducation()) {
                        saveEducation(education, cvId);
                    }

                    for (Experience experience : cv.getExperience()) {
                        saveExperience(experience, cvId);
                    }

                    for (Skill skill : cv.getSkills()) {
                        saveSkill(skill, cvId);
                    }

                    for (Language language : cv.getLanguages()) {
                        saveLanguage(language, cvId);
                    }

                    return cvId;
                }
            }
        } catch (SQLException e) {
        	throw translateSQLException(e);
        }

        return -1;
    }

    public boolean updateCV(CV cv) throws SQLException {
        String update = "UPDATE cvs SET title = ?, full_name = ?, email = ?, phone = ?, " +
                       "address = ?, summary = ? WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(update)) {
            statement.setString(1, cv.getTitle());
            statement.setString(2, cv.getFullName());
            statement.setString(3, cv.getEmail());
            statement.setString(4, cv.getPhone());
            statement.setString(5, cv.getAddress());
            statement.setString(6, cv.getSummary());
            statement.setInt(7, cv.getId());

            int result = statement.executeUpdate();

            if (result > 0) {
                // Delete existing related data
                deleteEducationByCvId(cv.getId());
                deleteExperienceByCvId(cv.getId());
                deleteSkillsByCvId(cv.getId());
                deleteLanguagesByCvId(cv.getId());

                // Save updated related data
                for (Education education : cv.getEducation()) {
                    saveEducation(education, cv.getId());
                }

                for (Experience experience : cv.getExperience()) {
                    saveExperience(experience, cv.getId());
                }

                for (Skill skill : cv.getSkills()) {
                    saveSkill(skill, cv.getId());
                }

                for (Language language : cv.getLanguages()) {
                    saveLanguage(language, cv.getId());
                }

                return true;
            }
        } catch (SQLException e) {
        	throw translateSQLException(e);
        }

        return false;
    }

    public boolean deleteCV(int cvId) {
        // Delete related data first
        deleteEducationByCvId(cvId);
        deleteExperienceByCvId(cvId);
        deleteSkillsByCvId(cvId);
        deleteLanguagesByCvId(cvId);

        // Delete CV
        String delete = "DELETE FROM cvs WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(delete)) {
            statement.setInt(1, cvId);

            int result = statement.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CV> getCVsByUserId(int userId) {
        List<CV> cvList = new ArrayList<>();
        String query = "SELECT * FROM cvs WHERE user_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                CV cv = new CV();
                cv.setId(resultSet.getInt("id"));
                cv.setUserId(resultSet.getInt("user_id"));
                cv.setTitle(resultSet.getString("title"));
                cv.setFullName(resultSet.getString("full_name"));
                cv.setEmail(resultSet.getString("email"));
                cv.setPhone(resultSet.getString("phone"));
                cv.setAddress(resultSet.getString("address"));
                cv.setSummary(resultSet.getString("summary"));

                // Load related data
                loadEducation(cv);
                loadExperience(cv);
                loadSkills(cv);
                loadLanguages(cv);

                cvList.add(cv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cvList;
    }

    public CV getCVById(int cvId) {
        String query = "SELECT * FROM cvs WHERE id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, cvId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                CV cv = new CV();
                cv.setId(resultSet.getInt("id"));
                cv.setUserId(resultSet.getInt("user_id"));
                cv.setTitle(resultSet.getString("title"));
                cv.setFullName(resultSet.getString("full_name"));
                cv.setEmail(resultSet.getString("email"));
                cv.setPhone(resultSet.getString("phone"));
                cv.setAddress(resultSet.getString("address"));
                cv.setSummary(resultSet.getString("summary"));

                // Load related data
                loadEducation(cv);
                loadExperience(cv);
                loadSkills(cv);
                loadLanguages(cv);

                return cv;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Helper methods for related data
    private void saveEducation(Education education, int cvId) throws SQLException {
        String insert = "INSERT INTO education (cv_id, institution, degree, field_of_study, start_date, end_date, description) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setInt(1, cvId);
            statement.setString(2, education.getInstitution());
            statement.setString(3, education.getDegree());
            statement.setString(4, education.getFieldOfStudy());
            statement.setDate(5,  java.sql.Date.valueOf(education.getStartDate()));
            statement.setDate(6,  java.sql.Date.valueOf(education.getEndDate()));
            statement.setString(7, education.getDescription());

            statement.executeUpdate();
        } catch (SQLException e) {
        	throw translateSQLException(e);
        }
    }

    private void saveExperience(Experience experience, int cvId) throws SQLException {
        String insert = "INSERT INTO experience (cv_id, company, position, location, start_date, end_date, description) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setInt(1, cvId);
            statement.setString(2, experience.getCompany());
            statement.setString(3, experience.getPosition());
            statement.setString(4, experience.getLocation());
            statement.setDate(5,  java.sql.Date.valueOf(experience.getStartDate()));
            statement.setDate(6,  java.sql.Date.valueOf(experience.getEndDate()));
            statement.setString(7, experience.getDescription());

            statement.executeUpdate();
        } catch (SQLException e) {
        	throw translateSQLException(e);
        }
    }

    private void saveSkill(Skill skill, int cvId) throws SQLException {
        String insert = "INSERT INTO skills (cv_id, name, level) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setInt(1, cvId);
            statement.setString(2, skill.getName());
            statement.setInt(3, skill.getLevel());

            statement.executeUpdate();
        } catch (SQLException e) {
        	throw translateSQLException(e);
        }
    }

    private void saveLanguage(Language language, int cvId) throws SQLException {
        String insert = "INSERT INTO languages (cv_id, name, proficiency) VALUES (?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(insert)) {
            statement.setInt(1, cvId);
            statement.setString(2, language.getName());
            statement.setString(3, language.getProficiency());

            statement.executeUpdate();
        } catch (SQLException e) {
        	throw translateSQLException(e);
        }
    }

    private void loadEducation(CV cv) {
        String query = "SELECT * FROM education WHERE cv_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, cv.getId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Education education = new Education();
                education.setId(resultSet.getInt("id"));
                education.setCvId(resultSet.getInt("cv_id"));
                education.setInstitution(resultSet.getString("institution"));
                education.setDegree(resultSet.getString("degree"));
                education.setFieldOfStudy(resultSet.getString("field_of_study"));
                Date sqlDate = resultSet.getDate("start_date");
                education.setStartDate(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = resultSet.getDate("end_date");
                education.setEndDate(sqlDate != null ? sqlDate.toLocalDate() : null);
                education.setDescription(resultSet.getString("description"));

                cv.addEducation(education);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadExperience(CV cv) {
        String query = "SELECT * FROM experience WHERE cv_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, cv.getId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Experience experience = new Experience();
                experience.setId(resultSet.getInt("id"));
                experience.setCvId(resultSet.getInt("cv_id"));
                experience.setCompany(resultSet.getString("company"));
                experience.setPosition(resultSet.getString("position"));
                experience.setLocation(resultSet.getString("location"));
                Date sqlDate = resultSet.getDate("start_date");
                experience.setStartDate(sqlDate != null ? sqlDate.toLocalDate() : null);
                sqlDate = resultSet.getDate("end_date");
                experience.setEndDate(sqlDate != null ? sqlDate.toLocalDate() : null);
                experience.setDescription(resultSet.getString("description"));

                cv.addExperience(experience);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSkills(CV cv) {
        String query = "SELECT * FROM skills WHERE cv_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, cv.getId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Skill skill = new Skill();
                skill.setId(resultSet.getInt("id"));
                skill.setCvId(resultSet.getInt("cv_id"));
                skill.setName(resultSet.getString("name"));
                skill.setLevel(resultSet.getInt("level"));

                cv.addSkill(skill);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadLanguages(CV cv) {
        String query = "SELECT * FROM languages WHERE cv_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, cv.getId());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Language language = new Language();
                language.setId(resultSet.getInt("id"));
                language.setCvId(resultSet.getInt("cv_id"));
                language.setName(resultSet.getString("name"));
                language.setProficiency(resultSet.getString("proficiency"));

                cv.addLanguage(language);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteEducationByCvId(int cvId) {
        String delete = "DELETE FROM education WHERE cv_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(delete)) {
            statement.setInt(1, cvId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteExperienceByCvId(int cvId) {
        String delete = "DELETE FROM experience WHERE cv_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(delete)) {
            statement.setInt(1, cvId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteSkillsByCvId(int cvId) {
        String delete = "DELETE FROM skills WHERE cv_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(delete)) {
            statement.setInt(1, cvId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteLanguagesByCvId(int cvId) {
        String delete = "DELETE FROM languages WHERE cv_id = ?";

        try (PreparedStatement statement = connection.prepareStatement(delete)) {
            statement.setInt(1, cvId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

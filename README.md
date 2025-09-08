# 🚀 CV Builder Application

![Java](https://img.shields.io/badge/Java-17%2B-blue.svg)
![JavaFX](https://img.shields.io/badge/JavaFX-17%2B-orange.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-13%2B-blue.svg)
![License](https://img.shields.io/badge/License-MIT-green.svg)

A professional **desktop application** for creating, managing, and exporting CVs/resumes to PDF format.  
Built with **JavaFX** and **PostgreSQL**, featuring **multi-language support** and professional **LaTeX-based PDF generation**.

---

## ✨ Features

- 📝 **CV Creation & Management**: Create and edit multiple CVs with comprehensive sections  
- 🌐 **Multi-Language Support**: English and Russian interface support  
- 💾 **Database Storage**: PostgreSQL backend for data persistence  
- 📄 **Professional PDF Export**: LaTeX-based PDF generation with beautiful formatting  
- 🔐 **User Authentication**: Secure login and registration system  
- 🎨 **Intuitive UI**: Clean JavaFX interface with responsive design  

**CV Sections Included:**
- Personal Information  
- Education History  
- Work Experience  
- Skills with proficiency levels  
- Languages with proficiency ratings  
- Professional Summary  

---

## 🛠️ Technology Stack

- **Frontend**: JavaFX 17+, FXML, CSS  
- **Backend**: Java 17+  
- **Database**: PostgreSQL  
- **PDF Generation**: XeLaTeX + FreeMarker templates  
- **Build Tool**: Maven (recommended)  

---

## 🚀 Usage

### Registration & Login
- Launch app → Register or login  
- Passwords securely stored in DB  

### Creating a CV
- Fill in personal info, education, work experience, skills & languages  

### Editing & Management
- View CVs on dashboard → Edit/Delete  

### Exporting to PDF
- Click **Print CV** → Professional LaTeX PDF generated  

---

## 🔧 Configuration

- **Database** → update credentials in `DatabaseHandler.java`  
- **LaTeX** → ensure `XeLaTeX` is in system PATH  
- **Languages** → add `language_xx_XX.properties` in `resources/bundle/`  



## 🐛 Troubleshooting

- **DB Connection Error** → Check credentials & ensure PostgreSQL is running  
- **PDF Fails** → Ensure `XeLaTeX` installed + templates exist  
- **UI Not Loading** → Verify JavaFX in classpath  
- **Language Issues** → Check `.properties` files  
### Database Setup

###  Configure connection in DatabaseHandler.java:
```
private static final String DB_URL = "jdbc:postgresql://localhost:5432/cv_builder";
private static final String DB_USER = "your-username";
private static final String DB_PASSWORD = "your-password";```
```


# Create the database:

```sql
CREATE DATABASE cv_builder;
-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- CVs table
CREATE TABLE cvs (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    summary TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Education table
CREATE TABLE education (
    id SERIAL PRIMARY KEY,
    cv_id INTEGER REFERENCES cvs(id) ON DELETE CASCADE,
    institution VARCHAR(200) NOT NULL,
    degree VARCHAR(100),
    field_of_study VARCHAR(100),
    start_date DATE,
    end_date DATE,
    description TEXT
);

-- Experience table
CREATE TABLE experience (
    id SERIAL PRIMARY KEY,
    cv_id INTEGER REFERENCES cvs(id) ON DELETE CASCADE,
    company VARCHAR(200) NOT NULL,
    position VARCHAR(100),
    location VARCHAR(100),
    start_date DATE,
    end_date DATE,
    description TEXT
);

-- Skills table
CREATE TABLE skills (
    id SERIAL PRIMARY KEY,
    cv_id INTEGER REFERENCES cvs(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    level INTEGER CHECK (level BETWEEN 1 AND 5)
);

-- Languages table
CREATE TABLE languages (
    id SERIAL PRIMARY KEY,
    cv_id INTEGER REFERENCES cvs(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    proficiency VARCHAR(20)
);
---
---
```
## 📸 Screenshots

### Log in 
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/0e6f25dd-bd49-419e-b0ed-af225bc238f3" />


### Register 
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/4e06b6f2-5140-4019-be8f-ca9b6b8c6a8b" />


### CV Editor



<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/3488661c-c18e-4a18-882f-e77ecfed986e" />

---------------------------------------------------------------------------------------------------------------------------------
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/e55174b7-ed01-4647-8b2a-d334172d3a51" />

---------------------------------------------------------------------------------------------------------------------------------
<img width="500" height="500" alt="image" src="https://github.com/user-attachments/assets/69d2a747-d28e-430a-bc8c-607d44d3d832" />


----------------------------------------------------------------------------------------------------------------------------------
### Eample of the generated pdf file (notic that the dates will be in the language of the system) :
<img width="744" height="825" alt="image" src="https://github.com/user-attachments/assets/87b1acaf-a07c-4bcb-a4b7-cb06558e475f" />






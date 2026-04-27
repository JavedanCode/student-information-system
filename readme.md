# 🎓 Student Information System (Java Swing)

A desktop-based Student Information System built using Java Swing.
This application simulates a simplified university automation system with role-based access for **Admin**, **Instructor**, and **Student** users.

---

## 🚀 Features

### 🔐 Authentication

* Secure login system using username and password
* Role-based access control

---

### 👨‍💼 Admin Panel

* Add / delete users
* Assign roles (Admin, Instructor, Student)
* Create and delete courses
* Automatic cleanup:

    * Deleting instructors removes their courses
    * Deleting users removes enrollments & grades

---

### 👨‍🏫 Instructor Panel

* View assigned courses
* See enrolled students
* Enter and update grades (midterm & final)
* Automatic grade validation (0–100 range)
* Pre-filled grade editing for better UX

---

### 🎓 Student Panel

* View available courses with live quota tracking
* Enroll in courses
* Drop courses
* View enrolled courses
* View transcript and GPA (auto-calculated)

---

## 💾 Data Persistence

* File-based storage system
* Data is preserved between application runs

### Files used:

* `users.txt`
* `students.txt`
* `courses.txt`
* `enrollments.txt`
* `grades.txt`

---

## 🧠 Technical Highlights

* Object-Oriented Design (OOP principles)
* Singleton pattern (`DataStore`)
* Java Swing GUI (multi-panel architecture)
* Event-driven programming
* Input validation system
* Clean separation of:

    * UI (gui)
    * Data logic (data)
    * Models (model)
    * Utilities (util)

---

## 🗂️ Project Structure

```
src/
│
├── app/        → Application entry & panel manager  
├── data/       → DataStore (file I/O, business logic)  
├── model/      → Core domain classes  
├── gui/        → Swing UI panels  
└── util/       → Validation & helper utilities  
```

---

## ▶️ How to Run

1. Clone the repository
2. Open in IntelliJ IDEA (or any Java IDE)
3. Ensure JDK 17+ (or newer) is configured
4. Run:

```
UniversityAutomationApp.java
```

---

## 👤 Default User (for testing)

```
Username: admin  
Password: 1234  
```

---

## 🧪 Key Behaviors

* Course quota updates instantly when students enroll/drop
* Deleting users automatically cleans related data
* Grades update GPA in real-time
* UI reflects changes immediately (no restart required)

---

## 📌 Future Improvements

* UI redesign (modern styling, layout improvements)
* Dark mode / theme support
* Database integration (replace file system)
* Advanced reporting features

---

## 🎯 Purpose

This project was developed as a comprehensive exercise in:

* Java Swing development
* Object-oriented programming
* System design
* Data persistence
* UI interaction and validation

---

## 📄 License

This project is for educational purposes.

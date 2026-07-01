# Staff Management System

> Software Testing Project - SWT301  
> FPT University

## 📖 Overview

Staff Management System is a web-based application developed using **Java Servlet, JSP, JDBC, and SQL Server** following the **Model-View-Controller (MVC)** architecture.

The system allows administrators to manage staff information through a simple and user-friendly interface. This project was developed as the Group Assignment for the **SWT301 - Software Testing** course.

---

## 🎯 Project Objectives

- Provide a staff management solution.
- Practice software testing methodologies.
- Apply unit, integration, and system testing.
- Improve software quality through static analysis and code review.

---

## ✨ Features

### Authentication

- User login
- Session management
- Authentication validation

### Staff Management

- View staff list
- Search staff
- Add new staff
- Update staff information
- Delete staff
- View staff details

---

## 🏗️ System Architecture

```
Browser
        │
        ▼
 JSP Pages (View)
        │
        ▼
Servlet Controllers
        │
        ▼
 DAO Layer
        │
        ▼
 SQL Server Database
```

The project follows the MVC architecture to separate presentation, business logic, and data access.

---

## 🛠️ Technologies Used

| Technology | Description |
|------------|-------------|
| Java 17 | Programming Language |
| Java Servlet | Controller Layer |
| JSP | View Layer |
| JDBC | Database Connectivity |
| SQL Server | Database |
| Apache Tomcat 10 | Web Server |
| Maven | Dependency Management |
| Git & GitHub | Version Control |

---

## 📂 Project Structure

```
StaffManagement
│
├── src
│   ├── controller
│   │     ├── LoginServlet.java
│   │     ├── StaffCrudServlet.java
│   │     └── StaffListServlet.java
│   │
│   ├── dao
│   │     ├── StaffDAO.java
│   │     └── RoleDAO.java
│   │
│   ├── entity
│   │     ├── Staff.java
│   │     └── Role.java
│   │
│   ├── utils
│   │     └── DBUtils.java
│   │
│   └── config
│         └── DataInitializer.java
│
├── webapp
│   ├── login.jsp
│   ├── staff-list.jsp
│   ├── staff-form.jsp
│   └── index.jsp
│
└── pom.xml
```

---

## 🧪 Testing Strategy

The project follows a comprehensive testing process consisting of:

### Static Analysis

- SonarLint
- SpotBugs
- PMD

### Code Review

- Java Coding Convention
- MVC Architecture Review
- Input Validation
- Exception Handling
- SQL Best Practices

### Unit Testing

- JUnit 5
- Mockito

Target coverage:

- DAO Layer
- Utility Classes

### Integration Testing

Integration testing verifies communication between:

```
Browser
    ↓
Servlet
    ↓
DAO
    ↓
SQL Server
```

### System Testing

System testing covers:

- Login
- Staff CRUD
- Search Staff
- Data Validation
- Database Operations

---

## 📋 Test Documents

The following testing documents are prepared for this project:

- Software Test Plan
- Unit Test Cases
- Integration Test Cases
- System Test Cases
- Requirement Traceability Matrix (RTM)
- Defect Log
- Test Summary Report

---

## 🚀 Getting Started

### Prerequisites

- JDK 17 or later
- Apache Tomcat 10
- SQL Server
- Maven
- NetBeans or IntelliJ IDEA

### Installation

1. Clone the repository

```bash
git clone https://github.com/NguyenLam7804/StaffManagement.git
```

2. Open the project in your IDE.

3. Configure the SQL Server connection in `DBUtils`.

4. Execute the database initialization script.

5. Deploy the project to Apache Tomcat.

6. Open your browser.

```
http://localhost:8080/StaffManagement
```

---

## 📊 Project Status

Current Status:

- ✅ Authentication Module
- ✅ Staff Management Module
- ✅ Database Integration
- ✅ CRUD Operations
- ✅ Search Function
- 🚧 Unit Testing
- 🚧 Integration Testing
- 🚧 System Testing

---

## 👥 Team Members

| Name | Role |
|------|------|
| Nguyen Quang Lam | Team Leader / Developer |
| Member 2 | Developer |
| Member 3 | Tester / QA |
| Member 4 | Tester |
| Member 5 | Documentation |

---

## 📚 Course Information

**Course:** SWT301 – Software Testing

**University:** FPT University

**Semester:** Summer 2026

---

## 📄 License

This project is developed for educational purposes as part of the SWT301 course at FPT University.

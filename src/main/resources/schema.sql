/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  admin
 * Created: Jul 20, 2026
 */
CREATE TABLE Role (
    RoleID INT PRIMARY KEY,
    RoleName VARCHAR(50)
);

CREATE TABLE Staff (
    StaffID INT PRIMARY KEY AUTO_INCREMENT,
    FullName VARCHAR(100),
    Gender BIT,
    PhoneNumber VARCHAR(20),
    Email VARCHAR(100),
    Password VARCHAR(100),
    IsActive BIT,
    RoleID INT,
    FOREIGN KEY(RoleID)
        REFERENCES Role(RoleID)

);

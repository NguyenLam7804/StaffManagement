/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  admin
 * Created: Jul 20, 2026
 */

INSERT INTO Role
VALUES
(1,'Admin'),
(2,'Manager');
INSERT INTO Staff
(
FullName,
Gender,
PhoneNumber,
Email,
Password,
IsActive,
RoleID
)
VALUES
(
'Nguyen Van A',
1,
'0123456789',
'a@gmail.com',
'123',
1,
1
);

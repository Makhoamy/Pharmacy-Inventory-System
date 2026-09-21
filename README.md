HealthFirst Pharmacy Management System
========================================

This is my project for managing a pharmacy's inventory and
sales. It's called "HealthFirst" and it's made with Java Swing
for the GUI and JDBC to connect to a MySQL database.

What it does
------------
- Keeps track of medicines (name, quantity, expiry date, supplier)
- Lets a cashier sell items and generate a bill
- Lets the admin see reports (sales, stock, medicines about to expire)
- Has two types of logins:
    - Cashier -> can only make sales
    - Admin -> can manage inventory and see reports

Tech used
---------
- Java (Swing/AWT for the GUI)
- JDBC for connecting to the database
- MySQL as the database

How to run it
--------------
1. Install MySQL and create a database called healthfirst_pharmacy
2. Run the schema.sql file to create the tables
3. Open DBConnection.java and put in your own DB username/password
4. Add the MySQL JDBC driver jar to the project (lib folder / build path)
5. Compile and run Main.java

Example run command:
    java -cp bin;lib/mysql-connector-j.jar Main
(on Mac/Linux use : instead of ; between paths)

Login types
-----------
Cashier:
- can search for medicines and add them to a bill
- can checkout and print a bill
- can't touch inventory or reports

Admin:
- can add/edit/delete medicines
- can add suppliers
- can view sales reports, stock reports, and expiry reports

Project files (roughly)
------------------------
src/
  Main.java
  Login.java
  AdminDashboard.java
  CashierDashboard.java
  InventoryPanel.java
  Billing.java
  PointOfSale.java
  UserManagement.java
  SupplierManagement.java
  MedicineManagement.java
  SaleChecks.java
  ReportsPanel.java
  DBConnection.java
database/
  schema.sql

Known issues / things to improve later
---------------------------------------
- Passwords are stored as plain text right now, should hash them
- No barcode scanner support yet
- Reports can't be exported as PDF yet

Made by: Makhosazane
For: BSc in Information Technology

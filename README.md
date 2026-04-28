# Student Transport Management System

A Java Swing application for managing school student transport operations including routes, drivers, and fee challans. The project provides admin and student dashboards, route management, driver assignment, and fee handling with a simple GUI and database-backed persistence.

## Key Features

- Admin dashboard: manage routes, drivers, and student approvals
- Student dashboard: view route and fee challan status
- Route management: add, view, and assign routes
- Driver assignment: link drivers to routes
- Fee challan generation and management
- Student registration and login
- Simple Swing-based UI with reusable components under src/ui/components
- Database persistence; includes `database_setup.sql` for initial schema

## Prerequisites

- Java JDK (11 or newer recommended)
- A SQL database (run `database_setup.sql` to create schema)

## How to run (Windows)

1. Open a PowerShell or Command Prompt in the project root.
2. Compile the project:

```powershell
.\compile.bat
```

3. Run the application:

```powershell
.\run.bat
```

4. Run tests:

```powershell
.\run_tests.bat
```

5. If the application needs a database connection, edit the settings in `src/config/DatabaseConnection.java` and import `database_setup.sql` into your database.

## Important Files

- Main launcher: [src/Main.java](src/Main.java)
- Database schema: [database_setup.sql](database_setup.sql)
- Database connection: [src/config/DatabaseConnection.java](src/config/DatabaseConnection.java)
- UI screens: [src/ui/screens](src/ui/screens)

## Notes

- The project includes batch scripts for Windows; adapt them for other platforms if needed.
- Place generated receipts in the `receipts/` folder.

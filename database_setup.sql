-- =====================================================
-- Smart University Transport Management System
-- Complete Database Setup for XAMPP / phpMyAdmin
-- Run this ONCE to create everything from scratch
-- =====================================================

CREATE DATABASE IF NOT EXISTS transport_db;
USE transport_db;

-- ── Routes table ─────────────────────────────────────
CREATE TABLE IF NOT EXISTS routes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    route_name VARCHAR(150) NOT NULL,
    total_stops INT DEFAULT 0,
    distance DOUBLE DEFAULT 0.0
);

-- ── Drivers table ────────────────────────────────────
CREATE TABLE IF NOT EXISTS drivers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    driver_name VARCHAR(120) NOT NULL,
    route_id INT,
    FOREIGN KEY (route_id) REFERENCES routes(id)
);

-- ── Student Users (for login/auth) ───────────────────
CREATE TABLE IF NOT EXISTS student_users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(120) NOT NULL,
    student_roll VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ── Students (transport registration requests) ───────
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(120) NOT NULL,
    student_roll VARCHAR(50),
    selected_route_id INT,
    assigned_route_id INT,
    bus_number INT,
    status VARCHAR(20) DEFAULT 'PENDING',
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (selected_route_id) REFERENCES routes(id),
    FOREIGN KEY (assigned_route_id) REFERENCES routes(id)
);

-- ── Sample Routes (optional, delete if not needed) ───
INSERT INTO routes (route_name, total_stops, distance) VALUES
('Campus to City Center', 8, 15.5),
('Campus to Airport', 12, 32.0),
('Campus to Railway Station', 6, 10.2);

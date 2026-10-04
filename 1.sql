DROP DATABASE IF EXISTS railway_db;
CREATE DATABASE railway_db;
USE railway_db;

CREATE TABLE customers (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    national_id VARCHAR(20) UNIQUE NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL
);

CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    role VARCHAR(50) DEFAULT 'Staff'
);

CREATE TABLE stations (
    station_id INT PRIMARY KEY AUTO_INCREMENT,
    station_name VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL
);

CREATE TABLE trains (
    train_id INT PRIMARY KEY AUTO_INCREMENT,
    train_name VARCHAR(100) NOT NULL,
    train_type VARCHAR(50),
    capacity INT NOT NULL
);

CREATE TABLE trips (
    trip_id INT PRIMARY KEY AUTO_INCREMENT,
    train_id INT NOT NULL,
    departure_station INT NOT NULL,
    arrival_station INT NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(30) DEFAULT 'Available',
    FOREIGN KEY (train_id) REFERENCES trains(train_id),
    FOREIGN KEY (departure_station) REFERENCES stations(station_id),
    FOREIGN KEY (arrival_station) REFERENCES stations(station_id)
);

CREATE TABLE tickets (
    ticket_id INT PRIMARY KEY AUTO_INCREMENT,
    customer_id INT NOT NULL,
    trip_id INT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    booking_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    ticket_status VARCHAR(30) DEFAULT 'Booked',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (trip_id) REFERENCES trips(trip_id)
);

CREATE TABLE payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    ticket_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    payment_status VARCHAR(30) DEFAULT 'Paid',
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id)
);

INSERT INTO employees (full_name, username, password, role)
VALUES ('System Admin', 'admin', '1234', 'Admin');

INSERT INTO stations (station_name, city) VALUES
('Riyadh Station', 'Riyadh'),
('Dammam Station', 'Dammam'),
('Jeddah Station', 'Jeddah'),
('Makkah Station', 'Makkah'),
('Madinah Station', 'Madinah');

INSERT INTO trains (train_name, train_type, capacity) VALUES
('SAR Express 1', 'Passenger', 24),
('SAR Express 2', 'Passenger', 24),
('Haramain Express', 'Passenger', 24);

INSERT INTO trips 
(train_id, departure_station, arrival_station, departure_time, arrival_time, price, status)
VALUES
(1, 1, 2, '2026-05-01 08:00:00', '2026-05-01 12:00:00', 120.00, 'Available'),
(2, 2, 1, '2026-05-02 09:00:00', '2026-05-02 13:00:00', 120.00, 'Available'),
(3, 3, 4, '2026-05-03 10:00:00', '2026-05-03 11:30:00', 80.00, 'Available'),
(3, 4, 5, '2026-05-04 15:00:00', '2026-05-04 17:00:00', 95.00, 'Available');
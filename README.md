
# Loan Management API

A backend system built using Spring Boot that simulates a basic loan processing workflow.
The project demonstrates core backend and platform engineering concepts such as layered architecture, RESTful APIs, database integration, and error handling.

---

## Overview

This application allows users to:

* Create and manage users
* Apply for loans
* Track loan status

It is designed as a foundational system that can be extended into a microservices-based fintech platform.

---

## Architecture

The project follows a **layered architecture**:

* **Controller Layer** → Handles HTTP requests and responses
* **Service Layer** → Contains business logic
* **Repository Layer** → Handles database interactions
* **Model Layer** → Defines entities (User, Loan)

---

## Tech Stack

* Java
* Spring Boot
* Spring Data JPA
* H2 Database
* Maven

---

## API Endpoints

### Create User

POST /api/users

### Apply Loan

POST /api/loans/{userId}

### Get Loans by User

GET /api/loans/{userId}

### Test API

GET /api/test

---

## Sample Request

### Create User

```json
{
  "name": "Aadarsh",
  "email": "aadarsh@example.com"
}
```

### Apply Loan

```json
{
  "amount": 50000
}
```

---

## How to Run

1. Clone the repository
2. Navigate to project folder
3. Run the application:

```bash
mvn spring-boot:run
```

4. Access H2 Console (optional):
   http://localhost:8080/h2-console

---

## Features Implemented

* RESTful API design
* Entity relationships (User → Loan)
* Basic loan workflow (apply & track)
* Exception handling (basic)
* Clean layered structure

---

## Future Improvements

* Authentication & Authorization (JWT)
* Microservices architecture
* API Gateway integration
* Docker containerization
* CI/CD pipeline

---

## Status

Work in progress – actively being enhanced.

---

## Author

Aadarsh Pratap
B.Tech CSE Student

---

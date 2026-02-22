# Mundial 2026 Hub

## Description

Mundial 2026 Hub is an information system designed to centralize and optimize the digital experience of the 2026 World Cup. The platform provides fans with a reliable environment for match tracking, personalized scheduling, participation in social features such as prediction pools and a digital album, and secure ticket management.

The system is built with a strong focus on transparency, event traceability, security, and availability.

---

## Architecture

The backend is implemented as a modular monolithic application using:

- Java  
- Spring Boot  
- Layered architecture (Controller – Service – Repository)  
- RESTful API design  
- JWT-based authentication and authorization  
- Relational database persistence  
- Structured logging for traceability  

The application follows object-oriented design principles and enforces clear separation of responsibilities across layers.

---

## Project Objective

The objective of this project is to develop a backend platform capable of:

- Managing user accounts and preferences  
- Centralizing match-related information  
- Controlling ticket lifecycle states with full traceability  
- Supporting social interaction features such as prediction pools  
- Managing a digital album with controlled exchanges  
- Recording relevant system events for auditing purposes  

The system emphasizes controlled state transitions, data integrity, and operational transparency.

---

## Security

- JWT-based authentication  
- Role-based access control  
- Centralized exception handling  
- Service-layer validation  
- Event logging for auditability  

Sensitive operations such as ticket transfers and state changes are logged to ensure traceability.

---
## Running the Application

1. Clone the repository  
2. Configure environment variables (database connection and JWT secret)  
3. Run the application:



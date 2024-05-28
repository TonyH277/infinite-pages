# 📃Infinite Pages

![Infinite Pages Logo](images/infinite_pages_logo.jpg)

Welcome to Infinite Pages – the ultimate backend for your online bookshop! This project was inspired by the desire to create a seamless and efficient online platform for book lovers and sellers alike. With the growing demand for online bookstores, Infinite Pages aims to provide a robust, secure, and user-friendly backend system to manage book inventories, user accounts, and orders.

## 📋 Structure
- [📓Introduction](#-introduction)
- [📓Technologies Used](#-technologies-used)
- [📓Features](#-features)
- [📓Installation](#-installation)
- [📓Postman Collection](#-postman-collection)
- [📓Challenges Faced](#-challenges-faced)
- [📓Demo](#-demo)
- [📓License](#-license)


## 🚀 Introduction

Infinite Pages aims to simplify the process of managing an online bookstore. By leveraging modern technologies, it offers a reliable and efficient backend system to handle book inventories, user management, and order processing.

## 👨🏻‍💻 Technologies Used

**Infinite Pages** utilizes the following technologies and tools:

- **Spring Boot**: Simplifies the development of Java applications.
- **Spring Security**: Secures the application with robust authentication and authorization mechanisms.
- **Spring Data JPA**: Facilitates seamless database interactions.
- **Swagger**: Provides interactive API documentation and testing.
- **Mockito**: Helps to isolate the code under test by mocking its dependencies, making it easier to write effective unit tests.
- **Testcontainers**: Provides lightweight, throwaway instances of Docker containers for integration testing.


## ✨ Features

**Infinite Pages** includes a variety of features to manage an online bookstore effectively:

- **User Management**: Registration and authentication.
- **Book Management**: Adding, updating, deleting, and viewing books.
- **Order Management**: Placing orders, viewing order history, and managing order statuses.
- **Category Management**: Creating, updating, and deleting book categories.
- **Search and Filter**: Searching for books and filtering results based on various criteria.

## ⬇ Installation

Follow this steps to run Infinite Pages:
1. **Clone the repository**:
   ```bash
   git clone https://github.com/TonyH277/infinite-pages.git
   ```

2. **Navigate to the project directory**:
   ```bash
   cd infinite-pages
   ```
3. **Set Environment Variables**:

   Create a `.env` file in the project root directory and populate it with the following environment variables:
   ```env
   MYSQLDB_USER=your_db_user_name
   MYSQLDB_ROOT_PASSWORD=your_db_password
   JWT_SECRET=your_Jwt_Secret_Key
   JWT_EXPIRATION=3000000
   
   MYSQLDB_DATABASE=your_db_name
   MYSQLDB_LOCAL_PORT=3306
   MYSQLDB_DOCKER_PORT=3306
   
   SPRING_LOCAL_PORT=8080
   SPRING_DOCKER_PORT=8080
   DEBUG_PORT=5005
   ```

4. **Build the project**:
   ```bash
   ./mvnw clean install
   ```

5. **Build and Run the Docker Containers**:

    - Make sure you have Docker installed on your machine. If not, please visit the [official Docker website](https://www.docker.com/products/docker-desktop/) and download it before proceeding.

   ```bash
   docker-compose up
   ```


6. **Access the Application**:

   Open your browser and go to http://localhost:8080/api/swagger-ui.html to access the Swagger API documentation.

## 📨 Postman Collection
[![Run In Postman](https://run.pstmn.io/button.svg)](https://god.gw.postman.com/run-collection/27654110-e9da90b5-a162-403e-aa61-86128ee91941?action=collection%2Ffork&source=rip_markdown&collection-url=entityId%3D27654110-e9da90b5-a162-403e-aa61-86128ee91941%26entityType%3Dcollection%26workspaceId%3D0c7c53b2-2c1b-4b07-8cf8-3513ebbf748b)

## 🧠 Challenges Faced
**Security**: Setting up robust authentication and authorization using Spring Security, and implementing JWT (JSON Web Token) authentication for secure communication between client and server.

**Database Management**: Utilized Spring Data JPA and Hibernate for efficient database interactions and ORM capabilities.

**Database Migrations**: Implementing database migrations with Liquibase to ensure consistency across different environments.

**API Documentation**: Ensuring comprehensive API documentation with Springdoc OpenAPI.

**Global Exception Handling**: Implementing a custom global exception handler to ensure correct responses for clients in various error scenarios.

**Testing**: While routine, proper testing was necessary to ensure the correct functionality of my application, covering unit, integration, and end-to-end tests.

**Dockerization**: Utilizing Docker to containerize the application, ensuring portability and the ability to run the application consistently across different environments.

## 🎬 Demo

[Watch the demo](https://www.loom.com/share/a3f5e8ef8bc3424aafa6afb0a083ebd3?sid=a6095b47-eab9-448f-a4d8-e0095cfbc324)

## 👨🏾‍⚖️ License

[MIT](https://choosealicense.com/licenses/mit/)
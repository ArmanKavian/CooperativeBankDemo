# CoBank Service API

The CoBank Service API manages core banking operations, providing RESTful endpoints for account creation, balance retrieval, transaction processing (deposits and withdrawals), and transaction history retrieval. Built with an API-first approach, this service ensures each operation is accessible, well-documented, and ready for integration with external systems or client applications.

## Technologies Used

This project uses Spring Boot for REST API development, Spring Data JPA for database interactions, Spring Cache for performance optimization, and Spring Retry for handling transient failures. Postgres is employed for data storage, with Flyway migrations facilitating easy setup and versioning. OpenAPI 3.0 provides interactive API documentation via Swagger UI, and Docker is used to simplify deployment and testing.

## How to Run

1. **Setup Docker Environment**: Ensure Docker is installed and running.
2. **Build and Run Containers**:
    - Execute `docker-compose up --build` to start the application and database.
3. **Access the API**:
    - Visit `http://localhost:8080/swagger-ui.html` to explore API documentation and interact with endpoints.

## Main Design Choices

1. **API-First Design**: A RESTful approach to support standardized, easy-to-consume endpoints.
2. **Transactional Integrity**: Key operations are transactional to maintain data integrity, using specific isolation levels for concurrency.
3. **Retry and Recovery Mechanism**: Transactions include retry mechanisms for lock contention and failure resilience.
4. **Caching**: Transaction history retrieval is cached to enhance performance for frequently accessed data.
5. **Use Case Interfaces**: Defines use case interfaces like `CreateAccountUseCase` and `ProcessTransactionUseCase`, which are then implemented by service classes, keeping the business logic modular and testable.
6. **Fallbacks and Recoveries**: In the case of transaction failures, fallback mechanisms ensure a safe recovery path.
7. **Comprehensive Exception Handling**: A `GlobalExceptionHandler` handles validation, database, and general exceptions, returning meaningful error responses to the client.
8. **IBAN Generation Service**: The `IbanService` ensures unique and sequential IBAN generation, making it easier to maintain uniqueness without complex algorithms.
9. **Database Migrations with Flyway**: Flyway is used for schema migrations, ensuring a consistent database structure across environments.
10. **Separation of Concerns**: The design separates core concerns by defining distinct service, repository, and controller layers.
11. **OpenAPI Documentation**: Detailed API documentation allows for easy API exploration and testing.
12. **Configurable Security**: Security settings are managed in the `config` package, allowing for customizable authentication and authorization. While currently using basic authentication, this setup could easily be enhanced with more advanced security technologies, such as OAuth2 or JWT, for greater flexibility and robustness in securing endpoints.
13. **Customizable via Properties**: Configurations like retry delays, cache timeouts, and IBAN formats are adjustable via application properties for flexibility.
14. **Dependency Injection**: Constructor injection for services and repositories promotes testability and modularity.
15. **Dockerized Environment**: Docker allows the entire application to be spun up with dependencies, facilitating consistent deployment.
16. **Git Workflow**: The project follows a streamlined Git workflow with a feature branch for ongoing development and a master branch for stable, production-ready code. This approach keeps new features isolated until they’re tested and ready for release.

## Key Application Flows

1. **Account Creation**:
    - `createAccount` API receives account data, generates a unique IBAN, and saves the new account to the database.

2. **Balance Retrieval**:
    - `getBalance` API retrieves the balance for a given IBAN, providing fast and accurate financial data access.

3. **Transaction Processing**:
    - `processTransaction` API handles deposit and withdrawal requests, applying transaction logic and updating account balances in real-time.

4. **Transaction History Retrieval**:
    - `getTransactionHistory` API returns a paginated list of transactions for a specific account, leveraging caching to optimize performance.

## Package and Class Overview

- **api**: Contains API interfaces for defining endpoints and request/response schemas.
- **service**: Implements business logic, including `AccountService` and `TransactionService` for managing accounts and transactions, as well as `IbanService` for generating unique IBANs.
- **repository**: JPA repositories for accessing account and transaction data.
- **domain**: Entity classes representing `Account` and `TransactionHistory`.
- **config**: Manages API configurations and security settings.

This API-first, modular architecture enables clear separation of concerns and makes the application scalable, maintainable, and highly adaptable to complex business needs. The design choices ensure robust data handling, efficient response times, and flexibility in configuration, making this service a reliable foundation for enterprise-level banking operations.
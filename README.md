# distributed-metrics-platform
A scalable architecture comprising a PC client that measures CPU metrics and a server that exposes this data via a REST API.

## Overview

This project demonstrates a distributed system where a client application collects CPU metrics from a local machine and transmits them to a server. The server processes and exposes these metrics through a RESTful API, facilitating real-time monitoring and analysis.

## Architecture

The system is composed of two main components:

- **Client Application**: A Spring Boot application that runs on a PC, collecting CPU usage metrics at regular intervals.
- **Server Application**: A Spring Boot application that receives metric data from the client and exposes it through a REST API.

Both components are containerized using Docker and orchestrated with Docker Compose to ensure seamless deployment and scalability.

## Features

- **Real-time Metrics Collection**: The client application collects CPU usage metrics at configurable intervals.
- **REST API Exposure**: The server application exposes the collected metrics through a RESTful API.
- **Dockerized Deployment**: Both client and server applications are containerized using Docker, allowing for easy deployment and scalability.
- **Docker Compose Orchestration**: Docker Compose is used to manage the multi-container setup, ensuring all components work together seamlessly.

## Prerequisites

- Docker
- Docker Compose
- Java 17
- Maven

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/sacha-muratori/distributed-metrics-platform.git
cd distributed-metrics-platform
```

### 2. Build the Docker Images

You can simply run this following command in the project directory:

```bash
docker compose build
```

### 3. Start the Services

Use Docker Compose to start the client and server applications:

```bash
docker compose up
```

This command will start 3 Clients (for testing purposes) and the Server applications in separate containers.

## Usage

### Metrics Query Endpoints (REST API)

These endpoints are part of the `/api/metrics` controller. They return CPU usage metrics from clients.
More endpoints can be made available when needed.

#### Endpoints Summary

##### 1. GET `/api/metrics/today`
- **Description**: Returns today’s metrics.
- **Optional Param**: `clientId`

##### 2. GET `/api/metrics/day`
- **Description**: Returns metrics for a given day.
- **Params**:
  - `day` (e.g., `2025-05-24`)
  - `clientId` (optional)

##### 3. GET `/api/metrics/past-hour`
- **Description**: Returns metrics from the past hour.
- **Optional Param**: `clientId`

##### 4. GET `/api/metrics/range`
- **Description**: Returns metrics between two times on a given day.
- **Params**:
  - `day` (e.g., `2025-05-24`)
  - `from` (e.g., `14:30`)
  - `to` (e.g., `16:45`)
  - `clientId` (optional)

You can query the data after few minutes by using Postman, or performing a curl to localhost.

## Notes

- The client application collects CPU metrics at regular intervals. You can configure the interval by modifying the `application.properties` file in the `client-app` directory OR by modifying the contents present in the `metrics_policy` collection document in the MongoDB.
- The client application collects the metrics configuration from the server, like the scheduled intervals times and the type of metrics.
- The server application processes and exposes the collected metrics through a REST API. You can extend this application to include additional endpoints for querying different types of metrics.
- Both applications are containerized using Docker, allowing for easy deployment and scalability. You can deploy these applications to any environment that supports Docker.

## Future Improvements

- **Code Quality Enhancements**: Improve code cleanness by adopting consistent coding standards, enhancing exception handling strategies, and using well-defined interfaces with clean implementations to increase maintainability.
- **Comprehensive Testing**: Implement thorough unit, integration, load and chaos testing, including chaos testing to validate system resilience and fault tolerance under unexpected failures.
- **Security and Observability**: Add authentication/authorization, encrypted communication between client/server, and advanced observability with distributed tracing and metrics dashboards.

- **Client Location Awareness**: Enhance the client to send network-based location data (e.g., AWS region, data center location) to the server for better context-aware metrics aggregation and analysis.
- **Kafka, Server, and MongoDB Scalability**:
    - Introduce replication and sharding for Kafka brokers, the server application, and MongoDB to improve fault tolerance, availability, and horizontal scalability.
    - Use partitioning keys intelligently based on network topology and data locality to optimize data distribution and query performance.

- **Final Considerations**: This is just an MVP. In the future I would use protobuf with pre-defined versionable schema over gRPC and a time-series DB.

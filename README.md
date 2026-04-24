# Smart Campus Sensor and Room Management API

## Overview
This project is a RESTful API developed for the 5COSC022W Client-Server Architectures coursework.

It implements a Smart Campus system to manage:
- Rooms: Represents a physical campus room. Each room has an `id`, `name`, `capacity`, and a list of sensor IDs assigned to it.
- Sensors: Represents a smart campus sensor such as Temperature or CO2. Each sensor has an `id`, `type`, `status`, `currentValue`, and `roomId`.
- Sensor Readings: Represents a historical reading captured by a sensor. Each reading has an `id`, `timestamp`, and `value`.

The API follows RESTful design principles by:

- using resource-based URIs
- using HTTP methods such as GET, POST, and DELETE
- returning JSON responses
- supporting filtering with query parameters
- using nested resources for sensor readings

The API is built using JAX-RS (Jersey) and runs on Apache Tomcat. It uses in-memory data structures (`HashMap`, `ArrayList`) instead of a database.

## Technologies Used
- Java
- JAX-RS (Jersey)
- Maven
- Apache Tomcat
- JSON

## How to Run

1. Open the project in Apache NetBeans.
2. Ensure Apache Tomcat is configured.
3. Build the project: Right-click project then select Clean and Build
4. Run the project: Right-click project and select Run
5. Base URL:
   http://localhost:8080/SmartCampusAPI/api/v1

## API Endpoints

### Discovery
- GET /api/v1

### Rooms
- GET /api/v1/rooms
- POST /api/v1/rooms
- GET /api/v1/rooms/{roomId}
- DELETE /api/v1/rooms/{roomId}

### Sensors
- GET /api/v1/sensors
- GET /api/v1/sensors?type=CO2
- POST /api/v1/sensors

### Sensor Readings
- GET /api/v1/sensors/{sensorId}/readings
- POST /api/v1/sensors/{sensorId}/readings

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. Get all rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 3. Create a room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
-H "Content-Type: application/json" \
-d "{\"id\":\"SCI-202\",\"name\":\"Science Seminar Room\",\"capacity\":60}"
```

### 4. Create a sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"CO2-101\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":415.5,\"roomId\":\"SCI-202\"}"
```

### 5. Filter sensors by type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 6. Add a sensor reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-101/readings \
-H "Content-Type: application/json" \
-d "{\"id\":\"READ-CO2-2\",\"timestamp\":1713523333333,\"value\":430.8}"
```

## Error Handling

The API handles errors using custom exceptions:

- 404 Not Found → Resource does not exist  
- 409 Conflict → Room has sensors and cannot be deleted  
- 422 Unprocessable Entity → Linked room does not exist  
- 403 Forbidden → Sensor is in maintenance mode  
- 500 Internal Server Error → Unexpected errors  

## Logging

A logging filter records:

- HTTP request method  
- Request URI  
- Response status code  

Logs are visible in the Apache Tomcat console.



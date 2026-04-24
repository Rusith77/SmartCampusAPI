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

- 404 Not Found - Resource does not exist  
- 409 Conflict - Room has sensors and cannot be deleted  
- 422 Unprocessable Entity - Linked room does not exist  
- 403 Forbidden - Sensor is in maintenance mode  
- 500 Internal Server Error - Unexpected errors  

## Logging

A logging filter records:

- HTTP request method  
- Request URI  
- Response status code  

Logs are visible in the Apache Tomcat console.

# Coursework Questions

## Part 1: Service Architecture & Setup

### 1. Project & Application Configuration

**Answer:**

The default resource class lifecycle in JAX-RS is per-request, so a new resource instance is created per each incoming HTTP request. Thus Resource classes are  not singletons, and instance variables are not shared between requests.
This project uses shared in-memory data structures (HashMap, ArrayList) stored as static fields in a DataStore class to keep data across requests. This makes sure that the same data can be accessed and changed by all resource instances.
Nevertheless, these data structures may be shared, so they are prone to race condition when there are multiple requests accessing or modifying the same time.Synchronization has not been applied in this coursework since the environment is simple and controlled. To maintain data consistency, thread-safe data structures like ConcurrentHashMap, or synchronization mechanisms would be needed in a real-world system.

### 2. The ”Discovery” Endpoint

**Answer:**

Hypermedia is regarded as a feature of an advanced RESTful design as it enables the API to provide guidance to the clients by including useful resource links within the responses. Rather than depending only on external documentation, the client can find available actions and related resources directly from the API.
The discovery endpoint at GET /api/v1 in this project returns core API metadata along with links to the main collections, such as /api/v1/rooms and /api/v1/sensors. This simplifies the API and makes it easier to explore and utilize.

This is an advantage over using documentation that is static since client developers have better usability and less guesswork. Clients are not required to hard-code all possible paths beforehand, and navigate the API more easily based directly from the response. It also makes the API more maintainable, since when resource paths or available endpoints change, the discovery response can be used to communicate those changes in a more clear manner.

## Part 2: Room Management

### 1. Room Resource Implementation

**Answer:**

Two primary ways of returning a list of rooms are to only return room IDs or to return complete room objects.
The use of IDs only minimizes the network bandwidth usage, since the response is smaller.  This can enhance performance, particularly when there are numerous rooms. Nevertheless, it increases processing on the client-side, as the client would have to make additional requests (e.g., GET /rooms/{id}) in order to retrieve full details for each room.
Conversely, full room objects returned as done in this project, provides complete information in a single response. This makes the client development easier and minimizes the number of API calls required. Anyhow,it makes the size of the response larger, potentially affecting bandwidth and performance in case of large dataset.
Therefore, returning full objects is more convenient for clients, while returning only IDs is more efficient when considering the network usage. The decision will be based on the data size and the application requirements.

### 2. Room Deletion & Safety Logic

**Answer:**

Yes, the DELETE operation is idempotent in principle, because once a room has been deleted, repeating the same request should not affect the ultimate state any more. The goal state remains the same, the room does not exist.
In this implementation, a delete request is blocked and a custom error response is sent in case a room has sensors assigned to them. If the room has no sensors, it is cleared off the in-memory store.
In case of re-sending the same DELETE request, even after the successful deletion, the room will no longer exist, and instead of deleting again, the API sends a response with a style of a not found. Therefore, a first request alters the state by removing the room, while repeated identical requests do not produce any further alteration.
This is why the operation is considered idempotent: several identical DELETE requests lead to the same final resource state, although the HTTP response may differ between the first and later requests.

## Part 3: Sensor Operations & Linking

### 1. Sensor Resource & Integrity

**Answer:**

The @Consumes(MediaType.APPLICATION_JSON) annotation specifies that the POST method only accepts requests with the Content-Type set to application/json.
If a client sends data in a different format, such as text/plain or application/xml, JAX-RS will not be able to match the request to the method. As a result, the request is rejected automatically by the framework.
In this case, JAX-RS returns a 415 Unsupported Media Type response, indicating that the server cannot process the request because the payload format is not supported.
This behavior ensures that the API only processes data in the expected format, preventing parsing errors and maintaining consistency in how request data is handled.

### 2. Filtered Retrieval & Search

**Answer:**

Using @QueryParam for filtering (e.g., /api/v1/sensors?type=CO2) is generally preferred because it clearly represents a filtering operation on a collection, rather than a request for a different resource.
In this project, /api/v1/sensors represents the full collection of sensors, and the type query parameter is used to refine or filter that collection. This aligns with REST principles, where query parameters are used for searching, filtering, and optional criteria.
In contrast, using a path like /api/v1/sensors/type/CO2 suggests a more rigid structure and implies a distinct sub-resource, which reduces flexibility. It becomes harder to combine multiple filters (e.g., type and status) and leads to more complex URL designs.
The query parameter approach is superior because it is:

* more flexible (supports multiple filters easily)
* more intuitive for clients
* consistent with standard REST conventions for querying collections

Therefore, using @QueryParam provides a cleaner and more scalable design for filtering and searching resources.

## Part 4: Deep Nesting with Sub - Resources

### 1. The Sub-Resource Locator Pattern

**Answer:**

The Sub-Resource Locator pattern improves the structure of an API by delegating responsibility for nested resources to separate classes. Instead of handling all endpoints in a single large resource class, each related resource (such as sensor readings) is managed in its own dedicated class.
In this project, sensor-related operations are handled in SensorResource, while reading-related operations are handled in a separate ReadingResource. This separation makes the code more modular and easier to maintain.
If all nested paths (e.g., /sensors/{id}/readings/{rid}) were implemented in a single class, it would quickly become large and difficult to manage. This increases complexity, reduces readability, and makes future changes more error-prone. By using the Sub-Resource Locator pattern:

* responsibilities are clearly separated
* code is easier to understand and extend
* each resource class focuses on a specific part of the API

This approach is especially beneficial in large APIs, as it improves scalability and maintainability compared to a single, monolithic controller class.

## Part 5: Advanced Error Handling, Exception Mapping & Logging

### 1. Dependency Validation

**Answer:**

HTTP 422 Unprocessable Entity is more semantically accurate in this scenario because the request itself is well-formed and syntactically correct, but contains invalid data in its content.
In this project, when a client sends a POST request to create a sensor, the JSON body is valid, but the roomId provided does not correspond to an existing room. This means the server understands the request format, but cannot process it due to a logical validation failure.
In contrast, a 404 Not Found is typically used when the requested resource in the URL does not exist. In this case, the endpoint /api/v1/sensors does exist, so returning 404 would not accurately describe the problem.
Therefore, using 422 Unprocessable Entity clearly indicates that:

* the request structure is valid
* the error lies in the request data itself (invalid reference)

This makes the API more precise and informative for clients, helping them understand and correct the issue more effectively.

### 2. The Global Safety Net

**Answer:**

Exposing internal Java stack traces to external API consumers poses significant security risks, as it reveals sensitive details about the system’s internal structure and implementation.
A stack trace can disclose:

* class names and package structures, which reveal how the application is organized
* file names and line numbers, which indicate exactly where errors occur
* frameworks and libraries used, which may expose known vulnerabilities
* internal logic and method calls, which can help attackers understand system behavior

This information can be used by an attacker to perform targeted attacks, such as exploiting specific vulnerabilities, identifying weak points in the code, or crafting malicious inputs to trigger failures.
By returning a generic 500 Internal Server Error instead of the full stack trace, the API prevents leaking internal details while still informing the client that an unexpected error occurred. This improves the overall security and robustness of the system.

### 3. API Request & Response Logging Filters

**Answer:**

Using JAX-RS filters for logging is advantageous because it allows logging to be handled centrally and consistently for all requests and responses.
In this project, the filter automatically logs the HTTP method, URI, and response status for every endpoint, without adding code inside each resource method. If logging were done manually using Logger.info() in every method, it would lead to code duplication, a higher chance of missing logs, and more difficult maintenance. Using filters follows the principle of separation of concerns, keeping logging separate from business logic, and improves maintainability, consistency, and scalability.
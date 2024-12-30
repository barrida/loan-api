# Modernized and Containerized OAuth 2.0 Authorization Server Sample

This project showcases an upgraded and containerized OAuth 2.0 Authorization Server using Spring Security. It supports both authorization_code and client_credentials grant types, along with OpenID Connect 1.0, and is configured to issue JWT tokens signed with the RS256 algorithm. The project features enhancements including an upgrade to Gradle 8.9 and Java 17 for improved performance and compatibility, as well as comprehensive Dockerization with multi-stage builds, a lightweight Alpine-based image, dynamic port selection, and shared network configuration for microservices integration.

# List of improvements

### **Upgrade to Gradle 8.9 and Java 17**  
Brought the project up to date with the latest Gradle and Java versions for better performance and compatibility.

### **Dockerized the Project**   
The application is now containerized with Docker, making it easy to run alongside other microservices on a shared network. 

- **Multi-Stage Builds**: The Dockerfile uses a multi-stage build process to keep the final image small and efficient. Only the necessary files are included in the final image. 
- **Alpine-Based Image**: The Docker image is based on Alpine Linux, keeping it lightweight while ensuring all dependencies are covered.
- **Dynamic Port Selection**  
 Added support for dynamic port selection to make the application more flexible in different environments. Configurations for this feature can be found in the `application.yml` and `docker-compose.yml` files:

   - `application.yml`:

     ```yaml
     server:
       port: ${PORT:9090}  # Defaults to 9090 if not provided
     ```
   - `docker-compose.yml`:
     ```yaml
         ports:
           - "${PORT:-9090}:${PORT:-9090}" # Defaults to 9090 if not provided
     ```

- **Shared Network Configuration**  
The Docker setup includes a shared network configuration for seamless integration with other microservices. You need to define the `shared-network` for your microservices, such as `my-app` service below: 

```yaml
services:
  my-app:
    container_name: my-app
    ports:
      - 8091-8093:8091-8093
    networks: ## define shared network(s) here
      - shared-network

networks:
  todo-network:
    driver: bridge
```
# Run Application

### Run a as Docker container:  

When running docker-compose, set the `PORT` environment variable in your shell. Application uses `9090` as a fallback if `PORT` is not set.

```bash
PORT=9090 docker compose up --build -d
```

## Run as a stand-alone application:  

```bash
PORT=9090 ./gradlew bootRun 
```

# Retrieve Access Token

Once the application is up and running you can get token via `Postman` or `curl`:  

### Using Postman
You can import `Authorization Server.postman_collection.json` collection to Postman if you don't want to deal with curl requests manually.

### Using Curl

#
Use `scope=message:read` for read requests, such as HTTP GET

```shell
curl -X POST client:secret@localhost:9090/oauth2/token -d "grant_type=client_credentials" -d "scope=read"
```

Use `scope=message:write` for wrote requests, such as POST, PUT, PATCH, and DELETE

```shell
curl -X POST client:secret@localhost:9090/oauth2/token -d "grant_type=client_credentials" -d "scope=write"
```

This returns something like the following:

```json
{
    "access_token": "eyJraWQiOiI4YWY4Zjc2Zi0zMTdkLTQxZmYtYWY5Yi1hZjg5NDg4ODM5YzciLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...",
    "expires_in": 299,
    "scope": "read",
    "token_type": "Bearer"
}
```



# Use Access Token

Once you retrieve the token, export the access token:   

```bash
export TOKEN=...
```

Then issue the following request from your Resource Server:

```bash
curl -H "Authorization: Bearer $TOKEN" localhost:8080
```


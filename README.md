# Run Application
In root director,run

```shell
./gradlew bootRun 
```

# OpenAPI Swagger endpoint definition

Browse to Swagger to analyse endpoints http://localhost:8080/swagger-ui/index.html#/

# Run Authorization Server
build*Assumptions:**
- ADMIN can operate for all customers. Only admins can create and list customers. CUSTOMER can operate for themselves.
- The .requestMatchers("/v1/**").hasAnyAuthority("ADMIN", "CUSTOMER") ensures that both scopes (ADMIN and CUSTOMER) can access any endpoint under "/v1/**".
- For example: The @PreAuthorize("hasAuthority('ADMIN') or (hasAuthority('CUSTOMER') and #loanRequest.customerId == principal.id)") ensures that ADMIN can create loans for any customer. CUSTOMER can only create loans for themselves.

You must run auth server to access endpoints. I slightly modified my own Auth server [sample](https://github.com/barrida/authorization-server). If you want to run Dockerized version, update the roles as ADMIN and CUSTOMER, then follow the steps in auth server repository. OOtherwise, follow instructions below:

## Step 1: Run auth-server locally
In root director, run

```shell
./gradlew bootRun 
```

## Step 2: Get a JWT token from the Authorization Server.
Use `scope=CUSTOMER` for GET requests

```shell
curl -X POST client:secret@localhost:9090/oauth2/token -d "grant_type=client_credentials" -d "scope=CUSTOMER"
```
Use `scope=ADMIN` for others such as POST, PUT etc..

```shell
curl -X POST client:secret@localhost:9090/oauth2/token -d "grant_type=client_credentials" -d "scope=ADMIN"
```

Authorization server should return a JSON response with an access token:

```json
{
"access_token": "eyJraWQiOiIxNDY0NmYzYi0wZGU1LTQ1MDYtYTdjZi0zNWYxYWU0ZjU5MjIiLCJhbGciOiJSUzI1NiJ9...",
"token_type": "Bearer",
"expires_in": 299,
"scope": "message:ADMIN"
}
```

## Step 3: Send an HTTP Request

Now that you have the access token, you can use it to send a http request. HTTP POST to Register a User to the `/v1/users` endpoint:

```shell
curl --location 'localhost:8080/v1/create-loan' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer <access_token>' \
--data '{
    "customerId": 1,
    "loanAmount": 100000,
    "interestRate": 0.2,
    "installments": 12
}'
```
# Test Endpoints via Postman

Import `Loan API.postman_collection.json` collection to Postman. All endpoints including JWT token generation are represented with sample data.

Adjust the scope (ADMIN or CUSTOMER), port and other configurations if you need.



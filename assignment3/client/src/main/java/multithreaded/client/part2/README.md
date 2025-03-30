# Client Setup Guide
## URL Setting

To change the base URL, update the following line in  
`src/main/java/io/swagger/client/ApiClient.java`:

```java
private String basePath = "http://35.88.148.64:8080/skiers-servlet-1.0-SNAPSHOT";
```

Modify the value of `basePath` as needed to match your server's address.

---

## Index Settings

In `Main.java`, you can adjust key parameters related to request handling:

```java
private static final int TOTAL_REQUESTS = 200000;
private static final int INITIAL_THREADS = 32;
private static final int INITIAL_REQUESTS_PER_THREAD = 1000;
private static final int SUBSEQUENT_THREADS = 168;
```

- **TOTAL_REQUESTS**: Total number of requests the client will send.
- **INITIAL_THREADS**: Number of initial threads used for processing.
- **INITIAL_REQUESTS_PER_THREAD**: Number of requests each thread handles initially.
- **SUBSEQUENT_THREADS**: Number of threads used after the initial phase.

---

## Running the Client

To execute the client, simply run the `Main.java` class:

### **For Windows (CMD or PowerShell)**
```cmd
# Compile the Java file
javac -d out src/main/java/multithreaded/client/part2/Main.java

# Run the client
java -cp out multithreaded.client.part2.Main
```

Ensure that server is running and accessible at the configured URL before starting the client.

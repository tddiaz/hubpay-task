# Added Assumptions
1. Deposit and withdrawing of funds via a Bank Transfer.
2. A callback API is provided for the custody bank (where the customer funds are held) to call and notify the status of the deposit/withdrawal requests whether it's a success or fail. For the purpose of testing, the callback API should be called manually to complete the flow.
3. Reversal notification from the bank is out of scope.
4. Added an `on-hold-balance` for the wallet when a withdrawal request is pending and will be reset in case of successful status from callback, else will be deposited back when settling the transaction.
5. A wallet can only support one currency.
6. The only supported currency is GBP.
7. Additional currency to be supported can be added via configuration.
8. Deposit and Withdrawal limits are configurable.

# Technologies Used
- Java 17
- Spring Boot
- Maven
- Hibernate
- Flyway
- Postgres DB
- Docker
- [TSID Creator](https://github.com/f4b6a3/tsid-creator) - for generating unique identifiers
- [Spring Boot Error Handling Library](https://wimdeblauwe.github.io/error-handling-spring-boot-starter/4.1.0/) - autoconfigures controller advice and standardize error response body.
- RestAssured - for API tests
- TestContainers - embedded Postgres DB for integration test

# Run and Build

## Build the Project
This command will build the wallet-service without running the test
```
make build
```

With tests (Unit and Integration Tests)
```
make build-with-tests
```
*Note*: The tests includes integration tests running iwth embedded PostgresDB using container, so make sure docker is running.

### Test Container Issues
In Mac OS, If you encounter an error saying 'docker environment is not available' even though docker is running,
execute this command
```
sudo ln -s $HOME/.docker/run/docker.sock /var/run/docker.sock
```
In case you are running in different OS. see [https://www.testcontainers.org/supported_docker_environment/](https://www.testcontainers.org/supported_docker_environment/)

## Run the Project
### Run in IntelliJ

1. Setup your local DB
- Run local DB

*Note*: Make sure your docker is running before executing the command.
```
make run-local-db
```
- To stop
```
make stop-local-db
```
- Connect to DB
![connect-db](.images/db-connect.png)

2. Start `Application.java` in `wallet-service` module

### Run with Docker
1. Build the wallet-service docker image

*Note*: Make sure your docker is running before executing the command.
```
make build-image
```

verify that the wallet-service image is created, run:
```
docker images
```

# Implementation Details

## High Level Flow

### Deposit

### Withdraw

## Idempotency
A `referenceId` field is provided for deposit/withdrawal API request. The `referenceId` value is assumed as the reference number returned from the bank transfer integration.

If an existing transaction in DB has the same `referenceId` from the deposit/withdraw request, the request will be ignored and the API will return the existing details of the transaction.

## Concurrency Handling

To achieve concurrent update of the wallet balance, a DB row lock is implemented using pessimistic write lock.

```
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id AND w.customerId = :customerId")
    Optional<Wallet> findByIdAndCustomerIdWithExclusiveLock(@Param("id") Long walletId, @Param("customerId") Long customerId);
```

## APIs
Postman collections is provided for import. Locate the file under `.postman` folder.

### Wallet API

#### Deposit Order
##### Endpoint
##### Sample Request
##### Sample Response

#### Withdraw Order
#### Deposit Order
##### Endpoint
##### Sample Request
##### Sample Response

### Transaction API
#### Get Transactions
##### Endpoint
##### Sample Request
##### Sample Response

### Bank Callback API
#### Notify Transfer Status
##### Endpoint
##### Sample Request
##### Sample Response

# Testing

## Manual Testing
A test data is automatically created in DB when starting the application. To check the params needed for testing, search for similar key words in the logs.

ex: `TEST DATA`

![test-data](.images/test-data.png)

## Tests Coverage
![test-coverage](.images/test-coverage.png)

## Concurrency Tests

### Deposit Flow
[DepositFlowConcurrencyIT]()

### Withdraw Flow
[WithdrawFlowConcurrencyIT]()
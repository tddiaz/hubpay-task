run-local-db:
	docker-compose -f .local-db/docker-compose.yml up -d

stop-local-db:
	docker-compose -f .local-db/docker-compose.yml down

build:
	./mvnw -B -f wallet-service/pom.xml clean install -DskipTests

build-with-tests:
	./mvnw -B -f wallet-service/pom.xml clean install

build-image:
	./mvnw -B -f wallet-service spring-boot:build-image -DskipTests

apiVersion: v1
kind: ConfigMap
metadata:
  name: oficina-config
  namespace: oficina
  labels:
    app.kubernetes.io/name: oficina-api
data:
  SPRING_PROFILES_ACTIVE: "docker"
  SPRING_DATASOURCE_URL: "jdbc:postgresql://__RDS_ENDPOINT__:5432/oficina"
  SERVER_PORT: "8080"
  SECURITY_JWT_EXPIRATION: "7200000"
  SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE: "2"
  SPRING_FLYWAY_ENABLED: "true"
  SPRING_FLYWAY_LOCATIONS: "classpath:db/migration"

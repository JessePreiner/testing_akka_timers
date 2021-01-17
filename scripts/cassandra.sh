docker network create cassandra_network
docker run --name cassandra -d -p 9042:9042 --network=cassandra_network bitnami/cassandra
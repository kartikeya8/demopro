FROM mysql:latest

# Set root password (optional)
ENV MYSQL_ROOT_PASSWORD=admin@123

# Copy initialization script
COPY init.sql /docker-entrypoint-initdb.d/

EXPOSE 3306
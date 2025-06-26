FROM mysql:latest

# Set root password (optional)
ENV MYSQL_ROOT_PASSWORD rootpassword

# Copy initialization script
COPY init.sql /docker-entrypoint-initdb.d/

EXPOSE 3306
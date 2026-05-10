CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255),
    role VARCHAR(255) NOT NULL
);

CREATE TABLE drivers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    license_number VARCHAR(255) NOT NULL UNIQUE,
    status VARCHAR(255) NOT NULL,
    CONSTRAINT fk_drivers_user
        FOREIGN KEY (user_id)
        REFERENCES users (id)
);

CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    driver_id BIGINT NOT NULL UNIQUE,
    brand VARCHAR(255),
    model VARCHAR(255),
    color VARCHAR(255),
    plate_number VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT fk_vehicles_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers (id)
);

CREATE TABLE rides (
    id BIGSERIAL PRIMARY KEY,
    rider_id BIGINT NOT NULL,
    driver_id BIGINT,
    vehicle_id BIGINT,
    pickup_latitude DOUBLE PRECISION NOT NULL,
    pickup_longitude DOUBLE PRECISION NOT NULL,
    dropoff_latitude DOUBLE PRECISION NOT NULL,
    dropoff_longitude DOUBLE PRECISION NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    CONSTRAINT fk_rides_rider
        FOREIGN KEY (rider_id)
        REFERENCES users (id),
    CONSTRAINT fk_rides_driver
        FOREIGN KEY (driver_id)
        REFERENCES drivers (id),
    CONSTRAINT fk_rides_vehicle
        FOREIGN KEY (vehicle_id)
        REFERENCES vehicles (id)
);

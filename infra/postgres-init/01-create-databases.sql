-- Each service owns its own database (the "one database per service" microservices
-- rule) even though, for simplicity, they all live in one shared Postgres container.
-- In a real deployment each of these would typically be its own separate instance.
CREATE DATABASE catalog_db OWNER gearshare;
CREATE DATABASE approval_db OWNER gearshare;
CREATE DATABASE payment_db OWNER gearshare;
CREATE DATABASE handover_db OWNER gearshare;
CREATE DATABASE booking_db OWNER gearshare;
CREATE DATABASE orchestrator_db OWNER gearshare;

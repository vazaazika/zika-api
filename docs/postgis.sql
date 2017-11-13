psql -d yourdatabase -c "CREATE EXTENSION postgis;"
psql -d yourdatabase -c "CREATE EXTENSION postgis_topology;"
psql -d yourdatabase -c "CREATE EXTENSION postgis_tiger_geocoder;"
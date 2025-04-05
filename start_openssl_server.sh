#!/bin/bash

# Create a self-signed certificate if it doesn't exist
if [ ! -f "server.key" ] || [ ! -f "server.crt" ]; then
  echo "Generating self-signed certificate..."
  openssl req -x509 -newkey rsa:2048 -keyout server.key -out server.crt -days 365 -nodes -subj "/CN=localhost" -addext "subjectAltName = DNS:localhost"
  cat server.key server.crt > server.pem
fi

# Define the port to listen on
PORT=4433

# Set environment variable for key logging
export SSLKEYLOGFILE="$(pwd)/ssl_keylog.txt"

echo "Starting OpenSSL server on port $PORT..."
echo "Key log file: $SSLKEYLOGFILE"

# Start the server with essential debugging options
openssl s_server \
  -cert server.crt \
  -key server.key \
  -accept $PORT \
  -www \
  -tls1_3 \
  -keylogfile "$SSLKEYLOGFILE" \
  -ciphersuites TLS_AES_128_GCM_SHA256 \
  -msg \
  -debug \
  -state

# Note: Ctrl+C to stop the server
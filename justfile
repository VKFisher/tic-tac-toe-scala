_default:
  @ just --list --unsorted 

# Checks if any of the ports required by the app are in use
check-ports:
  #!/bin/sh
  set -eu
  required_ports="8080,8000,8001,6650,8090"
  ports_in_use=$(lsof +c0 -sTCP:LISTEN -nPi :$required_ports || true)
  if [ -z "$ports_in_use" ]; 
  then echo "✅ All ports required by this app ($required_ports) are available"
  else echo "❌ some required ports are occupied:\n\n$ports_in_use"
  fi

#!/usr/bin/env bash
# Creates, if necessary, and starts a MongoDB Docker container.

# List containers with vrem-container in the name.
containers=$(docker ps -al | grep "vrem-container")

if [ -z "$containers" ]; then
    # Variable empty: Run new container.
	echo "No vrem-container found, creating new one."
	docker run --name vrem-container -d -p 27017:27017 mongo
	echo "Started new container."
else
    # Container exists.
    if [[ $containers == *Exited* ]]; then
        # Stopped, restart.
		echo "Existing container was stopped. Restarting."
		docker start "vrem-container"
	    echo "Restarted container."
	else
	    # Already running, do nothing.
		echo "vrem-container already running."
	fi
fi


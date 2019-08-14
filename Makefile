docker-clean:
	@echo deleting containers
	-docker rm `docker ps -q -f status=exited`
	@echo "Delete all untagged/dangling (<none>) images"
	-docker rmi `docker images -q -f dangling=true`


DOCKER_STOP=docker-compose --file docker-compose.yml down

dockerRun: ## Run MA in docker
	@echo starting container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&
	docker-compose --file docker-compose.yml up --force-recreate -d --build cassandra1
	## start controller
	docker-compose --file docker-compose.yml up --force-recreate -d --build controller
	## wait until it installs controller and ES
	sleep 600
	## start machine agent
	docker-compose --verbose --file docker-compose.yml up --force-recreate -d --build  -t 120 machine
	@echo started container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&

dockerStop:
	## stop and remove all containers
	sleep 60
	@echo remove containers and images
    docker-compose down --rmi all -v

sleep:
	@echo Waiting for 5 minutes to read the metrics
	sleep 300
	@echo Wait finished
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
	docker stop machine controller cassandra1
	docker rm machine controller cassandra1
	docker rmi dtr.corp.appdynamics.com/appdynamics/machine-agent:latest
	docker rmi dtr.corp.appdynamics.com/appdynamics/enterprise-console:latest
    docker rmi $(docker images -a |  grep "machine")
    docker rmi cassandra:3.11.2
	@echo remove containers and images
	## always remove all unused networks, will cause a leak otherwise. use --force when running on TC
	docker network prune --force

sleep:
	@echo Waiting for 5 minutes to read the metrics
	sleep 300
	@echo Wait finished
# docker-clean:
# 	@echo deleting containers
# 	-docker rm `docker ps -q -f status=exited`
# 	@echo "Delete all untagged/dangling (<none>) images"
# 	-docker rmi `docker images -q -f dangling=true`
#
#
# DOCKER_STOP=docker-compose --file docker-compose.yml down
#
# dockerRun: ## Run MA in docker
# 	@echo starting container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&
# 	docker-compose --file docker-compose.yml up --force-recreate -d --build cassandra1
# 	## start controller
# 	docker-compose --file docker-compose.yml up --force-recreate -d --build controller
# 	## wait until it installs controller and ES
# 	sleep 600
# 	## start machine agent
# 	docker-compose --verbose --file docker-compose.yml up --force-recreate -d --build  -t 120 machine
# 	@echo started container ##################%%%%%%%%%%%%%%%%%%%&&&&&&&&&&&&&&&&&&&&&&
#
# dockerStop:
# 	## stop and remove all containers
# 	sleep 60
# 	@echo remove containers and images
# 	docker-compose down --rmi all -v
#
# sleep:
# 	@echo Waiting for 5 minutes to read the metrics
# 	sleep 300
# 	@echo Wait finished


##Makefile Setup
dockerRun: ##Spin up docker containers for MA with extension, controller and other apps
	@echo "------- Starting controller -------"
	docker-compose up -d --force-recreate controller
#wait until controller and ES installation completes
	sleep 600
	@echo "------- Controller started -------"
#bash into the controller, change props to enable port 9200
#	docker exec controller /bin/bash -c "sed -i s/ad.es.node.http.enabled=false/ad.es.node.http.enabled=true/g events-service/processor/conf/events-service-api-store.properties"
#restart ES to make the changes reflect
#	docker exec controller /bin/bash -c "pa/platform-admin/bin/platform-admin.sh submit-job --platform-name AppDynamicsPlatform --service events-service --job restart-cluster"
#	sleep 60

#start machine agent
	@echo ------- Starting machine agent -------
	docker-compose up --force-recreate -d --build machine
	@echo ------- Machine agent started -------

dockerStop: ##Stop and remove all containers
	@echo ------- Stop and remove containers, images, networks and volumes -------
	docker-compose down --rmi all -v --remove-orphans
	docker rmi dtr.corp.appdynamics.com/appdynamics/machine-agent:latest
	docker rmi alpine
	@echo ------- Done -------

sleep: ##sleep for x seconds
	@echo Waiting for 5 minutes to read the metrics
	sleep 300
	@echo Wait finished

workbenchTest: ##test workbench mode
	@echo "Creating docker container for workbench"
	docker build -t 'workbench:latest' --no-cache -f Dockerfile_WorkBench .
	docker run --name workbench -d workbench
	@echo "Done"
# wait 60 seconds for workbench to report metrics
	sleep 60
	@echo "Checking /api/metric-paths"
	@out=$$(docker exec workbench /bin/sh -c "curl -s -w '\n%{http_code}\n' localhost:9090/api/metric-paths"); \
	printf "*****/api/metric-path returned*****\n%s\n**********\n" "$$out"; \
	code=$$(echo "$$out"|tail -1); \
	[ "$$code" = "200" ] || { echo "Failure: code=$$code"; exit 1; }; \
	[ "$$(echo "$$out"|grep ".*Heart Beat.*")" = "Custom Metrics|Cassandra Monitor|Local Cassandra Server 1|Heart Beat" ] || { echo "Hear Beat metric not found"; exit 1; }
	@echo "Workbench Tested successfully"
	@echo "Stopping docker container workbench"
	docker stop workbench
	docker rm workbench
	docker rmi dtr.corp.appdynamics.com/appdynamics/machine-agent:latest
	docker rmi alpine

dockerClean: ##Clean any left over containers, images, networks and volumes
	@if [[ -n "`docker ps -q`" ]]; then \
	docker stop `docker ps -q`; \
	fi
	docker rm -f `docker ps -a -q` || echo 0
	docker system prune -f -a --volumes
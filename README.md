# DAI_HTTP_infra
## STEP 0: GitHub Repository
Creation of the new git repository from the github website. Added ReadME and .gitignore files.

## Step 1: Static Website
Downloaded template (one page) from Start Bootstrap.
Pull the official nginx:latest image hosted on the Docker Hub and run it in a container. 
This has been made with the command: docker run -it --rm -d -p 8080:80 --name web nginx.
After running the container as a daemon (-d) and published it on port 8080 on the host network, opening a browser and navigating to http://localhost:8080, I can see the NGINX welcome page.
![nginx_welcome.png](nginx_welcome.png).
I stop the container.

To make a custom website, I added the files of the downloaded template in a directory named static-site-content/static-sire-files.

To build a custom image, I created a Dockerfile inside the static_site_content directory.
My custom image is built on top of the nginx image (FROM nginx:latest). 
I added a line to copy my static files in the right place (the default directory to serve static files) on the nginx server:
COPY ./static_site_files /usr/share/nginx/html
I didn't add any ENTRYPOINT neither CMD, since they are provided by the base NGINX image.
I also added the line EXPOSE 8080 to inform Docker that the container will listen on the port 8080 at runtime.
I built my custom image with the command: docker build -t webserver .
(the point is important to specify the directory!)
After running the newly created image in a container at port 8080, my static website is being served correctly.
Command: docker run -it --rm -d -p 8080:80 --name custom_nginx webserver
Then go to localhost:8080

This works because nginx has a default configuration, that is already enough.
Since I want to have a custom configuration, I enter the bash of the nginx service running on Docker and look for the default configuration (that's already working) to copy it in my directory.
To enter the bash:
docker exec -it <id-image-with-nginx> /bin/bash

Then the configuration that I need to copy can be found under /etc/nginx/conf.d/. Its name is default.conf.
This configuration is referenced by the main nginx.conf configuration file, under /etc/nginx/.

So I created a custom configuration file copying the content of the default.conf file, with a basic Nginx configuration.
In this file, that I simply named nginx.conf, I configure nginx to listen on port 80 and to serve the static files from the default /usr/share/nginx/html directory, where I copied the files thanks to my Dockerfile.

To copy my nginx configuration file in the default nginx configuration folder, I added to the Dockerfile the line:
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

I deleted and built my custom image again with docker build -t webserver .
And now again, my static website is being served correctly.

## Step 2: Docker compose
I added a docker-compose.yaml file in the same directory of my Dockerfile (my project directory).
It contains a simple configuration for one single service, called web_app.

In this configuration, I specify the container name, the image name and the Dockerfile to rebuild the image.
Thanks to the build command, whenever we run docker-compose built, the image of the server will be recreated based on the specified Dockerfile.

Now I can start and stop my simple infrastructure with a single static web server using docker compose, I can access the web server on my local machine on the port 8080 and rebuild the docker image with the docker compose build command.

## Step 3: HTTP API server
I decided to build an API to store pizzas with their ingredients. I store data directly in memory.

With this API, it's possible to:
- read all the pizzas that are saved in memory (GET on /api/pizzas)
- read one pizza given its id (GET on /api/pizzas/{id}}
- create a new one (POST on /api/pizzas, with a JSON body for the pizza to create) 
- update an existing one (PUT on /api/pizzas/{id}, with a JSON body to replace the original pizza)
- delete an existing one (DELETE on /api/pizzas/{id})

To test my API I used Bruno.

I create the jar file with all dependencies of my application using maven (mvn clean and mvn package).

At this point, I create the Dockerfile for this APIservice.
My Pizza API service will run on port 7000. 
I build the new image with the command: docker build -t pizza_api .
I run a container based on the image to check that everything works fine: docker run -p 7000:7000 --name pizza_api_service pizza_api
Since this works well, I add this service to my docker-compose file.

## Step 4: Reverse proxy with Traefik
I added a new service called "reverse_proxy" to my docker compose file, using the Traefik official docker image.
After reading the documentation, I changed the docker-compose file to add the traefik configuration.
The final docker-compose file has a reverse_proxy service with the following parameters:

image: "traefik:v2.10" -> the official traefik image
container_name: "traefik" -> the name I want to give to my container
command:
- "--api.insecure=true" -> to be able to expose other ports rather than only port 80
- "--providers.docker=true" -> to set docker as provider
- "--providers.docker.exposedbydefault=false" -> to avoid to expose all services by default
- "--entrypoints.web_app.address=:5000" -> to set the port to access the web_app entrypoint, which is for the static website
- "--entrypoints.pizza_api.address=:7000" -> to set the port to access the pizza_api entrypoint
ports: -> to set the open ports for the different services
- "5000:5000"
- "7000:7000"
- "8080:8080" -> this is the port we need to open to see the traefik dashboard
volumes:
- "/var/run/docker.sock:/var/run/docker.sock:ro"

I also needed to add some extra-parameters to each of the other services:
- instead of "ports:..." now there is "expose:...", because the ports are already opened by the traefix service.
- a new section "labels" is added with the following content:
  - "traefik.enable=true" -> to enable trafik for the service
  - "traefik.http.routers.<route_name>.rule=Host(`localhost`)" -> the domain the service responds to
  - "traefik.http.routers.<route_name>.entrypoints=<entrypoint_name>" -> the entrypoint for the service (web_app or pizza_api, as specified in the reverse_proxy service)
  - "traefik.http.services.<service_name>.loadbalancer.server.port=<server_port>" -> the port the server listens to for the service (80 for nginx, 7000 for the api)

Now, we can run docker compose up again to create the new infrastructure from scratch.

To access the dashboard of Traefik, since we set --api.insecure=true in the docker compose file for its service, we can simply visit localhost:8080/dashboard/#/. 
There we can see much useful information:
- our defined entrypoints, with their respective ports
- the current active routes handled by Traefik for HTTP (the only one in our case), TCP, UDP
- which other features are set (like tracing or access logs)
- the providers we set

A reverse proxy is very useful, not only to improve performances (with load balancing, sending requests to different servers), but also to improve the security of the infrastructure.
Indeed, by intercepting requests, the reverse proxy protects the web server’s identity: a website or service never needs to reveal the IP address of their origin server(s).
In this way, the reverse proxy acts as an additional defense against security attacks: the attackers will only be able to target the reverse proxy, which will have tighter security and more resources to fend off a cyberattack.

## Step 5: Scalability and load balancing
To start several instances of a server inside the docker-compose file, it's very simple. We need to:
1) add deploy options with mode "replicated" and a fixed number of replicas
2) remove the container name for that server, so that docker can create multiple instances giving them different names

So, since we need to start several instances of our two servers, we repeat these steps twice.
For both the webserver and the pizza_api service I want to add 5 replicas, so I add:
deploy:
  mode: replicated
  replicas: 5

Then I remove the container-name I had specified (I commented the lines).
This is enough to start multiple instances when we run the command docker-compose.

We can check that there are 5 replicas running in the dashboard of Traefik, under HTTP Services.
Here we can see that the number of servers running is 5 for both the static web app and the api.

Since I also want to be able to scale up and down (add/remove instances) my services, this is not enough.
Indeed, to achieve this goal, I need an orchestrator. A simple one, already linked with Docker, is Docker Swarm.
To deploy my services using Docker Swarm, I initialise docker swarm, running the command:

docker swarm init

After, I need to choose a name for my stack (I choose dai-lab-infra) and to run the following:

docker stack deploy --compose-file docker-compose.yml dai-lab-infra

This command will start, like before, the 5 replicas for each server that I demanded.
But now, if I want to change the number of replicas, I can just use the scale function (for one service or both of them).

To scale both the services: docker service scale dai-lab-infra_web_app=4 dai-lab-infra_pizza_api=6

To stop and delete a container, we just need to specify the number of replicas equals to 0 or we can run:

docker service rm <service_name>

Note: to stop or delete the services through Docker Desktop will NOT work: once we exit one service, another one will be created and run to replace it.

## Step 6: Load balancing with round-robin and sticky sessions
Since so far we used round-robin to distribute the load among the available servers, every time we connect to a server, the information of the previous session is lost.
This is what we refer to as stateless.
For my API, I would like the information of a past session (like a pizza that I created) to be available when I reconnect.
This is possible if I also send the cookie previously sent to my client, to make the server recognize where the request come from.
To do so, we can use sticky-sessions, which enable my API service to be stateful, so to "keep memory" of a previous state by asking the load balancer to direct my requests always to the same server.

To demonstrate this, I connect to my pizza_api using Bruno and I create a new pizza.
If then I try to get the newly created pizza, sometimes the server will return it, but most of the time it won't, since the new pizza is saved in the memory of a single server.

Now I add the following lines to the Traefik section in my pizza_api service inside docker compose:
- "traefik.http.services.pizza_api.loadbalancer.sticky.cookie=true"
- "traefik.http.services.pizza_api.loadBalancer.sticky.cookie.name=sticky_pizza"

I stop and rerun my services (as explained in previous step).
I retry to create a new pizza with Bruno.
Now, every time I try to run a GET on the newly created pizza, the server returns it.

How to prove that that my load balancer can distribute HTTP requests in a round-robin fashion to the static server nodes?

## Step 7: Securing Traefik with HTTPS
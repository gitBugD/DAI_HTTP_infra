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

I also needed to add some extra-parameters to each of he other services:
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
## Step 6: Load balancing with round-robin and sticky sessions
## Step 7: Securing Traefik with HTTPS
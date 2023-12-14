# DAI_HTTP_infra
STEP 0: GitHub Repository
Creation of the new git repository from the github website. Added ReadME and .gitignore files.

Step 1: Static Website
Downloaded template (one page) from Start Bootstrap.
Pull the official nginx:latest image hosted on the Docker Hub and run it in a container. 
This has been made with the command: docker run -it --rm -d -p 8080:80 --name web nginx.
After running the container as a daemon (-d) and published it on port 8080 on the host network, opening a browser and navigating to http://localhost:8080, I can see the NGINX welcome page.
![nginx_welcome.png](nginx_welcome.png).
I stop the container.

To make a custom website, I added the files of the downloaded template in a directory named static-site-content.

To build a custom image, I created a Dockerfile.
My custom image is built on top of the nginx image (FROM nginx:latest). 
I added a line to copy my static files in the right place (the default directory to serve static files) on the nginx server:
COPY ./static_site_content /usr/share/nginx/html
I didn't add any ENTRYPOINT neither CMD, since they are provided by the base NGINX image.
I also added the line EXPOSE 8080 to inform Docker that the container will listen on the port 8080 at runtime.
I built my custom image with the command: docker build -t webserver .
(the point is important to specify the directory!)
After running the newly created image in a container at port 8080, my static website is being served correctly.
Command: docker run -it --rm -d -p 8080:80 --name test static-web-nginx
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

https://www.docker.com/blog/how-to-use-the-official-nginx-docker-image/
https://sabe.io/tutorials/serve-static-files-nginx-docker#nginx-configuration

Step 2: Docker compose
Step 3: HTTP API server
Step 4: Reverse proxy with Traefik
Step 5: Scalability and load balancing
Step 6: Load balancing with round-robin and sticky sessions
Step 7: Securing Traefik with HTTPS
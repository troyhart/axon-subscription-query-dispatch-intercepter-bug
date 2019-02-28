# Sample project that demonstrates an issue with Axon Subscription Queries

The initial result part of the subscription query doesn't trigger the dispatch intercepter and this causes issues for
our application as we handle authentication/authorization via the dispatch and handler intercepters. In the dispatch
interceper we pull the authenticated principal from the security context that has been established on the dispatch
thread. We include that principal in the message metadata and then in the handler intercepter we lookup the principal
from the metadata and when not present we throw a SecurityException.

Making matters worse, when I invoke a subscription query, and the handler intercepter raises a SecurityException, it is
swallowed and the only way I can see it is by putting a break piont in the handler interceptor.

This sure seems like a bug, unless there is a different way to configure the subscription query intercepter, which I
can't find any documentation for.

## Usage

This project has some infrastructure requirements that are configurred and made available via docker compose. This
infrastructure include a mongo database and the axon server. 

You will need to bring the infrastructure up with:

```
$ docker-compose up
```

Then, you will need to bring up an editor that supports spring boot. I use Spring STS 4, but you can use whatever you
use. For me, I just launch the application with the `Boot Dashboard`. 

Next you will point your browser to the [swagger API documentation](http://localhost:8080/swagger-ui.html)

In order to execute the subscription query, you need a client that is capable of dealing with the JSON stream. Curl
works great for this. Below is a sample curl command that will subscribe to a model with id `c8b162dc-87a9-45b8-ab14-b24f98a08349`:

```
curl --request GET \
  --url http://localhost:8080/packages/c8b162dc-87a9-45b8-ab14-b24f98a08349/subscription
```

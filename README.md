# Simple chat server

This is a simple chat server built as requested as a demo for Signal. The exercise prompt contains several points that strongly informed the technology choices in this project. In particular:

> - Implement a non-blocking HTTP server using Core Java only that listens on port 80.
> - The server must be able to handle multiple concurrent requests.

Although [Netty](https://netty.io/) would usually be my first choice for low-level, non-blocking networking projects and [Micronaut](https://micronaut.io/) or [Spring Boot](https://spring.io/projects/spring-boot) would be my first choices for general-purpose REST servers, the "core Java only" requirement eliminates all of those options. I also interpreted "core Java" as excluding Java EE. Within the core JDK, our options are, broadly, the `java.io` and `java.nio` packages. The former only really supports blocking I/O, which pushes us into `java.nio`.

Within `java.nio`, I opted to build on the `Asynchronous...` classes rather than manage `Selector` lifecycles and thread dispatch explicitly. Because (for this exercise, at least) all data (contact lists, chats, and messages) is memory-resident, all "business" operations are non-blocking, and request handling time will be dominated by network I/O while reading requests and sending responses. Using asynchronous I/O scratches the "non-blocking" itch and is generally a good fit for this problem space; it allows us to use a small number of threads that will make the most of the available CPU time with minimal memory overhead and resource contention/context switching.

The project is organized into several components:

- The `http` package contains an asynchronous `HttpServer`. The `HttpServer` (in conjunction with several supporting components) reads HTTP requests from incoming connections, parses them, and delegates the requests to appropriate `Controller` instances.
- The `controller` package contains web controllers that handle requests and delegate work to various back-end services, especially `ChatService`.
- The `contacts` package contains a service for managing users' contact lists. For this project, contact lists are loaded from `contacts.json` and are memory-resident after the initial load.
- The `chat` package contains a service that manages chats and messages. For this project, chats and messages are memory-resident.

## Running the server

To start the server using Maven, run:

```sh
mvn exec:java
```

A trivial testing script, `chat.sh`, provides a quick and easy way to see the basic functions of the server at work.

## Next steps and known issues

Because this project is intended as a time-limited demo rather than a production service, lots of corners got cut, but the core functionality should all be there. If this project were to continue with the same constraints, some areas I'd like to address include:

- Persistent storage of chats/messages/contact lists
- Less string manipulation (HTTP request parsing is currently a String-y GC mess)
- Caching
- Make `HttpRequest` more sophisticated (handle queries as distinct entities from paths, for example)
- Defend against large or maliciously-crafted requests (Tomcat, for example, was infamously vulnerable to a header/parameter hashing attack)
- Meaningful read/write timeouts

I've also discovered a really gnarly issue where, after `16,384 - T` (where `T` is the number of IO threads in the server) requests, the server just… stops. There are (to the best of my ability to determine) no exceptions getting thrown, and we're not exhausing open file descriptors (which was my first guess). What's really bizarre is that the problem seems to be solveable with garbage collection. For example, the second `ab` run in the following example will stall out:

```sh
$ ab -c1 -n10000 http://localhost/chats?userId=1
$ ab -c1 -n10000 http://localhost/chats?userId=1
```

…but if the server is monitored in (for example) [VisualVM](https://visualvm.github.io/) and a manual GC cycle is triggered:

```sh
$ ab -c1 -n10000 http://localhost/chats?userId=1
# Manually trigger a GC cycle here
$ ab -c1 -n10000 http://localhost/chats?userId=1
```

…then the second `ab` run will complete as expected. After checking a number of likely suspects, I've concluded that whatever is going on here is either really subtle or really embarrassing. Either way, I've left it out of scope for now.

# Notes on time

Getting to this point probably took me closer to ten hours than four or six. For an accurate sense of where I wound up in the four-to-six hour time frame, plese see [a9d26c7](https://github.com/jchambers/signal-chat-project/commit/a9d26c735fb71f379f5dce0bc30fcb0559f624b6).
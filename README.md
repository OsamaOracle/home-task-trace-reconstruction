# home-task-trace-reconstruction
Trace reconstruction from logs


Each application in a microservice environment outputs some log describing the boundaries of an HTTP request, with the following format:

[start-timestamp] [end-timestamp] [trace] [service-name] [caller-span]->[span]

The trace ID is a random string that is passed along every service interaction. The first service (called from outside) generates the string and passes it to every other service it calls during the execution of the request. The called services take the trace (let’s say, from an HTTP header) and also pass it to the services the call themselves.

The span ID is generated for every request. When a service calls another, it passes its own span ID to the callee. The callee will generate its own span ID, and using the span passed by the caller, log the last part of the line, that allows to connect the requests.

So, a trace could look like this:
````bash
2016-10-20 12:43:34.000 2016-10-20 12:43:35.000 trace1 back-end-3 ac->ad
2016-10-20 12:43:33.000 2016-10-20 12:43:36.000 trace1 back-end-1 aa->ac
2016-10-20 12:43:38.000 2016-10-20 12:43:40.000 trace1 back-end-2 aa->ab
2016-10-20 12:43:32.000 2016-10-20 12:43:42.000 trace1 front-end null->aa


This execution trace can then be represented as:

```json
{
  "id": "trace1",
  "root": {
    "service": "front-end",
    "start": "2016-10-20 12:43:32.000",
    "end": "2016-10-20 12:43:42.000",
    "calls": [
      {
        "service": "back-end-1",
        "start": "2016-10-20 12:43:33.000",
        "end": "2016-10-20 12:43:36.000",
        "calls": [
          {
            "service": "back-end-3",
            "start": "2016-10-20 12:43:34.000",
            "end": "2016-10-20 12:43:35.000",
            "calls": []
          }
        ]
      },
      {
        "service": "back-end-2",
        "start": "2016-10-20 12:43:38.000",
        "end": "2016-10-20 12:43:40.000",
        "calls": []
      }
    ]
  }
}

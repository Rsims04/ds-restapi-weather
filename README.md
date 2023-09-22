# ds-restapi-weather

A system that aggregates and distributes weather data in JSON format using a RESTful API.

## Build

To build all files, type: `make`.<br />

## Start the Aggregation Server

(Still in production) To start the Aggregation server, use: `java -cp dest build.AggregationServer`.<br />
Aggregation Server takes one parameter:<br />
i.e., use `java -cp dest build.AggregationServer [port number]` <br />
This will start the server on the specified port. (default port is 4567).<br/>

## Run a Content Server

(Still in production) To run a Content Server, use: `java -cp dest build.ContentServer [server name and port number] [Input File]` <br/>
Content Server will get input from a specified `File` (weather station), <br/>
File must be in the format:<br/>

```
id:IDS60901
name:Adelaide (West Terrace /  ngayirdapira)
state: SA
time_zone:CST
lat:-34.9
lon:138.6
local_date_time:15/04:00pm
local_date_time_full:20230715160000
air_temp:13.3
apparent_t:9.5
cloud:Partly cloudy
dewpt:5.7
press:1023.9
rel_hum:60
wind_dir:S
wind_spd_kmh:15
wind_spd_kt:8
```

Multiple entries in this format can be included in a single file with a blank line in between.<br/>

## Run a GET Client

(Still in production) To run a client, use: `java -cp dest build.GETClient [server name and port number] [(optional)stationID]` <br/>
Possible formats for server name and port number include: <br />
`"http://servername.domain.domain:portnumber"`,<br/>
`"http://servername:portnumber"`,<br/>
`"servername:portnumber"`<br/>
i.e.,
`java -cp dest build.GETClient localhost:4567 IDS60901`. <br />
Default servername is `localhost`.<br/>
Default port is `4567`.<br/>

## Local Storage

When the Aggregation Server receives a successful PUT request, it will store the data in a file named `localStorage.txt`. <br />
As a result data will be restored on starting the server again in the event of a crash.<br/>
When the request is successfully sent to local storage a 30 second timer will initiate, after which the entry from that particular Content Server will be removed.<br/>
Any stale existing entries that are older than 30 seconds will also be removed.<br/>

## Lamport Clocks

The system uses a single `Lamport Clock` to synchronise and maintain correct order of requests from multiple Content Servers to the Aggregation Server. <br/>
The Lamport Clock timestamp will be stored in a file named `clock` each time the timestamp is modified. When any entity creates a new `Lamport Clock` it will reference this file to restore and ensure correct time across all requests.<br/>

## Reset

Running `make clean` will delete `build`, `localStorage` and `clock` files essentially resetting the system.

## Testing

(Testing is still in production)
To run all tests, type: `make test`. <br />

---

## Bonus

Custom JSONParser has been implemented.

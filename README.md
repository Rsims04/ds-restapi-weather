# ds-restapi-weather

A system that aggregates and distributes weather data in JSON format using a RESTful API.

## Build

To build all files, type: `make`.<br />

## Start the Aggregation Server

To start the Aggregation server, use: `./AggregationServer`.<br />
This will start the server on default host/port 4567.<br /><br />
Aggregation Server takes one parameter:<br />
i.e., use `./AggregationServer [port number]` <br />
example: `./AggregationServer 8080` <br />
This will start the server on the specified port.<br/>

## Run a Content Server

To run a Content Server, use: `./ContentServer [host name:port number] [Input File]` <br/>
example: `./ContentServer localhost:4567 input.txt` <br/><br/>
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

Multiple entries in this format can be included in a single file with a blank line in between, as below.<br/>

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

id:IDS60902
name:Adelaide (West Terrace /  ngayirdapira)
state: SA
... etc
```

## Run a GET Client

To run a client, use: `./GETClient [(optional)server name:port number] [(optional)stationID]` <br/>
Possible formats for server name and port number include: <br />
`"http://servername.domain.domain:portnumber"`,<br/>
`"http://servername:portnumber"`,<br/>
`"servername:portnumber"`<br/>
i.e.,
`java -cp dest build.GETClient localhost:4567 IDS60901`. <br />
Default servername is `localhost`.<br/>
Default port is `4567`.<br/>
Default stationID will get `/` or everything on the server.

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
To run all Junit Unit Tests, type: `make test`. <br />

### Random Input Generator

I have written a random input generator: `randomInputGenerator.py`.<br/>
To run enter `./randomInputGenerator.py`. <br/>
You will then be prompted to enter:<br/>
entries: How many entries per file.
files: How many files containing the number of entries.<br/><br/>

This is useful for stress testing with large amounts of data and content servers. I also used it to manually analyse the effectiveness of Lamport Clocks to maintain order with a large number of Content Servers running concurrently. Using `./startConcurrentContent`. <br/>

---

## Bonus

Custom JSONParser has been implemented.

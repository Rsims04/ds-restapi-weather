#!/usr/bin/python3

import os;
import random;
import shutil;

# Modify LIMIT here
LIMIT = 100

# How many entries per file
print('How many entries:')
numEntries = int(input());

# How many files to create
print('How many files:')
numFiles = int(input());

dPath = "./randInputs"
dirExists = os.path.exists(dPath);

# Make directory if it doesn't exist
# Otherwise delete existing directory
if dirExists:
    shutil.rmtree(dPath)
os.mkdir(dPath)


stations = {
  "23090":"ADELAIDE (KENT TOWN)",
  "23000":"ADELAIDE (WEST TERRACE / NGAYIRDAPIRA)",
  "23034":"ADELAIDE AIRPORT",
  "23046":"ADELAIDE AIRPORT OLD SITE",
  "22823":"CAPE BORDA",
  "26095":"CAPE JAFFA (THE LIMESTONE)",
  "22803":"CAPE WILLOUGHBY",
  "18012":"CEDUNA AMO",
  "21131":"CLARE HIGH SCHOOL",
  "18116":"CLEVE AERODROME",
  "18230":"COFFIN BAY (POINT AVOID)",
  "16090":"COOBER PEDY AIRPORT",
  "26091":"COONAWARRA",
  "18191":"COULTA (COLES POINT)",
  "18229":"CULTANA (DEFENCE)",
  "18217":"CUMMINS AERO",
  "23083":"EDINBURGH RAAF",
  "22046":"EDITHBURGH",
  "16097":"ERNABELLA (PUKATJA)",
  "23894":"HINDMARSH ISLAND AWS",
  "22050":"KADINA AWS",
  "25557":"KEITH (MUNKORA)",
  "22841":"KINGSCOTE AERO",
  "23887":"KUITPO FOREST RESERVE",
  "25562":"LAMEROO (AUSTIN PLAINS)",
  "17110":"LEIGH CREEK AIRPORT",
  "24024":"LOXTON RESEARCH CENTRE",
  "17126":"MARREE AERO",
  "22031":"MINLATON AERO",
  "18195":"MINNIPA PIRSA",
  "17123":"MOOMBA AIRPORT",
  "23878":"MOUNT CRAWFORD AWS",
  "26021":"MOUNT GAMBIER AERO",
  "23842":"MOUNT LOFTY",
  "24584":"MURRAY BRIDGE (PALLAMANA AERODROME)",
  "26099":"NARACOORTE AERODROME",
  "18115":"NEPTUNE ISLAND",
  "23885":"NOARLUNGA",
  "18192":"NORTH SHIELDS (PORT LINCOLN AWS)",
  "18106":"NULLARBOR",
  "23373":"NURIOOTPA PIRSA",
  "17043":"OODNADATTA AIRPORT",
  "26100":"PADTHAWAY SOUT",
  "23013":"PARAFIELD AIRPORT",
  "23875":"PARAWA (SECOND VALLEY FOREST AWS)",
  "22843":"PARNDANA CFS AWS",
  "18201":"PORT AUGUSTA AERO",
  "16092":"PORT AUGUSTA ARID LANDS",
  "21139":"PORT PIRIE AERODROME AWS",
  "24048":"RENMARK AERO",
  "26105":"ROBE AIRFIELD",
  "23122":"ROSEWORTHY AWS",
  "16096":"ROXBY DOWNS (OLYMPIC DAM AERODROME)",
  "21133":"SNOWTOWN (RAYVILLE PARK)",
  "22049":"STENHOUSE BAY",
  "24580":"STRATHALBYN RACECOURSE",
  "16098":"TARCOOLA AERO",
  "18120":"WHYALLA AERO",
  "16001":"WOOMERA AERODROME",
  "18083":"WUDINNA AERO",
  "20062":"YUNTA AIRSTRIP",
}
clouds = ['Sunny', 'Partly Cloudy', 'Overcast', 'Stormy']
windDirs = ['N','S','E','W']

if (numEntries <= LIMIT or numFiles <= LIMIT):
    # Create files and populate directory
    for i in range(numFiles):
        fPath = dPath + "/rand_" + str(i)
        if (os.path.exists(fPath)):
            os.remove(fPath)

        f = open(fPath,"w")

        for j in range(numEntries):

            station = random.choice(list(stations.items()))
            id = station[0]
            name = station[1]
            
            date = {
                "year":"2023",
                "month":str(random.randint(00,12)),
                "day":str(random.randint(1,31)),
                "hour":str(random.randint(00,24)),
                "minute":str(random.randint(00,59)),
                "second":str(random.randint(00,59)),
            }
            data = {
                "id": "IDS"+id,
                "name": name,
                "state": "SA",
                "time_zone":"CST",
                "lat":str(round(random.uniform(-500,500),1)),
                "lon":str(round(random.uniform(-500,500),1)),
                "local_date_time":date['month']+"/"+date['hour']+":"+date['minute'],
                "local_date_time_full":date['year']+date['month']+date['day']+date['hour']+date['minute']+date['second']+"00",
                "air_temp":str(round(random.uniform(-2,48),1)),
                "apparent_t":str(round(random.uniform(-2,48),1)),
                "cloud":clouds[random.randrange(len(clouds))],
                "dewpt":str(round(random.uniform(-10,10),1)),
                "press":str(round(random.uniform(-1000,1300),1)),
                "rel_hum":str(round(random.uniform(0,100),1)),
                "wind_dir":windDirs[random.randrange(len(windDirs))],
                "wind_spd_kmh":str(round(random.uniform(1,30),1)),
                "wind_spd_kt":str(round(random.uniform(1,30),1)),
            }

            for key, val in data.items():
                f.write(key+":"+val+'\n')
            f.write('\n')

else:
    print("LIMIT (100) exceeded, cannot create files.")
    print("Edit this file to increase the LIMIT.")



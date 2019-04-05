# Overview

This project demonstrates Hazelcast stream processing.   The main user
interface will be a map of Bejing on which is displayed summary information
about the taxis in Bejing.  A stream of location "ticks" will be fed into
a Hazelcast map, summarized using Jet and displayed on the map.  Initially, the
summary information will be a "heat map" showing how many taxis are currently
in each area.  Additional summaries may be added later.  

# Architecture

## Hazelcast Cluster
- 3 nodes, contains an input map with event journal on and , currently running
  on Vagrant
- Contains a Jet job for computing the counts of taxis for each rectangle
  in the map.  
- Each rectangle is another map entry with lat + long as the key and
  the count as the value.  Need to use some sort of atomic entry operator
  (entry processor ?) to safely apply increment / decrement operations.
  Definitely want to use Object serialization on this map and the input
  map.

## Loader
- reads 10k files and plays them into the Hazelcast map

## UI
- Will use leaflet and mapbox.  Will redraw itself every M seconds.

# Feature Guide
- Jet



# To Do

- is the hz map configured correctly ?
- implement a different serialization scheme for locations
- 

# Enhancements

- Drag a rectangle to display current location of cabs in a small area


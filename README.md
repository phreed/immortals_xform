
This project involves converting the json input produced from GME into a suitable clojure structure.

>>> [(feature :a)
>>>   (feature :b)
>>>   (feature :c)
>>>   (feature :d)
>>>   (feature :e)
>>>   (requires :e 1 1 [:b :d])
>>>   (excludes :b [:d])
>>>   (requires :a 2 3 [:b :c :d])
>>>   (resource_limit :cpu 12 {:a 10 :b 4 :e 1})
>>>   (selected [:a])]
>>>

Visit those items whose meta-type is 'Feature'.

Get the guid of the meta-node whose name is 'Feature'.

Find all of the nodes which are decendants of 'Feature', these are the features.
These can be identified in a number of ways.
Following the type.base..base chains up to the 'Feature' node is probably the best.


Visit those items whose meta-type is 'Requires' a connection.
There is a 'Requires' meta-node.

Find all nodes whose ancestor (follow the 'base') is the requires meta-node.
These each will be serialized as a (requires) tuple.
Check the "dst" node for any connected "CardinalitySource" nodes.
The "CardinalitySource" nodes will have associated "Cardinality" nodes 
which have "CardinalityTargets" and an attribute "Type" which carries the actual cardinality.ZZ

The model we are using should look like:

[(:feature :ATAK)
 (:feature :LocationProvider)
 (:feature :WifiLocationProvider)
 (:feature :GPSLocationProvider)
 (:feature :Hardware)
 (:feature :Network)
 (:feature :Wifi)
 (:feature :3G)
 (:feature :LTE)
 (:feature :TacticalRadio)
 (:feature :GeoLocation)
 (:feature :GPS)
 (:feature :ExternalGPS)
 (:feature :Cellular)
 (:feature :Memory)
 (:feature :1GB)
 (:feature :2GB)
 
 (:requires :ATAK 1 N :LocationProvider [:WifiLocationProvider :GPSLocationProvider])
 (:requires :WifiLocationProvider 1 1 :Wifi)
 (:requires :GPSLocationProvider 1 N [:GPS :ExternalGPS])

 (:requires :ATAK 1 N :Hardware)
 (:requires :Hardware 1 N :Network [:Wifi :3G :LTE :TacticalRadio])
 (:requires :Hardware 1 N :GeoLocation [:GPS :ExternalGPS :Cellular])
 (:requires :Hardware 1 1 :Memory [:1GB :2GB]) 

 (:resouce-limit :1GB 1024 [:Memory 1])
 ]



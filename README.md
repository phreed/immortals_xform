
This project involves converting the json input produced from GME into a suitable clojure structure.

> [(feature :a)
>   (feature :b)
>   (feature :c)
>   (feature :d)
>   (feature :e)
>   (requires :e 1 1 [:b :d])
>   (excludes :b [:d])
>   (requires :a 2 3 [:b :c :d])
>   (resource_limit :cpu 12 {:a 10 :b 4 :e 1})
>   (selected [:a])]
>

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

> [(feature :2GB)
>  (feature :Cellular)
>  (feature :Hardware)
>  (feature :GPSLocationProvider)
>  (feature :ATAK)
>  (feature :WifiLocationProvider)
>  (feature :TacticalRadio)
>  (feature :Wifi)
>  (feature :GeoLocation)
>  (feature :ExternalGPS)
>  (feature :GPS)
>  (feature :3G)
>  (feature :LocationProvider)
>  (feature :Memory)
>  (feature :Network)
>  (feature :1GB)
>  (feature :LTE)
>  (requires :ATAK 1 1 :Hardware)
>  (requires :ATAK 1 1 :LocationProvider)
>  (requires :Hardware 1 1 :Network)
>  (requires :Hardware 1 1 :Memory)
>  (requires :WifiLocationProvider 1 1 :Wifi)
>  (requires :Hardware 1 1 :GeoLocation)
>  (requires :GPSLocationProvider 1 N [:GPS :ExternalGPS])
>  (requires :GeoLocation 1 N [:GPS :ExternalGPS])
>  (requires :LocationProvider 1 N [:WifiLocationProvider])
>  (requires :Network 1 N [:Wifi :3G :LTE])
>  (requires :Memory 1 1 [:1GB :2GB])]


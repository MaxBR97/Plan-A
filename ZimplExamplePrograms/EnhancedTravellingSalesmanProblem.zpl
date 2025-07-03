# The good old classic TSP problem (the symmetric version), in an ENHANCED version with multiple optimization criterias.
# Define cities with their Cartesian coordinates, and plan an optimized route!

# Edges table - displays the transitions between cities in the outcome.
# Visits - Displays the list of visited cities.
# Totals - Display statistics of the solution.
# Visit_Order - Displays a suggestion to an order of transitioning between the cities. Cities with value 0 are excluded from the tour.
# The starting city is also the ending city. Therefore in Visit_Order it will appear as the last city in the order.

# good settings:
# set emph opt set presolv emph aggr

# Define set of cities with embedded coordinates (name, x, y)
set CitiesData := { 
    <"Tel Aviv", 5, 16>,
    <"Jerusalem", 25, 8>,
    <"Haifa", 6, 95>,
    <"Beer Sheva", -7, -23>,
    <"Eilat", 30, -103>,
    <"Tiberias", 40, 90>,
    <"Nazareth", 23, 85>,
    <"Ashdod", 3, 3>,
    <"Ashkelon", 2, -10>,
    <"Netanya", 8, 50>,

    <"Herzliya", 7, 25>,
    <"Ramat Gan", 6, 18>,
    <"Petah Tikva", 10, 20>,
    <"Rishon LeZion", 4, 10>,
    <"Rehovot", 5, 5>,
    <"Bat Yam", 4, 13>,
    <"Holon", 4, 12>,
    <"Raanana", 9, 30>,
    <"Kfar Saba", 11, 32>,
    <"Modiin", 15, 15>,
    
    <"Nahariya", 2, 105>,
    <"Kiryat Shmona", 45, 120>,
    <"Safed", 35, 100>,
    <"Dimona", 20, -35>,
    <"Lod", 8, 12>,
    <"Yotvata", 29, -101>
};

do print "Atleast two cities must be declared!";
do check card(CitiesData) >= 2;

set preferred_cities := {"Eilat"};

set indexSetOfCities := {<i,p,x,y> in {1.. card(CitiesData)} * CitiesData | ord(CitiesData,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

# Extract just the city names for use in variables and constraints
set Cities := proj(CitiesData, <1>);

# Parameter for number of cities to visit (default is all cities if commented out)
param cities_to_visit := 26;  # Change this value or comment out for full tour

do print "Number of cities to visit must be between 2 and the maximum number of cities!";
do check cities_to_visit <= card(Cities) and cities_to_visit >= 2;

# Calculate actual number of cities to visit based on the parameter value
param total_cities := card(Cities);
param actual_cities_to_visit := 
    if cities_to_visit <= 0 then 
        2  # Minimum tour length
    else if cities_to_visit > total_cities then 
        total_cities  # Maximum tour length
    else 
        cities_to_visit
    end end;

set preassigned_transitions := {<"Eilat","Jerusalem",1.0>};
set preassigned_visits := {<"Eilat",1.0>};

do print "Each city must be declared only once!";
do forall <city> in proj(CitiesData,<1>) do check 
    sum <city2,x2,y2> in CitiesData | city2 == city : 1 == 1;

do print "Preffered cities must be in declared cities data!";
do forall <preffered_city> in preferred_cities do check 
    sum <preffered_city> in proj(CitiesData,<1>)  : 1 == 1;

do print "Selected transitions must align with declared cities!";
do forall <fromCity,toCity> in {<a,b,c> in preassigned_transitions: <a,b>} do check
    sum <city> in proj(CitiesData,<1>) | city == fromCity : 1 == 1 and
    sum <city> in proj(CitiesData,<1>) | city == toCity : 1 == 1;

do print "Selected visits must align with declared cities!";
do forall <city> in {<a,b> in preassigned_visits: <a>} do check
    sum <city2> in proj(CitiesData,<1>) | city == city2 : 1 == 1;


# Function to get x coordinate of a city
defnumb getX(c) :=  ord({<city,x,y> in CitiesData | city == c },1,2);

# Function to get y coordinate of a city
defnumb getY(c) := ord({<city,x,y> in CitiesData | city == c },1,3);

defnumb getIndex(city) := ord({<index,city2,x,y> in indexSetOfCities | city2 == city},1,1);

# Calculate Euclidean distances between cities
param dist[<i, j> in Cities * Cities] := sqrt((getX(i) - getX(j))**2 + (getY(i) - getY(j))**2);

# Binary variable indicating if we use the edge between cities i and j
var Edges[Cities * Cities] binary;

# Binary variable indicating if a city is visited
var Visits[Cities] binary;
var Totals[{"Total Distance","Total Cities Visited","Total Preffered Cities Visited"}] real;
# Each visited city must have exactly one incoming and one outgoing edge
# Non-visited cities have no edges
subto enforce_preassigned_transitions: forall <fromCity,toCity,value> in preassigned_transitions:
    Edges[fromCity,toCity] == round(value);

subto enforce_preassigned_visits: forall <city,value> in preassigned_visits:
    Visits[city] == round(value);

subto degree: forall <i> in Cities:
    (sum <j> in Cities | i != j: Edges[i,j]) == Visits[i] and
    (sum <j> in Cities | i != j: Edges[j,i]) == Visits[i];

# Ensure we visit exactly the specified number of cities
subto visit_count:
    Totals["Total Cities Visited"] == actual_cities_to_visit;


# Subtour elimination constraints using Miller-Tucker-Zemlin formulation
var Visit_Order[Cities] integer >= 0 <= card(Cities);  # Auxiliary variables for subtour elimination


param starting_city := "Tel Aviv";

do print "Starting city must be in declared cities!";
do check sum <city> in Cities | city == starting_city : 1 == 1;

# Force first city to be visited (to break symmetry)
subto force_first:
    Visit_Order[starting_city] == Totals["Total Cities Visited"];
    
# Modified subtour elimination to only consider visited cities
subto subtour: forall <i,j> in Cities * Cities | i != j and i != starting_city :
    1 + Visit_Order[i] - Visit_Order[j] + Totals["Total Cities Visited"] * Edges[i,j] <= Totals["Total Cities Visited"];

# Additional constraint to ensure u values are 0 for non-visited cities
subto visit_order_visited: forall <i> in Cities:
    Visit_Order[i] <= Totals["Total Cities Visited"] * Visits[i] and Visit_Order[i] >= Visits[i];

param average_distance := (sum <i,j> in Cities * Cities: dist[i,j]) / actual_cities_to_visit**2; # used for normalization
do print "Average distance: ", average_distance;

subto calculate_totals:
    Totals["Total Distance"] == sum <i,j> in Cities * Cities: dist[i,j] * Edges[i,j] and
    Totals["Total Cities Visited"] == sum <i> in Cities: Visits[i] and
    Totals["Total Preffered Cities Visited"] == sum <i> in preferred_cities: Visits[i];

param cities_to_visit_coefficient := 100;
param distance_cost_coefficient := 10;
param preffered_cities_coefficient := 0;

# Minimize total distance traveled
minimize cost: 
    (-1 * cities_to_visit_coefficient) * (Totals["Total Cities Visited"]) +
    distance_cost_coefficient * (Totals["Total Distance"] / average_distance) -
    preffered_cities_coefficient * (sum <i> in preferred_cities: Visits[i]);
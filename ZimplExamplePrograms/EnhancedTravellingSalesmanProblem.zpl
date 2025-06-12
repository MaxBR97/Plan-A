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
    sum <i> in Cities: Visits[i] == actual_cities_to_visit;

# Force first city to be visited (to break symmetry)
subto force_first:
    Visits[ord(Cities, 1, 1)] == 1;

# Subtour elimination constraints using Miller-Tucker-Zemlin formulation
var VisitOrder[Cities] integer >= 0 <= card(Cities);  # Auxiliary variables for subtour elimination

# Modified subtour elimination to only consider visited cities
subto subtour: forall <i,j> in Cities * Cities | i != j and i != ord(Cities, 1,1) :
    1 + VisitOrder[i] - VisitOrder[j] + actual_cities_to_visit * Edges[i,j] <= actual_cities_to_visit;

# Additional constraint to ensure u values are 0 for non-visited cities
subto visit_order_visited: forall <i> in Cities:
    VisitOrder[i] <= actual_cities_to_visit * Visits[i] and VisitOrder[i] >= Visits[i];

subto calculate_totals:
    Totals["Total Distance"] == sum <i,j> in Cities * Cities: dist[i,j] * Edges[i,j] and
    Totals["Total Cities Visited"] == sum <i> in Cities: Visits[i] and
    Totals["Total Preffered Cities Visited"] == sum <i> in preferred_cities: Visits[i];

param cities_to_visit_coefficient := 90;
param distance_cost_coefficient := 20;
param preffered_cities_coefficient := 0;

# Minimize total distance traveled
minimize cost: 
    cities_to_visit_coefficient * Totals["Total Cities Visited"] +
    distance_cost_coefficient * Totals["Total Distance"] +
    preffered_cities_coefficient * Totals["Total Preffered Cities Visited"];
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

set preferred_cities := {"Eilat"};
do print "Preffered cities must be in declared cities data!";
do forall <preffered_city> in preferred_cities do check sum <preffered_city> in proj(CitiesData,<1>)  : 1 == 1;

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

# Function to get x coordinate of a city
defnumb getX(c) :=  ord({<city,x,y> in CitiesData | city == c },1,2);

# Function to get y coordinate of a city
defnumb getY(c) := ord({<city,x,y> in CitiesData | city == c },1,3);

defnumb getIndex(city) := ord({<index,city2,x,y> in indexSetOfCities | city2 == city},1,1);

# Calculate Euclidean distances between cities
param dist[<i, j> in Cities * Cities] := sqrt((getX(i) - getX(j))**2 + (getY(i) - getY(j))**2);

# Binary variable indicating if we use the edge between cities i and j
var edge[Cities * Cities] binary;

# Binary variable indicating if a city is visited
var visit[Cities] binary;
var totals[{"Total Distance","Total Cities Visited","Total Preffered Cities Visited"}] real;
# Each visited city must have exactly one incoming and one outgoing edge
# Non-visited cities have no edges
subto degree: forall <i> in Cities:
    (sum <j> in Cities | i != j: edge[i,j]) == visit[i] and
    (sum <j> in Cities | i != j: edge[j,i]) == visit[i];

# Ensure we visit exactly the specified number of cities
subto visit_count:
    sum <i> in Cities: visit[i] == actual_cities_to_visit;

# Force first city to be visited (to break symmetry)
subto force_first:
    visit[ord(Cities, 1, 1)] == 1;

# Subtour elimination constraints using Miller-Tucker-Zemlin formulation
var u[Cities] integer >= 0 <= card(Cities);  # Auxiliary variables for subtour elimination

# Modified subtour elimination to only consider visited cities
subto subtour: forall <i,j> in Cities * Cities | i != j and i != ord(Cities, 1,1) and j != ord(Cities, 1,1):
    u[i] - u[j] + total_cities * edge[i,j] <= total_cities - 1;

# Additional constraint to ensure u values are 0 for non-visited cities
subto u_visited: forall <i> in Cities:
    u[i] <= total_cities * visit[i];

subto calculate_totals:
    totals["Total Distance"] == sum <i,j> in Cities * Cities: dist[i,j] * edge[i,j] and
    totals["Total Cities Visited"] == sum <i> in Cities: visit[i] and
    totals["Total Preffered Cities Visited"] == sum <i> in preferred_cities: visit[i];

param cities_to_visit_coefficient := 90;
param distance_cost_coefficient := 20;
param preffered_cities_coefficient := 0;

# Minimize total distance traveled
minimize cost: 
    cities_to_visit_coefficient * totals["Total Cities Visited"] +
    distance_cost_coefficient * totals["Total Distance"] +
    preffered_cities_coefficient * totals["Total Preffered Cities Visited"];
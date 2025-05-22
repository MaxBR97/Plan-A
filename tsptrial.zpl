# This is a traveling salesman problem (TSP) which is NP-hard
# The complexity grows exponentially with the number of cities
# This version finds a minimum-cost Hamiltonian cycle without enforcing a starting city

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
    <"Lod", 8, 12>
};

set indexSetOfCities := {<i,p,x,y> in {1.. card(CitiesData)} * CitiesData | ord(CitiesData,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

# Extract just the city names for use in variables and constraints
set Cities := proj(CitiesData, <1>);

# Function to get x coordinate of a city
defnumb getX(c) :=  ord({<city,x,y> in CitiesData | city == c },1,2);

# Function to get y coordinate of a city
defnumb getY(c) := ord({<city,x,y> in CitiesData | city == c },1,3);

defnumb getIndex(city) := ord({<index,city2,x,y> in indexSetOfCities | city2 == city},1,1);

set distances := {<i,j> in Cities * Cities : <i,j,sqrt((random(1,10))**2 + (random(1,10))**2)>};
# Calculate Euclidean distances between cities
param dist[<i, j> in Cities * Cities] := 
    ord({<c1,c2,i_j_distance> in distances | c1 == i and c2 == j}, 1, 3);

defnumb getDist(i,j) := ord({<c1,c2,i_j_distance> in distances | c1 == i and c2 == j}, 1, 3);

# Binary variable indicating if we use the edge between cities i and j
var edge[Cities * Cities] binary;

# Each city must have exactly one incoming and one outgoing edge
subto degree: forall <i> in Cities:
    (sum <j> in Cities | i != j: edge[i,j]) == 1 and
    (sum <j> in Cities | i != j: edge[j,i]) == 1;

# Subtour elimination constraints using Miller-Tucker-Zemlin formulation
var u[Cities] integer >= 0 <= card(Cities);  # Auxiliary variables for subtour elimination
param n := card(Cities);

subto subtour: forall <i,j> in Cities * Cities with i != j and i != ord(Cities, 1,1) and j != ord(Cities, 1,1):
    u[i] - u[j] + n * edge[i,j] <= n - 1;

# Minimize total distance traveled
minimize cost: sum <i,j, di> in distances: di * edge[i,j];
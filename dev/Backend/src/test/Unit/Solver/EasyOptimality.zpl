# This is a traveling salesman problem (TSP) which is NP-hard
# The complexity grows exponentially with the number of cities
# This version enforces a Hamiltonian cycle starting and ending at Tel Aviv
# Each transition is indexed by step number to enforce ordering

# Define set of cities
set Cities := { 
    "Tel Aviv", "Jerusalem", "Haifa", "Beer Sheva"
    };

# Define x coordinates for each city
param x[Cities] := 
    <"Tel Aviv">       5   ,
    <"Jerusalem">      25  ,
    <"Haifa">         6   ,
    <"Beer Sheva">    -7  ;

# Define y coordinates for each city
param y[Cities] := 
    <"Tel Aviv">       16    ,
    <"Jerusalem">      8     ,
    <"Haifa">         95    ,
    <"Beer Sheva">    -23   ;

# Get the number of cities
param n := card(Cities);

# Define set of steps (n steps needed to visit n cities and return)
set Steps := {1 to n};

# Calculate Euclidean distances between cities
param dist[<city1, city2> in Cities * Cities] := 
    sqrt((x[city1] - x[city2])**2 + (y[city1] - y[city2])**2);

# Binary variable indicating if we travel from city i to j at step k
var edge[Steps * Cities * Cities] binary;

# Must start at Tel Aviv
subto start: sum <j> in Cities: edge[1,"Tel Aviv",j] == 1;

# Must end at Tel Aviv
subto endd: sum <i> in Cities: edge[n,i,"Tel Aviv"] == 1;

# Each city (except Tel Aviv) must be visited exactly once
subto visit: forall <j> in Cities with j != "Tel Aviv":
    sum <i> in Cities: sum <k> in Steps: edge[k,i,j] == 1;

# Flow conservation - if we enter a city at step k, we must leave it at step k+1
subto flow: forall <j,k> in Cities * Steps with k < n:
    sum <i> in Cities: edge[k,i,j] == sum <l> in Cities: edge[k+1,j,l];

# Can only use one transition per step
subto one_per_step: forall <k> in Steps:
    sum <i,j> in Cities * Cities: edge[k,i,j] == 1;

# Minimize total distance traveled
minimize cost: sum <i,j> in Cities * Cities: sum <k> in Steps: dist[i,j] * edge[k,i,j];

# This is a traveling salesman problem (TSP) which is NP-hard
# The complexity grows exponentially with the number of cities
# This version finds a minimum-cost Hamiltonian cycle without enforcing a starting city

# Define set of cities
set Cities := { 
    "Tel Aviv", "Jerusalem", "Haifa", "Beer Sheva", "Eilat",
    "Tiberias", "Nazareth", "Ashdod", "Ashkelon", "Netanya",
    "Herzliya", "Ramat Gan", "Petah Tikva", "Rishon LeZion", "Rehovot",
    "Bat Yam", "Holon", "Raanana", "Kfar Saba", "Modiin",
    # "Acre",
     "Nahariya", "Kiryat Shmona", "Safed", "Dimona",
    #  "Arad",
    #  "Mitzpe Ramon", "Sderot", "Yavne",
     "Lod"
};

# Define x coordinates for each city
param x[Cities] := 
    <"Tel Aviv">       5   ,
    <"Jerusalem">      25  ,
    <"Haifa">         6   ,
    <"Beer Sheva">    -7  ,
    <"Eilat">         30  ,
    <"Tiberias">      40  ,
    <"Nazareth">      23  ,
    <"Ashdod">        3   ,
    <"Ashkelon">      2   ,
    <"Netanya">       8   ,
    <"Herzliya">      7   ,
    <"Ramat Gan">     6   ,
    <"Petah Tikva">   10  ,
    <"Rishon LeZion"> 4   ,
    <"Rehovot">       5   ,
    <"Bat Yam">       4   ,
    <"Holon">         4   ,
    <"Raanana">       9   ,
    <"Kfar Saba">     11  ,
    <"Modiin">        15  ,
    # <"Acre">          3   
    # ,
    <"Nahariya">      2   ,
    <"Kiryat Shmona"> 45  ,
    <"Safed">         35  ,
    <"Dimona">        20  ,
    #  <"Arad">          25  ,
    # <"Mitzpe Ramon">  15  ,
    # <"Sderot">        -2  ,
    # <"Yavne">         4   ,
    <"Lod">           8   ;

# Define y coordinates for each city
param y[Cities] := 
    <"Tel Aviv">       16    ,
    <"Jerusalem">      8     ,
    <"Haifa">         95    ,
    <"Beer Sheva">    -23   ,
    <"Eilat">         -103  ,
    <"Tiberias">      90    ,
    <"Nazareth">      85    ,
    <"Ashdod">        3     ,
    <"Ashkelon">      -10   ,
    <"Netanya">       50    ,
    <"Herzliya">      25    ,
    <"Ramat Gan">     18    ,
    <"Petah Tikva">   20    ,
    <"Rishon LeZion"> 10    ,
    <"Rehovot">       5     ,
    <"Bat Yam">       13    ,
    <"Holon">         12    ,
    <"Raanana">       30    ,
    <"Kfar Saba">     32    ,
    <"Modiin">        15    ,
    # <"Acre">          98    
    # ,
    <"Nahariya">      105   ,
    <"Kiryat Shmona"> 120   ,
    <"Safed">         100   ,
    <"Dimona">        -35   ,
    #  <"Arad">          -30   ,
    # <"Mitzpe Ramon">  -70   ,
    # <"Sderot">        -15   ,
    # <"Yavne">         2     ,
    <"Lod">           12    ;

# Calculate Euclidean distances between cities
param dist[<city1, city2> in Cities * Cities] := 
    sqrt((x[city1] - x[city2])**2 + (y[city1] - y[city2])**2);

# Binary variable indicating if we use the edge between cities i and j
var edge[Cities * Cities] binary;

# Each city must have exactly one incoming and one outgoing edge
subto degree: forall <i> in Cities:
    (sum <j> in Cities | i != j: edge[i,j]) == 1 and
    (sum <j> in Cities | i != j: edge[j,i]) == 1;

# # Subtour elimination constraints using Miller-Tucker-Zemlin formulation
var u[Cities] integer >= 0;  # Auxiliary variables for subtour elimination
param n := card(Cities);

subto subtour: forall <i,j> in Cities * Cities with i != j and i != ord(Cities, 1,1) and j != ord(Cities, 1,1):
    u[i] - u[j] + n * edge[i,j] <= n - 1;

# Minimize total distance traveled
minimize cost: sum <i,j> in Cities * Cities: dist[i,j] * edge[i,j];

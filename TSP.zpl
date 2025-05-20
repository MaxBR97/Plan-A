# Sets
set V := {1 to 10};
set E := {<i,j> in V * V with i != j};

# Parameters
param d[<i,j> in E] := random(10, 20) * ((i * j) mod 57);

# Variables
var x[E] binary;
var u[V] real >= 1 <= card(V);  # Position of city in tour

# Constraints
# Each city must be entered exactly once
subto enter: forall <j> in V:
    sum <i2,j2> in E | j == j2: x[i2,j2] == 1;

# Each city must be left exactly once
subto leave: forall <i> in V:
    sum <i2,j2> in E | i == i2: x[i2,j2] == 1;


# Subtour elimination using MTZ formulation
subto mtz: forall <i,j> in E with j != 1 and j != 1 and i != j:
    u[i] - u[j] + card(V) * x[i,j] <= card(V) - 1;

# Fix position of first city
subto start: u[1] == 1;

# Objective: Minimize total distance
minimize cost: sum <i,j> in E: d[i,j] * x[i,j];
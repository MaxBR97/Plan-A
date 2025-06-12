# Chip Design Optimization Model
# This model optimizes the placement of components on a chip while considering:
# 1. Wire length minimization
# 2. Component overlap prevention
# 3. Thermal constraints
# 4. Power distribution
# 5. Signal timing requirements

# Basic parameters for integer to string conversion (for validation and output)
param toString[{0..9}] := <0> "0", <1> "1", <2> "2", <3> "3", <4> "4", 
                         <5> "5", <6> "6", <7> "7", <8> "8", <9> "9";
param stringToNumber[{"0","1","2","3","4","5","6","7","8","9"}] := 
    <"0"> 0, <"1"> 1, <"2"> 2, <"3"> 3, <"4"> 4, 
    <"5"> 5, <"6"> 6, <"7"> 7, <"8"> 8, <"9"> 9;


# Component types and their properties
# <component_type, width, height, power_consumption, max_temp>
set ComponentTypes := {
    <"CPU", 20, 20, 93.0, 95.0>,
    <"GPU", 15.7, 11.8, 83.0, 95.0>,
    <"Memory", 10.5, 30.0, 40, 85.0>,
    <"IO", 5.2 , 1.1, 10, 160.0>,
    <"Cache", 63.5, 8.0, 15, 90.0>
};

do print "Component types must be valid - WIDTH, HEIGHT must be positive, POWER must be non-negative, MAX_TEMP must be non-negative and greater than POWER!";
do forall <type, width, height, power, max_temp> in ComponentTypes do check
    width > 0 and
    height > 0  and
    power >= 0 and
    max_temp >= 0  and max_temp <= 200 and
    max_temp >= power;

# Component instances to be placed
# <instance_id, component_type>
set Components := {
    <1, "CPU">, <2, "GPU">, 
    <3, "Memory">, <4, "Memory">
    # <5, "IO">, <6, "IO">, <7, "IO">,
    # <8, "Cache">
};

do print "Components must reference valid component types!";
do forall <id, type> in Components do check
    card({<t,w,h,p,m> in ComponentTypes | t == type}) == 1;

# Connections between components (representing wires)
# <from_component, to_component, signal_priority>
set Connections := {
    <1,4,9.5>, 
    <2,3,8>, <2,4,9> 
    # <1,8,5>,          
    # <6,2,2>, <7,1,1>  
};

do print "Connections must reference valid components and have valid priority (0-10] !";
do forall <from_id, to_id, signal_priority> in Connections do check
    card({<id,type> in Components | id == from_id}) == 1 and
    card({<id,type> in Components | id == to_id}) == 1 and
    signal_priority > 0 and signal_priority <= 10;


#comp type: <component_type, width, height, power_consumption, max_temp>
# comp instance: <instance_id, component_type>

# Helper function to get component properties
defstrg getCompType(comp_id) := 
    ord({<c_id,t> in Components | c_id == comp_id},1,2);

defnumb getCompWidth(comp_id) := 
    ord({<t_1,width,c_id,t_2> in proj(ComponentTypes * Components, <1,2,6,7>) | t_1 == t_2 and c_id == comp_id},1,2);

defnumb getCompHeight(comp_id) := 
    ord({<t_1,height,c_id,t_2> in proj(ComponentTypes * Components, <1,3,6,7>) | t_1 == t_2 and c_id == comp_id},1,2);

defnumb getCompPower(comp_id) := 
    ord({<t_1,comp_power,c_id,t_2> in proj(ComponentTypes * Components, <1,4,6,7>) | t_1 == t_2 and c_id == comp_id},1,2);

defnumb getCompMaxTemp(comp_id) := 
    ord({<t_1,temp,c_id,t_2> in proj(ComponentTypes * Components, <1,5,6,7>) | t_1 == t_2 and c_id == comp_id},1,2);

# Decision 
# Chip dimensions are now variables to minimize
var Chip_Dimensions[{"Width","Height"}] integer >= 0 ;   # Width of the chip in units
var pos_x[Components] integer >= 0;   # x-coordinate of component placement
var pos_y[Components] integer >= 0;   # y-coordinate of component placement
# Temperature variables consolidated into one array
var temp[{"max_temp_x", "max_temp_y", "max_temp"}] real;

# Auxiliary variables for heat contribution at points of interest
var heat_at_max_point[Components] real >= 0;  # Heat contribution of each component at max temp point
var heat_at_component[Components * Components] real >= 0;  # Heat from component j at component i's location
var total_heat_at_component[Components] real >= 0;  # Total heat at each component's location

# Utility variables for relative positioning
var is_left_of[Components * Components] binary;    # 1 if component i is to the left of component j
var is_under[Components * Components] binary;      # 1 if component i is under component j

# Constraints

# param board_width := 200;
# param board_height := 200;
# param cell_size := 10;
# set grid := {<x,y> in {0 .. ceil((board_width-1)/cell_size)} * {0 ..ceil((board_height-1)/cell_size)}};
# var grid_occupancy[grid] integer >= 0;

# # Binary variable to track if a cell is occupied by a component
# var is_cell_occupied[Components * grid] binary;

# # # Calculate which cells are occupied by each component
# subto cell_component_occupation:
#     forall <id,t> in Components:
#         forall <x,y> in grid:
#             # Cell is occupied if it falls within component boundaries
#             vif 
#                 is_cell_occupied[id,t,x,y] == 1
#             then
#                 x >= pos_x[id,t] and x < pos_x[id,t] + getCompWidth(id) and
#                 y >= pos_y[id,t] and y < pos_y[id,t] + getCompHeight(id)
#             end;

# subto fix_board_dimensions:
#     chip_dimensions["Width"] == board_width and
#     chip_dimensions["Height"] == board_height;


subto temp_bounds:
    temp["max_temp_x"] >= 0 and temp["max_temp_x"] <= chip_dimensions["Width"] and
    temp["max_temp_y"] >= 0 and temp["max_temp_y"] <= chip_dimensions["Height"] and
    temp["max_temp"] >= 0;

# Components must fit within chip dimensions
subto components_within_chip:
    forall <id,t> in Components:
        pos_x[id,t] + getCompWidth(id) <= chip_dimensions["Width"] and
        pos_y[id,t] + getCompHeight(id) <= chip_dimensions["Height"];

# Define relative positioning using utility variables
subto define_left_of:
    forall <id_1,type_1> in Components:
        forall <id_2,type_2> in Components | id_1 != id_2:
            # If component 1 ends before component 2 starts in x-direction
            pos_x[id_1,type_1] + getCompWidth(id_1) <= pos_x[id_2,type_2] + chip_dimensions["Width"] * (1 - is_left_of[id_1,type_1,id_2,type_2]);

subto define_under:
    forall <id_1,type_1> in Components:
        forall <id_2,type_2> in Components | id_1 != id_2:
            # If component 1 ends before component 2 starts in y-direction
            pos_y[id_1,type_1] + getCompHeight(id_1) <= pos_y[id_2,type_2] + chip_dimensions["Height"] * (1 - is_under[id_1,type_1,id_2,type_2]);

# Prevent component overlap using utility variables
subto no_overlap:
    forall <id_1,type_1> in Components:
        forall <id_2,type_2> in Components | id_1 < id_2:
            # Components must be either to the left, right, under, or above each other
            is_left_of[id_1,type_1,id_2,type_2] + is_left_of[id_2,type_2,id_1,type_1] +
            is_under[id_1,type_1,id_2,type_2] + is_under[id_2,type_2,id_1,type_1] >= 1;

# Ensure consistent relative positioning
subto consistent_positioning:
    forall <id_1,type_1> in Components:
        forall <id_2,type_2> in Components | id_1 != id_2:
            # Cannot be both left of and right of
            is_left_of[id_1,type_1,id_2,type_2] + is_left_of[id_2,type_2,id_1,type_1] <= 1 and
            # Cannot be both under and above
            is_under[id_1,type_1,id_2,type_2] + is_under[id_2,type_2,id_1,type_1] <= 1;

# Calculate individual heat contributions at maximum temperature point
subto heat_contribution_at_max_point:
    forall <id,t> in Components:
        heat_at_max_point[id,t] * (1 + sqrt((temp["max_temp_x"] - pos_x[id,t])**2 + 
                                           (temp["max_temp_y"] - pos_y[id,t])**2)) == getCompPower(id);

# Maximum temperature point aggregates all heat
subto max_temperature_calculation:
    temp["max_temp"] == sum <id,t> in Components: heat_at_max_point[id,t];

# Calculate heat contributions at each component's location
subto heat_contribution_at_components:
    forall <id1,t1> in Components:
        forall <id2,t2> in Components:
            heat_at_component[id1,t1,id2,t2] * (1 + sqrt((pos_x[id1,t1] - pos_x[id2,t2])**2 + 
                                                        (pos_y[id1,t1] - pos_y[id2,t2])**2)) == getCompPower(id2);

# Calculate total heat at each component
subto total_heat_calculation:
    forall <id,t> in Components:
        total_heat_at_component[id,t] == sum <id2,t2> in Components: heat_at_component[id,t,id2,t2];

# Maximum temperature constraints for each component
subto temperature_limits:
    forall <id,t> in Components:
        total_heat_at_component[id,t] <= getCompMaxTemp(id);


# Minimize:
# 1. Total area (chip_width * chip_height)
# 2. Weighted distances between connected components
# 3. Maximum temperature
param area_weight := 1;
param connections_weight := 1;
param max_temp_weight := 0;


minimize obj:
    # Area minimization term
    area_weight * (chip_dimensions["Width"] * chip_dimensions["Height"]) +
    # Manhattan distances between connected components
    connections_weight * (sum <from_id,to_id,connection_priority> in Connections:
                                (connection_priority * (
                                    abs(pos_x[from_id,getCompType(from_id)] - pos_x[to_id,getCompType(to_id)]) +
                                    abs(pos_y[from_id,getCompType(from_id)] - pos_y[to_id,getCompType(to_id)])
                            ))) +
    # Temperature term
    max_temp_weight * temp["max_temp"];
    


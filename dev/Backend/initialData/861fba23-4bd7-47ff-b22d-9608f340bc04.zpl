# Chip Design Optimization Model
# This model optimizes the placement of components on a chip while considering:
# 1. Wire length minimization
# 2. Component overlap prevention
# 3. Thermal constraints
# 4. Power distribution
# 5. Signal timing requirements

# Basic parameters for single digit conversion
param toString[{0..9}] := <0> "0", <1> "1", <2> "2", <3> "3", <4> "4", 
                         <5> "5", <6> "6", <7> "7", <8> "8", <9> "9";

# Generic string conversion functions for any number of components
defstrg numberToString(n) := 
    if n < 10 then toString[n] else
    if n < 100 then toString[floor(n/10)] + toString[n mod 10] else
    if n < 1000 then toString[floor(n/100)] + toString[floor((n mod 100)/10)] + toString[n mod 10] else
    "999"  # Fallback for very large numbers
    end end end;



# Component types and their properties
# <component_type, width, height, power_consumption, max_temp>
set ComponentTypes := {
    <"CPU", 20, 20, 82.2, 95.0>,
    <"Memory", 10.5, 22.3, 75, 85.0>,
    <"IO", 5.2 , 17.1, 35, 60.0>,
    <"Cache", 3.8 , 6.45, 12, 60.0>,
    <"Clock", 2.0, 1.0, 9, 40.0>
};

do print "Highest power component is " , highest_power;
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
    <1, "CPU">,
    <2, "Memory">,
    <3, "IO">,
    <4, "IO">,
    <5, "Clock">
};

do print "Components must reference valid component types, and have an id between 1 and 999";
do forall <id, type> in Components do check
    card({<t,w,h,p,m> in ComponentTypes | t == type}) == 1
    and id >= 1 and id <= 999;

# Connections between components (representing wires)
# <from_component, to_component, signal_priority>
set Connections := {
    <1,2,5>,
    <2,3,3>,
    <1,3,9.5>,
    <1,4,7>,
    <1,5,10>,
    <2,5,10>,
    <3,5,10>,
    <4,5,2>   
};

do print "Connections must reference valid components, no duplicate connections, and have valid priority (0-10] !";
do forall <from_id, to_id, signal_priority> in Connections do check
    card({<id,type> in Components | id == from_id}) == 1 and
    card({<id,type> in Components | id == to_id}) == 1 and
    from_id != to_id and
    card({<from_id,to_id,signal_priority2> in Connections}) == 1 and
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

defstrg getPresentationFormat(comp_id,t) := numberToString(comp_id) + "_" + t;


set preassign_components := {<"X","1_CPU",0.0>};
do print "Selected components must reference valid components! Format should be 'ComponentIndex_Type";
do forall <xy,id_t,val> in preassign_components do check
    card({<id2,t2> in Components | id_t == getPresentationFormat(id2,t2)}) == 1;

set PresentationFormatOfComponents := {<id,t> in Components : <getPresentationFormat(id,t)>};
# Decision 
# Chip dimensions are now variables to minimize
var Chip_Dimensions[{"Width","Height"}] real >= 0;   # Width of the chip in units
var Component_Placement[{"X", "Y"} * PresentationFormatOfComponents] real >= if card(preassign_components) == 0 then 0.0001 else 0.000001 end; #For some reason, this makes a big difference
# Temperature variables consolidated into one array
var Chip_Temperature[<a,b> in {"X", "Y", "temp"} * {"max_temp", "min_temp"}] real >= 0 <= if a == "temp" then 9999 else 9999999 end;

# Auxiliary variables for heat contribution at points of interest
var heat_at_max_point[Components] real >= 0;  # Heat contribution of each component at max temp point
var Heat_Infliction_Of_Components[PresentationFormatOfComponents * PresentationFormatOfComponents] real >= 0;  # Heat from component j at component i's location
var Total_Heat_At_Each_Component[PresentationFormatOfComponents] real >= 0;  # Total heat at each component's location
var heat_at_min_point[Components] real >= 0;  # Heat contribution of each component at min temp point

# Utility variables for relative positioning
var is_left_of[Components * Components] binary;    # 1 if component i is to the left of component j
var is_under[Components * Components] binary;      # 1 if component i is under component j
var dummy_variables[ComponentTypes] binary;

# Wire length variables for connected components
var Wire_Lengths[{<from_id,to_id,connection_priority> in Connections : <from_id,to_id>}] real >= 0;  # Distance between closest points of connected components

# Constraints

subto preassign_components:
    forall <xy,id_t,val> in preassign_components:
        Component_Placement[xy,id_t] == val;

subto temp_bounds:
    Chip_Temperature["X","max_temp"] >= 0 and Chip_Temperature["X","max_temp"] <= Chip_Dimensions["Width"] and
    Chip_Temperature["Y","max_temp"] >= 0 and Chip_Temperature["Y","max_temp"] <= Chip_Dimensions["Height"] and
    Chip_Temperature["temp","max_temp"] >= 0 and  
    Chip_Temperature["X","min_temp"] >= 0 and Chip_Temperature["X","min_temp"] <= Chip_Dimensions["Width"] and
    Chip_Temperature["Y","min_temp"] >= 0 and Chip_Temperature["Y","min_temp"] <= Chip_Dimensions["Height"] and
    Chip_Temperature["temp","min_temp"] >= 0;

# Components must fit within chip dimensions
subto components_within_chip:
    forall <id,t> in Components:
        Component_Placement["X",getPresentationFormat(id,t)] + getCompWidth(id) <= Chip_Dimensions["Width"] and
        Component_Placement["Y",getPresentationFormat(id,t)] + getCompHeight(id) <= Chip_Dimensions["Height"];

# Define relative positioning using utility variables
subto define_left_of:
    forall <id_1,type_1> in Components:
        forall <id_2,type_2> in Components | id_1 != id_2:
            # If component 1 ends before component 2 starts in x-direction
            Component_Placement["X",getPresentationFormat(id_1,type_1)] + getCompWidth(id_1) <= Component_Placement["X",getPresentationFormat(id_2,type_2)] + Chip_Dimensions["Width"] * (1 - is_left_of[id_1,type_1,id_2,type_2]);

subto define_under:
    forall <id_1,type_1> in Components:
        forall <id_2,type_2> in Components | id_1 != id_2:
            # If component 1 ends before component 2 starts in y-direction
            Component_Placement["Y",getPresentationFormat(id_1,type_1)] + getCompHeight(id_1) <= Component_Placement["Y",getPresentationFormat(id_2,type_2)] + Chip_Dimensions["Height"] * (1 - is_under[id_1,type_1,id_2,type_2]);

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
        heat_at_max_point[id,t] * (1 + sqrt((Chip_Temperature["X","max_temp"] - (Component_Placement["X",getPresentationFormat(id,t)] + getCompWidth(id)/2))**2 + 
                                           (Chip_Temperature["Y","max_temp"] - (Component_Placement["Y",getPresentationFormat(id,t)] + getCompHeight(id)/2))**2)) == getCompPower(id);

subto heat_contribution_at_min_point:
    forall <id,t> in Components:
        heat_at_min_point[id,t] * (1 + sqrt((Chip_Temperature["X","min_temp"] - (Component_Placement["X",getPresentationFormat(id,t)] + getCompWidth(id)/2))**2 + 
                                            (Chip_Temperature["Y","min_temp"] - (Component_Placement["Y",getPresentationFormat(id,t)] + getCompHeight(id)/2))**2)) == getCompPower(id);

# Maximum temperature point aggregates all heat
subto temperature_calculation:
    Chip_Temperature["temp","max_temp"] == sum <id,t> in Components: heat_at_max_point[id,t];

subto min_temperature_calculation:
    Chip_Temperature["temp","min_temp"] == sum <id,t> in Components: heat_at_min_point[id,t];

subto max_temp_higher_than_each_component:
    forall <id,t> in Components:
        Chip_Temperature["temp","max_temp"] >= getCompPower(id);



# Calculate heat contributions at each component's location
subto heat_contribution_at_components:
    forall <id1,t1> in Components:
        forall <id2,t2> in Components:
            Heat_Infliction_Of_Components[getPresentationFormat(id1,t1),getPresentationFormat(id2,t2)] * (1 + sqrt(((Component_Placement["X",getPresentationFormat(id1,t1)] + getCompWidth(id1)/2) - (Component_Placement["X",getPresentationFormat(id2,t2)] + getCompWidth(id2)/2))**2 + 
                                                        ((Component_Placement["Y",getPresentationFormat(id1,t1)] + getCompHeight(id1)/2) - (Component_Placement["Y",getPresentationFormat(id2,t2)] + getCompHeight(id2)/2))**2)) == getCompPower(id2);

# Calculate total heat at each component
subto total_heat_calculation:
    forall <id,t> in Components:
        Total_Heat_At_Each_Component[getPresentationFormat(id,t)] == sum <id2,t2> in Components: Heat_Infliction_Of_Components[getPresentationFormat(id,t),getPresentationFormat(id2,t2)];

# Maximum temperature constraints for each component
subto temperature_limits:
    forall <id,t> in Components:
        Total_Heat_At_Each_Component[getPresentationFormat(id,t)] <= getCompMaxTemp(id);

# Calculate wire length between connected components based on relative positioning
subto calculate_Wire_Lengths:
    forall <from_id,to_id,connection_priority> in Connections:
        # Case 1: from_id is left of to_id
        Wire_Lengths[from_id,to_id] ** 2 >= 
            (((Component_Placement["X",getPresentationFormat(to_id,getCompType(to_id))] - 
             (Component_Placement["X",getPresentationFormat(from_id,getCompType(from_id))] + getCompWidth(from_id))) * 
            is_left_of[from_id,getCompType(from_id),to_id,getCompType(to_id)])
        +
            ((Component_Placement["X",getPresentationFormat(from_id,getCompType(from_id))] - 
             (Component_Placement["X",getPresentationFormat(to_id,getCompType(to_id))] + getCompWidth(to_id))) * 
            is_left_of[to_id,getCompType(to_id),from_id,getCompType(from_id)]))**2
    +
            (((Component_Placement["Y",getPresentationFormat(to_id,getCompType(to_id))] - 
             (Component_Placement["Y",getPresentationFormat(from_id,getCompType(from_id))] + getCompHeight(from_id))) * 
            is_under[from_id,getCompType(from_id),to_id,getCompType(to_id)])
        +
            ((Component_Placement["Y",getPresentationFormat(from_id,getCompType(from_id))] - 
             (Component_Placement["Y",getPresentationFormat(to_id,getCompType(to_id))] + getCompHeight(to_id))) * 
            is_under[to_id,getCompType(to_id),from_id,getCompType(from_id)]))**2;

# Minimize:
# 1. Total area (chip_width * chip_height)
# 2. Weighted distances between connected components
# 3. Maximum temperature
param area_weight := 1;
param connections_weight := 1;
param temp_weight := 1;

param highest_power_comp := max <type, width, height, power, max_temp> in ComponentTypes: power;

minimize obj:
    area_weight * (Chip_Dimensions["Width"] * Chip_Dimensions["Height"]) +
    connections_weight * (sum <from_id,to_id,connection_priority> in Connections:
                                connection_priority * (Wire_Lengths[from_id,to_id])**2) +
    (temp_weight) * (((highest_power_comp*5) - Chip_Temperature["temp","max_temp"]) + Chip_Temperature["temp","min_temp"]) +
    (highest_power_comp*5 - Chip_Temperature["temp","max_temp"]);



param weight := 10;
param NumberOfSoldiers := 6;
param bias := 100;

set Soldiers := {1..NumberOfSoldiers};
set Stations := {"Shin Gimel", "West"};  # Station names
set Times := {0, 1, 2, 3, 4};
set PossibleShiftAndRestTimes := {<time1, time2> in  Times*(Times union {max(Times)+1}) | time1 < time2: <time1,time2-time1>};

set Shifts := Stations * Times;
set SoldiersToShifts := Soldiers * Shifts;
set ShiftSpacings := {<soldier,station,time,rest> in Soldiers * Stations * PossibleShiftAndRestTimes};
set ArtificialShift := {<soldier,station,time,rest> in Soldiers * {"inv"} * ({<time1, time2> in  {-1}*(Times union {max(Times)+1,max(Times)+2}) | time1 < time2: <time1,time2-time1>} union {<max(Times)+1,0>}) };
# do print card(ShiftSpacings);
var Edge[SoldiersToShifts] binary;
var NeighbouringShifts[ShiftSpacings union ArtificialShift] binary;
do print card(SoldiersToShifts);
do print card(ShiftSpacings union ArtificialShift);
# subto SemiAssignment:
#     Edge[1,"Shin Gimel",0] == 1 and
#     Edge[2,"Shin Gimel",1] == 1 and
#     Edge[3,"Shin Gimel",2] == 1 and
#     Edge[4,"Shin Gimel",3] == 1 and
#     Edge[5,"Shin Gimel",4] == 1 and
#     Edge[6,"Fillbox",0] == 1 and
#     Edge[7,"Fillbox",1] == 1;

subto EnforceOnlyTwoArtificialShift:
    forall <soldier> in Soldiers:
            (sum <i,station,time,rest> in ArtificialShift | i == soldier: NeighbouringShifts[i,station,time,rest] ) == 2;
do print "loaded EnforceOnlyTwoArtificialShift";
# sum = 0 -> NeighbouringShifts = 1
subto EnforceMaximalRestTimeWhenNoShifts:
    forall <soldier> in Soldiers: 
        (sum <i,a,b> in SoldiersToShifts | i == soldier: Edge[i,a,b]) + NeighbouringShifts[soldier,"inv", -1, max(Times)+2] >= 1;
do print "loaded EnforceMaximalRestTimeWhenNoShifts";
subto EnforceMaximalRestTimeWhenNoShifts2:
    forall <soldier> in Soldiers: 
        NeighbouringShifts[soldier,"inv",max(Times)+1,0] == 1;
do print "loaded EnforceMaximalRestTimeWhenNoShifts2";
# Neighbouring = 1 -> (sum1 >= 1 && sum2 >= 1)
subto EnforceShiftSpacingLogic1:
    forall <soldier,station,time,rest> in ShiftSpacings : 
        NeighbouringShifts[soldier,station,time,rest] - 
            ((sum <i,a,b,r> in ShiftSpacings union ArtificialShift | i == soldier and b == (time + rest) : NeighbouringShifts[i,a,b,r]) 
             * (sum <i,a,b,r> in ShiftSpacings union ArtificialShift | i == soldier and (b + r) == time : NeighbouringShifts[i,a,b,r])) <= 0;
                                                        
do print "loaded EnforceShiftSpacingLogic1";

# #Neightbouring = 1 -> sum >=1
# subto EnforceShiftSpacingLogic2:
#     forall <soldier,station,time,rest> in ShiftSpacings : 
#          NeighbouringShifts[soldier,station,time,rest] - (sum <i,a,b,r> in ShiftSpacings union ArtificialShift | i == soldier and (b + r) == time : NeighbouringShifts[i,a,b,r]) <= 0;
                                                        

# # Neightbouring =1 -> edge =1
# subto EnforceShiftSpacingLogic3:
#     forall <soldier,station,time,rest> in ShiftSpacings : 
#      NeighbouringShifts[soldier,station,time,rest] - Edge[soldier,station,time] <= 0; 

# subto EnforceShiftSpacingLogic3:
#     forall <soldier,station,time> in SoldiersToShifts : 
#      (sum <i,a,b,r> in ShiftSpacings | i == soldier and a == station and b == time :NeighbouringShifts[soldier,station,time,r]) - Edge[soldier,station,time] <= 0; 
                                                        
# Edge = 1 <-> sum = 1 
subto EnforceShiftSpacingLogic4:
    forall <soldier,station,time,rest> in ShiftSpacings : 
        Edge[soldier,station,time] - (sum <i,a,b,r> in ShiftSpacings | i == soldier and a == station and b == time : NeighbouringShifts[i,a,b,r]) == 0;
                               
do print "loaded EnforceShiftSpacingLogic4";                          

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <soldier, time> in Soldiers * Times : 
        (sum<m,a,b> in SoldiersToShifts | m == soldier and time == b : Edge[m,a,b]) <= 1;

do print "loaded Soldier_Not_In_Two_Stations_Concurrently";

subto All_Stations_One_Soldier:
    forall <s,f> in Shifts : 
        (sum<a,b,c> in SoldiersToShifts | s==b and f==c : Edge[a,b,c]) == 1;

do print "loaded All_Stations_One_Soldier";

# subto experimenting: #AHA~
#     forall <soldier> in Soldiers:
#         sum <i,a,b> in SoldiersToShifts |i == soldier:Edge[i,a,b] >= 1;

set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize myObjective: 
    sum <soldier> in Soldiers : ((sum <i,station,time,rest> in (ShiftSpacings union ArtificialShift) | i == soldier : (NeighbouringShifts[i,station,time,rest]*(rest+1)**2)) ** 2);
    #+ (sum <i,person,station,time> in  indexSetOfPeople*Shifts | time <= card(Soldiers)/card(Stations)*1: (Edge[person,station,time]*bias*i*(time+1))); # assign first few soldiers by their order in the set

    #TODO: make a nice heuristic to auto-assign in the beggining the first few soldiers to first few stations.
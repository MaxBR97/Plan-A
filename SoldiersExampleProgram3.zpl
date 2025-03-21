param weight := 10;
param NumberOfSoldiers := 20;
param bias := 100;

set Soldiers := {1..NumberOfSoldiers};
set Stations := {"Shin Gimel","Siyur","Fillbox","North","West","South","East","Hamal"};  # Station names
set Times := {0 .. 50 by 1};

set Shifts := Stations * Times;
set SoldiersToShifts := Soldiers * Shifts;

var Edge[SoldiersToShifts] binary;



# subto SemiAssignment:
#     Edge[1,"Shin Gimel",0] == 1 and
#     Edge[2,"Shin Gimel",1] == 1 and
#     Edge[3,"Shin Gimel",2] == 1 and
#     Edge[4,"Shin Gimel",3] == 1 and
#     Edge[5,"Shin Gimel",4] == 1 and
#     Edge[6,"Fillbox",0] == 1 and
#     Edge[7,"Fillbox",1] == 1;

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <soldier,time> in Soldiers * Times : 
        (sum<m,a,b> in SoldiersToShifts | m == soldier and time == b : Edge[m,a,b]) <= 1;

subto All_Stations_One_Soldier:
    forall <station,time> in Shifts : 
        (sum<a,b,c> in SoldiersToShifts | station==b and time==c : Edge[a,b,c]) == 1;

# defnum(soldier,t1,t2) := sum <t3,station> in Times * Stations | t3 > t1 and t3 < t2 :Edge[soldier,station,t3];

# subto Calculate_Spacing_Cost:
#     forall <soldier> in Soldiers:
#         SpacingValue[soldier] == sum <t1,t2> in Times*Times | t1 < t2 :
#             (sum <station> in Stations : Edge[soldier,station,t1]) * (sum <station> in Stations : Edge[soldier,station,t2])
#             * abs((sum <t3,station> in Times * Stations | t3 >= t1 and t3 <= t2 :Edge[soldier,station,t3]) - 1)
#             * (t2 - t1) ;

             
# subto experimenting:
#     forall <soldier> in Soldiers:
#         sum <i,a,b> in SoldiersToShifts |i == soldier:Edge[i,a,b] >= 1;

set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize myObjective: 
    sum <i,a,b> in SoldiersToShifts : Edge[i,a,b]
    ;
    
    #+ (sum <i,person,station,time> in  indexSetOfPeople*Shifts | time <= card(Soldiers)/card(Stations)*1: (Edge[person,station,time]*bias*i*(time+1))); # assign first few soldiers by their order in the set

    #TODO: make a nice heuristic to auto-assign in the beggining the first few soldiers to first few stations.
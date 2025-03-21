
param NumberOfSoldiers := 50;
param bias := 100;

param planFromDay := "Sunday";
param planFromHour := 0;
param planUntilDay := "Tuesday";
param planUntilHour := 0;

set weekDays := {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
param orderWeekDays[weekDays] := <"Sunday"> 1 , <"Monday"> 2, <"Tuesday"> 3, <"Wednesday"> 4, <"Thursday"> 5, <"Friday"> 6, <"Saturday"> 7;
set hours := {0 .. 23};

defnumb timeDifference(fromDay, fromHour, toDay, toHour) := 
    if orderWeekDays[fromDay] == orderWeekDays[toDay]
    then if toHour > fromHour then toHour-fromHour else  (card(hours) * card(weekDays)) - (fromHour-toHour) end
    else
       if orderWeekDays[fromDay] < orderWeekDays[toDay]
        then    (toHour - fromHour) +  (card(hours) * (orderWeekDays[toDay] - orderWeekDays[fromDay]))
        else    (toHour - fromHour) +  (card(hours) * card(weekDays)) - (card(hours) * (orderWeekDays[fromDay] - orderWeekDays[toDay]))
       end
    end
    ;

# # Tests for timeDifference                     | expected
# do print timeDifference("Sunday",18,"Sunday",19); #1
# do print timeDifference("Sunday",0,"Sunday",23); #23
# do print timeDifference("Sunday",4,"Sunday",0); #164
# do print "-------";
# do print timeDifference("Sunday",0,"Monday",0); #24
# do print timeDifference("Sunday",0,"Tuesday",0); #48
# do print timeDifference("Sunday",23,"Tuesday",23); #48
# do print timeDifference("Sunday",23,"Tuesday",22); #47
# do print timeDifference("Sunday",21,"Tuesday",23); #50
# do print timeDifference("Sunday",0,"Tuesday",12); #60
# do print "-------";
# do print timeDifference("Saturday",0,"Sunday",0); #24
# do print timeDifference("Saturday",12,"Sunday",0); #12
# do print timeDifference("Saturday",12,"Sunday",1); #13
# do print timeDifference("Friday",1 ,"Monday",1); #72




defbool isBetween(fromDay,fromHour,toDay,toHour,targetDay,targetHour) :=
    (orderWeekDays[targetDay] == orderWeekDays[fromDay] and targetHour >= fromHour) or
    (orderWeekDays[targetDay] == orderWeekDays[toDay] and targetHour <= toHour) or
    if orderWeekDays[fromDay] > orderWeekDays[toDay] and targetDay != fromDay and targetDay != toDay
    then (orderWeekDays[targetDay] < orderWeekDays[toDay] or  orderWeekDays[targetDay] > orderWeekDays[fromDay])
    else (orderWeekDays[targetDay] < orderWeekDays[toDay] and  orderWeekDays[targetDay] > orderWeekDays[fromDay])
    end;

# Tests for isBetween                                   | expected
# do print isBetween("Friday",18,"Sunday",8,"Sunday",12); #false
# do print isBetween("Friday",18,"Sunday",8,"Sunday",7); #true
# do print isBetween("Friday",18,"Sunday",8,"Saturday",12); #true
# do print isBetween("Friday",18,"Sunday",8,"Friday",12); #false
# do print isBetween("Friday",18,"Sunday",8,"Friday",20); #true
# do print isBetween("Friday",18,"Sunday",8,"Monday",12); #false
# do print isBetween("Sunday",18,"Friday",8,"Saturday",12); #false
# do print isBetween("Sunday",18,"Friday",8,"Monday",12); #true

set Soldiers := {1..NumberOfSoldiers};
# <name,shift_time,people_in_shift>
set Stations := {<"Shin Gimel",4,1>,<"Siyur1",8,4>,<"Siyur2",8,4>,<"Siyur3",8,4>,<"Yezuma",10,2>,<"FillBox",12,4>,<"Hamal",24,1>};  # Station names
set Times := {<day,hour> in weekDays * hours | isBetween(planFromDay,planFromHour,planUntilDay,planUntilHour,day,hour)};
# do print Times;
set Shifts := {<station, stationInterval, requiredPeople, day, hour> in Stations * Times | timeDifference(planFromDay,planFromHour, day, hour) mod stationInterval == 0};
# do print Shifts;
# do print card(Shifts);
                                                                                                                                                                                                                                    #Heuristic
set PossibleShiftAndRestTimes := {<day,hour,untilDay,untilHour> in  proj(Shifts,<4,5>)* ({proj(Shifts,<4,5>)} union {<planUntilDay,timeDifference(planFromDay,planFromHour,planUntilDay,planUntilHour)+1>}) | timeDifference(day,hour,untilDay,untilHour) < 96 : <day,hour, timeDifference(day,hour,untilDay,untilHour)>};
# do print PossibleShiftAndRestTimes;
# do print card(PossibleShiftAndRestTimes);
set SoldiersToShifts := Soldiers * proj(Shifts,<1,4,5>);
set ShiftSpacings := {<soldier,station,stationInterval,day,hour,day2,hour2,rest> in Soldiers * proj(Shifts,<1,2,4,5>) * PossibleShiftAndRestTimes | day2 == day and hour2 == hour and rest - stationInterval >= 0 : <soldier,station,day,hour,rest - stationInterval>};
# do print ShiftSpacings;
do print card(ShiftSpacings);
set ArtificialShift := {<soldier,station,time,rest> in Soldiers * {"inv"} * ({<time1, time2> in  {-1}*(Times union {max(Times)+1,max(Times)+2}) | time1 < time2: <time1,time2-time1>} union {<max(Times)+1,0>}) };

var Edge[SoldiersToShifts] binary;
var NeighbouringShifts[ShiftSpacings union ArtificialShift] binary;

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

# sum = 0 -> NeighbouringShifts = 1
subto EnforceMaximalRestTimeWhenNoShifts:
    forall <soldier> in Soldiers: 
        (sum <i,a,b> in SoldiersToShifts | i == soldier: Edge[i,a,b]) + NeighbouringShifts[soldier,"inv", -1, max(Times)+2] >= 1;

subto EnforceMaximalRestTimeWhenNoShifts2:
    forall <soldier> in Soldiers: 
        NeighbouringShifts[soldier,"inv",max(Times)+1,0] == 1;

# Neighbouring = 1 -> (sum1 >= 1 && sum2 >= 1)
subto EnforceShiftSpacingLogic1:
    forall <soldier,station,time,rest> in ShiftSpacings : 
        NeighbouringShifts[soldier,station,time,rest] - 
            ((sum <i,a,b,r> in ShiftSpacings union ArtificialShift | i == soldier and b == (time + rest) : NeighbouringShifts[i,a,b,r]) 
             * (sum <i,a,b,r> in ShiftSpacings union ArtificialShift | i == soldier and (b + r) == time : NeighbouringShifts[i,a,b,r])) <= 0;
                                                        

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
                               

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <soldier, time> in Soldiers * Times : 
        (sum<m,a,b> in SoldiersToShifts | m == soldier and time == b : Edge[m,a,b]) <= 1;

subto All_Stations_One_Soldier:
    forall <s,f> in Shifts : 
        (sum<a,b,c> in SoldiersToShifts | s==b and f==c : Edge[a,b,c]) == 1;

# subto experimenting: #AHA~
#     forall <soldier> in Soldiers:
#         sum <i,a,b> in SoldiersToShifts |i == soldier:Edge[i,a,b] >= 1;

set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize myObjective: 
    sum <soldier> in Soldiers : ((sum <i,station,time,rest> in (ShiftSpacings union ArtificialShift) | i == soldier : (NeighbouringShifts[i,station,time,rest]*(rest+1)**2)) ** 2);
    #+ (sum <i,person,station,time> in  indexSetOfPeople*Shifts | time <= card(Soldiers)/card(Stations)*1: (Edge[person,station,time]*bias*i*(time+1))); # assign first few soldiers by their order in the set

    #TODO: make a nice heuristic to auto-assign in the beggining the first few soldiers to first few stations.
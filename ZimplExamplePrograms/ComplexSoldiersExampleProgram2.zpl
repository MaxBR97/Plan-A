
param NumberOfSoldiers := 20;
param bias := 100;

param planFromDay := "Sunday";
param planFromHour := 0;
param planUntilDay := "Monday";
param planUntilHour := 0;

param minimumRestTime := 0;

set weekDays := {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
param orderWeekDays[weekDays] := <"Sunday"> 1 , <"Monday"> 2, <"Tuesday"> 3, <"Wednesday"> 4, <"Thursday"> 5, <"Friday"> 6, <"Saturday"> 7;
param orderWeekDaysOpposite[{1..7}] := <1> "Sunday", <2> "Monday", <3>"Tuesday", <4>"Wednesday", <5>"Thursday", <6>"Friday", <7>"Saturday";
param convertToPresentation[weekDays] := <"Sunday"> "1 Sunday", <"Monday"> "2 Monday", <"Tuesday"> "3 Tuesday", <"Wednesday"> "4 Wednesday", <"Thursday"> "5 Thursday", <"Friday"> "6 Friday", <"Saturday"> "7 Saturday";
set hours := {0 .. 23};

defnumb timeDifference(fromDay, fromHour, toDay, toHour) := 
    if orderWeekDays[fromDay] == orderWeekDays[toDay]
    then if toHour >= fromHour then toHour-fromHour else  (card(hours) * card(weekDays)) - (fromHour-toHour) end
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
# do print timeDifference("Sunday",0,"Sunday",0); #0
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



defnumb getDay(time) := 
    orderWeekDaysOpposite[(((orderWeekDays[planFromDay] - 1) + floor(time / 24)) mod card(weekDays)) + 1];
# to test assign planFromDay = "Sunday", planFromHour = 0;
# do print getDay(1); # Sunday;
# do print getDay(23); # Sunday;
# do print getDay(24); # Monday;
# do print getDay(24*7); # Sunday;
# do print getDay(24*6); # Saturday;



defnumb getHour(time) := 
    ((time+ planFromHour) mod 24);

# to test, assign planFromHour = 6;
# do print getHour(0); # 6;
# do print getHour(1); # 7;
# do print getHour(24); # 6;
# do print getHour(48); # 6;
# do print getHour(12); # 18;
# do print getHour(24*7); # 6;
# do print getHour(24*7 - 1); # 5;

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
 set Stations := {<"Siyur1",8,4>,<"FillBox",4,1>,<"Shin Gimel",4,1>, <"Hamal",24,1>, <"Siyur2",8,4>, <"Siyur3",8,4>};  # Station names
# set Stations := {<"Siyur1",8,4>,<"FillBox",4,1>};  # Station names
set FormalTimes := {<day,hour> in weekDays * hours | isBetween(planFromDay,planFromHour,planUntilDay,planUntilHour,day,hour)};
param planTimeRange := timeDifference(planFromDay,planFromHour,planUntilDay,planUntilHour);
set Times := {0 .. planTimeRange};

set Shifts := {<station, stationInterval, requiredPeople, time> in Stations * Times | time mod stationInterval == 0 and time + stationInterval <= planTimeRange};

set SoldiersToShifts := Soldiers * proj(Shifts,<1,4>);

set ArtificialStation := {<"inv",0,1>};
set ArtificialShift := {<soldier,station,time> in Soldiers * proj(ArtificialStation,<1>) * {-1, card(Times) + 1 } };
set preAssign := {
                #  <1,"Siyur1","Monday",0>, <2,"Siyur1","Monday",0>, <3,"Siyur1","Monday",0>, <4,"Siyur1","Monday",0>,
                #  <5,"FillBox","Monday",0>,<6,"FillBox","Sunday",8>, <7,"FillBox","Sunday",12>, <8,"FillBox","Sunday",16>, <9,"FillBox","Sunday",20>
                };
set encodedPreAssign := {<soldier,station,day,hour> in preAssign: <soldier,station,timeDifference(planFromDay,planFromHour, day, hour)>};                
param AntiPreAssignRate := 0.15;
set antiPreAss := {<soldier,station,time> in SoldiersToShifts | floor(random(0+AntiPreAssignRate,0.9999999999+AntiPreAssignRate)) == 1} - encodedPreAssign;
var Edge[<i,a,b> in SoldiersToShifts union ArtificialShift] binary startval 0; #
var NeighbouringShifts[SoldiersToShifts union ArtificialShift] integer >= 0 <= card(Times)+2 startval 0; #
set FormalShiftsDescription := {<station, stationInterval, requiredPeople, day, hour> in Stations * FormalTimes | timeDifference(planFromDay,planFromHour, day, hour) mod stationInterval == 0 : <station, stationInterval, requiredPeople, convertToPresentation[day], hour>};
var FormalTimesEdges[Soldiers * proj(FormalShiftsDescription,<1,4,5>)] binary; #
# set labelsForShiftStatistics := {"Problem size (people * stations * times)", "Total Shifts Assigned", "Average Shifts Per Person", "Average Rest Time", "People Not Assigned Atleast Once"}
# var ShiftStatistics[labels] integer;
do print FormalShiftsDescription;
subto PreAssign:
    forall<a,b,c> in encodedPreAssign :
        Edge[a,b,c] == 1;

subto AntiPreAssign:
    forall<a,b,c> in antiPreAss :
        Edge[a,b,c] == 0 and NeighbouringShifts[a,b,c] == 0;

subto ConvertEnumeratedTimesToFormal:
    forall <soldier,station,time> in SoldiersToShifts:
        FormalTimesEdges[soldier,station,convertToPresentation[getDay(time)],getHour(time)] == Edge[soldier,station,time];

defnumb nextKnownShift(soldier,time) :=
    if card({<so,st,t> in encodedPreAssign | soldier == so and time < t}) == 0 
    then card(Times)+1
    else min <sol,station, t> in encodedPreAssign | t > time and soldier == sol: t end;

do print card({<soldier, station, time, joinStation ,shiftInterval, requiredPeople> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) * proj(Stations union ArtificialStation, <1,2,3>) | joinStation == station and time != (card(Times) + 1) and (card({<a,b,c> in encodedPreAssign | b == station and c == time}) < requiredPeople or card({<a,b,c> in encodedPreAssign | a == soldier and b == station and c == time}) >= 1)});

subto EnforceCalculationOfRestTimes1:
    forall <soldier, station, time, joinStation ,shiftInterval, requiredPeople> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) * proj(Stations union ArtificialStation, <1,2,3>) | joinStation == station and time != (card(Times) + 1) and (card({<a,b,c> in encodedPreAssign | b == station and c == time}) < requiredPeople or card({<a,b,c> in encodedPreAssign | a == soldier and b == station and c == time}) >= 1):
        forall <soldier2, station2, time2>  in ((SoldiersToShifts union ArtificialShift) - antiPreAss) | soldier == soldier2 and time2 <= (card(Times) + 1) and (time2 >= min(time + shiftInterval + minimumRestTime, card(Times)+1) or time == -1) :
            NeighbouringShifts[soldier,station,time] >= (time2-time-shiftInterval) * Edge[soldier,station,time] * Edge[soldier,station2,time2] * (1 - (sum <soldier3,station3,time3> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) | soldier3 == soldier and time3 > time  and time3 < time2 : Edge[soldier3,station3,time3]));
        
do print "loaded EnforceCalculationOfRestTimes1";


# Edge = 0 -> NeighbouringShifts = 0                                                      
subto NeighbouringShiftsAndEdgesAreConnected:
    forall <soldier,station,time> in SoldiersToShifts - antiPreAss:
        (1 - Edge[soldier,station,time]) * NeighbouringShifts[soldier,station,time] == 0;

do print "loaded NeighbouringShiftsAndEdgesAreConnected";

subto EnforceOnlyTwoArtificialShift:
    forall <soldier> in Soldiers:
            (sum <i,station,time> in ArtificialShift | i == soldier: Edge[i,station,time] ) == 2;

do print "loaded EnforceOnlyTwoArtificialShift";

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts : 
        Edge[soldier,station,time] * (sum <soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 >= time and time2 < (time + stationInterval) : Edge[soldier2,station2,time2]) <= 1;

subto Enforce_Minimum_Rest_Time_Heuristic:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts : 
        Edge[soldier,station,time] * (sum <soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 >= time + stationInterval and time2 < (time + stationInterval + minimumRestTime) : Edge[soldier2,station2,time2]) == 0;

do print "loaded Enforce_Minimum_Rest_Time_Heuristic";

subto Satisfy_Required_People_For_Shift:
    forall <station, stationInterval, requiredPeople, time> in Shifts : 
        (sum<soldier,station2,time2> in SoldiersToShifts | station == station2 and time == time2 : Edge[soldier,station,time]) == requiredPeople;

do print "loaded All_Stations_One_Soldier";

set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize myObjective: 
    sum <soldier> in Soldiers : ((sum <i,station,time> in (SoldiersToShifts union ArtificialShift) | i == soldier : ((NeighbouringShifts[i,station,time]+1)**2)));
    #+ (sum <i,person,station,time> in  indexSetOfPeople*Shifts | time <= card(Soldiers)/card(Stations)*1: (Edge[person,station,time]*bias*i*(time+1))); # assign first few soldiers by their order in the set

    #TODO: make a nice heuristic to auto-assign in the beggining the first few soldiers to first few stations.
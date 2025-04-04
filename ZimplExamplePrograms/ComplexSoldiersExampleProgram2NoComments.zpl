
param NumberOfSoldiers := 25;
param bias := 100;

param planFromDay := "Sunday";
param planFromHour := 0;
param planUntilDay := "Tuesday";
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

defnumb getDay(time) := 
    orderWeekDaysOpposite[(((orderWeekDays[planFromDay] - 1) + floor(time / 24)) mod card(weekDays)) + 1];

defnumb getHour(time) := 
    ((time+ planFromHour) mod 24);

defbool isBetween(fromDay,fromHour,toDay,toHour,targetDay,targetHour) :=
    (orderWeekDays[targetDay] == orderWeekDays[fromDay] and targetHour >= fromHour) or
    (orderWeekDays[targetDay] == orderWeekDays[toDay] and targetHour <= toHour) or
    if orderWeekDays[fromDay] > orderWeekDays[toDay] and targetDay != fromDay and targetDay != toDay
    then (orderWeekDays[targetDay] < orderWeekDays[toDay] or  orderWeekDays[targetDay] > orderWeekDays[fromDay])
    else (orderWeekDays[targetDay] < orderWeekDays[toDay] and  orderWeekDays[targetDay] > orderWeekDays[fromDay])
    end;

set Soldiers := {1..NumberOfSoldiers};
# <name,shift_time,people_in_shift>
set Stations := {<"Siyur1",8,4>,<"FillBox",4,1>,<"Shin Gimel",4,1>, <"Hamal",24,1>, <"Siyur2",8,4>, <"Siyur3",8,4>};  # Station names

set FormalTimes := {<day,hour> in weekDays * hours | isBetween(planFromDay,planFromHour,planUntilDay,planUntilHour,day,hour)};
param planTimeRange := timeDifference(planFromDay,planFromHour,planUntilDay,planUntilHour);
set Times := {0 .. planTimeRange};

set Shifts := {<station, stationInterval, requiredPeople, time> in Stations * Times | time mod stationInterval == 0 and time + stationInterval <= planTimeRange};

set SoldiersToShifts := Soldiers * proj(Shifts,<1,4>);

set ArtificialStation := {<"inv",0,1>};
set ArtificialShift := {<soldier,station,time> in Soldiers * proj(ArtificialStation,<1>) * {-1, card(Times) + 1 } };
set preAssign := {<1,"Siyur1","Sunday",0>, <2,"Siyur1","Sunday",0>, <3,"Siyur1","Sunday",0>, <4,"Siyur1","Sunday",0>,
                <5,"FillBox","Sunday",0>,<6,"FillBox","Sunday",4>, <7,"FillBox","Sunday",8>, <8,"FillBox","Sunday",12>, <9,"FillBox","Sunday",16>};
set encodedPreAssign := {<soldier,station,day,hour> in preAssign: <soldier,station,timeDifference(planFromDay,planFromHour, day, hour)>};                
param AntiPreAssignRate := 0.20;
set antiPreAss := {<soldier,station,time> in SoldiersToShifts | floor(random(0+AntiPreAssignRate,1+AntiPreAssignRate)) == 1} - encodedPreAssign;
var Edge[<i,a,b> in SoldiersToShifts union ArtificialShift] binary; #
var NeighbouringShifts[SoldiersToShifts union ArtificialShift] integer >= 0 <= card(Times)+2 startval 4; #
set FormalShiftsDescription := {<station, stationInterval, requiredPeople, day, hour> in Stations * FormalTimes | timeDifference(planFromDay,planFromHour, day, hour) mod stationInterval == 0 : <station, stationInterval, requiredPeople, convertToPresentation[day], hour>};
var FormalTimesEdges[Soldiers * proj(FormalShiftsDescription,<1,4,5>)] binary; #

subto PreAssign:
    forall<a,b,c> in encodedPreAssign :
        Edge[a,b,c] == 1;

subto AntiPreAssign:
    forall<a,b,c> in antiPreAss :
        Edge[a,b,c] == 0 and NeighbouringShifts[a,b,c] == 0;

subto ConvertEnumeratedTimesToFormal:
    forall <soldier,station,time> in SoldiersToShifts:
        FormalTimesEdges[soldier,station,convertToPresentation[getDay(time)],getHour(time)] == Edge[soldier,station,time];


subto EnforceCalculationOfRestTimes:
    forall <soldier, station, time, joinStation ,shiftInterval, requiredPeople> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) * proj(Stations union ArtificialStation, <1,2,3>) | joinStation == station and time != (card(Times) + 1) and (card({<a,b,c> in encodedPreAssign | b == station and c == time}) < requiredPeople or card({<a,b,c> in encodedPreAssign | a == soldier and b == station and c == time}) >= 1):
        forall <soldier2, station2, time2>  in ((SoldiersToShifts union ArtificialShift) - antiPreAss) | soldier == soldier2 and time2 <= (card(Times) + 1) and (time2 >= min(time + shiftInterval + minimumRestTime, card(Times)+1) or time == -1):
            NeighbouringShifts[soldier,station,time] >= (time2-time-shiftInterval) * Edge[soldier,station,time] * Edge[soldier,station2,time2] * (1 - (sum <soldier3,station3,time3> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) | soldier3 == soldier and time3 > time  and time3 < time2 : Edge[soldier3,station3,time3]));

# Edge = 0 -> NeighbouringShifts = 0                                                      
subto NeighbouringShiftsAndEdgesAreConnected:
    forall <soldier,station,time> in SoldiersToShifts - antiPreAss:
        (1 - Edge[soldier,station,time]) * NeighbouringShifts[soldier,station,time] == 0;


subto EnforceOnlyTwoArtificialShift:
    forall <soldier> in Soldiers:
            (sum <i,station,time> in ArtificialShift | i == soldier: Edge[i,station,time] ) == 2;

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts : 
        Edge[soldier,station,time] * (sum <soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 >= time and time2 < (time + stationInterval) : Edge[soldier2,station2,time2]) <= 1;

subto Enforce_Minimum_Rest_Time_Heuristic:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts : 
        Edge[soldier,station,time] * (sum <soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 >= time + stationInterval and time2 < (time + stationInterval + minimumRestTime) : Edge[soldier2,station2,time2]) == 0;

subto Satisfy_Required_People_For_Shift:
    forall <station, stationInterval, requiredPeople, time> in Shifts : 
        (sum<soldier,station2,time2> in SoldiersToShifts | station == station2 and time == time2 : Edge[soldier,station,time]) == requiredPeople;

set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize myObjective: 
    sum <soldier> in Soldiers : ((sum <i,station,time> in (SoldiersToShifts union ArtificialShift) | i == soldier : ((NeighbouringShifts[i,station,time]+1)**2)));
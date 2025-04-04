
param NumberOfSoldiers := 50;
param bias := 100;

param planFromDay := "Sunday";
param planFromHour := 0;
param planUntilDay := "Monday";
param planUntilHour := 0;

# Not working properly, leave at 0 for now. 
param minimumRestTime := 0;

set weekDays := {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
param orderWeekDays[weekDays] := <"Sunday"> 1 , <"Monday"> 2, <"Tuesday"> 3, <"Wednesday"> 4, <"Thursday"> 5, <"Friday"> 6, <"Saturday"> 7;
param orderWeekDaysOpposite[{1..7}] := <1> "Sunday", <2> "Monday", <3>"Tuesday", <4>"Wednesday", <5>"Thursday", <6>"Friday", <7>"Saturday";
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
# set Stations := {<"Siyur1",8,4>,<"FillBox",4,1>, <"Hamal",24,1>};  # Station names
set Stations := {<"Siyur1",8,4>,<"FillBox",4,1>,<"Shin Gimel",4,1>, <"Hamal",24,1>, <"Siyur2",8,4>, <"Siyur3",8,4>};  # Station names
set FormalTimes := {<day,hour> in weekDays * hours | isBetween(planFromDay,planFromHour,planUntilDay,planUntilHour,day,hour)};
param planTimeRange := timeDifference(planFromDay,planFromHour,planUntilDay,planUntilHour);
set Times := {0 .. planTimeRange};
# do print Times;
set Shifts := {<station, stationInterval, requiredPeople, time> in Stations * Times | time mod stationInterval == 0 and time + stationInterval <= planTimeRange};
#  do print Shifts;
# do print card(Shifts);
                                                                                                                                                                                                                                    #Heuristic
# set FormalPossibleShiftAndRestTimes := {<day,hour,untilDay,untilHour> in  proj(Shifts,<4,5>)* ({proj(Shifts,<4,5>)} union {<planUntilDay,timeDifference(planFromDay,planFromHour,planUntilDay,planUntilHour)+1>}) | timeDifference(day,hour,untilDay,untilHour) < 96 : <day,hour, timeDifference(day,hour,untilDay,untilHour)>};
set PossibleShiftAndRestTimes := {<shiftTime,nextShiftTime> in  proj(Shifts,<4>)* ({proj(Shifts,<4>)} union {planTimeRange+1}) | nextShiftTime >= shiftTime : <shiftTime,nextShiftTime>};
# do print PossibleShiftAndRestTimes;
# do print card(PossibleShiftAndRestTimes);
set SoldiersToShifts := Soldiers * proj(Shifts,<1,4>);
# set FormalShiftSpacings := {<soldier,station,stationInterval,day,hour,day2,hour2,rest> in Soldiers * proj(Shifts,<1,2,4,5>) * PossibleShiftAndRestTimes | day2 == day and hour2 == hour and rest - stationInterval >= 0 : <soldier,station,day,hour,rest - stationInterval>};
set ShiftSpacings := {<soldier,station,stationInterval,shiftStartTime,shiftStartTime2, nextShiftTime> in Soldiers * proj(Shifts,<1,2,4>) * PossibleShiftAndRestTimes | shiftStartTime == shiftStartTime2 and nextShiftTime - shiftStartTime - stationInterval >= minimumRestTime : <soldier,station,stationInterval,shiftStartTime, nextShiftTime - shiftStartTime - stationInterval>};
# do print ShiftSpacings;
# do print card(ShiftSpacings);
set ArtificialShift := {<soldier,station,interval,time,rest> in Soldiers * {<"inv",0>} * ({<time, firstShiftTime> in  {-1}*(Times union {max(Times)+1,max(Times)+2}) : <time, firstShiftTime-time>} union {<max(Times)+1,0>}) };
var Edge[SoldiersToShifts] binary; #
#<soldier,station,stationInterval,shiftStartTime,restAfterShift>
var NeighbouringShifts[ShiftSpacings union ArtificialShift] binary; #
set FormalShifts := {<station, stationInterval, requiredPeople, day, hour> in Stations * FormalTimes | timeDifference(planFromDay,planFromHour, day, hour) mod stationInterval == 0};
# do print FormalShifts;
var FormalTimesEdges[Soldiers * proj(FormalShifts,<1,4,5>)] binary; #
# var FormalTimesNeighbouringShifts[{<soldier,station,time, restAfterShift> in ShiftSpacings: <soldier,station, getDay(time), getHour(time), restAfterShift>} union {<soldier,station,time,rest> in ArtificialShift : <soldier,station, getDay(time), getHour(time), rest>}] binary; #
# do print card(SoldiersToShifts);
# do print card(ShiftSpacings union ArtificialShift);

subto ConvertEnumeratedTimesToFormal:
    forall <soldier,station,time> in SoldiersToShifts:
        FormalTimesEdges[soldier,station,getDay(time),getHour(time)] == Edge[soldier,station,time];

subto EnforceOnlyTwoArtificialShift:
    forall <soldier> in Soldiers:
            (sum <i,station,interval,time,rest> in ArtificialShift | i == soldier: NeighbouringShifts[i,station,interval,time,rest] ) == 2;

do print "loaded EnforceOnlyTwoArtificialShift";

# sum = 0 -> NeighbouringShifts = 1
subto EnforceMaximalRestTimeWhenNoShifts:
    forall <soldier> in Soldiers: 
        (sum <i,a,b> in SoldiersToShifts | i == soldier: Edge[i,a,b]) + NeighbouringShifts[soldier,"inv",0, -1, max(Times)+2] >= 1;

do print "loaded EnforceMaximalRestTimeWhenNoShifts";

# neighb = 1  -> sum * sum >= 1
subto EnforceInitialRest:
        forall <soldier,station,interval,time,rest> in ArtificialShift | station == "inv" and rest >= 1 and rest < max(Times) + 2:
           NeighbouringShifts[soldier,"inv",0, -1, rest] - ((sum <soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 <= (-1 + rest + interval) : Edge[soldier2,station2,time2])  * (sum <soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 == (-1 + rest + interval): Edge[soldier2,station2,time2])) <= 0
            ;

do print "loaded EnforceInitialRest";

subto EnforceEndShift:
    forall <soldier> in Soldiers: 
        NeighbouringShifts[soldier,"inv",0,max(Times)+1,0] == 1;

do print "loaded EnforceEndShift";

# Neighbouring = 1 -> (sum1 >= 1 && sum2 >= 1)
subto EnforceShiftSpacingLogic1:
    forall <soldier,station,interval,time,rest> in ShiftSpacings : 
        NeighbouringShifts[soldier,station,interval,time,rest] -
            ((sum <soldier2,station2,interval2,time2,rest2> in ShiftSpacings union ArtificialShift | soldier2 == soldier and time2 == (time + interval + rest) : NeighbouringShifts[soldier2,station2,interval2,time2,rest2]) ) <= 0;
               
do print "loaded EnforceShiftSpacingLogic1";
                                
# Edge = 1 <-> sum = 1 
subto EnforceShiftSpacingLogic4:
    forall <soldier,station,interval,time,rest> in ShiftSpacings : 
        Edge[soldier,station,time] - (sum <soldier2,station2,interval2,time2,rest2> in ShiftSpacings | soldier2 == soldier and station2 == station and time2 == time : NeighbouringShifts[soldier2,station2,interval2,time2,rest2]) == 0;
                               
do print "loaded EnforceShiftSpacingLogic4";                          

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts : 
        Edge[soldier,station,time] * (sum<soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 >= time and time2 < (time + stationInterval) : Edge[soldier2,station2,time2]) <= 1;

do print "loaded Soldier_Not_In_Two_Stations_Concurrently";

subto Satisfy_Required_People_For_Shift:
    forall <station, stationInterval, requiredPeople, time> in Shifts : 
        (sum<soldier,station2,time2> in SoldiersToShifts | station == station2 and time == time2 : Edge[soldier,station,time]) == requiredPeople;

do print "loaded All_Stations_One_Soldier";

set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize myObjective: 
    sum <soldier> in Soldiers : ((sum <i,station,interval,time,rest> in (ShiftSpacings union ArtificialShift) | i == soldier : (NeighbouringShifts[i,station,interval,time,rest]*(rest+1)**2)) ** 2);
    #+ (sum <i,person,station,time> in  indexSetOfPeople*Shifts | time <= card(Soldiers)/card(Stations)*1: (Edge[person,station,time]*bias*i*(time+1))); # assign first few soldiers by their order in the set

    #TODO: make a nice heuristic to auto-assign in the beggining the first few soldiers to first few stations.
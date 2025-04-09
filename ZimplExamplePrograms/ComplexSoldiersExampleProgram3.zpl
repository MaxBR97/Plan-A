# Changes from last generation: time is represented in minutes, and a significant scalability imporvement
param NumberOfSoldiers := 30;
param bias := 100;

param planFromDay := "Sunday";
param planFromHour := 0;
param planFromMinute := 0;
param planUntilDay := "Monday";
param planUntilHour := 12;
param planUntilMinute := 0;

param minimumRestHours := 0;

set weekDays := {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
param orderWeekDays[weekDays] := <"Sunday"> 1 , <"Monday"> 2, <"Tuesday"> 3, <"Wednesday"> 4, <"Thursday"> 5, <"Friday"> 6, <"Saturday"> 7;
param orderWeekDaysOpposite[{1..7}] := <1> "Sunday", <2> "Monday", <3>"Tuesday", <4>"Wednesday", <5>"Thursday", <6>"Friday", <7>"Saturday";
param convertToPresentation[weekDays] := <"Sunday"> "1 Sunday", <"Monday"> "2 Monday", <"Tuesday"> "3 Tuesday", <"Wednesday"> "4 Wednesday", <"Thursday"> "5 Thursday", <"Friday"> "6 Friday", <"Saturday"> "7 Saturday";
param toString[{0..9}] := <0> "0", <1> "1", <2> "2", <3> "3", <4> "4", <5> "5", <6> "6", <7> "7", <8> "8", <9> "9";
set hours := {0 .. 23};
set minutes := {0 .. 59};

defstrg makeTimeInString(hour,minute) :=
    toString[floor(hour/10) mod 10] + toString[hour mod 10] + ":" + toString[floor(minute/10) mod 10] + toString[minute mod 10];

defnumb HourMinutesDifference(fromHour,fromMinute,toHour,toMinute) :=
    if fromHour == toHour
    then if toMinute >= fromMinute then toMinute-fromMinute else  (card(minutes) * card(hours)) - (fromMinute-toMinute) end
    else
       if fromHour < toHour
        then    (toMinute - fromMinute) +  (card(minutes) * (toHour - fromHour))
        else    (toMinute - fromMinute) +  (card(minutes) * card(hours)) - (card(minutes) * (fromHour - toHour))
       end
    end
    ;

# do print HourMinutesDifference(1,30,2,0); # 30
# do print HourMinutesDifference(1,30,1,30); # 0
# do print HourMinutesDifference(1,30,1,0); # 1410
# do print HourMinutesDifference(1,30,2,40); # 70
# do print HourMinutesDifference(3,30,2,30); # 1380
# do print HourMinutesDifference(3,30,2,40); # 1390
# do print HourMinutesDifference(3,30,2,20); # 1370
# do print "------";

defnumb timeDifference(fromDay, fromHour, fromMinute, toDay, toHour, toMinute) := 
    (
        (orderWeekDays[toDay] * card(hours) * card(minutes) + toHour * card(minutes) + toMinute)
      - (orderWeekDays[fromDay] * card(hours) * card(minutes) + fromHour * card(minutes) + fromMinute)
      + card(weekDays) * card(hours) * card(minutes)   # ensure non-negative by adding a full week's worth
    ) mod (card(weekDays) * card(hours) * card(minutes));

# # # Tests for timeDifference                     | expected
# do print timeDifference("Sunday",18,0,"Sunday",19,0); #60
# do print timeDifference("Sunday",0,30,"Sunday",23,0); #1350
# do print timeDifference("Sunday",4,0,"Sunday",0,40); #9880
# do print timeDifference("Sunday",4,5,"Sunday",0,0); #9835
# do print timeDifference("Sunday",0,0,"Sunday",0,0); #0
# do print "-------";
# do print timeDifference("Sunday",0,0,"Monday",0,0); #1440
# do print timeDifference("Sunday",0,0,"Tuesday",0,0); #2880
# do print timeDifference("Sunday",23,0,"Tuesday",23,0); #2880
# do print timeDifference("Sunday",23,0,"Tuesday",22,0); #2820
# do print timeDifference("Sunday",21,0,"Tuesday",23,0); #3000
# do print timeDifference("Sunday",0,0,"Tuesday",12,0); #3600
# do print "-------";
# do print timeDifference("Saturday",0,0,"Sunday",0,0); #1440
# do print timeDifference("Saturday",12,0,"Sunday",0,0); #720
# do print timeDifference("Saturday",12,0,"Sunday",1,0); #780
# do print timeDifference("Friday",1 ,0,"Monday",1,0); #4320

defnumb getDay(time) := 
    orderWeekDaysOpposite[(((orderWeekDays[planFromDay] - 1) + floor(time / (24 * 60) )) mod card(weekDays)) + 1];
# to test assign planFromDay = "Sunday", planFromHour = 0;
# do print getDay(1); # Sunday;
# do print getDay(23); # Sunday;
# do print getDay(card(minutes)* card(hours)); # Monday;
# do print getDay(card(minutes)* card(hours) * 7); # Sunday;
# do print getDay(card(minutes)* card(hours) * 6); # Saturday;


defnumb getHour(time) := 
    (
        floor(((planFromHour * card(minutes)) + planFromMinute + time) 
        / card(minutes))
    ) mod card(hours);


# to test, assign planFromHour = 6;
# do print getHour(0); # 6;
# do print getHour(60); # 7;
# do print getHour(24*60); # 6;
# do print getHour(48*60); # 6;
# do print getHour(12*60); # 18;
# do print getHour(24*7*60); # 6;
# do print getHour((24*7*60) - 1); # 5;

defnumb getMinute(time) := 
    (planFromMinute + time) mod card(minutes);

defnumb convertToMinutesRepresentation(day, hour, minute) :=
    ((orderWeekDays[day] - 1) * 24 * 60) + (hour * 60) + minute;

defbool isBetween(fromTime, toTime, targetTime) := 
    if fromTime <= toTime
    then (targetTime >= fromTime and targetTime <= toTime)
    else (targetTime >= fromTime or targetTime <= toTime)
    end;



# Tests for isBetween                                   | expected
# do print isBetweenDayHourMinute("Friday",18,23,"Sunday",8,6,"Sunday",12,15); # false
# do print isBetweenDayHourMinute("Friday",18,2,"Sunday",8,1,"Sunday",7,0); # true
# do print isBetweenDayHourMinute("Friday",18,7,"Sunday",8,34,"Saturday",12,0); # true
# do print isBetweenDayHourMinute("Friday",18,53,"Sunday",8,13,"Friday",12,21); # false
# do print isBetweenDayHourMinute("Friday",18,0,"Sunday",8,0,"Friday",20,54); # true
# do print isBetweenDayHourMinute("Friday",18,0,"Sunday",8,0,"Monday",12,5); # false
# do print isBetweenDayHourMinute("Sunday",18,0,"Friday",8,0,"Saturday",12,12); # false
# do print isBetweenDayHourMinute("Sunday",18,0,"Friday",8,0,"Monday",12,32); # true
# do print isBetweenDayHourMinute("Sunday",18,0,"Friday",8,30,"Friday",8,32); # false
# do print isBetweenDayHourMinute("Sunday",18,0,"Friday",8,30,"Friday",8,29); # true


defbool isBetweenDayHourMinute(fromDay, fromHour, fromMinute, toDay, toHour, toMinute, targetDay, targetHour, targetMinute) := 
    isBetween(
        convertToMinutesRepresentation(fromDay, fromHour, fromMinute),
        convertToMinutesRepresentation(toDay, toHour, toMinute),
        convertToMinutesRepresentation(targetDay, targetHour, targetMinute)
    );

set Soldiers := {1..NumberOfSoldiers};
# <name,shift_time,people_in_shift>
# set Stations := {<"Siyur1",8,4>,<"FillBox",4,1>,<"Shin Gimel",4,1>, <"Hamal",16.5,1>, <"Siyur2",8,4>, <"Siyur3",8,4>};  # Station names
set Stations := {<"Siyur1",8,4>,<"FillBox",4,1>,<"a",16.5,1>};  # Station names
set FormalTimes := {<day,hour,minute> in weekDays * hours * minutes | isBetweenDayHourMinute(planFromDay,planFromHour,planFromMinute, planUntilDay,planUntilHour, planUntilMinute, day,hour, minute)};
param planTimeRange := timeDifference(planFromDay,planFromHour,planFromMinute, planUntilDay,planUntilHour, planUntilMinute);
set Times := {0 .. planTimeRange};

set Shifts := {<station, stationInterval, requiredPeople, time> in Stations * Times | time mod (stationInterval*card(minutes)) == 0 and time + (stationInterval*card(minutes)) <= planTimeRange};

set SoldiersToShifts := Soldiers * proj(Shifts,<1,4>);

set ArtificialStation := {<"inv",0,1>};
set ArtificialShift := {<soldier,station,time> in Soldiers * proj(ArtificialStation,<1>) * {-1, card(Times) + 1 } };
set preAssign := {<1,"Siyur1","Sunday",0,0>, <2,"Siyur1","Sunday",0,0>, <3,"Siyur1","Sunday",0,0>, <4,"Siyur1","Sunday",0,0>,
                <5,"FillBox","Monday",0,0>,<6,"FillBox","Sunday",8,0>, <7,"FillBox","Sunday",12,0>, <8,"FillBox","Sunday",16,0>, <9,"FillBox","Sunday",20,0>};
set encodedPreAssign := {<soldier,station,day,hour,minute> in preAssign: <soldier,station,timeDifference(planFromDay,planFromHour, planFromMinute, day, hour, minute)>};                
param AntiPreAssignRate := 0.0;
set antiPreAss := {<soldier,station,time> in SoldiersToShifts | floor(random(0+AntiPreAssignRate,1+AntiPreAssignRate)) == 1} - encodedPreAssign;
var Edge[<i,a,b> in SoldiersToShifts union ArtificialShift] binary; #
var NeighbouringShifts[SoldiersToShifts union ArtificialShift] real >= 0 <= (card(Times)/60)+2; #
set FormalShiftsDescription := {<station, stationInterval, requiredPeople, day, hour, minute> in Stations * FormalTimes | timeDifference(planFromDay,planFromHour, planFromMinute, day, hour, minute) mod stationInterval == 0 : <station, stationInterval, requiredPeople, convertToPresentation[day], makeTimeInString(hour,minute)>};
var FormalTimesEdges[Soldiers * proj(FormalShiftsDescription,<1,4,5>)] binary; #
# set labelsForShiftStatistics := {"Problem size (people * stations * times)", "Total Shifts Assigned", "Average Shifts Per Person", "Average Rest Time", "People Not Assigned Atleast Once"}
# var ShiftStatistics[labels] integer;

subto PreAssign:
    forall<a,b,c> in encodedPreAssign :
        Edge[a,b,c] == 1;

subto AntiPreAssign:
    forall<a,b,c> in antiPreAss :
        Edge[a,b,c] == 0 and NeighbouringShifts[a,b,c] == 0;

subto ConvertEnumeratedTimesToFormal:
    forall <soldier,station,time> in SoldiersToShifts:
        FormalTimesEdges[soldier,station,convertToPresentation[getDay(time)],makeTimeInString(getHour(time), getMinute(time))] == Edge[soldier,station,time];

defnumb nextKnownShift(soldier,time) :=
    if card({<so,st,t> in encodedPreAssign | soldier == so and time < t}) == 0 
    then card(Times)+1
    else min <sol,station, t> in encodedPreAssign | t > time and soldier == sol: t end;

do print card({<soldier, station, time, joinStation ,shiftInterval, requiredPeople> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) * proj(Stations union ArtificialStation, <1,2,3>) | joinStation == station and time != (card(Times) + 1) and (card({<a,b,c> in encodedPreAssign | b == station and c == time}) < requiredPeople or card({<a,b,c> in encodedPreAssign | a == soldier and b == station and c == time}) >= 1)});


# subto EnforceCalculationOfRestTimes1:
#     forall <soldier, station, time, joinStation ,shiftInterval, requiredPeople> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) * proj(Stations union ArtificialStation, <1,2,3>) | joinStation == station and time != (card(Times) + 1) and (card({<a,b,c> in encodedPreAssign | b == station and c == time}) < requiredPeople or card({<a,b,c> in encodedPreAssign | a == soldier and b == station and c == time}) >= 1):
#         forall <soldier2, station2, time2>  in ((SoldiersToShifts union ArtificialShift) - antiPreAss) | soldier == soldier2 and time2 <= (card(Times) + 1) and (time2 >= min(time + (shiftInterval + minimumRestHours)*60, card(Times)+1) or time == -1) :
#             NeighbouringShifts[soldier,station,time] >= ((time2-time-(shiftInterval*60)) / 60) * Edge[soldier,station,time] * Edge[soldier,station2,time2] * (1 - (sum <soldier3,station3,time3> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) | soldier3 == soldier and time3 > time  and time3 < time2 : Edge[soldier3,station3,time3]));
        
# do print "loaded EnforceCalculationOfRestTimes1";

subto EnforceCalculationOfRestTimes1:
    forall <soldier, station, time, joinStation ,shiftInterval, requiredPeople> in ((SoldiersToShifts union ArtificialShift) - antiPreAss) * proj(Stations union ArtificialStation, <1,2,3>) | joinStation == station and time != (card(Times) + 1) and (card({<a,b,c> in encodedPreAssign | b == station and c == time}) < requiredPeople or card({<a,b,c> in encodedPreAssign | a == soldier and b == station and c == time}) >= 1):
        forall <soldier2, station2, time2>  in ((SoldiersToShifts union ArtificialShift) - antiPreAss) | soldier == soldier2 and time2 <= (card(Times) + 1) and (time2 >= min(time + (shiftInterval + minimumRestHours)*60, card(Times)+1) or time == -1) :
            NeighbouringShifts[soldier,station,time] <=  (((time2-time-(shiftInterval*60)) / 60) * Edge[soldier,station,time] * Edge[soldier,station2,time2] ) +  ((1-(Edge[soldier,station,time] * Edge[soldier,station2,time2]))*170);
        
do print "loaded EnforceCalculationOfRestTimes1";

# subto try:
#     NeighbouringShifts[1,"Siyur1",0] >= 1;

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
        Edge[soldier,station,time] * (sum <soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 >= time and time2 < (time + (stationInterval*60)) : Edge[soldier2,station2,time2]) <= 1;

subto Enforce_Minimum_Rest_Time_Heuristic:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts : 
        Edge[soldier,station,time] * (sum <soldier2,station2,time2> in SoldiersToShifts | soldier2 == soldier and time2 >= time + stationInterval and time2 < (time + (stationInterval + minimumRestHours)*60) : Edge[soldier2,station2,time2]) == 0;

do print "loaded Enforce_Minimum_Rest_Time_Heuristic";

subto Satisfy_Required_People_For_Shift:
    forall <station, stationInterval, requiredPeople, time> in Shifts : 
        (sum<soldier,station2,time2> in SoldiersToShifts | station == station2 and time == time2 : Edge[soldier,station,time]) == requiredPeople;

do print "loaded All_Stations_One_Soldier";

set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}
param x:=(card(Times)/60)+2;
minimize myObjective: 
    sum <soldier> in Soldiers : (sum <i,station,time> in (SoldiersToShifts union ArtificialShift) | i == soldier : ((x - NeighbouringShifts[i,station,time])**2));
    # - (sum <soldier,station,time> in (SoldiersToShifts union ArtificialShift) : (NeighbouringShifts[soldier,station,time]+1)**2) ;
    # + (sum <i,person,station,time> in  indexSetOfPeople*Shifts | time <= card(Soldiers)/card(Stations)*1: (Edge[person,station,time]*bias*i*(time+1))); # assign first few soldiers by their order in the set

    #TODO: make a nice heuristic to auto-assign in the beggining the first few soldiers to first few stations.
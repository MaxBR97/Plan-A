# Changes from last generation: time is represented in minutes, and a significant scalability imporvement
param planFromDay := "Sunday";
param planFromHour := 0;
param planFromMinute := 0;
param planUntilDay := "Wednesday";
param planUntilHour := 7;
param planUntilMinute := 0;

param minutesFragmentation := 30;
param minimumRestHours := 0;
param nightTimeStartHour := 23;
param nightTimeEndHour := 23;

set weekDays := {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
param orderWeekDays[weekDays] := <"Sunday"> 1 , <"Monday"> 2, <"Tuesday"> 3, <"Wednesday"> 4, <"Thursday"> 5, <"Friday"> 6, <"Saturday"> 7;
param orderWeekDaysOpposite[{1..7}] := <1> "Sunday", <2> "Monday", <3>"Tuesday", <4>"Wednesday", <5>"Thursday", <6>"Friday", <7>"Saturday";
param convertToPresentation[weekDays] := <"Sunday"> "1 Sunday", <"Monday"> "2 Monday", <"Tuesday"> "3 Tuesday", <"Wednesday"> "4 Wednesday", <"Thursday"> "5 Thursday", <"Friday"> "6 Friday", <"Saturday"> "7 Saturday";
param toString[{0..9}] := <0> "0", <1> "1", <2> "2", <3> "3", <4> "4", <5> "5", <6> "6", <7> "7", <8> "8", <9> "9";
set hours := {0 .. 23};
set minutes := {0 .. 59 by minutesFragmentation};

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
    end;

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
    orderWeekDaysOpposite[(((orderWeekDays[planFromDay] - 1) + floor((time*minutesFragmentation) / (24 * 60) )) mod card(weekDays)) + 1];


defnumb getHour(time) := 
    (
        floor(((planFromHour * 60) + planFromMinute + (time*minutesFragmentation)) 
        / 60)
    ) mod card(hours);

defnumb getMinute(time) := 
    (planFromMinute + time*minutesFragmentation) mod 60;

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

#hard coded squads - each person belongs to one of the squads
set Squads := {"1a","1b","1c","2a","2b","2c","none"};
#hard coded roles, each person has one.
set AvailableRoles := {"Hapash","Medic","Commander","Officer","none"};
#<Name,Squad,Role>
set People := {
    <"Empty Soldier","none","none">,<"Max","1a","Hapash">, <"Erel","none","Medic">,
    <"Yoni","none","none">,<"Tal","1a","Hapash">, <"Oded","none","Medic">,
    <"Avi","none","none">,<"Nir","1a","Hapash">, <"Gadi","none","Medic">,
    <"Gal","none","none">,<"Zinger","1a","Officer">, <"Koplovich","none","Medic">,
    <"Shahor","none","Commander">,<"Denis","1a","Officer">, <"Vayl","none","none">,
    <"Melamed","none","Hapash">,<"Cohen Tov","1a","Hapash">, <"Zikri","none","Hapash">
};
#<station_name, required_people,FromHour,FromMinute,UntilHour,UntilMinute, default_shift_duration_hours, default_shift_duration_minutes>
set Everyday_Missions := {
    <"Patrol",1,6,0,22,30,1,30>
    # , <"Night Patrol",2,22,00,6,00,1,0> , <"Kitchen",1,6,0,20,00,14,0>
    };
#<station_name, required_people,FromDay,FromHour,FromMinute,UntilDay,UntilHour,UntilMinute>
set OneTime_Missions := {
    <"Avodot Rasar",3,"Sunday",0,0,"Monday",2,0>
    };

#<Person_name,FromDay,FromHour,FromMinute>
set PeopleAllowedToBeAssignedFrom := {<"Empty Soldier","Sunday",0,0>};
#<Person_name,Until,UntilHour,UntilMinute>
set PeopleAllowedToBeAssignedUntil := {<"Empty Soldier","Saturday",23,59>};


param planTimeRange := timeDifference(planFromDay,planFromHour,planFromMinute, planUntilDay,planUntilHour, planUntilMinute);
set Times := {0 .. planTimeRange};

# --- Derived sets and parameters ---

#<name>
set AllMissions := proj(Everyday_Missions,<1>) union proj(OneTime_Missions,<1>);

# Create combined missions set with standard structure:
# <name, requiredPeople, time_quant>
set AllShiftQuants := 
    {<station_name, required_people,fromHour,fromMinute,untilHour,untilMinute, default_shift_duration_hours, default_shift_duration_minutes, day, time_quant> in Everyday_Missions * weekDays * Times |
                                                                isBetweenDayHourMinute(day,fromHour,fromMinute,day,untilHour,untilMinute,getDay(time_quant),getHour(time_quant),getMinute(time_quant)) and
                                                                (convertToMinutesRepresentation(day,untilHour, untilMinute) > time_quant*minutesFragmentation) and
                                                                (convertToMinutesRepresentation(planUntilDay,planUntilHour, planUntilMinute) > time_quant*minutesFragmentation) and
                                                                ((orderWeekDays[day] >= orderWeekDays[planFromDay] and orderWeekDays[day] <= orderWeekDays[planUntilDay] and orderWeekDays[planFromDay] <= orderWeekDays[planUntilDay] ) or (orderWeekDays[day] >= orderWeekDays[planFromDay] and orderWeekDays[day] <= orderWeekDays[planUntilDay] and orderWeekDays[planFromDay] > orderWeekDays[planUntilDay])) : <station_name, required_people,time_quant>} union 
    {<station_name, required_people,fromDay,fromHour,fromMinute,untilDay,untilHour,untilMinute,time_quant> in OneTime_Missions * Times | 
                                                                isBetweenDayHourMinute(fromDay,fromHour,fromMinute,untilDay,untilHour,untilMinute,getDay(time_quant),getHour(time_quant),getMinute(time_quant)) and
                                                                (convertToMinutesRepresentation(planUntilDay,planUntilHour, planUntilMinute) > time_quant*minutesFragmentation) and
                                                                (convertToMinutesRepresentation(untilDay,untilHour, untilMinute) > time_quant*minutesFragmentation) : <station_name, required_people,time_quant>};

do print AllShiftQuants;

# <person,shift_name, time>
set PeopleToShifts := proj(People,<1>) * proj(AllShiftQuants,<1,3>);
set preAssign := {
    <"Max","Patrol","Sunday",0,0>,
     <"Max","Patrol","Sunday",0,5>
                };
# <soldier,station,minutes>
set encodedPreAssign := {<soldier,station,day,hour,minute> in preAssign: <soldier,station,timeDifference(planFromDay,planFromHour, planFromMinute, day, hour, minute)>};                

var AllShiftsDurations[proj(AllShiftQuants,<1,3>)] integer >= 0 <= card(Times) + 1;
var Assignments[PeopleToShifts] binary;
var AllRestDurations[PeopleToShifts] integer >= 0 <= card(Times) + 1;
var ShiftInterval[AllMissions] integer >= 1 <= 5;

var mid [proj(PeopleToShifts,<2,3>)] integer;

subto x:
     forall <p, station, time> in PeopleToShifts :
        
        mid[station,time] == (sum <person2,station2,time2> in PeopleToShifts | station == station2 and time == time2 : Assignments[person2,station2,time2]);

subto Dictate_Shift_Lengths:
    forall <soldier, station, time> in PeopleToShifts :
        AllShiftsDurations[station,time] * ( AllShiftsDurations[station,time] -ShiftInterval[station]) == 0;

do print "loaded Dictate_Shift_Lengths";

subto Dictate_Shift_Lengths2:
    forall <soldier, station, time> in PeopleToShifts :
        Assignments[soldier,station,time] - AllShiftsDurations[station,time] <= 0;

do print "loaded Dictate_Shift_Lengths2";

subto Satisfy_AllShifts:
    (sum <station, required_people, time> in AllShiftQuants : 
        (AllShiftsDurations[station,time] * required_people) ) == card(AllShiftQuants);

subto Satisfy_Required_People_For_Shift:
    forall <station, required_people, time> in AllShiftQuants :
        mid[station,time] * (mid[station,time] - required_people) == 0;

do print "loaded Satisfy_Required_People_For_Shift";

subto calculateRest:
    forall <soldier, station, time> in PeopleToShifts:
        forall <soldier2, station2, time2> in PeopleToShifts | soldier == soldier2 and time2 > time :
            AllRestDurations[soldier,station,time] <= (((time2-time-(AllShiftsDurations[station,time]))) * Assignments[soldier,station,time] * Assignments[soldier,station2,time2]);
        
do print "loaded calculateRest";

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <soldier, station, time> in PeopleToShifts :
        forall <soldier2, station2, time2> in PeopleToShifts | soldier == soldier2 and time2 > time :
            AllShiftsDurations[station,time] * Assignments[soldier2,station2,time2] <= (time2 - time);

do print "loaded Soldier_Not_In_Two_Stations_Concurrently";


minimize obj:
    sum <station, time> in proj(PeopleToShifts,<2,3>) : (card(Times) - AllShiftsDurations[station,time]) +
    sum <soldier, station, time> in PeopleToShifts : (card(Times) - AllRestDurations[soldier,station,time]);
    
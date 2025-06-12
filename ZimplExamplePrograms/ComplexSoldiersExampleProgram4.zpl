# Fixed scheduler with improved week boundary handling and relative day numbering

set weekDays := {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
param orderWeekDays[weekDays] := <"Sunday"> 1 , <"Monday"> 2, <"Tuesday"> 3, <"Wednesday"> 4, <"Thursday"> 5, <"Friday"> 6, <"Saturday"> 7;
param orderWeekDaysOpposite[{1..7}] := <1> "Sunday", <2> "Monday", <3> "Tuesday", <4> "Wednesday", <5> "Thursday", <6> "Friday", <7> "Saturday";
param toString[{0..9}] := <0> "0", <1> "1", <2> "2", <3> "3", <4> "4", <5> "5", <6> "6", <7> "7", <8> "8", <9> "9";
param stringToNumber[{"0","1","2","3","4","5","6","7","8","9"}] := <"0"> 0, <"1"> 1, <"2"> 2, <"3"> 3, <"4"> 4, <"5"> 5, <"6"> 6, <"7"> 7, <"8"> 8, <"9"> 9;

set hours := {0 .. 23};
set minutes := {0 .. 59};

defnumb convertStringToNumber(str) := 
    sum <i> in {0..length(str)-1} : (stringToNumber[substr(str,length(str) - 1 - i,1)] * (10**i));

# 13, 40 -> "13:40"
defstrg makeTimeInString(hour,minute) :=
    toString[floor(hour/10) mod 10] + toString[hour mod 10] + ":" + toString[floor(minute/10) mod 10] + toString[minute mod 10];

#"13:40" -> 40 , "13:02" -> 2 , "13:2" -> 2
defnumb getMinuteFromStringFormat(str) :=
    if length(str) == 5 then convertStringToNumber(substr(str,3,2)) else
        if substr(str,2,1) == ":" then convertStringToNumber(substr(str,3,2)) else convertStringToNumber(substr(str,2,2)) end 
    end;

#"13:40" -> 13  , "1:20" -> 1
defnumb getHourFromStringFormat(str) :=
    if length(str) == 5 then convertStringToNumber(substr(str,0,2)) else
        if substr(str,3,1) == ":" then convertStringToNumber(substr(str,0,2)) else convertStringToNumber(substr(str,0,1)) end 
    end;


defnumb validateTimeInStringFormat(str) := 
    if length(str) == 5 and 
        (sum <i> in {"0","1","2","3","4","5","6","7","8","9"} | i == substr(str,0,1) : 1) == 1 and
        (sum <i> in {"0","1","2","3","4","5","6","7","8","9"} | i == substr(str,1,1) : 1) == 1 and
        substr(str,2,1) == ":" and
        (sum <i> in {"0","1","2","3","4","5","6","7","8","9"} | i == substr(str,3,1) : 1) == 1 and
        (sum <i> in {"0","1","2","3","4","5","6","7","8","9"} | i == substr(str,4,1) : 1) == 1 and
        convertStringToNumber(substr(str,0,2)) < 24 and
        convertStringToNumber(substr(str,3,2)) < 60
    then 1 else 0 end;

param planFromTimeFormal := "00:00";
param planFromDay := "Sunday";
param planFromHour := getHourFromStringFormat(planFromTimeFormal);
param planFromMinute := getMinuteFromStringFormat(planFromTimeFormal);
param planUntilTimeFormal := "20:20";
param planUntilDay := "Monday";
param planUntilHour := getHourFromStringFormat(planUntilTimeFormal);
param planUntilMinute := getMinuteFromStringFormat(planUntilTimeFormal);
param nightTimeStart := "21:00";
param nightTimeEnd := "07:00";
param nightTimeStartHour := getHourFromStringFormat(nightTimeStart);
param nightTimeStartMinute := getMinuteFromStringFormat(nightTimeStart);
param nightTimeEndHour := getHourFromStringFormat(nightTimeEnd);
param nightTimeEndMinute := getMinuteFromStringFormat(nightTimeEnd);
param minimumRestHours := 0;

do print "Planning days range must be valid!";
do forall <day> in {planFromDay,planUntilDay} do check
    card({<day> in weekDays}) == 1;

do print "Planning time range must be valid, format is HH:MM !";
do forall <time> in {planFromTimeFormal,planUntilTimeFormal,nightTimeStart,nightTimeEnd} do check
    validateTimeInStringFormat(time) == 1;

do print "Minimum rest hours must be greater than or equal to 0!";
do check minimumRestHours >= 0;


# Define relative day numbering based on planFromDay
defnumb getDayNumber(day) := 
    ((orderWeekDays[day] - orderWeekDays[planFromDay] + 7) mod 7) + 1;

# Validate that a string matches the format "x Weekday" where x is the relative day number
defnumb validateDayPresentation(str) :=
    if length(str) < 3 then 0 else  # Minimum length check (e.g., "1 M")
        if substr(str,1,1) != " " then 0 else  # Check space after number
            if card({<substr(str,2,length(str)-2)> in weekDays}) != 1 then 0 else  # Check if weekday is valid
                if card({<substr(str,0,1)> in {"1","2","3","4","5","6","7","8","9"}}) != 1 then 0 else  # Check if number is valid
                    if convertStringToNumber(substr(str,0,1)) != getDayNumber(substr(str,2,length(str)-2)) then 0 else  # Check if number matches day
                        1
                    end
                end
            end
        end
    end;

# Update convertToPresentation to use relative day numbering
param convertToPresentation[<day> in weekDays] := 
    toString[getDayNumber(day)] + " " + day;


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

# Improved time difference calculation that properly handles week boundaries
defnumb timeDifference(fromDay, fromHour, fromMinute, toDay, toHour, toMinute) := 
    (
        (orderWeekDays[toDay] * card(hours) * card(minutes) + toHour * card(minutes) + toMinute)
      - (orderWeekDays[fromDay] * card(hours) * card(minutes) + fromHour * card(minutes) + fromMinute)
      + card(weekDays) * card(hours) * card(minutes)   # ensure non-negative by adding a full week's worth
    ) mod (card(weekDays) * card(hours) * card(minutes));

# Fixed getDay function that properly handles week transitions
defnumb getDay(time) := 
    orderWeekDaysOpposite[(((orderWeekDays[planFromDay] - 1) + floor((time + planFromHour*60 + planFromMinute) / (24 * 60) )) mod card(weekDays)) + 1];

defnumb getHour(time) := 
    (
        floor(((planFromHour * card(minutes)) + planFromMinute + time) 
        / card(minutes))
    ) mod card(hours);

defnumb getMinute(time) := 
    (planFromMinute + time) mod card(minutes);

# Convert day/hour/minute to total minutes representation for easier comparison
defnumb convertToMinutesRepresentation(day, hour, minute) :=
    ((orderWeekDays[day] - 1) * 24 * 60) + (hour * 60) + minute;

# Improved isBetween function that handles circular ranges
defbool isBetween(fromTime, toTime, targetTime) := 
    if fromTime <= toTime
    then (targetTime >= fromTime and targetTime <= toTime)
    else (targetTime >= fromTime or targetTime <= toTime)
    end;

# Improved function to check if a time point is between two other time points
defbool isBetweenDayHourMinute(fromDay, fromHour, fromMinute, toDay, toHour, toMinute, targetDay, targetHour, targetMinute) := 
    
    # Handle week wrapping - if toDay comes before fromDay in the week
    if orderWeekDays[toDay] < orderWeekDays[fromDay] or 
       (orderWeekDays[toDay] == orderWeekDays[fromDay] and 
        (toHour < fromHour or (toHour == fromHour and toMinute < fromMinute)))
    then 
        # Target is either after fromTime or before toTime (across week boundary)
        convertToMinutesRepresentation(targetDay, targetHour, targetMinute) >= convertToMinutesRepresentation(fromDay, fromHour, fromMinute) or convertToMinutesRepresentation(targetDay, targetHour, targetMinute) <= convertToMinutesRepresentation(toDay, toHour, toMinute)
    else
        # Normal case - target time should be between fromTime and toTime
        convertToMinutesRepresentation(targetDay, targetHour, targetMinute) >= convertToMinutesRepresentation(fromDay, fromHour, fromMinute) and convertToMinutesRepresentation(targetDay, targetHour, targetMinute) <= convertToMinutesRepresentation(toDay, toHour, toMinute)
    end;

defbool isBetweenHourMinute(fromHour,fromMinute,toHour,toMinute,targetHour,targetMinute) :=
    isBetweenDayHourMinute("Sunday",fromHour,fromMinute,"Sunday",toHour,toMinute,"Sunday",targetHour,targetMinute);

defnumb CommonTimeDuration(fromHour, fromMinute, toHour, toMinute, fromHour2, fromMinute2, toHour2, toMinute2) :=
    if isBetweenHourMinute(fromHour, fromMinute, toHour, toMinute,  fromHour2, fromMinute2) and isBetweenHourMinute(fromHour, fromMinute, toHour, toMinute,  toHour2, toMinute2) and HourMinutesDifference(fromHour2,fromMinute2,toHour2,toMinute2) >= 12*60
    then HourMinutesDifference(fromHour2,fromMinute2,toHour,toMinute) + (HourMinutesDifference(fromHour,fromMinute,toHour2,toMinute2))
    else if isBetweenHourMinute(fromHour, fromMinute, toHour, toMinute,  fromHour2, fromMinute2)
    then min(HourMinutesDifference(fromHour2,fromMinute2,toHour,toMinute), HourMinutesDifference(fromHour2,fromMinute2,toHour2,toMinute2))
    else if isBetweenHourMinute(fromHour, fromMinute, toHour, toMinute, toHour2, toMinute2)
    then min(HourMinutesDifference(fromHour,fromMinute,toHour2,toMinute2))
    else 0 
    end end end;

#hard coded squads - each person belongs to one of the squads
set Squads := {"1a","1b","1c","2a","2b","2c","none"};
#hard coded roles, each person has one.
set AvailableRoles := {"Hapash","Medic","Commander","Officer","none"};
#<Name,Squad,Role>
set Descriptive_Soldiers_List := {
    <"Empty Soldier","none","none">,<"Max","1a","Hapash">, <"Erel","none","Medic">, <"Sheshar","none","Hapash">,
    <"Yoni","none","none">,<"Tal","1a","Hapash">, <"Oded","none","Medic">, <"Moshe", "none", "Hapash">
     ,<"Avi","none","none">,<"Nir","1a","Hapash">, <"Gadi","none","Medic">, <"Dekel","none","Commander">
    # ,<"Gal","none","none">,<"Zinger","1a","Officer">, <"Koplovich","none","Medic">, <"Arsen","none","Commander">,
    # <"Shahor","none","Commander">,<"Denis","1a","Officer">, <"Vayl","none","none">,<"Zalsman","none","Commander">,
    # <"Melamed","none","Hapash">,<"Cohen Tov","1a","Hapash">, <"Zikri","none","Hapash">,<"Hason","none","Commander">,
    # <"Navon","none","Officer">,<"Yedidya","1a","Hapash">, <"Yuri","none","Commander">,<"Tsefler","none","Hapash">
};
do print "Soldiers list must not be empty!";
do check card(Descriptive_Soldiers_List) > 0;

do print "Soldier names must be unique!";
do forall <soldier,team,role> in Descriptive_Soldiers_List do check
    sum <soldier2,team2,role2> in Descriptive_Soldiers_List | soldier2 == soldier: 1 == 1;

do print "Descriptive soldiers list must be valid - team and role must match the declared lists!";
do forall <soldier, team , role> in Descriptive_Soldiers_List do check
    card({<team> in Squads}) == 1 and card({<role> in AvailableRoles}) == 1;

set Soldiers := proj(Descriptive_Soldiers_List,<1>);
#<station_name, required_people,FromStringTime,UntilStringTime,StringDurationTime>
set Everyday_Missions_Formal := {
    <"Patrol",1,"06:00","22:00","01:30">
    , <"Night Patrol",2,"22:00","06:00","01:00">, <"Kitchen",1,"06:00","20:00","14:00">
    };
#<station_name, required_people,FromHour,FromMinute,UntilHour,UntilMinute, default_shift_duration_hours, default_shift_duration_minutes>
set Everyday_Missions := {<station_name, required_people,FromStringTime,UntilStringTime,StringDurationTime> in Everyday_Missions_Formal :
<station_name,required_people,getHourFromStringFormat(FromStringTime), getMinuteFromStringFormat(FromStringTime),getHourFromStringFormat(UntilStringTime),getMinuteFromStringFormat(UntilStringTime),getHourFromStringFormat(StringDurationTime),getMinuteFromStringFormat(StringDurationTime)>};

#<station_name, required_people,FromDay,FromTime,UntilDay,UntilTime, DurationTime>
set OneTime_Missions_Formal := {<"Avodot Rasar",2,"Sunday","00:00","Sunday","00:00","01:00">};
#<station_name, required_people,FromDay,FromHour,FromMinute,UntilDay,UntilHour,UntilMinute, default_shift_duration_hours, default_shift_duration_minutes>
set OneTime_Missions := {
    <station_name,requiredPpl,fromDay,fromTimeString,untilDay,untilTimeString,duration> in OneTime_Missions_Formal:
    <station_name,requiredPpl,fromDay,getHourFromStringFormat(fromTimeString),getMinuteFromStringFormat(fromTimeString),untilDay,getHourFromStringFormat(untilTimeString),getMinuteFromStringFormat(untilTimeString),getHourFromStringFormat(duration),getMinuteFromStringFormat(duration)>
    };

do print "Mission times must be valid - Enter valid weekdays and times in HH:MM format, required people must be greater than 0!";
do forall <station_name,requiredPpl,from_day,from_time,until_day,until_time, duration> in OneTime_Missions_Formal do check
    validateTimeInStringFormat(from_time) == 1 and validateTimeInStringFormat(until_time) == 1 and validateTimeInStringFormat(duration) == 1 and card({<from_day> in weekDays}) == 1 and card({<until_day> in weekDays}) == 1 and requiredPpl > 0;
do forall <station_name,requiredPpl,from_time,until_time, duration> in Everyday_Missions_Formal do check
    validateTimeInStringFormat(from_time) == 1 and validateTimeInStringFormat(until_time) == 1 and validateTimeInStringFormat(duration) == 1 and requiredPpl > 0;

set PeopleAllowedToBeAssignedFromFormal := {<"Empty Soldier","Sunday","00:00">};
set PeopleAllowedToBeAssignedUntilFormal := {<"Empty Soldier","Sunday","00:00">};

do print "People allowed to be assigned from/to must be valid - Enter valid names (from the declared list), weekdays and times in HH:MM format!";
do forall <soldier,day,time> in PeopleAllowedToBeAssignedFromFormal union PeopleAllowedToBeAssignedUntilFormal do check
    validateTimeInStringFormat(time) == 1 and card({<day> in weekDays}) == 1 and card({<soldier> in Soldiers}) == 1;
#<Person_name,FromDay,FromHour,FromMinute>
set PeopleAllowedToBeAssignedFrom := {<soldier,day,stringTime> in PeopleAllowedToBeAssignedFromFormal: <soldier,day,getHourFromStringFormat(stringTime),getMinuteFromStringFormat(stringTime)>};
#<Person_name,Until,UntilHour,UntilMinute>
set PeopleAllowedToBeAssignedUntil := {<soldier,day,stringTime> in PeopleAllowedToBeAssignedUntilFormal: <soldier,day,getHourFromStringFormat(stringTime),getMinuteFromStringFormat(stringTime)>};

#<station,station_interval,required_ppl>
set AllMissions :={<station,interval_hours,interval_minutes,required_people> in proj(Everyday_Missions,<1,7,8,2>) union proj(OneTime_Missions,<1,9,10,2>) : <station,interval_hours*60 + interval_minutes ,required_people>};

# Improved FormalTimes definition that properly handles planning across week boundaries
set FormalTimes := {<day,hour,minute> in weekDays * hours * minutes | 
    if convertToMinutesRepresentation(planFromDay, planFromHour, planFromMinute) <= convertToMinutesRepresentation(planUntilDay, planUntilHour, planUntilMinute) 
    then (convertToMinutesRepresentation(day, hour, minute) >= convertToMinutesRepresentation(planFromDay, planFromHour, planFromMinute) and convertToMinutesRepresentation(day, hour, minute) <= convertToMinutesRepresentation(planUntilDay, planUntilHour, planUntilMinute))
    else (convertToMinutesRepresentation(day, hour, minute) >= convertToMinutesRepresentation(planFromDay, planFromHour, planFromMinute) or convertToMinutesRepresentation(day, hour, minute) <= convertToMinutesRepresentation(planUntilDay, planUntilHour, planUntilMinute))
    end
};

param planTimeRange := timeDifference(planFromDay,planFromHour,planFromMinute, planUntilDay,planUntilHour, planUntilMinute);
set Times := {0 .. planTimeRange};

# Improved Shifts definition with fixed shift duration handling
set Shifts := {
    <station_name, required_people, fromHour, fromMinute, untilHour, untilMinute, default_shift_duration_hours, default_shift_duration_minutes, day, time_quant> 
    in Everyday_Missions * weekDays * Times |
        # Current time is within our planning range
        isBetweenDayHourMinute(planFromDay, planFromHour, planFromMinute, planUntilDay, planUntilHour, planUntilMinute, day, getHour(time_quant), getMinute(time_quant)) and
        
        # Current time corresponds to a valid shift start time
        (
            # For missions crossing midnight - the daily start time
            ((getHour(time_quant) == fromHour and getMinute(time_quant) == fromMinute) or
             # For subsequent shifts based on shift duration
             (isBetweenHourMinute(fromHour, fromMinute, untilHour, untilMinute, getHour(time_quant), getMinute(time_quant)) and
             (((getHour(time_quant) - fromHour) * 60 + (getMinute(time_quant) - fromMinute) + 24*60) mod (24*60)) mod (default_shift_duration_hours * 60 + default_shift_duration_minutes) == 0))
        ) and
        (
            # For missions that don't cross midnight
            (fromHour < untilHour and 
             (getHour(time_quant) * 60 + getMinute(time_quant) + default_shift_duration_hours * 60 + default_shift_duration_minutes) <= (untilHour * 60 + untilMinute)) or
            
            # For missions that cross midnight (e.g. 23:00-01:00)
            (fromHour > untilHour and
             ((getHour(time_quant) * 60 + getMinute(time_quant) + default_shift_duration_hours * 60 + default_shift_duration_minutes) <= (untilHour * 60 + untilMinute) or
              (getHour(time_quant) * 60 + getMinute(time_quant)) >= fromHour * 60 + fromMinute))
        ) and
        
        # Ensure the shift doesn't exceed the planning period
        (time_quant + (default_shift_duration_hours*60 + default_shift_duration_minutes) <= planTimeRange) :
        
        <station_name, default_shift_duration_hours*60 + default_shift_duration_minutes, required_people, time_quant>
} union 
{
    <station_name, required_people, fromDay, fromHour, fromMinute, untilDay, untilHour, untilMinute, default_shift_duration_hours, default_shift_duration_minutes, time_quant> 
    in OneTime_Missions * Times | 
        # Check if the time point is within our planning range
        isBetweenDayHourMinute(planFromDay, planFromHour, planFromMinute, planUntilDay, planUntilHour, planUntilMinute, getDay(time_quant), getHour(time_quant), getMinute(time_quant)) and
        
        # Check if this time point is within the mission's bounds
        isBetweenDayHourMinute(fromDay, fromHour, fromMinute, untilDay, untilHour, untilMinute, getDay(time_quant), getHour(time_quant), getMinute(time_quant)) and
        
        # Either it's the exact start time or it's on a valid shift boundary
        (
            (getDay(time_quant) == fromDay and getHour(time_quant) == fromHour and getMinute(time_quant) == fromMinute) or
            (timeDifference(fromDay, fromHour, fromMinute, getDay(time_quant), getHour(time_quant), getMinute(time_quant)) mod (default_shift_duration_hours*60 + default_shift_duration_minutes) == 0)
        ) and
        (
            timeDifference(getDay(time_quant), getHour(time_quant), getMinute(time_quant), untilDay, untilHour, untilMinute) >= (default_shift_duration_hours*60 + default_shift_duration_minutes)
        ) and
        
        # Ensure the shift doesn't exceed the planning period
        (time_quant + (default_shift_duration_hours*60 + default_shift_duration_minutes) <= planTimeRange) :
        
        <station_name, default_shift_duration_hours*60 + default_shift_duration_minutes, required_people, time_quant>
};

do print Shifts;
# do print Everyday_Missions;
# do print isBetweenDayHourMinute("Friday",6,0,"Friday",0,0,getDay(0),getHour(0),getMinute(0));
# do print getDay(0) == "Friday";
# do print ((convertToMinutesRepresentation("Friday",0, 0) > 0) or (convertToMinutesRepresentation("Friday",6, 0) < 0));
# do print (convertToMinutesRepresentation(planUntilDay,planUntilHour, planUntilMinute) >= 0 + (1*60 + 30));
# do print floor((0-convertToMinutesRepresentation("Friday",6, 0)) mod (1*60 + 30)) == 0 ;
# do print ((orderWeekDays["Friday"] >= orderWeekDays[planFromDay] and orderWeekDays["Friday"] <= orderWeekDays[planUntilDay] and orderWeekDays[planFromDay] <= orderWeekDays[planUntilDay] ) or (orderWeekDays["Friday"] >= orderWeekDays[planFromDay] and orderWeekDays["Friday"] <= (orderWeekDays[planUntilDay] + 7) and orderWeekDays[planFromDay] > orderWeekDays[planUntilDay]));
# do print "-----";
# do print (convertToMinutesRepresentation(planUntilDay,planUntilHour, planUntilMinute));

set SoldiersToShifts := Soldiers * proj(Shifts,<1,4>);
#<soldier,station,day,hour_and_minute>
set preAssign := {<"Max","Patrol","1 Sunday","06:00",1.0>};
do print "Selected shift assignment must be valid - Enter valid names (from the declared list), weekdays (x Weekday - where 'x' is the index of the weekday compared to the first day of the planning period) and times in HH:MM format!";
do forall <soldier,station,day,time,value> in preAssign do check
    validateTimeInStringFormat(time) == 1 and card({<soldier> in Soldiers}) == 1 and card({<station> in proj(AllMissions,<1>)}) == 1 and
    validateDayPresentation(day) == 1;
#<soldier,station,time,value>
set encodedPreAssign := {<soldier,station,day,hour_minute,value> in preAssign: <soldier,station,timeDifference(planFromDay,planFromHour, planFromMinute, substr(day,2,10), getHourFromStringFormat(hour_minute), getMinuteFromStringFormat(hour_minute)), if value > -0.5 and value < 0.5 then 0 else 1 end>};
set zero_out := { <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts | (sum <soldier2,station2,time2,value2> in encodedPreAssign | station == station2 and time == time2 and value2 == 1: 1) == requiredPeople and card({<soldier3,station3,time3,value3> in encodedPreAssign | soldier3 == soldier and station3 == station and time3 == time and value3 == 1}) == 0};
param AntiPreAssignRate := 0.15;
do print "Anti-pre-assign rate must be valid - Enter a number between 0 and 1!";
do check AntiPreAssignRate >= 0 and AntiPreAssignRate <= 1;
set antiPreAss := {<soldier,station,time> in SoldiersToShifts | floor(random(0+AntiPreAssignRate,0.99999999+AntiPreAssignRate)) == 1} - proj(encodedPreAssign,<1,2,3>);
var Edge[<i,a,b> in SoldiersToShifts] binary priority 6000000; #
set RestTimes := { <soldier,station,interval,time> in Soldiers * proj(Shifts,<1,2,4>) : <soldier,time + interval>} union {<soldier,zero_time> in Soldiers * {0}};
var NeighbouringShifts[<soldier,endTime> in RestTimes] real >= 0 <= max((max(Times)-endTime)/60,0);
set RestTimes2 := { <soldier,station,interval,time> in Soldiers * proj(Shifts,<1,2,4>) : <soldier,time>} union {<soldier,zero_time> in Soldiers * {0}};
#<station, interval, required, day , hour_and_minute>
set FormalShiftsDescription := {<station, stationInterval, requiredPeople,time, day, hour, minute> in Shifts * FormalTimes | getMinute(time) == minute and getHour(time) == hour and getDay(time) == day :
 <station, stationInterval, requiredPeople, convertToPresentation[day], makeTimeInString(hour,minute)>};
var Shift_Assignments[Soldiers * proj(FormalShiftsDescription,<1,4,5>)] binary; #

subto EnforceNeighbouringShiftsTotalSum:
    forall <soldier> in Soldiers:
        (sum <soldier2,endTime2> in RestTimes | soldier == soldier2: NeighbouringShifts[soldier2,endTime2]) +
        (sum <soldier3,station3,interval3,time3> in Soldiers * proj(Shifts,<1,2,4>) | soldier3 == soldier : Edge[soldier3,station3,time3]*(interval3/60)) == (card(Times)-1)/60;

subto PreAssignZero:
    forall <soldier, station, time> in proj(zero_out,<1,2,5>) union antiPreAss :
        Edge[soldier,station,time] == 0;

set already_satisfied_1 := { <station, stationInterval, requiredPeople, time> in Shifts | (sum <soldier2,station2,time2,value2> in encodedPreAssign | station == station2 and time == time2 and value2 == 1: 1) == requiredPeople };
subto Satisfy_Required_People_For_Shift:
    forall <station, stationInterval, requiredPeople, time> in Shifts - already_satisfied_1 : 
        (sum<soldier,station2,time2> in SoldiersToShifts | station == station2 and time == time2 : Edge[soldier,station,time]) == requiredPeople;

do print "loaded All_Stations_One_Soldier";

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts : 
        Edge[soldier,station,time] * (sum <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | 
                                        soldier2 == soldier and ((time2 >= time and time2 < (time + stationInterval)) or ((time2+stationInterval2) > time and (time2+stationInterval2) < (time + stationInterval))) : Edge[soldier2,station2,time2]) <= 1;

do print "loaded Soldier_Not_In_Two_Stations_Concurrently";

subto ConvertEnumeratedTimesToFormal:
    forall <soldier,station,time> in SoldiersToShifts:
        Shift_Assignments[soldier,station,convertToPresentation[getDay(time)],makeTimeInString(getHour(time), getMinute(time))] == Edge[soldier,station,time];


set already_satisfied_5 := { <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts | (sum <soldier2,station2,time2,value2> in encodedPreAssign | station == station2 and time == time2 and value2 == 1: 1) == requiredPeople and card({<soldier3,station3,time3,value3> in encodedPreAssign | soldier3 == soldier and station3 == station and time3 == time and value3 == 1}) == 0};
do print already_satisfied_5;
subto EnforceCalculationOfRestTimes1:
    forall <soldier, station, stationInterval, requiredPeople, time> in (Soldiers * Shifts) - already_satisfied_5: 
       forall <soldier2, station2, stationInterval2, requiredPeople2, time2> in (Soldiers * Shifts)  | soldier == soldier2 and time2 >= stationInterval+time:
            NeighbouringShifts[soldier,time+stationInterval]  * Edge[soldier,station,time] * Edge[soldier2,station2,time2] <= (((time2-time-(stationInterval)) / 60) );
        
do print "loaded EnforceCalculationOfRestTimes1";

subto EnforceCalculationOfRestTimes2:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts : 
            NeighbouringShifts[soldier,0] * Edge[soldier,station,time] <= time/60;
        
do print "loaded EnforceCalculationOfRestTimes2";

subto EnforceCalculationOfRestTimes3:
    forall <soldier, endTime> in RestTimes | endTime != 0 : 
        vif (sum <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | time2+stationInterval2 == endTime and soldier == soldier2: Edge[soldier2,station2,time2]) == 0
        then NeighbouringShifts[soldier,endTime] == 0 end;
        
do print "loaded EnforceCalculationOfRestTimes3";

subto EnforceAvailabilityUntil:
    forall <soldier, day,hour,minute> in PeopleAllowedToBeAssignedUntil : 
        forall <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | soldier2 == soldier and time2+stationInterval2 > convertToMinutesRepresentation(day,hour,minute):
            Edge[soldier2,station2,time2] == 0;
        
do print "loaded EnforceInavailabilityUntil";

subto EnforceAvailabilityFrom:
    forall <soldier, day,hour,minute> in PeopleAllowedToBeAssignedFrom : 
        forall <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | soldier2 == soldier and time2 < convertToMinutesRepresentation(day,hour,minute):
            Edge[soldier2,station2,time2] == 0;

subto EnforceMinimalRestTimeHeuristic:
    forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts:
        forall <station2, stationInterval2, requiredPeople2, time2> in Shifts | time2 > time and time2 < (time + minimumRestHours*60 + stationInterval):
            Edge[soldier,station,time] * Edge[soldier,station2,time2] == 0;
#         # can also replace by: 
# do print "loaded EnforceInavailabilityFrom";

set preAssignSoldierStatistics := {<"Empty Soldier", "Total Duty Hours", 0.0>};
set preAssignShiftStatistics := {<"People Not Assigned Atleast Once", 1.0>};
# set preAssignSoldierStatistics := {};
# set preAssignShiftStatistics := {};
set labelsForSoldierStatistics := {"Shift Spacing Mark", "Total Night Duty Hours", "Total Duty Hours", "Repetitivity Mark", "Total Rest Time"};
var Per_Person_Statistics[Soldiers * labelsForSoldierStatistics] real;
set labelsGeneralStatistics := {"Problem size (people * possible_shifts)", "Total Shifts Assigned", "Average Shifts Per Person", "Average Rest Time Per Person", "People Not Assigned Atleast Once", "Average Night Hours", "Average Total Duty Hours", "Average Spacing Mark", "Average Repetitivity Mark", "Optimization Score"};
var Total_Statistics[labelsGeneralStatistics] real;
var Assigned_Atleast_Once[Soldiers] binary;
param x:=(max(Times)/60)+1; 
do print "Pre-assigned soldier statistics must be valid - Enter valid names (from the declared list), valid labels, and reasonable values!";
do forall <soldier,label,value> in preAssignSoldierStatistics do check
    card({<soldier> in Soldiers}) == 1 and                          # Soldier must exist
    card({<label> in labelsForSoldierStatistics}) == 1 and         # Label must be valid
    (
        (label == "Total Duty Hours" and value >= 0) or            # Total duty hours must be non-negative
        (label == "Total Night Duty Hours" and value >= 0) or      # Night duty hours must be non-negative
        (label == "Total Rest Time" and value >= 0) or             # Rest time must be non-negative
        (label == "Shift Spacing Mark") or          # Spacing mark must be at least 1
        (label == "Repetitivity Mark" )              # Repetitivity mark must be non-negative
    );

subto CalculateProblemSize:
    Total_Statistics["Problem size (people * possible_shifts)"] == card(Soldiers) * card(Shifts);
do print "loaded CalculateProblemSize";

subto CalculateShiftsAssigned:
    Total_Statistics["Total Shifts Assigned"] == sum <soldier,station,time> in SoldiersToShifts : Edge[soldier,station,time];
do print "loaded CalculateShiftsAssigned";

subto CalculateAverageShiftsPerPerson:
    Total_Statistics["Average Shifts Per Person"] == (sum <soldier,station,time> in SoldiersToShifts : Edge[soldier,station,time]) / card(Soldiers);
do print "loaded CalculateAverageShiftsPerPerson";

subto CalculateAverageRestTime:
    Total_Statistics["Average Rest Time Per Person"] == (sum <soldier,endTime> in RestTimes : NeighbouringShifts[soldier,endTime]) / card(Soldiers);
do print "loaded CalculateAverageRestTime";

subto CalculatePeopleNotAssigned:
    Total_Statistics["People Not Assigned Atleast Once"] == card(Soldiers) - (sum <soldier> in Soldiers : Assigned_Atleast_Once[soldier]);

do print "loaded CalculatePeopleNotAssigned";

subto CalculateShiftSpacingsCost:
    forall <soldier> in Soldiers: 
        Per_Person_Statistics[soldier,"Shift Spacing Mark"] == (sum <soldier2, endTime2> in RestTimes | soldier2 == soldier : ((NeighbouringShifts[soldier2,endTime2]+1)**2)) / (card(RestTimes)/card(Soldiers));
do print "loaded CalculateShiftSpacingsCost";

subto CalculateTotalNightDutyDuration:
    forall <soldier> in Soldiers :
        Per_Person_Statistics[soldier,"Total Night Duty Hours"] == (sum <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | soldier == soldier2 and CommonTimeDuration(nightTimeStartHour,nightTimeStartMinute,nightTimeEndHour,nightTimeEndMinute,getHour(time2),getMinute(time2),getHour(time2+stationInterval2),getMinute(time2+stationInterval2)) > 0: Edge[soldier2,station2,time2] * CommonTimeDuration(nightTimeStartHour,nightTimeStartMinute,nightTimeEndHour,nightTimeEndMinute,getHour(time2),getMinute(time2),getHour(time2+stationInterval2),getMinute(time2+stationInterval2)))/60;
do print "loaded CalculateTotalNightDutyDuration";

subto CalculateTotalMissionsTimes:
    forall <soldier> in Soldiers :
        Per_Person_Statistics[soldier,"Total Duty Hours"] == (sum <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | soldier == soldier2 : Edge[soldier2,station2,time2] * stationInterval2)/60;

do print "loaded CalculateTotalMissionsTimes";

subto CalculateRepetitivity:
    forall <soldier> in Soldiers :                      #Multiple by TotalDutyHours instead of dividing the rhs by it.
        Per_Person_Statistics[soldier,"Repetitivity Mark"] * (Per_Person_Statistics[soldier,"Total Duty Hours"])== (sum <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | soldier == soldier2 : Edge[soldier2,station2,time2] * (sum <soldier3,station3,time3> in SoldiersToShifts | soldier2 == soldier3 and (station3 != station2 or time3 != time2) and (station3 == station2 or time3 == time2) : 1));

do print "loaded CalculateRepetitivity";

subto CalculateTotalRest:
    forall <soldier> in Soldiers : 
        Per_Person_Statistics[soldier,"Total Rest Time"] == (sum <soldier2, endTime2> in RestTimes | soldier == soldier2 : NeighbouringShifts[soldier2,endTime2]);

do print "loaded CalculateTotalRest";

subto CalculateAverageTotalDutyTime:
    Total_Statistics["Average Total Duty Hours"] == (sum <person> in Soldiers : Per_Person_Statistics[person, "Total Duty Hours"])/card(Soldiers);

subto CalculateAverageTotalNightTime:
    Total_Statistics["Average Night Hours"] == (sum <person> in Soldiers : Per_Person_Statistics[person, "Total Night Duty Hours"])/card(Soldiers);

subto CalculateAverageSpacingMark:
    Total_Statistics["Average Spacing Mark"] == (sum <person> in Soldiers : Per_Person_Statistics[person, "Shift Spacing Mark"])/card(Soldiers);

subto CalculateAverageRepetitionMark:
    Total_Statistics["Average Repetitivity Mark"] == (sum <person> in Soldiers : Per_Person_Statistics[person, "Repetitivity Mark"])/card(Soldiers);

subto CalculateIsAssigned:
    forall <soldier> in Soldiers : 
        vif (sum <soldier2, station2, time2> in SoldiersToShifts | soldier == soldier2 : Edge[soldier2,station2,time2]) > 0
        then Assigned_Atleast_Once[soldier] == 1 else Assigned_Atleast_Once[soldier] == 0 end;

do print "loaded CalculateIsAssigned";

subto PreAssignShiftStatistics:
    forall <label,value> in preAssignShiftStatistics:
        Total_Statistics[label] == value;

subto PreAssignSoldierStatistics:
    forall <soldier,label,value> in preAssignSoldierStatistics:
        Per_Person_Statistics[soldier,label] == value; 

subto PreAssign:
    forall<a,b,c,v> in encodedPreAssign :
        Edge[a,b,c] == round(v);


set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}


param soldierSpacingCoefficient := 70;
param soldierTotalNightDutyCoefficient := 10;
param soldierTotalMissionsTimesCoefficient := 30;
param repetitiveMissionsCoefficient := 1;

param soldierSpacingDegree := 1;
param soldierTotalNightDutyDegree := 2;
param soldierTotalMissionsTimesDegree := 2;
param repetitiveMissionsDegree := 2;



subto CalculateOptimizationScore:
    Total_Statistics["Optimization Score"] == 
    soldierSpacingCoefficient * (sum <soldier> in Soldiers : ((Per_Person_Statistics[soldier,"Shift Spacing Mark"] - Total_Statistics["Average Spacing Mark"])**soldierSpacingDegree)) +
    soldierTotalNightDutyCoefficient * (sum <soldier> in Soldiers : ((Per_Person_Statistics[soldier,"Total Night Duty Hours"] - Total_Statistics["Average Night Hours"])**soldierTotalNightDutyDegree)) +
    soldierTotalMissionsTimesCoefficient * (sum <soldier> in Soldiers : ((Per_Person_Statistics[soldier,"Total Duty Hours"] - Total_Statistics["Average Total Duty Hours"])**soldierTotalMissionsTimesDegree)) +
    repetitiveMissionsCoefficient * (sum <soldier> in Soldiers : ((Per_Person_Statistics[soldier,"Repetitivity Mark"] - Total_Statistics["Average Repetitivity Mark"])**repetitiveMissionsDegree));

do print "loaded CalculateOptimizationScroe";

minimize myObjective: 
    soldierSpacingCoefficient * (sum <soldier> in Soldiers : ((Per_Person_Statistics[soldier,"Shift Spacing Mark"] - Total_Statistics["Average Spacing Mark"])**soldierSpacingDegree)) +
    soldierTotalNightDutyCoefficient * (sum <soldier> in Soldiers : ((Per_Person_Statistics[soldier,"Total Night Duty Hours"] - Total_Statistics["Average Night Hours"])**soldierTotalNightDutyDegree)) +
    soldierTotalMissionsTimesCoefficient * (sum <soldier> in Soldiers : ((Per_Person_Statistics[soldier,"Total Duty Hours"] - Total_Statistics["Average Total Duty Hours"])**soldierTotalMissionsTimesDegree)) +
    repetitiveMissionsCoefficient * (sum <soldier> in Soldiers : ((Per_Person_Statistics[soldier,"Repetitivity Mark"] - Total_Statistics["Average Repetitivity Mark"])**repetitiveMissionsDegree));
    
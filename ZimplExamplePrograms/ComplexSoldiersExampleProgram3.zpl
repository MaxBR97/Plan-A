# Changes from last generation: time is represented in minutes, and a significant scalability imporvement

set weekDays := {"Sunday","Monday","Tuesday","Wednesday","Thursday","Friday","Saturday"};
param orderWeekDays[weekDays] := <"Sunday"> 1 , <"Monday"> 2, <"Tuesday"> 3, <"Wednesday"> 4, <"Thursday"> 5, <"Friday"> 6, <"Saturday"> 7;
param orderWeekDaysOpposite[{1..7}] := <1> "Sunday", <2> "Monday", <3> "Tuesday", <4> "Wednesday", <5> "Thursday", <6> "Friday", <7> "Saturday";
param convertToPresentation[weekDays] := <"Sunday"> "1 Sunday", <"Monday"> "2 Monday", <"Tuesday"> "3 Tuesday", <"Wednesday"> "4 Wednesday", <"Thursday"> "5 Thursday", <"Friday"> "6 Friday", <"Saturday"> "7 Saturday";
param toString[{0..9}] := <0> "0", <1> "1", <2> "2", <3> "3", <4> "4", <5> "5", <6> "6", <7> "7", <8> "8", <9> "9";
param stringToNumber[{"0","1","2","3","4","5","6","7","8","9"}] := <"0"> 0, <"1"> 1, <"2"> 2, <"3"> 3, <"4"> 4, <"5"> 5, <"6"> 6, <"7"> 7, <"8"> 8, <"9"> 9;

set hours := {0 .. 23};
set minutes := {0 .. 59};


defnumb convertStringToNumber(str) := 
    sum <i> in {0..length(str)-1} : (stringToNumber[substr(str,length(str) - 1 - i,1)] * (10**i));

# do print convertStringToNumber("132"); # 132
# do print convertStringToNumber("002"); # 2

# 13, 40 -> "13:40"
defstrg makeTimeInString(hour,minute) :=
    toString[floor(hour/10) mod 10] + toString[hour mod 10] + ":" + toString[floor(minute/10) mod 10] + toString[minute mod 10];

#"13:40" -> 40 , "13:02" -> 2 , "13:2" -> 2
defnumb getMinuteFromStringFormat(str) :=
    if length(str) == 5 then convertStringToNumber(substr(str,3,2)) else
        if substr(str,2,1) == ":" then convertStringToNumber(substr(str,3,2)) else convertStringToNumber(substr(str,2,2)) end 
    end;

# do print getMinuteFromStringFormat("13:40"); #40
# do print getMinuteFromStringFormat("13:02")+2; #4
# do print getMinuteFromStringFormat("13:2"); #2
# do print getMinuteFromStringFormat("1:04"); #4

#"13:40" -> 13  , "1:20" -> 1
defnumb getHourFromStringFormat(str) :=
    if length(str) == 5 then convertStringToNumber(substr(str,0,2)) else
        if substr(str,3,1) == ":" then convertStringToNumber(substr(str,0,2)) else convertStringToNumber(substr(str,0,1)) end 
    end;

# do print "getHourFromStringTests";
# do print getHourFromStringFormat("13:40"); #13
# do print getHourFromStringFormat("1:20"); #1
# do print getHourFromStringFormat("1:2"); #1
# do print getHourFromStringFormat("1:2")*5; #5

param planFromTimeFormal := "00:00";
param planFromDay := "Sunday";
param planFromHour := getHourFromStringFormat(planFromTimeFormal);
param planFromMinute := getMinuteFromStringFormat(planFromTimeFormal);
param planUntilTimeFormal := "20:20";
param planUntilDay := "Tuesday";
param planUntilHour := getHourFromStringFormat(planUntilTimeFormal);
param planUntilMinute := getMinuteFromStringFormat(planUntilTimeFormal);
param nightTimeStart := "21:00";
param nightTimeEnd := "7:00";
param nightTimeStartHour := getHourFromStringFormat(nightTimeStart);
param nightTimeStartMinute := getMinuteFromStringFormat(nightTimeStart);
param nightTimeEndHour := getHourFromStringFormat(nightTimeEnd);
param nightTimeEndMinute := getMinuteFromStringFormat(nightTimeEnd);


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

defbool isBetweenHourMinute(fromHour,fromMinute,toHour,toMinute,targetHour,targetMinute) :=
    isBetweenDayHourMinute("Sunday",fromHour,fromMinute,"Sunday",toHour,toMinute,"Sunday",targetHour,targetMinute);

# do print isBetweenHourMinute(22,30,5,00,2,0); #true
# do print isBetweenHourMinute(22,30,5,00,5,30); #false
# do print isBetweenHourMinute(00,30,5,00,5,30); #false
# do print isBetweenHourMinute(00,30,5,00,22,30); #false
# do print isBetweenHourMinute(22,30,5,00,5,20); #false
# do print isBetweenHourMinute(22,30,00,00,23,30); #true
# do print isBetweenHourMinute(22,30,00,00,22,10); #false

defnumb CommonTimeDuration(fromHour, fromMinute, toHour, toMinute, fromHour2, fromMinute2, toHour2, toMinute2) :=
    if isBetweenHourMinute(fromHour, fromMinute, toHour, toMinute,  fromHour2, fromMinute2) and isBetweenHourMinute(fromHour, fromMinute, toHour, toMinute,  toHour2, toMinute2) and HourMinutesDifference(fromHour2,fromMinute2,toHour2,toMinute2) >= 12*60
    then HourMinutesDifference(fromHour2,fromMinute2,toHour,toMinute) + (HourMinutesDifference(fromHour,fromMinute,toHour2,toMinute2))
    else if isBetweenHourMinute(fromHour, fromMinute, toHour, toMinute,  fromHour2, fromMinute2)
    then min(HourMinutesDifference(fromHour2,fromMinute2,toHour,toMinute), HourMinutesDifference(fromHour2,fromMinute2,toHour2,toMinute2))
    else if isBetweenHourMinute(fromHour, fromMinute, toHour, toMinute, toHour2, toMinute2)
    then min(HourMinutesDifference(fromHour,fromMinute,toHour2,toMinute2))
    else 0 
    end end end;

# do print CommonTimeDuration(22,30,5,00,2,00,6,00); # 180 (minutes) because from 22:30 until 5:00 and from 2:00 and 6:00 there is common time of 3 hours from 2:00 to 5:00
# do print CommonTimeDuration(00,30,5,00,2,00,20,53); # 180
# do print CommonTimeDuration(00,30,5,00,5,00,20,53); # 0
# do print CommonTimeDuration(00,30,5,00,5,01,20,53); # 0
# do print CommonTimeDuration(00,30,5,00,5,00,2,00); # 90
# do print CommonTimeDuration(04,30,5,00,5,00,4,59); # 29
# do print CommonTimeDuration(04,30,5,00,5,00,5,00); # 0
# do print CommonTimeDuration(04,30,5,00,2,00,4,50); # 20
# do print CommonTimeDuration(04,00,8,00,7,00,5,00); # 120
do print CommonTimeDuration(07,00,5,00,4,00,8,00); # 120


#hard coded squads - each person belongs to one of the squads
set Squads := {"1a","1b","1c","2a","2b","2c","none"};
#hard coded roles, each person has one.
set AvailableRoles := {"Hapash","Medic","Commander","Officer","none"};
#<Name,Squad,Role>
set Descriptive_Soldiers_List := {
    <"Empty Soldier","none","none">,<"Max","1a","Hapash">, <"Erel","none","Medic">, <"Sheshar","none","Hapash">,
    <"Yoni","none","none">,<"Tal","1a","Hapash">, <"Oded","none","Medic">, <"Moshe", "none", "Hapash">
    ,<"Avi","none","none">,<"Nir","1a","Hapash">, <"Gadi","none","Medic">, <"Dekel","none","Commander">,
    <"Gal","none","none">,<"Zinger","1a","Officer">, <"Koplovich","none","Medic">, <"Arsen","none","Commander">,
    <"Shahor","none","Commander">,<"Denis","1a","Officer">, <"Vayl","none","none">,<"Zalsman","none","Commander">,
    <"Melamed","none","Hapash">,<"Cohen Tov","1a","Hapash">, <"Zikri","none","Hapash">,<"Hason","none","Commander">,
    <"Navon","none","Officer">,<"Yedidya","1a","Hapash">, <"Yuri","none","Commander">,<"Tsefler","none","Hapash">
};

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

set PeopleAllowedToBeAssignedFromFormal := {<"Empty Soldier","Sunday","00:00">};
set PeopleAllowedToBeAssignedUntilFormal := {<"Empty Soldier","Sunday","00:00">};
#<Person_name,FromDay,FromHour,FromMinute>
set PeopleAllowedToBeAssignedFrom := {<soldier,day,stringTime> in PeopleAllowedToBeAssignedFromFormal: <soldier,day,getHourFromStringFormat(stringTime),getMinuteFromStringFormat(stringTime)>};
#<Person_name,Until,UntilHour,UntilMinute>
set PeopleAllowedToBeAssignedUntil := {<soldier,day,stringTime> in PeopleAllowedToBeAssignedUntilFormal: <soldier,day,getHourFromStringFormat(stringTime),getMinuteFromStringFormat(stringTime)>};

#<station,station_interval,required_ppl>
set AllMissions :={<station,interval_hours,interval_minutes,required_people> in proj(Everyday_Missions,<1,7,8,2>) union proj(OneTime_Missions,<1,9,10,2>) : <station,interval_hours*60 + interval_minutes ,required_people>};

set FormalTimes := {<day,hour,minute> in weekDays * hours * minutes | isBetweenDayHourMinute(planFromDay,planFromHour,planFromMinute, planUntilDay,planUntilHour, planUntilMinute, day,hour, minute)};
param planTimeRange := timeDifference(planFromDay,planFromHour,planFromMinute, planUntilDay,planUntilHour, planUntilMinute);
set Times := {0 .. planTimeRange};

#<station,stationInterval,requiredppl,time>
set Shifts := 
    {<station_name, required_people,fromHour,fromMinute,untilHour,untilMinute, default_shift_duration_hours, default_shift_duration_minutes, day, time_quant> in Everyday_Missions * weekDays * Times |
                                                                isBetweenDayHourMinute(day,fromHour,fromMinute,day,untilHour,untilMinute,getDay(time_quant),getHour(time_quant),getMinute(time_quant)) and
                                                                getDay(time_quant) == day and
                                                                (if untilHour < fromHour or (untilHour == fromHour and untilMinute < fromMinute) then
                                                                ((convertToMinutesRepresentation(day,untilHour, untilMinute) > time_quant) or (convertToMinutesRepresentation(day,fromHour, fromMinute) < time_quant))
                                                                else (convertToMinutesRepresentation(day,untilHour, untilMinute) > time_quant) end) and
                                                                (convertToMinutesRepresentation(planUntilDay,planUntilHour, planUntilMinute) > time_quant + (default_shift_duration_hours*60 + default_shift_duration_minutes)) and
                                                                floor((time_quant-convertToMinutesRepresentation(day,fromHour, fromMinute)) mod (default_shift_duration_hours*60 + default_shift_duration_minutes)) == 0 and  
                                                                ((orderWeekDays[day] >= orderWeekDays[planFromDay] and orderWeekDays[day] <= orderWeekDays[planUntilDay] and orderWeekDays[planFromDay] <= orderWeekDays[planUntilDay] ) or (orderWeekDays[day] >= orderWeekDays[planFromDay] and orderWeekDays[day] <= orderWeekDays[planUntilDay] and orderWeekDays[planFromDay] > orderWeekDays[planUntilDay])) :
                                                                <station_name, default_shift_duration_hours*60 + default_shift_duration_minutes , required_people,time_quant>} union 
    {<station_name, required_people,fromDay,fromHour,fromMinute,untilDay,untilHour,untilMinute,default_shift_duration_hours,default_shift_duration_minutes,time_quant> in OneTime_Missions * Times | 
                                                                isBetweenDayHourMinute(fromDay,fromHour,fromMinute,untilDay,untilHour,untilMinute,getDay(time_quant),getHour(time_quant),getMinute(time_quant)) and
                                                                (convertToMinutesRepresentation(planUntilDay,planUntilHour, planUntilMinute) > time_quant + (default_shift_duration_hours*60 + default_shift_duration_minutes)) and
                                                                (convertToMinutesRepresentation(untilDay,untilHour, untilMinute) > time_quant) and
                                                                floor((time_quant-convertToMinutesRepresentation(fromDay,fromHour, fromMinute)) mod (default_shift_duration_hours*60 + default_shift_duration_minutes)) == 0 : 
                                                                <station_name,default_shift_duration_hours*60 + default_shift_duration_minutes , required_people,time_quant>};
do print Shifts;


set SoldiersToShifts := Soldiers * proj(Shifts,<1,4>);
#<soldier,station,day,hour_and_minute>
set preAssign := {<"Max","Patrol","1 Sunday","6:00",1>};
#<soldier,station,time>
set encodedPreAssign := {<soldier,station,day,hour_minute,value> in preAssign: <soldier,station,timeDifference(planFromDay,planFromHour, planFromMinute, substr(day,2,10), getHourFromStringFormat(hour_minute), getMinuteFromStringFormat(hour_minute)),value>};
set zero_out := { <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts | (sum <soldier2,station2,time2,value2> in encodedPreAssign | station == station2 and time == time2 and value2 == 1: 1) == requiredPeople and card({<soldier3,station3,time3,value3> in encodedPreAssign | soldier3 == soldier and station3 == station and time3 == time and value3 == 1}) == 0};
# param AntiPreAssignRate := 0.0;
# set antiPreAss := {<soldier,station,time> in SoldiersToShifts | floor(random(0+AntiPreAssignRate,1+AntiPreAssignRate)) == 1} - encodedPreAssign;
var Edge[<i,a,b> in SoldiersToShifts] binary; #
set RestTimes := { <soldier,station,interval,time> in Soldiers * proj(Shifts,<1,2,4>) : <soldier,time + interval>} union {<soldier,zero_time> in Soldiers * {0}};
var NeighbouringShifts[<soldier,endTime> in RestTimes] real >= 0 <= max((max(Times)-endTime)/60,0);
set RestTimes2 := { <soldier,station,interval,time> in Soldiers * proj(Shifts,<1,2,4>) : <soldier,time>} union {<soldier,zero_time> in Soldiers * {0}};
var IntervalsBetweenShiftStarts[<soldier,time> in RestTimes2] real >= 0;
#<station, interval, required, day , hour_and_minute>
set FormalShiftsDescription := {<station, stationInterval, requiredPeople,time, day, hour, minute> in Shifts * FormalTimes | getMinute(time) == minute and getHour(time) == hour and getDay(time) == day :
 <station, stationInterval, requiredPeople, convertToPresentation[day], makeTimeInString(hour,minute)>};
var FormalTimesEdges[Soldiers * proj(FormalShiftsDescription,<1,4,5>)] binary; #

# subto CalculateIntervalsBetweenShifts1:
#     forall <soldier,endTime> in RestTimes:
#         forall <soldier2,station2,interval2,time2> in Soldiers * proj(Shifts,<1,2,4>) | soldier == soldier2 and time2 + interval2 == endTime:
#             Edge[soldier2,station2,time2] * (IntervalsBetweenShiftStarts[soldier2,time2]) == Edge[soldier2,station2,time2] * (NeighbouringShifts[soldier,endTime] + (interval2/60));

# subto CalculateIntervalsBetweenShifts2:
#         forall <soldier, station, stationInterval, requiredPeople, time> in Soldiers * Shifts | time > 0: 
#             IntervalsBetweenShiftStarts[soldier,0] * Edge[soldier,station,time] <= time/60;

subto EnforceNeighbouringShiftsTotalSum:
    forall <soldier> in Soldiers:
        (sum <soldier2,endTime2> in RestTimes | soldier == soldier2: NeighbouringShifts[soldier2,endTime2]) +
        (sum <soldier3,station3,interval3,time3> in Soldiers * proj(Shifts,<1,2,4>) | soldier3 == soldier : Edge[soldier3,station3,time3]*(interval3/60)) == (card(Times)-1)/60;

subto PreAssignZero:
    forall <soldier, station, stationInterval, requiredPeople, time> in zero_out :
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
        FormalTimesEdges[soldier,station,convertToPresentation[getDay(time)],makeTimeInString(getHour(time), getMinute(time))] == Edge[soldier,station,time];

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
        
do print "loaded EnforceInavailabilityFrom";
set preAssignSoldierStatistics := {<"Empty Soldier", "Total Duty Hours", 0>};
set preAssignShiftStatistics := {<"People Not Assigned Atleast Once",1>};
# set preAssignSoldierStatistics := {};
# set preAssignShiftStatistics := {};
set labelsForSoldierStatistics := {"Shift Spacing Mark", "Total Night Duty Hours", "Total Duty Hours", "Repetitivity Mark", "Total Rest Time", "Rest Time Mean"};
var SoldierStatistics[Soldiers * labelsForSoldierStatistics] real;
set labelsGeneralStatistics := {"Problem size (people * possible_shifts)", "Total Shifts Assigned", "Average Shifts Per Person", "Average Rest Time Per Person", "People Not Assigned Atleast Once", "Optimization Score"};
var ShiftStatistics[labelsGeneralStatistics] real;
var AssignedAtleastOnce[Soldiers] binary;
param x:=(max(Times)/60)+1; 

subto CalculateProblemSize:
    ShiftStatistics["Problem size (people * possible_shifts)"] == card(Soldiers) * card(Shifts);
do print "loaded CalculateProblemSize";

subto CalculateShiftsAssigned:
    ShiftStatistics["Total Shifts Assigned"] == sum <soldier,station,time> in SoldiersToShifts : Edge[soldier,station,time];
do print "loaded CalculateShiftsAssigned";

subto CalculateAverageShiftsPerPerson:
    ShiftStatistics["Average Shifts Per Person"] == (sum <soldier,station,time> in SoldiersToShifts : Edge[soldier,station,time]) / card(Soldiers);
do print "loaded CalculateAverageShiftsPerPerson";

subto CalculateAverageRestTime:
    ShiftStatistics[ "Average Rest Time Per Person"] == (sum <soldier,endTime> in RestTimes : NeighbouringShifts[soldier,endTime]) / card(Soldiers);
do print "loaded CalculateAverageRestTime";

subto CalculatePeopleNotAssigned:
    ShiftStatistics["People Not Assigned Atleast Once"] == card(Soldiers) - (sum <soldier> in Soldiers : AssignedAtleastOnce[soldier]);

do print "loaded CalculatePeopleNotAssigned";

# subto CalculateShiftSpacingsCost:
#     forall <soldier> in Soldiers: 
#         SoldierStatistics[soldier,"Shift Spacing Mark"] == (sum <soldier2, endTime2> in RestTimes | soldier2 == soldier : ((x - sqrt((NeighbouringShifts[soldier2,endTime2]*2)+1))**2))/(card(RestTimes) * (x**2)) * 100;
# do print "loaded CalculateShiftSpacingsCost";

# subto CalculateRestTimeMean:
#     forall <soldier> in Soldiers:
#         SoldierStatistics[soldier,"Rest Time Mean"] == SoldierStatistics[soldier,"Total Rest Time"] / SoldierStatistics[soldier,"Total Duty Hours"];

subto CalculateShiftSpacingsCost:
    forall <soldier> in Soldiers: 
        SoldierStatistics[soldier,"Shift Spacing Mark"] == (sum <soldier2, endTime2> in RestTimes | soldier2 == soldier : ((NeighbouringShifts[soldier2,endTime2]+1)**2)) / card(RestTimes);
do print "loaded CalculateShiftSpacingsCost";

subto CalculateTotalNightDutyDuration:
    forall <soldier> in Soldiers :
        SoldierStatistics[soldier,"Total Night Duty Hours"] == (sum <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | soldier == soldier2 and CommonTimeDuration(nightTimeStartHour,nightTimeStartMinute,nightTimeEndHour,nightTimeEndMinute,getHour(time2),getMinute(time2),getHour(time2+stationInterval2),getMinute(time2+stationInterval2)) > 0: Edge[soldier2,station2,time2] * CommonTimeDuration(nightTimeStartHour,nightTimeStartMinute,nightTimeEndHour,nightTimeEndMinute,getHour(time2),getMinute(time2),getHour(time2+stationInterval2),getMinute(time2+stationInterval2)))/60;
do print "loaded CalculateTotalNightDutyDuration";

subto CalculateTotalMissionsTimes:
    forall <soldier> in Soldiers :
        SoldierStatistics[soldier,"Total Duty Hours"] == (sum <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | soldier == soldier2 : Edge[soldier2,station2,time2] * stationInterval2)/60;

do print "loaded CalculateTotalMissionsTimes";

subto CalculateRepetitivity:
    forall <soldier> in Soldiers : 
        SoldierStatistics[soldier,"Repetitivity Mark"] == (sum <soldier2, station2, stationInterval2, requiredPeople2, time2> in Soldiers * Shifts | soldier == soldier2 : Edge[soldier2,station2,time2] * (sum <soldier3,station3,time3> in SoldiersToShifts | soldier2 == soldier3 and (station3 != station2 or time3 != time2) and (station3 == station2 or time3 == time2) : 1))/(2*card(Shifts));

do print "loaded CalculateRepetitivity";

subto CalculateTotalRest:
    forall <soldier> in Soldiers : 
        SoldierStatistics[soldier,"Total Rest Time"] == (sum <soldier2, endTime2> in RestTimes | soldier == soldier2 : NeighbouringShifts[soldier2,endTime2]);

do print "loaded CalculateTotalRest";

subto CalculateIsAssigned:
    forall <soldier> in Soldiers : 
        vif (sum <soldier2, station2, time2> in SoldiersToShifts | soldier == soldier2 : Edge[soldier2,station2,time2]) > 0
        then AssignedAtleastOnce[soldier] == 1 else AssignedAtleastOnce[soldier] == 0 end;

do print "loaded CalculateIsAssigned";

subto PreAssignShiftStatistics:
    forall <label,value> in preAssignShiftStatistics:
        ShiftStatistics[label] == value;

subto PreAssignSoldierStatistics:
    forall <soldier,label,value> in preAssignSoldierStatistics:
        SoldierStatistics[soldier,label] == value; 

subto PreAssign:
    forall<a,b,c,v> in encodedPreAssign :
        Edge[a,b,c] == v;


set indexSetOfPeople := {<i,p> in {1.. card(Soldiers)} * Soldiers | ord(Soldiers,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

param betterTotalRestTimeCoefficient := 90;
param soldierSpacingCoefficient := 70;
param soldierTotalNightDutyCoefficient := 10;
param soldierTotalMissionsTimesCoefficient := 3;
param repetitiveMissionsCoefficient := 1;

param betterTotalRestTimeDegree := 2;
param soldierSpacingDegree := 1;
param soldierTotalNightDutyDegree := 2;
param soldierTotalMissionsTimesDegree := 2;
param repetitiveMissionsDegree := 2;

param betterTotalRestTimeBias := 1;
param soldierSpacingBias := 1;
param soldierTotalNightDutyBias := 1;
param soldierTotalMissionsTimesBias := 1;
param repetitiveMissionsBias := 1;

subto CalculateOptimizationScore:
    ShiftStatistics["Optimization Score"] == betterTotalRestTimeCoefficient * (sum <soldier> in Soldiers : ((SoldierStatistics[soldier,"Total Rest Time"]+betterTotalRestTimeBias))**betterTotalRestTimeDegree) +
    soldierSpacingCoefficient * (sum <soldier> in Soldiers : (SoldierStatistics[soldier,"Shift Spacing Mark"]+soldierSpacingBias)**soldierSpacingDegree) +
    soldierTotalNightDutyCoefficient * (sum <soldier> in Soldiers : (SoldierStatistics[soldier,"Total Night Duty Hours"]+soldierTotalNightDutyBias)**soldierTotalNightDutyDegree) +
    soldierTotalMissionsTimesCoefficient * (sum <soldier> in Soldiers : (SoldierStatistics[soldier,"Total Duty Hours"]+soldierTotalMissionsTimesBias)**soldierTotalMissionsTimesDegree) +
    repetitiveMissionsCoefficient * (sum <soldier> in Soldiers : (SoldierStatistics[soldier,"Repetitivity Mark"]+repetitiveMissionsBias)**repetitiveMissionsDegree);

do print "loaded CalculatePeopleNotAssigned";

minimize myObjective: 
    betterTotalRestTimeCoefficient * (sum <soldier> in Soldiers : ((SoldierStatistics[soldier,"Total Rest Time"]+betterTotalRestTimeBias))**betterTotalRestTimeDegree) +
    soldierSpacingCoefficient * (sum <soldier> in Soldiers : (SoldierStatistics[soldier,"Shift Spacing Mark"]+soldierSpacingBias)**soldierSpacingDegree) +
    soldierTotalNightDutyCoefficient * (sum <soldier> in Soldiers : (SoldierStatistics[soldier,"Total Night Duty Hours"]+soldierTotalNightDutyBias)**soldierTotalNightDutyDegree) +
    soldierTotalMissionsTimesCoefficient * (sum <soldier> in Soldiers : (SoldierStatistics[soldier,"Total Duty Hours"]+soldierTotalMissionsTimesBias)**soldierTotalMissionsTimesDegree) +
    repetitiveMissionsCoefficient * (sum <soldier> in Soldiers : (SoldierStatistics[soldier,"Repetitivity Mark"]+repetitiveMissionsBias)**repetitiveMissionsDegree);
    #1000000*ShiftStatistics["People Not Assigned Atleast Once"] ; #Heuristic
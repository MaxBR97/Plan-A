param restHours := 5;
param shiftTime := 4;
param totalShiftsPerStation := 5;
param shiftsUntil := shiftTime * totalShiftsPerStation;
param bias := 500;

set People := {"Yoni","Denis","Nadav","Max"};
set Station := {"North", "South"};
set Times := {0.. shiftsUntil by shiftTime};

set invalidShifts := {<"Yoni","North",0,20>};
    
set Mishmarot := Station * Times; # -> {<North,16>, <North,20>, ....}

#<person,station,time,result>
set AssignmentsBoundSet := {<"Max","North",0,1>};

#<person,result>
set TotalsBoundSet := {<"Max",3>};

do print "Invalid Shifts - Person and Station must be defined, times must be in the defined range, beggining time must be before end time.";
do forall <person,station,from,until> in invalidShifts do check
    from >= 0 and until <= shiftsUntil and
    from < until and
    from mod shiftTime == 0 and until mod shiftTime == 0 and
    card({<person> in People}) == 1 and
    card({<station> in Station}) == 1;

do print "Selected Shifts must have a defined person, station and time!";
do forall <person,station,time,value> in AssignmentsBoundSet do check
    card({<person> in People}) == 1 and
    card({<station> in Station}) == 1 and
    time >= 0 and time <= shiftsUntil and
    time mod shiftTime == 0;


do print "Selected Total Shifts must have a defined person and non-negative number of shifts.";
do forall <person,result> in TotalsBoundSet do check
    card({<person> in People}) == 1 and
    result >= 0 ;


var Assignments[People * Mishmarot] binary; # -> {<Max,North,16>, <Max,North,20>, ....}
var Totals [People] integer >= 0;

subto PreAssignAssignments:
    forall <person,station,time,result> in AssignmentsBoundSet:
        Assignments[person,station,time] == result;

subto TotalsBoundSet:
    forall <person,result> in TotalsBoundSet:
        Totals[person] == result;

subto EachPersonAssignedOneShiftAtATime:
    forall <i,t> in People*Times: (sum <j,a,b> in People*Mishmarot | b == t and i == j : Assignments[i,a,b]) <= 1;

subto EachShiftMustBeFilled:
    forall <a,b> in Mishmarot : (sum <i,c,d> in People*Mishmarot| a==c and b==d: Assignments[i,a,b]) == 1;

subto EnforceRestTimes:
    forall <person, emda, zman> in People * Mishmarot : 
            vif Assignments[person,emda,zman] == 1
            then  (sum <person2, emda2, zman2> in People * Mishmarot | person == person2 and zman2 >= zman and zman2 <= zman+restHours : Assignments[person2, emda2, zman2]) == 1 end;

subto CalculateTotalShiftsForEveryPerson:
    forall <person> in People: 
        Totals[person] == sum <person2,emda,zman> in People * Mishmarot | person ==person2 : Assignments[person2,emda,zman];

subto EnforceInvalidShifts:
    forall <person , station , fromTime , toTime> in invalidShifts:
        (sum <p,s,time> in People * Mishmarot | p == person and station == s and time >= fromTime and time <= toTime : Assignments[p,s,time]) == 0;

set indexSetOfPeople := {<i,p> in {1.. card(People)} * People | ord(People,i,1) == p}; # -> {<1,"Yoni">, <2,"Denis"> ...}

minimize distributeShiftsEqually:
    (sum <person> in People : ((Totals[person]+1)**2))
    + (sum <i,person,station,time> in  indexSetOfPeople*Mishmarot | time <= card(People)/card(Station)*shiftTime: (Assignments[person,station,time]*bias*i*time));


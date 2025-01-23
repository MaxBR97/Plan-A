param weight := 10;
param absoluteMinimalSpacing := 8;
param soldiers := 10;

set C := {1 .. soldiers};

#set C := {"Ben","Dan","Ron","Nir","Niv","Avi","Shlomo"};
set Stations := {"Shin Gimel", "Fillbox"};
#Hours from 0:00 to 20:00 in 4 hour intervals
set Times := {0,4,8,12,16,20};
set S := Stations * Times;
set Possible_Soldier_Shifts := C * S; # [<Ben, <Fillbox, 4>> , <Ron, 8>]
set Possible_Transitions := {<i,a,b,c,d> in C * S * S | b < d };

var Soldier_Shift[Possible_Soldier_Shifts] binary;
var Soldier_Transitions[Possible_Transitions] binary;


subto trivial1:
    forall <j,a1,a2,b1,b2> in Possible_Transitions | a1 != b1 or a2 != b2 : vif Soldier_Transitions[j,a1,a2,b1,b2] == 1 then Soldier_Shift[j,a1,a2] == 1  end;

subto trivial2:
    forall <j,a1,a2,b1,b2> in Possible_Transitions | a1 != b1 or a2 != b2 : vif Soldier_Transitions[j,a1,a2,b1,b2] == 1 then Soldier_Shift[j,a1,a2] == Soldier_Shift[j,b1,b2]  end;

subto trivial3:
    forall <i,a,b> in Possible_Soldier_Shifts | b < max(Times): vif Soldier_Shift[i,a,b] == 1 and (sum<k,m,n> in Possible_Soldier_Shifts| k==i and n > b: Soldier_Shift[k,m,n]) >= 1 then sum <j,a1,a2,b1,b2> in Possible_Transitions | i == j and (a==a1 and b==a2) : Soldier_Transitions[j,a1,a2,b1,b2] == 1 end;

subto trivial4:
    forall <i,a,b> in Possible_Soldier_Shifts | b > min(Times): vif Soldier_Shift[i,a,b] == 1 and (sum<k,m,n> in Possible_Soldier_Shifts| k==i and n < b: Soldier_Shift[k,m,n]) >= 1 then sum <j,a1,a2,b1,b2> in Possible_Transitions | i == j and (a==b1 and b==b2) : Soldier_Transitions[j,a1,a2,b1,b2] == 1 end;

subto trivial5:
    forall <j,a1,a2,b1,b2> in Possible_Transitions | a1 != b1 or a2 != b2 : Soldier_Transitions[j,a1,a2,b1,b2] == Soldier_Transitions[j,a1,a2,b1,b2] *((sum <m,n> in S | n > a2 and n < b2: Soldier_Shift[j,m,n]) + 1);

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <i,t,z> in Possible_Soldier_Shifts : ( sum<m,a,b> in Possible_Soldier_Shifts | m == i and z == b : Soldier_Shift[i,a,b]) <= 1;

subto All_Stations_One_Soldier:
    forall <s,f> in S: (sum<a,b,c> in Possible_Soldier_Shifts | s==b and f==c: Soldier_Shift[a,b,c]) == 1;

var minGuards integer  >= 0 <= 5 priority 2 startval 1;
var maxGuards integer >= 1 <= 7 priority 4 startval 2;
var minimalSpacing integer >= absoluteMinimalSpacing <= absoluteMinimalSpacing+4 priority 12 startval 12;

subto minGuardsCons:
    forall <i> in C: (sum <m,a,b> in Possible_Soldier_Shifts | i==m : Soldier_Shift[m,a,b]) >= minGuards;

subto maxGuardsCons:
    forall <i> in C: (sum <m,a,b> in Possible_Soldier_Shifts | i==m : Soldier_Shift[m,a,b]) <= maxGuards;

subto minimalSpacingCons:
    forall <i,a,b,c,d> in Possible_Transitions: vif (d-b) < minimalSpacing then Soldier_Transitions[i,a,b,c,d] == 0 end;

minimize Spacing:
    ((maxGuards-minGuards)+weight)**3 -
    (minimalSpacing)**2 +
    sum<i,a,b> in Possible_Soldier_Shifts: sum<m,n> in S | m != a or b!=n :(Soldier_Shift[i,a,b] * Soldier_Shift[i,m,n] * (b-n));

param weight := 10;
param soldiers := 9;
param absoluteMinimalSpacing := 8;


set C := {1..soldiers};
set Stations := {"Shin Gimel", "Fillbox"};
set Times := {0,4,8,12,16,20};
set S := Stations * Times;
set CxS := C * S;
set CxSxS := {<i,a,b,c,d> in C * S * S | b < d };
set preassign_soldier_shifts := {<1,"Fillbox",4>};

var edge[CxS] binary;
var couples[CxSxS] binary;

subto preassign:
    forall <i,a,b> in preassign_soldier_shifts: edge[i,a,b] == 1;

subto trivial1:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : vif couples[j,a1,a2,b1,b2] == 1 then edge[j,a1,a2] == 1  end;

subto trivial4:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : vif couples[j,a1,a2,b1,b2] == 1 then edge[j,a1,a2] == edge[j,b1,b2]  end;

subto trivial5:
    forall <i,a,b> in CxS | b < max(Times): vif edge[i,a,b] == 1 and (sum<k,m,n> in CxS| k==i and n > b: edge[k,m,n]) >= 1 then sum <j,a1,a2,b1,b2> in CxSxS | i == j and (a==a1 and b==a2) : couples[j,a1,a2,b1,b2] == 1 end;

subto trivial7:
    forall <i,a,b> in CxS | b > min(Times): vif edge[i,a,b] == 1 and (sum<k,m,n> in CxS| k==i and n < b: edge[k,m,n]) >= 1 then sum <j,a1,a2,b1,b2> in CxSxS | i == j and (a==b1 and b==b2) : couples[j,a1,a2,b1,b2] == 1 end;

subto trivial3:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : couples[j,a1,a2,b1,b2] == couples[j,a1,a2,b1,b2] *((sum <m,n> in S | n > a2 and n < b2: edge[j,m,n]) + 1);

subto Soldier_Not_In_Two_Stations_Concurrently:
    forall <i,t,z> in CxS : ( sum<m,a,b> in CxS | m == i and z == b : edge[i,a,b]) <= 1;

subto All_Stations_One_Soldier:
    forall <s,f> in S: (sum<a,b,c> in CxS | s==b and f==c: edge[a,b,c]) == 1;

var minGuards integer  >= 0 <= 5 priority 2 startval 1;
var maxGuards integer >= 1 <= 7 priority 4 startval 2;
var minimalSpacing integer >= absoluteMinimalSpacing <= absoluteMinimalSpacing+4 priority 12 startval 12;

subto minGuardsCons:
    forall <i> in C: (sum <m,a,b> in CxS | i==m : edge[m,a,b]) >= minGuards;

subto maxGuardsCons:
    forall <i> in C: (sum <m,a,b> in CxS | i==m : edge[m,a,b]) <= maxGuards;

subto minimalSpacingCons:
    forall <i,a,b,c,d> in CxSxS: vif (d-b) < minimalSpacing then couples[i,a,b,c,d] == 0 end;

set preffered_soldiers := {<1,2>,<2,3>};
param preffered_soldiers_weight := 10;
minimize Spacing: 
    ((maxGuards-minGuards)+weight)**3 -
    (minimalSpacing)**2 +
    (sum<i,a,b> in CxS: sum<m,n> in S | m != a or b!=n :(edge[i,a,b] * edge[i,m,n] * (b-n))) +
    (sum <a,b> in preffered_soldiers: (a + b + weight) * preffered_soldiers_weight)
    ;

param conditioner := 10;
param soldiers := 12;
param absoluteMinimalRivuah := 8;

set setWithRange := {1..100 by conditioner};
set C := {1..soldiers};
set Emdot := {"Shin Gimel", "Fillbox"};
set Zmanim := {0,4,8,12,16,20};
set S := Emdot * Zmanim;
set CxS := C * S;
set CxSxS := {<i,a,b,c,d> in C * S * S | b < d };
set forTest1 := {<a> in {"a","b","c"} : <soldiers, a>};
set forTest2 := {"a", "b"} * S * {1..soldiers} * C * {<"h",2.2> , <"a",-3.14>}; 
set forTest3 := {<1,"gsd",3>, <54,"g5h",3>};
set forTest4 := proj(forTest3,<2,1>);
set forTest5 := proj(forTest4 * {<2,"a">,<1,"f">},<2,4>); # -> <INT,TEXT>

var edge[CxS] binary;
var couples[CxSxS] binary;
var varForTest1[CxS *{"A","a"} * S * {1 .. 5}];


subto trivial1:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : vif couples[j,a1,a2,b1,b2] == 1 then edge[j,a1,a2] == 1  end;

subto trivial4:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : vif couples[j,a1,a2,b1,b2] == 1 then edge[j,a1,a2] == edge[j,b1,b2]  end;

subto trivial5:
    forall <i,a,b> in CxS | b < max(Zmanim): vif edge[i,a,b] == 1 and (sum<k,m,n> in CxS| k==i and n > b: edge[k,m,n]) >= 1 then sum <j,a1,a2,b1,b2> in CxSxS | i == j and (a==a1 and b==a2) : couples[j,a1,a2,b1,b2] == 1 end;

subto trivial7:
    forall <i,a,b> in CxS | b > min(Zmanim): vif edge[i,a,b] == 1 and (sum<k,m,n> in CxS| k==i and n < b: edge[k,m,n]) >= 1 then sum <j,a1,a2,b1,b2> in CxSxS | i == j and (a==b1 and b==b2) : couples[j,a1,a2,b1,b2] == 1 end;

subto trivial3:
    forall <j,a1,a2,b1,b2> in CxSxS | a1 != b1 or a2 != b2 : couples[j,a1,a2,b1,b2] == couples[j,a1,a2,b1,b2] *((sum <m,n> in S | n > a2 and n < b2: edge[j,m,n]) + 1);

subto Hayal_Lo_Shomer_Beshtey_Emdot_Bo_Zmanit:
    forall <i,t,z> in CxS : ( sum<m,a,b> in CxS | m == i and z == b : edge[i,a,b]) <= 1;

subto Kol_Haemdot_Meshubatsot_Hayal_Ehad:
    forall <s,f> in S: (sum<a,b,c> in CxS | s==b and f==c: edge[a,b,c]) == 1;

var minShmirot integer  >= 0 <= 5 priority 2 startval 1;
var maxShmirot integer >= 1 <= 7 priority 4 startval 2;
var minimalRivuah integer >= absoluteMinimalRivuah <= absoluteMinimalRivuah+4 priority 12 startval 12;

subto minShmirotCons:
    forall <i> in C: (sum <m,a,b> in CxS | i==m : edge[m,a,b]) >= minShmirot;

subto maxShmirotCons:
    forall <i> in C: (sum <m,a,b> in CxS | i==m : edge[m,a,b]) <= maxShmirot;

subto minimalRivuahCons:
    forall <i,a,b,c,d> in CxSxS: vif (d-b) < minimalRivuah then couples[i,a,b,c,d] == 0 end;

minimize rivuah: 
    ((maxShmirot-minShmirot)+conditioner)**3 -
    (minimalRivuah)**2 +
    (sum <i,a,b> in CxS: sum<m,n> in S | m != a or b!=n :(edge[i,a,b] * edge[i,m,n] * (b-n)))*8;

set People := {};
var TotalMishmarot[People] integer;

maximize distributeShiftsEqually:
    sum <person> in People : (TotalMishmarot[person]**2);
set Cities := {<"Tel Aviv",5,16>, <"Yafo",5,14>, <"Jerusalem",25,8>, <"Tveria",40,90>, <"Nahariya",6,95>, <"Eilat",30,-103>,
                <"Dimona",70,-20>, <"Ashkelon",2,-10>, <"Ashdod",3,3>, <"Beer Sheva",-7,-23>, <"Kiryat Arba",68,40>,                
                <"Akko",23,85>, <"Hermon",80,130>, <"Beit Shean",75,50>, <"Katserin",50,95>, <"Ein Gedi",75,15>, <"Hadera",8,50>,
                <"Ramat Gan",6,17>, <"Kfar Saba",8,23>, <"Mitzpe Ramon",31,-80>, <"Yotvata",28,-89>, <"Beit Shemesh",22,9>};
param StartingCity := "Tel Aviv";
param citiesToVisit := card(Cities);

set CitiesNames := proj(Cities,<1>);
set AllPossibleCombinations := CitiesNames * CitiesNames;
var Edges[AllPossibleCombinations] integer >= 0 <= citiesToVisit;

subto StartFromStartingCity:
    sum <src,dest> in AllPossibleCombinations | src == StartingCity : Edges[src,dest] == 1;

subto EachStepHappensOnce:
        forall <src,dest> in AllPossibleCombinations : 
            forall <src2,dest2> in AllPossibleCombinations | src2 != src or dest2 != dest : vif Edges[src,dest] == Edges[src2,dest2] 
                                                                                                            then Edges[src2,dest2] == 0 end;
                                                                
subto CantArriveSameCityTwice:
    forall <city> in CitiesNames:
        forall <src,dest> in AllPossibleCombinations | dest == city: vif Edges[src,dest] != 0
                                                                     then sum <src2,dest2> in AllPossibleCombinations| src2 != src and dest2 == dest: Edges[src2,dest2] == 0 end;

subto CantDepartSameCityTwice:
    forall <city> in CitiesNames:
        forall <src,dest> in AllPossibleCombinations | src == city: vif Edges[src,dest] != 0
                                                                     then sum <src2,dest2> in AllPossibleCombinations| src2 == src and dest2 != dest: Edges[src2,dest2] == 0 end;

subto CantStayInPlace:
    forall <src,dest> in AllPossibleCombinations | src == dest : Edges[src,dest] == 0;

subto VisitsExactNumberOfCities:
    sum <src,dest> in AllPossibleCombinations: Edges[src,dest] == sum <i> in {1..citiesToVisit} : i;

subto DepartFromLastArrivedCity:
    forall <src,dest> in AllPossibleCombinations:
        vif Edges[src,dest] != 0 and Edges[src,dest] != citiesToVisit
        then (sum <src2,dest2> in AllPossibleCombinations | dest == src2 : Edges[src2,dest2]) - 1 == Edges[src,dest] end;

var ExistingEdgeFlags[AllPossibleCombinations] binary;

subto FlagEdgesThatArePositive:
    forall <src,dest> in AllPossibleCombinations: vif Edges[src,dest] != 0
                                                    then ExistingEdgeFlags[src,dest] == 1 
                                                    else ExistingEdgeFlags[src,dest] == 0 end;                     

minimize distance:    
    sum <src,x1,y1,dest,x2,y2> in Cities * Cities: 
        sqrt((x1-x2)**2 + (y1-y2)**2) * ExistingEdgeFlags[src,dest];
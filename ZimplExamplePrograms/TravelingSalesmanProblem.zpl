# 21 Cities
# one thread solving time: 497 seconds  (solved in vm, which is slower)

#Input fields: Cities, StartingCity, citiesToVisit
set Cities := {<"Tel Aviv",5,16>, <"Yafo",5,14>, <"Jerusalem",25,8>, <"Tveria",40,90>, <"Nahariya",6,95>, <"Eilat",30,-103>,
                <"Dimona",70,-20>, <"Ashkelon",2,-10>, <"Ashdod",3,3>, <"Beer Sheva",-7,-23>, <"Kiryat Arba",68,40>,                
                <"Akko",23,85>, <"Hermon",80,130>, <"Beit Shean",75,50>, <"Katserin",50,95>, <"Ein Gedi",75,15>, <"Hadera",8,50>,
                <"Ramat Gan",6,17>, <"Kfar Saba",8,23>, <"Mitzpe Ramon",31,-80>, <"Yotvata",28,-89>, <"Beit Shemesh",22,9>};
param StartingCity := "Tel Aviv";
param NumberOfCitiesToVisit := 21;
param maximumVisits := card(Cities);

set CitiesNames := proj(Cities,<1>);
set Steps:= {1..maximumVisits};
set AllPossibleCombinations := {<i,a,b> in Steps * CitiesNames * CitiesNames | a != b};
var Edges[AllPossibleCombinations] binary;
var citiesToVisit integer >= max(1,min(NumberOfCitiesToVisit,maximumVisits)) <= maximumVisits;

subto StartFromStartingCity:
    sum <step,src,dest> in AllPossibleCombinations | step == 1 and src == StartingCity : Edges[step,src,dest] == 1;

subto EndInStartingCity:
    sum <src,secondCity,penultimateCity> in {<a,b,c> in CitiesNames * CitiesNames * CitiesNames | a!=b and a!=c} : 
        (Edges[1,src,secondCity] * (Edges[maximumVisits,penultimateCity,src] + Edges[NumberOfCitiesToVisit,penultimateCity,src])) == 1;

subto EachStepHappensUpToOnce:
    forall <step> in Steps | step < maximumVisits :
        sum <step2,src,dest> in AllPossibleCombinations | step2 == step : Edges[step2,src,dest] <= 1; 

subto VisitExactNumberOfCities:
    citiesToVisit == max(1,min(NumberOfCitiesToVisit,maximumVisits));

subto EnforceVisitExactNumberOfCities:
    sum <step, src,dest> in AllPossibleCombinations: Edges[step,src,dest] == citiesToVisit;

subto CantArriveSameCityTwice:
    forall <city> in CitiesNames:
        sum <step,src,dest> in AllPossibleCombinations | dest == city: Edges[step,src,dest]  <= 1;

subto CantDepartSameCityTwice:
    forall <city> in CitiesNames:
        sum <step,src,dest> in AllPossibleCombinations | src == city: Edges[step,src,dest]  <= 1;
 
subto CantStayInPlace:
    forall <step,src,dest> in AllPossibleCombinations | src == dest : Edges[step, src,dest] == 0;

subto DepartFromLastArrivedCity:
    forall <step,src,dest> in AllPossibleCombinations | step != 1 :
        vif Edges[step,src,dest] != 0 
        then sum <step2,src2,dest2> in AllPossibleCombinations | dest2 == src and step2 + 1 == step : Edges[step2,src2,dest2] == 1 end;
         
param distances[<src,x1,y1,dest,x2,y2> in Cities * Cities] := sqrt((x1-x2)**2 + (y1-y2)**2);

minimize distance:    
    sum <step,src,x1,y1,dest,x2,y2> in Steps * Cities * Cities | src != dest: 
        Edges[step,src,dest] * distances[src,x1,y1,dest,x2,y2]
    - ((citiesToVisit+1)*10)**2;
# 21 Cities
# one thread solving time: 497 seconds  (solved in vm, which is slower)

#Input fields: Cities, StartingCity, citiesToVisit
set Cities := {<"Tel Aviv",5,16>, <"Yafo",5,14>, <"Jerusalem",25,8>, <"Tveria",40,90>, <"Nahariya",6,95>, <"Eilat",30,-103>,
                <"Dimona",70,-20>, <"Ashkelon",2,-10>, <"Ashdod",3,3>, <"Beer Sheva",-7,-23>, <"Kiryat Arba",68,40>,
                <"Akko",23,85>, <"Hermon",80,130>, <"Beit Shean",75,50>, <"Katserin",50,95>, <"Ein Gedi",75,15>, <"Hadera",8,50>,
                <"Ramat Gan",6,17>, <"Kfar Saba",8,23>, <"Mitzpe Ramon",31,-80>, <"Yotvata",28,-89>,
                <"Beit Shemesh",22,9> };
param StartingCity := "Tel Aviv";
param citiesToVisit := card(Cities);

set CitiesNames := proj(Cities,<1>);
set Steps:= {1..citiesToVisit};
set AllPossibleCombinations := {<i,a,b> in Steps * CitiesNames * CitiesNames | a != b};
var Edges[AllPossibleCombinations] binary;
# var TotalSteps integer >= 0 <= citiesToVisit;


subto StartFromStartingCity:
    sum <step,src,dest> in AllPossibleCombinations | step == 1 and src == StartingCity : Edges[step,src,dest]   == 1;

subto EndInStartingCity:
    sum <step,src,dest> in AllPossibleCombinations | step != 1 and dest == StartingCity : Edges[step,src,dest]  == 1;

subto EachStepHappensAtMostOnce:
    forall <step> in Steps :
        sum <step2,src,dest> in AllPossibleCombinations | step2 == step : Edges[step2,src,dest]   <= 1;
                                                      
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
        then sum <step2,src2,dest2> in AllPossibleCombinations | src == dest2 and step2 + 1 == step : Edges[step2,src2,dest2] == 1 end;

subto EnforceStepsSequential:
    forall <step> in Steps:
        vif sum <step2,src,dest> in AllPossibleCombinations | step == step2 : Edges[step2,src,dest] == 0 
        then sum <step2,src,dest> in AllPossibleCombinations | step2 >= step : Edges[step2,src,dest] == 0 end;

# subto trial:
#     forall <step> in Steps:
#         vif step == TotalSteps + 1
#         then sum <step2,src,dest> in AllPossibleCombinations | step2 >= step : Edges[step2,src,dest] == 0 end;

# subto EnforceTotalStepsVariable:
#     TotalSteps == sum <step,src,dest> in AllPossibleCombinations: Edges[step,src,dest];

param citiesVisitedCoefficient := 1;         
param distances[<src,x1,y1,dest,x2,y2> in Cities * Cities] := sqrt((x1-x2)**2 + (y1-y2)**2);
param distancesCoefficient := 1;
param privilegedCitiesCoefficient := 1000;
param distanceNormalizationFactor := (sum <src,x1,y1,dest,x2,y2> in Cities*Cities | src == StartingCity : distances[src,x1,y1,dest,x2,y2]) / card(Cities) ;
set privilegedCities := {"Eilat"};

do print distanceNormalizationFactor;

minimize distance:    
    1*(sum <step,src,x1,y1,dest,x2,y2> in Steps * Cities * Cities | src != dest: 
        (Edges[step,src,dest] * distances[src,x1,y1,dest,x2,y2] * distancesCoefficient / distanceNormalizationFactor))
    

    - privilegedCitiesCoefficient*((sum <priv> in privilegedCities : 
                                        sum <step,src,dest> in AllPossibleCombinations | dest == priv :
                                             Edges[step,src,dest]))

    - (citiesVisitedCoefficient*(sum <step,src,dest> in AllPossibleCombinations: Edges[step,src,dest]));
    

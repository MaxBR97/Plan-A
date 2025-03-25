param InputLength := 30;
param BinarySecret := 0000101011; #43
param NoiseProbability := 0.05;
set TrainingData := {<1010,1>,
                    <0010,1>,
                    <0000,0>,
                    <1101,0>};
set EnumerateVectorDimension := {0..InputLength-1};
set RepresentSecret := { <bit> in  EnumerateVectorDimension : <bit ,(floor(BinarySecret/(10**bit)) mod 10) mod 2>};
set RepresentVectors := {<vector,bit> in {0..card(TrainingData)-1} * EnumerateVectorDimension : <vector,bit ,(floor(ord(TrainingData,vector+1,1) / (10 ** bit)) mod 10) mod 2>};
set RepresentResults := {<vector> in {0..card(TrainingData)-1} : <vector, ord(TrainingData,vector+1,2) mod 2 > };

param NumberOfExamples := 400;
# number is decimal number

defnumb GetBitValue(number,bit) := 
    floor(number / 2**bit) mod 2;
# do print GetBitValue(6,2); #1
# do print GetBitValue(6,1); #1
# do print GetBitValue(6,0); #0
# do print GetBitValue(1,0); #1

defnumb ConvertBinaryToDecimal(number) := 
    (sum <bit> in {0..InputLength-1} : (2**bit) * ((floor(number/(10**bit)) mod 10) mod 2) );

# do print ConvertBinaryToDecimal(001011); #11
# do print ConvertBinaryToDecimal(011011); #27

# input, secret are decimal numbers
defnumb CalculateParity2(input, secret, noise) :=
    ((sum <i> in {0..InputLength-1} : (GetBitValue(input,i) * GetBitValue(secret,i)) mod 2) + noise) mod 2;

# do print CalculateParity2(6,2,0); #1
# do print CalculateParity2(6,1,0); #0
# do print CalculateParity2(6,6,0); #0
# do print CalculateParity2(6,0,0); #0

# param GenerateSecret := random(0,(2**InputLength) - 1);

# <decimal_number,parity result>
set GeneratedData := {<vector> in {<vector> in {0..NumberOfExamples-1} : <floor(random(0,(2**InputLength) - 1))>} : <vector, CalculateParity2(vector,ConvertBinaryToDecimal(BinarySecret),floor(random(0+NoiseProbability, 0.999999999+NoiseProbability)))>};

set Labels := {"Estimated Vector", "Accuracy Probability"};

set AllPossibleSecrets := {<vector> in {0 .. (2**InputLength)-1}};

var PredictedNoiseLess[EnumerateVectorDimension] binary;

param TopResultsToSelect := 4;
var CalculateProbabilities real >= 0 <= 1;
var BestVectors integer >= -(2**(InputLength-1)) <= 2**(InputLength-1) priority 1000 startval 11 ;
var BestVectorBinary[{0..InputLength-1}] binary;
var CalculateResultForEachExample[{1..card(GeneratedData)}] integer;
var CalculateResultForEachExampleDivideByTwo[{1..card(GeneratedData)}] integer;
var CalculateCorrectlyMatchedExamples integer;

defnumb CalculateParitySingleBit(input, secret, noise) :=
    ((input mod 2 ) * (secret mod 2)) + noise;

subto Calculate_GaussianElimination:
    forall <input,result> in TrainingData:
        (sum <bit> in {0..InputLength-1} : (((floor(input/(10**bit)) mod 10) mod 2) * PredictedNoiseLess[bit])  ) == result;

subto BinaryRepresentation: 
    BestVectors == sum <bit> in {0..InputLength-1} : ((2**bit) * BestVectorBinary[bit]);

subto Calculate_Combinatorial_GeneratedData:
    forall <example> in {1..card(GeneratedData)}:
        CalculateResultForEachExample[example] == (sum <i> in {0..InputLength-1} : (GetBitValue(ord(GeneratedData,example,1),i) * BestVectorBinary[i])) - ord(GeneratedData,example,2);

subto CalculateDivideByTwo:
    forall <example> in {1..card(GeneratedData)}:
        CalculateResultForEachExampleDivideByTwo[example] <= (CalculateResultForEachExample[example]/2);

subto CalculateDivideByTwo2:
    forall <example> in {1..card(GeneratedData)}:
        CalculateResultForEachExampleDivideByTwo[example]*2 >= ((CalculateResultForEachExample[example]/2)-1);

subto CalculateCorrectlyMatchedExamples2:
    CalculateCorrectlyMatchedExamples == card(GeneratedData) - (sum <example> in {1..card(GeneratedData)} :
        (CalculateResultForEachExample[example] - (CalculateResultForEachExampleDivideByTwo[example]*2)));

subto CalculateProbabilities:
    CalculateProbabilities ==  (CalculateCorrectlyMatchedExamples)/ card(GeneratedData) * (1-NoiseProbability) + (1 - ((CalculateCorrectlyMatchedExamples)/ card(GeneratedData))) * (NoiseProbability);

minimize obj:
   - ((sum <example> in {1..card(GeneratedData)} : CalculateResultForEachExampleDivideByTwo[example])/card(GeneratedData))
   - ((CalculateProbabilities+1)*1000);


    
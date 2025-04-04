param InputLength := 14;
param BinarySecret := 001011;
param NoiseProbability := 0.1;
set TrainingData := {<1010,1>,
                    <0010,1>,
                    <0000,0>,
                    <1101,0>};
set EnumerateVectorDimension := {0..InputLength-1};
set RepresentSecret := { <bit> in  EnumerateVectorDimension : <bit ,(floor(BinarySecret/(10**bit)) mod 10) mod 2>};
set RepresentVectors := {<vector,bit> in {0..card(TrainingData)-1} * EnumerateVectorDimension : <vector,bit ,(floor(ord(TrainingData,vector+1,1) / (10 ** bit)) mod 10) mod 2>};
set RepresentResults := {<vector> in {0..card(TrainingData)-1} : <vector, ord(TrainingData,vector+1,2) mod 2 > };

param NumberOfExamples := 100;
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
set GeneratedData := {<vector> in {<vector> in {0..NumberOfExamples-1} : <floor(random(0,(2**InputLength) - 1))>} : <vector, CalculateParity2(vector,ConvertBinaryToDecimal(BinarySecret),floor(random(0+NoiseProbability, 0.99999+NoiseProbability)))>};


# set GeneratedVectors := {<vector,bit> in {0..NumberOfExamples-1} * EnumerateVectorDimension : <vector,bit ,floor(random(0.5,1.5))>};
# set GenerateSecret := { <bit> in  EnumerateVectorDimension : <bit ,floor(random(0.5,1.5))>};
# set GeneratedResults := {<vector> in {0..NumberOfExamples-1} : <vector,
#     (((sum <specVec, bit, value> in GenerateVectors | specVec == vector : 
#         (value * ord(RepresentSecret,bit+1,2))) + random(0 + NoiseProbability,0.99999999 + NoiseProbability)) mod 2)>};

set Labels := {"Estimated Vector", "Accuracy Probability"};

set AllPossibleSecrets := {<vector> in {0 .. (2**InputLength)-1}};

var PredictedNoiseLess[EnumerateVectorDimension] binary;

param TopResultsToSelect := 4;
var CalculateProbabilities[AllPossibleSecrets] real >= 0 <= 1;
var PredictedCombinatorialWithNoise[AllPossibleSecrets * EnumerateVectorDimension * {"0", "1"}] real >= 0 <= 1;
var SelectedVectors[AllPossibleSecrets] binary;

defnumb CalculateParitySingleBit(input, secret, noise) :=
    ((input mod 2 ) * (secret mod 2)) + noise;

subto Calculate_GaussianElimination:
    forall <input,result> in TrainingData:
        (sum <bit> in {0..InputLength-1} : (((floor(input/(10**bit)) mod 10) mod 2) * PredictedNoiseLess[bit])  ) == result;

subto Calculate_Combinatorial_GeneratedData:
    forall <possible_secret> in AllPossibleSecrets:
        CalculateProbabilities[possible_secret] ==
            sum <input,result> in GeneratedData :
                if CalculateParity2(input,possible_secret,0) == result then (1/card(GeneratedData))*(1-NoiseProbability) else  (1/card(GeneratedData))*NoiseProbability end;

subto SelectLimitedResults:
    (sum <possible_secret> in AllPossibleSecrets: SelectedVectors[possible_secret]) == TopResultsToSelect;

subto BinaryRepresentationOfSelectedResults:
    forall <possible_secret> in AllPossibleSecrets :
        forall <resultNum, bit, value> in AllPossibleSecrets * EnumerateVectorDimension * {"0", "1"} | possible_secret == resultNum and (GetBitValue(possible_secret,bit) == 0 and value == "0" or GetBitValue(possible_secret,bit) == 1 and value == "1"):
            PredictedCombinatorialWithNoise[resultNum,bit,value] == CalculateProbabilities[possible_secret] * SelectedVectors[possible_secret];

minimize obj:
   - sum <possible_secret> in AllPossibleSecrets: (SelectedVectors[possible_secret] * CalculateProbabilities[possible_secret]);


    
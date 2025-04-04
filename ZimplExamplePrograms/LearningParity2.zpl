param SecretLength :=9;
param BinarySecret := 000101011; #43
param GeneratedSecret := floor(random(0,2**(SecretLength)));
param NoiseProbability := 0.20;
param MaximumZerosPerExampleLine := 0;
param NumberOfNonZeroResults := 0;


# number is decimal number

defnumb GetBitValue(number,bit) := 
    floor(number / 2**bit) mod 2;
# do print GetBitValue(6,2); #1
# do print GetBitValue(6,1); #1
# do print GetBitValue(6,0); #0
# do print GetBitValue(1,0); #1

defnumb ConvertBinaryToDecimal(number) := 
    (sum <bit> in {0..SecretLength-1} : (2**bit) * ((floor(number/(10**bit)) mod 10) mod 2) );

# do print ConvertBinaryToDecimal(001011); #11
# do print ConvertBinaryToDecimal(011011); #27

# input, secret are decimal numbers
defnumb CalculateParity2(input, secret, noise) :=
    ((sum <i> in {0..SecretLength-1} : (GetBitValue(input,i) * GetBitValue(secret,i)) mod 2) + noise) mod 2;

# do print CalculateParity2(6,2,0); #1
# do print CalculateParity2(6,1,0); #0
# do print CalculateParity2(6,6,0); #0
# do print CalculateParity2(6,0,0); #0

set TrainingData := { <0010,1>,
                    <0010,1>,
                    <0000,0>,
                    <1101,0>};
param NumberOfExamples := 150;
# <generatedDecimalInput,generatedDecimalResult>
set GeneratedTrainingData := {<index,decimalVal> in {<index> in {1..NumberOfExamples} : <index,floor(random(0, 2**SecretLength))>} : <index,decimalVal,CalculateParity2(decimalVal,ConvertBinaryToDecimal(BinarySecret),floor(random(0,0.9999999+NoiseProbability)))>};
set EnumerateVectorDimension := {0..SecretLength-1};

# <row,value>
set RepresentUserSecret := { <bit> in  EnumerateVectorDimension : <bit ,(floor(BinarySecret/(10**bit)) mod 10) mod 2>};
# <row,col,value>
set RepresentGeneratedMatrix := {<row,bit> in {0..card(GeneratedTrainingData)-1} * EnumerateVectorDimension : <row,bit,GetBitValue(ord(GeneratedTrainingData,row+1,2),bit)>};
set RepresentUserMatrix := {<row,bit> in {0..card(TrainingData)-1} * EnumerateVectorDimension : <row,bit ,(floor(ord(TrainingData,row+1,1) / (10 ** bit)) mod 10)>};
# <row,value>
set RepresentGeneratedResults := {<row> in {0..card(GeneratedTrainingData)-1} : <row, ord(GeneratedTrainingData,row+1,3) mod 2 > };
set RepresentUserResults := {<row> in {0..card(TrainingData)-1} : <row, ord(TrainingData,row+1,2) mod 2 > };

# <rowIndex, decimal_value>
set GeneratedMatrixConvertedToDecimals := {<row,bit,value> in RepresentGeneratedMatrix : <row, (sum <row2, bit2,value2> in RepresentGeneratedMatrix | row == row2 : ((2 ** bit2) * value2))> };
set UserMatrixConvertedToDecimals := {<row,bit,value> in RepresentUserMatrix : <row, (sum <row2, bit2,value2> in RepresentUserMatrix | row == row2 : ((2 ** bit2) * value2))> };
param SecretConvertedToDecimal := sum <bit,value> in RepresentUserSecret: (value ** bit);

# <decimal_number,parity result>
set GeneratedDataPairs := {<row,decimalVal,row2,resVal> in GeneratedMatrixConvertedToDecimals * RepresentGeneratedResults | row == row2 : <row,decimalVal,resVal> };
# do print GeneratedTrainingData;
# do print "------------------";
# do print RepresentGeneratedMatrix;
# do print "------------------";
# do print GeneratedMatrixConvertedToDecimals;
# do print "------------------";
# do print RepresentGeneratedResults;
# do print "gre"+GeneratedDataPairs;
set Labels := {"Estimated Vector", "Accuracy Probability"};

set AllPossibleSecrets := {<vector> in {0 .. (2**SecretLength)}};

var PredictedNoiseLess[EnumerateVectorDimension] binary;

param TopResultsToSelect := 4;

var BestVector integer >= 0 <= 2**(SecretLength) priority 1000 startval 11 ;
var BestVectorBinary[{0..SecretLength-1}] binary;
var CalculateResultForEachExample[{1..card(GeneratedDataPairs)}] integer;
var CalculateResultForEachExampleDivideByTwo[{1..card(GeneratedDataPairs)}] integer;
var CalculateCorrectlyMatchedExamples integer;

defnumb CalculateParitySingleBit(input, secret, noise) :=
    ((input mod 2 ) * (secret mod 2)) + noise;

subto Calculate_GaussianElimination:
    forall <input,result> in TrainingData:
        (sum <bit> in {0..SecretLength-1} : (((floor(input/(10**bit)) mod 10) mod 2) * PredictedNoiseLess[bit])  ) == result;

subto BinaryRepresentation: 
    BestVector == sum <bit> in {0..SecretLength-1} : ((2**bit) * BestVectorBinary[bit]);

subto Calculate_Combinatorial_GeneratedDataPairs:
    forall <example> in {1..card(GeneratedDataPairs)}:
        CalculateResultForEachExample[example] == (sum <i> in {0..SecretLength-1} : (GetBitValue(ord(GeneratedDataPairs,example,2),i) * BestVectorBinary[i])) - ord(GeneratedDataPairs,example,3);

subto CalculateDivideByTwo:
    forall <example> in {1..card(GeneratedDataPairs)}:
        CalculateResultForEachExampleDivideByTwo[example] <= (CalculateResultForEachExample[example]/2);

subto CalculateDivideByTwo2:
    forall <example> in {1..card(GeneratedDataPairs)}:
        CalculateResultForEachExampleDivideByTwo[example]*2 >= ((CalculateResultForEachExample[example]/2)-1);

subto CalculateCorrectlyMatchedExamples2:
    CalculateCorrectlyMatchedExamples == card(GeneratedDataPairs) - (sum <example> in {1..card(GeneratedDataPairs)} :
        (CalculateResultForEachExample[example] - (CalculateResultForEachExampleDivideByTwo[example]*2)));

minimize obj:
   - ((sum <example> in {1..card(GeneratedDataPairs)} : CalculateResultForEachExampleDivideByTwo[example])/card(GeneratedDataPairs))
   - ((CalculateCorrectlyMatchedExamples+1)*1000);


    
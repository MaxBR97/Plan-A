# Play a sudoku game! 
# Choose a sudoku board size, and how many clues you would like to generate with the "Get A Clue" constraint.
# Click optimize to get an initial Game_Board, and its Solution.
# Play by filling numbers in the Game_Board. If you are stuck you can generate more clues with the "Get A Clue" constraint, or pick at the Solution table.
# When you have finished, select the entire Game_Board and click "Optimize". If solving status is "Optimal" -  you made it, if "Infeasible" you are mistaken. 
# Note: board size greater than 16 might get too hard to generate.


param stringToNumber[{"0","1","2","3","4","5","6","7","8","9"}] := <"0"> 0, <"1"> 1, <"2"> 2, <"3"> 3, <"4"> 4, <"5"> 5, <"6"> 6, <"7"> 7, <"8"> 8, <"9"> 9;
param toString[{0..9}] := <0> "0", <1> "1", <2> "2", <3> "3", <4> "4", 
                         <5> "5", <6> "6", <7> "7", <8> "8", <9> "9";

defnumb convertStringToNumber(str) := 
    sum <ind> in {0..length(str)-1} : (stringToNumber[substr(str,length(str) - 1 - ind,1)] * (10**ind));
do print "convertStringToNumber: ", convertStringToNumber("9");

defnumb isStringNumber(str) :=
    if length(str) == 0 then 0 else
    if (sum <ind> in {0..length(str)-1} : 
        card({<char> in {"0","1","2","3","4","5","6","7","8","9"} | char == substr(str,ind,1)})) == length(str) 
    then 1 else 0 end end;

defstrg numberToString(num) := 
    if num < 10 then toString[num] else
    if num < 100 then toString[floor(num/10)] + toString[num mod 10] else
    if num < 1000 then toString[floor(num/100)] + toString[floor((num mod 100)/10)] + toString[num mod 10] else
    "999"  # Fallback for very large numbers
    end end end;

# Sets for rows, columns and numbers
param sudoku_size := 9;
param box_size := sqrt(sudoku_size);
param empty_cell_char := "-";
do print "Sudoku Board Size Must be a Perfect Square and non-zero!";
do check sudoku_size > 0 and sudoku_size == floor(box_size) * floor(box_size);
set I := {1 .. sudoku_size};  # rows
set J := {1 .. sudoku_size};  # columns
set N := {1 .. sudoku_size};  # numbers

# Decision variables: x[i,j,n] = 1 if number n is placed at position (i,j)
var Solution[ <i,j,n> in I*J*N] binary priority round(random(100,1000)) * (i*j*n mod i*j);
set Nstr := {<i> in N : <numberToString(i)>};
var Game_Board[I*J*Nstr union I*J*{empty_cell_char}] binary priority round(random(1,100));

# Example Sudoku puzzle (0 represents empty cells)
# You can modify these values to solve different puzzles
set input := {<1,1,"1",1.0>, <2,2,"-",1.0>, <3,3,"13",1.0>, <4,4,"4",1.0>, <5,5,"5",1.0>, <6,6,"6",1.0>, <7,7,"7",1.0>, <8,8,"8",1.0>, <9,9,"9",1.0>};
set sanitized_input := {<i,j,n,val> in input | isStringNumber(n) == 1 : <i,j,convertStringToNumber(n),val>};

do print "Selected numbers must be between ",1,"-",sudoku_size,"!";
do forall <num> in {<i,j,n,val> in sanitized_input: <n>}
    do check num <= sudoku_size and num >= 1;

do print "Numbers should be placed between ",1,"-",sudoku_size,"!";
do forall <i,j> in {<i,j,n,val> in sanitized_input: <i,j>}
    do check i>=1 and i<=sudoku_size and j>=1 and j<=sudoku_size;

subto Set_Game_Board:
    forall <i,j,n> in I*J*N:
        Game_Board[i,j,numberToString(n)] <= Solution[i,j,n] and
        (sum <ind> in {1..sudoku_size}: Game_Board[i,j,numberToString(ind)]) + Game_Board[i,j,empty_cell_char] == 1;

# Each cell must contain exactly one number
subto one_number_per_cell:
    forall <i,j> in I*J:
        sum <n> in N: Solution[i,j,n] == 1;

# Each number must appear exactly once in each row
subto one_number_per_row:
    forall <i,n> in I*N:
        sum <j> in J: Solution[i,j,n] == 1;

# Each number must appear exactly once in each column
subto one_number_per_column:
    forall <j,n> in J*N:
        sum <i> in I: Solution[i,j,n] == 1;

# Each number must appear exactly once in each 3x3 box
subto one_number_per_box:
    forall <n> in N:
        forall <bi> in {0 to box_size-1}:
            forall <bj> in {0 to box_size-1}:
                sum <i> in {box_size*bi+1 to box_size*bi+box_size}:
                    sum <j> in {box_size*bj+1 to box_size*bj+box_size}: Solution[i,j,n] == 1;

# Fix the given numbers
subto fix_given_numbers:
    forall <i,j,num,val> in sanitized_input:
        Solution[i,j,num] == round(val) and 
        Game_Board[i,j,numberToString(num)] == round(val);

param fill_up_to := 19;
subto Fill_Fixed_Number_Of_Squares:
    sum <i,j,n> in I*J*N: Game_Board[i,j,numberToString(n)] == min(card(sanitized_input) + fill_up_to, sudoku_size**2);

do print "Number of cells to be filled must be between 0 and ",sudoku_size**2," !";
do check fill_up_to <= card(I)*card(J) and fill_up_to >= 0;

maximize objective: 
    sum <i,j,n> in I*J*N: Solution[i,j,n] +
    sum <i,j,n> in I*J*N: Game_Board[i,j,numberToString(n)];


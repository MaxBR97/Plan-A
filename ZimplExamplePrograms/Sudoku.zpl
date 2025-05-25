# Sudoku solver in ZIMPL
# Sets for rows, columns and numbers
param sudoku_size := 9;
param box_size := 3; 

set I := {1 .. sudoku_size};  # rows
set J := {1 .. sudoku_size};  # columns
set N := {1 .. sudoku_size};  # numbers

# Decision variables: x[i,j,n] = 1 if number n is placed at position (i,j)
var board[I*J*N] binary;

# Example Sudoku puzzle (0 represents empty cells)
# You can modify these values to solve different puzzles
set input := {<1,1,5,1.0>, <6,4,6,1.0>, <6,6,5,1.0>, <6,8,3,1.0>, <6,9,9,1.0>, <7,2,6,1.0>, <7,4,8,1.0>, <7,6,2,1.0>, <7,8,7,1.0>, <7,9,1,1.0>, <8,1,2,1.0>, <8,3,7,1.0>, <8,5,4,1.0>, <9,8,2,1.0>, <9,9,5,1.0>};

# Each cell must contain exactly one number
subto one_number_per_cell:
    forall <i,j> in I*J:
        sum <n> in N: board[i,j,n] == 1;

# Each number must appear exactly once in each row
subto one_number_per_row:
    forall <i,n> in I*N:
        sum <j> in J: board[i,j,n] == 1;

# Each number must appear exactly once in each column
subto one_number_per_column:
    forall <j,n> in J*N:
        sum <i> in I: board[i,j,n] == 1;

# Each number must appear exactly once in each 3x3 box
subto one_number_per_box:
    forall <n> in N:
        forall <bi> in {0 to box_size-1}:
            forall <bj> in {0 to box_size-1}:
                sum <i> in {box_size*bi+1 to box_size*bi+box_size}:
                    sum <j> in {box_size*bj+1 to box_size*bj+box_size}: board[i,j,n] == 1;

# Fix the given numbers
subto fix_given_numbers:
    forall <i,j,num,val> in input:
        board[i,j,num] == val;

minimize objective: 
    sum <i,j,n> in I*J*N: board[i,j,n];



param boardSize:=8;
set board := {0..boardSize} * {0..boardSize};
param queen_x := 3;
param queen_y := 2;

set pawns := {<2,0>, <1,2>, <4,3>, <2,3>};

param diagonal_one := min <dist> in proj({<x,y,dist> in pawns*{0..boardSize} | x + dist == queen_x and y + dist == queen_y },<3>) union {min(queen_x, queen_y)}: dist; #down_left
param diagonal_two := min <dist> in proj({<x,y,dist> in pawns*{0..boardSize} | x + dist == queen_x and y - dist == queen_y },<3>) union {min(queen_x, boardSize - queen_y)}: dist; #up_left
param diagonal_three := min <dist> in proj({<x,y,dist> in pawns*{0..boardSize} | x - dist == queen_x and y + dist == queen_y },<3>) union {min(boardSize - queen_x, queen_y)}: dist;#down_right
param diagonal_four := min <dist> in proj({<x,y,dist> in pawns*{0..boardSize} | x - dist == queen_x and y - dist == queen_y },<3>) union {min(boardSize - queen_x, boardSize - queen_y)}: dist;#up_right
do print diagonal_one;
do print diagonal_two;
do print diagonal_three;
do print diagonal_four;

param row_one := min <dist> in proj({<x,y,dist> in pawns*{0..boardSize} | x + dist == queen_x and y == queen_y },<3>) union {queen_x} : dist;
param row_two := abs ( max <dist> in proj({<x,y,dist> in pawns*{0..(-1* boardSize) by -1} | x + dist == queen_x and y == queen_y },<3>) union {- boardSize + queen_x}: dist);
# do print row_two;
param col_one := min <dist> in proj({<x,y,dist> in pawns*{0..boardSize} | x == queen_x and y + dist == queen_y },<3>) union {queen_y}: dist;
param col_two := abs (max <dist> in proj({<x,y,dist> in pawns*{0..(-1* boardSize) by -1} | x == queen_x and y + dist == queen_y },<3>) union {- boardSize + queen_y}: dist);

# do print col_two;
# set diagonal := {<x,y,dist>  in board * {(-1* boardSize)..boardSize} | x == queen_x + dist and y == queen_y + dist and x >1 and y>1 and x<= boardSize and y <= boardSize};
# set row := {<x,y,dist>  in board * {(-1* boardSize)..boardSize} | x == queen_x + dist and y == queen_y and x >1 and y>1 and x<= boardSize and y <= boardSize};
# set col := {<x,y,dist>  in board * {(-1* boardSize)..boardSize} | x == queen_x and y == queen_y + dist and x >1 and y>1 and x<= boardSize and y <= boardSize};
var queenPos[{queen_x} * {queen_y}] binary;
var addedPawns[pawns] binary;
var possibleMoves[{"col","row", "diagonal", "total"}] integer;
var visualizeBoard[board * {"O","PAWN","QUEEN"}] binary startval 0;

subto constCol:
    possibleMoves["col"] == col_one + col_two;

subto constRow:
    possibleMoves["row"] == row_one + row_two; 

subto constDiag:
    possibleMoves["diagonal"] == diagonal_one + diagonal_two +diagonal_three + diagonal_four;

subto constTotal:
    possibleMoves["total"] == possibleMoves["col"] +  possibleMoves["row"] +  possibleMoves["diagonal"];

subto assignPawns:
    forall <x,y> in pawns:
        visualizeBoard[x,y,"PAWN"] == 1;

subto assignQueen:
    visualizeBoard[queen_x,queen_y,"QUEEN"] == 1;

subto assignEmptySquares:
    forall <x,y> in board:
        visualizeBoard[x,y,"O"] + visualizeBoard[x,y,"QUEEN"] + visualizeBoard[x,y,"PAWN"] == 1;

minimize ob:
    sum <x,y,t> in  board * {"PAWN","QUEEN"}: visualizeBoard[x,y,t];
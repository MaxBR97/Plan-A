# Chess Game Model - Multiple Move Determination
# This model takes the current board state and determines a sequence of valid moves

# Define the board coordinates
set ROWS := {1 to 8};
set COLS := {1 to 8};
set BOARD := ROWS * COLS;

# Piece types
set PIECE_TYPES := {"PAWN", "KNIGHT", "BISHOP", "ROOK", "QUEEN", "KING"};
set COLORS := {"WHITE", "BLACK"};

        
# Initial chess position as <row,col,figure,color>
set INITIAL_POSITIONS := {
    # White pieces back row
    <1,1,"ROOK","WHITE">,   <1,8,"ROOK","WHITE">,
    <1,2,"KNIGHT","WHITE">, <1,7,"KNIGHT","WHITE">,
    <1,3,"BISHOP","WHITE">, <1,6,"BISHOP","WHITE">,
    <1,4,"QUEEN","WHITE">,  <1,5,"KING","WHITE">,
    
    # White pawns
    # <2,1,"PAWN","WHITE">, <2,2,"PAWN","WHITE">,
    # <2,3,"PAWN","WHITE">, <2,4,"PAWN","WHITE">,
    <2,5,"PAWN","WHITE">, <2,6,"PAWN","WHITE">,
    <2,7,"PAWN","WHITE">, <2,8,"PAWN","WHITE">,
    
    # Black pieces back row
    <8,1,"ROOK","BLACK">,   <8,8,"ROOK","BLACK">,
    <8,2,"KNIGHT","BLACK">, <8,7,"KNIGHT","BLACK">,
    <8,3,"BISHOP","BLACK">, <8,6,"BISHOP","BLACK">,
    <8,4,"QUEEN","BLACK">,  <8,5,"KING","BLACK">,
    
    # Black pawns
    # <7,1,"PAWN","BLACK">, <7,2,"PAWN","BLACK">,
    # <7,3,"PAWN","BLACK">, <7,4,"PAWN","BLACK">,
    <7,5,"PAWN","BLACK">, <7,6,"PAWN","BLACK">,
    <7,7,"PAWN","BLACK">, <7,8,"PAWN","BLACK">
};


# Parameter to determine which color to move (1 for white, 0 for black)
param computer_plays_as_white := 1;  # Change this to 0 to play as black

# Parameter to determine how many future moves to calculate
param future_depth := 3;  # Number of future moves to consider

# Set of move numbers for future moves
set FUTURE_MOVES := {0 to future_depth};

# Movement validation functions
# These only check if the move pattern is valid, ignoring other pieces

# Determine which color plays at each move number
# When computer_plays_as_white == 1:
#   White plays on odd moves (1,3,5...)
#   Black plays on even moves (2,4,6...)
# When computer_plays_as_white == 0:
#   Black plays on odd moves (1,3,5...)
#   White plays on even moves (2,4,6...)
defnumb is_playing_at_move(move,color) :=
    if computer_plays_as_white == 1 then
        if move mod 2 == 1 and color == "WHITE" then 1
        else if move mod 2 == 0 and color == "BLACK" then 1
        else 0 end end
    else
        if move mod 2 == 1 and color == "BLACK" then 1
        else if move mod 2 == 0 and color == "WHITE" then 1
        else 0 end end
    end;

# Check if coordinates are within board bounds
defnumb is_valid_position(r, c) :=
    if r >= 1 and r <= 8 and c >= 1 and c <= 8 then 1 else 0 end;

# Pawn move validation (without capture)
defnumb valid_pawn_move(r, c, dest_r, dest_c, is_white) :=
    if is_valid_position(dest_r, dest_c) == 0  then 0
    else            
        if is_white == "WHITE" then
            if dest_c != c then 0  # Must stay in same column
            else if r == 2 and dest_r == 4 and dest_c == c then 1  # First move two squares
            else if dest_r == r + 1 and dest_c == c then 1  # Regular move
            else 0 end end end
        else  # Black pawn
            if dest_c != c then 0  # Must stay in same column
            else if r == 7 and dest_r == 5 and dest_c == c then 1  # First move two squares
            else if dest_r == r - 1 and dest_c == c then 1  # Regular move
            else 0 end end end
        end
    end;

# Pawn capture validation
defnumb valid_pawn_eat_move(r, c, dest_r, dest_c, is_white) := 
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if is_white == "WHITE" then
            if dest_r == r + 1 and (dest_c == c + 1 or dest_c == c - 1) then 1
            else 0 end
        else
            if dest_r == r - 1 and (dest_c == c + 1 or dest_c == c - 1) then 1
            else 0 end
        end
    end;

# Rook move validation
defnumb valid_rook_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if r == dest_r or c == dest_c then 1
        else 0 end
    end;

# Bishop move validation
defnumb valid_bishop_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if abs(dest_r - r) == abs(dest_c - c) then 1
        else 0 end
    end;

# Knight move validation
defnumb valid_knight_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if (abs(dest_r - r) == 2 and abs(dest_c - c) == 1) or
           (abs(dest_r - r) == 1 and abs(dest_c - c) == 2) then 1
        else 0 end
    end;

# Queen move validation (combination of rook and bishop)
defnumb valid_queen_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if r == dest_r or c == dest_c or abs(dest_r - r) == abs(dest_c - c) then 1
        else 0 end
    end;

# King move validation
defnumb valid_king_move(r, c, dest_r, dest_c) :=
    if is_valid_position(dest_r, dest_c) == 0 then 0
    else
        if abs(dest_r - r) <= 1 and abs(dest_c - c) <= 1 then 1
        else 0 end
    end;


defstrg piece_display(type,color) := substr(color,0,1) + "_" + substr(type,0,10);
defstrg piece_display_reverse_piece(str) := substr(str,2,10); 
defstrg piece_display_reverse_color(str) := if substr(str,0,1) == "W" then "WHITE" else "BLACK" end; 

# Last move positions (current board state)
# Format: <row, col, piece_type, value>
set LAST_MOVE_POSITIONS_INPUT := {<4,8,"O",1>, <4,6,"O",1>, <2,1,"O",1>, <4,4,"O",1>, <6,7,"O",1>, <2,3,"O",1>, <4,2,"O",1>, <6,5,"O",1>, <8,6,"B_BISHOP",1>, <7,7,"B_PAWN",1>, <8,4,"W_QUEEN",1>, <7,5,"B_PAWN",1>, <1,2,"W_KNIGHT",1>, <1,1,"W_ROOK",1>, <1,3,"W_BISHOP",1>, <8,5,"B_KING",1>, <6,1,"O",1>, <3,8,"O",1>, <3,6,"O",1>, <5,7,"O",1>, <5,5,"O",1>, <3,4,"O",1>, <5,3,"O",1>, <3,2,"O",1>, <8,8,"B_ROOK",1>, <5,1,"O",1>, <7,4,"O",1>, <7,2,"O",1>, <2,4,"O",1>, <4,7,"O",1>, <6,8,"O",1>, <4,5,"O",1>, <6,6,"O",1>, <2,2,"O",1>, <4,3,"O",1>, <6,4,"O",1>, <7,8,"B_PAWN",1>, <1,7,"W_KNIGHT",1>, <7,6,"B_PAWN",1>, <8,3,"B_BISHOP",1>, <8,7,"B_KNIGHT",1>, <4,1,"O",1>, <6,2,"O",1>, <1,8,"W_ROOK",1>, <2,7,"W_PAWN",1>, <2,5,"W_PAWN",1>, <3,5,"O",1>, <5,8,"O",1>, <3,3,"O",1>, <5,6,"O",1>, <1,6,"W_BISHOP",1>, <3,1,"O",1>, <5,4,"O",1>, <8,2,"B_KNIGHT",1>, <5,2,"O",1>, <7,3,"O",1>, <7,1,"O",1>, <8,1,"B_ROOK",1>, <3,7,"W_PAWN",1>, <2,6,"O",1>, <2,8,"O",1>, <6,3,"W_PAWN",1>, <1,5,"O",1>, <1,4,"W_KING",1>};

set LAST_MOVE_POSITIONS := {<r,c,piece,value> in LAST_MOVE_POSITIONS_INPUT | piece != "O" : <r,c,piece_display_reverse_piece(piece),piece_display_reverse_color(piece)>};
do print LAST_MOVE_POSITIONS;

set CURRENT_PIECES := proj(LAST_MOVE_POSITIONS,<3,4>);

set PIECES_DISPLAY := {<type,color> in PIECE_TYPES * COLORS: <piece_display(type,color)>} union {"O"};

# Variables for board states at different moves
# Move 0: Current position (last_move)
var last_move[BOARD * PIECES_DISPLAY] binary;

# Move 1: Next immediate move
var next_move[BOARD * PIECES_DISPLAY] binary;

# Moves 2 to future_depth: Future positions
var future_moves[FUTURE_MOVES * BOARD * PIECE_TYPES * COLORS] binary;

subto fill_empty_squares_last_move: forall <r,c> in BOARD:
    last_move[r,c,"O"] + (sum <t,color> in PIECE_TYPES * COLORS: last_move[r,c,piece_display(t,color)]) == 1;

subto fill_empty_squares_next_move: forall <r,c> in BOARD:
    next_move[r,c,"O"] + (sum <t,color> in PIECE_TYPES * COLORS: next_move[r,c,piece_display(t,color)]) == 1;

# Initialize last_move to match LAST_MOVE_POSITIONS

subto init_last_move: forall <r,c,t,color> in BOARD * PIECE_TYPES * COLORS | card(LAST_MOVE_POSITIONS) > 0:
        last_move[r,c,piece_display(t,color)] == (sum <r2,c2,t2,color2> in LAST_MOVE_POSITIONS | r2 == r and c2 == c and t2 == t and color2 == color: 1);

subto init_last_move2:
    forall <r2,c2,t,color> in BOARD * PIECE_TYPES * COLORS:
        last_move[r2,c2,piece_display(t,color)] == future_moves[0,r2,c2,t,color];

subto init_next_move:
    forall <r2,c2,t,color> in BOARD * PIECE_TYPES * COLORS:
        next_move[r2,c2,piece_display(t,color)] == future_moves[1,r2,c2,t,color];

subto init_new_game: 
    forall <r,c,t,color> in INITIAL_POSITIONS  | card(LAST_MOVE_POSITIONS) == 0:
        future_moves[0,r,c,t,color] == 1;

subto init_new_game2: 
    forall <r,c,t,color> in (BOARD * PIECE_TYPES * COLORS) without INITIAL_POSITIONS | card(LAST_MOVE_POSITIONS) == 0:
        future_moves[0,r,c,t,color] == 0;
    
# player whose playing will move exactly once per move
subto playing_side_moves_once: forall <move> in FUTURE_MOVES | move > 0 :
    (sum <t,color> in PIECE_TYPES * COLORS | is_playing_at_move(move,color) == 1:
        sum <r2,c2> in BOARD:
            ((future_moves[move-1,r2,c2,t,color])*(1-future_moves[move,r2,c2,t,color]))  ) == 1;

# the player whose playing will have the same amount of figures of each type as he had at the previous move
subto playing_side_moves_once2: forall <move> in FUTURE_MOVES | move > 0:
    forall <piece,color> in PIECE_TYPES * COLORS | is_playing_at_move(move,color) == 1:
        (sum <r2,c2> in BOARD: (future_moves[move-1,r2,c2,piece,color] - future_moves[move,r2,c2,piece,color]) ) == 0;

# the player whose not playing will have his figures stay in place, or exactly one figure eaten.
subto passive_side_stays_in_place: forall <move> in FUTURE_MOVES | move > 0 :
    forall <r2,c2, t,color> in BOARD * PIECE_TYPES * COLORS | is_playing_at_move(move,color) == 0:
            vif future_moves[move-1,r2,c2,t,color] == 1 then
                future_moves[move,r2,c2,t,color] + (sum <r3,c3,t3,color3> in BOARD * PIECE_TYPES * COLORS | r3 == r2 and c3 == c2 and is_playing_at_move(move,color3) == 1 : future_moves[move,r3,c3,t3,color3]) == 1
                else future_moves[move,r2,c2,t,color] == 0 end;
            

subto playing_side_will_eat_figure_where_he_lands: forall <move> in FUTURE_MOVES | move > 0 :
    forall <row,col,t,color> in BOARD * PIECE_TYPES * COLORS | is_playing_at_move(move,color) == 1:
        vif future_moves[move,row,col,t,color] == 1 then
        (sum <row2,col2,t2,color2> in BOARD * PIECE_TYPES * COLORS | row2 == row and col2 == col and is_playing_at_move(move,color2) == 0: future_moves[move,row2,col2,t2,color2]) == 0 end;


subto pawn_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <r2,c2,t,color> in BOARD * PIECE_TYPES * COLORS | t == "PAWN" and is_playing_at_move(move,color) == 1:
        vif future_moves[move-1,r2,c2,t,color] * (1-future_moves[move,r2,c2,t,color]) == 1 then 
        (sum <r3,c3> in BOARD |  (valid_pawn_move(r2, c2, r3, c3, color) == 1 or valid_pawn_eat_move(r2, c2, r3, c3, color) == 1 or (r2 == r3 and c2 == c3)) :
            future_moves[move,r3,c3,t,color] ) == 1 end;

subto queen_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <r2,c2,t,color> in BOARD * PIECE_TYPES * COLORS | t == "QUEEN" and is_playing_at_move(move,color) == 1:
        vif future_moves[move-1,r2,c2,t,color] * (1-future_moves[move,r2,c2,t,color]) == 1 then 
        (sum <r3,c3> in BOARD |  (valid_queen_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            future_moves[move,r3,c3,t,color] ) == 1 end;

subto rook_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <r2,c2,t,color> in BOARD * PIECE_TYPES * COLORS | t == "ROOK" and is_playing_at_move(move,color) == 1:
        vif future_moves[move-1,r2,c2,t,color] * (1-future_moves[move,r2,c2,t,color]) == 1 then 
        (sum <r3,c3> in BOARD |  (valid_rook_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            future_moves[move,r3,c3,t,color] ) == 1 end;

subto bishop_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <r2,c2,t,color> in BOARD * PIECE_TYPES * COLORS | t == "BISHOP" and is_playing_at_move(move,color) == 1:
        vif future_moves[move-1,r2,c2,t,color] * (1-future_moves[move,r2,c2,t,color]) == 1 then 
        (sum <r3,c3> in BOARD |  (valid_bishop_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            future_moves[move,r3,c3,t,color] ) == 1 end;

subto knight_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <r2,c2,t,color> in BOARD * PIECE_TYPES * COLORS | t == "KNIGHT" and is_playing_at_move(move,color) == 1:
        vif future_moves[move-1,r2,c2,t,color] * (1-future_moves[move,r2,c2,t,color]) == 1 then 
        (sum <r3,c3> in BOARD |  (valid_knight_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            future_moves[move,r3,c3,t,color] ) == 1 end;

subto king_move: forall <move> in FUTURE_MOVES | move > 0:
    forall <r2,c2,t,color> in BOARD * PIECE_TYPES * COLORS | t == "KING" and is_playing_at_move(move,color) == 1:
        vif future_moves[move-1,r2,c2,t,color] * (1-future_moves[move,r2,c2,t,color]) == 1 then 
        (sum <r3,c3> in BOARD |  (valid_king_move(r2, c2, r3, c3) == 1 or (r2 == r3 and c2 == c3)) :
            future_moves[move,r3,c3,t,color] ) == 1 end;

param pawn_cost := 1;
param knight_cost := 3;
param bishop_cost := 3;
param rook_cost := 5;
param queen_cost := 9;
param king_cost := 1000;

param decayFactor := 0.8;

maximize obj:
    sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "PAWN":
        future_moves[move,r,c,t,color] * pawn_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "KNIGHT":
        future_moves[move,r,c,t,color] * knight_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "BISHOP":
        future_moves[move,r,c,t,color] * bishop_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "ROOK":
        future_moves[move,r,c,t,color] * rook_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "QUEEN":
        future_moves[move,r,c,t,color] * queen_cost * decayFactor^move
    + sum <move,r,c,t,color> in FUTURE_MOVES * BOARD * CURRENT_PIECES | is_playing_at_move(move,color) == 1 and t == "KING":
        future_moves[move,r,c,t,color] * king_cost * decayFactor^move;
